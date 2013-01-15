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
package org.apache.wicket.protocol.http.mock;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.protocol.http.IMetaDataBufferingWebResponse;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.value.ValueMap;


/**
 * Mock servlet response. Implements all of the methods from the standard HttpServletResponse class
 * plus helper methods to aid viewing the generated response.
 * 
 * @author Chris Turner
 */
public class MockHttpServletResponse implements HttpServletResponse, IMetaDataBufferingWebResponse
{
	private static final int MODE_BINARY = 1;

	private static final int MODE_NONE = 0;

	private static final int MODE_TEXT = 2;

	private ByteArrayOutputStream byteStream;

	private String characterEncoding = "UTF-8";

	private final List<Cookie> cookies = new ArrayList<Cookie>();

	private String errorMessage = null;

	private final ValueMap headers = new ValueMap();

	private Locale locale = null;

	private int mode = MODE_NONE;

	private PrintWriter printWriter;

	private String redirectLocation = null;

	private ServletOutputStream servletStream;

	private int status = HttpServletResponse.SC_OK;

	private StringWriter stringWriter;

	private final MockHttpServletRequest servletRequest;

	/**
	 * Create the response object.
	 * 
	 * @param servletRequest
	 */
	public MockHttpServletResponse(MockHttpServletRequest servletRequest)
	{
		this.servletRequest = servletRequest;
		initialize();
	}

	/**
	 * Add a cookie to the response.
	 * 
	 * @param cookie
	 *            The cookie to add
	 */
	@Override
	public void addCookie(final Cookie cookie)
	{
		// remove any potential duplicates
		// see http://www.ietf.org/rfc/rfc2109.txt, p.4.3.3
		Iterator<Cookie> iterator = cookies.iterator();
		while (iterator.hasNext())
		{
			Cookie old = iterator.next();
			if (cookie.getName().equals(old.getName()) &&
				((cookie.getPath() == null && old.getPath() == null) || (cookie.getPath().equals(old.getPath()))) &&
				((cookie.getDomain() == null && old.getDomain() == null) || (cookie.getDomain().equals(old.getDomain()))))
			{
				iterator.remove();
			}
		}
		cookies.add(Cookies.copyOf(cookie));
	}

	/**
	 * Add a date header.
	 * 
	 * @param name
	 *            The header value
	 * @param l
	 *            The long value
	 */
	@Override
	public void addDateHeader(String name, long l)
	{
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		addHeader(name, df.format(new Date(l)));
	}

	/**
	 * Add the given header value, including an additional entry if one already exists.
	 * 
	 * @param name
	 *            The name for the header
	 * @param value
	 *            The value for the header
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void addHeader(final String name, final String value)
	{
		List<String> list = (List<String>)headers.get(name);
		if (list == null)
		{
			list = new ArrayList<String>(1);
			headers.put(name, list);
		}
		list.add(value);
	}

	/**
	 * Add an int header value.
	 * 
	 * @param name
	 *            The header name
	 * @param i
	 *            The value
	 */
	@Override
	public void addIntHeader(final String name, final int i)
	{
		addHeader(name, "" + i);
	}

	/**
	 * Check if the response contains the given header name.
	 * 
	 * @param name
	 *            The name to check
	 * @return Whether header in response or not
	 */
	@Override
	public boolean containsHeader(final String name)
	{
		return headers.containsKey(name);
	}

	/**
	 * Encode the redirectLocation URL. Does no changes as this test implementation uses cookie
	 * based url tracking.
	 * 
	 * @param url
	 *            The url to encode
	 * @return The encoded url
	 */
	@Override
	public String encodeRedirectUrl(final String url)
	{
		return url;
	}

	/**
	 * Encode the redirectLocation URL. Does no changes as this test implementation uses cookie
	 * based url tracking.
	 * 
	 * @param url
	 *            The url to encode
	 * @return The encoded url
	 */
	@Override
	public String encodeRedirectURL(final String url)
	{
		return url;
	}

	/**
	 * Encode the URL. Does no changes as this test implementation uses cookie based url tracking.
	 * 
	 * @param url
	 *            The url to encode
	 * @return The encoded url
	 */
	@Override
	public String encodeUrl(final String url)
	{
		return url;
	}

	/**
	 * Encode the URL. Does no changes as this test implementation uses cookie based url tracking.
	 * 
	 * @param url
	 *            The url to encode
	 * @return The encoded url
	 */
	@Override
	public String encodeURL(final String url)
	{
		return url;
	}

