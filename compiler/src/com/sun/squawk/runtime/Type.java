package com.sun.squawk.runtime;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;

import java.io.IOException;
import java.util.Vector;

public class Type extends com.sun.squawk.irvm.BasicObject implements RuntimeConstants {

   /* ------------------------------------------------------------------------ *\
    *                         Manifest class definitions                       *
   \* ------------------------------------------------------------------------ */

   /*
    * The route of all data types
    */
    public static Type UNIVERSE;

   /*
    * Primitive data types
    */
    public static Type PRIMITIVE, BOOLEAN, BYTE, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, VOID;

   /*
    * Objects
    */
    public static Type OBJECT, STRING, CLASS, THREAD, SYSTEM, ARRAY, STRINGBUFFER;

   /*
    * Special objects used by the verifier
    */
    public static Type BOGUS, NULLOBJECT, INITOBJECT, NEWOBJECT, LONG2, DOUBLE2;

   /*
    * Throwables
    */
    public static Type THROWABLE, ERROR, EXCEPTION, NOMEMORYERROR;

   /*
    * Arrays
    */
    public static Type BOOLEAN_ARRAY, BYTE_ARRAY,  CHAR_ARRAY,   SHORT_ARRAY,  INT_ARRAY,
                       LONG_ARRAY,    FLOAT_ARRAY, DOUBLE_ARRAY, OBJECT_ARRAY, STRING_ARRAY;

   /**
    * Initialize this class
    */
    static void initialize(VirtualMachine vm) {

       /*
        * The route of all data types
        */
        UNIVERSE        = Type.create(null,         "U");

       /*
        * Unreal data types
        */
        VOID            = Type.create(UNIVERSE,     "V");
        PRIMITIVE       = Type.create(UNIVERSE,     "P");
        BOGUS           = Type.create(UNIVERSE,     "X");           // An invalid type
        LONG2           = Type.create(UNIVERSE,     "J2");          // Second word of a long
        DOUBLE2         = Type.create(UNIVERSE,     "D2");          // Second word of a double

       /*
        * Primitive data types
        */
        INT             = Type.create(PRIMITIVE,    "I");
        LONG            = Type.create(PRIMITIVE,    "J");
        FLOAT           = Type.create(PRIMITIVE,    "F");
        DOUBLE          = Type.create(PRIMITIVE,    "D");
        BOOLEAN         = Type.create(INT,          "Z");
        CHAR            = Type.create(INT,          "C");
        SHORT           = Type.create(INT,          "S");
        BYTE            = Type.create(INT,          "B");

       /*
        * Objects
        */
        OBJECT          = Type.create(UNIVERSE,     "Ljava/lang/Object;");
        CLASS           = Type.create(OBJECT,       "Ljava/lang/Class;");
        STRING          = Type.create(OBJECT,       "Ljava/lang/String;");
        CLASS           = Type.create(OBJECT,       "Ljava/lang/Class;");
        THREAD          = Type.create(OBJECT,       "Ljava/lang/Thread;");
        SYSTEM          = Type.create(OBJECT,       "Ljava/lang/System;");
        STRINGBUFFER    = Type.create(OBJECT,       "Ljava/lang/StringBuffer;");

       /*
        * Special objects used by the verifier
        */
        NULLOBJECT      = Type.create(OBJECT,       "-NULL-");      // Result of an acoust_null
        INITOBJECT      = Type.create(OBJECT,       "-INIT-");      // "this" in <init> before call to super()
        NEWOBJECT       = Type.create(OBJECT,       "-NEW-");       // Result of "new" before call to <init>

       /*
        * Throwables
        */
        THROWABLE       = Type.create(OBJECT,       "Ljava/lang/Throwable;");
        ERROR           = Type.create(THROWABLE,    "Ljava/lang/Error;");
        EXCEPTION       = Type.create(THROWABLE,    "Ljava/lang/Exception;");
        //NOMEMORYERROR   = Type.create(ERROR,        "Ljava/lang/OutOfMemoryError;");

       /*
        * Arrays
        */
        ARRAY           = Type.create(OBJECT,       "A");

        BOOLEAN_ARRAY   = BOOLEAN.asArray();
        BYTE_ARRAY      = BYTE.asArray();
        CHAR_ARRAY      = CHAR.asArray();
        SHORT_ARRAY     = SHORT.asArray();
        INT_ARRAY       = INT.asArray();
        LONG_ARRAY      = LONG.asArray();
        FLOAT_ARRAY     = FLOAT.asArray();
        DOUBLE_ARRAY    = DOUBLE.asArray();
        OBJECT_ARRAY    = OBJECT.asArray();

       /*
        * Fudge to make the type of Type.OBJECT and Type.CLASS equal to Type.CLASS
        */
        OBJECT.tweakMetatype(CLASS);
        CLASS.tweakMetatype(CLASS);
        //CLASS.load();
       // CLASS.convert();
    }

