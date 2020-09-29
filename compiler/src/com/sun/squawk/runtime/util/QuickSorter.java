package com.sun.squawk.runtime.util;

public class QuickSorter {

    Object[] elementData;
    Comparer comparer;

   /**
    * Public Constructor
    */
    public static void sort(Object[] elementData, Comparer comparer) {
        new QuickSorter(elementData, comparer);
    }

   /**
    * Public Constructor
    */
    private QuickSorter(Object[] elementData, Comparer comparer) {
        this.elementData = elementData;
        this.comparer    = comparer;
        if (elementData.length > 1) {
            quicksort(0, elementData.length - 1);
        }
    }

   /**
    * Quicksort
    */
    private void quicksort(int p, int r) {
        if(p < r) {
            int q = partition(p, r);
            if(q == r) {
                q--;
            }
            quicksort(p, q);
            quicksort(q+1, r);
        }
    }

   /**
    * partition
    */
    private int partition(int lo, int hi) {
        Object pivot = elementData[lo];
        while (true) {
            while(comparer.compare(elementData[hi], pivot) >= 0 && lo < hi) {
                hi--;
            }
            while(comparer.compare(elementData[lo], pivot) <  0 && lo < hi) {
                lo++;
            }
            if(lo < hi) {
                Object T        = elementData[lo];
                elementData[lo] = elementData[hi];
                elementData[hi] = T;
            } else {
                return hi;
            }
        }
    }

}