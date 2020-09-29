
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.util.*;

public abstract class BytecodeAddress extends BaseFunctions {

   /**
    * IP address in the java bytecodes
    */
    private int ip;


   /**
    * Constructor
    */
    public BytecodeAddress(int ip) {
        this.ip = ip;
    }

   /**
    * Get the ip
    */
    public int getIP() {
        return ip;
    }

   /**
    * Get the ip
    */
    public int getBytecodeIP() {
        return ip;
    }

    public abstract int sortKey();
    public abstract int subKey();
    public abstract int opcode();
}
