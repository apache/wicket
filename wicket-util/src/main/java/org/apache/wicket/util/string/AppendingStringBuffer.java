/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.util.string;

import java.io.IOException;

/**
 * This is a copy or combination of <code>java.lang.StringBuffer</code> and
 * <code>java.lang.String</code> It has a special method getValue() which returns the internal char
 * array.
 * 
 * Hashcode and equals methods are also implemented.
 * 
 * This AppendingStringBuffer is not synchronized.
 * 
 * @author Johan Compagner
 * @see java.lang.StringBuffer
 */
public final class AppendingStringBuffer implements java.io.Serializable, CharSequence
{
	/** use serialVersionUID from JDK 1.0.2 for interoperability */
	private static final long serialVersionUID = 1L;

	private static final AppendingStringBuffer NULL = new AppendingStringBuffer("null");
	private static final StringBuilder SB_NULL = new StringBuilder("null");
	private static final StringBuffer SBF_NULL = new StringBuffer("null");

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

	/**
	 * Constructs a string buffer with no characters in it and an initial capacity of 16 characters.
	 */
	public AppendingStringBuffer()
	{
		this(16);
	}

	/**
	 * Constructs a string buffer with no characters in it and an initial capacity specified by the
	 * <code>length</code> argument.
	 * 
	 * @param length
	 *            the initial capacity.
	 * @exception NegativeArraySizeException
	 *                if the <code>length</code> argument is less than <code>0</code>.
	 */
	public AppendingStringBuffer(final int length)
	{
		value = new char[length];
	}

	/**
	 * Constructs a string buffer so that it represents the same sequence of characters as the
	 * string argument; in other words, the initial contents of the string buffer is a copy of the
	 * argument string. The initial capacity of the string buffer is <code>16</code> plus the length
	 * of the string argument.
	 * 
	 * @param str
	 *            the initial contents of the buffer.
	 * @exception NullPointerException
	 *                if <code>str</code> is <code>null</code>
	 */
	public AppendingStringBuffer(final CharSequence str)
	{
		this(str.length() + 16);
		append(str);
	}

	/**
	 * Returns the length (character count) of this string buffer.
	 * 
	 * @return the length of the sequence of characters currently represented by this string buffer.
	 */
	@Override
	public int length()
	{
		return count;
	}

	/**
	 * Returns the current capacity of the String buffer. The capacity is the amount of storage
	 * available for newly inserted characters; beyond which an allocation will occur.
	 * 
	 * @return the current capacity of this string buffer.
	 */
	public int capacity()
	{
		return value.length;
	}

	/**
	 * Ensures that the capacity of the buffer is at least equal to the specified minimum. If the
	 * current capacity of this string buffer is less than the argument, then a new internal buffer
	 * is allocated with greater capacity. The new capacity is the larger of:
	 * <ul>
	 * <li>The <code>minimumCapacity</code> argument.
	 * <li>Twice the old capacity, plus <code>2</code>.
	 * </ul>
	 * If the <code>minimumCapacity</code> argument is nonpositive, this method takes no action and
	 * simply returns.
	 * 
	 * @param minimumCapacity
	 *            the minimum desired capacity.
	 */
	public void ensureCapacity(final int minimumCapacity)
	{
		if (minimumCapacity > value.length)
		{
			expandCapacity(minimumCapacity);
		}
	}

	/**
	 * This implements the expansion semantics of ensureCapacity but is unsynchronized for use
	 * internally by methods which are already synchronized.
	 * 
	 * @param minimumCapacity
	 * 
	 * @see java.lang.StringBuffer#ensureCapacity(int)
	 */
	private void expandCapacity(final int minimumCapacity)
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
	 * Sets the length of this String buffer. This string buffer is altered to represent a new
	 * character sequence whose length is specified by the argument. For every nonnegative index
	 * <i>k</i> less than <code>newLength</code>, the character at index <i>k</i> in the new
	 * character sequence is the same as the character at index <i>k</i> in the old sequence if
	 * <i>k</i> is less than the length of the old character sequence; otherwise, it is the null
	 * character <code>'&#92;u0000'</code>.
	 * 
	 * In other words, if the <code>newLength</code> argument is less than the current length of the
	 * string buffer, the string buffer is truncated to contain exactly the number of characters
	 * given by the <code>newLength</code> argument.
	 * <p>
	 * If the <code>newLength</code> argument is greater than or equal to the current length,
	 * sufficient null characters (<code>'&#92;u0000'</code>) are appended to the string buffer so
	 * that length becomes the <code>newLength</code> argument.
	 * <p>
	 * The <code>newLength</code> argument must be greater than or equal to <code>0</code>.
	 * 
	 * @param newLength
	 *            the new length of the buffer.
	 * @exception IndexOutOfBoundsException
	 *                if the <code>newLength</code> argument is negative.
	 * @see java.lang.StringBuffer#length()
	 */
	public void setLength(final int newLength)
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
	 * The specified character of the sequence currently represented by the string buffer, as
	 * indicated by the <code>index</code> argument, is returned. The first character of a string
	 * buffer is at index <code>0</code>, the next at index <code>1</code>, and so on, for array
	 * indexing.
	 * <p>
	 * The index argument must be greater than or equal to <code>0</code>, and less than the length
	 * of this string buffer.
	 * 
	 * @param index
	 *            the index of the desired character.
	 * @return the character at the specified index of this string buffer.
	 * @exception IndexOutOfBoundsException
	 *                if <code>index</code> is negative or greater than or equal to
	 *                <code>length()</code>.
	 * @see java.lang.StringBuffer#length()
	 */
	@Override
	public char charAt(final int index)
	{
		if ((index < 0) || (index >= count))
		{
			throw new StringIndexOutOfBoundsException(index);
		}
		return value[index];
	}

