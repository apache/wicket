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
package org.apache._wicket.request.response;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.wicket.WicketRuntimeException;

/**
 * Subclass of {@link WebResponse} that buffers the actions and performs those on another response.
 * 
 * @see #writeTo(WebResponse)
 * 
 * @author Matej Knopp
 */
public abstract class BufferedWebResponse extends WebResponse
{

	/**
	 * Construct.
	 */
	public BufferedWebResponse()
	{
	}

	private static class CookieEntry
	{
		Cookie cookie;
		boolean add;
	}

	private List<CookieEntry> cookieEntries = new ArrayList<CookieEntry>();

	@Override
	public void addCookie(Cookie cookie)
	{
		CookieEntry entry = new CookieEntry();
		entry.cookie = cookie;
		entry.add = true;
		cookieEntries.add(entry);
	}

	@Override
	public void clearCookie(Cookie cookie)
	{
		CookieEntry entry = new CookieEntry();
		entry.cookie = cookie;
		entry.add = false;
		cookieEntries.add(entry);
	}

	private Long contentLength = null;

	@Override
	public void setContentLength(long length)
	{
		contentLength = length;
	}

	private String contentType = null;

	@Override
	public void setContentType(String mimeType)
	{
		contentType = mimeType;
	}

	private Map<String, Long> dateHeaders = new HashMap<String, Long>();

	@Override
	public void setDateHeader(String name, long date)
	{
		dateHeaders.put(name, date);
	}

	private Map<String, String> headers = new HashMap<String, String>();

	@Override
	public void setHeader(String name, String value)
	{
		headers.put(name, value);
	}

	private StringBuilder builder;
	private ByteArrayOutputStream stream;

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

	private String redirectUrl = null;

	@Override
	public void sendRedirect(String url)
	{
		this.redirectUrl = url;
	}

	private Integer statusCode = null;

	@Override
	public void setStatus(int sc)
	{
		this.statusCode = sc;
	}

	/**
	 * Writes the content of the buffer to the specified response. Also sets the properties and and
	 * headers.
	 * 
	 * @param response
	 */
	public void writeTo(final WebResponse response)
	{
		if (response == null)
		{
			throw new IllegalArgumentException("Argument 'response' may not be null.");
		}
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
			response.sendRedirect(redirectUrl);
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
}
