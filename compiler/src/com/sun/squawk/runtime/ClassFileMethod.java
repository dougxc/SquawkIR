
package com.sun.squawk.runtime;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;


import java.io.IOException;

public class ClassFileMethod extends Method {

   /**
    * The bytecode holder
    */
    private BytecodeHolder holder;

   /**
    * Private constructor
    */
    protected ClassFileMethod(Type parent, String name, Type type, int flags, Type[] parms) {
        super(parent, name, type, flags, parms);
    }

   /**
    * setHolder
    */
    public void setHolder(BytecodeHolder holder) {
        this.holder = holder;
    }

   /**
    * getIR
    */
    private Instruction getIR(BytecodeHolder holder) throws IOException, VerificationException {
        Instruction ir = null;
        if (holder != null) {
            try {
                assume(getIRcount++ == 0); // check that serilization is working
                ir = holder.getIR();
            } finally {
                getIRcount--;
            }
        }
        return ir;
    }
    private static int getIRcount = 0;

   /**
    * asIrMethod
    */
    public IntermediateMethod asIrMethod() {
        try {
            Instruction ir = getIR(holder);
            IntermediateMethod res = new IntermediateMethod(parent(), name(), type(), flags(), getParms(), ir);
            res.setSlotOffset(getSlotOffset());
            return res;
        } catch (IOException ex) {
            throw new RuntimeException("IOException loading "+this);
        } catch (VerificationException ex) {
            ex.printStackTrace();
            throw new RuntimeException("VerificationException loading "+this);
        }
    }

}
