
package com.sun.squawk.test;

class MultiTest {

    Object o1 = new Object();
    Object o2 = new Object();

    public void foo() {

        Object o1 = new int[1];
        Object o2 = new int[2][];
        Object o3 = new int[3][][];
        Object o4 = new int[4][][][];

        Object o5 = new int[2][9];
        Object o6 = new int[3][9][];
        Object o7 = new int[4][9][][];

        Object o8 = new int[2][9];
        Object o9 = new int[3][9][8];
        Object o10 = new int[4][9][8][];
    }

}