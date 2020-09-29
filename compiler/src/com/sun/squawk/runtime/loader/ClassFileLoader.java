
package com.sun.squawk.runtime.loader;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.util.*;

import java.io.IOException;
import java.io.InputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Vector;
import java.util.StringTokenizer;
import java.util.Enumeration;
import java.util.zip.*;

public class ClassFileLoader extends BaseFunctions implements RuntimeConstants {

   /**
    * Static constructor
    */
    public static ClassFileLoader create(VirtualMachine vm, String classpath) throws IOException, VerificationException {
        return new ClassFileLoader(vm, classpath);
    }

   /**
    * The virtual machine for this class loader
    */
    private VirtualMachine vm;

   /**
    * The classpath array
    */
    private Vector classPathArray = new Vector();

   /**
    * Hashtable for debugging only
    */
    private ArrayHashtable checkLoaded;

   /**
    * Setup the classpath array
    */
    public void setClassPath(String path) {
        StringTokenizer st = new StringTokenizer(path, ";:");
        while (st.hasMoreTokens()) {
            String dirName = st.nextToken();
            if (dirName.endsWith("\\") || dirName.endsWith("/")) {
                dirName = dirName.substring(0, dirName.length() - 1);
            }
            classPathArray.addElement(dirName);
        }
    }

   /**
    * Constructor
    */
    private ClassFileLoader(VirtualMachine vm, String classPath) throws IOException, VerificationException {
        this.vm = vm;
        setClassPath(classPath);
    }

   /**
    * The loader's main function
    */
    public Type load(String fileName) throws IOException, VerificationException {

       /*
        * Trasnsform parameter
        *
        *   "Ljava/foo/Bar;" -> "java/foo/Bar"
        *
        *   "java.foo.Bar." -> "java/foo/Bar"
        */
        if (fileName.charAt(fileName.length() - 1) == ';') {
            assume(fileName.charAt(0) == 'L');
            fileName = fileName.substring(1, fileName.length() - 1);
        } else {
            fileName = fileName.replace('.', '/');
        }


        String loadName;
        InputStream is = null;

        for (int i = 0  ; i < classPathArray.size() ; i++) {

           /*
            * Get the section of the classpath array
            */
            String classPath = (String)classPathArray.elementAt(i);

            if (classPath.endsWith(".zip")) {
               /*
                * Open the file inside a zip file
                */
                loadName = fileName+".class";
                try {
                    ZipFile  z = new ZipFile(classPath);
                    ZipEntry e = z.getEntry(loadName);
                    if (e != null) {
                         is = z.getInputStream(e);
                    }
                } catch (IOException e1) {
                }
            } else {
               /*
                * Open the file
                */
                loadName = classPath+"/"+fileName+".class";
                try {
                    is = new FileInputStream(loadName);
                    break;
                } catch (FileNotFoundException ex) {
                }
            }
        }

        if (is == null) {
            throw new FileNotFoundException(fileName);
        }

        return load(fileName, is);
    }

   /**
    * The loader's main function
    */
    public Type load(String fileName, InputStream is) throws IOException, VerificationException {

       /*
        * Write trace message
        */
        trace(vm.traceloading(fileName), "Loading class " + fileName);

        assume(fileName.indexOf('\\') == -1);

        if (DEBUG) {
            if (checkLoaded == null) {
                checkLoaded = new ArrayHashtable();
            }
            assume(checkLoaded.get(fileName) == null);
            checkLoaded.put(fileName, fileName);
        }

       /*
        * Wrap the input stream int a ClassFileInputStream
        */
        ClassFileInputStream in = new ClassFileInputStream(is,  fileName);

       /*
        * Set trace if requested
        */
        in.setTrace(vm.tracepool(fileName));

       /*
        * Read the magic values
        */
        loadMagicValues(in);

       /*
        * Read the constant pool
        */
        ConstantPool pool = loadConstantPool(in);

       /*
        * Read the class information
        */
        Type type = loadClassInfo(in, pool);

       /*
        * Mark type as being loaded
        */
        type.loadingStarted();

       /*
        * Read the interface definitions
        */
        loadInterfaces(in, pool, type);

       /*
        * Trace
        */
        String classOrInterface = type.isInterface() ? "interface " : "class ";
        trace(vm.tracefields(type.name()), "\n"+classOrInterface+type.name()+"        (extends "+type.superType().name()+")");
        traceInterfaces(type);

       /*
        * Read the field definitions
        */
        loadFields(in, pool, type);

       /*
        * Read the method definitions
        */
        loadMethods(in, pool, type);

       /*
        * Workout which methods can be called from an invokeinterface
        */
        resolveInterfaces(in, pool, type);

       /*
        * Read the extra attributes
        */
        loadExtraAttributes(in, pool, type);

       /*
        * Close the input stream
        */
        in.close();

       /*
        * Trace
        */
        trace(vm.traceloading(fileName) && vm.verbose(), "Finshed Loading class " + fileName);

       /*
        * Mark type as loaded
        */
        type.loadingFinished();

       /*
        * Return the new type
        */
        return type;
    }


