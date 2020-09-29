
package com.sun.squawk.runtime.loader;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;

import java.io.PrintStream;

public class GraphPrinter extends InstructionVisitor implements RuntimeConstants {

    private PrintStream out;

    private VirtualMachine vm;
    private String name;
    private String pref;
    private MethodHeader methodHeader;

   /**
    * Main fucnction
    */
    public static void print(PrintStream out, Instruction ir, VirtualMachine vm, String name) {
        new GraphPrinter(out, ir, vm, vm.traceip(name), vm.tracelocals(name),  true);
    }

   /**
    * Main fucnction
    */
    public static void printOne(PrintStream out, Instruction ir, VirtualMachine vm) {
        new GraphPrinter(out, ir, vm, true, false, false);
    }


   /**
    * Private constructor
    */
    private GraphPrinter(PrintStream out, Instruction ir, VirtualMachine vm, boolean traceip, boolean tracelocals, boolean loop) {
        this.out  = out;
        this.vm   = vm;

        //ir.visitOn(this);

        do {
            pref = "";
            if (traceip) {
                pref += ir.getIP()+":\t";
                //pref += ir.getOrigionalIP()+":\t";

            }
            if (tracelocals) {
                pref += ir.getLoopDepth()+"\t";
            }
            ir.visit(this);
            ir = ir.getNext();
        } while (ir != null && loop);

    }

    private String prtType(Type type) {
        return ""+type;
    }

    private String prtLocal(Local local) {
        if (methodHeader != null) {
            assume (methodHeader.includes(local), "local="+local); // check the optomizer did include the local
        }
        return ""+local;
    }

    private String prtLocal(Instruction inst) {
        Local local = inst.getResultLocal();
        if (local != null) {
            return prtLocal(local);
        } else {
            if (inst instanceof LoadLocal) {
                return prtLocal(((LoadLocal)inst).local());
            } else if (inst instanceof LoadConstant) {
                return inst.toString();
            } else {
                throw fatal("bad instruction "+inst);
            }
        }
    }

    private String prtField(Field field, Instruction ref) {
        String res;
        String slot = " (fslot "+field.getSlotOffset()+")";
        String refStr = (ref == null) ? "" : prtLocal(ref);
        if (vm.verbose()) {
            res = "[" + field.parent() + "::" + field + slot +"] " + refStr;
        } else {
            res = "[" + field + slot +"] " + refStr;
        }
        return res;
    }

    private String prtMethod(Method method) {
        String res;
        String slot = " (mslot "+method.getSlotOffset()+ (method.isStatic()? "s)" : ")");
        if (vm.verbose()) {
            res = "[" + method.parent() + "::" + method + slot +"]";
        } else {
            res = "[" + method + slot +"]";
        }
        return res;
    }


    private void prt2op(Instruction result,  String op, Instruction src) {
        out.println(pref+"    "+ prtLocal(result) + "   \t= " + op + " " + prtLocal(src));
    }

    private void prt3op(Instruction result, Instruction left,  String op, Instruction right) {
        out.println(pref+"    "+ prtLocal(result) + "   \t= " + prtLocal(left) + " " + op + " " + prtLocal(right));
    }

    private void prt2op(Instruction result, int op, Instruction src) {
        prt2op(result, opNames[op], src);
    }

    private void prt3op(Instruction result, Instruction left, int op, Instruction right) {
        prt3op(result, left, opNames[op], right);
    }



    private void prtGoto(Target target) {
        out.println(pref+"    goto "+target.getIP());
    }

    private void prtif(Instruction left, String op, Instruction right, Target target) {
        out.print(pref+"    if "+ prtLocal(left) + " " + op + " " + prtLocal(right));
        out.println(" goto "+target.getIP());
    }






    public void doArithmeticOp(ArithmeticOp inst) {
        prt3op(inst, inst.left(), inst.op(), inst.right());
    }

    public void doArrayLength(ArrayLength inst) {
        prt2op(inst, "arraylength", inst.array());
    }

    public void doCheckCast(CheckCast inst) {
        //out.println(pref+"    checkcast "+ prtLocal(inst.value()) + " instanceof " + prtType(inst.checkType()));
        out.println(pref+"    "+ prtLocal(inst) + "   \t= " + prtLocal(inst.value()) +" checkcast " + prtType(inst.checkType()));
    }

    public void doConvertOp(ConvertOp inst) {
        prt2op(inst, inst.op(), inst.value());
    }

    public void doDropLocal(DropLocal inst) {
        out.println(pref+"    drop "+ inst.local());
    }

