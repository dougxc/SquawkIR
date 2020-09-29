package com.sun.squawk.irvm;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;
import  com.sun.squawk.runtime.loader.*;
import  java.util.Stack;
import  java.util.Vector;
import  java.util.Enumeration;

public class Interpreter extends InstructionVisitor implements RuntimeConstants {

   /**
    * The virtual machine context
    */
    private VirtualMachine vm;

   /**
    * The type whose main() should be called
    */
    private Type mainType;

   /**
    * The application arguments
    */
    private String[] mainArgs;

   /**
    * The current activation record
    */
    protected ActivationObject ar;

   /**
    * The current instruction pointer
    */
    private int ip;

   /**
    * Flag to say the program is running
    */
    private boolean running;

   /**
    * Flag to say if thread preemption is allowed
    */
    private boolean preemption;

   /**
    * Number of machine cycles between each premption point.
    */
    final static int TIMESLICE = 1000;

   /**
    * Number of machine cycles until the next premption point.
    */
    private int preemptionCounter;

   /**
    * Flag to prevent monitor use as the system is starting up
    */
    private boolean allowMonitors = false;

   /**
    * Code to exit with
    */
    private int exitCode;

   /**
    * Field offsets of Thread.ar and Thread.ip
    */
    private int thread_ar     = -1;
    private int thread_ip     = -1;
    private int thread_number = -1;

   /**
    * Method for various things
    */
    private IntermediateMethod call_Class_initialize;
    private IntermediateMethod call_Thread_callMain;
    private IntermediateMethod call_Thread_yield;
    private IntermediateMethod call_Thread_block;
    private IntermediateMethod call_Thread_callRun;
    private IntermediateMethod call_Thread_monitorEnter;
    private IntermediateMethod call_Thread_monitorExit;
    private IntermediateMethod call_Thread_monitorWait;
    private IntermediateMethod call_Thread_monitorNotify;
    private IntermediateMethod call_Application_main;

   /**
    * List of threads that are ready to be restarted
    */
    private Vector threadsToRestart;

   /**
    * Table that holds the initializing thread of each class
    */
    private ArrayHashtable classInitalizing = new ArrayHashtable();

   /**
    * The place various native functions are factored out to
    */
    protected Native natve;

   /**
    * Instruction Traceing flag
    */
    private boolean instructionTracing = false;
    private int counter;

    private boolean instructionTracing() {
        if (tracer != null && instructionTracing) {
            counter++;
            if (counter == 97) {
                counter = 0;
                return true;
            }
        }
        return false;
    }

   /**
    * InstructionTracer
    */
    InstructionTracer tracer;

   /**
    * The maximum number of exceptions that the VM may throw, or zero for infinite
    */
    int maxexceptions;

   /**
    * Private constructor
    */
    private Interpreter(VirtualMachine vm) {
        this.vm = vm;
        natve = new Native(this, vm);
    }


   /* ------------------------------------------------------------------------ *\
    *               Linkage to activation record accessor methods              *
   \* ------------------------------------------------------------------------ */


    protected BasicObject oopAt      (int index)                           { return ar.oopAt(index);              }
    protected int         intAt      (int index)                           { return ar.intAt(index);              }
    protected long        longAt     (int index)                           { return ar.longAt(index);             }
    protected float       floatAt    (int index)                           { return ar.floatAt(index);            }
    protected double      doubleAt   (int index)                           { return ar.doubleAt(index);           }

    protected void        oopAtPut   (int index, BasicObject value)        {        ar.oopAtPut(index, value);    }
    protected void        intAtPut   (int index, int         value)        {        ar.intAtPut(index, value);    }
    protected void        longAtPut  (int index, long        value)        {        ar.longAtPut(index, value);   }
    protected void        floatAtPut (int index, float       value)        {        ar.floatAtPut(index, value);  }
    protected void        doubleAtPut(int index, double      value)        {        ar.doubleAtPut(index, value); }

    protected BasicObject oopAt      (Instruction inst)                    { return ar.oopAt(inst);               }
    protected int         intAt      (Instruction inst)                    { return ar.intAt(inst);               }
    protected long        longAt     (Instruction inst)                    { return ar.longAt(inst);              }
    protected float       floatAt    (Instruction inst)                    { return ar.floatAt(inst);             }
    protected double      doubleAt   (Instruction inst)                    { return ar.doubleAt(inst);            }

    protected void        oopAtPut   (Instruction inst, BasicObject value) {        ar.oopAtPut(inst, value);     }
    protected void        intAtPut   (Instruction inst, int         value) {        ar.intAtPut(inst, value);     }
    protected void        longAtPut  (Instruction inst, long        value) {        ar.longAtPut(inst, value);    }
    protected void        floatAtPut (Instruction inst, float       value) {        ar.floatAtPut(inst, value);   }
    protected void        doubleAtPut(Instruction inst, double      value) {        ar.doubleAtPut(inst, value);  }

   /**
    * oopAtPutCheck
    */
    protected void oopAtPutCheck(int index, BasicObject value) {
        if (index >= 0) {
            oopAtPut(index, value);
        }
    }

   /**
    * intAtPutCheck
    */
    protected void intAtPutCheck(int index, int value) {
        if (index >= 0) {
            intAtPut(index, value);
        }
    }

   /**
    * longAtPutCheck
    */
    protected void longAtPutCheck(int index, long value) {
        if (index >= 0) {
            longAtPut(index, value);
        }
    }


   /**
    * makeString
    */
    protected InstanceObject makeString(String str) {
        if (str == null) {
            return null;
        }
        InstanceObject stringObject = new InstanceObject(Type.STRING);
        stringObject.oopAtPut("value",  ArrayObject.create(str));
        stringObject.intAtPut("offset", 0);
        stringObject.intAtPut("count",  str.length());
        return stringObject;
    }

   /**
    * stringAtPut - Build a string and place in a local variable
    */
    protected void stringAtPut(Instruction inst, String str) {
        oopAtPut(inst, makeString(str));
    }

   /**
    * convertArgs - Convert the programs's imput arguments to the corrisponding internal
    *               data strustures.
    */
    protected BasicObject convertArgs(String[] args) {
        ArrayObject argArray = ArrayObject.create(Type.OBJECT_ARRAY, args.length);
        for (int i = 0 ; i < args.length ; i++) {
            argArray.oopAtPut(i, makeString(args[i]));
        }
        return argArray;
    }


   /**
    * getString
    */
    protected String getString(InstanceObject str) {
        ArrayObject value = (ArrayObject)str.oopAt("value");
        int offset = str.intAt("offset");
        int count  = str.intAt("count");
        return value.substring(offset, count);
    }

