/*
 *  Copyright (c) 1999-2001 Sun Microsystems, Inc., 901 San Antonio Road,
 *  Palo Alto, CA 94303, U.S.A.  All Rights Reserved.
 *
 *  Sun Microsystems, Inc. has intellectual property rights relating
 *  to the technology embodied in this software.  In particular, and
 *  without limitation, these intellectual property rights may include
 *  one or more U.S. patents, foreign patents, or pending
 *  applications.  Sun, Sun Microsystems, the Sun logo, Java, KJava,
 *  and all Sun-based and Java-based marks are trademarks or
 *  registered trademarks of Sun Microsystems, Inc.  in the United
 *  States and other countries.
 *
 *  This software is distributed under licenses restricting its use,
 *  copying, distribution, and decompilation.  No part of this
 *  software may be reproduced in any form by any means without prior
 *  written authorization of Sun and its licensors, if any.
 *
 *  FEDERAL ACQUISITIONS:  Commercial Software -- Government Users
 *  Subject to Standard License Terms and Conditions
 */

package com.sun.cldc.io;

import java.io.*;
import javax.microedition.io.*;

/**
 * This is the base class for objects that implement the DataInput
 * and DataOutput interfaces. The class also behaves like a
 * DataOutputStream so it can be used for UniversalOutputStream
 *
 * @author  Nik Shaylor
 * @version 1.1 2/20/2000
 */
abstract public class GeneralBase implements DataInput, DataOutput {

    /**
     * Writes the specified byte (the low eight bits of the argument
     * <code>b</code>) to the underlying output stream. If no exception
     * is thrown, the counter <code>written</code> is incremented by
     * <code>1</code>.
     * <p>
     * Implements the <code>write</code> method of <code>OutputStream</code>.
     *
     * @param      b   the <code>byte</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(int b) throws IOException {
        throw new RuntimeException("No write()");
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to the underlying output stream.
     * If no exception is thrown, the counter <code>written</code> is
     * incremented by <code>len</code>.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @exception  IOException  if an I/O error occurs.
     */
    public void write(byte b[], int off, int len) throws IOException {
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        } else if (len == 0) {
            return;
        }
        for (int i = 0 ; i < len ; i++) {
            write(b[off + i]);
        }
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array.
     *
     * @param      b     the data.
     * @exception  IOException  if an I/O error occurs.
     */
    public final void write(byte b[]) throws IOException {
        write(b, 0, b.length);
    }

    /**
     * Writes a <code>boolean</code> to the underlying output stream as
     * a 1-byte value. The value <code>true</code> is written out as the
     * value <code>(byte)1</code>; the value <code>false</code> is
     * written out as the value <code>(byte)0</code>. If no exception is
     * thrown, the counter <code>written</code> is incremented by
     * <code>1</code>.
     *
     * @param      v   a <code>boolean</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeBoolean(boolean v) throws IOException {
        write(v ? 1 : 0);
    }

    /**
     * Writes out a <code>byte</code> to the underlying output stream as
     * a 1-byte value. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>1</code>.
     *
     * @param      v   a <code>byte</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeByte(int v) throws IOException {
        write(v);
    }

    /**
     * Writes a <code>short</code> to the underlying output stream as two
     * bytes, high byte first. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>2</code>.
     *
     * @param      v   a <code>short</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeShort(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }

    /**
     * Writes a <code>char</code> to the underlying output stream as a
     * 2-byte value, high byte first. If no exception is thrown, the
     * counter <code>written</code> is incremented by <code>2</code>.
     *
     * @param      v   a <code>char</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeChar(int v) throws IOException {
        write((v >>> 8) & 0xFF);
        write((v >>> 0) & 0xFF);
    }

    /**
     * Writes an <code>int</code> to the underlying output stream as four
     * bytes, high byte first. If no exception is thrown, the counter
     * <code>written</code> is incremented by <code>4</code>.
     *
     * @param      v   an <code>int</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeInt(int v) throws IOException {
        write((v >>> 24) & 0xFF);
        write((v >>> 16) & 0xFF);
        write((v >>>  8) & 0xFF);
        write((v >>>  0) & 0xFF);
    }

    /**
     * Writes a <code>long</code> to the underlying output stream as eight
     * bytes, high byte first. In no exception is thrown, the counter
     * <code>written</code> is incremented by <code>8</code>.
     *
     * @param      v   a <code>long</code> to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeLong(long v) throws IOException {
        write((int)(v >>> 56) & 0xFF);
        write((int)(v >>> 48) & 0xFF);
        write((int)(v >>> 40) & 0xFF);
        write((int)(v >>> 32) & 0xFF);
        write((int)(v >>> 24) & 0xFF);
        write((int)(v >>> 16) & 0xFF);
        write((int)(v >>>  8) & 0xFF);
        write((int)(v >>>  0) & 0xFF);
    }

    /**
     * Writes a 32 bit float.
     * @param v the float value to be written
     */
    public final void writeFloat(float v) throws IOException {
        writeInt(Float.floatToIntBits(v));
    }

