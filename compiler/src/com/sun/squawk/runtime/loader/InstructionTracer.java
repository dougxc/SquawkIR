
package com.sun.squawk.runtime.loader;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;

import java.io.PrintStream;

public class InstructionTracer extends InstructionVisitor implements RuntimeConstants {


    private static final String xopNames[] = {
        "**error**",
        "add",
        "sub",
        "mul",
        "div",
        "rem",
        "shl",
        "shr",
        "ushr",
        "and",
        "or",
        "xor",
        "i2l",
        "i2f",
        "i2d",
        "l2i",
        "l2f",
        "l2d",
        "f2i",
        "f2l",
        "f2d",
        "d2i",
        "d2l",
        "d2f",
        "i2b",
        "i2c",
        "i2s",
        "lcmp",
        "fcmpl",
        "fcmpg",
        "dcmpl",
        "dcmpg",
        "1eq",
        "2ne",
        "3lt",
        "4ge",
        "5gt",
        "6le",
    };


    private PrintStream out;
    private int lastRes = -1;


    private ArrayHashtable cons = new ArrayHashtable();
    private int nextCon = 10;

    private int getConNumber(String string) {
        Integer i = (Integer)cons.get(string);
        if (i == null) {
            i = new Integer(nextCon++);
            cons.put(string, i);
        }
        return i.intValue();
    }


    private ArrayHashtable types = new ArrayHashtable();
    private int nextType = 10;

    private int getTypeNumber(String type) {
        Integer i = (Integer)types.get(type);
        if (i == null) {
            i = new Integer(nextType++);
            types.put(type, i);
        }
        return i.intValue();
    }

   /**
    * Constructor
    */
    public InstructionTracer(PrintStream out) {
        this.out = out;
    }

   /**
    * Main fucnction
    */
    public void trace(Instruction ir) {
        ir.visit(this);
        out.println();
    }

    public void print(char x) {
        out.print(x);
        out.print(' ');
    }

    public void print(int x) {
        out.print(x);
        out.print(' ');
    }

    public void print(long x) {
        out.print(x);
        out.print(' ');
    }

    public void print(char x[]) {
        out.print(x);
        out.print(' ');
    }

    public void print(String x) {
        out.print(x);
        out.print(' ');
    }

    public void print(Object x) {
        out.print(x);
        out.print(' ');
    }


//    private void print(String, str, Type type) {
//        print(str);
//        prtSlotType(type);
//    }


    private void prtType(Type type) {
        String s = type.toString();
        int    n = getTypeNumber(s);
        out.print("type(");

        out.print(""+n+":"+s);
        out.print(") ");
    }

    private void prtSlotType(Type type) {
        switch (type.slotType()) {
            case Type.BASIC_OOP:  print("<O1>"); return;
            case Type.BASIC_INT:  print("<I2>"); return;
            case Type.BASIC_LONG: print("<L3>"); return;
        }
        //print("<?"+type.slotType()+"?>");
        shouldNotReachHere();
    }

    private void prtPrimitiveType(Type type) {
        if        (type == Type.DOUBLE) {
            print("<d1>");
        } else if (type == Type.FLOAT) {
            print("<f2>");
        } else if (type == Type.LONG) {
            print("<l3>");
        } else if (type.isAssignableTo(Type.INT)) {
            print("<i4>");
        } else if (!type.isPrimitive()) {
            print("<o5>");
        } else {
        //    print("<?"+type.slotType()+"?>");
            shouldNotReachHere();
        }
    }

    private void prtIndexType(Type type) {
        if        (type == Type.DOUBLE) {
            print("<_d1>");
        } else if (type == Type.FLOAT) {
            print("<_f2>");
        } else if (type == Type.LONG) {
            print("<_l3>");
        } else if (type == Type.INT) {
            print("<_i4>");
        } else if (type == Type.CHAR) {
            print("<_c5>");
        } else if (type == Type.SHORT) {
            print("<_s6>");
        } else if (type == Type.BYTE) {
            print("<_b7>");
        } else if (type == Type.BOOLEAN) {
            print("<_b8>");
        } else if (!type.isPrimitive()) {
            print("<_o9>");
        } else {
        //    print("<?"+type.slotType()+"?>");
            shouldNotReachHere();
        }
    }