   /* ------------------------------------------------------------------------ *\
    *                              Static functions                            *
   \* ------------------------------------------------------------------------ */

   /**
    * Static constructor only called from the code above
    */
    private static Type create(Type superType, String name) {
        return VirtualMachine.TopVM().createType(superType, name);
    }


   /**
    * Get an object array type for named type
    */
    private static Type getArraySuperTypeFor(String name) {
        int dims = 0;
        while (name.charAt(dims) == '[') {
             dims++;
        }

        String basicTypeName;
        Type superType;

        if (name.charAt(dims) != 'L' || name.endsWith("Ljava/lang/Object;")) {
            dims--;
            superType = Type.OBJECT;
        } else {
            if (dims == 1) {
               /*
                * Apart from being faster, this solves a problem where [Lfoo; is being created before Lfoo;
                * has finished being loaded. Presumable there is still a problem when [[Lfoo; is required.
                */
                //return OBJECT_ARRAY;
                // however : javasoft.sqe.tests.api.java.lang.Class.IsAssignableFromTests Fails
            }
            basicTypeName = name.substring(dims);
            Type basicType = Type.create(null, basicTypeName);
            basicType.load();
            superType = basicType.superType();
        }

        while (dims-- > 0) {
            superType = superType.asArray();
        }
//prtn("getArraySuperTypeFor "+name+" dims "+dims+" gets "+  superType);
        return superType;
    }


   /* ------------------------------------------------------------------------ *\
    *                               Type definiion                             *
   \* ------------------------------------------------------------------------ */

    public final static int DEFINED         = 1;
    public final static int LOADING         = 2;
    public final static int LOADED          = 3;
    public final static int CONVERTING      = 4;
    public final static int CONVERTED       = 5;
    public final static int INITIALIZING    = 6;
    public final static int INITIALIZED     = 7;
    public final static int FAILED          = 8;

   /**
    * The state of the class
    */
    private int state = DEFINED;

   /**
    * The entire name of the type (e.g "Ljava/lang/Object;")
    */
    private String name;

   /**
    * The VM this type was defined to be in
    */
    private VirtualMachine vm;

   /**
    * The superType
    */
    private Type superType;

   /**
    * An array's element type
    */
    private Type elementType;

   /**
    * Access flags
    */
    private int flags = -1;

   /**
    * Interface types implemented by this type
    */
    private Type[] interfaces = VirtualMachine.ZEROTYPES;

   /**
    * Fields implemented by this type
    */
    private Field[] fields;

   /**
    * Methods implemented by this type
    *
    * This array contains one entry for all the methods defined by this class
    * The array starts with the lowsest method slot number and ends with the
    * highest. If this class defines a method that replaces one in the hierarchy
    * then there will be entries missing. These are filled in with the corrosponding
    * entries from hierarchy. This is rather like vtables but in terms of intervals.
    * In a collection of classes with 1146 methods regular vtables would add up to
    * about 4400 entries. Doing things this way cuts this down to about 1600.
    */
    private Method[] methods;