	/**
	 * Characters are copied from this string buffer into the destination character array
	 * <code>dst</code>. The first character to be copied is at index <code>srcBegin</code>; the
	 * last character to be copied is at index <code>srcEnd-1</code>. The total number of characters
	 * to be copied is <code>srcEnd-srcBegin</code>. The characters are copied into the subarray of
	 * <code>dst</code> starting at index <code>dstBegin</code> and ending at index:
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
	 *                <li><code>srcBegin</code> is negative <li><code>dstBegin</code> is negative
	 *                <li>the <code>srcBegin</code> argument is greater than the <code>srcEnd</code>
	 *                argument. <li><code>srcEnd</code> is greater than <code>this.length()</code>,
	 *                the current length of this string buffer. <li><code>dstBegin+srcEnd-srcBegin
	 *                </code> is greater than <code>dst.length</code>
	 *                </ul>
	 */
	public void getChars(final int srcBegin, final int srcEnd, final char dst[], final int dstBegin)
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
	 * The character at the specified index of this string buffer is set to <code>ch</code>. The
	 * string buffer is altered to represent a new character sequence that is identical to the old
	 * character sequence, except that it contains the character <code>ch</code> at position
	 * <code>index</code>.
	 * <p>
	 * The index argument must be greater than or equal to <code>0</code>, and less than the length
	 * of this string buffer.
	 * 
	 * @param index
	 *            the index of the character to modify.
	 * @param ch
	 *            the new character.
	 * @exception IndexOutOfBoundsException
	 *                if <code>index</code> is negative or greater than or equal to
	 *                <code>length()</code>.
	 * @see java.lang.StringBuffer#length()
	 */
	public void setCharAt(final int index, final char ch)
	{
		if ((index < 0) || (index >= count))
		{
			throw new StringIndexOutOfBoundsException(index);
		}
		value[index] = ch;
	}

	/**
	 * Appends the string representation of the <code>Object</code> argument to this string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method <code>String.valueOf</code>, and
	 * the characters of that string are then appended to this string buffer.
	 * 
	 * @param obj
	 *            an <code>Object</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @see java.lang.String#valueOf(java.lang.Object)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public AppendingStringBuffer append(final Object obj)
	{
		if (obj instanceof AppendingStringBuffer)
		{
			return append((AppendingStringBuffer)obj);
		}
		else if (obj instanceof StringBuilder)
		{
			return append((StringBuilder)obj);
		}
		else if (obj instanceof StringBuffer)
		{
			return append(obj.toString());
		}
		return append(String.valueOf(obj));
	}

	/**
	 * Appends the string to this string buffer.
	 * <p>
	 * The characters of the <code>String</code> argument are appended, in order, to the contents of
	 * this string buffer, increasing the length of this string buffer by the length of the
	 * argument. If <code>str</code> is <code>null</code>, then the four characters
	 * <code>"null"</code> are appended to this string buffer.
	 * <p>
	 * Let <i>n</i> be the length of the old character sequence, the one contained in the string
	 * buffer just prior to execution of the <code>append</code> method. Then the character at index
	 * <i>k</i> in the new character sequence is equal to the character at index <i>k</i> in the old
	 * character sequence, if <i>k</i> is less than <i>n</i>; otherwise, it is equal to the
	 * character at index <i>k-n</i> in the argument <code>str</code>.
	 * 
	 * @param str
	 *            a string.
	 * @return a reference to this <code>AppendingStringBuffer</code>.
	 */
	public AppendingStringBuffer append(String str)
	{
		if (str == null)
		{
			str = String.valueOf(str);
		}

		int len = str.length();
		int newcount = count + len;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		str.getChars(0, len, value, count);
		count = newcount;
		return this;
	}

	/**
	 * Appends the specified <tt>AppendingStringBuffer</tt> to this <tt>AppendingStringBuffer</tt>.
	 * <p>
	 * The characters of the <tt>AppendingStringBuffer</tt> argument are appended, in order, to the
	 * contents of this <tt>AppendingStringBuffer</tt>, increasing the length of this
	 * <tt>AppendingStringBuffer</tt> by the length of the argument. If <tt>sb</tt> is <tt>null</tt>
	 * , then the four characters <tt>"null"</tt> are appended to this
	 * <tt>AppendingStringBuffer</tt>.
	 * <p>
	 * Let <i>n</i> be the length of the old character sequence, the one contained in the
	 * <tt>AppendingStringBuffer</tt> just prior to execution of the <tt>append</tt> method. Then
	 * the character at index <i>k</i> in the new character sequence is equal to the character at
	 * index <i>k</i> in the old character sequence, if <i>k</i> is less than <i>n</i>; otherwise,
	 * it is equal to the character at index <i>k-n</i> in the argument <code>sb</code>.
	 * <p>
	 * The method <tt>ensureCapacity</tt> is first called on this <tt>AppendingStringBuffer</tt>
	 * with the new buffer length as its argument. (This ensures that the storage of this
	 * <tt>AppendingStringBuffer</tt> is adequate to contain the additional characters being
	 * appended.)
	 * 
	 * @param sb
	 *            the <tt>AppendingStringBuffer</tt> to append.
	 * @return a reference to this <tt>AppendingStringBuffer</tt>.
	 * @since 1.4
	 */
	public AppendingStringBuffer append(AppendingStringBuffer sb)
	{
		if (sb == null)
		{
			sb = NULL;
		}

		int len = sb.length();
		int newcount = count + len;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		sb.getChars(0, len, value, count);
		count = newcount;
		return this;
	}

