/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.protocol.http;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.ByteArrayOutputStream;
import java.io.StringWriter;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Set;
import java.text.DateFormat;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.ServletOutputStream;

import wicket.util.value.ValueMap;

/**
 * Mock servlet wicket.response. Implements all of the methods from the standard
 * HttpServletResponse class plus helper methods to aid viewing the generated
 * wicket.response.
 * @author Chris Turner
 */
public class MockHttpServletResponse implements HttpServletResponse
{

	// Response information
	private final List cookies = new ArrayList();

	private final ValueMap headers = new ValueMap();

	// Error & status information
	private int code = HttpServletResponse.SC_OK;

	private String errorMessage = null;

	private String redirectLocation = null;

	private int status = HttpServletResponse.SC_OK;

	private String characterEncoding = "UTF-8";

	private Locale locale = null;

	// Content buffers
	private ByteArrayOutputStream byteStream;

	private ServletOutputStream servletStream;

	private StringWriter stringWriter;

	private PrintWriter printWriter;

	private int mode = MODE_NONE;

	// Write mode flags
	private static final int MODE_NONE = 0;

	private static final int MODE_BINARY = 1;

	private static final int MODE_TEXT = 2;

	/**
	 * Create the wicket.response object.
	 */
	public MockHttpServletResponse()
	{
		initialise();
	}

	/**
	 * Reset the wicket.response ready for reuse.
	 */
	public void initialise()
	{
		cookies.clear();
		headers.clear();
		code = HttpServletResponse.SC_OK;
		errorMessage = null;
		redirectLocation = null;
		status = HttpServletResponse.SC_OK;
		characterEncoding = "UTF-8";
		locale = null;

		byteStream = new ByteArrayOutputStream();
		servletStream = new ServletOutputStream()
		{
			public void write(int b)
			{
				byteStream.write(b);
			}
		};
		stringWriter = new StringWriter();
		printWriter = new PrintWriter(stringWriter)
		{
			public void close()
			{
				// Do nothing
			}

			public void flush()
			{
				// Do nothing
			}
		};
		mode = MODE_NONE;
	}

	/**
	 * Add a cookie to the wicket.response.
	 * @param cookie The cookie to add
	 */
	public void addCookie(final Cookie cookie)
	{
		cookies.add(cookie);
	}

	/**
	 * Get all of the cookies that have been added to the wicket.response.
	 * @return The collection of cookies
	 */
	public Collection getCookies()
	{
		return cookies;
	}

	/**
	 * Check if the wicket.response contains the given header name.
	 * @param name The name to check
	 * @return Whether header in wicket.response or not
	 */
	public boolean containsHeader(final String name)
	{
		return headers.containsKey(name);
	}

	/**
	 * Return the value of the given named header.
	 * @param name The header name
	 * @return The value, or null
	 */
	public String getHeader(final String name)
	{
		List l = (List) headers.get(name);
		if (l == null || l.size() < 1)
			return null;
		else
			return (String) l.get(0);
	}

	/**
	 * Get the names of all of the headers.
	 * @return The header names
	 */
	public Set getHeaderNames()
	{
		return headers.keySet();
	}

	/**
	 * Encode thr URL. Does no changes as this test implementation uses cookie based url
	 * tracking.
	 * @param url The url to encode
	 * @return The encoded url
	 */
	public String encodeURL(final String url)
	{
		return url;
	}

	/**
	 * Encode the redirectLocation URL. Does no changes as this test implementation uses
	 * cookie based url tracking.
	 * @param url The url to encode
	 * @return The encoded url
	 */
	public String encodeRedirectURL(final String url)
	{
		return url;
	}

	/**
	 * Encode thr URL. Does no changes as this test implementation uses cookie based url
	 * tracking.
	 * @param url The url to encode
	 * @return The encoded url
	 */
	public String encodeUrl(final String url)
	{
		return url;
	}

	/**
	 * Encode the redirectLocation URL. Does no changes as this test implementation uses
	 * cookie based url tracking.
	 * @param url The url to encode
	 * @return The encoded url
	 */
	public String encodeRedirectUrl(final String url)
	{
		return url;
	}

	/**
	 * Send an error code. This implementation just sets the internal error state
	 * information.
	 * @param code The error code
	 * @param msg The error message
	 * @throws IOException Not used
	 */
	public void sendError(final int code, final String msg) throws IOException
	{
		this.code = code;
		errorMessage = msg;
	}

	/**
	 * Send an error code. This implementation just sets the internal error state
	 * information.
	 * @param code The code
	 * @throws IOException Not used
	 */
	public void sendError(final int code) throws IOException
	{
		this.code = code;
		errorMessage = null;
	}

	/**
	 * Return whether the servlet returned an error code or not.
	 * @return Whether an error occurred or not
	 */
	public boolean isError()
	{
		return (code != HttpServletResponse.SC_OK);
	}

	/**
	 * Get the wicket.response code for this request.
	 * @return The wicket.response code
	 */
	public int getCode()
	{
		return code;
	}

	/**
	 * Get the error message.
	 * @return The error message, or null if no message
	 */
	public String getErrorMessage()
	{
		return errorMessage;
	}

	/**
	 * Indicate sending of a redirectLocation to a particular named resource. This
	 * implementation just keeps hold of the redirectLocation info and makes it available
	 * for query.
	 * @param location The location to redirectLocation to
	 * @throws IOException Not used
	 */
	public void sendRedirect(String location) throws IOException
	{
		this.redirectLocation = location;
	}

