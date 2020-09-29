
package com.sun.squawk.runtime.loader;
import  com.sun.squawk.runtime.*;
import  com.sun.squawk.runtime.util.*;

import java.io.InputStream;
import java.io.DataInputStream;
import java.io.IOException;

class ClassFileInputStream extends DataInputStream implements RuntimeConstants, InputContext {

   /**
    * The file name
    */
    String fileName;

   /**
    * The trace flag
    */
    boolean trace = false;

   /**
    * Constructor
    */
    public ClassFileInputStream(InputStream in) {
        super(in);
    }

    public ClassFileInputStream(InputStream in, String fileName) {
        this(in);
        this.fileName = fileName;
    }

    String getFileName() {
        return fileName;
    }

    String getDetails() {
        return "Verification error in "+fileName;
    }

    public VerificationException verificationException() throws VerificationException {
        return verificationException("");
    }

    public VerificationException verificationException(int code) throws VerificationException {
        return verificationException(code, null);
    }

    public VerificationException verificationException(int code, String msg) throws VerificationException {
        if (msg == null) {
            msg = "";
        } else {
            msg = " "+msg;
        }
        if (code < verifierMessage.length) {
             return verificationException(" - "+verifierMessage[code]+msg);
        } else {
             return verificationException(" - code "+code+" "+msg);
        }
    }

    public VerificationException verificationException(String msg) throws VerificationException {
        return new VerificationException(getDetails()+" "+msg);
    }

    void setTrace(boolean value) {
        trace = value;
    }

    public boolean trace() {
        return trace;
    }

    public int readInt(String s) throws IOException {
        int value = readInt();
        if (trace) {
           System.out.println(s+":"+value);
        }
        return value;

    }

    public int readUnsignedShort(String s) throws IOException {
        int value = readUnsignedShort();
        if (trace) {
           System.out.println(s+":"+value);
        }
        return value;
    }

    public char readChar(String s) throws IOException {
        return (char)readUnsignedShort(s);
    }

     public int readUnsignedByte(String s) throws IOException {
         int value = readUnsignedByte();
         if (trace) {
            System.out.println(s+":"+value);
         }
         return value;

     }


    public short readShort(String s) throws IOException {
        short value = readShort();
        if (trace) {
           System.out.println(s+":"+value);
        }
        return value;

    }

    public byte readByte(String s) throws IOException {
        byte value = readByte();
        if (trace) {
           System.out.println(s+":"+value);
        }
        return value;
    }


    public long readLong(String s) throws IOException {
        long value = readLong();
        if (trace) {
           System.out.println(s+":"+value);
        }
        return value;
    }

    public float readFloat(String s) throws IOException {
        float value = readFloat();
        if (trace) {
           System.out.println(s+":"+value);
        }
        return value;
    }

    public double readDouble(String s) throws IOException {
        double value = readDouble();
        if (trace) {
           System.out.println(s+":"+value);
        }
        return value;
    }


    public String readUTF(String s) throws IOException {
        String value = readUTF();
        if (trace) {
           System.out.println(s+":"+value);
        }
        return value;

    }

}
