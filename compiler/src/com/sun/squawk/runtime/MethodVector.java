
package com.sun.squawk.runtime;
import  com.sun.squawk.runtime.util.*;


/**
 * This is a subclass of Vector that can sort all its entries into order.
 * All the entries must be of type BytecodeAddress, and the vector must
 * be exactly filled before the sort can take place.
 */
public class MethodVector extends java.util.Vector implements Comparer {

   /**
    * Constructor
    */
    public MethodVector(int size) {
        super(size);
    }

   /**
    * Return the array sorted
    */
    public Object[] sorted() {
        BaseFunctions.assume(size() == elementData.length);
        if (size() > 1) {
            QuickSorter.sort(elementData, this);
        }
        return elementData;
    }

    public int compare(Object a, Object b) {
        return ((Method)a).getSlotOffset() - ((Method)b).getSlotOffset();
    }

}