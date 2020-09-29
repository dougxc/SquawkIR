
package com.sun.squawk.runtime;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;

import java.io.IOException;
import java.util.Vector;
import java.util.Enumeration;

public class VirtualMachine extends BaseFunctions {



   /* ------------------------------------------------------------------------ *\
    *                                C l a s s                                 *
   \* ------------------------------------------------------------------------ */

   /*
    * Empty lists
    */
    final public static Type[]   ZEROTYPES   = new Type[0];
    final public static Field[]  ZEROFIELDS  = new Field[0];
    final public static Method[] ZEROMETHODS = new Method[0];
    final public static Object[] ZEROOBJECTS = new Object[0];

   /**
    * Parent of this virtual machines
    */
    private static VirtualMachine topLevelVirtualMachine;

    public static VirtualMachine TopVM() {
        return topLevelVirtualMachine;
    }

    public static String INIT, CLINIT;


   /* ------------------------------------------------------------------------ *\
    *                              I n s t a n c e                             *
   \* ------------------------------------------------------------------------ */


   /**
    * The class loader for this VM
    */
    private ClassFileLoader loader;

   /**
    * Parent of this virtual machines
    */
    private VirtualMachine parentVirtualMachine;

   /**
    * Collection of all children of this virtual machines
    */
    private Vector childVirtualMachines = new Vector();

   /**
    * The class loading path
    */
    private String classPath = ".";

   /**
    * The name of the main class
    */
    private String mainClassName;

   /**
    * The type of the main class
    */
    private Type mainClass;

   /**
    * Command line arguments
    */
    private String[] args;

   /**
    * Array of global variables
    */
    private int[] globals;

   /**
    * Slot number of first global variable
    */
    public final static int FIRSTGLOBAL = 30000;

   /**
    * Offset to the next unused global
    */
    private int nextFreeGlobal = FIRSTGLOBAL+2; // zero and one are never used

   /**
    * Slot number of first interface slot
    */
    public final static int FIRSTINTERFACE = 10000;

   /**
    * Offset to the next unused interface slot
    */
    private int nextFreeInterfaceMethod = FIRSTINTERFACE+1;

   /**
    * The level of compiler optomization to be used
    */
    private int optimizationLevel = 1;

   /**
    * The maximum number of exceptions that the VM may throw, or zero for infinite
    */
    private int maxExceptions = 0;


   /*
    *
    */
    private void setMaxExceptions(String number) {
        try {
            maxExceptions = Integer.parseInt(number);
        } catch(NumberFormatException ex) {
            System.out.println("Bad -maxexceptions");
            System.exit(-1);
        }
    }

   /*
    * getMaxExceptions()
    */
    public int getMaxExceptions() {
        return maxExceptions;
    }


   /**
    * Flags
    */
    final static int TRACELOADING      = 1<<0;
    final static int TRACEPOOL         = 1<<1;
    final static int TRACEBYTECODES    = 1<<2;
    final static int TRACEIR0          = 1<<3;
    final static int TRACEIR1          = 1<<4;
    final static int TRACEFIELDS       = 1<<5;
    final static int EAGERLOADING      = 1<<6;
    final static int TRACELOCALS       = 1<<7;
    final static int TRACEIP           = 1<<8;
    final static int VERBOSE           = 1<<9;
    final static int TRACEEXEC         = 1<<10;
    final static int TRACEEXCEPTIONS   = 1<<11;
    final static int TRACEEVENTS       = 1<<12;
    final static int TRACEGRAPHICS     = 1<<13;
    final static int TRACETHREADS      = 1<<14;
    final static int TRACEINSTRUCTIONS = 1<<15;


    private int flags = 0;
    private String match;

    String prepData(String data) {
        data = data.replace('/', '.');
        data = data.replace('\\', '.');
        return data;
    }

    private boolean matches(String matchData) {
        if (match == null) {
           return true;
        }
        matchData = prepData(matchData);
        return matchData.indexOf(match) >= 0;
    }

    public boolean traceloading(String matchData) {
        return (flags & TRACELOADING) != 0 && matches(matchData);
    }

    public boolean tracepool(String matchData) {
        return (flags & TRACEPOOL) != 0 && matches(matchData);
    }

    public boolean tracebytecodes(String matchData) {
        return (flags & TRACEBYTECODES) != 0 && matches(matchData);
    }

    public boolean traceir0(String matchData) {
        return (flags & TRACEIR0) != 0 && matches(matchData);
    }

    public boolean traceir1(String matchData) {
        return (flags & TRACEIR1) != 0 && matches(matchData);
    }