   /**
    * The <clinit> method (or null)
    */
    private Method clinit;

   /**
    * Inferface methods implemented by this type
    */
    private short[] interfaceTable;

   /**
    * The size of an instance in words
    */
    private int instanceSize;

   /**
    * The method slot table size
    */
    private int methodTableSize;


   /* ------------------------------------------------------------------------ *\
    *                               Class creation                             *
   \* ------------------------------------------------------------------------ */

   /**
    * Static constructor only called from the VirtualMachine.java
    */
    static Type createForVM(VirtualMachine vm, Type superType, String name) {
        return new Type(vm, superType, name);
    }

   /**
    * Disable the default constructor
    */
    private Type() {
        super(null);
        shouldNotReachHere();
    }

   /**
    * Private constructor
    */
    protected Type(VirtualMachine vm, Type superType, String name) {
        super(CLASS);
//System.out.println("Type = "+name +" meta ="+CLASS);
//        assume(vm != null);
        this.vm          = vm;
        this.superType   = superType;
        this.name        = name;
        if (name.charAt(0) != 'L' || isArray()) {
            flags = ACC_PUBLIC;
        }
    }

   /**
    * Semi-public static constructor
    */
    Type createType(String name) {
        return createType(null, name);
    }

   /**
    * Semi-public static constructor
    */
    Type createType(Type superType, String name) {
        return vm.createType(superType, name);
    }

   /* ------------------------------------------------------------------------ *\
    *                     Class loading and initialization                     *
   \* ------------------------------------------------------------------------ */

    static Type convertionInProgress = null;
    static Vector  convertQueue = new Vector();

   /**
    * Execute or queue a type's convertion
    */
    private static void queueConvertion(Type type) {
       if (convertionInProgress == null) {
           convertType(type);
       } else {
           convertQueue.addElement(type);
       }
    }

   /**
    * Convert the methods in a a class
    */
    private static void convertType(Type type) {

        try {
            assume(convertionInProgress == null,"type="+convertionInProgress);
            convertionInProgress = type;

            while(true) {
                type.convertMain();
                int size = convertQueue.size();
                if (size == 0) {
                    break;
                }
                type = (Type)convertQueue.lastElement();
                convertQueue.removeElementAt(size - 1);
            }
        } finally {
            convertionInProgress = null;
        }
    }

   /**
    * Initialize this class
    */
    private void convertMain() {
        if (state < CONVERTING) {
            assume(state == LOADED, "state="+state+" type="+this);
           /*
            * Write trace message and set state
            */
            assume(vm != null);
            trace(vm.traceloading(name), "Converting class " + name);
            state = CONVERTING;

           /*
            * Convert this type's supertype first
            */
            if (this != Type.OBJECT) {
                 superType().convertMain();
            }

           /*
            * Convert all methods from their class file representation to their
            * intermediate representation
            */
            if (clinit != null) {
                clinit = clinit.asIrMethod();
            }
            for (int i = 0 ; i < methods.length ; i++) {
                if (methods[i] != null) {
                    methods[i] = methods[i].asIrMethod();
                }
            }
            state = CONVERTED;
        }
    }

   /**
    * Load this class
    */
    public void load() {
        try {
            if (state < LOADING) {
                int initial = name.charAt(0);
                if (initial == 'L') {
                    assume(state == DEFINED);
                    vm.load(name);
                    assume(state == LOADED);
                    if (vm.eagerloading()) {
                        queueConvertion(this);
                    }
                } else {
                    if (initial == '[') {
                        elementType().load();
                    }
                    state = LOADED;
                }
            }
        } catch (IOException ex) {
            throw new RuntimeException("IOException loading "+this);
        } catch (VerificationException ex) {
            throw new RuntimeException("VerificationException loading "+this);
        }
    }

