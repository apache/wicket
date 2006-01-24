/*
 * @(#)StringBuffer.java 1.78 03/05/16
 * 
 * Copyright 2003 Sun Microsystems, Inc. All rights reserved. SUN
 * PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */

package wicket.util.string;

import java.io.IOException;

/**
 * A string buffer implements a mutable sequence of characters. A string buffer
 * is like a {@link String}, but can be modified. At any point in time it
 * contains some particular sequence of characters, but the length and content
 * of the sequence can be changed through certain method calls.
 * <p>
 * String buffers are safe for use by multiple threads. The methods are
 * synchronized where necessary so that all the operations on any particular
 * instance behave as if they occur in some serial order that is consistent with
 * the order of the method calls made by each of the individual threads
 * involved.
 * <p>
 * String buffers are used by the compiler to implement the binary string
 * concatenation operator <code>+</code>. For example, the code:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * x = &quot;a&quot; + 4 + &quot;c&quot;
 * </pre>
 * 
 * </blockquote>
 * <p>
 * is compiled to the equivalent of:
 * <p>
 * <blockquote>
 * 
 * <pre>
 * x = new StringBuffer().append(&quot;a&quot;).append(4).append(&quot;c&quot;).toString()
 * </pre>
 * 
 * </blockquote> which creates a new string buffer (initially empty), appends
 * the string representation of each operand to the string buffer in turn, and
 * then converts the contents of the string buffer to a string. Overall, this
 * avoids creating many temporary strings.
 * <p>
 * The principal operations on a <code>StringBuffer</code> are the
 * <code>append</code> and <code>insert</code> methods, which are overloaded
 * so as to accept data of any type. Each effectively converts a given datum to
 * a string and then appends or inserts the characters of that string to the
 * string buffer. The <code>append</code> method always adds these characters
 * at the end of the buffer; the <code>insert</code> method adds the
 * characters at a specified point.
 * <p>
 * For example, if <code>z</code> refers to a string buffer object whose
 * current contents are "<code>start</code>", then the method call
 * <code>z.append("le")</code> would cause the string buffer to contain "<code>startle</code>",
 * whereas <code>z.insert(4, "le")</code> would alter the string buffer to
 * contain "<code>starlet</code>".
 * <p>
 * In general, if sb refers to an instance of a <code>StringBuffer</code>,
 * then <code>sb.append(x)</code> has the same effect as
 * <code>sb.insert(sb.length(),&nbsp;x)</code>.
 * <p>
 * Every string buffer has a capacity. As long as the length of the character
 * sequence contained in the string buffer does not exceed the capacity, it is
 * not necessary to allocate a new internal buffer array. If the internal buffer
 * overflows, it is automatically made larger.
 * 
 * @author Arthur van Hoff
 * @version 1.78, 05/16/03
 * @see java.io.ByteArrayOutputStream
 * @see java.lang.String
 * @since JDK1.0
 */

public final class StringBuffer implements java.io.Serializable, CharSequence
{
	/**
	 * The value is used for character storage.
	 * 
	 * @serial
	 */
	private char value[];

	/**
	 * The count is the number of characters in the buffer.
	 * 
	 * @serial
	 */
	private int count;

	/** use serialVersionUID from JDK 1.0.2 for interoperability */
	static final long serialVersionUID = 1L;

	/**
	 * Constructs a string buffer with no characters in it and an initial
	 * capacity of 16 characters.
	 */
	public StringBuffer()
	{
		this(16);
	}

	/**
	 * Constructs a string buffer with no characters in it and an initial
	 * capacity specified by the <code>length</code> argument.
	 * 
	 * @param length
	 *            the initial capacity.
	 * @exception NegativeArraySizeException
	 *                if the <code>length</code> argument is less than
	 *                <code>0</code>.
	 */
	public StringBuffer(int length)
	{
		value = new char[length];
	}

	/**
	 * Constructs a string buffer so that it represents the same sequence of
	 * characters as the string argument; in other words, the initial contents
	 * of the string buffer is a copy of the argument string. The initial
	 * capacity of the string buffer is <code>16</code> plus the length of the
	 * string argument.
	 * 
	 * @param str
	 *            the initial contents of the buffer.
	 * @exception NullPointerException
	 *                if <code>str</code> is <code>null</code>
	 */
	public StringBuffer(String str)
	{
		this(str.length() + 16);
		append(str);
	}

	/**
	 * Returns the length (character count) of this string buffer.
	 * 
	 * @return the length of the sequence of characters currently represented by
	 *         this string buffer.
	 */
	public int length()
	{
		return count;
	}

	/**
	 * Returns the current capacity of the String buffer. The capacity is the
	 * amount of storage available for newly inserted characters; beyond which
	 * an allocation will occur.
	 * 
	 * @return the current capacity of this string buffer.
	 */
	public int capacity()
	{
		return value.length;
	}