   /**
    * blockThread
    */
    protected void blockThread(Instruction inst) {
        preemption = false;
        doInvokeInternal(call_Thread_block, null, inst);
    }

   /**
    * restartThread
    */
    protected void restartThread(BasicObject thread) {
        threadsToRestart.addElement(thread);
        preemptionCounter = 1;
    }

   /* ------------------------------------------------------------------------ *\
    *                                 Main loop                                *
   \* ------------------------------------------------------------------------ */


    private String stackTrace(ActivationObject act, int ip) {
        StringBuffer sb = new StringBuffer();
        while (act != null) {
            sb.append("method = "+act.getMethod().parent()+"::"+act.getMethod().name());
            Instruction inst = act.getInstruction(ip);
            int line = inst.getLine();
            if (line >= 0) {
                sb.append(" line = "+line);
            } //else {
                sb.append(" ip = "+ip+"\n");
            //}
            Instruction caller = act.caller();
            if (caller != null) {
                ip = caller.getIP();
            } else {
                ip = -1;
            }
            act = act.previousActivation();
        }
        return sb.toString();
    }


   /**
    * traceException
    */
    private void traceException(Throwable ex) {
        if (vm.traceexceptions()) {
            System.out.println("\n\n *** Trying to throw "+ex+ " ***\n");
            System.out.println(stackTrace(ar, ip));
            ex.printStackTrace();
            System.out.println("\n\n");
        }
    }

   /**
    * traceInstruction
    */
    private void traceInstruction(Instruction inst) {
        if (vm.traceexec()) {
            String lastPut = ar.lastPut();
            if (lastPut != null) {
                System.out.println("===>"+lastPut);
            }
            if (inst instanceof Invoke) {
                System.out.println("\n\n\n");
            }
            Method method = ar.getMethod();
            String preable = method.parent().name() + "::" + method.name() + "                                   ";
            preable = preable.substring(0, 40);
            if (!preemption) {
                System.out.print("***");
            }
            System.out.print(preable);
            GraphPrinter.printOne(System.out, inst, vm);
        }

        if (instructionTracing()) {
            System.out.print("+");
            tracer.trace(inst);
        }
    }

   /**
    * getMethod
    */
    private IntermediateMethod getMethod(Type type, String name, String desc) {
        type.load();
        IntermediateMethod method = type.findIntermediateMethod(vm.internString(name), desc);
        if (method == null) {
            throw new RuntimeException("Cannot find "+name+"() in "+type);
        }
        return method;
    }

   /**
    * initialize
    */
    private void initialize(Type mainType, String[] mainArgs) {

       /*
        * Setup the max number of exceptions allowed
        */
        maxexceptions = vm.getMaxExceptions();
        if (maxexceptions != 0) {
            maxexceptions++; // count stops at one
        }

       /*
        * Setup the type of the main class and its arguments
        */
        this.mainType = mainType;
        this.mainArgs = mainArgs;

       /*
        * Initialize thread ready queue
        */
        threadsToRestart = new Vector();

       /*
        * Setup the initial preemption values. Preemption is initially
        * disabled by seting preemptionCounter to Integer.MAX_VALUE
        * This is reset when the main thread calls startPreemptionScheduling()
        */
        preemption        = true;
        preemptionCounter = Integer.MAX_VALUE;

       /*
        * Find various methods
        */
        call_Application_main       = getMethod(mainType,    "main",            "([Ljava/lang/String;)V");
        call_Class_initialize       = getMethod(Type.CLASS,  "initialize",      "(Ljava/lang/Class;)V");
        call_Thread_callMain        = getMethod(Type.THREAD, "callMain",        "()V");
        call_Thread_yield           = getMethod(Type.THREAD, "yield",           "()V");
        call_Thread_block           = getMethod(Type.THREAD, "block",           "()V");
        call_Thread_callRun         = getMethod(Type.THREAD, vm.tracethreads() ? "callRunTracing" : "callRun",  "()V");
        call_Thread_monitorEnter    = getMethod(Type.THREAD, "monitorEnter",    "(Ljava/lang/Monitor;)V");
        call_Thread_monitorExit     = getMethod(Type.THREAD, "monitorExit",     "(Ljava/lang/Monitor;)V");
        call_Thread_monitorWait     = getMethod(Type.THREAD, "monitorWait",     "(Ljava/lang/Object;Ljava/lang/Monitor;J)V");
        call_Thread_monitorNotify   = getMethod(Type.THREAD, "monitorNotify",   "(Ljava/lang/Object;Ljava/lang/Monitor;J)V");

       /*
        * Find the slot offset for ar and ip in Thread
        */
        Field threadar     = Type.THREAD.findField(vm.internString("ar"));
        Field threadip     = Type.THREAD.findField(vm.internString("ip"));
        Field threadnumber = Type.THREAD.findField(vm.internString("threadNumber"));

        assume(threadar     != null);
        assume(threadip     != null);
        assume(threadnumber != null);

        thread_ar     = threadar.getSlotOffset();
        thread_ip     = threadip.getSlotOffset();
        thread_number = threadnumber.getSlotOffset();

       /*
        * Call various <clinit> functions.
        */
        for (Enumeration list = InitializeClass.getPreInitializedList() ; list.hasMoreElements() ;) {
            Type type = (Type)list.nextElement();
            if (doInitializeClass(type, null)) {
                interpret();
            }
        }

       /*
        * Now monitiors will work
        */
        allowMonitors = true;

       /*
        * Call main()
        *
        * Note - If the class has an initializer that needs calling then an InitialiseClass instruction
        * will have been placed at the begining of main() by the graph builder.
        */
        doInvokeInternal(call_Thread_callMain, null, null);
    }

   /**
    * The main loop
    */
    private int interpret() {
        int lastIP;
        running = true;
        final boolean trace = vm.traceexec() || instructionTracing();

        while (running) {
            assume(ar != null);
            lastIP = ip;
            Instruction inst = ar.getInstruction(ip++);
            if (trace) {
                traceInstruction(inst);
            }
            try {
                inst.visit(this);
            } catch (Throwable ex) {
                if (instructionTracing()) {
                    System.out.println("+exception");
                }
                ip = lastIP;
                if (maxexceptions != 0) {
                    if (maxexceptions-- == 1) {
                        System.out.println("maxexceptions reached");
                        System.exit(-1);
                    }
                }
                traceException(ex);
                doException(ex.getClass().getName());
            }
            //preempt(TIMESLICE);
        }
        ar = null; // safety
        return exitCode;
    }

   /**
    * exit
    */
    public void exit(int code) {
        System.exit(1);
    }

