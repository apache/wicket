/*
 * $Id: WebResponse.java 5231 2006-04-01 15:34:49 -0800 (Sat, 01 Apr 2006)
 * joco01 $ $Revision$ $Date: 2006-04-01 15:34:49 -0800 (Sat, 01 Apr
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Locale;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.Strings;
import wicket.util.time.Time;

/**
 * Implements responses over the HTTP protocol by holding an underlying
 * HttpServletResponse object and providing convenience methods for using that
 * object. Convenience methods include methods which: add a cookie, close the
 * stream, encode a URL, redirect a request to another resource, determine if a
 * redirect has been issued, set the content type, set the locale and, most
 * importantly, write a String to the response output.
 * 
 * @author Jonathan Locke
 */
public class WebResponse extends Response
{
	/** Log. */
	private static final Log log = LogFactory.getLog(WebResponse.class);

	/** True if response is a redirect. */
	protected boolean redirect;

	/** The underlying response object. */
	private final HttpServletResponse httpServletResponse;

	/**
	 * Constructor for testing harness.
	 */
	public WebResponse()
	{
		this.httpServletResponse = null;
	}

	/**
	 * Package private constructor.
	 * 
	 * @param httpServletResponse
	 *            The servlet response object
	 */
	public WebResponse(final HttpServletResponse httpServletResponse)
	{
		this.httpServletResponse = httpServletResponse;
	}

	/**
	 * Add a cookie to the web response
	 * 
	 * @param cookie
	 */
	public void addCookie(final Cookie cookie)
	{
		getHttpServletResponse().addCookie(cookie);
	}

	/**
	 * Convenience method for clearing a cookie.
	 * 
	 * @param cookie
	 *            The cookie to set
	 * @see WebResponse#addCookie(Cookie)
	 */
	public void clearCookie(final Cookie cookie)
	{
		cookie.setMaxAge(0);
		cookie.setValue(null);
		addCookie(cookie);
	}

	/**
	 * Closes response output.
	 */
	public void close()
	{
		// NOTE: Servlet container will close the response output stream
		// automatically, so we do nothing here.
	}

	/**
	 * Returns the given url encoded.
	 * 
	 * @param url
	 *            The URL to encode
	 * @return The encoded url
	 */
	public CharSequence encodeURL(CharSequence url)
	{
		if (httpServletResponse != null && url != null)
		{
			return httpServletResponse.encodeURL(url.toString());
		}
		return url;
	}

	/**
	 * Gets the wrapped http servlet response object.
	 * 
	 * @return The wrapped http servlet response object
	 */
	public final HttpServletResponse getHttpServletResponse()
	{
		return httpServletResponse;
	}

	/**
	 * @see wicket.Response#getOutputStream()
	 */
	public OutputStream getOutputStream()
	{
		try
		{
			return httpServletResponse.getOutputStream();
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Error while getting output stream.", e);
		}
	}

	/**
	 * Whether this response is going to redirect the user agent.
	 * 
	 * @return True if this response is going to redirect the user agent
	 */
	public final boolean isRedirect()
	{
		return redirect;
	}

	/**
	 * Redirects to the given url. Implementations should encode the URL to make
	 * sure cookie-less operation is supported in case clients forgot.
	 * 
	 * @param url
	 *            The URL to redirect to
	 */
	public void redirect(String url)
	{
		if (!redirect)
		{
			if (httpServletResponse != null)
			{
				// encode to make sure no caller forgot this
				url = encodeURL(url).toString();
				try
				{
					if (httpServletResponse.isCommitted())
					{
						log.error("Unable to redirect to: " + url
								+ ", HTTP Response has already been committed.");
					}

					if (log.isDebugEnabled())
					{
						log.debug("Redirecting to " + url);
					}

					httpServletResponse.sendRedirect(url);
					redirect = true;
				}
				catch (IOException e)
				{
					throw new WicketRuntimeException("Redirect failed", e);
				}
			}
		}
		else
		{
			log.info("Already redirecting to an url current one ignored: " + url);
		}
	}

	/**
	 * Set the content type on the response.
	 * 
	 * @param mimeType
	 *            The mime type
	 */
	public final void setContentType(final String mimeType)
	{
		httpServletResponse.setContentType(mimeType);
	}

	/**
	 * @see wicket.Response#setContentLength(long)
	 */
	public void setContentLength(long length)
	{
		httpServletResponse.setContentLength((int)length);
	}

	/**
	 * @see wicket.Response#setLastModifiedTime(wicket.util.time.Time)
	 */
	public void setLastModifiedTime(Time time)
	{
		if (time != null && time.getMilliseconds() != -1)
		{
			httpServletResponse.setDateHeader("Last-Modified", time.getMilliseconds());
		}
	}

	/**
	 * Output stream encoding. If the deployment descriptor contains a
	 * locale-encoding-mapping-list element, and that element provides a mapping
	 * for the given locale, that mapping is used. Otherwise, the mapping from
	 * locale to character encoding is container dependent. Default is
	 * ISO-8859-1.
	 * 
	 * @see javax.servlet.ServletResponse#setLocale(java.util.Locale)
	 * 
	 * @param locale
	 *            The locale use for mapping the character encoding
	 */
	public final void setLocale(final Locale locale)
	{
		httpServletResponse.setLocale(locale);
	}

	/**
	 * Writes string to response output.
	 * 
	 * @param string
	 *            The string to write
	 */
	public void write(final CharSequence string)
	{
		if (string instanceof AppendingStringBuffer)
		{
			write((AppendingStringBuffer)string);
		}
		else if (string instanceof StringBuffer)
		{
			try
			{
				StringBuffer sb = (StringBuffer)string;
				char[] array = new char[sb.length()];
				sb.getChars(0, sb.length(), array, 0);
				httpServletResponse.getWriter().write(array, 0, array.length);
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException("Error while writing to servlet output writer.", e);
			}
		}
		else
		{
			try
			{
				httpServletResponse.getWriter().write(string.toString());
			}
			catch (IOException e)
			{
				throw new WicketRuntimeException("Error while writing to servlet output writer.", e);
			}
		}
	}

	/**
	 * Writes AppendingStringBuffer to response output.
	 * 
	 * @param asb
	 *            The AppendingStringBuffer to write to the stream
	 */
	public void write(AppendingStringBuffer asb)
	{
		try
		{
			httpServletResponse.getWriter().write(asb.getValue(), 0, asb.length());
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException("Error while writing to servlet output writer.", e);
		}
	}

	/**
	 * Set a header to the date value in the servlet response stream.
	 * 
	 * @param header
	 * @param date
	 */
	public void setDateHeader(String header, long date)
	{
		httpServletResponse.setDateHeader(header, date);
	}


	/**
	 * Set a header to the string value in the servlet response stream.
	 * 
	 * @param header
	 * @param value
	 */
	public void setHeader(String header, String value)
	{
		httpServletResponse.setHeader(header, value);
	}

	/**
	 * Convinience method for setting the content-disposition:attachment header.
	 * This header is used if the response should prompt the user to download it
	 * as a file instead of opening in a browser.
	 * 
	 * @param filename
	 *            file name of the attachment
	 */
	public void setAttachmentHeader(String filename)
	{
		setHeader("Content-Disposition", "attachment"
				+ ((!Strings.isEmpty(filename)) ? ("; filename=\"" + filename + "\"") : ""));
	}
}