	/**
	 * Ensures that the capacity of the buffer is at least equal to the
	 * specified minimum. If the current capacity of this string buffer is less
	 * than the argument, then a new internal buffer is allocated with greater
	 * capacity. The new capacity is the larger of:
	 * <ul>
	 * <li>The <code>minimumCapacity</code> argument.
	 * <li>Twice the old capacity, plus <code>2</code>.
	 * </ul>
	 * If the <code>minimumCapacity</code> argument is nonpositive, this
	 * method takes no action and simply returns.
	 * 
	 * @param minimumCapacity
	 *            the minimum desired capacity.
	 */
	public void ensureCapacity(int minimumCapacity)
	{
		if (minimumCapacity > value.length)
		{
			expandCapacity(minimumCapacity);
		}
	}

	/**
	 * This implements the expansion semantics of ensureCapacity but is
	 * unsynchronized for use internally by methods which are already
	 * synchronized.
	 * 
	 * @param minimumCapacity
	 * 
	 * @see java.lang.StringBuffer#ensureCapacity(int)
	 */
	private void expandCapacity(int minimumCapacity)
	{
		int newCapacity = (value.length + 1) * 2;
		if (newCapacity < 0)
		{
			newCapacity = Integer.MAX_VALUE;
		}
		else if (minimumCapacity > newCapacity)
		{
			newCapacity = minimumCapacity;
		}

		char newValue[] = new char[newCapacity];
		System.arraycopy(value, 0, newValue, 0, count);
		value = newValue;
	}

	/**
	 * Sets the length of this String buffer. This string buffer is altered to
	 * represent a new character sequence whose length is specified by the
	 * argument. For every nonnegative index <i>k</i> less than
	 * <code>newLength</code>, the character at index <i>k</i> in the new
	 * character sequence is the same as the character at index <i>k</i> in the
	 * old sequence if <i>k</i> is less than the length of the old character
	 * sequence; otherwise, it is the null character <code>'&#92;u0000'</code>.
	 * 
	 * In other words, if the <code>newLength</code> argument is less than the
	 * current length of the string buffer, the string buffer is truncated to
	 * contain exactly the number of characters given by the
	 * <code>newLength</code> argument.
	 * <p>
	 * If the <code>newLength</code> argument is greater than or equal to the
	 * current length, sufficient null characters (<code>'&#92;u0000'</code>)
	 * are appended to the string buffer so that length becomes the
	 * <code>newLength</code> argument.
	 * <p>
	 * The <code>newLength</code> argument must be greater than or equal to
	 * <code>0</code>.
	 * 
	 * @param newLength
	 *            the new length of the buffer.
	 * @exception IndexOutOfBoundsException
	 *                if the <code>newLength</code> argument is negative.
	 * @see java.lang.StringBuffer#length()
	 */
	public void setLength(int newLength)
	{
		if (newLength < 0)
		{
			throw new StringIndexOutOfBoundsException(newLength);
		}

		if (newLength > value.length)
		{
			expandCapacity(newLength);
		}

		if (count < newLength)
		{
			for (; count < newLength; count++)
			{
				value[count] = '\0';
			}
		}
		else
		{
			count = newLength;
		}
	}

	/**
	 * The specified character of the sequence currently represented by the
	 * string buffer, as indicated by the <code>index</code> argument, is
	 * returned. The first character of a string buffer is at index
	 * <code>0</code>, the next at index <code>1</code>, and so on, for
	 * array indexing.
	 * <p>
	 * The index argument must be greater than or equal to <code>0</code>,
	 * and less than the length of this string buffer.
	 * 
	 * @param index
	 *            the index of the desired character.
	 * @return the character at the specified index of this string buffer.
	 * @exception IndexOutOfBoundsException
	 *                if <code>index</code> is negative or greater than or
	 *                equal to <code>length()</code>.
	 * @see java.lang.StringBuffer#length()
	 */
	public char charAt(int index)
	{
		if ((index < 0) || (index >= count))
		{
			throw new StringIndexOutOfBoundsException(index);
		}
		return value[index];
	}

	/**
	 * Characters are copied from this string buffer into the destination
	 * character array <code>dst</code>. The first character to be copied is
	 * at index <code>srcBegin</code>; the last character to be copied is at
	 * index <code>srcEnd-1</code>. The total number of characters to be
	 * copied is <code>srcEnd-srcBegin</code>. The characters are copied into
	 * the subarray of <code>dst</code> starting at index
	 * <code>dstBegin</code> and ending at index:
	 * <p>
	 * <blockquote>
	 * 
	 * <pre>
	 * dstbegin + (srcEnd - srcBegin) - 1
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * @param srcBegin
	 *            start copying at this offset in the string buffer.
	 * @param srcEnd
	 *            stop copying at this offset in the string buffer.
	 * @param dst
	 *            the array to copy the data into.
	 * @param dstBegin
	 *            offset into <code>dst</code>.
	 * @exception NullPointerException
	 *                if <code>dst</code> is <code>null</code>.
	 * @exception IndexOutOfBoundsException
	 *                if any of the following is true:
	 *                <ul>
	 *                <li><code>srcBegin</code> is negative
	 *                <li><code>dstBegin</code> is negative
	 *                <li>the <code>srcBegin</code> argument is greater than
	 *                the <code>srcEnd</code> argument.
	 *                <li><code>srcEnd</code> is greater than
	 *                <code>this.length()</code>, the current length of this
	 *                string buffer.
	 *                <li><code>dstBegin+srcEnd-srcBegin</code> is greater
	 *                than <code>dst.length</code>
	 *                </ul>
	 */
	public void getChars(int srcBegin, int srcEnd, char dst[], int dstBegin)
	{
		if (srcBegin < 0)
		{
			throw new StringIndexOutOfBoundsException(srcBegin);
		}
		if ((srcEnd < 0) || (srcEnd > count))
		{
			throw new StringIndexOutOfBoundsException(srcEnd);
		}
		if (srcBegin > srcEnd)
		{
			throw new StringIndexOutOfBoundsException("srcBegin > srcEnd");
		}
		System.arraycopy(value, srcBegin, dst, dstBegin, srcEnd - srcBegin);
	}

