
package com.sun.squawk.runtime.loader;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;

public class LocalVector extends java.util.Vector implements Comparer {

   /**
    * Constructor
    */
    public LocalVector(int size) {
        super(size);
    }

   /**
    * Return the array sorted
    */
    public Object[] sort() {
        BaseFunctions.assume(size() == elementData.length);
        if (size() > 1) {
            QuickSorter.sort(elementData, this);
        }
        return elementData;
    }

    public int compare(Object a, Object b) {
        return ((Local)b).getUseCount() - ((Local)a).getUseCount(); // highest first
    }

}