	/**
	 * Appends the specified <tt>AppendingStringBuffer</tt> to this <tt>AppendingStringBuffer</tt>.
	 * <p>
	 * The characters of the <tt>AppendingStringBuffer</tt> argument are appended, in order, to the
	 * contents of this <tt>AppendingStringBuffer</tt>, increasing the length of this
	 * <tt>AppendingStringBuffer</tt> by the length of the argument. If <tt>sb</tt> is <tt>null</tt>
	 * , then the four characters <tt>"null"</tt> are appended to this
	 * <tt>AppendingStringBuffer</tt>.
	 * <p>
	 * Let <i>n</i> be the length of the old character sequence, the one contained in the
	 * <tt>AppendingStringBuffer</tt> just prior to execution of the <tt>append</tt> method. Then
	 * the character at index <i>k</i> in the new character sequence is equal to the character at
	 * index <i>k</i> in the old character sequence, if <i>k</i> is less than <i>n</i>; otherwise,
	 * it is equal to the character at index <i>k-n</i> in the argument <code>sb</code>.
	 * <p>
	 * The method <tt>ensureCapacity</tt> is first called on this <tt>AppendingStringBuffer</tt>
	 * with the new buffer length as its argument. (This ensures that the storage of this
	 * <tt>AppendingStringBuffer</tt> is adequate to contain the additional characters being
	 * appended.)
	 * 
	 * @param sb
	 *            the <tt>AppendingStringBuffer</tt> to append.
	 * @return a reference to this <tt>AppendingStringBuffer</tt>.
	 * @since 1.4
	 */
	public AppendingStringBuffer append(StringBuilder sb)
	{
		if (sb == null)
		{
			sb = SB_NULL;
		}

		int len = sb.length();
		int newcount = count + len;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		sb.getChars(0, len, value, count);
		count = newcount;
		return this;
	}

	/**
	 * Appends the string representation of the <code>char</code> array argument to this string
	 * buffer.
	 * <p>
	 * The characters of the array argument are appended, in order, to the contents of this string
	 * buffer. The length of this string buffer increases by the length of the argument.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a string by the method
	 * {@link String#valueOf(char[])} and the characters of that string were then
	 * {@link #append(String) appended} to this <code>AppendingStringBuffer</code> object.
	 * 
	 * @param str
	 *            the characters to be appended.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 */
	public AppendingStringBuffer append(final char str[])
	{
		int len = str.length;
		int newcount = count + len;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		System.arraycopy(str, 0, value, count, len);
		count = newcount;
		return this;
	}

	/**
	 * Appends the string representation of a subarray of the <code>char</code> array argument to
	 * this string buffer.
	 * <p>
	 * Characters of the character array <code>str</code>, starting at index <code>offset</code>,
	 * are appended, in order, to the contents of this string buffer. The length of this string
	 * buffer increases by the value of <code>len</code>.
	 * <p>
	 * The overall effect is exactly as if the arguments were converted to a string by the method
	 * {@link String#valueOf(char[],int,int)} and the characters of that string were then
	 * {@link #append(String) appended} to this <code>AppendingStringBuffer</code> object.
	 * 
	 * @param str
	 *            the characters to be appended.
	 * @param offset
	 *            the index of the first character to append.
	 * @param len
	 *            the number of characters to append.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 */
	public AppendingStringBuffer append(final char str[], final int offset, final int len)
	{
		int newcount = count + len;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		System.arraycopy(str, offset, value, count, len);
		count = newcount;
		return this;
	}

	/**
	 * Appends the string representation of the <code>boolean</code> argument to the string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method <code>String.valueOf</code>, and
	 * the characters of that string are then appended to this string buffer.
	 * 
	 * @param b
	 *            a <code>boolean</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code>.
	 * @see java.lang.String#valueOf(boolean)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public AppendingStringBuffer append(final boolean b)
	{
		if (b)
		{
			int newcount = count + 4;
			if (newcount > value.length)
			{
				expandCapacity(newcount);
			}
			value[count++] = 't';
			value[count++] = 'r';
			value[count++] = 'u';
			value[count++] = 'e';
		}
		else
		{
			int newcount = count + 5;
			if (newcount > value.length)
			{
				expandCapacity(newcount);
			}
			value[count++] = 'f';
			value[count++] = 'a';
			value[count++] = 'l';
			value[count++] = 's';
			value[count++] = 'e';
		}
		return this;
	}

	/**
	 * Appends the string representation of the <code>char</code> argument to this string buffer.
	 * <p>
	 * The argument is appended to the contents of this string buffer. The length of this string
	 * buffer increases by <code>1</code>.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a string by the method
	 * {@link String#valueOf(char)} and the character in that string were then
	 * {@link #append(String) appended} to this <code>AppendingStringBuffer</code> object.
	 * 
	 * @param c
	 *            a <code>char</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 */
	public AppendingStringBuffer append(final char c)
	{
		int newcount = count + 1;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		value[count++] = c;
		return this;
	}

	/**
	 * Appends the string representation of the <code>int</code> argument to this string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method <code>String.valueOf</code>, and
	 * the characters of that string are then appended to this string buffer.
	 * 
	 * @param i
	 *            an <code>int</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @see java.lang.String#valueOf(int)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public AppendingStringBuffer append(final int i)
	{
		return append(String.valueOf(i));
	}

	/**
	 * Appends the string representation of the <code>long</code> argument to this string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method <code>String.valueOf</code>, and
	 * the characters of that string are then appended to this string buffer.
	 * 
	 * @param l
	 *            a <code>long</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @see java.lang.String#valueOf(long)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public AppendingStringBuffer append(final long l)
	{
		return append(String.valueOf(l));
	}

	/**
	 * Appends the string representation of the <code>float</code> argument to this string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method <code>String.valueOf</code>, and
	 * the characters of that string are then appended to this string buffer.
	 * 
	 * @param f
	 *            a <code>float</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @see java.lang.String#valueOf(float)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public AppendingStringBuffer append(final float f)
	{
		return append(String.valueOf(f));
	}

	/**
	 * Appends the string representation of the <code>double</code> argument to this string buffer.
	 * <p>
	 * The argument is converted to a string as if by the method <code>String.valueOf</code>, and
	 * the characters of that string are then appended to this string buffer.
	 * 
	 * @param d
	 *            a <code>double</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @see java.lang.String#valueOf(double)
	 * @see java.lang.StringBuffer#append(java.lang.String)
	 */
	public AppendingStringBuffer append(final double d)
	{
		return append(String.valueOf(d));
	}