   /**
    * preempt
    */
    private void preempt(int timeslice) {
        //if (tracer != null) {
        //    System.out.println("+invoke yield");
        //}
        if (preemption && ip > 0 && --preemptionCounter <= 0) {
            preemptionCounter = timeslice;
            doInvokeInternal(call_Thread_yield, null, ar.getInstruction(ip-1));
        }
    }

   /**
    * run
    */
    private void run(Type mainType, String[] mainArgs) {
        if (vm.traceinstructions()) {
            tracer = new InstructionTracer(System.out);
        }
        initialize(mainType, mainArgs);
        exit(interpret());
    }

   /**
    * The program entry point
    */
    public static void main(String[] args) throws Throwable {
        VirtualMachine.main(args);
        VirtualMachine vm = VirtualMachine.TopVM();
        args = vm.getArgs();
        if (vm.traceexec()) {
            System.out.print("\n\nArgs = ");
            for (int i = 0 ; i < args.length ; i++) {
                System.out.print(args[i]+" ");
            }
            System.out.println("\n\n");

        }
        new Interpreter(vm).run(vm.mainClass(), args);
    }



   /* ------------------------------------------------------------------------ *\
    *                               doException                                *
   \* ------------------------------------------------------------------------ */

    public void doException(String name) {
        name = "L" + name.replace('.', '/') +";";
        Type exceptionType = vm.createType(null, name);
        exceptionType.load();
        doException(new InstanceObject(exceptionType));
    }

    private Target stackPop(Stack stack) {
        if (stack.isEmpty()) {
            return null;
        }
        return (Target)stack.pop();
    }

    public void doException(InstanceObject object) {
        Type exceptionType = object.type();
        assume(exceptionType != null);
        Stack stack = new Stack();

        while (ar != null) {

           /*
            * Dynamically construct the handler table
            */
            for (int i = 0 ; i < ip ; i++) {
                Instruction inst = ar.getInstruction(i);
                if (inst instanceof HandlerEnter) {
//System.out.println("push "+ ((HandlerEnter)inst).target() );
                    stack.push(((HandlerEnter)inst).target());
                }
                if (inst instanceof HandlerExit) {
                    Target popi = (Target) stack.pop();
//System.out.println("pop "+ popi );

                }
            }

           /*
            * Print the state of the exception table
            */
            if (vm.traceexceptions()) {
                System.out.println("\n\n *** Exception table for ip = "+ip+ " ***\n");
            }


           /*
            * Search the handler table
            */
            for (Target target = stackPop(stack); target != null ; target = stackPop(stack)) {
                Type targetType = target.getExceptionTargetType();
                assume(targetType != null);
                assume(exceptionType != null);

                if (vm.traceexceptions()) {
                    System.out.print(target);
                }

                if (exceptionType.isAssignableTo(targetType)) {
                    LoadException targetInst = (LoadException)target.getTargetInstruction();
                    oopAtPut(targetInst, object); // Do the LoadException here
                    assume(targetInst != null);
                    ip = targetInst.getIP() + 1;
                    if (vm.traceexceptions()) {
                        System.out.println(" ip = "+ip);
                    }
                    return;
                }

                if (vm.traceexceptions()) {
                    System.out.println();
                }

            }

           /*
            * Drop back to the previous activation record and try again
            */
            assume(ar != null);
            if(ar.caller() == null) {
                break;
            }
            ip = ar.caller().getIP() + 1; // Skip over invoke instruction
            ar = ar.previousActivation();
        }

        throw new RuntimeException("Uncaught exception");

    }

   /* ------------------------------------------------------------------------ *\
    *                              doArithmeticOp                              *
   \* ------------------------------------------------------------------------ */

    public void doArithmeticOp(ArithmeticOp inst) {
        Type ltype = inst.left().type();
        if (ltype == Type.INT || ltype == Type.SHORT || ltype == Type.CHAR || ltype == Type.BYTE || ltype == Type.BOOLEAN) {
            int left  = intAt(inst.left());
            int right = intAt(inst.right());
            switch (inst.op()) {
                case OP_ADD:    intAtPut(inst, left + right);     break;
                case OP_SUB:    intAtPut(inst, left - right);     break;
                case OP_MUL:    intAtPut(inst, left * right);     break;
                case OP_DIV:    intAtPut(inst, left / right);     break;
                case OP_REM:    intAtPut(inst, left % right);     break;
                case OP_AND:    intAtPut(inst, left & right);     break;
                case OP_OR:     intAtPut(inst, left | right);     break;
                case OP_XOR:    intAtPut(inst, left ^ right);     break;
                case OP_SHL:    intAtPut(inst, left << right);    break;
                case OP_SHR:    intAtPut(inst, left >> right);    break;
                case OP_USHR:   intAtPut(inst, left >>> right);   break;
                default:        shouldNotReachHere();
            }
            return;
        }
        if (ltype == Type.LONG) {
            int op = inst.op();
            if(op == OP_SHL || op == OP_SHR || op == OP_USHR) {
                long left  = longAt(inst.left());
                int  right = intAt(inst.right());
                switch (op) {
                    case OP_SHL:    longAtPut(inst, left << right);   break;
                    case OP_SHR:    longAtPut(inst, left >> right);   break;
                    case OP_USHR:   longAtPut(inst, left >>> right);  break;
                    default:        shouldNotReachHere();
                }
            } else {
                long left  = longAt(inst.left());
                long right = longAt(inst.right());
                switch (op) {
                    case OP_ADD:    longAtPut(inst, left + right);    break;
                    case OP_SUB:    longAtPut(inst, left - right);    break;
                    case OP_MUL:    longAtPut(inst, left * right);    break;
                    case OP_DIV:    longAtPut(inst, left / right);    break;
                    case OP_REM:    longAtPut(inst, left % right);    break;
                    case OP_AND:    longAtPut(inst, left & right);    break;
                    case OP_OR:     longAtPut(inst, left | right);    break;
                    case OP_XOR:    longAtPut(inst, left ^ right);    break;
                    case OP_LCMP:   intAtPut(inst, (left < right) ? -1 : (left == right) ? 0 : 1); break;
                    default:        shouldNotReachHere();
                }
            }
            return;
        }
        if (ltype == Type.FLOAT) {
            float left  = floatAt(inst.left());
            float right = floatAt(inst.right());
            switch (inst.op()) {
                case OP_ADD:    floatAtPut(inst, left + right);   break;
                case OP_SUB:    floatAtPut(inst, left - right);   break;
                case OP_MUL:    floatAtPut(inst, left * right);   break;
                case OP_DIV:    floatAtPut(inst, left / right);   break;
                case OP_REM:    floatAtPut(inst, left % right);   break;
                case OP_FCMPL:
                case OP_FCMPG:  intAtPut(inst, (left < right) ? -1 : (left == right) ? 0 : 1); break;
                default:        shouldNotReachHere();

            }
            return;
        }
        if (ltype == Type.DOUBLE) {
            double left  = doubleAt(inst.left());
            double right = doubleAt(inst.right());
            switch (inst.op()) {
                case OP_ADD:    doubleAtPut(inst, left + right);  break;
                case OP_SUB:    doubleAtPut(inst, left - right);  break;
                case OP_MUL:    doubleAtPut(inst, left * right);  break;
                case OP_DIV:    doubleAtPut(inst, left / right);  break;
                case OP_REM:    doubleAtPut(inst, left % right);  break;
                case OP_DCMPL:
                case OP_DCMPG:  intAtPut(inst, (left < right) ? -1 : (left == right) ? 0 : 1); break;
                default:        shouldNotReachHere();
            }
            return;
        }
        System.out.println("left type="+inst.left().type());
        shouldNotReachHere();
    }

