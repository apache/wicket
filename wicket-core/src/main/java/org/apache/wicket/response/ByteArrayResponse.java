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

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.Response;


/**
 * Response used to capture output as a byte array
 * 
 * @author igor.vaynberg
 */
public class ByteArrayResponse extends Response
{

	private ByteArrayOutputStream bytes;
	private Response original;

	/**
	 * Constructor
	 * 
	 * @param original
	 */
	public ByteArrayResponse(Response original)
	{
		this.original = original;
		reset();
	}

	/**
	 * Constructor
	 */
	public ByteArrayResponse()
	{
		this(null);
	}

	/**
	 * @return bytes
	 */
	public byte[] getBytes()
	{
		return bytes.toByteArray();
	}

	/**
	 * @see org.apache.wicket.request.Response#write(CharSequence)
	 */
	@Override
	public void write(final CharSequence string)
	{
		try
		{
			bytes.write(string.toString().getBytes());
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Cannot write into internal byte stream", e);
		}
	}

	/**
	 * @see org.apache.wicket.request.Response#reset()
	 */
	@Override
	public void reset()
	{
		bytes = new ByteArrayOutputStream();
	}


	/**
	 * @see org.apache.wicket.request.Response#getOutputStream()
	 */
	@Override
	public void write(byte[] array)
	{
		try
		{
			bytes.write(array);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Cannot write into internal byte stream", e);
		}
	}

	@Override
	public void write(byte[] array, int offset, int length)
	{
		try
		{
			bytes.write(array, offset, length);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Cannot write into internal byte stream", e);
		}

	}

	@Override
	public String encodeURL(CharSequence url)
	{
		if (original != null)
		{
			return original.encodeURL(url);
		}
		else
		{
			return url != null ? url.toString() : null;
		}
	}

	@Override
	public Object getContainerResponse()
	{
		return original.getContainerResponse();
	}
}