	/**
	 * Removes the characters in a substring of this <code>AppendingStringBuffer</code>. The
	 * substring begins at the specified <code>start</code> and extends to the character at index
	 * <code>end - 1</code> or to the end of the <code>AppendingStringBuffer</code> if no such
	 * character exists. If <code>start</code> is equal to <code>end</code>, no changes are made.
	 * 
	 * @param start
	 *            The beginning index, inclusive.
	 * @param end
	 *            The ending index, exclusive.
	 * @return This string buffer.
	 * @exception StringIndexOutOfBoundsException
	 *                if <code>start</code> is negative, greater than <code>length()</code>, or
	 *                greater than <code>end</code>.
	 * @since 1.2
	 */
	public AppendingStringBuffer delete(final int start, int end)
	{
		if (start < 0)
		{
			throw new StringIndexOutOfBoundsException(start);
		}
		if (end > count)
		{
			end = count;
		}
		if (start > end)
		{
			throw new StringIndexOutOfBoundsException();
		}

		int len = end - start;
		if (len > 0)
		{
			System.arraycopy(value, start + len, value, start, count - end);
			count -= len;
		}
		return this;
	}

	/**
	 * Removes the character at the specified position in this <code>AppendingStringBuffer</code>
	 * (shortening the <code>AppendingStringBuffer</code> by one character).
	 * 
	 * @param index
	 *            Index of character to remove
	 * @return This string buffer.
	 * @exception StringIndexOutOfBoundsException
	 *                if the <code>index</code> is negative or greater than or equal to
	 *                <code>length()</code>.
	 * @since 1.2
	 */
	public AppendingStringBuffer deleteCharAt(final int index)
	{
		if ((index < 0) || (index >= count))
		{
			throw new StringIndexOutOfBoundsException();
		}
		System.arraycopy(value, index + 1, value, index, count - index - 1);
		count--;
		return this;
	}

	/**
	 * Replaces the characters in a substring of this <code>AppendingStringBuffer</code> with
	 * characters in the specified <code>String</code>. The substring begins at the specified
	 * <code>start</code> and extends to the character at index <code>end - 1</code> or to the end
	 * of the <code>AppendingStringBuffer</code> if no such character exists. First the characters
	 * in the substring are removed and then the specified <code>String</code> is inserted at
	 * <code>start</code>. (The <code>AppendingStringBuffer</code> will be lengthened to accommodate
	 * the specified String if necessary.)
	 * 
	 * @param start
	 *            The beginning index, inclusive.
	 * @param end
	 *            The ending index, exclusive.
	 * @param str
	 *            String that will replace previous contents.
	 * @return This string buffer.
	 * @exception StringIndexOutOfBoundsException
	 *                if <code>start</code> is negative, greater than <code>length()</code>, or
	 *                greater than <code>end</code>.
	 * @since 1.2
	 */
	public AppendingStringBuffer replace(final int start, int end, final String str)
	{
		if (start < 0)
		{
			throw new StringIndexOutOfBoundsException(start);
		}
		if (end > count)
		{
			end = count;
		}
		if (start > end)
		{
			throw new StringIndexOutOfBoundsException();
		}

		int len = str.length();
		int newCount = count + len - (end - start);
		if (newCount > value.length)
		{
			expandCapacity(newCount);
		}

		System.arraycopy(value, end, value, start + len, count - end);
		str.getChars(0, len, value, start);
		count = newCount;
		return this;
	}

	/**
	 * Returns a new <code>String</code> that contains a subsequence of characters currently
	 * contained in this <code>AppendingStringBuffer</code>.The substring begins at the specified
	 * index and extends to the end of the <code>AppendingStringBuffer</code>.
	 * 
	 * @param start
	 *            The beginning index, inclusive.
	 * @return The new string.
	 * @exception StringIndexOutOfBoundsException
	 *                if <code>start</code> is less than zero, or greater than the length of this
	 *                <code>AppendingStringBuffer</code>.
	 * @since 1.2
	 */
	public String substring(final int start)
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
	 * This method is provided so that the <tt>AppendingStringBuffer</tt> class can implement the
	 * {@link CharSequence} interface.
	 * </p>
	 * 
	 * @param start
	 *            the start index, inclusive.
	 * @param end
	 *            the end index, exclusive.
	 * @return the specified subsequence.
	 * 
	 * @throws IndexOutOfBoundsException
	 *             if <tt>start</tt> or <tt>end</tt> are negative, if <tt>end</tt> is greater than
	 *             <tt>length()</tt>, or if <tt>start</tt> is greater than <tt>end</tt>
	 * 
	 * @since 1.4
	 * @spec JSR-51
	 */
	@Override
	public CharSequence subSequence(final int start, final int end)
	{
		return this.substring(start, end);
	}

