
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.loader.*;

public class DropLocal extends Instruction {

    private Local local;

    public static Instruction create(Local local) {
        return new DropLocal(local);
    }

    private DropLocal(Local local) {
        super(null);
        this.local   = local;
    }

    public Local local()      { return local; }

    public void visit(InstructionVisitor visitor) {
        visitor.doDropLocal(this);
    }
}