    /**
     * Writes a 64 bit double.
     * @param v the double value to be written
     */
    public final void writeDouble(double v) throws IOException {
        writeLong(Double.doubleToLongBits(v));
    }

    /**
     * Writes out the string to the underlying output stream as a
     * sequence of bytes. Each character in the string is written out, in
     * sequence, by discarding its high eight bits. If no exception is
     * thrown, the counter <code>written</code> is incremented by the
     * length of <code>s</code>.
     *
     * @param      s   a string of bytes to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeBytes(String s) throws IOException {
        throw new RuntimeException("Function not supported");
    }

    /**
     * Writes a string to the underlying output stream as a sequence of
     * characters. Each character is written to the data output stream as
     * if by the <code>writeChar</code> method. If no exception is
     * thrown, the counter <code>written</code> is incremented by twice
     * the length of <code>s</code>.
     *
     * @param      s   a <code>String</code> value to be written.
     * @exception  IOException  if an I/O error occurs.
     * @see        java.io.DataOutputStream#writeChar(int)
     */
    public void writeChars(String s) throws IOException {
        int len = s.length();
        for (int i = 0 ; i < len ; i++) {
            int v = s.charAt(i);
            write((v >>> 8) & 0xFF);
            write((v >>> 0) & 0xFF);
        }
    }

    /**
     * Writes a string to the underlying output stream using UTF-8
     * encoding in a machine-independent manner.
     * <p>
     * First, two bytes are written to the output stream as if by the
     * <code>writeShort</code> method giving the number of bytes to
     * follow. This value is the number of bytes actually written out,
     * not the length of the string. Following the length, each character
     * of the string is output, in sequence, using the UTF-8 encoding
     * for the character. If no exception is thrown, the counter
     * <code>written</code> is incremented by the total number of
     * bytes written to the output stream. This will be at least two
     * plus the length of <code>str</code>, and at most two plus
     * thrice the length of <code>str</code>.
     *
     * @param      str   a string to be written.
     * @exception  IOException  if an I/O error occurs.
     */
    public void writeUTF(String str) throws IOException {
        writeUTF(str, this);
    }

    /**
     * Writes a string to the specified DataOutput using UTF-8 encoding in a
     * machine-independent manner.
     * <p>
     * First, two bytes are written to out as if by the <code>writeShort</code>
     * method giving the number of bytes to follow. This value is the number of
     * bytes actually written out, not the length of the string. Following the
     * length, each character of the string is output, in sequence, using the
     * UTF-8 encoding for the character. If no exception is thrown, the
     * counter <code>written</code> is incremented by the total number of
     * bytes written to the output stream. This will be at least two
     * plus the length of <code>str</code>, and at most two plus
     * thrice the length of <code>str</code>.
     *
     * @param      str   a string to be written.
     * @param      out   destination to write to
     * @return     The number of bytes written out.
     * @exception  IOException  if an I/O error occurs.
     */
    public static int writeUTF(String str, DataOutput out)
        throws IOException {

        int strlen = str.length();
        int utflen = 0;
        char[] charr = new char[strlen];
        int c, count = 0;

        str.getChars(0, strlen, charr, 0);

        for (int i = 0; i < strlen; i++) {
            c = charr[i];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                utflen++;
            } else if (c > 0x07FF) {
                utflen += 3;
            } else {
                utflen += 2;
            }
        }

