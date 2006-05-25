/*
 * $Id$ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.string;

/**
 * This is a prepending stringbuffer optimized for constantly prepending strings
 * to the front of the buffer.
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
	public PrependingStringBuffer(int size)
	{
		this.buffer = new char[size];
		this.position = size;
		this.size = 0;
	}

	/**
	 * Constructs and direct inserts the given string. The buffer size will be
	 * string.length+16
	 * 
	 * @param start
	 *            The string that is directly inserted.
	 */
	public PrependingStringBuffer(String start)
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
	public PrependingStringBuffer prepend(char ch)
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
	public PrependingStringBuffer prepend(String str)
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

	private void expandCapacity(int minimumCapacity)
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
	 * Retuns the size of this PrependingStringBuffer
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
}