    private boolean isZero(Instruction inst) {
        if (inst instanceof LoadConstant) {
            LoadConstant con = (LoadConstant)inst;
            String s = con.toString();
            if (s.equals("const(\"null\")") || s.equals("const(null)") || s.equals("const(0)")) {
                return true;
            }
        }
        return false;
    }


    private boolean isTwo(Local local) {
        int offset = local.getOffset()+3;
        return offset == lastRes;
    }


    private boolean isTwo(Instruction inst) {
        Local local = inst.getResultLocal();
        if (local != null) {
            return isTwo(local);
        } else {
            if (inst instanceof LoadLocal) {
                return isTwo(((LoadLocal)inst).local());
            }
        }
        return false;
    }





    private int prtLocal(Local local, boolean output) {
        int offset = local.getOffset()+5; // because of 0, 1, 2, -1, and lastRes
        if (offset > 15 && offset == lastRes && !output) {
            print("4_was_"+offset);
            return lastRes;
        }
        print(offset);
        return offset;
    }

    private int prtLocal(Local local) {
        return prtLocal(local, false);
    }


    private void prtConst(LoadConstant con) {
        String s = con.toString();
        if (s.equals("const(\"null\")") || s.equals("const(null)") || s.equals("const(0)")) {
            print("const(0)");
        } else {
            if (con.isInt()) {
                int val = con.getInt();
                // 0, 1, and -1 are mappped to r0, r1, r2
                if (val == 0 || val == 1 || val == 2) {
                    print("const("+val+")");
                    return;
                } else if (val == -1) {
                    print("const(3)");
                    return;
                }
            }
            int lp = getConNumber(s);
            print("lpool(-"+lp+":"+s+")");
        }
    }

    private int prtLocal(Instruction inst, boolean output) {
        Local local = inst.getResultLocal();
        if (local != null) {
            return prtLocal(local, output);
        } else {
            if (inst instanceof LoadLocal) {
                return prtLocal(((LoadLocal)inst).local(), output);
            } else if (inst instanceof LoadConstant) {
                prtConst((LoadConstant)inst);
                return -1;
            } else {
                throw fatal("bad instruction "+inst);
            }
        }
    }

    private int prtLocal(Instruction inst) {
        return prtLocal(inst, false);
    }

    private int prtLocal(Instruction inst, int x) {
        return prtLocal(inst, false);
    }

    private int prtLocalRes(Instruction inst) {
        return prtLocal(inst, true);
    }



    private void prtField(Field field, Instruction ref) {
        if(ref == null) {
            print("~");
        } else {
            prtLocal(ref);
        }
        int offset = field.getSlotOffset();
        if (offset >= 30000) {
            out.print("global(");
            out.print(offset-30000);

        } else {
            out.print("offset(");
            out.print(offset);
        }
        out.print(") ");
    }

    private void prtMethod(Method method) {
        out.print("offset(");
        out.print(method.getSlotOffset());
        out.print(":"+method.name());
        out.print(") ");
    }


    private void prt2op(Instruction result,  String op, Instruction src) {
        print(op);
       // prtSlotType(src.type());
        int r = prtLocalRes(result);
        prtLocal(src, r);
        lastRes = r;
    }

    private void prt3op(Instruction result, Instruction left,  String op, Instruction right) {
        print(op);
        prtPrimitiveType(left.type());
        int r = prtLocalRes(result);
        prtLocal(left, r);
        prtLocal(right, r);
        lastRes = r;
    }

    private void prt2op(Instruction result, int op, Instruction src) {
        prt2op(result, xopNames[op], src);
    }

    private void prt3op(Instruction result, Instruction left, int op, Instruction right) {
        prt3op(result, left, xopNames[op], right);
    }

    private void prtTarget(int ip, Target target) {
        int delta = target.getIP() - ip;
        if (delta >= 0) {
            out.print('+');
        }
        print(delta);
    }






    public void doArithmeticOp(ArithmeticOp inst) {
        prt3op(inst, inst.left(), inst.op(), inst.right());
    }

    public void doArrayLength(ArrayLength inst) {
        prt2op(inst, "arraylength", inst.array());
    }

    public void doCheckCast(CheckCast inst) {
        print("checkcast");
        prtType(inst.checkType());
        lastRes = prtLocalRes(inst);
        prtLocal(inst.value());
    }

