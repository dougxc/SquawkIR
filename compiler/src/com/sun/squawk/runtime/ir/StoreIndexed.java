
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class StoreIndexed extends Instruction {

    private Instruction array;
    private Instruction index;
    private Instruction value;

    public static Instruction create(Instruction array, Instruction index, Instruction value) {
        return new StoreIndexed(array, index, value);
    }

    protected StoreIndexed(Instruction array, Instruction index, Instruction value) {
        super(null);
        this.array = array;
        this.index = index;
        this.value = value;
    }

    public Instruction array()  { return array; }
    public Instruction index()  { return index; }
    public Instruction value()  { return value; }

    public boolean canTrap() {
        return true;
    }

    public void visit(InstructionVisitor visitor) {
        visitor.doStoreIndexed(this);
    }

    public void visit(ParameterVisitor visitor) {
        array = visitor.doParameter(this, array);
        index = visitor.doParameter(this, index);
        value = visitor.doParameter(this, value);
    }
}
