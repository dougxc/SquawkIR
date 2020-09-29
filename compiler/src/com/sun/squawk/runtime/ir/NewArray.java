
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class NewArray extends Instruction {

    private Instruction size;

    public static Instruction create(Type type, Instruction size) {
        return new NewArray(type, size);
    }

    protected NewArray(Type type, Instruction size) {
        super(type);
        this.size = size;
    }

    public Instruction size() { return size; }

    public boolean canTrap() {
        return true;
    }

    public void visit(InstructionVisitor visitor) {
        visitor.doNewArray(this);
    }

    public void visit(ParameterVisitor visitor) {
        size  = visitor.doParameter(this, size);
    }
}
