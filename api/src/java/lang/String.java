/*
 * Copyright 1994-2001 Sun Microsystems, Inc. All Rights Reserved.
 *
 * This software is the proprietary information of Sun Microsystems, Inc.
 * Use is subject to license terms.
 *
 */

package java.lang;

import java.io.UnsupportedEncodingException;
import com.sun.cldc.i18n.*;

/**
 * The <code>String</code> class represents character strings. All
 * string literals in Java programs, such as <code>"abc"</code>, are
 * implemented as instances of this class.
 * <p>
 * Strings are constant; their values cannot be changed after they
 * are created. String buffers support mutable strings.
 * Because String objects are immutable they can be shared. For example:
 * <p><blockquote><pre>
 *     String str = "abc";
 * </pre></blockquote><p>
 * is equivalent to:
 * <p><blockquote><pre>
 *     char data[] = {'a', 'b', 'c'};
 *     String str = new String(data);
 * </pre></blockquote><p>
 * Here are some more examples of how strings can be used:
 * <p><blockquote><pre>
 *     System.out.println("abc");
 *     String cde = "cde";
 *     System.out.println("abc" + cde);
 *     String c = "abc".substring(2,3);
 *     String d = cde.substring(1, 2);
 * </pre></blockquote>
 * <p>
 * The class <code>String</code> includes methods for examining
 * individual characters of the sequence, for comparing strings, for
 * searching strings, for extracting substrings, and for creating a
 * copy of a string with all characters translated to uppercase or to
 * lowercase.
 * <p>
 * The Java language provides special support for the string
 * concatenation operator (&nbsp;+&nbsp;), and for conversion of
 * other objects to strings. String concatenation is implemented
 * through the <code>StringBuffer</code> class and its
 * <code>append</code> method.
 * String conversions are implemented through the method
 * <code>toString</code>, defined by <code>Object</code> and
 * inherited by all classes in Java. For additional information on
 * string concatenation and conversion, see Gosling, Joy, and Steele,
 * <i>The Java Language Specification</i>.
 *
 * @author  Lee Boynton
 * @author  Arthur van Hoff
 * @version 1.121, 10/06/99 (CLDC 1.0, Spring 2000)
 * @see     java.lang.Object#toString()
 * @see     java.lang.StringBuffer
 * @see     java.lang.StringBuffer#append(boolean)
 * @see     java.lang.StringBuffer#append(char)
 * @see     java.lang.StringBuffer#append(char[])
 * @see     java.lang.StringBuffer#append(char[], int, int)
 * @see     java.lang.StringBuffer#append(int)
 * @see     java.lang.StringBuffer#append(long)
 * @see     java.lang.StringBuffer#append(java.lang.Object)
 * @see     java.lang.StringBuffer#append(java.lang.String)
 * @since   JDK1.0
 */
public final
class String {

    /** The value is used for character storage. */
    private char value[];

    /** The offset is the first index of the storage that is used. */
    private int offset;

    /** The count is the number of characters in the String. */
    private int count;

    /**
     * Initializes a newly created <code>String</code> object so that it
     * represents an empty character sequence.
     */
    public String() {
        value = new char[0];
    }

    /**
     * Initializes a newly created <code>String</code> object so that it
     * represents the same sequence of characters as the argument; in other
     * words, the newly created string is a copy of the argument string.
     *
     * @param   value   a <code>String</code>.
     */
    public String(String value) {
        count = value.length();
        this.value = new char[count];
        value.getChars(0, count, this.value, 0);
    }

    /**
     * Allocates a new <code>String</code> so that it represents the
     * sequence of characters currently contained in the character array
     * argument. The contents of the character array are copied; subsequent
     * modification of the character array does not affect the newly created
     * string.
     *
     * @param  value   the initial value of the string.
     * @throws NullPointerException if <code>value</code> is <code>null</code>.
     */
    public String(char value[]) {
        this.count = value.length;
        this.value = new char[count];
        System.arraycopy(value, 0, this.value, 0, count);
    }

    /**
     * Allocates a new <code>String</code> that contains characters from
     * a subarray of the character array argument. The <code>offset</code>
     * argument is the index of the first character of the subarray and
     * the <code>count</code> argument specifies the length of the
     * subarray. The contents of the subarray are copied; subsequent
     * modification of the character array does not affect the newly
     * created string.
     *
     * @param      value    array that is the source of characters.
     * @param      offset   the initial offset.
     * @param      count    the length.
     * @exception  IndexOutOfBoundsException  if the <code>offset</code>
     *               and <code>count</code> arguments index characters outside
     *               the bounds of the <code>value</code> array.
     * @exception NullPointerException if <code>value</code> is
     *               <code>null</code>.
     */
    public String(char value[], int offset, int count) {
        if (offset < 0) {
            throw new StringIndexOutOfBoundsException(offset);
        }
        if (count < 0) {
            throw new StringIndexOutOfBoundsException(count);
        }
        // Note: offset or count might be near -1>>>1.
        if (offset > value.length - count) {
            throw new StringIndexOutOfBoundsException(offset + count);
        }

        this.value = new char[count];
        this.count = count;
        System.arraycopy(value, offset, this.value, 0, count);
    }

