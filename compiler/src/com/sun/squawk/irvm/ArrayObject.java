package com.sun.squawk.irvm;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;

public abstract class ArrayObject extends BasicObject {

   /**
    * Public constructor
    */
    public static ArrayObject create(Type type, int size) {
        switch (type.elementType().slotType()) {
            case Type.BASIC_OOP:  return new OopArrayObject (type, size);
            case Type.BASIC_INT:  return new IntArrayObject (type, size);
            case Type.BASIC_LONG: return new LongArrayObject(type, size);
        }
        shouldNotReachHere();
        return null;
    }


   /**
    * Public constructor
    */
    public static ArrayObject create(String str) {
        return new IntArrayObject(str);
    }

   /**
    * Private constructor
    */
    protected ArrayObject(Type type) {
        super(type);
    }

    abstract public int length();
    abstract public void get(int arrayOffset, RecordObject record, Instruction inst);
    abstract public void put(int arrayOffset, RecordObject record, Instruction inst);

    public void oopAtPut(int arrayOffset, BasicObject subarray) {
        shouldNotReachHere();
    }

    public BasicObject oopAt(int arrayOffset) {
        shouldNotReachHere();
        return null;
    }

    public int intAt(int arrayOffset) {
        return shouldNotReachHere();
    }

    public void copy(int dstPos, ArrayObject src, int srcPos, int length) {

//prtn("dst "+this);
//prtn("src "+src);
//prtn("dstPos "+dstPos);
//prtn("srcPos "+srcPos);
//prtn("length "+length);

        if (src.type().elementType().isPrimitive()) {
            if (src.type() != type()) {
                throw new ArrayStoreException();
            }
        } else {
            if (type().elementType().isPrimitive()) {
                throw new ArrayStoreException();
            }
        }

        int srcEnd = length + srcPos;
        int dstEnd = length + dstPos;

        if (     (length < 0) || (srcPos < 0) || (dstPos < 0)
              || (length > 0 && (srcEnd < 0 || dstEnd < 0))
              || (srcEnd > src.length())
              || (dstEnd >     length())) {
            throw new ArrayIndexOutOfBoundsException();
        }

        if (!src.type().isAssignableTo(type())) {
            Type dstElementType = type().elementType();
            for (int i = 0; i < length; i++) {
                BasicObject item = src.oopAt(srcPos + i);
                if ((item != null) && !item.type().isAssignableTo(dstElementType)) {
                    throw new ArrayStoreException();
                }
                oopAtPut(dstPos + i, item);
            }
            return;
        }

        System.arraycopy(src.data(), srcPos, this.data(), dstPos, length);
    }

    protected Object data() {
        shouldNotReachHere();
        return null;
    }


    public String substring(int offset, int count) {
        shouldNotReachHere();
        return null;
    }

    public String toString() {
        //return "ArrayObject("+type()+")";
        String cname = type().toString();
        cname = cname.substring(cname.lastIndexOf('.')+1);
        return "ArrayObject("+cname+":"+length()+")";
    }
}


class OopArrayObject extends ArrayObject {
    private BasicObject[] data;

    public OopArrayObject(Type type, int size) {
        super(type);
        data = new BasicObject[size];
    }

    public int length() {
        return data.length;
    }

    public void get(int arrayOffset, RecordObject record, Instruction inst) {
        record.oopAtPut(inst, data[arrayOffset]);
    }

    public void put(int arrayOffset, RecordObject record, Instruction inst) {
        BasicObject oop = record.oopAt(inst);
        if (oop != null && !oop.type().isAssignableTo(type().elementType())) {
//prtn("ArrayStoreException "+ oop.type() +" !=== " +  type().elementType());
            throw new ArrayStoreException();
        }
        data[arrayOffset] = oop;
    }

    public BasicObject oopAt(int arrayOffset) {
        return data[arrayOffset];
    }

    public void oopAtPut(int arrayOffset, BasicObject subarray) {
        data[arrayOffset] = subarray;
    }

    protected Object data() {
        return data;
    }
}

class IntArrayObject extends ArrayObject {
    private int[] data;

    public IntArrayObject(Type type, int size) {
        super(type);
        data = new int[size];
    }

    public IntArrayObject(String str) {
        super(Type.CHAR_ARRAY);
        data = new int[str.length()];
        for (int i = 0 ; i < str.length() ; i++) {
            data[i] = str.charAt(i);
        }
    }

    public String substring(int offset, int count) {
        char buf[] = new char[count];
        for(int i = 0 ; i < count ; i++) {
            buf[i] = (char)data[offset+i];
        }
        return new String(buf);
    }

    public int intAt(int arrayOffset) {
        return data[arrayOffset];
    }

    public int length() {
        return data.length;
    }

    public void get(int arrayOffset, RecordObject record, Instruction inst) {
        record.intAtPut(inst, data[arrayOffset]);
    }

    public void put(int arrayOffset, RecordObject record, Instruction inst) {
        data[arrayOffset] = record.intAt(inst);
    }

    protected Object data() {
        return data;
    }
}

class LongArrayObject extends ArrayObject {
    private long[] data;

    public LongArrayObject(Type type, int size) {
        super(type);
        data = new long[size];
    }

    public int length() {
        return data.length;
    }

    public void get(int arrayOffset, RecordObject record, Instruction inst) {
        record.longAtPut(inst, data[arrayOffset]);
    }

    public void put(int arrayOffset, RecordObject record, Instruction inst) {
        data[arrayOffset] = record.longAt(inst);
    }

    protected Object data() {
        return data;
    }
}