	/**
	 * The character at the specified index of this string buffer is set to
	 * <code>ch</code>. The string buffer is altered to represent a new
	 * character sequence that is identical to the old character sequence,
	 * except that it contains the character <code>ch</code> at position
	 * <code>index</code>.
	 * <p>
	 * The index argument must be greater than or equal to <code>0</code>,
	 * and less than the length of this string buffer.
	 * 
	 * @param index
	 *            the index of the character to modify.
	 * @param ch
	 *            the new character.
	 * @exception IndexOutOfBoundsException
	 *                if <code>index</code> is negative or greater than or
	 *                equal to <code>length()</code>.
	 * @see java.lang.StringBuffer#length()
	 */
	public void setCharAt(int index, char ch)
	{
		if ((index < 0) || (index >= count))
		{
			throw new StringIndexOutOfBoundsException(index);
		}
		value[index] = ch;
	}

	/**
	 * Appends the string representation of the <code>Object</code> argument
	 * to this string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then appended to this string buffer.
	 * 
	 * @param obj
	 *            an <code>Object</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @see java.lang.String#valueOf(java.lang.Object)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public StringBuffer append(Object obj)
	{
		return append(String.valueOf(obj));
	}

	/**
	 * Appends the string to this string buffer.
	 * <p>
	 * The characters of the <code>String</code> argument are appended, in
	 * order, to the contents of this string buffer, increasing the length of
	 * this string buffer by the length of the argument. If <code>str</code>
	 * is <code>null</code>, then the four characters <code>"null"</code>
	 * are appended to this string buffer.
	 * <p>
	 * Let <i>n</i> be the length of the old character sequence, the one
	 * contained in the string buffer just prior to execution of the
	 * <code>append</code> method. Then the character at index <i>k</i> in
	 * the new character sequence is equal to the character at index <i>k</i>
	 * in the old character sequence, if <i>k</i> is less than <i>n</i>;
	 * otherwise, it is equal to the character at index <i>k-n</i> in the
	 * argument <code>str</code>.
	 * 
	 * @param str
	 *            a string.
	 * @return a reference to this <code>StringBuffer</code>.
	 */
	public StringBuffer append(String str)
	{
		if (str == null)
		{
			str = String.valueOf(str);
		}

		int len = str.length();
		int newcount = count + len;
		if (newcount > value.length)
			expandCapacity(newcount);
		str.getChars(0, len, value, count);
		count = newcount;
		return this;
	}

	/**
	 * Appends the specified <tt>StringBuffer</tt> to this
	 * <tt>StringBuffer</tt>.
	 * <p>
	 * The characters of the <tt>StringBuffer</tt> argument are appended, in
	 * order, to the contents of this <tt>StringBuffer</tt>, increasing the
	 * length of this <tt>StringBuffer</tt> by the length of the argument. If
	 * <tt>sb</tt> is <tt>null</tt>, then the four characters
	 * <tt>"null"</tt> are appended to this <tt>StringBuffer</tt>.
	 * <p>
	 * Let <i>n</i> be the length of the old character sequence, the one
	 * contained in the <tt>StringBuffer</tt> just prior to execution of the
	 * <tt>append</tt> method. Then the character at index <i>k</i> in the
	 * new character sequence is equal to the character at index <i>k</i> in
	 * the old character sequence, if <i>k</i> is less than <i>n</i>;
	 * otherwise, it is equal to the character at index <i>k-n</i> in the
	 * argument <code>sb</code>.
	 * <p>
	 * The method <tt>ensureCapacity</tt> is first called on this
	 * <tt>StringBuffer</tt> with the new buffer length as its argument. (This
	 * ensures that the storage of this <tt>StringBuffer</tt> is adequate to
	 * contain the additional characters being appended.)
	 * 
	 * @param sb
	 *            the <tt>StringBuffer</tt> to append.
	 * @return a reference to this <tt>StringBuffer</tt>.
	 * @since 1.4
	 */
	public StringBuffer append(StringBuffer sb)
	{
		if (sb == null)
		{
			sb = NULL;
		}

		int len = sb.length();
		int newcount = count + len;
		if (newcount > value.length)
			expandCapacity(newcount);
		sb.getChars(0, len, value, count);
		count = newcount;
		return this;
	}

