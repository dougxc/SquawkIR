
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class MethodHeader extends Instruction {

    private int icount;
    private Object[] locals;

    public static Instruction create(int icount, Object[] locals) {
        return new MethodHeader(icount, locals);
    }

    protected MethodHeader(int icount, Object[] locals) {
        super(null);
        this.icount = icount;
        this.locals = locals;
    }

    public int   getInstructionCount() { return icount;           }
    public int   getLocalCount()       { return locals.length;    }
    public Local getLocal(int n)       { return (Local)locals[n]; }
/*
    public int getActivationSize() {
        int size = 0;
        for(int i = 0 ; i < locals.length ; i++) {
            Local local = (Local)locals[i];
            size++;
            if (local.slotType() == Type.BASIC_LONG) {
                size++;
            }
        }
        return size;
    }
*/
    public boolean includes(Object o) {
        for(int i = 0 ; i < locals.length ; i++) {
            if (locals[i] == o) {
                return true;
            }
        }
        return false;
    }

    public void visit(InstructionVisitor visitor) {
        visitor.doMethodHeader(this);
    }

}
