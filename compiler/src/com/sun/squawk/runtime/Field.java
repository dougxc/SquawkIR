
package com.sun.squawk.runtime;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;

import java.io.IOException;

public class Field extends Member {

   /**
    * Offset into the instance or global vector
    */
    private int offset;

   /**
    * Value for the field to be initialized to
    */
    private Object initialValue;

   /**
    * Private constructor
    */
    private Field(Type parent, String name, Type type, int flags) {
        super(parent, name, type, flags);
    }

   /**
    * Public constructor
    */
    public static Field create(VirtualMachine vm, InputContext ctx, Type parent, String name, String descriptor, int flags) {
        Field field = new Field(parent, name, parent.createType(descriptor), flags);
        field.setFlag(flags);
        return field;
    }

   /**
    * Loading
    */
    public void load() throws IOException, VerificationException {
        if (!isLoaded()) {
            parent().load();
            type().load();
            setLoaded();
        }
    }

   /**
    * Set slot
    */
    public void setSlot(int offset, Object initialValue) {
        this.offset = offset;
        this.initialValue = initialValue;
    }

   /**
    * Get slot offset
    */
    public int getSlotOffset() {
        return offset;
    }

   /**
    * Get slot initial value
    */
    public Object getInitialValue() {
        return initialValue;
    }

}
