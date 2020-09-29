
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class StoreField extends Instruction {

    private Field field;
    private Instruction ref;
    private Instruction value;

    public static Instruction create(Field field, Instruction ref, Instruction value) {
        return new StoreField(field, ref, value);
    }

    protected StoreField(Field field, Instruction ref, Instruction value) {
        super(null);
        this.field = field;
        this.ref   = ref;
        this.value = value;
        assume(field.getSlotOffset() != -1);
    }

    public Field field()        { return field; }
    public Instruction ref()    { return ref; }
    public Instruction value()  { return value; }

    public boolean canTrap() {
        return true;
    }

    public void visit(InstructionVisitor visitor) {
        visitor.doStoreField(this);
    }

    public void visit(ParameterVisitor visitor) {
        if (ref != null) {
            ref = visitor.doParameter(this, ref);
        }
        value = visitor.doParameter(this, value);
    }
}
