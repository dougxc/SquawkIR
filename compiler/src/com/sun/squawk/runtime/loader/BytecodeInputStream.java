
package com.sun.squawk.runtime.loader;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.ir.*;
import  com.sun.squawk.runtime.util.*;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;

public class BytecodeInputStream extends ClassFileInputStream implements RuntimeConstants {

   /**
    * Method being read
    */
    private Method method;

   /**
    * Constant pool for method
    */
    private ConstantPool pool;

   /**
    * Stack map for method
    */
    private StackMap map;

   /**
    * The highest valid ldc slot value
    */
    private int maxLocals;

   /**
    * The IP of the start of the last bytecode
    */
    private int lastBytecode;

   /**
    * Limiter for the bytecodes input stream
    */
    private LimitInputStream lis;

   /**
    * The target that just produced a opc_branchtarget or opc_exceptiontarget
    */
    private BytecodeAddress lastTarget;

   /**
    * The target of a opc_handlerend that was held back until after a rfeal bytecode
    */
//    private BytecodeAddress heldBackTarget;

   /**
    * Hashtable of branch targets
    */
    private IntHashtable targets;

   /**
    * List of sorted BytecodeAddress objects
    */
    private Object[] addressList;

   /**
    * Current entry in the above list
    */
    private int listEntry = 0;

   /**
    * The ip address in the current list entry
    */
    private int nextListEntry;

   /**
    * Public static constructor
    */
    public static BytecodeInputStream create(
                                              InputStream is,
                                              int length,
                                              String fileName,
                                              Method method,
                                              ConstantPool pool,
                                              int maxLocals,
                                              StackMap map,
                                              Object[] addressList
                                             ) {
        return new BytecodeInputStream(new LimitInputStream(is, length), fileName, method, pool, maxLocals, map, addressList);
    }

   /**
    * Private constructor
    */
    private BytecodeInputStream(LimitInputStream lis, String fileName, Method method, ConstantPool pool, int maxLocals, StackMap map, Object[] addressList) {
        super(lis, fileName);
        this.lis = lis;
        this.method = method;
        this.pool = pool;
        this.maxLocals = maxLocals;
        this.map = map;
        this.addressList = addressList;

        if (map != null) {
            targets = map.getTargets();
        } else {
            targets = StackMap.ZEROTABLE;
        }

       /*
        * Get the ip address of the first entry in the address list
        */
        if (addressList.length > 0) {
            nextListEntry = ((BytecodeAddress)addressList[listEntry]).getIP();
        } else {
            nextListEntry = Integer.MAX_VALUE;
        }
    }

   /**
    * Return EOF state
    */
    public boolean atEof() {
        return lis.atEof();
    }

   /**
    * Get the last IP address
    */
    public Method getMethod() {
        return method;
    }

   /**
    * Get the current IP address
    */
    public int getCurrentIP() {
        return lis.getIP();
    }

   /**
    * Get the last IP address
    */
    public int getLastIP() {
        return lastBytecode;
    }

   /*
    * Write a trace message
    */
    public void trace(String str) {
        if (trace) {
            System.out.print(str);
        }
    }

   /*
    * Write a trace message
    */
    public void traceln(String str) {
        if (trace) {
            System.out.println(str);
        }
    }