   /**
    * Load the magic values
    */
    private void loadMagicValues(ClassFileInputStream in) throws IOException, VerificationException {
        int magic = in.readInt("magic");
        int minor = in.readUnsignedShort("minor");
        int major = in.readUnsignedShort("magor");
        if (magic != 0xCAFEBABE) {
            throw in.verificationException("Bad magic value");
        }
        if (major != 45 || minor != 3) {
            throw in.verificationException("Bad class file version number");
        }
    }

   /**
    *  Load the constant pool
    */
    private ConstantPool loadConstantPool(ClassFileInputStream in) throws IOException, VerificationException {
        return ConstantPool.create(vm, in);
    }

   /**
    *  Load the class information
    */
    private Type loadClassInfo(ClassFileInputStream in, ConstantPool pool) throws IOException, VerificationException {
        int accessFlags = in.readUnsignedShort("cls-flags");
        int classIndex  = in.readUnsignedShort("cls-index");
        int superIndex  = in.readUnsignedShort("cls-super index");

       /*
        * Loading the constant pool will have created the Type object.
        */
        Type superType = pool.getType(superIndex);
        Type type      = pool.bootstrapType(classIndex);

       /*
        * Fill in just what is known about the type thus far
        */
        type.setSuperType(superType);
        type.setAccessFlags(accessFlags);

        return type;
    }

   /**
    *  Load the class's interfaces
    */
    private void loadInterfaces(ClassFileInputStream in, ConstantPool pool, Type type) throws IOException, VerificationException {
        int count = in.readUnsignedShort("i/f-count");
        if (count > 0) {

           /*
            * Temporary vector to hold all the interfaces
            */
            Vector interfaces = new Vector(count);

           /*
            * Include in the interfaces table for this type all the interfaces specified
            * in the class file plus all the interfaces implemented by those interfaces.
            * Because this is recersive all the possible interfaces implemented by this class
            * (except those further up the hierarchy) will be directly included in the list.
            */
            for (int i = 0 ; i < count ; i++) {
                Type iface = pool.getType(in.readUnsignedShort("i/f-index"));
                if (interfaces.indexOf(iface) < 0) {
                    interfaces.addElement(iface);
                    Type[] subInterfaces = iface.getInterfaces();
                    for (int k = 0 ; k < subInterfaces.length ; k++) {
                        Type sface = subInterfaces[k];
                        if (interfaces.indexOf(sface) < 0) {
                            interfaces.addElement(sface);
                        }
                    }
                }
            }

           /*
            * Make into a Type[] of all the above
            */
            Type[] interfaces2 = new Type[interfaces.size()];

            int j = 0;
            for(Enumeration e = interfaces.elements() ; e.hasMoreElements() ;) {
                 Type iface = (Type)e.nextElement();
                 interfaces2[j++] = iface;
            }

            assume(j == interfaces2.length);

           /*
            * Set in the type
            */
            type.setInterfaces(interfaces2);
        }
    }


   /**
    *  Trace the class's interfaces
    */
    private void traceInterfaces(Type type) {
        Type[] interfaces = type.getInterfaces();
        for (int i = 0 ; i < interfaces.length ; i++) {
            trace(vm.tracefields(type.name()), "    Implements\t"+interfaces[i].name());
        }
    }