   /**
    * Mark type as being loaded
    */
    public void loadingStarted()  {
        assume(state == DEFINED);
        state = LOADING;
    }


   /**
    * Mark type as loaded
    */
    public void loadingFinished()  {
        assume(state == LOADING);
        state = LOADED;
    }

   /**
    * getState
    */
    public int getState() {
        return state;
    }

   /**
    * setState
    */
    public void setState(int state) {
        this.state = state;
    }

   /**
    * Convert the methods in a class
    */
    public void convert() {
        if (state != CONVERTED) {
            load();
            convertType(this);
        }
    }




   /* ------------------------------------------------------------------------ *\
    *                             Instance functions                           *
   \* ------------------------------------------------------------------------ */

   /**
    * Return the type's VM
    */
    public VirtualMachine getVM() {
        return vm;
    }

   /**
    * Return the type's superType
    */
    public Type superType() {
        if (superType == null && name.charAt(0) == '[') {
            superType = getArraySuperTypeFor(name);
        }
        assume(superType != null, "superType null for class "+name);
        return superType;
    }

   /**
    * Set the type's superType
    */
    public void setSuperType(Type superType) {
        if (superType != null) {
//prtn("name="+ name);
//prtn("superType="+ superType);
//prtn("this.superType="+ this.superType);
            assume(this.superType == superType || this.superType == null, "type ="+this+" superType="+superType);
            this.superType = superType;
        }
    }

   /**
    * Set the type's attribute flags
    */
    public void setAccessFlags(int flags) {
        this.flags = flags;
    }

public int getAccessFlags() { return flags; }

   /**
    * Set the type's interface table
    */
    public void setInterfaces(Type[] interfaces) {
        this.interfaces = interfaces;
    }

   /**
    * Get the type's interface table
    */
    public Type[] getInterfaces() {
        return interfaces;
    }

   /**
    * Set the type's field table
    */
    public void setFields(Field[] fields, int instanceSize) {
        this.fields = fields;
        this.instanceSize = instanceSize;
        assume(instanceSize >= 0);
    }

   /**
    * Get the type's field table
    */
    public Field[] getFields() {
        return fields;
    }

   /**
    * Get the type's field table width
    */
    public int getInstanceSize() {
        assume(instanceSize >= 0);
        return instanceSize;
    }

   /**
    * Set the type's method table
    */
    public void setMethods(Method[] methods, int methodTableSize) {
        this.methods = methods;
        this.methodTableSize = methodTableSize;
    }

   /**
    * Get the type's method table
    */
    public Method[] getMethods() {
        return methods;
    }

   /**
    * Get the method slot table size
    */
    public int getMethodTableSize() {
        return methodTableSize;
    }

   /**
    * Set the type's <clinit>
    */
    public void setClinit(Method clinit) {
        this.clinit = clinit;
    }

   /**
    * Get the type's <clinit>
    */
    public Method getClinit() {
        return clinit;
    }

   /**
    * Set the type's interface table
    */
    public void setInterfaceTable(short[] interfaceTable) {
        this.interfaceTable = interfaceTable;
//prtn("setInterfaceTable for "+this+" = "+ interfaceTable);
    }

   /**
    * Get the type's interface table
    */
    public short[] getInterfaceTable() {
        return interfaceTable;
    }

   /**
    * Return the name of the type
    */
    public String name() {
        return name;
    }

   /**
    * Return a string representation of the type for debug
    */
    public String toString() {
        return name;
    }

   /**
    * Get the Java super class of this type
    */
    public Type superClass() {
        if (this == Type.OBJECT) {
            return null;
        }
        if (isArray()) {
            return Type.OBJECT;
        } else {
            return superType();
        }
    }

   /**
    * Work out if this is a hierarchical subtype of another type
    */
    private boolean isKindOf(Type aType) {
        Type thiz = this;
        for (;;) {
            if (thiz == aType) {
                return true;
            }
            if (thiz == UNIVERSE) {
                return false;
            }
            thiz = thiz.superType();
        }
    }



