package com.sun.squawk.irvm;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;

public abstract class RecordObject extends BasicObject {

    private final static boolean ASSUMING = false;
    private final static boolean LASTPUT = false;

    protected int[]         intArray;
    protected BasicObject[] oopArray;

   /**
    * Constructor
    */
    public RecordObject(Type type, int slots) {
        super(type);
        intArray = new int[slots];
        oopArray = new BasicObject[slots];
    }

    private String lastPut;

    public String lastPut() {
        String res = lastPut;
        lastPut = null;
        return res;
    }


   /* ------------------------------------------------------------------------ *\
    *                             Indexed by integer                           *
   \* ------------------------------------------------------------------------ */

   /**
    * oopAt
    */
    public BasicObject oopAt(int index) {
        if (ASSUMING) assume(index >= 0);
        return oopArray[index];
    }

   /**
    * oopAtPut
    */
    public void oopAtPut(int index, BasicObject value) {
        if (ASSUMING) assume(index >= 0);
        oopArray[index] = value;
        if (LASTPUT) lastPut = "oopAtPut "+index+" = "+value;
    }

   /**
    * intAt
    */
    public int intAt(int index) {
        if (ASSUMING) assume(index >= 0, "index="+index);
        return intArray[index];
    }

   /**
    * intAtPut
    */
    public void intAtPut(int index, int value) {
        if (ASSUMING) assume(index >= 0 && index < intArray.length,"intArray="+intArray.length+" index="+index);
        intArray[index] = value;
        if (LASTPUT) lastPut = "intAtPut "+index+" = "+value;
    }

   /**
    * longAt
    */
    public long longAt(int index) {
        if (ASSUMING) assume(index >= 0);
        return ((long)(intArray[index]) << 32) + (intArray[index+1] & 0xFFFFFFFFL);
    }

   /**
    * longAtPut
    */
    public void longAtPut(int index, long value) {
        if (ASSUMING) assume(index >= 0);
        intArray[index]   = (int)(value >>> 32) & 0xFFFFFFFF;
        intArray[index+1] = (int)(value >>> 0 ) & 0xFFFFFFFF;
        if (LASTPUT) lastPut = "longAtPut "+index+" = "+value;
    }

   /**
    * floatAt
    */
    public float floatAt(int index) {
        if (ASSUMING) assume(index >= 0);
        return Float.intBitsToFloat(intAt(index));
    }

   /**
    * floatAtPut
    */
    public void floatAtPut(int index, float value) {
        if (ASSUMING) assume(index >= 0);
        intAtPut(index, Float.floatToIntBits(value));
        if (LASTPUT) lastPut = "floatAtPut "+index+" = "+value;
    }

   /**
    * doubleAt
    */
    public double doubleAt(int index) {
        if (ASSUMING) assume(index >= 0);
        return Double.longBitsToDouble(longAt(index));
    }

   /**
    * doubleAtPut
    */
    public void doubleAtPut(int index, double value) {
        if (ASSUMING) assume(index >= 0);
        longAtPut(index, Double.doubleToLongBits(value));
        if (LASTPUT) lastPut = "doubleAtPut "+index+" = "+value;
    }


   /* ------------------------------------------------------------------------ *\
    *                            Indexed by instruction                        *
   \* ------------------------------------------------------------------------ */

   /**
    * oopAt
    */
    public BasicObject oopAt(Instruction inst) {
        return oopAt(inst.getResultOffset());
    }

   /**
    * oopAtPut
    */
    public void oopAtPut(Instruction inst, BasicObject value) {
        oopAtPut(inst.getResultOffset(), value);
    }

   /**
    * intAt
    */
    public int intAt(Instruction inst) {
        return intAt(inst.getResultOffset());
    }

   /**
    * intAtPut
    */
    public void intAtPut(Instruction inst, int value) {
        intAtPut(inst.getResultOffset(), value);
    }

   /**
    * longAt
    */
    public long longAt(Instruction inst) {
        return longAt(inst.getResultOffset());
    }

   /**
    * longAtPut
    */
    public void longAtPut(Instruction inst, long value) {
        longAtPut(inst.getResultOffset(), value);
    }

   /**
    * floatAt
    */
    public float floatAt(Instruction inst) {
        return floatAt(inst.getResultOffset());
    }

   /**
    * floatAtPut
    */
    public void floatAtPut(Instruction inst, float value) {
        floatAtPut(inst.getResultOffset(), value);
    }

   /**
    * doubleAt
    */
    public double doubleAt(Instruction inst) {
        return doubleAt(inst.getResultOffset());
    }

   /**
    * doubleAtPut
    */
    public void doubleAtPut(Instruction inst, double value) {
        doubleAtPut(inst.getResultOffset(), value);
    }

}
