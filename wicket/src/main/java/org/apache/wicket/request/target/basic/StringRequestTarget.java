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
package org.apache.wicket.request.target.basic;

import java.io.OutputStream;
import java.nio.charset.Charset;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Response;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.resource.StringBufferResourceStream;
import org.apache.wicket.util.string.Strings;


/**
 * Request target that responds by sending its string property.
 * 
 * @author Eelco Hillenius
 */
public class StringRequestTarget implements IRequestTarget
{
	/** the string for the response. */
	private final String string;

	/** content type for the string */
	private final String contentType;

	/** charset of the string */
	private final Charset charset;


	/**
	 * Creates a string request target with content type <code>text/plain</code> and default charset
	 * (usually UTF-8)
	 * 
	 * @param string
	 *            the string for the response
	 */
	public StringRequestTarget(String string)
	{
		this("text/plain", Charset.defaultCharset(), string);
	}


	/**
	 * Constructor
	 * 
	 * @param contentType
	 *            content type of the data the string represents eg
	 *            <code>text/html; charset=utf-8</code>
	 * @param charset
	 *            charset to use
	 * @param string
	 *            string for the response
	 */
	public StringRequestTarget(String contentType, Charset charset, String string)
	{
		if (string == null)
		{
			throw new IllegalArgumentException("Argument string must be not null");
		}
		if (Strings.isEmpty(contentType))
		{
			throw new IllegalArgumentException("Argument contentType must not be null or empty");
		}
		if (charset == null)
		{
			throw new IllegalArgumentException("Argument charset must not be null");
		}
		this.contentType = contentType;
		this.string = string;
		this.charset = charset;
	}


	/**
	 * Responds by sending the string property.
	 * 
	 * @see org.apache.wicket.IRequestTarget#respond(org.apache.wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		// Get servlet response to use when responding with resource
		final Response response = requestCycle.getResponse();
		response.setContentType(contentType);
		final StringBufferResourceStream stream = new StringBufferResourceStream(contentType);
		stream.setCharset(charset);
		stream.append(string);

		// Respond with resource
		try
		{
			final OutputStream out = response.getOutputStream();
			try
			{
				Streams.copy(stream.getInputStream(), out);
			}
			finally
			{
				stream.close();
				out.flush();
			}
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Unable to render resource stream " + stream, e);
		}
	}

	/**
	 * @see org.apache.wicket.IRequestTarget#detach(org.apache.wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
	}

	/**
	 * Gets the string property.
	 * 
	 * @return the string property
	 */
	public String getString()
	{
		return string;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof StringRequestTarget)
		{
			StringRequestTarget that = (StringRequestTarget)obj;
			return string.equals(that.string);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "StringRequestTarget".hashCode();
		result += string.hashCode();
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[StringRequestTarget@" + hashCode() + " " + string + "]";
	}
}