   /* ------------------------------------------------------------------------ *\
    *                              doArrayLength                               *
   \* ------------------------------------------------------------------------ */

    public void doArrayLength(ArrayLength inst) {
        ArrayObject array = (ArrayObject)oopAt(inst.array());
        intAtPut(inst, array.length());
    }

   /* ------------------------------------------------------------------------ *\
    *                               doCheckCast                                *
   \* ------------------------------------------------------------------------ */

    public void doCheckCast(CheckCast inst) {
        BasicObject object = oopAt(inst.value());
        if (object != null && !object.type().isAssignableTo(inst.checkType())) {
//prtn("doCheckCast "+ object.type() + " !=== " +  inst.checkType());
            throw new ClassCastException();
        }
        oopAtPut(inst, object);
    }

   /* ------------------------------------------------------------------------ *\
    *                               doConvertOp                                *
   \* ------------------------------------------------------------------------ */

    public void doConvertOp(ConvertOp inst) {
        switch (inst.op()) {
            case OP_I2L: longAtPut(inst,     (long)   intAt(inst.value()));    break;
            case OP_I2F: floatAtPut(inst,    (float)  intAt(inst.value()));    break;
            case OP_I2D: doubleAtPut(inst,   (double) intAt(inst.value()));    break;
            case OP_L2I: intAtPut(inst,      (int)    longAt(inst.value()));   break;
            case OP_L2F: floatAtPut(inst,    (float)  longAt(inst.value()));   break;
            case OP_L2D: doubleAtPut(inst,   (double) longAt(inst.value()));   break;
            case OP_F2I: intAtPut(inst,      (int)    floatAt(inst.value()));  break;
            case OP_F2L: longAtPut(inst,     (long)   floatAt(inst.value()));  break;
            case OP_F2D: doubleAtPut(inst,   (double) floatAt(inst.value()));  break;
            case OP_D2I: intAtPut(inst,      (int)    doubleAt(inst.value())); break;
            case OP_D2L: longAtPut(inst,     (long)   doubleAt(inst.value())); break;
            case OP_D2F: floatAtPut(inst,    (float)  doubleAt(inst.value())); break;
            case OP_I2B: intAtPut(inst,      (byte)   intAt(inst.value()));    break;
            case OP_I2C: intAtPut(inst,      (char)   intAt(inst.value()));    break;
            case OP_I2S: intAtPut(inst,      (short)  intAt(inst.value()));    break;
        }
    }

   /* ------------------------------------------------------------------------ *\
    *                               doDropLocal                                *
   \* ------------------------------------------------------------------------ */

    public void doDropLocal(DropLocal inst) {
    }

   /* ------------------------------------------------------------------------ *\
    *                                 doGoto                                   *
   \* ------------------------------------------------------------------------ */

    public void doGoto(Goto inst) {
        int oldip = ip;
        int newip = inst.target().getIP();
        ip = newip;
        if (newip < oldip) {
            preempt(TIMESLICE);      // Do preemption on a backward branch only
        }
    }

   /* ------------------------------------------------------------------------ *\
    *                                 doIfOp                                   *
   \* ------------------------------------------------------------------------ */

    public void doIfOp(IfOp inst) {
        Type ltype = inst.left().type();
        boolean res = false;
        if (ltype.isAssignableTo(Type.INT)) {
            int left  = intAt(inst.left());
            int right = intAt(inst.right());
            switch (inst.op()) {
                case OP_EQ: res = left == right; break;
                case OP_NE: res = left != right; break;
                case OP_LT: res = left <  right; break;
                case OP_GE: res = left >= right; break;
                case OP_GT: res = left >  right; break;
                case OP_LE: res = left <= right; break;
                default:    shouldNotReachHere();
            }
        } else if (ltype.isAssignableTo(Type.OBJECT)) {
            BasicObject left  = oopAt(inst.left());
            BasicObject right = oopAt(inst.right());
            switch (inst.op()) {
                case OP_EQ: res = left == right; break;
                case OP_NE: res = left != right; break;
                default:    shouldNotReachHere();
            }
        } else {
            throw fatal("if type = "+ltype);
        }
        if (res) {
            doGoto(inst);
        }
    }

   /* ------------------------------------------------------------------------ *\
    *                             doInitializeClass                            *
   \* ------------------------------------------------------------------------ */

    public void doInitializeClass(InitializeClass inst) {
        doInitializeClass(inst.parent(), inst);
    }

    public boolean doInitializeClass(Type parent, Instruction inst) {
        parent.load();
        parent.convert();
        if (parent.getState() < Type.INITIALIZED) {
            if (parent.superType().getState() == Type.INITIALIZED && parent.getClinit() == null) {
                parent.setState(Type.INITIALIZED);
            } else {
                doInvokeInternal(call_Class_initialize, parent, inst);
//prtn("doInitializeClass type="+parent.name());
                return true;
            }
        }
        return false;
    }


   /* ------------------------------------------------------------------------ *\
    *                               doInstanceOf                               *
   \* ------------------------------------------------------------------------ */

    public void doInstanceOf(InstanceOf inst) {
        BasicObject object = oopAt(inst.value());
        if (object != null && object.type().isAssignableTo(inst.checkType())) {
            intAtPut(inst, 1);  /*TRUE*/
        } else {
            intAtPut(inst, 0);  /*FALSE*/
        }
    }

   /* ------------------------------------------------------------------------ *\
    *                              doHandlerEnter                              *
   \* ------------------------------------------------------------------------ */

    public void doHandlerEnter(HandlerEnter inst) {
    }