   /**
    *  Load the class's fields
    */
    private void loadFields(ClassFileInputStream in, ConstantPool pool, Type type) throws IOException, VerificationException {

       /*
        * The number of words that each instance of this class will need for instance data.
        */
        int instanceSize = type.superType().getInstanceSize();

       /*
        * Get count of fields
        */
        int count = in.readUnsignedShort("fld-count");
        if (count == 0) {
            type.setFields(VirtualMachine.ZEROFIELDS, instanceSize);
            return;
        }

       /*
        * Allocate the field table
        */
        Field[] fields = new Field[count];

        /*
         * Read in all the fields
         */
        for (int i = 0; i < count; i++) {
            int accessFlags     = in.readUnsignedShort("fld-flags");
            int nameIndex       = in.readUnsignedShort("fld-nameIndex");
            int descriptorIndex = in.readUnsignedShort("fld-descIndex");
            int attributesCount = in.readUnsignedShort("fld-AttbCount");
            int slot            = -1;
            Object initialValue = null;

            String fieldName = pool.getStringInterning(nameIndex);
            String fieldSig  = pool.getString(descriptorIndex);

           /*
            * Process the field's attruibutes
            */
            for (int j = 0; j < attributesCount; j++) {
                int    attributeNameIndex = in.readUnsignedShort("fld-att-nameIndex");
                int    attributeLength    = in.readInt("fld-att-length");
                String attributeName      = pool.getString(attributeNameIndex);

                if (attributeName.equals("ConstantValue") && attributeLength == 2) {
                    if (initialValue != null) {
                        throw in.verificationException("More than one ConstantValue attributes");
                    }
                    assume((accessFlags & ACC_STATIC) != 0);
                    initialValue = pool.getEntry(in.readUnsignedShort("fld-ConstantValue")); // Get the varibale initialzation value
                } else {
                    while (attributeLength-- > 0) {
                        in.readByte(); // Ignore this attribute
                    }
                }
            }

            Field field = Field.create(vm, in, type, fieldName, fieldSig, accessFlags);

            int width = field.type().isTwoWords() ? 2 : 1;

            if (!field.isStatic()) {
               /*
                * Allocate an instance variable
                */
                slot = instanceSize;
                instanceSize += width;
            } else {
               /*
                * Allocate a global variable (but not if its a constant)
                */
                if (initialValue == null || !field.isFinal()) {
                    slot = vm.allocateGlobal(width);
                }
            }

            field.setSlot(slot, initialValue);
            fields[i] = field;

            if (vm.tracefields(type.name()+"::"+fieldName+fieldSig)) {
                String slotstr = slot == -1           ? "   " : "["+slot+"]";
                String initstr = initialValue == null ? "" : " \t(init="+initialValue+")";
                trace(true, "    Field"+slotstr+"\t"+fieldName+" "+fieldSig+initstr);
            }
        }

        type.setFields(fields, instanceSize);
    }