    public boolean tracefields(String matchData) {
        return (flags & TRACEFIELDS) != 0 && matches(matchData);
    }

    public boolean traceip(String matchData) {
        return (flags & TRACEIP) != 0 && matches(matchData);
    }

    public boolean eagerloading() {
        return (flags & EAGERLOADING) != 0;
    }

    public boolean tracelocals(String matchData) {
        return (flags & TRACELOCALS) != 0 && matches(matchData);
    }

    public boolean traceexec() {
        return (flags & TRACEEXEC) != 0;
    }

    public boolean traceexceptions() {
        return (flags & TRACEEXCEPTIONS) != 0;
    }

    public boolean traceevents() {
        return (flags & TRACEEVENTS) != 0;
    }

    public boolean tracegraphics() {
        return (flags & TRACEGRAPHICS) != 0;
    }

    public boolean tracethreads() {
        return (flags & TRACETHREADS) != 0;
    }

    public boolean traceinstructions() {
        return (flags & TRACEINSTRUCTIONS) != 0;
    }

    public boolean verbose() {
        return (flags & VERBOSE) != 0;
    }

   /**
    * The main routine
    */
    public static void main(String[] args) throws Throwable {
        new VirtualMachine(args).start();
    }

   /**
    * Constructor for initial VM
    */
    private VirtualMachine(String[] args) throws IOException, VerificationException {

        INIT   = internString("<init>");
        CLINIT = internString("<clinit>");

        int i = 0;
        for ( ; i < args.length ; i++) {
            if (args[i].charAt(0) != '-') {
                break;
            }
            if (args[i].equals("-classpath") || args[i].equals("-cp")) {
                classPath = args[++i];
            } else if (args[i].equals("-maxexceptions")) {
                setMaxExceptions(args[++i]);
            } else if (args[i].equals("-matching")) {
                match = prepData(args[++i]);
            } else if (args[i].equals("-o0")) {
                optimizationLevel = 0;
            } else if (args[i].equals("-o1")) {
                optimizationLevel = 1;
            } else if (args[i].equals("-o2")) {
                optimizationLevel = 2;
            } else if (args[i].equals("-eagerloading")) {
               flags |= EAGERLOADING;
            } else if (args[i].equals("-traceloading")) {
               flags |= TRACELOADING;
            } else if (args[i].equals("-tracefields")) {
               flags |= TRACEFIELDS;
            } else if (args[i].equals("-tracepool")) {
               flags |= TRACEPOOL;
            } else if (args[i].equals("-tracebytecodes")) {
               flags |= TRACEBYTECODES;
            } else if (args[i].equals("-traceir0")) {
               flags |= TRACEIR0;
            } else if (args[i].equals("-traceir1")) {
               flags |= TRACEIR1;
            } else if (args[i].equals("-tracelocals")) {
               flags |= TRACELOCALS;
            } else if (args[i].equals("-traceip")) {
               flags |= TRACEIP;
            } else if (args[i].equals("-verbose")) {
               flags |= VERBOSE;
            } else if (args[i].equals("-traceexec")) {
               flags |= TRACEEXEC;
            } else if (args[i].equals("-traceexceptions")) {
               flags |= TRACEEXCEPTIONS;
            } else if (args[i].equals("-traceevents")) {
               flags |= TRACEEVENTS;
            } else if (args[i].equals("-tracegraphics")) {
               flags |= TRACEGRAPHICS;
            } else if (args[i].equals("-tracethreads")) {
               flags |= TRACETHREADS;
            } else if (args[i].equals("-traceinstructions")) {
               flags |= TRACEINSTRUCTIONS;
            } else if (args[i].equals("-traceall")) {
               flags |= TRACELOADING;
               flags |= TRACEPOOL;
               flags |= TRACEBYTECODES;
               flags |= TRACEIR0;
               flags |= TRACEIR1;
               flags |= TRACEFIELDS;
               flags |= TRACELOCALS;
               flags |= TRACEIP;
               flags |= TRACEEXEC;
               flags |= TRACEEXCEPTIONS;
               flags |= TRACEEVENTS;
               flags |= TRACEGRAPHICS;
               flags |= TRACETHREADS;
               flags |= TRACEINSTRUCTIONS;
            } else {
                throw fatal("Bad switch "+args[i]);
            }
        }

        mainClassName = args[i++];
        this.args = new String[args.length - i];
        System.arraycopy(args, i, this.args, 0, this.args.length);

        globals = new int[32];
        topLevelVirtualMachine = this;
        loader = ClassFileLoader.create(this, classPath);
        Type.initialize(this);

    }

   /**
    * The type of the main class
    */
    public Type mainClass() {
        return mainClass;
    }

