
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class CheckCast extends Instruction {

    private Type checkType;
    private Instruction value;

    public static Instruction create(Type checkType, Instruction value) {
        return new CheckCast(checkType, value);
    }

    protected CheckCast(Type checkType, Instruction value) {
        super(checkType);
        this.checkType = checkType;
        this.value = value;
    }

    public Type checkType()     { return checkType; }
    public Instruction value()  { return value;     }

    public boolean canTrap() {
        return true;
    }

    public void visit(InstructionVisitor visitor) {
        visitor.doCheckCast(this);
    }

    public void visit(ParameterVisitor visitor) {
        value = visitor.doParameter(this, value);
    }
}
