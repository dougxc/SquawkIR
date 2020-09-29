package com.sun.squawk.irvm;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;

public class InstanceObject extends RecordObject {

   /**
    * Constructor
    */
    public InstanceObject(Type type) {
        this(type, type.getInstanceSize());
    }

    protected InstanceObject(Type type, int size) {
        super(type, size);
        if (type != null) {
            Field[] fields = type.getFields();
            for (int i = 0 ; i < fields.length ; i++) {
                Field field = fields[i];
                int slot = field.getSlotOffset();
                if (slot >= 0) {
                    InstanceObject rec;
                    if (slot >= VirtualMachine.FIRSTGLOBAL) {
                        rec = GlobalObject.getGlobals();
                        slot -= VirtualMachine.FIRSTGLOBAL;
                        ((GlobalObject)rec).extendIfneeded(slot);
                    } else {
                        rec = this;
                    }
                    Object o = field.getInitialValue();
                    if (o != null) {
//prtn("s="+slot+" o="+o+" t="+type);
                        if        (o instanceof Integer) {
                            rec.intAtPut(slot, ((Integer)o).intValue());
                        } else if (o instanceof Long) {
                            rec.longAtPut(slot, ((Long)o).longValue());
                        } else if (o instanceof Float) {
                            rec.floatAtPut(slot, ((Float)o).floatValue());
                        } else if (o instanceof Double) {
                            rec.doubleAtPut(slot, ((Double)o).doubleValue());
                        } else {
                            shouldNotReachHere();
                        }
                    }
                }
            }
        }
    }

    public String toString() {
        //return "ArrayObject("+type()+")";
        String cname = type().toString();
        cname = cname.substring(cname.lastIndexOf('.')+1);
        //return "InstanceObject("+cname+")";

        StringBuffer sb = new StringBuffer();
        sb.append("InstanceObject(");
        sb.append(cname);
        sb.append(" : ");
        int size = type().getInstanceSize();
        for (int i = 0 ; i < size ; i++) {
            BasicObject obj = oopAt(i);
            if (obj != null) {
                if (obj instanceof InstanceObject) {
                    sb.append("this");
                } else {
                    sb.append(obj);
                }
            } else {
                sb.append(intAt(i));
            }
            sb.append(" ");
        }
        sb.append(") ");
        return sb.toString();
    }


    public void get(Field field, RecordObject rec, Instruction inst) {
        Type  type = field.type();
        int offset = field.getSlotOffset();

        assume (offset < VirtualMachine.FIRSTGLOBAL);

        switch (type.slotType()) {
            case Type.BASIC_OOP:  rec.oopAtPut(inst,  oopAt(offset));  break;
            case Type.BASIC_INT:  rec.intAtPut(inst,  intAt(offset));  break;
            case Type.BASIC_LONG: rec.longAtPut(inst, longAt(offset)); break;
        }
    }

    public void put(Field field, RecordObject rec, Instruction inst) {
        Type  type = field.type();
        int offset = field.getSlotOffset();

        assume (offset < VirtualMachine.FIRSTGLOBAL);

        switch (type.slotType()) {
            case Type.BASIC_OOP:  oopAtPut(offset,  rec.oopAt(inst));  break;
            case Type.BASIC_INT:  intAtPut(offset,  rec.intAt(inst));  break;
            case Type.BASIC_LONG: longAtPut(offset, rec.longAt(inst)); break;
        }
    }


    public BasicObject oopAt(String str) {
        Field field = type().findField(type().getVM().internString(str));
        assume(field != null);
        return oopAt(field.getSlotOffset());
    }

    public int intAt(String str) {
        Field field = type().findField(type().getVM().internString(str));
        assume(field != null);
        return intAt(field.getSlotOffset());
    }

    public void oopAtPut(String str, BasicObject value) {
        Field field = type().findField(type().getVM().internString(str));
        assume(field != null);
        oopAtPut(field.getSlotOffset(), value);
    }

    public void intAtPut(String str, int value) {
        Field field = type().findField(type().getVM().internString(str));
        assume(field != null);
        intAtPut(field.getSlotOffset(), value);
    }

}
