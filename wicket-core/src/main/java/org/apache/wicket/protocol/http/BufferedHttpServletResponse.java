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
package org.apache.wicket.protocol.http;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.collections.MultiMap;
import org.apache.wicket.util.io.StringBufferWriter;
import org.apache.wicket.util.string.AppendingStringBuffer;


/**
 * Implementation of {@link HttpServletResponse} that saves the output in a string buffer. This is
 * used in REDIRECT_TO_BUFFER render strategy to create the buffer of the output that can be held on
 * to until the redirect part of the render strategy.
 * 
 * @author jcompagner
 */
class BufferedHttpServletResponse implements HttpServletResponse
{
	/** the print writer for the response */
	private StringBufferWriter sbw = new StringBufferWriter();
	private PrintWriter pw = new PrintWriter(sbw);

	/** cookies list */
	private List<Cookie> cookies;

	/** status code */
	private int status = -1;

	/** headers map */
	private MultiMap<String, Object> headers;

	/** the real response for encoding the url */
	private HttpServletResponse realResponse;

	private String redirect;
	private String contentType;
	private byte[] byteBuffer;
	private Locale locale;
	private String encoding;

	/**
	 * Constructor.
	 * 
	 * @param realResponse
	 *            The real response for encoding the url
	 */
	public BufferedHttpServletResponse(HttpServletResponse realResponse)
	{
		this.realResponse = realResponse;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#addCookie(javax.servlet.http.Cookie)
	 */
	@Override
	public void addCookie(Cookie cookie)
	{
		isOpen();
		if (cookies == null)
		{
			cookies = new ArrayList<Cookie>(2);
		}
		cookies.add(cookie);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#containsHeader(java.lang.String)
	 */
	@Override
	public boolean containsHeader(String name)
	{
		isOpen();
		if (headers == null)
		{
			return false;
		}
		return headers.containsKey(name);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeURL(java.lang.String)
	 */
	@Override
	public String encodeURL(String url)
	{
		isOpen();
		return realResponse.encodeURL(url);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectURL(java.lang.String)
	 */
	@Override
	public String encodeRedirectURL(String url)
	{
		isOpen();
		return realResponse.encodeRedirectURL(url);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeUrl(java.lang.String)
	 * @deprecated
	 */
	@Override
	@Deprecated
	public String encodeUrl(String url)
	{
		isOpen();
		return realResponse.encodeURL(url);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#encodeRedirectUrl(java.lang.String)
	 * @deprecated
	 */
	@Override
	@Deprecated
	public String encodeRedirectUrl(String url)
	{
		isOpen();
		return realResponse.encodeRedirectURL(url);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendError(int, java.lang.String)
	 */
	@Override
	public void sendError(int sc, String msg) throws IOException
	{
		isOpen();
		realResponse.sendError(sc, msg);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendError(int)
	 */
	@Override
	public void sendError(int sc) throws IOException
	{
		isOpen();
		realResponse.sendError(sc);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#sendRedirect(java.lang.String)
	 */
	@Override
	public void sendRedirect(String location) throws IOException
	{
		isOpen();
		redirect = location;
	}

	/**
	 * @return The redirect url
	 */
	public String getRedirectUrl()
	{
		isOpen();
		return redirect;
	}


	private void testAndCreateHeaders()
	{
		isOpen();
		if (headers == null)
		{
			headers = new MultiMap<String, Object>();
		}
	}

	private void isOpen()
	{
		if (realResponse == null)
		{
			throw new WicketRuntimeException("the buffered servlet response already closed.");
		}
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setDateHeader(java.lang.String, long)
	 */
	@Override
	public void setDateHeader(String name, long date)
	{
		testAndCreateHeaders();
		headers.replaceValues(name, date);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#addDateHeader(java.lang.String, long)
	 */
	@Override
	public void addDateHeader(String name, long date)
	{
		testAndCreateHeaders();
		headers.addValue(name, date);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void setHeader(String name, String value)
	{
		testAndCreateHeaders();
		headers.replaceValues(name, value);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#addHeader(java.lang.String, java.lang.String)
	 */
	@Override
	public void addHeader(String name, String value)
	{
		testAndCreateHeaders();
		headers.addValue(name, value);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setIntHeader(java.lang.String, int)
	 */
	@Override
	public void setIntHeader(String name, int value)
	{
		testAndCreateHeaders();
		headers.replaceValues(name, value);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#addIntHeader(java.lang.String, int)
	 */
	@Override
	public void addIntHeader(String name, int value)
	{
		testAndCreateHeaders();
		headers.addValue(name, value);
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 */
	@Override
	public void setStatus(int statusCode)
	{
		status = statusCode;
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int, java.lang.String)
	 * @deprecated use setStatus(int) instead
	 */
	@Override
	@Deprecated
	public void setStatus(int sc, String sm)
	{
		setStatus(sc);
	}

	/**
	 * @see javax.servlet.ServletResponse#getCharacterEncoding()
	 */
	@Override
	public String getCharacterEncoding()
	{
		isOpen();
		return encoding;
	}

	/**
	 * Set the character encoding to use for the output.
	 * 
	 * @param encoding
	 */
	@Override
	public void setCharacterEncoding(String encoding)
	{
		this.encoding = encoding;
	}

	/**
	 * @see javax.servlet.ServletResponse#getOutputStream()
	 */
	@Override
	public ServletOutputStream getOutputStream() throws IOException
	{
		throw new UnsupportedOperationException("Cannot get output stream on BufferedResponse");
	}

	/**
	 * @see javax.servlet.ServletResponse#getWriter()
	 */
	@Override
	public PrintWriter getWriter() throws IOException
	{
		isOpen();
		return pw;
	}

	/**
	 * @see javax.servlet.ServletResponse#setContentLength(int)
	 */
	@Override
	public void setContentLength(int len)
	{
		isOpen();
		// ignored will be calculated when the buffer is really streamed.
	}

	/**
	 * @see javax.servlet.ServletResponse#setContentType(java.lang.String)
	 */
	@Override
	public void setContentType(String type)
	{
		isOpen();
		contentType = type;
	}

	/**
	 * @return The content type
	 */
	@Override
	public String getContentType()
	{
		return contentType;
	}

	/**
	 * @see javax.servlet.ServletResponse#setBufferSize(int)
	 */
	@Override
	public void setBufferSize(int size)
	{
		isOpen();
		// ignored every thing will be buffered
	}

	/**
	 * @see javax.servlet.ServletResponse#getBufferSize()
	 */
	@Override
	public int getBufferSize()
	{
		isOpen();
		return Integer.MAX_VALUE;
	}

	/**
	 * @see javax.servlet.ServletResponse#flushBuffer()
	 */
	@Override
	public void flushBuffer() throws IOException
	{
		isOpen();
	}

	/**
	 * @see javax.servlet.ServletResponse#resetBuffer()
	 */
	@Override
	public void resetBuffer()
	{
		isOpen();
		sbw.reset();
	}

	/**
	 * @see javax.servlet.ServletResponse#isCommitted()
	 */
	@Override
	public boolean isCommitted()
	{
		return pw == null;
	}

	/**
	 * @see javax.servlet.ServletResponse#reset()
	 */
	@Override
	public void reset()
	{
		resetBuffer();
		headers = null;
		cookies = null;
	}

	/**
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 */
	@Override
	public void setLocale(Locale loc)
	{
		isOpen();
		locale = loc;
	}

	/**
	 * @see javax.servlet.ServletResponse#getLocale()
	 */
	@Override
	public Locale getLocale()
	{
		isOpen();
		if (locale == null)
		{
			return realResponse.getLocale();
		}
		return locale;
	}

	/**
	 * @return The length of the complete string buffer
	 */
	public int getContentLength()
	{
		isOpen();
		return sbw.getStringBuffer().length();
	}

	/**
	 * 
	 */
	public void close()
	{
		isOpen();
		pw.close();
		byteBuffer = convertToCharset(sbw.getStringBuffer(), getCharacterEncoding());

		pw = null;
		sbw = null;
		realResponse = null;
	}

	/**
	 * Convert the string into the output encoding required
	 * 
	 * @param output
	 * 
	 * @param encoding
	 *            The output encoding
	 * @return byte[] The encoded characters converted into bytes
	 */
	private static byte[] convertToCharset(final AppendingStringBuffer output, final String encoding)
	{
		if (encoding == null)
		{
			throw new WicketRuntimeException("Internal error: encoding must not be null");
		}

		final ByteArrayOutputStream baos = new ByteArrayOutputStream((int)(output.length() * 1.2));

		final OutputStreamWriter osw;
		final byte[] bytes;
		try
		{
			osw = new OutputStreamWriter(baos, encoding);
			osw.write(output.getValue(), 0, output.length());
			osw.close();

			bytes = baos.toByteArray();
		}
		catch (Exception ex)
		{
			throw new WicketRuntimeException("Can't convert response to charset: " + encoding, ex);
		}

		return bytes;
	}

	/**
	 * @param servletResponse
	 * @throws IOException
	 */
	public void writeTo(HttpServletResponse servletResponse) throws IOException
	{
		if (status != -1)
		{
			servletResponse.setStatus(status);
		}
		if (headers != null)
		{
			for (Entry<String, List<Object>> stringObjectEntry : headers.entrySet())
			{
				String name = stringObjectEntry.getKey();
				List<Object> values = stringObjectEntry.getValue();
				for (Object value : values)
				{
					addHeader(name, value, servletResponse);
				}
			}
		}

		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				servletResponse.addCookie(cookie);
			}
		}
		if (locale != null)
		{
			servletResponse.setLocale(locale);
		}
		// got a buffered response; now write it
		servletResponse.setContentLength(byteBuffer.length);
		servletResponse.setContentType(contentType);

		final OutputStream out = servletResponse.getOutputStream();
		out.write(byteBuffer);
		out.close();

	}

	/**
	 * @param name
	 *            Name of the header to set
	 * @param value
	 *            The value can be String/Long/Int
	 * @param servletResponse
	 *            The response to set it to.
	 */
	private static void setHeader(String name, Object value, HttpServletResponse servletResponse)
	{
		if (value instanceof String)
		{
			servletResponse.setHeader(name, (String)value);
		}
		else if (value instanceof Long)
		{
			servletResponse.setDateHeader(name, (Long)value);
		}
		else if (value instanceof Integer)
		{
			servletResponse.setIntHeader(name, (Integer)value);
		}
	}

	/**
	 * @param name
	 *            Name of the header to set
	 * @param value
	 *            The value can be String/Long/Int
	 * @param servletResponse
	 *            The response to set it to.
	 */
	private static void addHeader(String name, Object value, HttpServletResponse servletResponse)
	{
		if (value instanceof String)
		{
			servletResponse.addHeader(name, (String)value);
		}
		else if (value instanceof Long)
		{
			servletResponse.addDateHeader(name, (Long)value);
		}
		else if (value instanceof Integer)
		{
			servletResponse.addIntHeader(name, (Integer)value);
		}
	}

	/**
	 * @see javax.servlet.http.HttpServletResponse#setStatus(int)
	 * @return status
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * @see javax.servlet.http.HttpServletRequest#getHeader(java.lang.String)
	 * @param name
	 * @return the first header with name
	 */
	public String getHeader(String name)
	{
		Object value = headers.getFirstValue(name);
		if (value == null)
		{
			return null;
		}
		return value.toString();
	}

	/**
	 * @see javax.servlet.http.HttpServletRequest#getHeaders(java.lang.String)
	 * @param name
	 * @return all headers with name
	 */
	public Collection<String> getHeaders(String name)
	{
		List<Object> values = headers.get(name);
		if (values == null)
		{
			return Collections.emptyList();
		}
		List<String> ret = new ArrayList<String>(values.size());
		for (Object value : values)
		{
			ret.add(value.toString());
		}
		return ret;
	}

	/**
	 * @see javax.servlet.http.HttpServletRequest#getHeaderNames()
	 * @return all header names
	 */
	public Collection<String> getHeaderNames()
	{
		return Collections.unmodifiableCollection(headers.keySet());
	}
}