   /**
    * Read the next bytecode
    */
    public int readBytecode() throws IOException, VerificationException {

//        if (heldBackTarget != null) {
//            lastTarget = heldBackTarget;
//            heldBackTarget = null;
//            return lastTarget.opcode();
//        }


       /*
        * Get the ip address of the current bytecode
        */
        lastBytecode = lis.getIP();

       /*
        * First look for a list entry
        */
        if (nextListEntry <= lastBytecode) {

           /*
            * Check for corrup stackmap
            */
            if (nextListEntry < lastBytecode) {
                throw verificationException(VE_BAD_STACKMAP, ""+nextListEntry+" < "+lastBytecode);
            }

           /*
            * Get the BytecodeAddress entry
            */
            lastTarget = (BytecodeAddress)addressList[listEntry++];

           /*
            * Get the ip address of the next entry in the address list
            */
            if (listEntry < addressList.length) {
                nextListEntry =  ((BytecodeAddress)addressList[listEntry]).getIP();
            } else {
                nextListEntry = Integer.MAX_VALUE;
            }

           /*
            * Get the opcode
            */
            int opcode = lastTarget.opcode();

           /*
            * Return the opcode if it is not opc_handlerend
            */
//            if (opcode != opc_handlerend) {
                return opcode;
//            }

           /*
            * opc_handlerends must be held back until after the real instruction has finished
            */
//            heldBackTarget = lastTarget;
        }

       /*
        * Zero lastTarget for safety
        */
        lastTarget = null;

       /*
        * Get the bytecode
        */
        int res = readUnsignedByte();

       /*
        * If at the end make sure that the address list was completely read
        */
        if (res == -1 && nextListEntry != Integer.MAX_VALUE) {
            throw verificationException(VE_BAD_STACKMAP);
        }

       /*
        * Return the bytecode
        */
        return res;
    }

   /**
    * Read the next logical bytecode
    */
    public int readBytecode(String s) throws IOException, VerificationException {
        int code = readBytecode();
        if (trace) {
           s = "["+lastBytecode+"] ";
           trace(s);
           if (code < opcNames.length) {
               traceln(opcNames[code]);
           } else {
               traceln(""+code);
           }
           //traceln("");
        }
        return code;
    }

   /**
    * Return the target at lastBytecode
    */
    public Target readTarget() {
        traceln("readTarget "+lastTarget.getIP());
        return (Target)lastTarget;
    }

   /**
    * Return the target at lastBytecode
    */
    public Target readPoint() {
        if (lastTarget instanceof HandlerStartPoint) {
            traceln("readStartPoint "+((HandlerStartPoint)lastTarget).target().getIP());
            return ((HandlerStartPoint)lastTarget).target();
        }
        if (lastTarget instanceof HandlerEndPoint) {
            traceln("readEndPoint "+((HandlerEndPoint)lastTarget).target().getIP());
            return ((HandlerEndPoint)lastTarget).target();
        }
        BaseFunctions.shouldNotReachHere();
        return null;
    }

   /**
    * Return the target at lastBytecode + offset
    */
    public Target readTarget2() throws IOException, VerificationException {
        int offset = lastBytecode + readShort();
        Target target = (Target)targets.get(offset);
        traceln("readTarget2 "+target.getIP());
        if (target == null) {
            throw verificationException(VE_TARGET_BAD_TYPE);
        }
        return target;
    }

   /**
    * Return the target at lastBytecode + offset
    */
    public Target readTarget4() throws IOException, VerificationException {
        int offset = lastBytecode + readInt();
        Target target = (Target)targets.get(offset);
        traceln("readTarget4 "+target.getIP());
        if (target == null) {
            throw verificationException(VE_TARGET_BAD_TYPE);
        }
        return target;
    }

   /**
    * Read a few nulls until the strean is aligned to a cell boundry
    */
    public void roundToCellBoundry() throws IOException, VerificationException {
        while (lis.getIP()%4 != 0) {
            int ch = read();
            if (ch != 0) {
                throw verificationException();
            }
            traceln("roundToCellBoundry++");
        }
    }

   /**
    * Return a constant
    */
    public Instruction readLdc() throws IOException, VerificationException {
        LoadConstant lc = ldc(readUnsignedByte(), false);
        traceln("readLdc "+lc);
        return lc;
    }

   /**
    * Return a constant
    */
    public Instruction readLdc_w() throws IOException, VerificationException {
        LoadConstant lc = ldc(readUnsignedShort(), false);
        traceln("readLdc_w "+lc);
        return lc;
    }

   /**
    * Return a constant long/double
    */
    public Instruction readLdc2_w() throws IOException, VerificationException {
        LoadConstant lc = ldc(readUnsignedShort(), true);
        traceln("readLdc2_w "+lc);
        return lc;
    }