	/**
	 * Flush the buffer.
	 * 
	 * @throws IOException
	 */
	@Override
	public void flushBuffer() throws IOException
	{
	}

	/**
	 * Get the binary content that was written to the servlet stream.
	 * 
	 * @return The binary content
	 */
	public byte[] getBinaryContent()
	{
		return byteStream.toByteArray();
	}

	/**
	 * Return the current buffer size
	 * 
	 * @return The buffer size
	 */
	@Override
	public int getBufferSize()
	{
		if (mode == MODE_NONE)
		{
			return 0;
		}
		else if (mode == MODE_BINARY)
		{
			return byteStream.size();
		}
		else
		{
			return stringWriter.getBuffer().length();
		}
	}

	/**
	 * Get the character encoding of the response.
	 * 
	 * @return The character encoding
	 */
	@Override
	public String getCharacterEncoding()
	{
		return characterEncoding;
	}


	/**
	 * Get all of the cookies that have been added to the response.
	 * 
	 * @return The collection of cookies
	 */
	public List<Cookie> getCookies()
	{
		return Cookies.copyOf(cookies);
	}

	/**
	 * Get the text document that was written as part of this response.
	 * 
	 * @return The document
	 */
	public String getDocument()
	{
		if (mode == MODE_BINARY)
		{
			return new String(byteStream.toByteArray());
		}
		else
		{
			return stringWriter.getBuffer().toString();
		}
	}

	/**
	 * Get the error message.
	 * 
	 * @return The error message, or null if no message
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * Return the value of the given named header.
	 * 
	 * @param name
	 *            The header name
	 * @return The value, or null
	 */
	@SuppressWarnings("unchecked")
	public String getHeader(final String name)
	{
		List<String> l = (List<String>)headers.get(name);
		if (l == null || l.size() < 1)
		{
			return null;
		}
		else
		{
			return l.get(0);
		}
	}

	/**
	 * Get the names of all of the headers.
	 * 
	 * @return The header names
	 */
	public Set<String> getHeaderNames()
	{
		return headers.keySet();
	}

	/**
	 * Get the encoded locale
	 * 
	 * @return The locale
	 */
	@Override
	public Locale getLocale()
	{
		return locale;
	}

	/**
	 * Get the output stream for writing binary data from the servlet.
	 * 
	 * @return The binary output stream.
	 */
	@Override
	public ServletOutputStream getOutputStream()
	{
		if (mode == MODE_TEXT)
		{
			throw new IllegalArgumentException("Can't write binary after already selecting text");
		}
		mode = MODE_BINARY;
		return servletStream;
	}

	/**
	 * Get the location that was redirected to.
	 * 
	 * @return The redirect location, or null if not a redirect
	 */
	public String getRedirectLocation()
	{
		return redirectLocation;
	}

	/**
	 * Get the status code.
	 * 
	 * @return The status code
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * Get the print writer for writing text output for this response.
	 * 
	 * @return The writer
	 * @throws IOException
	 *             Not used
	 */
	@Override
	public PrintWriter getWriter() throws IOException
	{
		if (mode == MODE_BINARY)
		{
			throw new IllegalArgumentException("Can't write text after already selecting binary");
		}
		mode = MODE_TEXT;
		return printWriter;
	}