	private static final StringBuffer NULL = new StringBuffer("null");

	/**
	 * Appends the string representation of the <code>char</code> array
	 * argument to this string buffer.
	 * <p>
	 * The characters of the array argument are appended, in order, to the
	 * contents of this string buffer. The length of this string buffer
	 * increases by the length of the argument.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a
	 * string by the method {@link String#valueOf(char[])} and the characters of
	 * that string were then {@link #append(String) appended} to this
	 * <code>StringBuffer</code> object.
	 * 
	 * @param str
	 *            the characters to be appended.
	 * @return a reference to this <code>StringBuffer</code> object.
	 */
	public StringBuffer append(char str[])
	{
		int len = str.length;
		int newcount = count + len;
		if (newcount > value.length)
			expandCapacity(newcount);
		System.arraycopy(str, 0, value, count, len);
		count = newcount;
		return this;
	}

	/**
	 * Appends the string representation of a subarray of the <code>char</code>
	 * array argument to this string buffer.
	 * <p>
	 * Characters of the character array <code>str</code>, starting at index
	 * <code>offset</code>, are appended, in order, to the contents of this
	 * string buffer. The length of this string buffer increases by the value of
	 * <code>len</code>.
	 * <p>
	 * The overall effect is exactly as if the arguments were converted to a
	 * string by the method {@link String#valueOf(char[],int,int)} and the
	 * characters of that string were then {@link #append(String) appended} to
	 * this <code>StringBuffer</code> object.
	 * 
	 * @param str
	 *            the characters to be appended.
	 * @param offset
	 *            the index of the first character to append.
	 * @param len
	 *            the number of characters to append.
	 * @return a reference to this <code>StringBuffer</code> object.
	 */
	public StringBuffer append(char str[], int offset, int len)
	{
		int newcount = count + len;
		if (newcount > value.length)
			expandCapacity(newcount);
		System.arraycopy(str, offset, value, count, len);
		count = newcount;
		return this;
	}

	/**
	 * Appends the string representation of the <code>boolean</code> argument
	 * to the string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then appended to this string buffer.
	 * 
	 * @param b
	 *            a <code>boolean</code>.
	 * @return a reference to this <code>StringBuffer</code>.
	 * @see java.lang.String#valueOf(boolean)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public StringBuffer append(boolean b)
	{
		if (b)
		{
			int newcount = count + 4;
			if (newcount > value.length)
				expandCapacity(newcount);
			value[count++] = 't';
			value[count++] = 'r';
			value[count++] = 'u';
			value[count++] = 'e';
		}
		else
		{
			int newcount = count + 5;
			if (newcount > value.length)
				expandCapacity(newcount);
			value[count++] = 'f';
			value[count++] = 'a';
			value[count++] = 'l';
			value[count++] = 's';
			value[count++] = 'e';
		}
		return this;
	}

	/**
	 * Appends the string representation of the <code>char</code> argument to
	 * this string buffer.
	 * <p>
	 * The argument is appended to the contents of this string buffer. The
	 * length of this string buffer increases by <code>1</code>.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a
	 * string by the method {@link String#valueOf(char)} and the character in
	 * that string were then {@link #append(String) appended} to this
	 * <code>StringBuffer</code> object.
	 * 
	 * @param c
	 *            a <code>char</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 */
	public StringBuffer append(char c)
	{
		int newcount = count + 1;
		if (newcount > value.length)
			expandCapacity(newcount);
		value[count++] = c;
		return this;
	}

	/**
	 * Appends the string representation of the <code>int</code> argument to
	 * this string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then appended to this string buffer.
	 * 
	 * @param i
	 *            an <code>int</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @see java.lang.String#valueOf(int)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public StringBuffer append(int i)
	{
		return append(String.valueOf(i));
	}

	/**
	 * Appends the string representation of the <code>long</code> argument to
	 * this string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then appended to this string buffer.
	 * 
	 * @param l
	 *            a <code>long</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @see java.lang.String#valueOf(long)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public StringBuffer append(long l)
	{
		return append(String.valueOf(l));
	}

	/**
	 * Appends the string representation of the <code>float</code> argument to
	 * this string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then appended to this string buffer.
	 * 
	 * @param f
	 *            a <code>float</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @see java.lang.String#valueOf(float)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public StringBuffer append(float f)
	{
		return append(String.valueOf(f));
	}

	/**
	 * Appends the string representation of the <code>double</code> argument
	 * to this string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then appended to this string buffer.
	 * 
	 * @param d
	 *            a <code>double</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @see java.lang.String#valueOf(double)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public StringBuffer append(double d)
	{
		return append(String.valueOf(d));
	}

	/**
	 * Removes the characters in a substring of this <code>StringBuffer</code>.
	 * The substring begins at the specified <code>start</code> and extends to
	 * the character at index <code>end - 1</code> or to the end of the
	 * <code>StringBuffer</code> if no such character exists. If
	 * <code>start</code> is equal to <code>end</code>, no changes are
	 * made.
	 * 
	 * @param start
	 *            The beginning index, inclusive.
	 * @param end
	 *            The ending index, exclusive.
	 * @return This string buffer.
	 * @exception StringIndexOutOfBoundsException
	 *                if <code>start</code> is negative, greater than
	 *                <code>length()</code>, or greater than <code>end</code>.
	 * @since 1.2
	 */
	public StringBuffer delete(int start, int end)
	{
		if (start < 0)
			throw new StringIndexOutOfBoundsException(start);
		if (end > count)
			end = count;
		if (start > end)
			throw new StringIndexOutOfBoundsException();

		int len = end - start;
		if (len > 0)
		{
			System.arraycopy(value, start + len, value, start, count - end);
			count -= len;
		}
		return this;
	}