    public void doGoto(Goto inst) {
        prtGoto(inst.target());
    }

    public void doIfOp(IfOp inst) {
        prtif(inst.left(), opNames[inst.op()], inst.right(), inst.target());
    }

    public void doInstanceOf(InstanceOf inst) {
        out.println(pref+"    "+ prtLocal(inst) + "   \t= " + prtLocal(inst.value()) +" instanceof " + prtType(inst.checkType()));
    }

    public void doHandlerEnter(HandlerEnter inst) {
        out.println(pref+"    handlerenter " + inst.target().getIP());
    }

    public void doHandlerExit(HandlerExit inst) {
        out.println(pref+"    handlerexit " + inst.target().getIP());
    }

    public void doInitializeClass(InitializeClass inst) {
        out.println(pref+"    InitializeClass "+ inst.parent());
    }

    public void doInvoke(Invoke inst) {
        if (inst.getResultLocal() == null) {
            out.print(pref+"    invoke "+ prtMethod(inst.method()));
        } else {
            out.print(pref+"    "+ prtLocal(inst) + "   \t= invoke "+ prtMethod(inst.method()));
        }
        for (int i = 0 ; i < inst.parms().length ; i++) {
            out.print(" " + prtLocal(inst.parms()[i]));
        }
        out.println();
    }

    public void doLoadConstant(LoadConstant inst) {
        if (inst.getResultLocal() != null) {
            out.println(pref+"    "+ prtLocal(inst) + "   \t= " +  inst);
        }
    }

    public void doLoadException(LoadException inst) {
        out.print(pref + inst.target().getIP() + ":");
        out.println(" " + prtLocal(inst) + " = exception("+inst.target().getExceptionTargetType()+")");
    }

    public void doLoadField(LoadField inst) {
        out.println(pref+"    "+ prtLocal(inst) + "   \t= " + prtField(inst.field(), inst.ref()));
    }

    public void doLoadIndexed(LoadIndexed inst) {
        out.println(pref+"    "+ prtLocal(inst) + "   \t= " +  prtLocal(inst.array()) + "[" + prtLocal(inst.index()) + "]");
    }

    public void doLoadLocal(LoadLocal inst) {

//temp
//        if (inst.getResultLocal() == null) {
//            out.println(pref+"    XXX1  "+ prtLocal(inst) + "   \t= " + inst.local());
//        } else if (inst.getResultLocal() ==  inst.local()) {
//            out.println(pref+"    XXX2  "+ prtLocal(inst) + "   \t= " + inst.local());
//        } else {
//temp
        if (inst.getResultLocal() != null && inst.getResultLocal() !=  inst.local()) {
            out.println(pref+"    "+ prtLocal(inst) + "   \t= " + inst.local());
        }
    }

/*
    public void doLoadType(LoadType inst) {
        //assume (inst.getResultLocal() != null);
        out.println(pref+"    "+ prtLocal(inst) + "   \t= loadtype" + prtType(inst.realType()));
    }
*/
    public void doMethodHeader(MethodHeader inst) {
        assume(methodHeader == null);
        methodHeader = inst;
        out.print(pref+"    Instructions = "+inst.getInstructionCount()+" Locals =");
        int count = inst.getLocalCount();
        for (int i = 0 ; i < count ; i++) {
            Local local = inst.getLocal(i);
            if (local.getParameterNumber() >= 0) {
                out.print(" *"+prtLocal(local));
            } else {
                out.print(" "+prtLocal(local));
            }
            //out.print(" "+prtLocal(local));
            //if (local.getParameterNumber() >= 0) {
            //    out.print("{"+local.getParameterNumber()+"}");
            //}
        }
        out.print("\n");
    }

    public void doLookupSwitch(LookupSwitch inst) {
        out.print(pref+"    lookupswitch "+ prtLocal(inst.key()) + " ");
        for (int i = 0 ; i < inst.matches().length ; i++) {
            out.print(" (" + inst.matches()[i] + "=" + inst.targets()[i].getIP() + ")");
        }
        out.println(" (default=" + inst.defaultTarget().getIP() + ")");
    }

    public void doMonitorEnter(MonitorEnter inst) {
        out.println(pref+"    monitorenter "+ prtLocal(inst.value()));
    }

    public void doMonitorExit(MonitorExit inst) {
        out.println(pref+"    monitorexit "+ prtLocal(inst.value()));
    }

    public void doNegateOp(NegateOp inst) {
        prt2op(inst, "!", inst.value());
    }