    public void doConvertOp(ConvertOp inst) {
        prt2op(inst, inst.op(), inst.value());
    }

    public void doDropLocal(DropLocal inst) {
    }

    public void doGoto(Goto inst) {
        print("goto");
        prtTarget(inst.getIP(), inst.target());
        lastRes = -1;
    }

    public void doIfOp(IfOp inst) {
        Instruction right = inst.right();
        Instruction left  = inst.left();
        int op = inst.op();
/*
        if (op == OP_GT) {
            Instruction t = left;
            left = right;
            right = t;
            op = OP_LE;
        }

        if (inst.op() == OP_GE) {
            Instruction t = left;
            left = right;
            right = t;
            op = OP_LT;
        }
*/
        boolean rightZero = isZero(right);
        boolean leftZero  = isZero(left);
        boolean rightTwo  = isTwo(right);
        boolean leftTwo   = isTwo(left);


        // swap R- for -R
        if(leftTwo==true && rightTwo==false && rightZero==false) {
            Instruction t = left;
            left = right;
            right = t;
            switch(op) {
                case OP_LT: op = OP_GE; break;
                case OP_LE: op = OP_GT; break;
                case OP_GT: op = OP_LE; break;
                case OP_GE: op = OP_LT; break;
            }
            rightZero = isZero(right);
            leftZero  = isZero(left);
            rightTwo  = isTwo(right);
            leftTwo   = isTwo(left);
        }

        String opc = xopNames[op];

        if(leftZero) {
            opc += "Z";
        } else if(leftTwo) {
            opc += "R";
        } else {
            opc += "-";
        }

        if(rightZero) {
            opc += "Z";
        } else if(rightTwo) {
            opc += "R";
        } else {
            opc += "-";
        }

        if (opc.endsWith("RZ")) {
            print("ifRZ "+opc);
        } else if (opc.endsWith("ZR")) {
            print("ifZR "+opc);
        } else if(opc.endsWith("-R")) {
            print("if-R "+opc);
        } else if(opc.endsWith("-Z") && (op==OP_EQ || op==OP_NE)) {
            print("if-R 1"+opc);
        } else {
            print("if "+opc);
        }

/*
        if (rightZero) {
           print("if"+op+"Z2");
        } else if (rightTwo) {
           print("if"+op+"Z2");
        } else {
           print("if"+op);
        }
*/
       // print(xopNames[inst.op()]);
        prtLocal(left);
        //if (!rightTwo && !rightZero) {
        prtLocal(right);
        //}
        prtTarget(inst.getIP(), inst.target());
    }

    public void doInstanceOf(InstanceOf inst) {
        print("instanceof");
        prtType(inst.checkType());
        lastRes = prtLocalRes(inst);
        prtLocal(inst.value());
    }

    public void doHandlerEnter(HandlerEnter inst) {
    }

    public void doHandlerExit(HandlerExit inst) {
    }

    public void doInitializeClass(InitializeClass inst) {
        print("initclass");
        prtType(inst.parent());
    }

    public void doInvoke(Invoke inst) {
        int r = -1;
        boolean hasResult = inst.getResultLocal() != null;

        //out.print(inst.method().isStatic() ? "invokestatic" : "invoke");
        out.print("invoke");
      //  if(hasResult) {
      //      print('R');
      //      r = prtLocal(inst);
      //  } else {
      //      print('V');
      //  }

        if(inst.parms().length == 1) {
           print('1');
        } else {
           print('N');
        }

        //if (hasResult) {
        //    r = prtLocalRes(inst);
        //} else {
        //    print('~');
        //}
        prtMethod(inst.method());
        //for (int i = 0 ; i < inst.parms().length ; i++) {
        //    prtLocal(inst.parms()[i], r);
        //}
        prtLocal(inst.parms()[0], r);
        print("count("+inst.parms().length+")");
        lastRes = r;
    }

    public void doLoadConstant(LoadConstant inst) {
        if (inst.getResultLocal() != null) {
            print("loadconstant");
            prtPrimitiveType(inst.type());
            lastRes = prtLocalRes(inst);
            prtConst(inst);
        }
    }

    public void doLoadException(LoadException inst) {
        print("loadexception");
        lastRes = -1;
        prtLocal(inst);
    }

