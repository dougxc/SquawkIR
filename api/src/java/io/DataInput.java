/*
 * Copyright 1995-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

package java.io;

/**
 * The <code>DataInput</code> interface provides
 * for reading bytes from a binary stream and
 * reconstructing from them data in any of
 * the Java primitive types. There is also
 * a
 * facility for reconstructing a <code>String</code>
 * from data in Java modified UTF-8 format.
 * <p>
 * It is generally true of all the reading
 * routines in this interface that if end of
 * file is reached before the desired number
 * of bytes has been read, an <code>EOFException</code>
 * (which is a kind of <code>IOException</code>)
 * is thrown. If any byte cannot be read for
 * any reason other than end of file, an <code>IOException</code>
 * other than <code>EOFException</code> is
 * thrown. In particular, an <code>IOException</code>
 * may be thrown if the input stream has been
 * closed.
 *
 * @author  Frank Yellin
 * @version 1.15, 12/04/99 (CLDC 1.0, Spring 2000)
 * @see     java.io.DataInputStream
 * @see     java.io.DataOutput
 * @since   JDK1.0
 */
public
interface DataInput {
    /**
     * Reads some bytes from an input
     * stream and stores them into the buffer
     * array <code>b</code>. The number of bytes
     * read is equal
     * to the length of <code>b</code>.
     * <p>
     * This method blocks until one of the
     * following conditions occurs:<p>
     * <ul>
     * <li><code>b.length</code>
     * bytes of input data are available, in which
     * case a normal return is made.
     *
     * <li>End of
     * file is detected, in which case an <code>EOFException</code>
     * is thrown.
     *
     * <li>An I/O error occurs, in
     * which case an <code>IOException</code> other
     * than <code>EOFException</code> is thrown.
     * </ul>
     * <p>
     * If <code>b</code> is <code>null</code>,
     * a <code>NullPointerException</code> is thrown.
     * If <code>b.length</code> is zero, then
     * no bytes are read. Otherwise, the first
     * byte read is stored into element <code>b[0]</code>,
     * the next one into <code>b[1]</code>, and
     * so on.
     * If an exception is thrown from
     * this method, then it may be that some but
     * not all bytes of <code>b</code> have been
     * updated with data from the input stream.
     *
     * @param      b   the buffer into which the data is read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    void readFully(byte b[]) throws IOException;

    /**
     * Reads <code>len</code>
     * bytes from
     * an input stream.
     * <p>
     * This method
     * blocks until one of the following conditions
     * occurs:<p>
     * <ul>
     * <li><code>len</code> bytes
     * of input data are available, in which case
     * a normal return is made.
     *
     * <li>End of file
     * is detected, in which case an <code>EOFException</code>
     * is thrown.
     *
     * <li>An I/O error occurs, in
     * which case an <code>IOException</code> other
     * than <code>EOFException</code> is thrown.
     * </ul>
     * <p>
     * If <code>b</code> is <code>null</code>,
     * a <code>NullPointerException</code> is thrown.
     * If <code>off</code> is negative, or <code>len</code>
     * is negative, or <code>off+len</code> is
     * greater than the length of the array <code>b</code>,
     * then an <code>IndexOutOfBoundsException</code>
     * is thrown.
     * If <code>len</code> is zero,
     * then no bytes are read. Otherwise, the first
     * byte read is stored into element <code>b[off]</code>,
     * the next one into <code>b[off+1]</code>,
     * and so on. The number of bytes read is,
     * at most, equal to <code>len</code>.
     *
     * @param b    the buffer into which the data is read.
     * @param off  an int specifying the offset into the data.
     * @param len  an int specifying the number of bytes to read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    void readFully(byte b[], int off, int len) throws IOException;

    /**
     * Makes an attempt to skip over
     * <code>n</code> bytes
     * of data from the input
     * stream, discarding the skipped bytes. However,
     * it may skip
     * over some smaller number of
     * bytes, possibly zero. This may result from
     * any of a
     * number of conditions; reaching
     * end of file before <code>n</code> bytes
     * have been skipped is
     * only one possibility.
     * This method never throws an <code>EOFException</code>.
     * The actual
     * number of bytes skipped is returned.
     *
     * @param      n   the number of bytes to be skipped.
     * @return     the number of bytes skipped, which is always <code>n</code>.
     * @exception  EOFException  if this stream reaches the end before skipping
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    int skipBytes(int n) throws IOException;

    /**
     * Reads one input byte and returns
     * <code>true</code> if that byte is nonzero,
     * <code>false</code> if that byte is zero.
     * This method is suitable for reading
     * the byte written by the <code>writeBoolean</code>
     * method of interface <code>DataOutput</code>.
     *
     * @return     the <code>boolean</code> value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    boolean readBoolean() throws IOException;

    /**
     * Reads and returns one input byte.
     * The byte is treated as a signed value in
     * the range <code>-128</code> through <code>127</code>,
     * inclusive.
     * This method is suitable for
     * reading the byte written by the <code>writeByte</code>
     * method of interface <code>DataOutput</code>.
     *
     * @return     the 8-bit value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    byte readByte() throws IOException;

    /**
     * Reads one input byte, zero-extends
     * it to type <code>int</code>, and returns
     * the result, which is therefore in the range
     * <code>0</code>
     * through <code>255</code>.
     * This method is suitable for reading
     * the byte written by the <code>writeByte</code>
     * method of interface <code>DataOutput</code>
     * if the argument to <code>writeByte</code>
     * was intended to be a value in the range
     * <code>0</code> through <code>255</code>.
     *
     * @return     the unsigned 8-bit value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    int readUnsignedByte() throws IOException;

    /**
     * Reads two input bytes and returns
     * a <code>short</code> value. Let <code>a</code>
     * be the first byte read and <code>b</code>
     * be the second byte. The value
     * returned
     * is:
     * <p><pre><code>(short)((a &lt;&lt; 8) * | (b &amp; 0xff))
     * </code></pre>
     * This method
     * is suitable for reading the bytes written
     * by the <code>writeShort</code> method of
     * interface <code>DataOutput</code>.
     *
     * @return     the 16-bit value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    short readShort() throws IOException;

    /**
     * Reads two input bytes and returns
     * an <code>int</code> value in the range <code>0</code>
     * through <code>65535</code>. Let <code>a</code>
     * be the first byte read and
     * <code>b</code>
     * be the second byte. The value returned is:
     * <p><pre><code>(((a &amp; 0xff) &lt;&lt; 8) | (b &amp; 0xff))
     * </code></pre>
     * This method is suitable for reading the bytes
     * written by the <code>writeShort</code> method
     * of interface <code>DataOutput</code>  if
     * the argument to <code>writeShort</code>
     * was intended to be a value in the range
     * <code>0</code> through <code>65535</code>.
     *
     * @return     the unsigned 16-bit value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    int readUnsignedShort() throws IOException;

    /**
     * Reads an input <code>char</code> and returns the <code>char</code> value.
     * A Unicode <code>char</code> is made up of two bytes.
     * Let <code>a</code>
     * be the first byte read and <code>b</code>
     * be the second byte. The value
     * returned is:
     * <p><pre><code>(char)((a &lt;&lt; 8) | (b &amp; 0xff))
     * </code></pre>
     * This method
     * is suitable for reading bytes written by
     * the <code>writeChar</code> method of interface
     * <code>DataOutput</code>.
     *
     * @return     the Unicode <code>char</code> read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    char readChar() throws IOException;

    /**
     * Reads four input bytes and returns an
     * <code>int</code> value. Let <code>a</code>
     * be the first byte read, <code>b</code> be
     * the second byte, <code>c</code> be the third
     * byte,
     * and <code>d</code> be the fourth
     * byte. The value returned is:
     * <p><pre>
     * <code>
     * (((a &amp; 0xff) &lt;&lt; 24) | ((b &amp; 0xff) &lt;&lt; 16) |
     * &#32;((c &amp; 0xff) &lt;&lt; 8) | (d &amp; 0xff))
     * </code></pre>
     * This method is suitable
     * for reading bytes written by the <code>writeInt</code>
     * method of interface <code>DataOutput</code>.
     *
     * @return     the <code>int</code> value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    int readInt() throws IOException;

    /**
     * Reads eight input bytes and returns
     * a <code>long</code> value. Let <code>a</code>
     * be the first byte read, <code>b</code> be
     * the second byte, <code>c</code> be the third
     * byte, <code>d</code>
     * be the fourth byte,
     * <code>e</code> be the fifth byte, <code>f</code>
     * be the sixth byte, <code>g</code> be the
     * seventh byte,
     * and <code>h</code> be the
     * eighth byte. The value returned is:
     * <p><pre> <code>
     * (((long)(a &amp; 0xff) &lt;&lt; 56) |
     *  ((long)(b &amp; 0xff) &lt;&lt; 48) |
     *  ((long)(c &amp; 0xff) &lt;&lt; 40) |
     *  ((long)(d &amp; 0xff) &lt;&lt; 32) |
     *  ((long)(e &amp; 0xff) &lt;&lt; 24) |
     *  ((long)(f &amp; 0xff) &lt;&lt; 16) |
     *  ((long)(g &amp; 0xff) &lt;&lt;  8) |
     *  ((long)(h &amp; 0xff)))
     * </code></pre>
     * <p>
     * This method is suitable
     * for reading bytes written by the <code>writeLong</code>
     * method of interface <code>DataOutput</code>.
     *
     * @return     the <code>long</code> value read.
     * @exception  EOFException  if this stream reaches the end before reading
     *             all the bytes.
     * @exception  IOException   if an I/O error occurs.
     */
    long readLong() throws IOException;