   /**
    *  Load the class's methods
    */
    private void loadMethods(ClassFileInputStream in, ConstantPool pool, Type type) throws IOException, VerificationException {

       /*
        * Setup nextSlot. If this is an interface and the super type is Object then
        * nextSlot is set to the next unused interface number. Otherwize the super
        * type's method table size if the next starting place/
        */

        int nextSlot;
        if (type.isInterface() && type.superType() == Type.OBJECT) {
            nextSlot = vm.nextInterfaceMethod();
        } else {
            nextSlot = type.superType().getMethodTableSize();
        }

       /*
        * Get count of methods and exit if there are none
        */
        int count = in.readUnsignedShort("mth-count");
        if (count == 0) {
            type.setMethods(VirtualMachine.ZEROMETHODS, nextSlot);
            return;
        }

       /*
        * In this routine statics start at 20000 so they will sort after the instance methods
        */
        int nextStatic = 20000;

       /*
        * Flag to say if <clinit> was found
        */
        boolean sawClinit = false;

       /*
        * Allocate the method vector
        */
        MethodVector methods = new MethodVector(count);

       /*
        * Read in all the methods
        */
        for (int i = 0; i < count; i++) {
            int accessFlags     = in.readUnsignedShort("mth-flags");
            int nameIndex       = in.readUnsignedShort("mth-nameIndex");
            int descriptorIndex = in.readUnsignedShort("mth-descIndex");
            int attributesCount = in.readUnsignedShort("mth-AttbCount");

            String methodName = pool.getStringInterning(nameIndex);
            String methodSig  = pool.getString(descriptorIndex);
//prtn("name="+ methodName);

           /*
            * Get the method structure
            */
            Method method = Method.create(vm, in, type, methodName, methodSig, accessFlags);

           /*
            * Process the method's attruibutes
            */
            for (int j = 0; j < attributesCount; j++) {
                int    attributeNameIndex = in.readUnsignedShort("mth-att-nameIndex");
                int    attributeLength    = in.readInt("mth-att-length");
                String attributeName      = pool.getString(attributeNameIndex);

                if (attributeName.equals("Code")) {
                    loadMethodCode(in, pool, method, attributeLength);
                } else {
                    while (attributeLength-- > 0) {
                        in.readByte(); // Ignore this attribute
                    }
                }
            }

            assume((methodName == VirtualMachine.INIT)   == methodName.equals("<init>"));
            assume((methodName == VirtualMachine.CLINIT) == methodName.equals("<clinit>"));

            if (methodName == VirtualMachine.CLINIT) {
               /*
                * <clinit> is not really wanted in the method table but the sort routine
                * in MethodVector requires that the vector is completely populated. So
                * set the the slot number to something high.
                */
                method.setSlotOffset(30000);

               /*
                * Setup special pointer in type
                */
                type.setClinit(method);

               /*
                * Set flag for later
                */
                sawClinit = true;

            } else if (method.isStatic()) {
               /*
                * Statics get slot numbers starting from 20000 (for now)
                */
                method.setSlotOffset(nextStatic++);

            } else {
               /*
                * Look for this method in the supertype, but don't look if the method name
                * is <init>. This last point just avoids all the vtables having to go back
                * as far as the <init> entry in Object.
                */

                Method smethod = null;
                if (methodName != VirtualMachine.INIT && type.superType() != Type.UNIVERSE) {
                    smethod = type.superType().findMethod(methodName, method.getParms());
                    if (smethod != null && smethod.isStatic() != method.isStatic()) {
                        smethod = null; // dont match a virtual method with a static method
                    }
                }
                if (smethod != null) {
                    method.setSlotOffset(smethod.getSlotOffset());
                } else {
                    if (type.isInterface()) {
                        method.setSlotOffset(vm.allocateInterfaceMethod());
                        nextSlot++;
                    } else {
                        method.setSlotOffset(nextSlot++);
                    }
                }
            }
            methods.addElement(method);
        }

       /*
        * Sort the methods by slot number
        */
        Object[] sortedMethods = methods.sorted();

       /*
        * If there was only <clinit> then exit now
        */
        if (sortedMethods.length == 1 && sawClinit) {
            type.setMethods(VirtualMachine.ZEROMETHODS, nextSlot);
//prtn("sawClinit only");
//            return;
        }

       /*
        * Get the slot number of the first entry.
        */
        int firstSlot = ((Method)sortedMethods[0]).getSlotOffset();

       /*
        * If there are no virtual methods then
        */
        if (firstSlot >= 20000) {
            firstSlot = nextSlot;
        }

//prtn("firstSlot="+firstSlot);
//prtn("nextSlot="+nextSlot);
//prtn("nextStatic="+nextStatic);
//prtn("size="+((nextSlot-firstSlot)+(nextStatic-20000)));

        //assume(!((Method)sortedMethods[0]).isStatic(), "m="+(Method)sortedMethods[0]);



       /*
        * Allocate a method array large enough to hold the instance range from
        * the lowest slot number defined in this method to the highest, plus
        * all the static methods which come afterwards.
        */
        Method[] allMethods = new Method[(nextSlot-firstSlot)+(nextStatic-20000)];

       /*
        * Go through the method vector filling in the method array
        */
        for (int i = 0 ; i < sortedMethods.length ; i++) {
            Method m = (Method)sortedMethods[i];

           /*
            * If the slot number is 30000 then this is <clinit> which is
            * not wanted and must also be the last entry.
            */
            int slotOffset = m.getSlotOffset();
            if (slotOffset == 30000) {
                break;
            }

            if (slotOffset < 20000) {
               /*
                * These are instance methods. Put them into the method array at
                * their slot number offset (relative to the starting point of the
                * method array).
                */
                slotOffset -= firstSlot;
                assume(slotOffset < allMethods.length,"slotOffset="+slotOffset);
                assume(allMethods[slotOffset] == null, "slot = "+allMethods[slotOffset]+" m = "+m+" slotOffset = "+slotOffset+" type = "+type+" firstSlot="+firstSlot);
                allMethods[slotOffset] = m;
            } else {
               /*
                * These are static methods. Put them at the end using the next
                * available slot numbers.
                */
                assume(!type.isInterface());
                slotOffset = ((slotOffset - 20000) + nextSlot) - firstSlot;
                assume(slotOffset < allMethods.length,"slotOffset="+slotOffset);
                assume(allMethods[slotOffset] == null);
                m.setSlotOffset(slotOffset+firstSlot);
                allMethods[slotOffset] = m;
            }
        }

       /*
        * Now fill in the other entries
        */
        for (int i = 0 ; i < allMethods.length ; i++) {
            if (allMethods[i] == null) {
                Method m = type.superType().findMethod(firstSlot+i);
                assume(m != null);
                allMethods[i] = m;
            }
        }

       /*
        * Trace
        */
        trace(sawClinit && vm.tracefields(type.name()+"<clinit>"), "    Method[-]\t<clinit>");

        for (int i = 0 ; i < allMethods.length ; i++) {
            Method m = allMethods[i];
            int slot = (m != null) ? m.getSlotOffset() : i;
            String slotString = (m.isStatic()) ? ""+slot+"s" : ""+slot;
            String verbose = vm.verbose() ? ""+m.parent()+"::" : "";
            trace(vm.tracefields(type.name()+"::"+m), "    Method["+slotString+"]\t"+verbose+m);
        }

        type.setMethods(allMethods, nextSlot);
    }


