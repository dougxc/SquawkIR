
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class IfOp extends Goto {

    private int op;
    private Instruction left;
    private Instruction right;
 //   private Target target;

    public static IfOp create(int op, Instruction left, Instruction right, Target target) {
        return new IfOp(op, left, right, target);
    }

    public IfOp(int op, Instruction left, Instruction right, Target target) {
        super(target);
        this.op = op;
        this.left = left;
        this.right = right;
 //       this.target = target;
    }

    public int op()             { return op;     }
    public Instruction left()   { return left;   }
    public Instruction right()  { return right;  }
//    public Target target()      { return target; }

    public void visit(InstructionVisitor visitor) {
        visitor.doIfOp(this);
    }

    public void visit(ParameterVisitor visitor) {
        left  = visitor.doParameter(this, left);
        right = visitor.doParameter(this, right);
    }
}
