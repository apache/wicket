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

/**
 * This is a prepending stringbuffer optimized for constantly prepending strings to the front of the
 * buffer.
 * 
 * @author jcompagner
 */
public class PrependingStringBuffer
{
	private int size;
	private int position;

	private char[] buffer;

	/**
	 * Default constructor, the internal initial buffer size will be 16
	 */
	public PrependingStringBuffer()
	{
		this(16);
	}

	/**
	 * Constructs this PrependingStringBuffer with the given buffer size.
	 * 
	 * @param size
	 *            The initial size of the buffer.
	 */
	public PrependingStringBuffer(final int size)
	{
		buffer = new char[size];
		position = size;
		this.size = 0;
	}

	/**
	 * Constructs and direct inserts the given string. The buffer size will be string.length+16
	 * 
	 * @param start
	 *            The string that is directly inserted.
	 */
	public PrependingStringBuffer(final String start)
	{
		this(start.length() + 16);
		prepend(start);
	}

	/**
	 * Prepends one char to this PrependingStringBuffer
	 * 
	 * @param ch
	 *            The char that will be prepended
	 * @return this
	 */
	public PrependingStringBuffer prepend(final char ch)
	{
		int len = 1;
		if (position < len)
		{
			expandCapacity(size + len);
		}
		position -= len;
		buffer[position] = ch;
		size += len;
		return this;
	}

	/**
	 * Prepends the string to this PrependingStringBuffer
	 * 
	 * @param str
	 *            The string that will be prepended
	 * @return this
	 */
	public PrependingStringBuffer prepend(final String str)
	{
		int len = str.length();
		if (position < len)
		{
			expandCapacity(size + len);
		}
		str.getChars(0, len, buffer, position - len);
		position -= len;
		size += len;
		return this;
	}

	private void expandCapacity(final int minimumCapacity)
	{
		int newCapacity = (buffer.length + 1) * 2;
		if (newCapacity < 0)
		{
			newCapacity = Integer.MAX_VALUE;
		}
		else if (minimumCapacity > newCapacity)
		{
			newCapacity = minimumCapacity;
		}

		char newValue[] = new char[newCapacity];
		System.arraycopy(buffer, position, newValue, newCapacity - size, size);
		buffer = newValue;
		position = newCapacity - size;
	}

	/**
	 * Returns the size of this PrependingStringBuffer
	 * 
	 * @return The size
	 */
	public int length()
	{
		return size;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return new String(buffer, position, size);
	}

	@Override
	public boolean equals(final Object obj)
	{
		if (obj == this)
		{
			return true;
		}
		else if (obj == null)
		{
			return false;
		}
		else
		{
			return toString().equals(obj.toString());
		}
	}

	@Override
	public int hashCode()
	{
		return toString().hashCode();
	}
}