   /* ------------------------------------------------------------------------ *\
    *                              doHandlerExit                               *
   \* ------------------------------------------------------------------------ */

    public void doHandlerExit(HandlerExit inst) {
    }


   /* ------------------------------------------------------------------------ *\
    *                             doInvokeInternal                             *
   \* ------------------------------------------------------------------------ */

    private void doInvokeInternal(Method method, BasicObject parm, Instruction inst) {
        doInvokeInternal(method, parm, null, 0, inst);
    }

    private void doInvokeInternal(Method method, BasicObject parm, BasicObject parm2, long parm3, Instruction inst) {
        IntermediateMethod callee = (IntermediateMethod)method;
        if (callee != null) {
            Instruction[] instructions = callee.getInstructions();
            MethodHeader hdr           = (MethodHeader)instructions[0];
            int localCount             = hdr.getLocalCount();
            ActivationObject nar       = new ActivationObject(callee, ar, inst);

            for (int i = 0 ; i < localCount ; i++) {
                Local local      = hdr.getLocal(i);
                int   offset     = local.getOffset();
                int   parmNumber = local.getParameterNumber();
                if (parmNumber >= 0) {
                    if (parm2 == null) {
                        assume(parmNumber == (callee.isStatic() ? 1 : 0));
                        assume(local.slotType() == Type.BASIC_OOP);
                        nar.oopAtPut(offset, parm);
                        break;
                    } else {
                       /*
                        * Ugly fudge for wait(), notify(), and notifyAll()
                        */
                        assume(callee.isStatic());
                        switch (parmNumber) {
                            case 1: {
                                assume(local.slotType() == Type.BASIC_OOP);
                                nar.oopAtPut (offset, parm);
                                break;
                            }
                            case 2: {
                                assume(local.slotType() == Type.BASIC_OOP);
                                nar.oopAtPut (offset, parm2);
                                break;
                            }
                            case 3: {
                                assume(local.slotType() == Type.BASIC_LONG);
                                nar.longAtPut(offset, parm3);
                                break;
                            }
                        }
                    }
                }
            }
            ar = nar;
            ip = 0;
        }
    }


   /* ------------------------------------------------------------------------ *\
    *                                doInvoke                                  *
   \* ------------------------------------------------------------------------ */

    public void doInvoke(Invoke inst) {
        Instruction[] parms = inst.parms();
        Type type;
        if (!inst.isVirtual()) {
            type = inst.method().parent();
        } else {
            type = oopAt(parms[0]).type();
        }

        //assume(!type.isInterface());
        IntermediateMethod callee  = type.findMethodOrInterface(inst.offset());
        if (callee == null) {
            assume(callee != null, "type="+type+" inst.method()="+inst.method()+" inst.offset()="+inst.offset());
        }
        Instruction[] instructions = callee.getInstructions();

        if (instructions == null) {
            doInvokeNative(callee, parms, inst);
            return;
        }

        MethodHeader hdr      = (MethodHeader)instructions[0];
        int localCount        = hdr.getLocalCount();
        ActivationObject nar  = new ActivationObject(callee, ar, inst);

        for (int i = 0 ; i < localCount ; i++) {
            Local local    = hdr.getLocal(i);
            int offset     = local.getOffset();
            int parmNumber = local.getParameterNumber();
            if (parmNumber >= 0) {
                Instruction parm = parms[parmNumber];
                switch (local.slotType()) {
                    case Type.BASIC_OOP:  nar.oopAtPut(offset,  oopAt(parm));  break;
                    case Type.BASIC_INT:  nar.intAtPut(offset,  intAt(parm));  break;
                    case Type.BASIC_LONG: nar.longAtPut(offset, longAt(parm)); break;
                }
                if (vm.traceexec()) {
                    String lastPut = nar.lastPut();
                    System.out.println("parm "+parmNumber+" ("+local+") ===>"+lastPut);
                }
            }
        }
        ar = nar;
        ip = 0;
    }

   /* ------------------------------------------------------------------------ *\
    *                              doLoadConstant                              *
   \* ------------------------------------------------------------------------ */

    public void doLoadConstant(LoadConstant inst) {
        if (inst.getResultLocal() != null) {
                 if (inst.isConstNull()) { oopAtPut(inst, null);                }
            else if (inst.isInt())       { intAtPut(inst, inst.getInt());       }
            else if (inst.isLong())      { longAtPut(inst, inst.getLong());     }
            else if (inst.isFloat())     { floatAtPut(inst, inst.getFloat());   }
            else if (inst.isString())    { stringAtPut(inst, inst.getString()); }
            else if (inst.isDouble())    { doubleAtPut(inst, inst.getDouble()); }
            else if (inst.isType())      { oopAtPut(inst, inst.getType());      }
            else { shouldNotReachHere(); }
        }
    }

   /* ------------------------------------------------------------------------ *\
    *                               doLoadException                            *
   \* ------------------------------------------------------------------------ */

    public void doLoadException(LoadException inst) {
        shouldNotReachHere();
    }

   /* ------------------------------------------------------------------------ *\
    *                                 doLoadField                              *
   \* ------------------------------------------------------------------------ */

    public void doLoadField(LoadField inst) {
        Instruction ref = inst.ref();
        InstanceObject rec = (ref == null) ? GlobalObject.getGlobals() : (InstanceObject)oopAt(inst.ref());
        rec.get(inst.field(), ar, inst);
    }

   /* ------------------------------------------------------------------------ *\
    *                                doLoadIndexed                             *
   \* ------------------------------------------------------------------------ */

    public void doLoadIndexed(LoadIndexed inst) {
        ArrayObject array = (ArrayObject)oopAt(inst.array());
        int        offset = intAt(inst.index());
        array.get(offset, ar, inst);
    }

   /* ------------------------------------------------------------------------ *\
    *                                 doLoadLocal                              *
   \* ------------------------------------------------------------------------ */

    public void doLoadLocal(LoadLocal inst) {
        int offset = inst.local().getOffset();
        Type  type = inst.type();
        switch (type.slotType()) {
            case Type.BASIC_OOP:  oopAtPut(inst,  oopAt(offset));  break;
            case Type.BASIC_INT:  intAtPut(inst,  intAt(offset));  break;
            case Type.BASIC_LONG: longAtPut(inst, longAt(offset)); break;
        }
    }

   /* ------------------------------------------------------------------------ *\
    *                                 doLoadType                               *
   \* ------------------------------------------------------------------------ */
/*
    public void doLoadType(LoadType inst) {
        assume (inst.getResultLocal() != null);
        oopAtPut(inst, inst.realType());
    }
*/

