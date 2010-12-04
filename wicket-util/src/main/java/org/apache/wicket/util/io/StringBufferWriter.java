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
package org.apache.wicket.util.io;

import java.io.Writer;

import org.apache.wicket.util.string.AppendingStringBuffer;


/**
 * @author jcompagner
 */
public class StringBufferWriter extends Writer
{
	/** The buffer which holds the chars */
	private AppendingStringBuffer buffer;

	/**
	 * Constructor
	 */
	public StringBufferWriter()
	{
		buffer = new AppendingStringBuffer(4096);
	}

	/**
	 * @return The AppendingStringBuffer with the written data
	 */
	public AppendingStringBuffer getStringBuffer()
	{
		return buffer;
	}

	/**
	 * @param buffer
	 */
	public void setStringBuffer(final AppendingStringBuffer buffer)
	{
		this.buffer = buffer;
	}

	/**
	 * Writers the char to the buffer
	 * 
	 * @param ch
	 */
	public void write(final char ch)
	{
		buffer.append(ch);
	}

	/**
	 * @see java.io.Writer#write(char[])
	 */
	@Override
	public void write(final char charArray[])
	{
		buffer.append(charArray);
	}

	/**
	 * @see java.io.Writer#write(char[], int, int)
	 */
	@Override
	public void write(final char charArray[], final int offset, final int length)
	{
		buffer.append(charArray, offset, length);
	}

	/**
	 * @see java.io.Writer#write(java.lang.String)
	 */
	@Override
	public void write(final String string)
	{
		buffer.append(string);
	}

	/**
	 * @see java.io.Writer#write(java.lang.String, int, int)
	 */
	@Override
	public void write(final String string, final int offset, final int length)
	{
		buffer.append(string.substring(offset, offset + length));
	}

	/**
	 * @see java.io.Writer#flush()
	 */
	@Override
	public void flush()
	{
	}

	/**
	 * resets the buffer.
	 */
	public void reset()
	{
		buffer.setLength(0);
	}

	/**
	 * @see java.io.Writer#close()
	 */
	@Override
	public void close()
	{
	}

}
