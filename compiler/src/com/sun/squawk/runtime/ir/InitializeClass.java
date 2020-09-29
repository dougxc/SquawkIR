
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;
import  java.util.Enumeration;

public class InitializeClass extends Instruction {

    private Type parent;

    private static ArrayHashtable exclude;

    static {
       exclude = new ArrayHashtable();
       exclude.put(Type.OBJECT,       exclude);
       exclude.put(Type.STRING,       exclude);
       exclude.put(Type.CLASS,        exclude);
       exclude.put(Type.THREAD,       exclude);
       exclude.put(Type.SYSTEM,       exclude);
       exclude.put(Type.STRINGBUFFER, exclude);
    }

   /*
    * Retuen an enumeration of the types that the VM should pre-initialize.
    */
    public static Enumeration getPreInitializedList() {
        return exclude.keys();
    }

    public static Instruction create(Type type) {
       /*
        * Some types are initialized by the system. If this is one of those
        * then return null because no instruction is required.
        */
        if (exclude.get(type) != null) {
            return null;
        }
        return new InitializeClass(type);
    }

    public boolean canTrap() {
        return true;
    }

    private InitializeClass(Type parent) {
        super(null);
        this.parent = parent;
    }

    public Type parent()  { return parent; }

    public void visit(InstructionVisitor visitor) {
        visitor.doInitializeClass(this);
    }
}
