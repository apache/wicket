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
package org.apache.wicket.ng.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.apache.wicket.ng.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebResponse;

/**
 * Mocked {@link WebResponse2}.
 * 
 * @author Matej Knopp
 */
public class MockWebResponse extends WebResponse
{

	/**
	 * Construct.
	 */
	public MockWebResponse()
	{
	}

	private final List<Cookie> cookies = new ArrayList<Cookie>();

	@Override
	public void addCookie(Cookie cookie)
	{
		cookies.add(cookie);
	}

	@Override
	public void clearCookie(Cookie cookie)
	{
		cookies.remove(cookie);
	}

	/**
	 * @return cookies set in this response
	 */
	public List<Cookie> getCookies()
	{
		return Collections.unmodifiableList(cookies);
	}

	private String redirectUrl;

	@Override
	public void redirect(String url)
	{
		redirectUrl = url;
	}

	/**
	 * @return redirect URL or <code>null</code> if {@link #sendRedirect(String)} was not called.
	 */
	public String getRedirectUrl()
	{
		return redirectUrl;
	}

	/**
	 * @return <code>true</code> if redirect URL was set, <code>false</code> otherwise.
	 */
	@Override
	public boolean isRedirect()
	{
		return redirectUrl != null;
	}

	private Long contentLength;

	@Override
	public void setContentLength(long length)
	{
		contentLength = length;
	}

	/**
	 * @return content length (set by {@link #setContentLength(long)})
	 */
	public Long getContentLength()
	{
		return contentLength;
	}

	private String contentType;

	@Override
	public void setContentType(String mimeType)
	{
		contentType = mimeType;
	}

	/**
	 * @return content mime type
	 */
	public String getContentType()
	{
		return contentType;
	}

	private final Map<String, Object> headers = new HashMap<String, Object>();

	@Override
	public void setDateHeader(String name, long date)
	{
		headers.put(name, date);
	}

	/**
	 * @param name
	 * 
	 * @return date header with specified name
	 */
	public long getDateHeader(String name)
	{
		Object value = headers.get(name);
		if (value == null)
		{
			throw new WicketRuntimeException("Date header '" + name + "' is not set.");
		}
		else if (value instanceof Long == false)
		{
			throw new WicketRuntimeException("Header '" + name + "' is not date type.");
		}
		else
		{
			return (Long)value;
		}
	}

	@Override
	public void setHeader(String name, String value)
	{
		headers.put(name, value);
	}

	/**
	 * @param name
	 * 
	 * @return header string with specified name
	 */
	public String getHeader(String name)
	{
		Object value = headers.get(name);
		return value != null ? value.toString() : null;
	}

	/**
	 * @param name
	 * 
	 * @return <code>true</code> if the header was set, <code>false</code> otherwise
	 */
	public boolean hasHeader(String name)
	{
		return headers.containsKey(name);
	}

	/**
	 * @return set of all header names
	 */
	public Set<String> getHeaderNames()
	{
		return Collections.unmodifiableSet(headers.keySet());
	}

	Integer status;

	@Override
	public void setStatus(int sc)
	{
		status = sc;
	}

	/**
	 * @return status code or <code>null</code> if status was not set
	 */
	public Integer getStatus()
	{
		return status;
	}

	@Override
	public String encodeURL(CharSequence url)
	{
		return url.toString();
	}

	private StringBuilder textResponse;
	private ByteArrayOutputStream binaryResponse;

	@Override
	public void write(CharSequence sequence)
	{
		if (binaryResponse != null)
		{
			throw new IllegalStateException("Binary response has already been initiated.");
		}
		if (textResponse == null)
		{
			textResponse = new StringBuilder();
		}
		textResponse.append(sequence);
	}

	/**
	 * @return text response
	 */
	public CharSequence getTextResponse()
	{
		return textResponse;
	}

	@Override
	public void write(byte[] array)
	{
		if (textResponse != null)
		{
			throw new IllegalStateException("Text response has already been initiated.");
		}
		if (binaryResponse == null)
		{
			binaryResponse = new ByteArrayOutputStream();
		}
		try
		{
			binaryResponse.write(array);
		}
		catch (IOException yeahRight)
		{
		}
	}

	/**
	 * @return binary response
	 */
	public byte[] getBinaryResponse()
	{
		if (binaryResponse == null)
		{
			return null;
		}
		else
		{
			return binaryResponse.toByteArray();
		}
	}

	@Override
	public OutputStream getOutputStream()
	{
		throw new UnsupportedOperationException();
	}
}