	/**
	 * Reset the response ready for reuse.
	 */
	public void initialize()
	{
		cookies.clear();
		headers.clear();
		errorMessage = null;
		redirectLocation = null;
		status = HttpServletResponse.SC_OK;
		characterEncoding = "UTF-8";
		locale = null;

		byteStream = new ByteArrayOutputStream();
		servletStream = new ServletOutputStream()
		{
			@Override
			public void write(int b)
			{
				byteStream.write(b);
			}
		};
		stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter)
		{
			@Override
			public void close()
			{
				// Do nothing
			}

			@Override
			public void flush()
			{
				// Do nothing
			}
		};
		mode = MODE_NONE;
	}

	/**
	 * Always returns false.
	 * 
	 * @return Always false
	 */
	@Override
	public boolean isCommitted()
	{
		return false;
	}

	/**
	 * Return whether the servlet returned an error code or not.
	 * 
	 * @return Whether an error occurred or not
	 */
	public boolean isError()
	{
		return (status != HttpServletResponse.SC_OK);
	}

	/**
	 * Check whether the response was redirected or not.
	 * 
	 * @return Whether the state was redirected or not
	 */
	public boolean isRedirect()
	{
		return (redirectLocation != null);
	}

	/**
	 * Delegate to initialize method.
	 */
	@Override
	public void reset()
	{
		initialize();
	}

	/**
	 * Clears the buffer.
	 */
	@Override
	public void resetBuffer()
	{
		if (mode == MODE_BINARY)
		{
			byteStream.reset();
		}
		else if (mode == MODE_TEXT)
		{
			stringWriter.getBuffer().delete(0, stringWriter.getBuffer().length());
		}
	}

	/**
	 * Send an error code. This implementation just sets the internal error state information.
	 * 
	 * @param code
	 *            The code
	 * @throws IOException
	 *             Not used
	 */
	@Override
	public void sendError(final int code) throws IOException
	{
		status = code;
		errorMessage = null;
	}

	/**
	 * Send an error code. This implementation just sets the internal error state information.
	 * 
	 * @param code
	 *            The error code
	 * @param msg
	 *            The error message
	 * @throws IOException
	 *             Not used
	 */
	@Override
	public void sendError(final int code, final String msg) throws IOException
	{
		status = code;
		errorMessage = msg;
	}

	/**
	 * @return url
	 * @see org.apache.wicket.request.Request#getUrl()
	 */
	private String getURL()
	{
		/*
		 * Servlet 2.3 specification :
		 * 
		 * Servlet Path: The path section that directly corresponds to the mapping which activated
		 * this request. This path starts with a "/" character except in the case where the request
		 * is matched with the "/*" pattern, in which case it is the empty string.
		 * 
		 * PathInfo: The part of the request path that is not part of the Context Path or the
		 * Servlet Path. It is either null if there is no extra path, or is a string with a leading
		 * "/".
		 */
		String url = servletRequest.getServletPath();
		final String pathInfo = servletRequest.getPathInfo();

		if (pathInfo != null)
		{
			url += pathInfo;
		}

		final String queryString = servletRequest.getQueryString();

		if (queryString != null)
		{
			url += ("?" + queryString);
		}

		// If url is non-empty it will start with '/', which we should lose
		if (url.length() > 0 && url.charAt(0) == '/')
		{
			// Remove leading '/'
			url = url.substring(1);
		}
		return url;
	}

	/**
	 * Indicate sending of a redirectLocation to a particular named resource. This implementation
	 * just keeps hold of the redirectLocation info and makes it available for query.
	 * 
	 * @param location
	 *            The location to redirectLocation to
	 * @throws IOException
	 *             Not used
	 */
	@Override
	public void sendRedirect(String location) throws IOException
	{
		redirectLocation = location;
		status = HttpServletResponse.SC_FOUND;
	}

	/**
	 * Method ignored.
	 * 
	 * @param size
	 *            The size
	 */
	@Override
	public void setBufferSize(final int size)
	{
	}

	/**
	 * Set the character encoding.
	 * 
	 * @param characterEncoding
	 *            The character encoding
	 */
	@Override
	public void setCharacterEncoding(final String characterEncoding)
	{
		this.characterEncoding = characterEncoding;
	}

	/**
	 * Set the content length.
	 * 
	 * @param length
	 *            The length
	 */
	@Override
	public void setContentLength(final int length)
	{
		setIntHeader("Content-Length", length);
	}

	/**
	 * Set the content type.
	 * 
	 * @param type
	 *            The content type
	 */
	@Override
	public void setContentType(final String type)
	{
		setHeader("Content-Type", type);
	}

	/**
	 * @return value of content-type header
	 */
	@Override
	public String getContentType()
	{
		return getHeader("Content-Type");
	}

	/**
	 * Set a date header.
	 * 
	 * @param name
	 *            The header name
	 * @param l
	 *            The long value
	 */
	@Override
	public void setDateHeader(final String name, final long l)
	{
		setHeader(name, formatDate(l));
	}

	/**
	 * @param l
	 * @return formatted date
	 */
	public static String formatDate(long l)
	{
		StringBuilder _dateBuffer = new StringBuilder(32);
		Calendar _calendar = new GregorianCalendar(TimeZone.getTimeZone("GMT"));
		_calendar.setTimeInMillis(l);
		formatDate(_dateBuffer, _calendar, false);
		return _dateBuffer.toString();
	}

	/* BEGIN: This code comes from Jetty 6.1.1 */
	private static String[] DAYS = { "Sat", "Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat" };
	private static String[] MONTHS = { "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug",
			"Sep", "Oct", "Nov", "Dec", "Jan" };

	/**
	 * Format HTTP date "EEE, dd MMM yyyy HH:mm:ss 'GMT'" or "EEE, dd-MMM-yy HH:mm:ss 'GMT'"for
	 * cookies
	 * 
	 * @param buf
	 * @param calendar
	 * @param cookie
	 */
	public static void formatDate(StringBuilder buf, Calendar calendar, boolean cookie)
	{
		// "EEE, dd MMM yyyy HH:mm:ss 'GMT'"
		// "EEE, dd-MMM-yy HH:mm:ss 'GMT'", cookie

		int day_of_week = calendar.get(Calendar.DAY_OF_WEEK);
		int day_of_month = calendar.get(Calendar.DAY_OF_MONTH);
		int month = calendar.get(Calendar.MONTH);
		int year = calendar.get(Calendar.YEAR);
		int century = year / 100;
		year = year % 100;

		int epoch = (int)((calendar.getTimeInMillis() / 1000) % (60 * 60 * 24));
		int seconds = epoch % 60;
		epoch = epoch / 60;
		int minutes = epoch % 60;
		int hours = epoch / 60;

		buf.append(DAYS[day_of_week]);
		buf.append(',');
		buf.append(' ');
		append2digits(buf, day_of_month);

		if (cookie)
		{
			buf.append('-');
			buf.append(MONTHS[month]);
			buf.append('-');
			append2digits(buf, year);
		}
		else
		{
			buf.append(' ');
			buf.append(MONTHS[month]);
			buf.append(' ');
			append2digits(buf, century);
			append2digits(buf, year);
		}
		buf.append(' ');
		append2digits(buf, hours);
		buf.append(':');
		append2digits(buf, minutes);
		buf.append(':');
		append2digits(buf, seconds);
		buf.append(" GMT");
	}

	/**
	 * @param buf
	 * @param i
	 */
	public static void append2digits(StringBuilder buf, int i)
	{
		if (i < 100)
		{
			buf.append((char)(i / 10 + '0'));
			buf.append((char)(i % 10 + '0'));
		}
	}

	/* END: This code comes from Jetty 6.1.1 */

	/**
	 * Set the given header value.
	 * 
	 * @param name
	 *            The name for the header
	 * @param value
	 *            The value for the header
	 */
	@Override
	public void setHeader(final String name, final String value)
	{
		List<String> l = new ArrayList<String>(1);
		l.add(value);
		headers.put(name, l);
	}

	/**
	 * Set an int header value.
	 * 
	 * @param name
	 *            The header name
	 * @param i
	 *            The value
	 */
	@Override
	public void setIntHeader(final String name, final int i)
	{
		setHeader(name, "" + i);
	}

	/**
	 * Set the locale in the response header.
	 * 
	 * @param locale
	 *            The locale
	 */
	@Override
	public void setLocale(final Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * Set the status for this response.
	 * 
	 * @param status
	 *            The status
	 */
	@Override
	public void setStatus(final int status)
	{
		this.status = status;
	}

	/**
	 * Set the status for this response.
	 * 
	 * @param status
	 *            The status
	 * @param msg
	 *            The message
	 * @deprecated
	 */
	@Override
	@Deprecated
	public void setStatus(final int status, final String msg)
	{
		setStatus(status);
	}

	/**
	 * @deprecated use {@link #getDocument()}
	 * @return response as String
	 */
	@Deprecated
	public String getTextResponse()
	{
		return getDocument();
	}

	/**
	 * @return binary response
	 */
	public String getBinaryResponse()
	{
		String ctheader = getHeader("Content-Length");
		if (ctheader == null)
		{
			return getDocument();
		}
		else
		{
			return getDocument().substring(0, Integer.valueOf(ctheader));
		}
	}

	/**
	 * @param name
	 * @return headers with given name
	 */
	public Collection<String> getHeaders(String name)
	{
		return Collections.singletonList(headers.get(name).toString());
	}

	@Override
	public void writeMetaData(WebResponse webResponse)
	{
		for (Cookie cookie : cookies)
		{
			webResponse.addCookie(Cookies.copyOf(cookie));
		}
		for (String name : headers.keySet())
		{
			webResponse.setHeader(name, headers.get(name).toString());
		}
		webResponse.setStatus(status);
	}
}