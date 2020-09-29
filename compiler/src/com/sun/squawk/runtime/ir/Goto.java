
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class Goto extends Instruction {

    private Target target;

    public static Goto create(Target target) {
        return new Goto(target);
    }

    protected Goto(Target target) {
        super(null);
        this.target = target;
    }

    public Target target() { return target; };

    public void visit(InstructionVisitor visitor) {
        visitor.doGoto(this);
    }
}
