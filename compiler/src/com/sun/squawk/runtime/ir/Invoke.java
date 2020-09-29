
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public class Invoke extends Instruction {

    private Method method; //temp
    private Type parent;
    private int  offset;
    private Instruction[] parms;
    private boolean isVirtual;
    //private static Instruction[] clinitParms = new Instruction[] {null};


    public static Instruction create(Method method, Instruction[] parms, boolean isVirtual) {
        return new Invoke(method, parms, isVirtual);
    }

    //public static Instruction clinit(Method method) {
    //    return new Invoke(method, clinitParms);
    //}

    protected Invoke(Method method, Instruction[] parms, boolean isVirtual) {
        super(method.type());
        this.method = method; // temp
        this.parent = parent;
        this.offset = method.getSlotOffset();
        this.parms  = parms;
        this.isVirtual  = isVirtual;
    }

    public Method method()          { return method; } //temp
    public Type   parent()          { return parent; }
    public int    offset()          { return offset; }
    public Instruction[] parms()    { return parms;  }
    public boolean isVirtual()      { return isVirtual; }

    public boolean canTrap() {
        return true;
    }

    public void visit(InstructionVisitor visitor) {
        visitor.doInvoke(this);
    }

    public void visit(ParameterVisitor visitor) {
        for (int i = 0 ; i < parms.length ; i++) {
            parms[i] = visitor.doParameter(this, parms[i]);
        }
    }
}