   /* ------------------------------------------------------------------------ *\
    *                                doLookupSwitch                            *
   \* ------------------------------------------------------------------------ */

    public void doLookupSwitch(LookupSwitch inst) {
        int      key     = intAt(inst.key());
        int[]    matches = inst.matches();
        Target[] targets = inst.targets();
        for (int i = 0 ; i < targets.length ; i++) {
            if (key == matches[i]) {
                ip = targets[i].getIP();
                return;
            }
        }
        ip = inst.defaultTarget().getIP();
    }

   /* ------------------------------------------------------------------------ *\
    *                                doMethodHeader                            *
   \* ------------------------------------------------------------------------ */

    public void doMethodHeader(MethodHeader hdr) {
    }

   /* ------------------------------------------------------------------------ *\
    *                                doMonitorEnter                            *
   \* ------------------------------------------------------------------------ */

    public void doMonitorEnter(MonitorEnter inst) {
        if (allowMonitors) {
            BasicObject     object  = oopAt(inst.value());
            MonitorHashcode monitor = object.getMonitor();
            doInvokeInternal(call_Thread_monitorEnter, monitor, inst);
        }
    }

   /* ------------------------------------------------------------------------ *\
    *                                doMonitorExit                             *
   \* ------------------------------------------------------------------------ */

    public void doMonitorExit(MonitorExit inst) {
        if (allowMonitors) {
            BasicObject     object  = oopAt(inst.value());
            MonitorHashcode monitor = object.getMonitor();
            doInvokeInternal(call_Thread_monitorExit, monitor, inst);
        }
    }

   /* ------------------------------------------------------------------------ *\
    *                                 doNegateOp                               *
   \* ------------------------------------------------------------------------ */

    public void doNegateOp(NegateOp inst) {
        Type type = inst.value().type();
        if (type == Type.INT) {
            int value  = intAt(inst.value());
            intAtPut(inst, 0 - value);
            return;
        }
        if (type == Type.LONG) {
            long value  = longAt(inst.value());
            longAtPut(inst, (long)0 - value);
            return;
        }
        if (type == Type.FLOAT) {
            float value  = floatAt(inst.value());
            floatAtPut(inst, (float)0 - value);
            return;
        }
        if (type == Type.DOUBLE) {
            double value  = doubleAt(inst.value());
            doubleAtPut(inst, (double)0 - value);
            return;
        }
        shouldNotReachHere();
    }

   /* ------------------------------------------------------------------------ *\
    *                                 doNewArray                               *
   \* ------------------------------------------------------------------------ */

    public void doNewArray(NewArray inst) {
        oopAtPut(inst, ArrayObject.create(inst.type(), intAt(inst.size())));
    }

   /* ------------------------------------------------------------------------ *\
    *                                addDimension                              *
   \* ------------------------------------------------------------------------ */

    private void addDimension(ArrayObject array, Instruction[] dimList, int position) {
        if (position < dimList.length) {
            int size = intAt(dimList[position]);
            for (int i = 0 ; i < array.length() ; i++) {
                Type subType = array.type().elementType();
                ArrayObject subarray = ArrayObject.create(subType, size);
                array.oopAtPut(i, subarray);
                addDimension(subarray, dimList, position+1);
            }
        }
    }

   /* ------------------------------------------------------------------------ *\
    *                               doNewMultiArray                            *
   \* ------------------------------------------------------------------------ */

    public void doNewMultiArray(NewMultiArray inst) {
        Instruction[] dimList = inst.dimList();
        int size = intAt(dimList[0]);
        ArrayObject array = ArrayObject.create(inst.type(), size);
        oopAtPut(inst, array);
        addDimension(array, dimList, 1);
    }

   /* ------------------------------------------------------------------------ *\
    *                                doNewObject                               *
   \* ------------------------------------------------------------------------ */

    public void doNewObject(NewObject inst) {
        oopAtPut(inst, new InstanceObject(inst.type()));
    }

   /* ------------------------------------------------------------------------ *\
    *                                  doPhi                                   *
   \* ------------------------------------------------------------------------ */

    public void doPhi(Phi inst) {
    }

   /* ------------------------------------------------------------------------ *\
    *                                 doReturn                                 *
   \* ------------------------------------------------------------------------ */

    public void doReturn(Return inst) {
        ActivationObject par = ar.previousActivation();
        if (par == null) {
            running = false;
            exitCode = 1;
            return;
        }
        Instruction caller = ar.caller();
        int result = caller.getResultOffset();
        Instruction value = inst.value();
        if (value != null && result >= 0) {
            int slotType = value.type().slotType();
                 if (slotType == Type.BASIC_OOP)  { par.oopAtPut(result,  oopAt(value));  }
            else if (slotType == Type.BASIC_INT)  { par.intAtPut(result,  intAt(value));  }
            else if (slotType == Type.BASIC_LONG) { par.longAtPut(result, longAt(value)); }
        }
        ip = caller.getIP() + 1; // Skip over Invoke
        ar = par;
        assume(ar != null);

    }

   /* ------------------------------------------------------------------------ *\
    *                               doStoreField                               *
   \* ------------------------------------------------------------------------ */

    public void doStoreField(StoreField inst) {
        Instruction ref = inst.ref();
        InstanceObject rec = (ref == null) ? GlobalObject.getGlobals() : (InstanceObject)oopAt(inst.ref());
        rec.put(inst.field(), ar, inst.value());
    }

   /* ------------------------------------------------------------------------ *\
    *                              doStoreIndexed                              *
   \* ------------------------------------------------------------------------ */

    public void doStoreIndexed(StoreIndexed inst) {
        ArrayObject array = (ArrayObject)oopAt(inst.array());
        int        offset = intAt(inst.index());
        array.put(offset, ar, inst.value());
    }

   /* ------------------------------------------------------------------------ *\
    *                               doStoreLocal                               *
   \* ------------------------------------------------------------------------ */

    public void doStoreLocal(StoreLocal inst) {
        Local local = inst.local();
        assume(local != null);
        Instruction value = inst.value();
        int offset = local.getOffset();
        Type  type = value.type();
        switch (type.slotType()) {
            case Type.BASIC_OOP:  oopAtPut(offset,  oopAt(value));  break;
            case Type.BASIC_INT:  intAtPut(offset,  intAt(value));  break;
            case Type.BASIC_LONG: longAtPut(offset, longAt(value)); break;
        }
    }

   /* ------------------------------------------------------------------------ *\
    *                              doTableSwitch                               *
   \* ------------------------------------------------------------------------ */

