
package com.sun.squawk.runtime.loader;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.util.*;

public class TypeProxy extends Type {

    private Type proxy;

   /**
    * Static constructor only called from the VirtualMachine.java
    */
    public static TypeProxy createForMap(VirtualMachine vm, Type superType, String name) {
        return new TypeProxy(vm, superType, name);
    }

    public TypeProxy(VirtualMachine vm, Type superType, String name) {
        super(vm, superType, name);
    }

    public void setProxy(Type proxy) {
        this.proxy = proxy;
    }

    public Type getProxy() {
        return proxy;
    }
}
