
package com.sun.squawk.runtime.ir;
import  com.sun.squawk.runtime.util.*;

public abstract class ParameterVisitor extends BaseFunctions {
    public abstract Instruction doParameter(Instruction inst, Instruction parm);
}