	/**
	 * Returns a new <code>String</code> that contains a subsequence of characters currently
	 * contained in this <code>AppendingStringBuffer</code>. The substring begins at the specified
	 * <code>start</code> and extends to the character at index <code>end - 1</code>. An exception
	 * is thrown if
	 * 
	 * @param start
	 *            The beginning index, inclusive.
	 * @param end
	 *            The ending index, exclusive.
	 * @return The new string.
	 * @exception StringIndexOutOfBoundsException
	 *                if <code>start</code> or <code>end</code> are negative or greater than
	 *                <code>length()</code>, or <code>start</code> is greater than <code>end</code>.
	 * @since 1.2
	 */
	public String substring(final int start, final int end)
	{
		if (start < 0)
		{
			throw new StringIndexOutOfBoundsException(start);
		}
		if (end > count)
		{
			throw new StringIndexOutOfBoundsException(end);
		}
		if (start > end)
		{
			throw new StringIndexOutOfBoundsException(end - start);
		}
		return new String(value, start, end - start);
	}

	/**
	 * Inserts the string representation of a subarray of the <code>str</code> array argument into
	 * this string buffer. The subarray begins at the specified <code>offset</code> and extends
	 * <code>len</code> characters. The characters of the subarray are inserted into this string
	 * buffer at the position indicated by <code>index</code>. The length of this
	 * <code>AppendingStringBuffer</code> increases by <code>len</code> characters.
	 * 
	 * @param index
	 *            position at which to insert subarray.
	 * @param str
	 *            A character array.
	 * @param offset
	 *            the index of the first character in subarray to to be inserted.
	 * @param len
	 *            the number of characters in the subarray to to be inserted.
	 * @return This string buffer.
	 * @exception StringIndexOutOfBoundsException
	 *                if <code>index</code> is negative or greater than <code>length()</code>, or
	 *                <code>offset</code> or <code>len</code> are negative, or
	 *                <code>(offset+len)</code> is greater than <code>str.length</code>.
	 * @since 1.2
	 */
	public AppendingStringBuffer insert(final int index, final char str[], final int offset,
		final int len)
	{
		if ((index < 0) || (index > count))
		{
			throw new StringIndexOutOfBoundsException();
		}
		if ((offset < 0) || (offset + len < 0) || (offset + len > str.length))
		{
			throw new StringIndexOutOfBoundsException(offset);
		}
		if (len < 0)
		{
			throw new StringIndexOutOfBoundsException(len);
		}
		int newCount = count + len;
		if (newCount > value.length)
		{
			expandCapacity(newCount);
		}
		System.arraycopy(value, index, value, index + len, count - index);
		System.arraycopy(str, offset, value, index, len);
		count = newCount;
		return this;
	}

	/**
	 * Inserts the string representation of the <code>Object</code> argument into this string
	 * buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method <code>String.valueOf</code>,
	 * and the characters of that string are then inserted into this string buffer at the indicated
	 * offset.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>, and less than or equal
	 * to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param obj
	 *            an <code>Object</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(java.lang.Object)
	 * @see AppendingStringBuffer#insert(int, java.lang.String)
	 * @see AppendingStringBuffer#length()
	 */
	public AppendingStringBuffer insert(final int offset, final Object obj)
	{
		if (obj instanceof AppendingStringBuffer)
		{
			AppendingStringBuffer asb = (AppendingStringBuffer)obj;
			return insert(offset, asb.value, 0, asb.count);
		}
		else if (obj instanceof StringBuffer)
		{
			return insert(offset, (StringBuffer)obj);
		}
		else if (obj instanceof StringBuilder)
		{
			return insert(offset, (StringBuilder)obj);
		}
		return insert(offset, String.valueOf(obj));
	}