    /**
     * Reads a 32 bit float.
     * @return the 32 bit float read.
     * @exception EOFException If end of file is reached.
     * @exception IOException If other I/O error has occurred.
     */
    float readFloat() throws IOException;

    /**
     * Reads a 64 bit double.
     * @return the 64 bit double read.
     * @exception EOFException If end of file is reached.
     * @exception IOException If other I/O error has occurred.
     */
    double readDouble() throws IOException;

    /**
     * Reads in a string that has been encoded using a modified UTF-8 format.
     * The general contract of <code>readUTF</code>
     * is that it reads a representation of a Unicode
     * character string encoded in Java modified
     * UTF-8 format; this string of characters
     * is then returned as a <code>String</code>.
     * <p>
     * First, two bytes are read and used to
     * construct an unsigned 16-bit integer in
     * exactly the manner of the <code>readUnsignedShort</code>
     * method . This integer value is called the
     * <i>UTF length</i> and specifies the number
     * of additional bytes to be read. These bytes
     * are then converted to characters by considering
     * them in groups. The length of each group
     * is computed from the value of the first
     * byte of the group. The byte following a
     * group, if any, is the first byte of the
     * next group.
     * <p>
     * If the first byte of a group
     * matches the bit pattern <code>0xxxxxxx</code>
     * (where <code>x</code> means "may be <code>0</code>
     * or <code>1</code>"), then the group consists
     * of just that byte. The byte is zero-extended
     * to form a character.
     * <p>
     * If the first byte
     * of a group matches the bit pattern <code>110xxxxx</code>,
     * then the group consists of that byte <code>a</code>
     * and a second byte <code>b</code>. If there
     * is no byte <code>b</code> (because byte
     * <code>a</code> was the last of the bytes
     * to be read), or if byte <code>b</code> does
     * not match the bit pattern <code>10xxxxxx</code>,
     * then a <code>UTFDataFormatException</code>
     * is thrown. Otherwise, the group is converted
     * to the character:<p>
     * <pre><code>(char)(((a&amp; 0x1F) &lt;&lt; 6) | (b &amp; 0x3F))
     * </code></pre>
     * If the first byte of a group
     * matches the bit pattern <code>1110xxxx</code>,
     * then the group consists of that byte <code>a</code>
     * and two more bytes <code>b</code> and <code>c</code>.
     * If there is no byte <code>c</code> (because
     * byte <code>a</code> was one of the last
     * two of the bytes to be read), or either
     * byte <code>b</code> or byte <code>c</code>
     * does not match the bit pattern <code>10xxxxxx</code>,
     * then a <code>UTFDataFormatException</code>
     * is thrown. Otherwise, the group is converted
     * to the character:<p>
     * <pre><code>
     * (char)(((a &amp; 0x0F) &lt;&lt; 12) | ((b &amp; 0x3F) &lt;&lt; 6) | (c &amp; 0x3F))
     * </code></pre>
     * If the first byte of a group matches the
     * pattern <code>1111xxxx</code> or the pattern
     * <code>10xxxxxx</code>, then a <code>UTFDataFormatException</code>
     * is thrown.
     * <p>
     * If end of file is encountered
     * at any time during this entire process,
     * then an <code>EOFException</code> is thrown.
     * <p>
     * After every group has been converted to
     * a character by this process, the characters
     * are gathered, in the same order in which
     * their corresponding groups were read from
     * the input stream, to form a <code>String</code>,
     * which is returned.
     * <p>
     * The <code>writeUTF</code>
     * method of interface <code>DataOutput</code>
     * may be used to write data that is suitable
     * for reading by this method.
     * @return     a Unicode string.
     * @exception  EOFException            if this stream reaches the end
     *             before reading all the bytes.
     * @exception  IOException             if an I/O error occurs.
     * @exception  UTFDataFormatException  if the bytes do not represent a
     *             valid UTF-8 encoding of a string.
     */
    String readUTF() throws IOException;

}