	/**
	 * Removes the character at the specified position in this
	 * <code>StringBuffer</code> (shortening the <code>StringBuffer</code>
	 * by one character).
	 * 
	 * @param index
	 *            Index of character to remove
	 * @return This string buffer.
	 * @exception StringIndexOutOfBoundsException
	 *                if the <code>index</code> is negative or greater than or
	 *                equal to <code>length()</code>.
	 * @since 1.2
	 */
	public StringBuffer deleteCharAt(int index)
	{
		if ((index < 0) || (index >= count))
			throw new StringIndexOutOfBoundsException();
		System.arraycopy(value, index + 1, value, index, count - index - 1);
		count--;
		return this;
	}

	/**
	 * Replaces the characters in a substring of this <code>StringBuffer</code>
	 * with characters in the specified <code>String</code>. The substring
	 * begins at the specified <code>start</code> and extends to the character
	 * at index <code>end - 1</code> or to the end of the
	 * <code>StringBuffer</code> if no such character exists. First the
	 * characters in the substring are removed and then the specified
	 * <code>String</code> is inserted at <code>start</code>. (The
	 * <code>StringBuffer</code> will be lengthened to accommodate the
	 * specified String if necessary.)
	 * 
	 * @param start
	 *            The beginning index, inclusive.
	 * @param end
	 *            The ending index, exclusive.
	 * @param str
	 *            String that will replace previous contents.
	 * @return This string buffer.
	 * @exception StringIndexOutOfBoundsException
	 *                if <code>start</code> is negative, greater than
	 *                <code>length()</code>, or greater than <code>end</code>.
	 * @since 1.2
	 */
	public StringBuffer replace(int start, int end, String str)
	{
		if (start < 0)
			throw new StringIndexOutOfBoundsException(start);
		if (end > count)
			end = count;
		if (start > end)
			throw new StringIndexOutOfBoundsException();

		int len = str.length();
		int newCount = count + len - (end - start);
		if (newCount > value.length)
			expandCapacity(newCount);

		System.arraycopy(value, end, value, start + len, count - end);
		str.getChars(0, len, value, start);
		count = newCount;
		return this;
	}

	/**
	 * Returns a new <code>String</code> that contains a subsequence of
	 * characters currently contained in this <code>StringBuffer</code>.The
	 * substring begins at the specified index and extends to the end of the
	 * <code>StringBuffer</code>.
	 * 
	 * @param start
	 *            The beginning index, inclusive.
	 * @return The new string.
	 * @exception StringIndexOutOfBoundsException
	 *                if <code>start</code> is less than zero, or greater than
	 *                the length of this <code>StringBuffer</code>.
	 * @since 1.2
	 */
	public String substring(int start)
	{
		return substring(start, count);
	}

	/**
	 * Returns a new character sequence that is a subsequence of this sequence.
	 * 
	 * <p>
	 * An invocation of this method of the form
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 * sb.subSequence(begin, end)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * behaves in exactly the same way as the invocation
	 * 
	 * <blockquote>
	 * 
	 * <pre>
	 * sb.substring(begin, end)
	 * </pre>
	 * 
	 * </blockquote>
	 * 
	 * This method is provided so that the <tt>StringBuffer</tt> class can
	 * implement the {@link CharSequence} interface.
	 * </p>
	 * 
	 * @param start
	 *            the start index, inclusive.
	 * @param end
	 *            the end index, exclusive.
	 * @return the specified subsequence.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if <tt>start</tt> or <tt>end</tt> are negative, if
	 *             <tt>end</tt> is greater than <tt>length()</tt>, or if
	 *             <tt>start</tt> is greater than <tt>end</tt>
	 * 
	 * @since 1.4
	 * @spec JSR-51
	 */
	public CharSequence subSequence(int start, int end)
	{
		return this.substring(start, end);
	}