   /*
    * ld slotType local ref offset
    */
    public void doLoadField(LoadField inst) {
        int offset = inst.field().getSlotOffset();
        print(offset < 30000 ? "loadfield" : "loadglobal");
        prtSlotType(inst.type());
        lastRes = prtLocalRes(inst);
        prtField(inst.field(), inst.ref());
    }

   /*
    * ldx slotType local ref index
    */
    public void doLoadIndexed(LoadIndexed inst) {
        print("loadindexed");
        prtIndexType(inst.array().type().elementType());
        lastRes = prtLocalRes(inst);
        prtLocal(inst.array());
        prtLocal(inst.index());
    }

    public void doLoadLocal(LoadLocal inst) {
        if (inst.getResultLocal() != null && inst.getResultLocal() !=  inst.local()) {
            print("add");
            prtPrimitiveType(inst.type());
            lastRes = prtLocalRes(inst);
            prtLocal(inst.local());
            print("0");
        }
    }

/*
    public void doLoadType(LoadType inst) {
        //assume (inst.getResultLocal() != null);
        print("loadtype");
        lastRes = prtLocalRes(inst);
        prtType(inst.realType());
    }
*/
    public void doMethodHeader(MethodHeader inst) {
    }

    public void doLookupSwitch(LookupSwitch inst) {
        int ip = inst.getIP();
        print("lookupswitch");
        prtLocal(inst.key());
        prtTarget(ip, inst.defaultTarget());
      //  for (int i = 0 ; i < inst.targets().length ; i++) {
      //      print(inst.matches()[i]);
      //      prtTarget(ip, inst.targets()[i]);
      //  }
    }

    public void doMonitorEnter(MonitorEnter inst) {
        print("monitorenter");
        prtLocal(inst.value());
    }

    public void doMonitorExit(MonitorExit inst) {
        print("monitorexit");
        prtLocal(inst.value());
    }

    public void doNegateOp(NegateOp inst) {
        print("neg");
        prtSlotType(inst.type());
        lastRes = prtLocalRes(inst);
        prtLocal(inst.value());
    }

    public void doNewArray(NewArray inst) {
        print("newarray");
        prtType(inst.type());
        lastRes = prtLocalRes(inst);
        prtLocal(inst.size());
    }

    public void doNewMultiArray(NewMultiArray inst) {
        print("newmultiarray");
        prtType(inst.type());
        lastRes = prtLocalRes(inst);
        for (int i = 0 ; i < inst.dimList().length ; i++) {
            print(inst.dimList()[i]);
        }
    }

    public void doNewObject(NewObject inst) {
        print("new");
        prtType(inst.type());
        lastRes = prtLocalRes(inst);
    }

    public void doPhi(Phi inst) {
        lastRes = -1;
    }

    public void doReturn(Return inst) {
        print("return");
        if (inst.value() == null) {
  //          print("~");
        } else {
            prtPrimitiveType(inst.type());
            prtLocal(inst.value());
        }
    }

    public void doStoreField(StoreField inst) {
        int offset = inst.field().getSlotOffset();
        print(offset < 30000 ? "storefield" : "storeglobal");
        prtSlotType(inst.value().type());
        prtLocal(inst.value());
        prtField(inst.field(), inst.ref());
    }

    public void doStoreIndexed(StoreIndexed inst) {
        print("storeindexed");
        prtIndexType(inst.array().type().elementType());
        prtLocal(inst.value());
        prtLocal(inst.array());
        prtLocal(inst.index());
    }

    public void doStoreLocal(StoreLocal inst) {
        if (inst.local() != null) {
            print("add");
            prtPrimitiveType(inst.value().type());
            prtLocal(inst.local());
            prtLocal(inst.value());
            print("0");
        }
    }

    public void doTableSwitch(TableSwitch inst) {
        int ip = inst.getIP();
        print("tableswitch");
        prtLocal(inst.key());
        print(inst.low());
        prtTarget(ip, inst.defaultTarget());
      //  for (int i = 0 ; i < inst.targets().length ; i++) {
      //      prtTarget(ip, inst.targets()[i]);
      //  }
    }

