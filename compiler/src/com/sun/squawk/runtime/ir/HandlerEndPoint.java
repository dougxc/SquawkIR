
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.util.*;

public class HandlerEndPoint extends BytecodeAddress implements RuntimeConstants {

   /**
    * Exception target
    */
    private Target target;

   /**
    * Constructor
    */
    public HandlerEndPoint(int ip, Target target) {
        super(ip);
        this.target = target;
    }

    public Target target() { return target; }

    public int sortKey() {
        return (getIP() << 4) + 0;
    }

    public int subKey() {
        return target.getIP();
    }


    public int opcode() {
        return  opc_handlerend;
    }
}