        if (utflen > 65535)
            throw new UTFDataFormatException();

        byte[] bytearr = new byte[utflen+2];
        bytearr[count++] = (byte) ((utflen >>> 8) & 0xFF);
        bytearr[count++] = (byte) ((utflen >>> 0) & 0xFF);
        for (int i = 0; i < strlen; i++) {
            c = charr[i];
            if ((c >= 0x0001) && (c <= 0x007F)) {
                bytearr[count++] = (byte) c;
            } else if (c > 0x07FF) {
                bytearr[count++] = (byte) (0xE0 | ((c >> 12) & 0x0F));
                bytearr[count++] = (byte) (0x80 | ((c >>  6) & 0x3F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            } else {
                bytearr[count++] = (byte) (0xC0 | ((c >>  6) & 0x1F));
                bytearr[count++] = (byte) (0x80 | ((c >>  0) & 0x3F));
            }
        }
        out.write(bytearr);

        return utflen + 2;
    }

    /**
     * Flushes this data output stream. This forces any buffered output
     * bytes to be written out to the stream.
     * <p>
     * The <code>flush</code> method of <code>DataOutputStream</code>
     * calls the <code>flush</code> method of its underlying output stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void flush() throws IOException {
    }

    /**
     * Closes this output stream and releases any system resources
     * associated with the stream.
     * <p>
     * The <code>close</code> method
     * calls its <code>flush</code> method, and then calls the
     * <code>close</code> method of its underlying output stream.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    public void close() throws IOException {
    }

//
// DataInput functions
//

    /**
     * Reads the next byte of data from the input stream. The value byte is
     * returned as an <code>int</code> in the range <code>0</code> to
     * <code>255</code>. If no byte is available because the end of the stream
     * has been reached, the value <code>-1</code> is returned. This method
     * blocks until input data is available, the end of the stream is detected,
     * or an exception is thrown.
     *
     * <p> A subclass must provide an implementation of this method.
     *
     * @return     the next byte of data, or <code>-1</code> if the end of the
     *             stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read() throws IOException {
        throw new RuntimeException("No read()");
    }

    /**
     * Skips over and discards <code>n</code> bytes of data from this input
     * stream. The <code>skip</code> method may, for a variety of reasons, end
     * up skipping over some smaller number of bytes, possibly <code>0</code>.
     * This may result from any of a number of conditions; reaching end of file
     * before <code>n</code> bytes have been skipped is only one possibility.
     * The actual number of bytes skipped is returned.  If <code>n</code> is
     * negative, no bytes are skipped.
     *
     * <p> The <code>skip</code> method of <code>InputStream</code> creates a
     * byte array and then repeatedly reads into it until <code>n</code> bytes
     * have been read or the end of the stream has been reached. Subclasses are
     * encouraged to provide a more efficient implementation of this method.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException  if an I/O error occurs.
     */
    public long skip(long n) throws IOException {
        long m = n;
        while (m > 0) {
            if(read() < 0) {
                break;
            }
            --m;
        }
        return n-m;
    }

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @param      b   the buffer into which the data is read.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public void readFully(byte b[]) throws IOException {
        readFully(b, 0, b.length);
    }

    /**
     * See the general contract of the <code>readFully</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @param      b     the buffer into which the data is read.
     * @param      off   the start offset of the data.
     * @param      len   the number of bytes to read.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public void readFully(byte b[], int off, int len) throws IOException {
        if (len < 0)
            throw new IndexOutOfBoundsException();

        int n = 0;
        while (n < len) {
            int ch = read();
            if (ch < 0) {
                throw new EOFException();
            }
            b[off+(n++)] = (byte)ch;
        }
    }

    /**
     * See the general contract of the <code>skipBytes</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the actual number of bytes skipped.
     * @exception  IOException   if an I/O error occurs.
     */
    public int skipBytes(int n) throws IOException {
        int total = 0;
        int cur = 0;

        while ((total<n) && ((cur = (int) skip(n-total)) > 0)) {
            total += cur;
        }

        return total;
    }

    /**
     * See the general contract of the <code>readBoolean</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the <code>boolean</code> value read.
     * @exception  EOFException  if this input stream has reached the end.
     */
    public boolean readBoolean() throws IOException {
        int ch = read();
        if (ch < 0)
            throw new EOFException();
        return (ch != 0);
    }

    /**
     * See the general contract of the <code>readByte</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next byte of this input stream as a signed 8-bit
     *             <code>byte</code>.
     * @exception  EOFException  if this input stream has reached the end.
     * @exception  IOException   if an I/O error occurs.
     */
    public byte readByte() throws IOException {
        int ch = read();
        if (ch < 0)
            throw new EOFException();
        return (byte)(ch);
    }

    /**
     * See the general contract of the <code>readUnsignedByte</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next byte of this input stream, interpreted as an
     *             unsigned 8-bit number.
     * @exception  EOFException  if this input stream has reached the end.
     * @exception  IOException   if an I/O error occurs.
     */
    public int readUnsignedByte() throws IOException {
        int ch = read();
        if (ch < 0)
            throw new EOFException();
        return ch;
    }

    /**
     * See the general contract of the <code>readShort</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes
     * for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream, interpreted
     *             as a signed 16-bit number.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading two bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public short readShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
             throw new EOFException();
        return (short)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * See the general contract of the <code>readUnsignedShort</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream, interpreted
     *             as an unsigned 16-bit integer.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading two bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public int readUnsignedShort() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
             throw new EOFException();
        return (ch1 << 8) + (ch2 << 0);
    }

    /**
     * See the general contract of the <code>readChar</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next two bytes of this input stream as
     *             a Unicode character.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading two bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public char readChar() throws IOException {
        int ch1 = read();
        int ch2 = read();
        if ((ch1 | ch2) < 0)
             throw new EOFException();
        return (char)((ch1 << 8) + (ch2 << 0));
    }

    /**
     * See the general contract of the <code>readInt</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next four bytes of this input stream, interpreted
     *             as an <code>int</code>.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading four bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public int readInt() throws IOException {
        int ch1 = read();
        int ch2 = read();
        int ch3 = read();
        int ch4 = read();
        if ((ch1 | ch2 | ch3 | ch4) < 0)
             throw new EOFException();
        return ((ch1 << 24) + (ch2 << 16) + (ch3 << 8) + (ch4 << 0));
    }

    /**
     * See the general contract of the <code>readLong</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     the next eight bytes of this input stream, interpreted
     *             as a
     *             <code>long</code>.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading eight bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    public long readLong() throws IOException {
        return ((long)(readInt()) << 32) + (readInt() & 0xFFFFFFFFL);
    }

    /**
     * Reads a 32 bit float.
     * @return the read 32 bit float.
     */
    public final float readFloat() throws IOException {
        return Float.intBitsToFloat(readInt());
    }

    /**
     * Reads a 64 bit double.
     * @return the 64 bit double read.
     */
    public final double readDouble() throws IOException {
        return Double.longBitsToDouble(readLong());
    }


    /**
     * See the general contract of the <code>readUTF</code>
     * method of <code>DataInput</code>.
     * <p>
     * Bytes for this operation are read from the contained
     * input stream.
     *
     * @return     a Unicode string.
     * @exception  EOFException  if this input stream reaches the end
     *                           before reading all the bytes.
     * @exception  IOException   if an I/O error occurs.
     * @see        java.io.DataInputStream#readUTF(java.io.DataInput)
     */
    public String readUTF() throws IOException {
        return DataInputStream.readUTF(this);
    }

    /**
     * Unsupported function
     */
    public String readLine() throws IOException {
        throw new RuntimeException("Function not supported");
    }

    /**
     * Allow other threads to run whilst I/O is not available.
     */
    public native static void iowait();

}