    /**
     * Construct a new <code>String</code> by converting the specified
     * subarray of bytes using the specified character encoding.  The length of
     * the new <code>String</code> is a function of the encoding, and hence may
     * not be equal to the length of the subarray.
     *
     * @param  bytes   The bytes to be converted into characters
     * @param  off     Index of the first byte to convert
     * @param  len     Number of bytes to convert
     * @param  enc     The name of a character encoding
     *
     * @exception  UnsupportedEncodingException
     *             If the named encoding is not supported
     * @since      JDK1.1
     */
    public String(byte bytes[], int off, int len, String enc)
        throws UnsupportedEncodingException
    {
        this(Helper.byteToCharArray(bytes, off, len, enc));
    }

    /**
     * Construct a new <code>String</code> by converting the specified array
     * of bytes using the specified character encoding.  The length of the new
     * <code>String</code> is a function of the encoding, and hence may not be
     * equal to the length of the byte array.
     *
     * @param  bytes   The bytes to be converted into characters
     * @param  enc     The name of a supported character encoding
     *
     * @exception  UnsupportedEncodingException
     *             If the named encoding is not supported
     * @since      JDK1.1
     */
    public String(byte bytes[], String enc)
        throws UnsupportedEncodingException
    {
        this(bytes, 0, bytes.length, enc);
    }

    /**
     * Construct a new <code>String</code> by converting the specified
     * subarray of bytes using the platform's default character encoding.  The
     * length of the new <code>String</code> is a function of the encoding, and
     * hence may not be equal to the length of the subarray.
     *
     * @param  bytes   The bytes to be converted into characters
     * @param  off     Index of the first byte to convert
     * @param  len     Number of bytes to convert
     * @since  JDK1.1
     */
    public String(byte bytes[], int off, int len) {
        this(Helper.byteToCharArray(bytes, off, len));
    }

    /**
     * Construct a new <code>String</code> by converting the specified array
     * of bytes using the platform's default character encoding.  The length of
     * the new <code>String</code> is a function of the encoding, and hence may
     * not be equal to the length of the byte array.
     *
     * @param  bytes   The bytes to be converted into characters
     * @since  JDK1.1
     */
    public String(byte bytes[]) {
        this(bytes, 0, bytes.length);
    }

    /**
     * Allocates a new string that contains the sequence of characters
     * currently contained in the string buffer argument. The contents of
     * the string buffer are copied; subsequent modification of the string
     * buffer does not affect the newly created string.
     *
     * @param   buffer   a <code>StringBuffer</code>.
     * @throws NullPointerException If <code>buffer</code> is
     * <code>null</code>.
     */
    public String (StringBuffer buffer) {
        synchronized(buffer) {
            buffer.setShared();
            this.value = buffer.getValue();
            this.offset = 0;
            this.count = buffer.length();
        }
    }

    // Private constructor which shares value array for speed.
    private String(int offset, int count, char value[]) {
        this.value = value;
        this.offset = offset;
        this.count = count;
    }

    /**
     * Returns the length of this string.
     * The length is equal to the number of 16-bit
     * Unicode characters in the string.
     *
     * @return  the length of the sequence of characters represented by this
     *          object.
     */
    public int length() {
        return count;
    }

    /**
     * Returns the character at the specified index. An index ranges
     * from <code>0</code> to <code>length() - 1</code>. The first character
     * of the sequence is at index <code>0</code>, the next at index
     * <code>1</code>, and so on, as for array indexing.
     *
     * @param      index   the index of the character.
     * @return     the character at the specified index of this string.
     *             The first character is at index <code>0</code>.
     * @exception  IndexOutOfBoundsException  if the <code>index</code>
     *             argument is negative or not less than the length of this
     *             string.
     */
    //public native char charAt(int index);

