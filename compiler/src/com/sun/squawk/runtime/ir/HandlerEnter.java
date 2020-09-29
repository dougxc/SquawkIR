
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class HandlerEnter extends Instruction {

    private Target target;

    public static Instruction create(Target target) {
        return new HandlerEnter(target);
    }

    protected HandlerEnter(Target target) {
        super(null);
        this.target = target;
    }

    public Target target() { return target; }

    public void visit(InstructionVisitor visitor) {
        visitor.doHandlerEnter(this);
    }

}