	/**
	 * Returns a new <code>String</code> that contains a subsequence of
	 * characters currently contained in this <code>StringBuffer</code>. The
	 * substring begins at the specified <code>start</code> and extends to the
	 * character at index <code>end - 1</code>. An exception is thrown if
	 * 
	 * @param start
	 *            The beginning index, inclusive.
	 * @param end
	 *            The ending index, exclusive.
	 * @return The new string.
	 * @exception StringIndexOutOfBoundsException
	 *                if <code>start</code> or <code>end</code> are negative
	 *                or greater than <code>length()</code>, or
	 *                <code>start</code> is greater than <code>end</code>.
	 * @since 1.2
	 */
	public String substring(int start, int end)
	{
		if (start < 0)
			throw new StringIndexOutOfBoundsException(start);
		if (end > count)
			throw new StringIndexOutOfBoundsException(end);
		if (start > end)
			throw new StringIndexOutOfBoundsException(end - start);
		return new String(value, start, end - start);
	}

	/**
	 * Inserts the string representation of a subarray of the <code>str</code>
	 * array argument into this string buffer. The subarray begins at the
	 * specified <code>offset</code> and extends <code>len</code>
	 * characters. The characters of the subarray are inserted into this string
	 * buffer at the position indicated by <code>index</code>. The length of
	 * this <code>StringBuffer</code> increases by <code>len</code>
	 * characters.
	 * 
	 * @param index
	 *            position at which to insert subarray.
	 * @param str
	 *            A character array.
	 * @param offset
	 *            the index of the first character in subarray to to be
	 *            inserted.
	 * @param len
	 *            the number of characters in the subarray to to be inserted.
	 * @return This string buffer.
	 * @exception StringIndexOutOfBoundsException
	 *                if <code>index</code> is negative or greater than
	 *                <code>length()</code>, or <code>offset</code> or
	 *                <code>len</code> are negative, or
	 *                <code>(offset+len)</code> is greater than
	 *                <code>str.length</code>.
	 * @since 1.2
	 */
	public StringBuffer insert(int index, char str[], int offset, int len)
	{
		if ((index < 0) || (index > count))
			throw new StringIndexOutOfBoundsException();
		if ((offset < 0) || (offset + len < 0) || (offset + len > str.length))
			throw new StringIndexOutOfBoundsException(offset);
		if (len < 0)
			throw new StringIndexOutOfBoundsException(len);
		int newCount = count + len;
		if (newCount > value.length)
			expandCapacity(newCount);
		System.arraycopy(value, index, value, index + len, count - index);
		System.arraycopy(str, offset, value, index, len);
		count = newCount;
		return this;
	}

	/**
	 * Inserts the string representation of the <code>Object</code> argument
	 * into this string buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then inserted into this string buffer at the indicated offset.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>,
	 * and less than or equal to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param obj
	 *            an <code>Object</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(java.lang.Object)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public StringBuffer insert(int offset, Object obj)
	{
		return insert(offset, String.valueOf(obj));
	}

	/**
	 * Inserts the string into this string buffer.
	 * <p>
	 * The characters of the <code>String</code> argument are inserted, in
	 * order, into this string buffer at the indicated offset, moving up any
	 * characters originally above that position and increasing the length of
	 * this string buffer by the length of the argument. If <code>str</code>
	 * is <code>null</code>, then the four characters <code>"null"</code>
	 * are inserted into this string buffer.
	 * <p>
	 * The character at index <i>k</i> in the new character sequence is equal
	 * to:
	 * <ul>
	 * <li>the character at index <i>k</i> in the old character sequence, if
	 * <i>k</i> is less than <code>offset</code>
	 * <li>the character at index <i>k</i><code>-offset</code> in the
	 * argument <code>str</code>, if <i>k</i> is not less than
	 * <code>offset</code> but is less than <code>offset+str.length()</code>
	 * <li>the character at index <i>k</i><code>-str.length()</code> in the
	 * old character sequence, if <i>k</i> is not less than
	 * <code>offset+str.length()</code>
	 * </ul>
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>,
	 * and less than or equal to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param str
	 *            a string.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.StringBuffer#length()
	 */
	public StringBuffer insert(int offset, String str)
	{
		if ((offset < 0) || (offset > count))
		{
			throw new StringIndexOutOfBoundsException();
		}

		if (str == null)
		{
			str = String.valueOf(str);
		}
		int len = str.length();
		int newcount = count + len;
		if (newcount > value.length)
			expandCapacity(newcount);
		System.arraycopy(value, offset, value, offset + len, count - offset);
		str.getChars(0, len, value, offset);
		count = newcount;
		return this;
	}

	/**
	 * Inserts the string representation of the <code>char</code> array
	 * argument into this string buffer.
	 * <p>
	 * The characters of the array argument are inserted into the contents of
	 * this string buffer at the position indicated by <code>offset</code>.
	 * The length of this string buffer increases by the length of the argument.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a
	 * string by the method {@link String#valueOf(char[])} and the characters of
	 * that string were then {@link #insert(int,String) inserted} into this
	 * <code>StringBuffer</code> object at the position indicated by
	 * <code>offset</code>.
	 * 
	 * @param offset
	 *            the offset.
	 * @param str
	 *            a character array.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 */
	public StringBuffer insert(int offset, char str[])
	{
		if ((offset < 0) || (offset > count))
		{
			throw new StringIndexOutOfBoundsException();
		}
		int len = str.length;
		int newcount = count + len;
		if (newcount > value.length)
			expandCapacity(newcount);
		System.arraycopy(value, offset, value, offset + len, count - offset);
		System.arraycopy(str, 0, value, offset, len);
		count = newcount;
		return this;
	}

