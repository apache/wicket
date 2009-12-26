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
package org.apache.wicket.ng.protocol.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.AppendingStringBuffer;


/**
 * Subclass of WebResponse which buffers output and any redirection.
 * 
 * @author Jonathan Locke
 */
public class BufferedWebResponse extends WebResponse
{
	private final List<CookieEntry> cookieEntries = new ArrayList<CookieEntry>();

	private Long contentLength;

	private String contentType;

	private final Map<String, Long> dateHeaders = new HashMap<String, Long>();

	private final Map<String, String> headers = new HashMap<String, String>();

	private StringBuilder builder;

	private ByteArrayOutputStream stream;

	private String redirectUrl;

	private Integer statusCode;

	/**
	 * 
	 */
	private static class CookieEntry
	{
		Cookie cookie;
		boolean add;
	}

	/**
	 * Construct.
	 */
	public BufferedWebResponse()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param httpServletResponse
	 */
	public BufferedWebResponse(HttpServletResponse httpServletResponse)
	{
		super(httpServletResponse);
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebResponse#addCookie(javax.servlet.http.Cookie)
	 */
	@Override
	public void addCookie(Cookie cookie)
	{
		CookieEntry entry = new CookieEntry();
		entry.cookie = cookie;
		entry.add = true;
		cookieEntries.add(entry);
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebResponse#clearCookie(javax.servlet.http.Cookie)
	 */
	@Override
	public void clearCookie(Cookie cookie)
	{
		CookieEntry entry = new CookieEntry();
		entry.cookie = cookie;
		entry.add = false;
		cookieEntries.add(entry);
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebResponse#setContentLength(long)
	 */
	@Override
	public void setContentLength(long length)
	{
		contentLength = length;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebResponse#setContentType(java.lang.String)
	 */
	@Override
	public void setContentType(String mimeType)
	{
		contentType = mimeType;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebResponse#setDateHeader(java.lang.String, long)
	 */
	@Override
	public void setDateHeader(String name, long date)
	{
		dateHeaders.put(name, date);
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebResponse#setHeader(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void setHeader(String name, String value)
	{
		headers.put(name, value);
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebResponse#write(java.lang.CharSequence)
	 */
	@Override
	public void write(CharSequence sequence)
	{
		if (stream != null)
		{
			throw new IllegalStateException(
				"Can't call write(CharSequence) after write(byte[]) has been called.");
		}

		if (builder == null)
		{
			builder = new StringBuilder(4096);
		}

		builder.append(sequence);
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebResponse#write(byte[])
	 */
	@Override
	public void write(byte[] array)
	{
		if (builder != null)
		{
			throw new IllegalStateException(
				"Can't call write(byte[]) after write(CharSequence) has been called.");
		}
		if (stream == null)
		{
			stream = new ByteArrayOutputStream(array.length);
		}
		try
		{
			stream.write(array);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebResponse#redirect(java.lang.String)
	 */
	@Override
	public void redirect(String url)
	{
		redirectUrl = url;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebResponse#setStatus(int)
	 */
	@Override
	public void setStatus(int sc)
	{
		statusCode = sc;
	}

	/**
	 * Writes the content of the buffer to the specified response. Also sets the properties and and
	 * headers.
	 * 
	 * @param response
	 */
	public void writeTo(final WebResponse response)
	{
		Checks.argumentNotNull(response, "response");

		for (CookieEntry e : cookieEntries)
		{
			if (e.add)
			{
				response.addCookie(e.cookie);
			}
			else
			{
				response.clearCookie(e.cookie);
			}
		}
		if (contentLength != null)
		{
			response.setContentLength(contentLength);
		}
		if (contentType != null)
		{
			response.setContentType(contentType);
		}
		for (String s : dateHeaders.keySet())
		{
			response.setDateHeader(s, dateHeaders.get(s));
		}
		for (String s : headers.keySet())
		{
			response.setHeader(s, headers.get(s));
		}
		if (statusCode != null)
		{
			response.setStatus(statusCode);
		}
		if (redirectUrl != null)
		{
			response.redirect(redirectUrl);
		}
		if (builder != null)
		{
			response.write(builder);
		}
		else if (stream != null)
		{
			final boolean copied[] = { false };
			try
			{
				// try to avoid copying the array
				stream.writeTo(new OutputStream()
				{
					@Override
					public void write(int b) throws IOException
					{

					}

					@Override
					public void write(byte[] b, int off, int len) throws IOException
					{
						if (off == 0 && len == b.length)
						{
							response.write(b);
							copied[0] = true;
						}
					}
				});
			}
			catch (IOException e1)
			{
				throw new WicketRuntimeException(e1);
			}
			if (copied[0] == false)
			{
				response.write(stream.toByteArray());
			}
		}
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API.
	 */
	public final void filter()
	{
		if (redirectUrl == null && builder.length() != 0)
		{ // TODO WICKET-NG clean up this conversion
			builder = new StringBuilder(
				filter(new AppendingStringBuffer(builder.toString())).toString());
		}
	}
}