    public void doThrow(Throw inst) {
        print("throw");
        prtLocal(inst.value());
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

/*


    while (true) {
        reg[0] = 0;
        curent = nextInst;
        top4 = (curent>>12) & 0xF;
        nextInst = halfcodes[ip++];
        if (top4 < 4) {
            int rs1 =  curent     & 0xF;
            int rs2 = (curent>>4) & 0xF;
            int rs3 = (curent>>8) & 0xF;
            if (rs1 == 0xF) {
                rs1 = nextInst;
                nextInst = halfcodes[ip++];
            }
            if (rs2 == 0xF) {
                rs2 = nextInst;
                nextInst = halfcodes[ip++];
            }
            if (rs3 == 0xF) {
                rs3 = nextInst;
                nextInst = halfcodes[ip++];
            }
            if (top4 == 0) {                        // ld
                value = (reg[rs2])[rs3];
            } else if (top4 == 1) {                 // add
                value = reg[rs2] + reg[rs3];
            } else {                                // st
                (reg[rs2])[rs3] = reg[rs1];
                if (WRITEBARRIER && (top4 == 3) {
                   // do write barrier stuff
                }
                continue;
            }
            reg[rs1] = value;
            reg[2]   = value;
            continue;
        }
        if (top < 8) {
            continue;
        }

        offset = curent & 0x3F;
        if (offset == 0x3F) {
            offset = nextInst;
            nextInst = halfcodes[ip++];
        }

        if (top >= 12) {                // if
            int right = reg[2];
            int left = (curent>>6)  & 0xF;
            int op   = (curent>>10) & 0xF;
            if (left == 0xF) {
                left = nextInst;
                nextInst = halfcodes[ip++];
            }
            left = reg[left];
            if        (op == 0) {        // eq
                res = left == 0;
            } else if (op == 1) {        // ne
                res = left == right;
            } else if (op == 2) {        // eq
                res = left != 0;
            } else if (op == 3) {        // ne
                res = left != right;
            } else if (op == 4) {        // lt
                res = left < right;
            } else if (op == 5) {        // le
                res = left <= right;
            } else if (op == 6) {        // gt
                res = left > right;
            } else              {        // ge
                res = left >= right;
            }
            if (res) {
               ip += offset;
            }
            continue;
        }






    }



            if (rs3 >= 0xF) {
                rs3 = nextInst;
                nextInst = halfcodes[ip++];
            }



            if (rs3 < 0xF) {
                rs3 = reg[rs3];
            } else {
                rs3 = nextInst;
                nextInst = halfcodes[ip++];
                if (rs3 <= -(32768-256)) {
                    rs3 = reg[rs3&0xFF];
                }
            }

*/


















/*


    while (true) {
        curent = nextInst;
        top4 = (current>>12) & 0xF;
        nextInst = halfcodes[ip++];


        if (top4 < 4) {
            int rs1 =  current     & 0xF;
            int rs2 = (current>>4) & 0xF;
            int rs3 = (current>>8) & 0xF;
            if (top4 == 0) {                        // ld
                value = (reg[rs2])[rs3];
            } else if (top4 == 1) {                 // add
                value = reg[rs2] + reg[rs3];
            } else {                                // st
                (reg[rs2])[rs3] = reg[rs1];
                if (WRITEBARRIER && (top4 == 3) {   // stoop
                   // do write barrier stuff
                }
                continue;
            }
            reg[rs1] = value;
            continue;
        }


        if (top < 8) {
            continue;
        }

        offset = curent & 0x3F;
        if (offset == 0x3F) {
            offset = nextInst;
            nextInst = halfcodes[ip++];
        }

        if (top >= 12) {                // if
            int right = reg[2];
            int left = (curent>>6)  & 0xF;
            int op   = (curent>>10) & 0xF;
            if (left == 0xF) {
                left = nextInst;
                nextInst = halfcodes[ip++];
            }
            left = reg[left];
            if        (op == 0) {        // eq
                res = left == 0;
            } else if (op == 1) {        // ne
                res = left == right;
            } else if (op == 2) {        // eq
                res = left != 0;
            } else if (op == 3) {        // ne
                res = left != right;
            } else if (op == 4) {        // lt
                res = left < right;
            } else if (op == 5) {        // le
                res = left <= right;
            } else if (op == 6) {        // gt
                res = left > right;
            } else              {        // ge
                res = left >= right;
            }
            if (res) {
               ip += offset;
            }
            continue;
        }
*/