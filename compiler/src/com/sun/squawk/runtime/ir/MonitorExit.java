
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class MonitorExit extends Instruction {

    private Instruction value;

    public static Instruction create(Instruction value) {
        return new MonitorExit(value);
    }

    protected MonitorExit(Instruction value) {
        super(null);
        this.value = value;
    }

    public Instruction value() { return value; };

    public boolean canTrap() {
        return true;
    }

    public void visit(InstructionVisitor visitor) {
        visitor.doMonitorExit(this);
    }

    public void visit(ParameterVisitor visitor) {
        value = visitor.doParameter(this, value);
    }
}