    public void doNewArray(NewArray inst) {
        out.println(pref+"    "+ prtLocal(inst) + "   \t= new " +inst.type() + " [" + prtLocal(inst.size()) + "]");
    }

    public void doNewMultiArray(NewMultiArray inst) {
        out.print(pref+"    "+ prtLocal(inst) + "   \t= new " +inst.type());
        for (int i = 0 ; i < inst.dimList().length ; i++) {
            out.print(" [" + inst.dimList()[i] + "]");
        }
        out.println();
    }

    public void doNewObject(NewObject inst) {
        out.println(pref+"    "+ prtLocal(inst) + "   \t= new " +inst.type());
    }

    public void doPhi(Phi inst) {
        out.print(pref+inst.target().getIP() + ":");
        if (inst.parms() != null) {
            out.print(" phi "+ prtLocal(inst) + " = " + inst.parms().length + " sources");
        }
        out.println();
    }

    public void doReturn(Return inst) {
        out.print(pref+"    return");
        if (inst.value() != null) {
            out.print(" "+ prtLocal(inst.value()));
        }
        out.println();
    }

    public void doStoreField(StoreField inst) {
        out.println(pref + "    " +  prtField(inst.field(), inst.ref()) + " \t= " + prtLocal(inst.value()));
    }

    public void doStoreIndexed(StoreIndexed inst) {
        out.println(pref+"    "+ prtLocal(inst.array()) + "[" + prtLocal(inst.index()) + "]" + "   \t= " + prtLocal(inst.value()));
    }

    public void doStoreLocal(StoreLocal inst) {
        if (inst.local() != null) {
            out.println(pref+"    " + inst.local() + "   \t= " + prtLocal(inst.value()));
        }
        // else {
        //    out.println(pref+"    XXX3 " + inst.local() + "   \t= " + prtLocal(inst.value())); // temp
        //}

    }

    public void doTableSwitch(TableSwitch inst) {
        out.print(pref+"    tableswitch "+ prtLocal(inst.key()) + " ");
        for (int i = 0 ; i < inst.targets().length ; i++) {
            out.print(" (" + inst.low()+i + "=" + inst.targets()[i].getIP() + ")");
        }
        out.println(" (default=" + inst.defaultTarget().getIP() + ")");
    }

    public void doThrow(Throw inst) {
        out.println(pref+"    throw "+ prtLocal(inst.value()));
    }

}





/*
++IR1 trace for open(Ljavax/microedition/io/Connector;Ljava/lang/String;IZ)Ljavax/microedition/io/Connection;
    t0#         = Ljavax/microedition/io/Connector; [platform Ljava/lang/String;]
    if t0# == const("null")    goto 17
    handlerenter 16
    t1#         = Ljavax/microedition/io/Connector; [platform Ljava/lang/String;]
    t0#         = invoke openPrim(Ljavax/microedition/io/Connector;Ljava/lang/String;IZLjava/lang/String;)Ljavax/microedition/io/Connection;  l1# l2 l3 t1#
    return t0#
    handlerexit 16
16: l4# = exception
    handlerenter 37
17:
    t0#         = l1#
    t2          = l2
    t3          = l3
    t4          = Ljavax/microedition/io/Connector; [j2me Z]
    if t4 == const(0)    goto 31
    t1#         = const("j2me")
    goto 33
31:
    t1#         = const("j2se")
33: phi t1# = 1 sources
    t0#         = invoke openPrim(Ljavax/microedition/io/Connector;Ljava/lang/String;IZLjava/lang/String;)Ljavax/microedition/io/Connection;  t0# t2 t3 t1#
    return t0#
    handlerexit 37
37: l4# = exception
    t0#         = new Ljavax/microedition/io/ConnectionNotFoundException;
    t1#         = new Ljava/lang/StringBuffer;
    invoke <init>(Ljava/lang/StringBuffer;)V  t1#
    t1#         = invoke append(Ljava/lang/StringBuffer;Ljava/lang/String;)Ljava/lang/StringBuffer;  t1# const("The requested protocol does not exist ")
    t1#         = invoke append(Ljava/lang/StringBuffer;Ljava/lang/String;)Ljava/lang/StringBuffer;  t1# l1#
    t1#         = invoke toString(Ljava/lang/StringBuffer;)Ljava/lang/String;  t1#
    invoke <init>(Ljavax/microedition/io/ConnectionNotFoundException;Ljava/lang/String;)V  t0# t1#
    throw t0#

*/