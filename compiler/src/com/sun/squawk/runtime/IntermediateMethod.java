
package com.sun.squawk.runtime;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;


import java.io.IOException;

public class IntermediateMethod extends Method {

    private Instruction[] list;
    private int activationSize;

   /**
    * Private constructor
    */
    protected IntermediateMethod(Type parent, String name, Type type, int flags, Type[] parms, Instruction ir) {
        super(parent, name, type, flags, parms);

       /*
        * Read the IR into an array
        */
        if (ir != null) {
            MethodHeader hdr = (MethodHeader)ir;
            list = new Instruction[hdr.getInstructionCount()];
            int i = 0;
            while (ir != null) {
               /*
                * Do not include instructions eliminated by the optomizer
                */
                if (ir.getIP() != -1) {
                    list[i++] = ir;
                }
                ir = ir.getNext();
            }
            assume(i == list.length);

           /*
            * Assign offsets to the local variables
            */
            int localCount = hdr.getLocalCount();
            int offset = 0;
            for(i = 0 ; i < localCount ; i++) {
                Local local = hdr.getLocal(i);
                local.setOffset(offset);
                offset++;
                if (local.slotType() == Type.BASIC_LONG) {
                    offset++;
                }
            }
            activationSize = offset;

            if (parent.getVM().traceinstructions()) {
                InstructionTracer tracer =  new InstructionTracer(System.out);
                System.out.println("* "+ parent.name() + "::" + name);
                for (i = 0 ; i < list.length ; i++) {
                    System.out.print("-");
                    tracer.trace(list[i]);
                }
            }
        }
    }

    public IntermediateMethod asIrMethod() {
        return this;
    }

    public int getActivationSize() {
        return activationSize;
    }

    public Instruction[] getInstructions() {
        return list;
    }

    public Instruction getInstruction(int ip) {
        return list[ip];
    }

}
