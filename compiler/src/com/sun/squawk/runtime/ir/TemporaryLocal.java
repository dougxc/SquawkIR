
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.util.*;

public class TemporaryLocal extends Local {

    private int referenceCount;

    public TemporaryLocal(int slotType) {
        super(slotType);
    }

    public void incrementReferenceCount() {
        referenceCount++;
    }

    public boolean decrementReferenceCount() {
        return --referenceCount == 0;
    }

    public String toString() {
        return "t"+idstr();
    }
}
