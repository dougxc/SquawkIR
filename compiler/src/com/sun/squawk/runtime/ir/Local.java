
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.util.*;

public class Local extends BaseFunctions {

    private int slotType;
    private int useCount;
    private int parameterNumber = -1;
    private int id;
    private int offset = -1;

    public Local(int slotType) {
        this.slotType = slotType;
    }

    public int slotType() {
       return slotType;
    }

   // public void incrementUseCount() {
   //     useCount++;
   // }

   // public void decrementUseCount() {
   //     --useCount;
   // }

    public void addUseCount(int n) {
        useCount += n;
    }

    public int getUseCount() {
        return useCount;
    }

    public void setParameterNumber(int n) {
        parameterNumber = n;
    }

    public int getParameterNumber() {
        return parameterNumber;
    }

    public void setID(int n) {
        id = n;
    }

    public String toString() {
        return "l"+idstr();
    }

    protected String idstr() {
        if (slotType == Type.BASIC_INT) {
            return ""+id;
        } else if(slotType == Type.BASIC_LONG) {
            return ""+id+"L";
        } else {
            return ""+id+"#";
        }
    }

    public void setOffset(int n) {
        offset = n;
    }

    public int getOffset() {
        return offset;
    }



    //void incrementReferenceCount() {
    //    shouldNotReachHere();
    //}

    //boolean decrementReferenceCount() {
    //    shouldNotReachHere();
    //}

}


/*
    t3&         = new Ljava/lang/Double;
    t7L         = const(1.0D)


    if l3 >= const(0)    goto 147
    if l3 >= const(1)    goto 141
    t8L         = const(2.0D)
    goto 174
141:
    t8L         = const(3.0D)
    goto 174
147:
    if l3 >= const(2)    goto 159
    t8L         = const(4.0D)
    goto 174
159:
    if l3 >= const(3)    goto 171
    t8L         = const(5.0D)
    goto 174
171:
    t8L         = const(6.0D)
174: phi t8L = 5 sources


    t7L         = t8L * t7L
    invoke <init>  t3& t7L
*/