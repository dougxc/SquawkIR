
package com.sun.squawk.runtime.loader;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.util.*;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class ConstantPool extends BaseFunctions implements RuntimeConstants {

   /**
    * The constant pool entries are encoded as regular Java objects. The list
    * of valid objects for each tag type are:
    *
    *   CONSTANT_Utf8               null (Not retained)
    *   CONSTANT_NameAndType        null (Not retained)
    *   CONSTANT_Integer            java.lang.Integer
    *   CONSTANT_Float              java.lang.Float
    *   CONSTANT_Long               java.lang.Long
    *   CONSTANT_Double             java.lang.Double
    *   CONSTANT_String             java.lang.String
    *   CONSTANT_Class              com.sun.squawk.runtime.Type
    *   CONSTANT_Field              com.sun.squawk.runtime.Field
    *   CONSTANT_Method             com.sun.squawk.runtime.Method
    *   CONSTANT_InterfaceMethod    com.sun.squawk.runtime.Method
    *
    * Thus only a null, Integer, Long, Float, Double, Type, Field, or Method will
    * be found in this array.
    *
    * CONSTANT_Utf8 entries are converted into Strings
    * CONSTANT_NameAndType are not needed becuse the UTF8 strings they refer
    * to is converted into strings and places in the approperate Field and Method
    * data structures.
    */

   /**
    * The virtual machine for this constant pool
    */
    private VirtualMachine vm;

   /**
    * Input stream context
    */
    private InputContext ctx;

   /**
    * Pool entry tags
    */
    private byte[] tags;

   /**
    * Temporary values taken from the origional pool structures
    */
    private int[] temp;

   /**
    * Resolved pool entries for all object types
    */
    private Object[] entries;

   /**
    * Prevent direct construction
    */
    private ConstantPool() {}

   /**
    * Get the tag for the entry
    */
    public int getTag(int index) throws IOException, VerificationException {
        if (index < 0 || index >= entries.length ) {
            throw ctx.verificationException(120); // 120????????????
        }
        return tags[index];
    }

    public int getInt(int index) throws IOException, VerificationException {
        return temp[index];
    }

    public long getLong(int index) throws IOException, VerificationException {
        return ((Long)entries[index]).longValue();
    }

    public float getFloat(int index) throws IOException, VerificationException {
        return ((Float)entries[index]).floatValue();
    }

    public double getDouble(int index) throws IOException, VerificationException {
        return ((Double)entries[index]).doubleValue();
    }

    public String getString(int index) throws IOException, VerificationException {
        return (String)entries[index];
    }

    public String getStringInterning(int index) throws IOException, VerificationException {
        return vm.internString(getString(index));
    }

    public Type bootstrapType(int index) throws IOException, VerificationException {
        return (Type)entries[index];
    }

    public Type getType(int index) throws IOException, VerificationException {
        Type type = (Type)entries[index];
        if (type != null && !type.isLoaded()) {
            type.load();
        }
        return type;
    }

    public Field getField(int index) throws IOException, VerificationException {
        Field field = (Field)entries[index];
        if (field == null) {
            field = (Field)resolve(index);
            entries[index] = field;
        }
        return field;
    }

    public Method getMethod(int index) throws IOException, VerificationException {
        Method method = (Method)entries[index];
        if (method == null) {
            method = (Method)resolve(index);
            entries[index] = method;
        }
        return method;
    }

    public Object getEntry(int index) throws IOException, VerificationException {
        if (getTag(index) == CONSTANT_Integer) {
            return new Integer(temp[index]);
        }
        assume(!(entries[index] instanceof Type));
        assume(!(entries[index] instanceof Field));
        assume(!(entries[index] instanceof Method));
        return entries[index];
    }


   /* ------------------------------------------------------------------------ *\
    *                             Pool loading code                            *
   \* ------------------------------------------------------------------------ */

   /**
    * Create a new constant pool from the input stream
    */
    public static ConstantPool create(VirtualMachine vm, ClassFileInputStream in) throws IOException, VerificationException {
        return new ConstantPool(vm, in);
    }


    private ConstantPool(VirtualMachine vm, ClassFileInputStream in) throws IOException, VerificationException {

       /*
        * Keep input stream pointer in order to get verification errors
        */
        this.vm = vm;
        this.ctx = in;

       /*
        * Read the constant pool entry count
        */
        int count = in.readUnsignedShort("cp-count");

       /*
        * Allocate the required lists
        */
        tags    = new byte[count];
        temp    = new int[count];
        entries = new Object[count];

       /*
        * Read the constant pool entries from the classfile
        * and initialize the constant pool correspondingly.
        * Remember that constant pool indices start from 1
        * rather than 0 and that last index is count-1.
        */

       /*
        * Pass 1 read in the primitive values
        */
        for (int i = 1 ; i < count ; i++) {
            int tag = in.readUnsignedByte("cp-tag");
            tags[i] = (byte)tag;
            switch (tag) {
                case CONSTANT_Utf8: {
                    //ntries[i] = vm.internString(in.readUTF("CONSTANT_Utf8"));
                    entries[i] = in.readUTF("CONSTANT_Utf8");
                    break;
                }
                case CONSTANT_Integer: {
                    temp[i] = in.readInt("CONSTANT_Integer");
                    break;
                }
                case CONSTANT_Float: {
                    entries[i] = new Float(in.readFloat("CONSTANT_Float"));
                    break;
                }
                case CONSTANT_Long: {
                    entries[i] = new Long(in.readLong("CONSTANT_Long"));
                    i++; // Longs take two slots
                    break;
                }
                case CONSTANT_Double: {
                    entries[i] = new Double(in.readDouble("CONSTANT_Double"));
                    i++; // Doubles take two slots
                    break;
                }

                case CONSTANT_String:
                case CONSTANT_Class: {
                    temp[i] = in.readUnsignedShort("CONSTANT_String/Class");
                    break;
                }

                case CONSTANT_Field:
                case CONSTANT_Method:
                case CONSTANT_InterfaceMethod:
                case CONSTANT_NameAndType: {
                    temp[i] = (in.readUnsignedShort("CONSTANT_F/M/I/N-1") <<16) + in.readUnsignedShort("CONSTANT_F/M/I/N-2");
                    break;
                }

                default: {
                    throw in.verificationException("tag="+tag);
                }
            }
        }

       /*
        * Pass 2 fixup types and strings
        */
        for (int i = 1 ; i < count ; i++) {
            switch (tags[i]) {
                case CONSTANT_String: {
                    entries[i] = entries[temp[i]];
                    break;
                }
                case CONSTANT_Class: {
                    String type = (String)entries[temp[i]];
                    type = type.replace('.', '/');
                    if (type.charAt(0) != '[') {
                        assume(type.charAt(0) != 'L');
                        assume(type.charAt(type.length()-1) != ';');
                        type = "L"+type+';';
                    }
                    entries[i] = vm.createType(null, type);
                    break;
                }
            }
        }
    }


   /**
    * Resolve member references
    */
    private Object resolve(int i) throws IOException, VerificationException {
        int classIndex       = temp[i] >> 16;
        int nameAndTypeIndex = temp[i] & 0xFFFF;
        int nameIndex        = temp[nameAndTypeIndex] >> 16;
        int descriptorIndex  = temp[nameAndTypeIndex] & 0xFFFF;
        Type   parentType    = (Type)entries[classIndex];
        String name          = getStringInterning(nameIndex);
        String sig           = (String)entries[descriptorIndex];

        if (!parentType.isLoaded()) {
            parentType.load();
        }

        assume(parentType.superType() != null);

        switch(tags[i]) {
            case CONSTANT_Field: {
                Field field = parentType.findField(name, sig);
                if (field == null) {
                    throw ctx.verificationException("Could not find field "+name+sig+" in "+parentType);
                }
                field.load();
                return field;
            }

            case CONSTANT_Method:
            case CONSTANT_InterfaceMethod: {
                Method method = parentType.findMethod(name, sig);
                if (method == null) {
                    //prtn("hash="+type.hashCode()+" j="+type.methods.length);
                    //for(int j = 0 ; j < type.methods.length ; j++) {
                    //    prtn(type.methods[j]+"\n");
                    //}
                    throw ctx.verificationException("Could not find method "+name+sig+" in "+parentType);
                }
                if (tags[i] == CONSTANT_Method) {
                    if (method.parent().isInterface()) {
                        ctx.verificationException("Method should not be in interface "+name+" in "+parentType);
                    }
                } else {
                    if (!method.parent().isInterface()) {
                        throw ctx.verificationException("Method should be in interface "+name+" in "+parentType);
                    }
                }
                method.load();
                return method;
            }
            default: {
                shouldNotReachHere();
            }
        }
        return null;
    }




}