   /**
    * Return a constant
    */
    private LoadConstant ldc(int index, boolean isLong) throws IOException, VerificationException {
        int tag = pool.getTag(index);
        if (isLong) {
            if (tag == CONSTANT_Long) {
                return LoadConstant.create(pool.getLong(index));
            }
            if (tag == CONSTANT_Double) {
                return LoadConstant.create(pool.getDouble(index));
            }
        } else {
            if (tag == CONSTANT_Integer) {
                return LoadConstant.create(pool.getInt(index));
            }
            if (tag == CONSTANT_Float) {
                return LoadConstant.create(pool.getFloat(index));
            }
            if (tag == CONSTANT_String) {
                return LoadConstant.create(pool.getString(index));
            }
        }
        throw verificationException(VE_BAD_LDC);
    }

   /**
    * Return a new array type
    */
    public Type readNewArrayType() throws IOException, VerificationException {
        int code = readUnsignedByte();
        Type result;
        switch (code) {
            case T_BOOLEAN: result =  Type.BOOLEAN_ARRAY;   break;
            case T_BYTE:    result =  Type.BYTE_ARRAY;      break;
            case T_CHAR:    result =  Type.CHAR_ARRAY;      break;
            case T_SHORT:   result =  Type.SHORT_ARRAY;     break;
            case T_INT:     result =  Type.INT_ARRAY;       break;
            case T_LONG:    result =  Type.LONG_ARRAY;      break;
            case T_FLOAT:   result =  Type.FLOAT_ARRAY;     break;
            case T_DOUBLE:  result =  Type.DOUBLE_ARRAY;    break;
            default: throw verificationException("Bad new array type "+code);
        }
        traceln("readNewArrayType "+code+" = "+result);
        return result;

    }

   /**
    * Return a type
    */
    public Type readType() throws IOException, VerificationException {
        int index = readUnsignedShort();
        int tag = pool.getTag(index);
        if (tag != CONSTANT_Class) {
            throw verificationException(VE_EXPECT_CLASS);
        }
        Type type = pool.getType(index);
        traceln("readType "+type);
        return type;

    }

   /**
    * Return a type for a new instruction
    */
    public Type readNewType() throws IOException, VerificationException {
       /*
        * Get the constant pool entry
        */
        int typeIndex = readUnsignedShort();
        Type newType = pool.getType(typeIndex);

       /*
        * Look for a type proxy for this location in the stackmap.
        * If there is one then set the proxy's type to the one just
        * found, Otherwize simply return the constant pool type.
        */
        if (map != null) {
            TypeProxy proxy = map.findNewType(lastBytecode, newType);
            if (proxy != null) {
                proxy.setProxy(newType);
                newType = proxy;
            }
        }

       /*
        * Trace and return
        */
        traceln("readNewType "+newType);
        return newType;
    }


   /**
    * Return a field
    */
    public Field readField() throws IOException, VerificationException {
        int index = readUnsignedShort();
        int tag = pool.getTag(index);
        if (tag != CONSTANT_Field) {
            throw verificationException(VE_EXPECT_FIELDREF);
        }
        Field f = pool.getField(index);
        traceln("readField "+f);
        return f;
    }

   /**
    * Return a method
    */
    public Method readMethod() throws IOException, VerificationException {
        int index = readUnsignedShort();
        int tag = pool.getTag(index);
        if (tag != CONSTANT_Method && tag != CONSTANT_InterfaceMethod) {
            throw verificationException(VE_EXPECT_METHODREF);
        }
        Method m = pool.getMethod(index);
        String verbose = ""+m.parent()+"::";
        traceln("readMethod "+verbose+m);
        return m;
    }


   /**
    * Return slot in the next u1
    */
    private int checkSlot(int slot, int width) throws VerificationException {
        if ((slot+width) > maxLocals) {
            String msg = ""+(slot+width)+" is more than "+maxLocals;
            throw verificationException(VE_LOCALS_OVERFLOW, msg);
        }
        traceln("readSlot "+slot);
        return slot;
    }

   /**
    * Return slot in the next u1
    */
    public int readSlot1(int width) throws IOException, VerificationException {
        return checkSlot(readUnsignedByte(), width);
    }

   /**
    * Return slot in the next u2
    */
    public int readSlot2(int width) throws IOException, VerificationException {
        return checkSlot(readUnsignedShort(), width);
    }
}