   /**
    *  Load the method's code
    */
    private void loadMethodCode(ClassFileInputStream in, ConstantPool pool, Method method, int attributeLengthXX) throws IOException, VerificationException {
        int maxStack   = in.readUnsignedShort("cod-maxStack");  // Maximum stack need
        int maxLocals  = in.readUnsignedShort("cod-maxLocals"); // Max locals need
        int codeLength = in.readInt("cod-length");              // Length of the bytecode array
        StackMap   map = null;

       /*
        * Read the bytecodes into a buffer. The GraphBuilder needs to know
        * about the exception handlers and stack maps which come after the
        * bytecodes.
        */
        byte[] bytecodes = new byte[codeLength];
        in.readFully(bytecodes);

        BytecodeHolder holder = new BytecodeHolder(method, pool, bytecodes, maxStack, maxLocals);

       /*
        * Read in the exception handlers
        */
        int handlers = in.readShort("hnd-handlers");
        if (handlers > 0) {
            holder.setHandlerCount(handlers);
            for (int i = 0; i < handlers; i++) {
                char startPC    = in.readChar("hnd-startPC");               // Code range where handler is valid
                char endPC      = in.readChar("hnd-endPC");                 // (as offsets within bytecode)
                char handlerPC  = in.readChar("hnd-handlerPC");             // Offset to handler code
                char catchIndex = in.readChar("hnd-catchIndex");            // Exception (constant pool index)

               /*
                * Check that all the pc addresses look reasionable
                */
                if (startPC >= codeLength || endPC >= codeLength || startPC >= endPC || handlerPC >= codeLength) {
                    throw in.verificationException("KVM_MSG_BAD_EXCEPTION_HANDLER_FOUND");
                }

               /*
                * Set in methid structure
                */
                holder.setHandler(i, startPC, endPC, handlerPC, catchIndex);
            }
        }

       /*
        * Read in the code attributes
        */
        int attributesCount = in.readUnsignedShort("cod-attributesCount");
        for (int i = 0; i < attributesCount; i++) {
            int attributeNameIndex = in.readUnsignedShort("cod-attributeNameIndex");
            int attributeLength    = in.readInt("cod-attributeLength");
            String attributeName   = pool.getString(attributeNameIndex);
            if (attributeName.equals("StackMap")) {
                byte[] mapdata = new byte[attributeLength];
                in.readFully(mapdata);
                holder.setMapData(mapdata);
            } else if(attributeName.equals("LineNumberTable")) {
                int lineNumberTableLength = in.readUnsignedShort("lin-lineNumberTableLength") * 2;
                char[] lines = new char[lineNumberTableLength];
                for (int k = 0 ; k < lineNumberTableLength ; ) {
                    lines[k++] = (char)in.readUnsignedShort("lin-startPC");
                    lines[k++] = (char)in.readUnsignedShort("lin-lineNumber");
                }
                holder.setLineTable(lines);
            } else {
                prtn("ignored attributeName="+attributeName);
                while (attributeLength-- > 0) {
                    in.readByte(); // Ignore this attribute
                }
            }

        }

        method.setHolder(holder);
    }


