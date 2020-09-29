package com.sun.squawk.irvm;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;

public class GlobalObject extends InstanceObject {

   /**
    * Static vector of global variables
    */
    private static GlobalObject globals = new GlobalObject();

    public static InstanceObject getGlobals() {
        return globals;
    }

   /**
    * Constructor
    */
    public GlobalObject() {
        super(null, 0);
    }

   /**
    * extendOfneeded
    */
    public void extendIfneeded(int slot) {
        slot++; // for longs
        if (intArray.length <= slot) {
            int[]         new_intArray = new int[slot+1];
            BasicObject[] new_oopArray = new BasicObject[slot+1];
            System.arraycopy(intArray, 0, new_intArray, 0, intArray.length);
            System.arraycopy(oopArray, 0, new_oopArray, 0, intArray.length);
            intArray = new_intArray;
            oopArray = new_oopArray;
        }
    }


    public void get(Field field, RecordObject rec, Instruction inst) {
        Type  type = field.type();
        int offset = field.getSlotOffset();

        assume (offset >= VirtualMachine.FIRSTGLOBAL);
        offset -= VirtualMachine.FIRSTGLOBAL;
        globals.extendIfneeded(offset);

        switch (type.slotType()) {
            case Type.BASIC_OOP:  rec.oopAtPut(inst,  oopAt(offset));  break;
            case Type.BASIC_INT:  rec.intAtPut(inst,  intAt(offset));  break;
            case Type.BASIC_LONG: rec.longAtPut(inst, longAt(offset)); break;
        }
    }

    public void put(Field field, RecordObject rec, Instruction inst) {
        Type  type = field.type();
        int offset = field.getSlotOffset();

        assume (offset >= VirtualMachine.FIRSTGLOBAL);
        offset -= VirtualMachine.FIRSTGLOBAL;
        globals.extendIfneeded(offset);

        switch (type.slotType()) {
            case Type.BASIC_OOP:  oopAtPut(offset,  rec.oopAt(inst));  break;
            case Type.BASIC_INT:  intAtPut(offset,  rec.intAt(inst));  break;
            case Type.BASIC_LONG: longAtPut(offset, rec.longAt(inst)); break;
        }
    }

}
