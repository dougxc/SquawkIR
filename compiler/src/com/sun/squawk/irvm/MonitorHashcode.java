package com.sun.squawk.irvm;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;

public class MonitorHashcode extends InstanceObject {

    static int realTypeOffset;
    static Type monitorType;

    static {
        VirtualMachine vm =  VirtualMachine.TopVM();
        monitorType = vm.createType(null, "Ljava/lang/Monitor;");
        assume(monitorType != null);
        monitorType.load();
        Field field = monitorType.findField(vm.internString("realType"));
        assume(field != null);
        realTypeOffset = field.getSlotOffset();
    }

   /**
    * Constructor
    */
    public MonitorHashcode(Type type) {
        super(monitorType);
        assume(type != null);
        oopAtPut(realTypeOffset, type);
    }

    public Type realType() {
        Type type = (Type)oopAt(realTypeOffset);
        assume(type != null);
        return type;

    }
}
