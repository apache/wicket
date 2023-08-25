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
package org.apache.wicket.response;

import org.apache.wicket.request.Response;
import org.apache.wicket.util.string.AppendingStringBuffer;


/**
 * Response object that writes to an AppendingStringBuffer. This class is functionally equivalent to
 * {@link StringResponse}, but defers creating the buffer until it is needed.
 * 
 * @author Thomas Heigl
 * @deprecated In Wicket 10 {@link StringResponse} will be made lazy and this class will be removed
 */
@Deprecated(since = "9.13.0", forRemoval = true)
public class LazyStringResponse extends Response
{

	private static final int DEFAULT_INITIAL_CAPACITY = 128;

	/** Initial capacity of the buffer */
	private final int initialCapacity;

	/** Buffer to write to */
	private AppendingStringBuffer out;

	public LazyStringResponse()
	{
		this(DEFAULT_INITIAL_CAPACITY);
	}

	public LazyStringResponse(int initialCapacity)
	{
		this.initialCapacity = initialCapacity;
	}

	/**
	 * @see Response#write(CharSequence)
	 */
	@Override
	public void write(final CharSequence string)
	{
		if (out == null)
		{
			out = new AppendingStringBuffer(initialCapacity);
		}
		out.append(string);
	}

	/**
	 * @see Response#reset()
	 */
	@Override
	public void reset()
	{
		if (out != null)
		{
			out.clear();
		}
	}

	/**
	 * @see Object#toString()
	 */
	@Override
	public String toString()
	{
		return getBuffer().toString();
	}

	/**
	 * @return The internal buffer as a {@link CharSequence} or an empty string if no content has
	 *         been written to the response
	 */
	public CharSequence getBuffer()
	{
		return out != null ? out : "";
	}

	@Override
	public void write(byte[] array)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public void write(byte[] array, int offset, int length)
	{
		throw new UnsupportedOperationException();
	}

	@Override
	public String encodeURL(CharSequence url)
	{
		return url != null ? url.toString() : null;
	}

	@Override
	public Object getContainerResponse()
	{
		return null;
	}
}