	/**
	 * Inserts the string into this string buffer.
	 * <p>
	 * The characters of the <code>String</code> argument are inserted, in order, into this string
	 * buffer at the indicated offset, moving up any characters originally above that position and
	 * increasing the length of this string buffer by the length of the argument. If
	 * <code>str</code> is <code>null</code>, then the four characters <code>"null"</code> are
	 * inserted into this string buffer.
	 * <p>
	 * The character at index <i>k</i> in the new character sequence is equal to:
	 * <ul>
	 * <li>the character at index <i>k</i> in the old character sequence, if <i>k</i> is less than
	 * <code>offset</code>
	 * <li>the character at index <i>k</i><code>-offset</code> in the argument <code>str</code>, if
	 * <i>k</i> is not less than <code>offset</code> but is less than
	 * <code>offset+str.length()</code>
	 * <li>the character at index <i>k</i><code>-str.length()</code> in the old character sequence,
	 * if <i>k</i> is not less than <code>offset+str.length()</code>
	 * </ul>
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>, and less than or equal
	 * to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param str
	 *            a string.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.StringBuffer#length()
	 */
	public AppendingStringBuffer insert(final int offset, String str)
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
		{
			expandCapacity(newcount);
		}
		System.arraycopy(value, offset, value, offset + len, count - offset);
		str.getChars(0, len, value, offset);
		count = newcount;
		return this;
	}

	/**
	 * Inserts the string into this string buffer.
	 * <p>
	 * The characters of the <code>StringBuilder</code> argument are inserted, in order, into this
	 * string buffer at the indicated offset, moving up any characters originally above that
	 * position and increasing the length of this string buffer by the length of the argument. If
	 * <code>str</code> is <code>null</code>, then the four characters <code>"null"</code> are
	 * inserted into this string buffer.
	 * <p>
	 * The character at index <i>k</i> in the new character sequence is equal to:
	 * <ul>
	 * <li>the character at index <i>k</i> in the old character sequence, if <i>k</i> is less than
	 * <code>offset</code>
	 * <li>the character at index <i>k</i><code>-offset</code> in the argument <code>str</code>, if
	 * <i>k</i> is not less than <code>offset</code> but is less than
	 * <code>offset+str.length()</code>
	 * <li>the character at index <i>k</i><code>-str.length()</code> in the old character sequence,
	 * if <i>k</i> is not less than <code>offset+str.length()</code>
	 * </ul>
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>, and less than or equal
	 * to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param str
	 *            a string.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.StringBuffer#length()
	 */
	public AppendingStringBuffer insert(final int offset, StringBuilder str)
	{
		if ((offset < 0) || (offset > count))
		{
			throw new StringIndexOutOfBoundsException();
		}

		if (str == null)
		{
			str = SB_NULL;
		}
		int len = str.length();
		int newcount = count + len;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		System.arraycopy(value, offset, value, offset + len, count - offset);
		str.getChars(0, len, value, offset);
		count = newcount;
		return this;
	}

	/**
	 * Inserts the string into this string buffer.
	 * <p>
	 * The characters of the <code>StringBuffer</code> argument are inserted, in order, into this
	 * string buffer at the indicated offset, moving up any characters originally above that
	 * position and increasing the length of this string buffer by the length of the argument. If
	 * <code>str</code> is <code>null</code>, then the four characters <code>"null"</code> are
	 * inserted into this string buffer.
	 * <p>
	 * The character at index <i>k</i> in the new character sequence is equal to:
	 * <ul>
	 * <li>the character at index <i>k</i> in the old character sequence, if <i>k</i> is less than
	 * <code>offset</code>
	 * <li>the character at index <i>k</i><code>-offset</code> in the argument <code>str</code>, if
	 * <i>k</i> is not less than <code>offset</code> but is less than
	 * <code>offset+str.length()</code>
	 * <li>the character at index <i>k</i><code>-str.length()</code> in the old character sequence,
	 * if <i>k</i> is not less than <code>offset+str.length()</code>
	 * </ul>
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>, and less than or equal
	 * to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param str
	 *            a string.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.StringBuffer#length()
	 */
	public AppendingStringBuffer insert(final int offset, StringBuffer str)
	{
		if ((offset < 0) || (offset > count))
		{
			throw new StringIndexOutOfBoundsException();
		}

		if (str == null)
		{
			str = SBF_NULL;
		}
		int len = str.length();
		int newcount = count + len;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		System.arraycopy(value, offset, value, offset + len, count - offset);
		str.getChars(0, len, value, offset);
		count = newcount;
		return this;
	}

	/**
	 * Inserts the string representation of the <code>char</code> array argument into this string
	 * buffer.
	 * <p>
	 * The characters of the array argument are inserted into the contents of this string buffer at
	 * the position indicated by <code>offset</code>. The length of this string buffer increases by
	 * the length of the argument.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a string by the method
	 * {@link String#valueOf(char[])} and the characters of that string were then
	 * {@link #insert(int,String) inserted} into this <code>AppendingStringBuffer</code> object at
	 * the position indicated by <code>offset</code>.
	 * 
	 * @param offset
	 *            the offset.
	 * @param str
	 *            a character array.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 */
	public AppendingStringBuffer insert(final int offset, final char str[])
	{
		if ((offset < 0) || (offset > count))
		{
			throw new StringIndexOutOfBoundsException();
		}
		int len = str.length;
		int newcount = count + len;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		System.arraycopy(value, offset, value, offset + len, count - offset);
		System.arraycopy(str, 0, value, offset, len);
		count = newcount;
		return this;
	}

	/**
	 * Inserts the string representation of the <code>boolean</code> argument into this string
	 * buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method <code>String.valueOf</code>,
	 * and the characters of that string are then inserted into this string buffer at the indicated
	 * offset.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>, and less than or equal
	 * to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param b
	 *            a <code>boolean</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(boolean)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public AppendingStringBuffer insert(final int offset, final boolean b)
	{
		return insert(offset, String.valueOf(b));
	}

	/**
	 * Inserts the string representation of the <code>char</code> argument into this string buffer.
	 * <p>
	 * The second argument is inserted into the contents of this string buffer at the position
	 * indicated by <code>offset</code>. The length of this string buffer increases by one.
	 * <p>
	 * The overall effect is exactly as if the argument were converted to a string by the method
	 * {@link String#valueOf(char)} and the character in that string were then
	 * {@link #insert(int, String) inserted} into this <code>AppendingStringBuffer</code> object at
	 * the position indicated by <code>offset</code>.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>, and less than or equal
	 * to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param c
	 *            a <code>char</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception IndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.StringBuffer#length()
	 */
	public AppendingStringBuffer insert(final int offset, final char c)
	{
		int newcount = count + 1;
		if (newcount > value.length)
		{
			expandCapacity(newcount);
		}
		System.arraycopy(value, offset, value, offset + 1, count - offset);
		value[offset] = c;
		count = newcount;
		return this;
	}

	/**
	 * Inserts the string representation of the second <code>int</code> argument into this string
	 * buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method <code>String.valueOf</code>,
	 * and the characters of that string are then inserted into this string buffer at the indicated
	 * offset.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>, and less than or equal
	 * to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param i
	 *            an <code>int</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(int)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public AppendingStringBuffer insert(final int offset, final int i)
	{
		return insert(offset, String.valueOf(i));
	}

	/**
	 * Inserts the string representation of the <code>long</code> argument into this string buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method <code>String.valueOf</code>,
	 * and the characters of that string are then inserted into this string buffer at the position
	 * indicated by <code>offset</code>.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>, and less than or equal
	 * to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param l
	 *            a <code>long</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(long)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public AppendingStringBuffer insert(final int offset, final long l)
	{
		return insert(offset, String.valueOf(l));
	}

	/**
	 * Inserts the string representation of the <code>float</code> argument into this string buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method <code>String.valueOf</code>,
	 * and the characters of that string are then inserted into this string buffer at the indicated
	 * offset.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>, and less than or equal
	 * to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param f
	 *            a <code>float</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(float)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public AppendingStringBuffer insert(final int offset, final float f)
	{
		return insert(offset, String.valueOf(f));
	}

	/**
	 * Inserts the string representation of the <code>double</code> argument into this string
	 * buffer.
	 * <p>
	 * The second argument is converted to a string as if by the method <code>String.valueOf</code>,
	 * and the characters of that string are then inserted into this string buffer at the indicated
	 * offset.
	 * <p>
	 * The offset argument must be greater than or equal to <code>0</code>, and less than or equal
	 * to the length of this string buffer.
	 * 
	 * @param offset
	 *            the offset.
	 * @param d
	 *            a <code>double</code>.
	 * @return a reference to this <code>AppendingStringBuffer</code> object.
	 * @exception StringIndexOutOfBoundsException
	 *                if the offset is invalid.
	 * @see java.lang.String#valueOf(double)
	 * @see java.lang.StringBuffer#insert(int, java.lang.String)
	 * @see java.lang.StringBuffer#length()
	 */
	public AppendingStringBuffer insert(final int offset, final double d)
	{
		return insert(offset, String.valueOf(d));
	}

	/**
	 * Returns the index within this string of the first occurrence of the specified substring. The
	 * integer returned is the smallest value <i>k</i> such that: <blockquote>
	 * 
	 * <pre>
	 *       this.toString().startsWith(str, &lt;i&gt;k&lt;/i&gt;)
	 * </pre>
	 * 
	 * </blockquote> is <code>true</code>.
	 * 
	 * @param str
	 *            any string.
	 * @return if the string argument occurs as a substring within this object, then the index of
	 *         the first character of the first such substring is returned; if it does not occur as
	 *         a substring, <code>-1</code> is returned.
	 * @exception java.lang.NullPointerException
	 *                if <code>str</code> is <code>null</code>.
	 * @since 1.4
	 */
	public int indexOf(final String str)
	{
		return indexOf(str, 0);
	}

	/**
	 * Returns the index within this string of the first occurrence of the specified substring,
	 * starting at the specified index. The integer returned is the smallest value <tt>k</tt> for
	 * which: <blockquote>
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
	 * @return the index within this string of the first occurrence of the specified substring,
	 *         starting at the specified index.
	 * @exception java.lang.NullPointerException
	 *                if <code>str</code> is <code>null</code>.
	 * @since 1.4
	 */
	public int indexOf(final String str, final int fromIndex)
	{
		return indexOf(value, 0, count, str.toCharArray(), 0, str.length(), fromIndex);
	}

	static int indexOf(final char[] source, final int sourceOffset, final int sourceCount,
		final char[] target, final int targetOffset, final int targetCount, int fromIndex)
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
			while ((i <= max) && (source[i] != first))
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
	 * Returns the index within this string of the rightmost occurrence of the specified substring.
	 * The rightmost empty string "" is considered to occur at the index value
	 * <code>this.length()</code>. The returned index is the largest value <i>k</i> such that
	 * <blockquote>
	 * 
	 * <pre>
	 * this.toString().startsWith(str, k)
	 * </pre>
	 * 
	 * </blockquote> is true.
	 * 
	 * @param str
	 *            the substring to search for.
	 * @return if the string argument occurs one or more times as a substring within this object,
	 *         then the index of the first character of the last such substring is returned. If it
	 *         does not occur as a substring, <code>-1</code> is returned.
	 * @exception java.lang.NullPointerException
	 *                if <code>str</code> is <code>null</code>.
	 * @since 1.4
	 */
	public int lastIndexOf(final String str)
	{
		return lastIndexOf(str, count);
	}

	/**
	 * Returns the index within this string of the last occurrence of the specified substring. The
	 * integer returned is the largest value <i>k</i> such that: <blockquote>
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
	 * @return the index within this string of the last occurrence of the specified substring.
	 * @exception java.lang.NullPointerException
	 *                if <code>str</code> is <code>null</code>.
	 * @since 1.4
	 */
	public int lastIndexOf(final String str, final int fromIndex)
	{
		return lastIndexOf(value, 0, count, str.toCharArray(), 0, str.length(), fromIndex);
	}

	static int lastIndexOf(final char[] source, final int sourceOffset, final int sourceCount,
		final char[] target, final int targetOffset, final int targetCount, int fromIndex)
	{
		/*
		 * Check arguments; return immediately where possible. For consistency, don't check for null
		 * str.
		 */
		int rightIndex = sourceCount - targetCount;
		if (fromIndex < 0)
		{
			return -1;
		}
		if (fromIndex > rightIndex)
		{
			fromIndex = rightIndex;
		}
		/* Empty string always matches. */
		if (targetCount == 0)
		{
			return fromIndex;
		}

		int strLastIndex = targetOffset + targetCount - 1;
		char strLastChar = target[strLastIndex];
		int min = sourceOffset + targetCount - 1;
		int i = min + fromIndex;

		startSearchForLastChar : while (true)
		{
			while ((i >= min) && (source[i] != strLastChar))
			{
				i--;
			}
			if (i < min)
			{
				return -1;
			}
			int j = i - 1;
			int start = j - (targetCount - 1);
			int k = strLastIndex - 1;

			while (j > start)
			{
				if (source[j--] != target[k--])
				{
					i--;
					continue startSearchForLastChar;
				}
			}
			return start - sourceOffset + 1;
		}
	}

	/**
	 * Tests if this AppendingStringBuffer starts with the specified prefix beginning a specified
	 * index.
	 * 
	 * @param prefix
	 *            the prefix.
	 * @param toffset
	 *            where to begin looking in the string.
	 * @return <code>true</code> if the character sequence represented by the argument is a prefix
	 *         of the substring of this object starting at index <code>toffset</code>;
	 *         <code>false</code> otherwise. The result is <code>false</code> if
	 *         <code>toffset</code> is negative or greater than the length of this
	 *         <code>String</code> object; otherwise the result is the same as the result of the
	 *         expression
	 * 
	 *         <pre>
	 * this.subString(toffset).startsWith(prefix)
	 * </pre>
	 */
	public boolean startsWith(final CharSequence prefix, final int toffset)
	{
		char ta[] = value;
		int to = toffset;
		int po = 0;
		int pc = prefix.length();
		// Note: toffset might be near -1>>>1.
		if ((toffset < 0) || (toffset > count - pc))
		{
			return false;
		}
		while (--pc >= 0)
		{
			if (ta[to++] != prefix.charAt(po++))
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Tests if this AppendingStringBuffer starts with the specified prefix.
	 * 
	 * @param prefix
	 *            the prefix.
	 * @return <code>true</code> if the character sequence represented by the argument is a prefix
	 *         of the character sequence represented by this AppendingStringBuffer;
	 *         <code>false</code> otherwise. Note also that <code>true</code> will be returned if
	 *         the argument is an empty string or is equal to this
	 *         <code>AppendingStringBuffer</code> object as determined by the
	 *         {@link #equals(Object)} method.
	 * @since 1. 0
	 */
	public boolean startsWith(final CharSequence prefix)
	{
		return startsWith(prefix, 0);
	}

	/**
	 * Tests if this AppendingStringBuffer ends with the specified suffix.
	 * 
	 * @param suffix
	 *            the suffix.
	 * @return <code>true</code> if the character sequence represented by the argument is a suffix
	 *         of the character sequence represented by this AppendingStringBuffer;
	 *         <code>false</code> otherwise. Note that the result will be <code>true</code> if the
	 *         argument is the empty string or is equal to this <code>AppendingStringBuffer</code>
	 *         object as determined by the {@link #equals(Object)} method.
	 */
	public boolean endsWith(final CharSequence suffix)
	{
		return startsWith(suffix, count - suffix.length());
	}

	/**
	 * Converts to a string representing the data in this AppendingStringBuffer. A new
	 * <code>String</code> object is allocated and initialized to contain the character sequence
	 * currently represented by this string buffer. This <code>String</code> is then returned.
	 * Subsequent changes to the string buffer do not affect the contents of the <code>String</code>
	 * .
	 * <p>
	 * Implementation advice: This method can be coded so as to create a new <code>String</code>
	 * object without allocating new memory to hold a copy of the character sequence. Instead, the
	 * string can share the memory used by the string buffer. Any subsequent operation that alters
	 * the content or capacity of the string buffer must then make a copy of the internal buffer at
	 * that time. This strategy is effective for reducing the amount of memory allocated by a string
	 * concatenation operation when it is implemented using a string buffer.
	 * 
	 * @return a string representation of the string buffer.
	 */
	@Override
	public String toString()
	{
		return new String(value, 0, count);
	}

	/**
	 * This method returns the internal char array. So it is not
	 * 
	 * @return The internal char array
	 */
	public final char[] getValue()
	{
		return value;
	}


	/**
	 * readObject is called to restore the state of the AppendingStringBuffer from a stream.
	 * 
	 * @param s
	 * @throws ClassNotFoundException
	 * @throws IOException
	 */
	private void readObject(final java.io.ObjectInputStream s) throws IOException,
		ClassNotFoundException
	{
		s.defaultReadObject();
		value = value.clone();
	}

	/**
	 * Compares this AppendingStringBuffer to the specified object. The result is <code>true</code>
	 * if and only if the argument is not <code>null</code> and is a
	 * <code>AppendingStringBuffer</code> object or another charsequence object! that represents the
	 * same sequence of characters as this object.
	 * 
	 * @param anObject
	 *            the object to compare this <code>AppendingStringBuffer</code> against.
	 * @return <code>true</code> if the <code>AppendingStringBuffer</code>are equal;
	 *         <code>false</code> otherwise.
	 */
	@Override
	public boolean equals(final Object anObject)
	{
		if (this == anObject)
		{
			return true;
		}
		if (anObject instanceof AppendingStringBuffer)
		{
			AppendingStringBuffer anotherString = (AppendingStringBuffer)anObject;
			int n = count;
			if (n == anotherString.count)
			{
				char v1[] = value;
				char v2[] = anotherString.value;
				int i = 0;
				while (n-- != 0)
				{
					if (v1[i] != v2[i++])
					{
						return false;
					}
				}
				return true;
			}
		}
		else if (anObject instanceof CharSequence)
		{
			CharSequence sequence = (CharSequence)anObject;
			int n = count;
			if (sequence.length() == count)
			{
				char v1[] = value;
				int i = 0;
				while (n-- != 0)
				{
					if (v1[i] != sequence.charAt(i++))
					{
						return false;
					}
				}
				return true;
			}
		}
		return false;
	}

	/**
	 * Returns a hash code for this AppendingStringBuffer. The hash code for a
	 * <code>AppendingStringBuffer</code> object is computed as <blockquote>
	 * 
	 * <pre>
	 *    s[0]*31&circ;(n-1) + s[1]*31&circ;(n-2) + ... + s[n-1]
	 * </pre>
	 * 
	 * </blockquote> using <code>int</code> arithmetic, where <code>s[i]</code> is the <i>i</i>th
	 * character of the AppendingStringBuffer, <code>n</code> is the length of the
	 * AppendingStringBuffer, and <code>^</code> indicates exponentiation. (The hash value of the
	 * empty AppendingStringBuffer is zero.)
	 * 
	 * @return a hash code value for this object.
	 */
	@Override
	public int hashCode()
	{
		int h = 0;
		if (h == 0)
		{
			int off = 0;
			char val[] = value;
			int len = count;

			for (int i = 0; i < len; i++)
			{
				h = 31 * h + val[off++];
			}
		}
		return h;
	}

	/**
	 * Clears the buffer contents, but leaves the allocated size intact
	 */
	public void clear()
	{
		count = 0;
	}
}