   /**
    * Work out this type can be assigned to another type
    */
    public boolean isAssignableTo(Type aType) {

       /*
        * Quickly check for equalty, the most common case.
        */
        if (this == aType) {
           return true;
        }

       /*
        * Check to see of this class is somewhere in aType's hierarchy
        */
        if (this.isKindOf(aType)) {
            return true;
        }

       /*
        * If aType is an interface see if this class implements it
        */
        if (aType.isInterface()) {
            Type thiz = this;
            while (thiz != UNIVERSE) {
                assume(thiz.interfaces != null, "thiz="+thiz);
               /*
                * The interface list in each class is a transitive closure of all the
                * interface types specified in the class file. Therefore it is only
                * necessary to check this list and not the interfaces implemented by
                * the interfaces.
                */
                for (int i = 0 ; i < thiz.interfaces.length ; i++) {
                    if (thiz.interfaces[i] == aType) {
                        return true;
                    }
                }
                thiz = thiz.superType();
            }
        }

       /*
        * If aType is some like of object and this is the null object
        * then assignment is allowed
        */
        if (this == NULLOBJECT && aType.isKindOf(OBJECT)) {
            return true;
        }


       /*
        * This is needed to cast arrays of classes into arrays of interfaces
        */
        if (this.isArray() && aType.isArray()) {
            return this.elementType().isAssignableTo(aType.elementType());
        }

       /*
        * Otherwise there is no match
        */
        return false;
    }

   /**
    * Special version of the above routine for the vertifier
    */
    public boolean vIsAssignableTo(Type aType) {
//prtn(""+this+" vIsAssignableTo "+aType);
        if (this == aType || aType == Type.BOGUS) {
           return true;
        }
        if (this == Type.NEWOBJECT || aType == Type.NEWOBJECT) {
            return false;
        }
        if (!this.isPrimitive() && aType == Type.NULLOBJECT) {
            return true;
        }
        if (aType.isInterface()) {
            aType = Type.OBJECT; // The verifer treats interfaces as java.lang.Object
        }
        return isAssignableTo(aType);
    }


    public boolean isLoaded() {
        return state >= LOADED;
    }

    public boolean isPublic() {
        assume(flags != -1);
        return (flags & ACC_PUBLIC) != 0;
    }

    public boolean isPrivate() {
        assume(flags != -1);
        return (flags & ACC_PRIVATE) != 0;
    }

    public boolean isProtected() {
        assume(flags != -1);
        return (flags & ACC_PROTECTED) != 0;
    }

    public boolean isFinal() {
        assume(flags != -1);
        return (flags & ACC_FINAL) != 0;
    }

    public boolean isInterface() {
        assume(flags != -1, " for "+name);
        return (flags & ACC_INTERFACE) != 0;
    }

    public boolean isAbstract() {
        assume(flags != -1);
        return (flags & ACC_ABSTRACT) != 0;
    }

    public boolean isPrimitive() {
       return superType == PRIMITIVE || superType == INT;
    }


    public final static int BASIC_OOP  = 0x10000;
    public final static int BASIC_INT  = 0x20000;
    public final static int BASIC_LONG = 0x30000;

   /**
    * Slots are of three types single word, double word, and oop
    */
    public int slotType() {
        if(!isPrimitive()) {
            return BASIC_OOP;               // Must be an oop
        } else if (isTwoWords()) {
            return BASIC_LONG;              // Two words
        } else {
            return BASIC_INT;               // Must be one word
        }
    }