   /**
    *  Workout which methods can be called from an invokeinterface
    */
    private void resolveInterfaces(ClassFileInputStream in, ConstantPool pool, Type toptype) throws IOException, VerificationException {
        Type type = toptype;
        if (!type.isInterface()) {

           /*
            * Get all the interface methods that this type implements
            */
            MethodVector interfaceMethods = new MethodVector(type.getInterfaces().length);
            while (type != Type.UNIVERSE) {
                Type[] interfaces = type.getInterfaces();
                for (int i = 0 ; i < interfaces.length ; i++) {
                    Type interfaceType = interfaces[i];
                    Method[] methods = interfaceType.getMethods();
                    for (int j = 0 ; j < methods.length ; j++) {
                        Method imethod = methods[j];
                        //trace(vm.tracefields(toptype.name()), "    Interface "+ourMethod.name()+" is "+imethod.getSlotOffset());

                       /*
                        * Add interface method to list (but don't add it more than once).
                        */
                        int n = interfaceMethods.indexOf(imethod);
                        if (n < 0) {
                            interfaceMethods.addElement(imethod);
                        }
                    }

                }
                type = type.superType();
            }

           /*
            * Sort the interface methods into order
            */
            interfaceMethods.trimToSize();
            Object[] sortedMethods = interfaceMethods.sorted();

            if (sortedMethods.length > 0) {
               /*
                * Get the inteface numbers for the first and last entries
                */
                int firstInterface = ((Method)sortedMethods[0]).getSlotOffset();
                int lastInterface  = ((Method)sortedMethods[sortedMethods.length - 1]).getSlotOffset();

//prtn("firstInterface="+firstInterface);
//prtn("lastInterface="+lastInterface);

               /*
                * Allocate an array of shorts that is large enough to hold all the interface entries
                * and an extra entry.
                */
                short[] interfaceTable = new short[1+(lastInterface-firstInterface)+1];

               /*
                * Make the first entry the number of the first interface
                */
                interfaceTable[0] = (short)firstInterface;
                for (int j = 0 ; j < sortedMethods.length ; j++) {
                    Method imethod = (Method)sortedMethods[j];
                    assume(!imethod.name().equals("<clinit>"));
                    Method ourMethod = toptype.findMethod(imethod.name(), imethod.getParms());
                    assume(ourMethod != null, "Failure to find method "+imethod.name()+" in "+toptype);
                    interfaceTable[(imethod.getSlotOffset() - firstInterface)+1] = (short)ourMethod.getSlotOffset();
                }

                toptype.setInterfaceTable(interfaceTable);

               /*
                * Trace
                */
                int first = interfaceTable[0];
//prtn("first="+first);

                for (int i = 1 ; i < interfaceTable.length ; i++) {
                    Method entry = null;
                    String entryName = null;
                    if (interfaceTable[i] != 0) {
                        entry = toptype.findMethod(interfaceTable[i]);
                        entryName = entry.name();
                    }
                    if (entryName != null) {
                        entryName = "Method[" + entry.getSlotOffset() + "] "+entryName;
                    }
                    trace(vm.tracefields(toptype.name()), "    Interface[" + (first+i-1) + "] = " + entryName);
                }
            }
        }
    }


   /**
    *  Load the class's other attributes
    */
    private void loadExtraAttributes(ClassFileInputStream in, ConstantPool pool, Type type) throws IOException, VerificationException {
        int attributesCount = in.readUnsignedShort("ex-count");
        for (int i = 0; i < attributesCount; i++) {
            int attributeNameIndex = in.readUnsignedShort("ex-index");
            int attributeLength    = in.readInt("ex-length");
            while (attributeLength-- > 0) {
                in.readByte(); // Ignore this attribute
            }
        }
    }

}