    public void doTableSwitch(TableSwitch inst) {
        int      key     = intAt(inst.key());
        int      low     = inst.low();
        Target[] targets = inst.targets();
        for (int i = 0 ; i < targets.length ; i++) {
            if (key == i+low) {
                ip = targets[i].getIP();
                return;
            }
        }
        ip = inst.defaultTarget().getIP();
    }

   /* ------------------------------------------------------------------------ *\
    *                                 doThrow                                  *
   \* ------------------------------------------------------------------------ */

    public void doThrow(Throw inst) {
        InstanceObject object = (InstanceObject)oopAt(inst.value());
        --ip;
        if (vm.traceexceptions()) {
            System.out.println("+++ doThrow "+object.type());
            System.out.println("+++ from method "+ar.getMethod().parent()+"::"+ar.getMethod().name());
/*
            Instruction ins = ar.getInstruction(ip);
            int line = ins.getLine();
            if (line >= 0) {
                System.out.print(" line = "+line);
            }
            System.out.println(" ip = "+ip+"\n");
*/
            System.out.println(stackTrace(ar, ip));
        }
        doException(object);
    }


   /* ------------------------------------------------------------------------ *\
    *                              doInvokeNative                              *
   \* ------------------------------------------------------------------------ */

    public void doInvokeNative(IntermediateMethod callee, Instruction[] parms, Invoke inst) {
        int  result = inst.getResultOffset();
        Type parent = callee.parent();

       /*
        * java.lang.Object
        */
        if (parent == Type.OBJECT) {
            if (callee.name().equals("getClass")) {
                oopAtPutCheck(result, oopAt(parms[0]).type());
                return;
            }
            if (callee.name().equals("hashCode")) {
                intAtPutCheck(result, oopAt(parms[0]).getHashCode());
                return;
            }
            if (callee.name().equals("wait0")) {
                assume(allowMonitors);
                BasicObject object = oopAt(parms[0]);
                long         delta = longAt(parms[1]);
                MonitorHashcode monitor = object.getMonitor();
                doInvokeInternal(call_Thread_monitorWait,   object, monitor, delta, inst);
                return;
            }
            if (callee.name().equals("notify")) {
                if (allowMonitors) {
                    BasicObject      object = oopAt(parms[0]);
                    MonitorHashcode monitor = object.getMonitor();
                    doInvokeInternal(call_Thread_monitorNotify, object, monitor, 0, inst); // 0 = false
                }
                return;
            }
            if (callee.name().equals("notifyAll")) {
                if (allowMonitors) {
                    BasicObject      object = oopAt(parms[0]);
                    MonitorHashcode monitor = object.getMonitor();
                    doInvokeInternal(call_Thread_monitorNotify, object, monitor, 1, inst); // 1 = true
                }
                return;
            }
        }

       /*
        * java.lang.System
        */
        if (parent == Type.SYSTEM) {
            if (callee.name().equals("arraycopy")) {
                if (oopAt(parms[1]) == null || oopAt(parms[3]) == null) {
                    throw new NullPointerException();
                }
                if (!(oopAt(parms[1]) instanceof ArrayObject) || !(oopAt(parms[3]) instanceof ArrayObject)) {
                    throw new ArrayStoreException();
                }
                ArrayObject from = (ArrayObject)oopAt(parms[1]);
                int fromOffset   =              intAt(parms[2]);
                ArrayObject to   = (ArrayObject)oopAt(parms[3]);
                int toOffset     =              intAt(parms[4]);
                int length       =              intAt(parms[5]);
                to.copy(toOffset, from, fromOffset, length);
                return;
            }
            if (callee.name().equals("getProperty0")) {
                InstanceObject string = (InstanceObject)oopAt(parms[1]);
                oopAtPutCheck(result, makeString(getSystemProperty(getString(string))));
                return;
            }
            if (callee.name().equals("currentTimeMillis")) {
                longAtPutCheck(result, System.currentTimeMillis());
                return;
            }
            if (callee.name().equals("identityHashCode")) {
                intAtPutCheck(result, oopAt(parms[1]).getHashCode());
                return;
            }
        }

       /*
        * java.lang.Class
        */
        if (parent == Type.CLASS) {
            if (callee.name().equals("isArray")) {
                Type type = (Type)oopAt(parms[0]);
                intAtPutCheck(result, type.isArray() ? 1 : 0);
                return;
            }
            if (callee.name().equals("isInterface")) {
                Type type = (Type)oopAt(parms[0]);
                intAtPutCheck(result, type.isInterface() ? 1 : 0);
                return;
            }
            if (callee.name().equals("isAbstract")) {
                Type type = (Type)oopAt(parms[0]);
                intAtPutCheck(result, type.isAbstract() ? 1 : 0);
                return;
            }
            if (callee.name().equals("callerHasAccessToClass")) {
                Type type = (Type)oopAt(parms[0]);
                Type callersParent = ar.previousActivation().getMethod().parent();
                int res;

//System.out.println("callerHasAccessToClass type="+type);
//System.out.println("callerHasAccessToClass type.isPublic()="+type.isPublic());
//System.out.println("callerHasAccessToClass callersParent="+callersParent);

                if (type == callersParent || type.isPublic() || type.inSamePackageAs(callersParent)) {
                    res = 1;
                } else {
                    res = 0;
                }
                intAtPutCheck(result, res);
                return;
            }
            if (callee.name().equals("getName")) {
                Type type = (Type)oopAt(parms[0]);
                String javaName;
                javaName = type.name();
                if (javaName.charAt(0) != '[') {
                    javaName = javaName.substring(1, javaName.length() - 1);
                }
                javaName = javaName.replace('/', '.');
//System.out.println(javaName);
                oopAtPutCheck(result, makeString(javaName));
                return;
            }
            if (callee.name().equals("isAssignableTo")) {
                Type type     = (Type)oopAt(parms[0]);
                Type parmType = (Type)oopAt(parms[1]);
                intAtPutCheck(result, type.isAssignableTo(parmType) ? 1 : 0);
                return;
            }
            if (callee.name().equals("getSuperclass")) {
                Type type = (Type)oopAt(parms[0]);
                oopAtPutCheck(result, type.superClass());
                return;
            }
            if (callee.name().equals("getInitalizingThread")) {
                BasicObject type = oopAt(parms[1]);
                oopAtPutCheck(result, (BasicObject)classInitalizing.get(type));
                return;
            }
            if (callee.name().equals("setInitalizingThread")) {
                BasicObject type   = oopAt(parms[1]);
                BasicObject thread = oopAt(parms[2]);
                if (thread == null) {
                    classInitalizing.remove(type);
                } else {
                    classInitalizing.put(type, thread);
                }
                return;
            }
            if (callee.name().equals("getState")) {
                Type type = (Type)oopAt(parms[0]);
                intAtPutCheck(result, type.getState());
                return;
            }
            if (callee.name().equals("setState")) {
                Type type = (Type)oopAt(parms[0]);
                type.setState(intAt(parms[1]));
                return;
            }
            if (callee.name().equals("runClinit")) {
                Type type = (Type)oopAt(parms[1]);
                doInvokeInternal(type.getClinit(), null, inst);
                return;
            }
            if (callee.name().equals("forName0")) {
                String name = getString((InstanceObject)oopAt(parms[1]));
                name = "L" + name.replace('.', '/') +";";
//System.out.println("Class::forName0 "+name);
                Type type = vm.createType(null, name);
                type.load();
                type.convert();
                oopAtPutCheck(result, type);
                doInitializeClass(type, inst);
                return;
            }
            if (callee.name().equals("newInstance0")) {
                Type type = (Type)oopAt(parms[0]);
                InstanceObject newInst = new InstanceObject(type);
                oopAtPutCheck(result, newInst);
                callee = getMethod(type, VirtualMachine.INIT, "()V");
                doInvokeInternal(callee, newInst, inst);
                return;
            }
        }

       /*
        * java.lang.Thread
        */
        if (parent == Type.THREAD) {
            if (callee.name().equals("startPreemptiveScheduling")) {
                preemptionCounter = TIMESLICE;
                return;
            }
            if (callee.name().equals("disablePreemption")) {
                assume(preemption);
                preemption = false;
                return;
            }
            if (callee.name().equals("enablePreemption")) {
                assume(!preemption);
                preemption = true;
                return;
            }
            if (callee.name().equals("switchAndEnablePreemption")) {
                assume(!preemption);
                InstanceObject fromThread = (InstanceObject)oopAt(parms[1]);
                InstanceObject   toThread = (InstanceObject)oopAt(parms[2]);

               /*
                * If there is a thread context then save the ar and ip.
                */
                if (fromThread != null) {
                    fromThread.oopAtPut(thread_ar, ar);
                    fromThread.intAtPut(thread_ip, ip);
                   /*
                    * Trace
                    */
                    if (vm.tracethreads()) {
                        System.out.print("Switch thread from "+fromThread.intAt(thread_number));
                    }
                }


               /*
                * Get the ap and ip of the new context
                */
                ar = (ActivationObject)toThread.oopAt(thread_ar);
                ip =                   toThread.intAt(thread_ip);

               /*
                * Trace
                */
                if (vm.tracethreads()) {
                    System.out.println(" to "+toThread.intAt(thread_number));
                }

               /*
                * If there is no activation record in the thread then this is
                * a new thread so the VM must call the callRun() function.
                */
                if (ar == null) {
                    doInvokeInternal(call_Thread_callRun, toThread, inst);
                }
                preemption = true;
                return;
            }
            if (callee.name().equals("threadToRestart")) {
               /*
                * Call Native.java to get threads to be restarted
                */
                natve.event();
               /*
                * Look for a thread to start
                */
                BasicObject thread = null;
                if (!threadsToRestart.isEmpty()) {
                    thread = (BasicObject)threadsToRestart.firstElement();
                    threadsToRestart.removeElement(thread);
                }
                oopAtPutCheck(result, thread);
                return;
            }
            if (callee.name().equals("waitForEvent")) {
                int aliveThreads = intAt(parms[1]);
                long nextTime    = longAt(parms[2]);

                if (aliveThreads == 0) {
                    running = false;
                    exitCode = 1;
                } else {
                    long timeNow = System.currentTimeMillis();
                    //System.out.println("nextTime="+nextTime+" timeNow="+timeNow+" time="+(nextTime - timeNow));
                    if (nextTime > timeNow) {
                        long delta = nextTime - timeNow;
                        //System.out.println("time="+delta);
                        //if (delta > 1000*60*60) {
                        //    System.out.println("Deadlock wait? "+delta);
                        //    throw new RuntimeException();
                        //}

                        trace(vm.tracethreads(), "Wait+ "+((double)delta)/1000);
                        synchronized(natve) {
                            try { natve.wait(delta); } catch(InterruptedException ex) {}
                        }
                        trace(vm.tracethreads(), "Wait-");
                    }
                }
                return;
            }
            if (callee.name().equals("doMain")) {
                doInvokeInternal(call_Application_main, convertArgs(mainArgs), inst);
                return;
            }
            if (callee.name().equals("fatalVMError")) {
                System.out.println("\n\n *** fatalVMError ***\n");
                System.out.println(stackTrace(ar, ip));
                exit(1);
            }
        }

        if (callee.name().equals("getStacktrace")) {
            oopAtPutCheck(result, makeString(stackTrace(ar, ip)));
            return;
        }

        if (callee.name().equals("cos")) {
            double d = ar.doubleAt(parms[1]);
            ar.doubleAtPut(result, Math.cos(d));
            return;
        }

        if (callee.name().equals("sqrt")) {
            double d = ar.doubleAt(parms[1]);
            ar.doubleAtPut(result, Math.sqrt(d));
            return;

        }

        if (callee.name().equals("exitInternal")) {
            int code = intAt(parms[1]);
            System.out.println("\n\n *** exitInternal ***" + code);
            System.exit(code);
            return;
        }

        if (callee.name().equals("freeMemory")) {
            longAtPutCheck(result, Runtime.getRuntime().freeMemory());
            return;
        }

        if (callee.name().equals("totalMemory")) {
            longAtPutCheck(result, Runtime.getRuntime().totalMemory());
            return;
        }

        if (callee.name().equals("gc")) {
            return;
        }

        if (callee.name().equals("startInstructionTrace")) {
            instructionTracing = true;
            trace(vm.traceinstructions(), "***startInstructionTrace");
            return;
        }

        if (callee.name().equals("stopInstructionTrace")) {
            instructionTracing = false;
            trace(vm.traceinstructions(), "***stopInstructionTrace");
            if(vm.traceinstructions()) {
                System.exit(1);
            }
            return;
        }


       /*
        * Try other native functions
        */
        natve.doNative(callee, parms, inst);
    }



   /* ------------------------------------------------------------------------ *\
    *                            getSystemProperty                             *
   \* ------------------------------------------------------------------------ */

    private String getSystemProperty(String key) {
        if (key.equals("microedition.configuration")) {
            return "CLDC-1.0";
        }
        if (key.equals("microedition.platform")) {
            return "j2me";
        }
        if (key.equals("microedition.encoding")) {
            return "ISO8859_1";
        }
        if (key.equals("de.kawt.classbase")) {
            return "de.kawt.impl.squawk";
        }

        return null;
    }

}
