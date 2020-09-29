
package com.sun.squawk.test;

class FieldTest {

    int slot = 1;

    public static void main(String[] s) {
        FieldTest3 f = new FieldTest3();
        System.out.println(""+f.getSlot()+" "+f.getSuperSlot()+" "+f.getFieldTest2Slot()+" "+f.getFieldTestSlot());
    }

}

class FieldTest2 extends FieldTest  {

    int slot = 2;

}

class FieldTest3 extends FieldTest2  {

    int slot = 3;

    int getSlot() {
        return slot;
    }

    int getSuperSlot() {
        return super.slot;
    }

    int getFieldTest2Slot() {
        return ((FieldTest2)this).slot;
    }

    int getFieldTestSlot() {
        return ((FieldTest)this).slot;
    }

}