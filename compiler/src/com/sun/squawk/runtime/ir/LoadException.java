
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class LoadException extends Instruction {

    private Target target;

    public static LoadException create(Target target) {
        return new LoadException(target);
    }

    private LoadException(Target target) {
        super(target.getExceptionTargetType());
        this.target = target;
        target.setTargetInstruction(this);
    }

    public Target target() { return target; }


    boolean isExceptionTarget() {
        return true;
    }

    public void visit(InstructionVisitor visitor) {
        visitor.doLoadException(this);
    }
}
