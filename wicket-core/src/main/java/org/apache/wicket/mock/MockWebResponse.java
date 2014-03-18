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
package org.apache.wicket.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import javax.servlet.http.Cookie;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.HttpHeaderCollection;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Time;

/**
 * Mocked {@link WebResponse}.
 * 
 * @author Matej Knopp
 */
public class MockWebResponse extends WebResponse
{
	/** response headers */
	private final HttpHeaderCollection headers = new HttpHeaderCollection();

	/** response cookies */
	private final List<Cookie> cookies = new ArrayList<Cookie>();

	/** url for redirection */
	private String redirectUrl;

	/** content type of response */
	private String contentType;

	/** content length of response in bytes */
	private Long contentLength;

	/** current characters in response body */
	private StringBuilder textResponse;

	/** current octets in response body */
	private ByteArrayOutputStream binaryResponse;

	/** http response status */
	private Integer status;

	/** response error message */
	private String errorMessage;

	/**
	 * Construct.
	 */
	public MockWebResponse()
	{
	}

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

	@Override
	public void sendRedirect(String url)
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

	@Override
	public void setContentLength(long length)
	{
		contentLength = length;
		setHeader("Content-Length", String.valueOf(length).intern());
	}

	/**
	 * @return content length (set by {@link #setContentLength(long)})
	 */
	public Long getContentLength()
	{
		return contentLength;
	}

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

	@Override
	public void setDateHeader(String name, Time date)
	{
		Args.notNull(date, "date");
		headers.setDateHeader(name, date);
	}

	/**
	 * @param name
	 * 
	 * @return date header with specified name
	 */
	public Time getDateHeader(String name)
	{
		final Time time = headers.getDateHeader(name);

		if (time == null)
		{
			throw new WicketRuntimeException("Date header '" + name + "' is not set.");
		}
		return time;
	}

	@Override
	public void setHeader(String name, String value)
	{
		internalSetContentType(name, value);
		headers.setHeader(name, value);
	}

	@Override
	public void addHeader(String name, String value)
	{
		internalSetContentType(name, value);
		headers.addHeader(name, value);
	}

	/**
	 * set content type if it is specified
	 * 
	 * @param name
	 *            header name
	 * @param value
	 *            header value
	 */
	private void internalSetContentType(String name, String value)
	{
		if ("Content-Type".equalsIgnoreCase(name))
		{
			if (headers.containsHeader(name) == false)
			{
				setContentType(value);
			}
		}
	}

	/**
	 * @param name
	 * 
	 * @return header string with specified name
	 */
	public String getHeader(String name)
	{
		return headers.getHeader(name);
	}

	/**
	 * @param name
	 * 
	 * @return <code>true</code> if the header was set, <code>false</code> otherwise
	 */
	public boolean hasHeader(String name)
	{
		return headers.containsHeader(name);
	}

	/**
	 * @return set of all header names
	 */
	public Set<String> getHeaderNames()
	{
		return headers.getHeaderNames();
	}

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

	@Override
	public String encodeRedirectURL(CharSequence url)
	{
		return url.toString();
	}

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
		catch (IOException ignored)
		{
		}
	}

	@Override
	public void write(byte[] array, int offset, int length)
	{
		if (textResponse != null)
		{
			throw new IllegalStateException("Text response has already been initiated.");
		}
		if (binaryResponse == null)
		{
			binaryResponse = new ByteArrayOutputStream();
		}

		binaryResponse.write(array, offset, length);
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
			byte[] bytes = binaryResponse.toByteArray();
			if (getContentLength() != null)
			{
				byte[] trimmed = new byte[getContentLength().intValue()];
				System.arraycopy(bytes, 0, trimmed, 0, getContentLength().intValue());
				return trimmed;
			}
			return bytes;
		}
	}

	@Override
	public void sendError(int sc, String msg)
	{
		status = sc;
		errorMessage = msg;
	}

	/**
	 * @return error message
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	@Override
	public void flush()
	{
	}

	@Override
	public void reset()
	{
		super.reset();
		if (binaryResponse != null)
		{
			binaryResponse = new ByteArrayOutputStream();
		}
		contentLength = null;
		contentType = null;
		if (cookies != null)
		{
			cookies.clear();
		}
		errorMessage = null;
		if (headers != null)
		{
			headers.clear();
		}
		redirectUrl = null;
		status = null;
		if (textResponse != null)
		{
			textResponse.setLength(0);
		}

	}

	@Override
	public Object getContainerResponse()
	{
		return this;
	}
}