    public char charAt(int index) {
       if ((index < 0) || (index >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return value[index + offset];
    }


    /**
     * Copies characters from this string into the destination character
     * array.
     * <p>
     * The first character to be copied is at index <code>srcBegin</code>;
     * the last character to be copied is at index <code>srcEnd-1</code>
     * (thus the total number of characters to be copied is
     * <code>srcEnd-srcBegin</code>). The characters are copied into the
     * subarray of <code>dst</code> starting at index <code>dstBegin</code>
     * and ending at index:
     * <p><blockquote><pre>
     *     dstbegin + (srcEnd-srcBegin) - 1
     * </pre></blockquote>
     *
     * @param      srcBegin   index of the first character in the string
     *                        to copy.
     * @param      srcEnd     index after the last character in the string
     *                        to copy.
     * @param      dst        the destination array.
     * @param      dstBegin   the start offset in the destination array.
     * @exception IndexOutOfBoundsException If any of the following
     *            is true:
     *            <ul><li><code>srcBegin</code> is negative.
     *            <li><code>srcBegin</code> is greater than <code>srcEnd</code>
     *            <li><code>srcEnd</code> is greater than the length of this
     *                string
     *            <li><code>dstBegin</code> is negative
     *            <li><code>dstBegin+(srcEnd-srcBegin)</code> is larger than
     *                <code>dst.length</code></ul>
     * @exception NullPointerException if <code>dst</code> is <code>null</code>
     */
    public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin) {
        if (srcBegin < 0) {
            throw new StringIndexOutOfBoundsException(srcBegin);
        }
        if (srcEnd > count) {
            throw new StringIndexOutOfBoundsException(srcEnd);
        }
        if (srcBegin > srcEnd) {
            throw new StringIndexOutOfBoundsException(srcEnd - srcBegin);
        }
        System.arraycopy(value, offset + srcBegin, dst, dstBegin,
                         srcEnd - srcBegin);
    }

    /**
     * Convert this <code>String</code> into bytes according to the specified
     * character encoding, storing the result into a new byte array.
     *
     * @param  enc  A character-encoding name
     * @return      The resultant byte array
     *
     * @exception  UnsupportedEncodingException
     *             If the named encoding is not supported
     * @since      JDK1.1
     */
    public byte[] getBytes(String enc) throws UnsupportedEncodingException {
        return Helper.charToByteArray(value, offset, count, enc);
    }

    /**
     * Convert this <code>String</code> into bytes according to the platform's
     * default character encoding, storing the result into a new byte array.
     *
     * @return  the resultant byte array.
     * @since   JDK1.1
     */
    public byte[] getBytes() {
        return Helper.charToByteArray(value, offset, count);
    }

    /**
     * Compares this string to the specified object.
     * The result is <code>true</code> if and only if the argument is not
     * <code>null</code> and is a <code>String</code> object that represents
     * the same sequence of characters as this object.
     *
     * @param   anObject   the object to compare this <code>String</code>
     *                     against.
     * @return  <code>true</code> if the <code>String </code>are equal;
     *          <code>false</code> otherwise.
     * @see     java.lang.String#compareTo(java.lang.String)
     */
    //public native boolean equals(Object anObject);
    public boolean equals(Object anObject) {
        if (this == anObject) {
            return true;
        }
        if (anObject instanceof String) {
            String anotherString = (String)anObject;
            int n = count;
            if (n == anotherString.count) {
                char v1[] = value;
                char v2[] = anotherString.value;
                int i = offset;
                int j = anotherString.offset;
                while (n-- != 0) {
                    if (v1[i++] != v2[j++]) {
                        return false;
                    }
                }
                return true;
            }
        }
        return false;
    }


    /**
     * Compares two strings lexicographically.
     * The comparison is based on the Unicode value of each character in
     * the strings. The character sequence represented by this
     * <code>String</code> object is compared lexicographically to the
     * character sequence represented by the argument string. The result is
     * a negative integer if this <code>String</code> object
     * lexicographically precedes the argument string. The result is a
     * positive integer if this <code>String</code> object lexicographically
     * follows the argument string. The result is zero if the strings
     * are equal; <code>compareTo</code> returns <code>0</code> exactly when
     * the {@link #equals(Object)} method would return <code>true</code>.
     * <p>
     * This is the definition of lexicographic ordering. If two strings are
     * different, then either they have different characters at some index
     * that is a valid index for both strings, or their lengths are different,
     * or both. If they have different characters at one or more index
     * positions, let <i>k</i> be the smallest such index; then the string
     * whose character at position <i>k</i> has the smaller value, as
     * determined by using the < operator, lexicographically precedes the
     * other string. In this case, <code>compareTo</code> returns the
     * difference of the two character values at position <code>k</code> in
     * the two string -- that is, the value:
     * <blockquote><pre>
     * this.charAt(k)-anotherString.charAt(k)
     * </pre></blockquote>
     * If there is no index position at which they differ, then the shorter
     * string lexicographically precedes the longer string. In this case,
     * <code>compareTo</code> returns the difference of the lengths of the
     * strings -- that is, the value:
     * <blockquote><pre>
     * this.length()-anotherString.length()
     * </pre></blockquote>
     *
     * @param   anotherString   the <code>String</code> to be compared.
     * @return  the value <code>0</code> if the argument string is equal to
     *          this string; a value less than <code>0</code> if this string
     *          is lexicographically less than the string argument; and a
     *          value greater than <code>0</code> if this string is
     *          lexicographically greater than the string argument.
     * @exception java.lang.NullPointerException if <code>anotherString</code>
     *          is <code>null</code>.
     */
    public int compareTo(String anotherString) {
        int len1 = count;
        int len2 = anotherString.count;
        int n = Math.min(len1, len2);
        char v1[] = value;
        char v2[] = anotherString.value;
        int i = offset;
        int j = anotherString.offset;

        while (n-- != 0) {
            char c1 = v1[i++];
            char c2 = v2[j++];
            if (c1 != c2) {
                return c1 - c2;
            }
        }
        return len1 - len2;
    }

    /**
     * Tests if two string regions are equal.
     * <p>
     * A substring of this <tt>String</tt> object is compared to a substring
     * of the argument <tt>other</tt>. The result is <tt>true</tt> if these
     * substrings represent character sequences that are the same, ignoring
     * case if and only if <tt>ignoreCase</tt> is true. The substring of
     * this <tt>String</tt> object to be compared begins at index
     * <tt>toffset</tt> and has length <tt>len</tt>. The substring of
     * <tt>other</tt> to be compared begins at index <tt>ooffset</tt> and
     * has length <tt>len</tt>. The result is <tt>false</tt> if and only if
     * at least one of the following is true:
     * <ul><li><tt>toffset</tt> is negative.
     * <li><tt>ooffset</tt> is negative.
     * <li><tt>toffset+len</tt> is greater than the length of this
     * <tt>String</tt> object.
     * <li><tt>ooffset+len</tt> is greater than the length of the other
     * argument.
     * <li>There is some nonnegative integer <i>k</i> less than <tt>len</tt>
     * such that:
     * <blockquote><pre>
     * this.charAt(toffset+k) != other.charAt(ooffset+k)
     * </pre></blockquote>
     * <li><tt>ignoreCase</tt> is <tt>true</tt> and there is some nonnegative
     * integer <i>k</i> less than <tt>len</tt> such that:
     * <blockquote><pre>
     * Character.toLowerCase(this.charAt(toffset+k)) !=
               Character.toLowerCase(other.charAt(ooffset+k))
     * </pre></blockquote>
     * and:
     * <blockquote><pre>
     * Character.toUpperCase(this.charAt(toffset+k)) !=
     *         Character.toUpperCase(other.charAt(ooffset+k))
     * </pre></blockquote>
     * </ul>
     *
     * @param   ignoreCase   if <code>true</code>, ignore case when comparing
     *                       characters.
     * @param   toffset      the starting offset of the subregion in this
     *                       string.
     * @param   other        the string argument.
     * @param   ooffset      the starting offset of the subregion in the string
     *                       argument.
     * @param   len          the number of characters to compare.
     * @return  <code>true</code> if the specified subregion of this string
     *          matches the specified subregion of the string argument;
     *          <code>false</code> otherwise. Whether the matching is exact
     *          or case insensitive depends on the <code>ignoreCase</code>
     *          argument.
     */
    public boolean regionMatches(boolean ignoreCase,
                                         int toffset,
                                       String other, int ooffset, int len) {
        char ta[] = value;
        int to = offset + toffset;
        int tlim = offset + count;
        char pa[] = other.value;
        int po = other.offset + ooffset;

        // Note: toffset, ooffset, or len might be near -1>>>1.
        if ((ooffset < 0) || (toffset < 0) || (toffset > (long)count - len) ||
            (ooffset > (long)other.count - len)) {
            return false;
        }
        while (len-- > 0) {
            char c1 = ta[to++];
            char c2 = pa[po++];
            if (c1 == c2)
                continue;
            if (ignoreCase) {
                // If characters don't match but case may be ignored,
                // try converting both characters to uppercase.
                // If the results match, then the comparison scan should
                // continue.
                char u1 = Character.toUpperCase(c1);
                char u2 = Character.toUpperCase(c2);
                if (u1 == u2)
                    continue;
                // Unfortunately, conversion to uppercase does not work properly
                // for the Georgian alphabet, which has strange rules about case
                // conversion.  So we need to make one last check before
                // exiting.
                if (Character.toLowerCase(u1) == Character.toLowerCase(u2))
                    continue;
            }
            return false;
        }
        return true;
    }

    /**
     * Tests if this string starts with the specified prefix beginning
     * a specified index.
     *
     * @param   prefix    the prefix.
     * @param   toffset   where to begin looking in the string.
     * @return  <code>true</code> if the character sequence represented by the
     *          argument is a prefix of the substring of this object starting
     *          at index <code>toffset</code>; <code>false</code> otherwise.
     *          The result is <code>false</code> if <code>toffset</code> is
     *          negative or greater than the length of this
     *          <code>String</code> object; otherwise the result is the same
     *          as the result of the expression
     *          <pre>
     *          this.subString(toffset).startsWith(prefix)
     *          </pre>
     * @exception java.lang.NullPointerException if <code>prefix</code> is
     *          <code>null</code>.
     */
    public boolean startsWith(String prefix, int toffset) {
        char ta[] = value;
        int to = offset + toffset;
        int tlim = offset + count;
        char pa[] = prefix.value;
        int po = prefix.offset;
        int pc = prefix.count;
        // Note: toffset might be near -1>>>1.
        if ((toffset < 0) || (toffset > count - pc)) {
            return false;
        }
        while (--pc >= 0) {
            if (ta[to++] != pa[po++]) {
                return false;
            }
        }
        return true;
    }

    /**
     * Tests if this string starts with the specified prefix.
     *
     * @param   prefix   the prefix.
     * @return  <code>true</code> if the character sequence represented by the
     *          argument is a prefix of the character sequence represented by
     *          this string; <code>false</code> otherwise.
     *          Note also that <code>true</code> will be returned if the
     *          argument is an empty string or is equal to this
     *          <code>String</code> object as determined by the
     *          {@link #equals(Object)} method.
     * @exception java.lang.NullPointerException if <code>prefix</code> is
     *          <code>null</code>.
     * @since   JDK1. 0
     */
    public boolean startsWith(String prefix) {
        return startsWith(prefix, 0);
    }

    /**
     * Tests if this string ends with the specified suffix.
     *
     * @param   suffix   the suffix.
     * @return  <code>true</code> if the character sequence represented by the
     *          argument is a suffix of the character sequence represented by
     *          this object; <code>false</code> otherwise. Note that the
     *          result will be <code>true</code> if the argument is the
     *          empty string or is equal to this <code>String</code> object
     *          as determined by the {@link #equals(Object)} method.
     * @exception java.lang.NullPointerException if <code>suffix</code> is
     *          <code>null</code>.
     */
    public boolean endsWith(String suffix) {
        return startsWith(suffix, count - suffix.count);
    }

    /**
     * Returns a hashcode for this string. The hashcode for a
     * <code>String</code> object is computed as
     * <blockquote><pre>
     * s[0]*31^(n-1) + s[1]*31^(n-2) + ... + s[n-1]
     * </pre></blockquote>
     * using <code>int</code> arithmetic, where <code>s[i]</code> is the
     * <i>i</i>th character of the string, <code>n</code> is the length of
     * the string, and <code>^</code> indicates exponentiation.
     * (The hash value of the empty string is zero.)
     *
     * @return  a hash code value for this object.
     */
    public int hashCode() {
        int h = 0;
        int off = offset;
        char val[] = value;
        int len = count;

        for (int i = 0; i < len; i++) {
            h = 31*h + val[off++];
        }
        return h;
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified character. If a character with value <code>ch</code> occurs
     * in the character sequence represented by this <code>String</code>
     * object, then the index of the first such occurrence is returned --
     * that is, the smallest value <i>k</i> such that:
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch
     * </pre></blockquote>
     * is <code>true</code>. If no such character occurs in this string,
     * then <code>-1</code> is returned.
     *
     * @param   ch   a character.
     * @return  the index of the first occurrence of the character in the
     *          character sequence represented by this object, or
     *          <code>-1</code> if the character does not occur.
     */

    //public native int indexOf(int ch);

    public int indexOf(int ch) {
        return indexOf(ch, 0);
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified character, starting the search at the specified index.
     * <p>
     * If a character with value <code>ch</code> occurs in the character
     * sequence represented by this <code>String</code> object at an index
     * no smaller than <code>fromIndex</code>, then the index of the first
     * such occurrence is returned--that is, the smallest value <i>k</i>
     * such that:
     * <blockquote><pre>
     * (this.charAt(<i>k</i>) == ch) && (<i>k</i> >= fromIndex)
     * </pre></blockquote>
     * is true. If no such character occurs in this string at or after
     * position <code>fromIndex</code>, then <code>-1</code> is returned.
     * <p>
     * There is no restriction on the value of <code>fromIndex</code>. If it
     * is negative, it has the same effect as if it were zero: this entire
     * string may be searched. If it is greater than the length of this
     * string, it has the same effect as if it were equal to the length of
     * this string: <code>-1</code> is returned.
     *
     * @param   ch          a character.
     * @param   fromIndex   the index to start the search from.
     * @return  the index of the first occurrence of the character in the
     *          character sequence represented by this object that is greater
     *          than or equal to <code>fromIndex</code>, or <code>-1</code>
     *          if the character does not occur.
     */

    //public native int indexOf(int ch, int fromIndex);

    public int indexOf(int ch, int fromIndex) {
        int max = offset + count;
        char v[] = value;

        if (fromIndex < 0) {
            fromIndex = 0;
        } else if (fromIndex >= count) {
            // Note: fromIndex might be near -1>>>1.
            return -1;
        }
        for (int i = offset + fromIndex ; i < max ; i++) {
            if (v[i] == ch) {
                 return i - offset;
            }
        }
        return -1;
    }

    /**
     * Returns the index within this string of the last occurrence of the
     * specified character. That is, the index returned is the largest
     * value <i>k</i> such that:
     * <blockquote><pre>
     * this.charAt(<i>k</i>) == ch
     * </pre></blockquote>
     * is true.
     * The String is searched backwards starting at the last character.
     *
     * @param   ch   a character.
     * @return  the index of the last occurrence of the character in the
     *          character sequence represented by this object, or
     *          <code>-1</code> if the character does not occur.
     */
    public int lastIndexOf(int ch) {
        return lastIndexOf(ch, count - 1);
    }

    /**
     * Returns the index within this string of the last occurrence of the
     * specified character, searching backward starting at the specified
     * index. That is, the index returned is the largest value <i>k</i>
     * such that:
     * <blockquote><pre>
     * this.charAt(k) == ch) && (k <= fromIndex)
     * </pre></blockquote>
     * is true.
     *
     * @param   ch          a character.
     * @param   fromIndex   the index to start the search from. There is no
     *          restriction on the value of <code>fromIndex</code>. If it is
     *          greater than or equal to the length of this string, it has
     *          the same effect as if it were equal to one less than the
     *          length of this string: this entire string may be searched.
     *          If it is negative, it has the same effect as if it were -1:
     *          -1 is returned.
     * @return  the index of the last occurrence of the character in the
     *          character sequence represented by this object that is less
     *          than or equal to <code>fromIndex</code>, or <code>-1</code>
     *          if the character does not occur before that point.
     */
    public int lastIndexOf(int ch, int fromIndex) {
        int min = offset;
        char v[] = value;

        for (int i = offset + ((fromIndex >= count) ? count - 1 : fromIndex) ; i >= min ; i--) {
            if (v[i] == ch) {
                return i - offset;
            }
        }
        return -1;
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring. The integer returned is the smallest value
     * <i>k</i> such that:
     * <blockquote><pre>
     * this.startsWith(str, <i>k</i>)
     * </pre></blockquote>
     * is <code>true</code>.
     *
     * @param   str   any string.
     * @return  if the string argument occurs as a substring within this
     *          object, then the index of the first character of the first
     *          such substring is returned; if it does not occur as a
     *          substring, <code>-1</code> is returned.
     * @exception java.lang.NullPointerException if <code>str</code> is
     *          <code>null</code>.
     */
    public int indexOf(String str) {
        return indexOf(str, 0);
    }

    /**
     * Returns the index within this string of the first occurrence of the
     * specified substring, starting at the specified index. The integer
     * returned is the smallest value <i>k</i> such that:
     * <blockquote><pre>
     * this.startsWith(str, <i>k</i>) && (<i>k</i> >= fromIndex)
     * </pre></blockquote>
     * is <code>true</code>.
     * <p>
     * There is no restriction on the value of <code>fromIndex</code>. If
     * it is negative, it has the same effect as if it were zero: this entire
     * string may be searched. If it is greater than the length of this
     * string, it has the same effect as if it were equal to the length of
     * this string: <code>-1</code> is returned.
     *
     * @param   str         the substring to search for.
     * @param   fromIndex   the index to start the search from.
     * @return  If the string argument occurs as a substring within this
     *          object at a starting index no smaller than
     *          <code>fromIndex</code>, then the index of the first character
     *          of the first such substring is returned. If it does not occur
     *          as a substring starting at <code>fromIndex</code> or beyond,
     *          <code>-1</code> is returned.
     * @exception java.lang.NullPointerException if <code>str</code> is
     *          <code>null</code>
     */
    public int indexOf(String str, int fromIndex) {
        char v1[] = value;
        char v2[] = str.value;
        int max = offset + (count - str.count);
        if (fromIndex >= count) {
            if (count == 0 && fromIndex == 0 && str.count == 0) {
                /* There is an empty string at index 0 in an empty string. */
                return 0;
            }
            /* Note: fromIndex might be near -1>>>1 */
            return -1;
        }
        if (fromIndex < 0) {
            fromIndex = 0;
        }
        if (str.count == 0) {
            return fromIndex;
        }

        int strOffset = str.offset;
        char first  = v2[strOffset];
        int i = offset + fromIndex;

    startSearchForFirstChar:
        while (true) {

            /* Look for first character. */
            while (i <= max && v1[i] != first) {
                i++;
            }
            if (i > max) {
                return -1;
            }

            /* Found first character, now look at the rest of v2 */
            int j = i + 1;
            int end = j + str.count - 1;
            int k = strOffset + 1;
            while (j < end) {
                if (v1[j++] != v2[k++]) {
                    i++;
                    /* Look for str's first char again. */
                    continue startSearchForFirstChar;
                }
            }
            return i - offset;  /* Found whole string. */
        }
    }

    /**
     * Returns a new string that is a substring of this string. The
     * substring begins with the character at the specified index and
     * extends to the end of this string. <p>
     * Examples:
     * <blockquote><pre>
     * "unhappy".substring(2) returns "happy"
     * "Harbison".substring(3) returns "bison"
     * "emptiness".substring(9) returns "" (an empty string)
     * </pre></blockquote>
     *
     * @param      beginIndex   the beginning index, inclusive.
     * @return     the specified substring.
     * @exception  IndexOutOfBoundsException  if
     *             <code>beginIndex</code> is negative or larger than the
     *             length of this <code>String</code> object.
     */
    public String substring(int beginIndex) {
        return substring(beginIndex, count);
    }

    /**
     * Returns a new string that is a substring of this string. The
     * substring begins at the specified <code>beginIndex</code> and
     * extends to the character at index <code>endIndex - 1</code>.
     * Thus the length of the substring is <code>endIndex-beginIndex</code>.
     * <p>
     * Examples:
     * <blockquote><pre>
     * "hamburger".substring(4, 8) returns "urge"
     * "smiles".substring(1, 5) returns "mile"
     * </pre></blockquote>
     *
     * @param      beginIndex   the beginning index, inclusive.
     * @param      endIndex     the ending index, exclusive.
     * @return     the specified substring.
     * @exception  IndexOutOfBoundsException  if the
     *             <code>beginIndex</code> is negative, or
     *             <code>endIndex</code> is larger than the length of
     *             this <code>String</code> object, or
     *             <code>beginIndex</code> is larger than
     *             <code>endIndex</code>.
     */
    public String substring(int beginIndex, int endIndex) {
        if (beginIndex < 0) {
            throw new StringIndexOutOfBoundsException(beginIndex);
        }
        if (endIndex > count) {
            throw new StringIndexOutOfBoundsException(endIndex);
        }
        if (beginIndex > endIndex) {
            throw new StringIndexOutOfBoundsException(endIndex - beginIndex);
        }
        return ((beginIndex == 0) && (endIndex == count)) ? this :
            new String(offset + beginIndex, endIndex - beginIndex, value);
    }

    /**
     * Concatenates the specified string to the end of this string.
     * <p>
     * If the length of the argument string is <code>0</code>, then this
     * <code>String</code> object is returned. Otherwise, a new
     * <code>String</code> object is created, representing a character
     * sequence that is the concatenation of the character sequence
     * represented by this <code>String</code> object and the character
     * sequence represented by the argument string.<p>
     * Examples:
     * <blockquote><pre>
     * "cares".concat("s") returns "caress"
     * "to".concat("get").concat("her") returns "together"
     * </pre></blockquote>
     *
     * @param   str   the <code>String</code> that is concatenated to the end
     *                of this <code>String</code>.
     * @return  a string that represents the concatenation of this object's
     *          characters followed by the string argument's characters.
     * @exception java.lang.NullPointerException if <code>str</code> is
     *          <code>null</code>.
     */
    public String concat(String str) {
        int otherLen = str.length();
        if (otherLen == 0) {
            return this;
        }
        char buf[] = new char[count + otherLen];
        getChars(0, count, buf, 0);
        str.getChars(0, otherLen, buf, count);
        return new String(0, count + otherLen, buf);
    }

    /**
     * Returns a new string resulting from replacing all occurrences of
     * <code>oldChar</code> in this string with <code>newChar</code>.
     * <p>
     * If the character <code>oldChar</code> does not occur in the
     * character sequence represented by this <code>String</code> object,
     * then a reference to this <code>String</code> object is returned.
     * Otherwise, a new <code>String</code> object is created that
     * represents a character sequence identical to the character sequence
     * represented by this <code>String</code> object, except that every
     * occurrence of <code>oldChar</code> is replaced by an occurrence
     * of <code>newChar</code>.
     * <p>
     * Examples:
     * <blockquote><pre>
     * "mesquite in your cellar".replace('e', 'o')
     *         returns "mosquito in your collar"
     * "the war of baronets".replace('r', 'y')
     *         returns "the way of bayonets"
     * "sparring with a purple porpoise".replace('p', 't')
     *         returns "starring with a turtle tortoise"
     * "JonL".replace('q', 'x') returns "JonL" (no change)
     * </pre></blockquote>
     *
     * @param   oldChar   the old character.
     * @param   newChar   the new character.
     * @return  a string derived from this string by replacing every
     *          occurrence of <code>oldChar</code> with <code>newChar</code>.
     */
    public String replace(char oldChar, char newChar) {
        if (oldChar != newChar) {
            int len = count;
            int i = -1;
            char[] val = value; /* avoid getfield opcode */
            int off = offset;   /* avoid getfield opcode */

            while (++i < len) {
                if (val[off + i] == oldChar) {
                    break;
                }
            }
            if (i < len) {
                char buf[] = new char[len];
                for (int j = 0 ; j < i ; j++) {
                    buf[j] = val[off+j];
                }
                while (i < len) {
                    char c = val[off + i];
                    buf[i] = (c == oldChar) ? newChar : c;
                    i++;
                }
                return new String(0, len, buf);
            }
        }
        return this;
    }

    /**
     * Converts all of the characters in this String to lower case.
     *
     * @return the String, converted to lowercase.
     * @see Character#toLowerCase
     * @see String#toUpperCase
     */
    public String toLowerCase() {
        char buf[] = new char[count];
        for(int i = 0 ; i < count ; i++) {
            buf[i] = Character.toLowerCase(value[offset+i]);
        }
        return new String(0, count, buf);
    }

    /**
     * Converts all of the characters in this String to upper case.
     *
     * @return the String, converted to uppercase.
     * @see Character#toLowerCase
     * @see String#toUpperCase
     */
    public String toUpperCase() {
        char buf[] = new char[count];
        for(int i = 0 ; i < count ; i++) {
            buf[i] = Character.toUpperCase(value[offset+i]);
        }
        return new String(0, count, buf);
    }

    /**
     * Removes white space from both ends of this string.
     * <p>
     * If this <code>String</code> object represents an empty character
     * sequence, or the first and last characters of character sequence
     * represented by this <code>String</code> object both have codes
     * greater than <code>'&#92;u0020'</code> (the space character), then a
     * reference to this <code>String</code> object is returned.
     * <p>
     * Otherwise, if there is no character with a code greater than
     * <code>'&#92;u0020'</code> in the string, then a new
     * <code>String</code> object representing an empty string is created
     * and returned.
     * <p>
     * Otherwise, let <i>k</i> be the index of the first character in the
     * string whose code is greater than <code>'&#92;u0020'</code>, and let
     * <i>m</i> be the index of the last character in the string whose code
     * is greater than <code>'&#92;u0020'</code>. A new <code>String</code>
     * object is created, representing the substring of this string that
     * begins with the character at index <i>k</i> and ends with the
     * character at index <i>m</i>-that is, the result of
     * <code>this.substring(<i>k</i>,&nbsp;<i>m</i>+1)</code>.
     * <p>
     * This method may be used to trim whitespace from the beginning and end
     * of a string; in fact, it trims all ASCII control characters as well.
     *
     * @return  this string, with white space removed from the front and end.
     */
    public String trim() {
        int len = count;
        int st = 0;
        int off = offset;      /* avoid getfield opcode */
        char[] val = value;    /* avoid getfield opcode */

        while ((st < len) && (val[off + st] <= ' ')) {
            st++;
        }
        while ((st < len) && (val[off + len - 1] <= ' ')) {
            len--;
        }
        return ((st > 0) || (len < count)) ? substring(st, len) : this;
    }

    /**
     * This object (which is already a string!) is itself returned.
     *
     * @return  the string itself.
     */
    public String toString() {
        return this;
    }

    /**
     * Converts this string to a new character array.
     *
     * @return  a newly allocated character array whose length is the length
     *          of this string and whose contents are initialized to contain
     *          the character sequence represented by this string.
     */
    public char[] toCharArray() {
        char result[] = new char[count];
        getChars(0, count, result, 0);
        return result;
    }

    /**
     * Returns the string representation of the <code>Object</code> argument.
     *
     * @param   obj   an <code>Object</code>.
     * @return  if the argument is <code>null</code>, then a string equal to
     *          <code>"null"</code>; otherwise, the value of
     *          <code>obj.toString()</code> is returned.
     * @see     java.lang.Object#toString()
     */
    public static String valueOf(Object obj) {
        return (obj == null) ? "null" : obj.toString();
    }

    /**
     * Returns the string representation of the <code>char</code> array
     * argument. The contents of the character array are copied; subsequent
     * modification of the character array does not affect the newly
     * created string.
     *
     * @param   data   a <code>char</code> array.
     * @return  a newly allocated string representing the same sequence of
     *          characters contained in the character array argument.
     */
    public static String valueOf(char data[]) {
        return new String(data);
    }

    /**
     * Returns the string representation of a specific subarray of the
     * <code>char</code> array argument.
     * <p>
     * The <code>offset</code> argument is the index of the first
     * character of the subarray. The <code>count</code> argument
     * specifies the length of the subarray. The contents of the subarray
     * are copied; subsequent modification of the character array does not
     * affect the newly created string.
     *
     * @param   data     the character array.
     * @param   offset   the initial offset into the value of the
     *                  <code>String</code>.
     * @param   count    the length of the value of the <code>String</code>.
     * @return  a newly allocated string representing the sequence of
     *          characters contained in the subarray of the character array
     *          argument.
     * @exception NullPointerException if <code>data</code> is
     *          <code>null</code>.
     * @exception IndexOutOfBoundsException if <code>offset</code> is
     *          negative, or <code>count</code> is negative, or
     *          <code>offset+count</code> is larger than
     *          <code>data.length</code>.
     */
    public static String valueOf(char data[], int offset, int count) {
        return new String(data, offset, count);
    }

    /**
     * Returns the string representation of the <code>boolean</code> argument.
     *
     * @param   b   a <code>boolean</code>.
     * @return  if the argument is <code>true</code>, a string equal to
     *          <code>"true"</code> is returned; otherwise, a string equal to
     *          <code>"false"</code> is returned.
     */
    public static String valueOf(boolean b) {
        return b ? "true" : "false";
    }

    /**
     * Returns the string representation of the <code>char</code>
     * argument.
     *
     * @param   c   a <code>char</code>.
     * @return  a newly allocated string of length <code>1</code> containing
     *          as its single character the argument <code>c</code>.
     */
    public static String valueOf(char c) {
        char data[] = {c};
        return new String(0, 1, data);
    }

    /**
     * Returns the string representation of the <code>int</code> argument.
     * <p>
     * The representation is exactly the one returned by the
     * <code>Integer.toString</code> method of one argument.
     *
     * @param   i   an <code>int</code>.
     * @return  a newly allocated string containing a string representation of
     *          the <code>int</code> argument.
     * @see     java.lang.Integer#toString(int, int)
     */
    public static String valueOf(int i) {
        return Integer.toString(i, 10);
    }

    /**
     * Returns the string representation of the <code>long</code> argument.
     * <p>
     * The representation is exactly the one returned by the
     * <code>Long.toString</code> method of one argument.
     *
     * @param   l   a <code>long</code>.
     * @return  a newly allocated string containing a string representation of
     *          the <code>long</code> argument.
     * @see     java.lang.Long#toString(long)
     */
    public static String valueOf(long l) {
        return Long.toString(l, 10);
    }


    /**
     * Returns a String object that represents the value of the specified float.
     * @param f the float
     */
    public static String valueOf(float f) {
        return Float.toString(f);
    }

    /**
     * Returns a String object that represents the value of the specified double.
     * @param d the double
     */
    public static String valueOf(double d) {
        return Double.toString(d);
    }

}