	/**
	 * Inserts the string representation of the <code>boolean</code> argument
	 * into this string buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then inserted into this string buffer at the indicated offset.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>,
	 * and less than or equal to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param b
	 *            a <code>boolean</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(boolean)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public StringBuffer insert(int offset, boolean b)
	{
		return insert(offset, String.valueOf(b));
	}

	/**
	 * Inserts the string representation of the <code>char</code> argument
	 * into this string buffer.
	 * <p>
	 * The second argument is inserted into the contents of this string buffer
	 * at the position indicated by <code>offset</code>. The length of this
	 * string buffer increases by one.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a
	 * string by the method {@link String#valueOf(char)} and the character in
	 * that string were then {@link #insert(int, String) inserted} into this
	 * <code>StringBuffer</code> object at the position indicated by
	 * <code>offset</code>.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>,
	 * and less than or equal to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param c
	 *            a <code>char</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @exception IndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.StringBuffer#length()
	 */
	public StringBuffer insert(int offset, char c)
	{
		int newcount = count + 1;
		if (newcount > value.length)
			expandCapacity(newcount);
		System.arraycopy(value, offset, value, offset + 1, count - offset);
		value[offset] = c;
		count = newcount;
		return this;
	}

	/**
	 * Inserts the string representation of the second <code>int</code>
	 * argument into this string buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then inserted into this string buffer at the indicated offset.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>,
	 * and less than or equal to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param i
	 *            an <code>int</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(int)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public StringBuffer insert(int offset, int i)
	{
		return insert(offset, String.valueOf(i));
	}

	/**
	 * Inserts the string representation of the <code>long</code> argument
	 * into this string buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then inserted into this string buffer at the position indicated by
	 * <code>offset</code>.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>,
	 * and less than or equal to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param l
	 *            a <code>long</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(long)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public StringBuffer insert(int offset, long l)
	{
		return insert(offset, String.valueOf(l));
	}

	/**
	 * Inserts the string representation of the <code>float</code> argument
	 * into this string buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then inserted into this string buffer at the indicated offset.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>,
	 * and less than or equal to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param f
	 *            a <code>float</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(float)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public StringBuffer insert(int offset, float f)
	{
		return insert(offset, String.valueOf(f));
	}

	/**
	 * Inserts the string representation of the <code>double</code> argument
	 * into this string buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method
	 * <code>String.valueOf</code>, and the characters of that string are
	 * then inserted into this string buffer at the indicated offset.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>,
	 * and less than or equal to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param d
	 *            a <code>double</code>.
	 * @return a reference to this <code>StringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(double)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public StringBuffer insert(int offset, double d)
	{
		return insert(offset, String.valueOf(d));
	}

	/**
	 * Returns the index within this string of the first occurrence of the
	 * specified substring. The integer returned is the smallest value <i>k</i>
	 * such that: <blockquote>
	 * 
	 * <pre>
	 *   this.toString().startsWith(str, &lt;i&gt;k&lt;/i&gt;)
	 * </pre>
	 * 
	 * </blockquote> is <code>true</code>.
	 * 
	 * @param str
	 *            any string.
	 * @return if the string argument occurs as a substring within this object,
	 *         then the index of the first character of the first such substring
	 *         is returned; if it does not occur as a substring, <code>-1</code>
	 *         is returned.
	 * @exception java.lang.NullPointerException
	 *                if <code>str</code> is <code>null</code>.
	 * @since 1.4
	 */
	public int indexOf(String str)
	{
		return indexOf(str, 0);
	}

	/**
	 * Returns the index within this string of the first occurrence of the
	 * specified substring, starting at the specified index. The integer
	 * returned is the smallest value <tt>k</tt> for which: <blockquote>
	 * 
	 * <pre>
	 * k &gt;= Math.min(fromIndex, str.length()) &amp;&amp; this.toString().startsWith(str, k)
	 * </pre>
	 * 
	 * </blockquote> If no such value of <i>k</i> exists, then -1 is returned.
	 * 
	 * @param str
	 *            the substring for which to search.
	 * @param fromIndex
	 *            the index from which to start the search.
	 * @return the index within this string of the first occurrence of the
	 *         specified substring, starting at the specified index.
	 * @exception java.lang.NullPointerException
	 *                if <code>str</code> is <code>null</code>.
	 * @since 1.4
	 */
	public int indexOf(String str, int fromIndex)
	{
		return indexOf(value, 0, count, str.toCharArray(), 0, str.length(), fromIndex);
	}

