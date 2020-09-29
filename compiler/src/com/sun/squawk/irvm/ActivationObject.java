package com.sun.squawk.irvm;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;

public class ActivationObject extends RecordObject {

    private IntermediateMethod method;
    private Instruction[] instructions;
    private ActivationObject previousActivation;
    private Instruction caller;

   /**
    * Constructor
    */
    public ActivationObject(IntermediateMethod method, ActivationObject previousActivation, Instruction caller) {
        super(null, method.getActivationSize());
        this.method = method;
        this.instructions = method.getInstructions();
        this.previousActivation = previousActivation;
        this.caller = caller;
    }

   /**
    * getMethod
    */
    public IntermediateMethod getMethod() {
        return method;
    }

   /**
    * getInstruction
    */
    public Instruction getInstruction(int ip) {
        return method.getInstruction(ip);
    }

   /**
    * caller
    */
    public Instruction caller() {
        return caller;
    }

   /**
    * previousActivation
    */
    public ActivationObject previousActivation() {
        return previousActivation;
    }


   /* ------------------------------------------------------------------------ *\
    *                   Extension that can resolve constants                   *
   \* ------------------------------------------------------------------------ */

   /**
    * getString
    */
    public BasicObject getString(String str) {
        if (str == null) {
            return null;
        }
        InstanceObject stringObject = new InstanceObject(Type.STRING);
        stringObject.oopAtPut("value",  ArrayObject.create(str));
        stringObject.intAtPut("offset", 0);
        stringObject.intAtPut("count",  str.length());
        return stringObject;
    }

   /**
    * oopAt
    */
    public BasicObject oopAt(Instruction inst) {
        if (inst.getResultOffset() == -1) {
            LoadConstant con = (LoadConstant)inst;
            if (con.isString()) {
                return getString(con.getString());
            } else if (con.isType()) {
                return con.getType();
            } else {
                assume(con.isConstNull());
                return null;
            }
        }
        return super.oopAt(inst);
    }

   /**
    * intAt
    */
    public int intAt(Instruction inst) {
        if (inst.getResultOffset() == -1) {
            return ((LoadConstant)inst).getInt();
        }
        return super.intAt(inst);
    }

   /**
    * longAt
    */
    public long longAt(Instruction inst) {
        if (inst.getResultOffset() == -1) {
            return ((LoadConstant)inst).getLong();
        }
        return super.longAt(inst);
    }


   /**
    * floatAt
    */
    public float floatAt(Instruction inst) {
        if (inst.getResultOffset() == -1) {
            return ((LoadConstant)inst).getFloat();
        }
        return super.floatAt(inst);
    }

   /**
    * doubleAt
    */
    public double doubleAt(Instruction inst) {
        if (inst.getResultOffset() == -1) {
            return ((LoadConstant)inst).getDouble();
        }
        return super.doubleAt(inst);
    }

}
