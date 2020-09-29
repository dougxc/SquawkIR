
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.*;

public abstract class LoadConstant extends Instruction {

    public static LoadConstant create(int value)      { return new LoadConstantInt(value);    } // int
    public static LoadConstant create(long value)     { return new LoadConstantLong(value);   } // long
    public static LoadConstant create(float value)    { return new LoadConstantFloat(value);  } // float
    public static LoadConstant create(double value)   { return new LoadConstantDouble(value); } // double
    public static LoadConstant create(String value)   { return new LoadConstantString(value); } // String
    public static LoadConstant createType(Type value) { return new LoadConstantType(value);   } // Type
    public static LoadConstant createNull()           { return new LoadConstantObject();      } // Result of an aconst_null

    public LoadConstant(Type type) {
        super(type);
    }

    public boolean isConstant() {
        return true;
    }

    public boolean isSimpleArgument() {
        return true;
    }

    public int getInt() {
        return shouldNotReachHere();
    }

    public long getLong() {
        return shouldNotReachHere();
    }

    public float getFloat() {
        return shouldNotReachHere();
    }

    public double getDouble() {
        return shouldNotReachHere();
    }

    public String getString() {
        shouldNotReachHere();
        return null;
    }

    public Type getType() {
        shouldNotReachHere();
        return null;
    }

    public boolean isConstNull() {
        return false;
    }

    public boolean isConstInit() {
        return false;
    }

    public boolean isConstNew() {
        return false;
    }

    public boolean isInt() {
        return false;
    }

    public boolean isLong() {
        return false;
    }

    public boolean isFloat() {
        return false;
    }

    public boolean isDouble() {
        return false;
    }

    public boolean isString() {
        return false;
    }

    public boolean isType() {
        return false;
    }

    public int getNewIP() {
        return shouldNotReachHere();
    }

    public String toString() {
        shouldNotReachHere();
        return null;
    }

    public void visit(InstructionVisitor visitor) {
        visitor.doLoadConstant(this);
    }
}

class LoadConstantInt extends LoadConstant {
    int value;
    public LoadConstantInt(int value) {
        super(Type.INT);
        this.value = value;
    }
    public boolean isInt() {
        return true;
    }
    public int getInt() {
        return value;
    }
    public String toString() {
        return "const("+value+")";
    }
}

class LoadConstantLong extends LoadConstant {
    long value;
    public LoadConstantLong(long value) {
        super(Type.LONG);
        this.value = value;
    }
    public boolean isLong() {
        return true;
    }
    public long getLong() {
        return value;
    }
    public String toString() {
        return "const("+value+"L)";
    }
}

class LoadConstantFloat extends LoadConstant {
    float value;
    public LoadConstantFloat(float value) {
        super(Type.FLOAT);
        this.value = value;
    }
    public boolean isFloat() {
        return true;
    }
    public float getFloat() {
        return value;
    }
    public int getInt() {
        return Float.floatToIntBits(value);
    }
    public String toString() {
        return "const("+value+"F)";
    }
}

class LoadConstantDouble extends LoadConstant {
    double value;
    public LoadConstantDouble(double value) {
        super(Type.DOUBLE);
        this.value = value;
    }
    public boolean isDouble() {
        return true;
    }
    public double getDouble() {
        return value;
    }
    public long getLong() {
        return Double.doubleToLongBits(value);
    }
    public String toString() {
        return "const("+value+"D)";
    }
}

class LoadConstantString extends LoadConstant {
    String value;
    public LoadConstantString(String value) {
        super(Type.STRING);
        this.value = value;
    }
    public boolean isString() {
        return true;
    }
    public String getString() {
        return value;
    }
    public String toString() {
        String s = value;
        if (s == null) {
           s = "null";
        }
        s = s.replace('\n', '~');
        return "const(\""+s+"\")";
    }
}

class LoadConstantType extends LoadConstant {
    Type realType;
    public LoadConstantType(Type realType) {
        super(Type.CLASS);
        this.realType = realType;
    }
    public boolean isType() {
        return true;
    }
    public Type getType() {
        return realType;
    }
    public String toString() {
        return "const("+realType.toString()+")";
    }
}

class LoadConstantObject extends LoadConstant {
    public LoadConstantObject() {
        super(Type.NULLOBJECT);
    }
    public boolean isConstNull() {
        return true;
    }
    public String toString() {
        return "const(null)";
    }
}