   /**
    * The main routine
    */
    public String[] getArgs() {
        return args;
    }


   /**
    * Constructor for forked VMs
    */
    public VirtualMachine(VirtualMachine parentVirtualMachine, String mainClassName, String[] args) throws IOException, VerificationException {

        parentVirtualMachine.childVirtualMachines.addElement(this);

        this.mainClassName           = mainClassName;
        this.args                    = args;
        this.parentVirtualMachine    = parentVirtualMachine;
        this.classPath               = parentVirtualMachine.classPath;
        this.flags                   = parentVirtualMachine.flags;
        this.optimizationLevel       = parentVirtualMachine.optimizationLevel;
        this.nextFreeGlobal          = parentVirtualMachine.nextFreeGlobal;
        this.nextFreeInterfaceMethod = parentVirtualMachine.nextFreeInterfaceMethod;

        this.globals = new int[parentVirtualMachine.globals.length];
        System.arraycopy(parentVirtualMachine.globals, 0, this.globals, 0, globals.length);

        loader = ClassFileLoader.create(this, classPath);
    }

   /**
    * Load a class into this VM
    */
    public Type load(String className) throws IOException, VerificationException {
        return loader.load(className);
    }

   /**
    * Start the VM running
    */
    public void start() throws IOException, VerificationException {
        mainClass = load(mainClassName);
        //mainClass = Type.createForVM(this, null, mainClassName);
        mainClass.convert();
/*
        prtn("strings = "+internedStrings.size());
        prtn("types = "+internedTypes.size());

        for(Enumeration e = internedStrings.elements() ; e.hasMoreElements() ;) {
             String str = (String)e.nextElement();
             prtn("\t"+str);
        }
*/
    }

    /**
     * Allocate a global variable
     */
    public int allocateGlobal(int size) {
        int result = nextFreeGlobal;
        nextFreeGlobal += size;
        return result;
    }

    /**
     * Get the number the next interface method will be
     */
    public int nextInterfaceMethod() {
        return nextFreeInterfaceMethod;
    }

    /**
     * Allocate an interface vtable entry
     */
    public int allocateInterfaceMethod() {
        int res = nextFreeInterfaceMethod++;
        return res;
    }


    /**
     * Return the compiler optimization level
     */
    public int optimizationLevel() {
        return optimizationLevel;
    }


   /* ------------------------------------------------------------------------ *\
    *                      Object type database managemant                     *
   \* ------------------------------------------------------------------------ */

   /**
    * Hashtale to keep track of existing strings
    */
    private ArrayHashtable internedTypes = new ArrayHashtable(32);

   /**
    * Find an interned type
    */
    public Type findType(String name) {
        Type type = (Type)internedTypes.get(name);
        if (type == null && parentVirtualMachine != null) {
            type = parentVirtualMachine.findType(name);
        }
        return type;
    }

   /**
    * Get an interned type
    */
    public Type createType(Type superType, String name) {
        Type type = findType(name);
        if (type == null) {
//prtn("calling createForVM for "+name);
            type = Type.createForVM(this, superType, name);
//prtn("returned createForVM for "+name);
            internedTypes.put(name, type);
        } else {
            type.setSuperType(superType);
        }
        return type;
    }


   /* ------------------------------------------------------------------------ *\
    *                              Type[] interning                            *
   \* ------------------------------------------------------------------------ */

   /**
    * Find an interned member
    */
    private Type[] findList(Type[] list) {
        Type[] list2 = (Type[])internedTypes.get(list);
        if (list2 == null && parentVirtualMachine != null) {
            list2 = parentVirtualMachine.findList(list);
        }
        return list2;
    }

   /**
    * Get an interned member
    */
    public Type[] internList(Type[] list) {
        Type[] list2 = findList(list);
        if (list2 != null) {
            return list2;
        }
        internedTypes.put(list, list);
        return list;
    }


   /* ------------------------------------------------------------------------ *\
    *                             String interning                             *
   \* ------------------------------------------------------------------------ */

   /**
    * Hashtale to keep track of existing classes
    */
    private ArrayHashtable internedStrings = new ArrayHashtable(64);

   /**
    * Find an interned string
    */
    private String findString(String string) {
        string = (String)internedStrings.get(string);
        if (string == null && parentVirtualMachine != null) {
            string = parentVirtualMachine.findString(string);
        }
        return string;
    }

   /**
    * Get an interned class
    */
    public String internString(String string) {
        if (string != null) {
            String s = findString(string);
            if (s == null) {
                internedStrings.put(string, string);
                s = string;
            }
            return s;
        }
        return null;
    }


}