    public boolean isTwoWords() {
        assume(this != Type.VOID);
        if (this == Type.LONG || this == Type.DOUBLE) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isLong() {
        return this == Type.LONG;
    }

    public boolean isDouble() {
        return this == Type.DOUBLE;
    }

    public boolean isArray() {
        assume(this != Type.VOID);
        return name.charAt(0) == '[';
    }

    public int dimensions() {
        assume(this != Type.VOID);
        int i;
        for (i = 0 ; i < name.length() ; i++) {
            if (name.charAt(i) != '[') {
                return i;
            }
        }
        return shouldNotReachHere();
    }

   /**
    * elementType
    */
    public Type elementType() {
        assume(name.charAt(0) == '[',"name="+name);
        if (elementType == null) {
            elementType = Type.create(null, name.substring(1));
        }
        assume(elementType != null);
        return elementType;
    }


   /**
    * Get the array type of this type
    */
    public Type asArray() {
        return Type.create(null, "["+name);
    }

   /**
    * Return true if the types are in the same package
    */
    public boolean inSamePackageAs(Type aType) {
        String name1 = this.name();
        String name2 = aType.name();
        int last1 = name1.lastIndexOf('/');
        int last2 = name2.lastIndexOf('/');
        if (last1 != last2) {
            return false;
        }
        if (last1 == -1) {
            return true;
        }
        for (int i = 0 ; i < last1 ; i++) {
            if (name1.charAt(i) != name2.charAt(i)) {
                return false;
            }
        }
        return true;
    }


   /* ------------------------------------------------------------------------ *\
    *                               Member lookup                              *
   \* ------------------------------------------------------------------------ */

   /**
    * Find a field
    */
    public Field findField(String name, String descriptor) {
        Field f = findField(name);
        if (f != null) {
           /*
            * If the field type is not the same as the type of the descriptor
            * then the field just returned is not correct and the classes must
            * have gotten out of sync with each other.
            */
            if (f.type() != createType(descriptor)) {
                return null;
            }
        }
        return f;
    }

   /**
    * Find a field
    */
    public Field findField(String name) {
        assume(fields != null,"fields==null in"+this);
        for (int i = 0 ; i < fields.length ; i++) {
            if (name == fields[i].name()) {
                assume(name.equals(fields[i].name())); // test that interning is working
                return fields[i];
            } else {
                assume(!name.equals(fields[i].name()));// test that interning is working
            }
        }
        if (superType != null) {
            return superType.findField(name);
        }
        return null;
    }

   /**
    * Find a method
    */
    public Method findMethod(String name, String descriptor) {
//prtn("findMethod "+ name+" "+   descriptor);
        int count = Method.countParameters(descriptor);
        if (count < 0) {
            return null;
        }
        Type[] parmTypes  = new Type[count];
        Type   returnType = Method.getParameterTypes(this, descriptor, parmTypes);
        parmTypes = vm.internList(parmTypes);

        Method m = findMethod(name, parmTypes);
        if (m != null) {
            if (m.type() != returnType) {
                return null;
            }
        }
        return m;
    }

   /**
    * Find a method
    */

    public Method findMethod(String name, Type[] parmTypes) {
        return findMethod(this, name, parmTypes);
    }

    private Method findMethod(Type parentType, String name, Type[] parmTypes) {
        assume(methods != null,"methods==null in "+this);
        for (int i = 0 ; i < methods.length ; i++) {
            if (methods[i] != null) {
                if (methods[i].parent() == parentType) {
                    if (name == methods[i].name()) {
                        assume(name.equals(methods[i].name()));     // test that interning is working
                        Type [] mparms = methods[i].getParms();
                        if (mparms == parmTypes) {
                            assume(areSame(mparms, parmTypes));     // test that interning is working
                            return methods[i];
                        } else {
                            assume(!areSame(mparms, parmTypes));    // test that interning is working
                        }
                    } else {
                        assume(!name.equals(methods[i].name()), ""+name);// test that interning is working
                    }
                }
            }
        }
        if (superType != Type.UNIVERSE) {
            return superType.findMethod(name, parmTypes);
        }
        return null;
    }


    private boolean areSame(Type[] aList, Type[] bList) {
        if (DEBUG) {
            if(aList.length != bList.length) {
                return false;
            }
            for (int j = 0 ; j < aList.length ; j++) {
                if(aList[j] != bList[j]) {
                    return false;
                }
            }
        }
        return true;
    }


    public Method findMethod(int slot) {
        if (methods != null) {
//System.out.println("findMethod slot "+slot+" in "+this);
            if (methods.length > 0) {
                int first = methods[0].getSlotOffset();
                if (slot >= first) {
                    slot -= first;
                    assume(slot < methods.length);//, "slot="+slot+" methods.length="+methods.length);
//System.out.println("Found slot "+slot+ " name ="+ methods[slot].name());
                    return methods[slot];
                }
            }
        }
        return superType().findMethod(slot);
    }

    public IntermediateMethod findIntermediateMethod(int slot) {
//System.out.println("findIntermediateMethod slot "+slot+" in "+this);
        Method method = findMethod(slot);
        if (method instanceof ClassFileMethod) {
            method.parent().convert();
            method = method.parent().findMethod(method.getSlotOffset());
        }
        return (IntermediateMethod)method;
    }

    public IntermediateMethod findIntermediateMethod(String name, String descriptor) {
        Method method = findMethod(name, descriptor);
        if (method instanceof ClassFileMethod) {
            method.parent().convert();
            method = method.parent().findMethod(method.getSlotOffset());
        }
        return (IntermediateMethod)method;
    }

    public IntermediateMethod findMethodOrInterface(int slot) {
//System.out.println("findMethodOrInterface slot "+slot+" in "+this);
        if (slot >= VirtualMachine.FIRSTINTERFACE) {
            assume(interfaceTable != null, "null interfaceTable in "+this);
            int first = interfaceTable[0];
            assume(slot-first+1 < interfaceTable.length, "length="+interfaceTable.length+" slot="+slot+" first="+first);
            slot = interfaceTable[slot-first+1];
        }

        return findIntermediateMethod(slot);
    }


   /* ------------------------------------------------------------------------ *\
    *                               Class fileout                              *
   \* ------------------------------------------------------------------------ */


/*

    void classFileOut(FileOut os) {
        VOID.instanceFileOut(os);                               // $nnnn=....
        BOOLEAN.instanceFileOut(os);                            // $nnnn=....
        INT.instanceFileOut(os);                                // $nnnn=....
        SHORT.instanceFileOut(os);                              // $nnnn=....

        os.writeRef   (this, "VOID",    VOID);          // com.sun.squawk.runtime.Type.VOID=$nnnn
        os.writeRef   (this, "BOOLEAN", BOOLEAN);       // com.sun.squawk.runtime.Type.BOOLEAN=$nnnn
        os.writeRef   (this, "INT",     INT);           // com.sun.squawk.runtime.Type.INT=$nnnn
        os.writeRef   (this, "SHORT",   SHORT);         // com.sun.squawk.runtime.Type.SHORT=$nnnn
    }



    void instanceFileOut(FileOut os) {
        os.fileOut(interfaces);                         // $nnnn=[Lcom.sun.squawk.runtime.Interface; $nnnn $nnnn $nnnn]
        os.writeDef   (this);                           // $nnnn=com.sun.squawk.runtime.Type {
        os.writeString("name",        name);            //      name="java.foo.Bar"
        os.writeInt   ("flags",       flags);           //      flags=1234
        os.writeRef   ("interfaces",  interfaces);      //      interfaces=$nnnn
        os.writeEndDef();                               // }
    }



    //   191:new             #15  <Class Integer>
    //   194:dup
    //   195:aload_1
    //   196:iload           6
    //   198:iload_3
    //   199:icmple          207
    //   202:iload           6
    //   204:goto            208
    //   207:iload_3               <-------------------  branch target with "new 191" in stackmap
    //   208:iaload
    //   209:invokespecial   #22  <Method void Integer(int)>
*/
}