	static int indexOf(char[] source, int sourceOffset, int sourceCount, char[] target,
			int targetOffset, int targetCount, int fromIndex)
	{
		if (fromIndex >= sourceCount)
		{
			return (targetCount == 0 ? sourceCount : -1);
		}
		if (fromIndex < 0)
		{
			fromIndex = 0;
		}
		if (targetCount == 0)
		{
			return fromIndex;
		}

		char first = target[targetOffset];
		int i = sourceOffset + fromIndex;
		int max = sourceOffset + (sourceCount - targetCount);

		startSearchForFirstChar : while (true)
		{
			/* Look for first character. */
			while (i <= max && source[i] != first)
			{
				i++;
			}
			if (i > max)
			{
				return -1;
			}

			/* Found first character, now look at the rest of v2 */
			int j = i + 1;
			int end = j + targetCount - 1;
			int k = targetOffset + 1;
			while (j < end)
			{
				if (source[j++] != target[k++])
				{
					i++;
					/* Look for str's first char again. */
					continue startSearchForFirstChar;
				}
			}
			return i - sourceOffset; /* Found whole string. */
		}
	}

	/**
	 * Returns the index within this string of the rightmost occurrence of the
	 * specified substring. The rightmost empty string "" is considered to occur
	 * at the index value <code>this.length()</code>. The returned index is
	 * the largest value <i>k</i> such that <blockquote>
	 * 
	 * <pre>
	 * this.toString().startsWith(str, k)
	 * </pre>
	 * 
	 * </blockquote> is true.
	 * 
	 * @param str
	 *            the substring to search for.
	 * @return if the string argument occurs one or more times as a substring
	 *         within this object, then the index of the first character of the
	 *         last such substring is returned. If it does not occur as a
	 *         substring, <code>-1</code> is returned.
	 * @exception java.lang.NullPointerException
	 *                if <code>str</code> is <code>null</code>.
	 * @since 1.4
	 */
	public int lastIndexOf(String str)
	{
		return lastIndexOf(str, count);
	}

	/**
	 * Returns the index within this string of the last occurrence of the
	 * specified substring. The integer returned is the largest value <i>k</i>
	 * such that: <blockquote>
	 * 
	 * <pre>
	 * k &lt;= Math.min(fromIndex, str.length()) &amp;&amp; this.toString().startsWith(str, k)
	 * </pre>
	 * 
	 * </blockquote> If no such value of <i>k</i> exists, then -1 is returned.
	 * 
	 * @param str
	 *            the substring to search for.
	 * @param fromIndex
	 *            the index to start the search from.
	 * @return the index within this string of the last occurrence of the
	 *         specified substring.
	 * @exception java.lang.NullPointerException
	 *                if <code>str</code> is <code>null</code>.
	 * @since 1.4
	 */
	public int lastIndexOf(String str, int fromIndex)
	{
		return lastIndexOf(value, 0, count, str.toCharArray(), 0, str.length(), fromIndex);
	}

    static int lastIndexOf(char[] source, int sourceOffset, int sourceCount,
                           char[] target, int targetOffset, int targetCount,
                           int fromIndex) {
        /*
		 * Check arguments; return immediately where possible. For consistency,
		 * don't check for null str.
		 */
        int rightIndex = sourceCount - targetCount;
	if (fromIndex < 0) {
	    return -1;
	}
	if (fromIndex > rightIndex) {
	    fromIndex = rightIndex;
	}
	/* Empty string always matches. */
	if (targetCount == 0) {
	    return fromIndex;
	}

        int strLastIndex = targetOffset + targetCount - 1;
	char strLastChar = target[strLastIndex];
	int min = sourceOffset + targetCount - 1;
	int i = min + fromIndex;

    startSearchForLastChar:
	while (true) {
	    while (i >= min && source[i] != strLastChar) {
		i--;
	    }
	    if (i < min) {
		return -1;
	    }
	    int j = i - 1;
	    int start = j - (targetCount - 1);
	    int k = strLastIndex - 1;

	    while (j > start) {
	        if (source[j--] != target[k--]) {
		    i--;
		    continue startSearchForLastChar;
		}
	    }
	    return start - sourceOffset + 1;
	}
    }
	
	/**
	 * Converts to a string representing the data in this string buffer. A new
	 * <code>String</code> object is allocated and initialized to contain the
	 * character sequence currently represented by this string buffer. This
	 * <code>String</code> is then returned. Subsequent changes to the string
	 * buffer do not affect the contents of the <code>String</code>.
	 * <p>
	 * Implementation advice: This method can be coded so as to create a new
	 * <code>String</code> object without allocating new memory to hold a copy
	 * of the character sequence. Instead, the string can share the memory used
	 * by the string buffer. Any subsequent operation that alters the content or
	 * capacity of the string buffer must then make a copy of the internal
	 * buffer at that time. This strategy is effective for reducing the amount
	 * of memory allocated by a string concatenation operation when it is
	 * implemented using a string buffer.
	 * 
	 * @return a string representation of the string buffer.
	 */
	public String toString()
	{
		return new String(this.value,0,count);
	}

	/**
	 * @return The internal char array
	 */
	public final char[] getValue()
	{
		return value;
	}


	/**
	 * readObject is called to restore the state of the StringBuffer from a
	 * stream.
	 * 
	 * @param s
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readObject(java.io.ObjectInputStream s) throws IOException, ClassNotFoundException
	{
		s.defaultReadObject();
		value = (char[])value.clone();
	}
}
