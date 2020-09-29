package com.sun.squawk.irvm;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;

public abstract class BasicObject extends BaseFunctions {

   /**
    * Pointer to the object's type, or its MonitorHashcode.
    */
    BasicObject typeOrMonitor;

   /**
    * Constructor
    */
    public BasicObject(Type type) {
        this.typeOrMonitor = type;
    }

   /**
    * Fudge to make the type of Type.OBJECT and Type.CLASS equal to Type.CLASS
    */
    public void tweakMetatype(Type type) {
        assume(this == Type.OBJECT || this == Type.CLASS);
        typeOrMonitor = type;
    }

   /**
    * type()
    */
    public Type type() {
        if (typeOrMonitor instanceof Type) {
            return (Type)typeOrMonitor;
        } else {
            assume(typeOrMonitor instanceof MonitorHashcode,"?="+typeOrMonitor);
            return ((MonitorHashcode)typeOrMonitor).realType();
        }
    }

   /**
    * getMonitor()
    */
    public MonitorHashcode getMonitor() {
        if (typeOrMonitor instanceof MonitorHashcode) {
            return (MonitorHashcode)typeOrMonitor;
        } else {
            typeOrMonitor = new MonitorHashcode((Type)typeOrMonitor);
            return (MonitorHashcode)typeOrMonitor;
        }
    }

   /**
    * getHashCode()
    */
    public int getHashCode() {
        return getMonitor().hashCode();
    }
}
