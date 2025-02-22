
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class NewObject extends Instruction {

    private boolean initalized = false;

    public static Instruction create(Type type) {
        return new NewObject(type);
    }

    protected NewObject(Type type) {
        super(type);
    }

    public boolean canTrap() {
        return true;
    }

    public boolean isUninitalizedNew() {
        return initalized;
    }

    public void setNewInitalized() {
    }

    public void visit(InstructionVisitor visitor) {
        visitor.doNewObject(this);
    }
}