	/**
	 * Check whether the wicket.response was redirected or not.
	 * @return Whether the state was redirected or not
	 */
	public boolean isRedirect()
	{
		return (redirectLocation != null);
	}

	/**
	 * Get the location that was redirected to.
	 * @return The redirect location, or null if not a redirect
	 */
	public String getRedirectLocation()
	{
		return redirectLocation;
	}

	/**
	 * Set a date header.
	 * @param name The header name
	 * @param l The long value
	 */
	public void setDateHeader(final String name, final long l)
	{
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		setHeader(name, df.format(new Date(l)));
	}

	/**
	 * Add a date header.
	 * @param name The header value
	 * @param l The long value
	 */
	public void addDateHeader(String name, long l)
	{
		DateFormat df = DateFormat.getDateInstance(DateFormat.FULL);
		addHeader(name, df.format(new Date(l)));
	}

	/**
	 * Set the given header value.
	 * @param name The name for the header
	 * @param value The value for the header
	 */
	public void setHeader(final String name, final String value)
	{
		List l = new ArrayList(1);
		l.add(value);
		headers.put(name, value);
	}

	/**
	 * Add the given header value, including an additional entry if one already exists.
	 * @param name The name for the header
	 * @param value The value for the header
	 */
	public void addHeader(final String name, final String value)
	{
		List l = (List) headers.get(name);
		if (l == null)
		{
			l = new ArrayList(1);
			headers.put(name, l);
		}
		l.add(value);
	}

	/**
	 * Set an int header value.
	 * @param name The header name
	 * @param i The value
	 */
	public void setIntHeader(final String name, final int i)
	{
		setHeader(name, "" + i);
	}

	/**
	 * Add an int header value.
	 * @param name The header name
	 * @param i The value
	 */
	public void addIntHeader(final String name, final int i)
	{
		addHeader(name, "" + i);
	}

	/**
	 * Set the status for this wicket.response.
	 * @param status The status
	 */
	public void setStatus(final int status)
	{
		this.status = status;
	}

	/**
	 * Set the status for this wicket.response.
	 * @param status The status
	 * @param msg The message
	 * @deprecated
	 */
	public void setStatus(final int status, final String msg)
	{
		setStatus(status);
	}

	/**
	 * Get the status code.
	 * @return The status code
	 */
	public int getStatus()
	{
		return status;
	}

	/**
	 * Get the character encoding of the wicket.response.
	 * @return The character encoding
	 */
	public String getCharacterEncoding()
	{
		return characterEncoding;
	}

	/**
	 * Set the character encoding.
	 * @param characterEncoding The character encoding
	 */
	public void setCharacterEncoding(final String characterEncoding)
	{
		this.characterEncoding = characterEncoding;
	}

	/**
	 * Get the output stream for writing binary data from the servlet.
	 * @return The binary output stream.
	 * @throws IOException If stream not available
	 */
	public ServletOutputStream getOutputStream() throws IOException
	{
		if (mode == MODE_TEXT)
		{
			throw new IllegalArgumentException("Can't write binary after already selecting text");
		}
		mode = MODE_BINARY;
		return servletStream;
	}

	/**
	 * Get the binary content that was written to the servlet stream.
	 * @return The binary content
	 */
	public byte[] getBinaryContent()
	{
		return byteStream.toByteArray();
	}

	/**
	 * Get the print writer for writing text output for this wicket.response.
	 * @return The writer
	 * @throws IOException Not used
	 */
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
	 * Get the text document that was written as part of this wicket.response.
	 * @return The document
	 */
	public String getDocument()
	{
		if (mode == MODE_BINARY)
			return new String(byteStream.toByteArray());
		else
			return stringWriter.getBuffer().toString();
	}

	/**
	 * Set the content length.
	 * @param length The length
	 */
	public void setContentLength(final int length)
	{
		setIntHeader("Content-Length", length);
	}

	/**
	 * Set the content type.
	 * @param type The content type
	 */
	public void setContentType(final String type)
	{
		setHeader("Content-Type", type);
	}

	/**
	 * Method ignored.
	 * @param size The size
	 */
	public void setBufferSize(final int size)
	{
	}

	/**
	 * Return the current buffer size
	 * @return The buffer size
	 */
	public int getBufferSize()
	{
		if (mode == MODE_NONE)
			return 0;
		else if (mode == MODE_BINARY)
			return byteStream.size();
		else
			return stringWriter.getBuffer().length();
	}

	/**
	 * Flush the buffer.
	 * @throws IOException
	 */
	public void flushBuffer() throws IOException
	{
	}

	/**
	 * Clears the buffer.
	 */
	public void resetBuffer()
	{
		if (mode == MODE_BINARY)
			byteStream.reset();
		else if (mode == MODE_TEXT)
			stringWriter.getBuffer().delete(0, stringWriter.getBuffer().length());
	}

	/**
	 * Always returns false.
	 * @return Always false
	 */
	public boolean isCommitted()
	{
		return false;
	}

	/**
	 * Delegate to initialise method.
	 */
	public void reset()
	{
		initialise();
	}

	/**
	 * Set the locale in the wicket.response header.
	 * @param locale The locale
	 */
	public void setLocale(final Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * Get the encoded locale
	 * @return The locale
	 */
	public Locale getLocale()
	{
		return locale;
	}

}
