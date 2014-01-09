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
package org.apache.wicket.request.http;

import java.io.IOException;

import javax.servlet.http.Cookie;

import org.apache.wicket.request.Response;
import org.apache.wicket.util.encoding.UrlEncoder;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.time.Time;

/**
 * Base class for web-related responses.
 * 
 * @author Matej Knopp
 */
public abstract class WebResponse extends Response
{
	/** Recommended value for cache duration */
	// one year, maximum recommended cache duration in RFC-2616
	public static final Duration MAX_CACHE_DURATION = Duration.days(365);

	/**
	 * Add a cookie to the web response
	 * 
	 * @param cookie
	 */
	public abstract void addCookie(final Cookie cookie);

	/**
	 * Convenience method for clearing a cookie.
	 * 
	 * @param cookie
	 *            The cookie to set
	 * @see WebResponse#addCookie(Cookie)
	 */
	public abstract void clearCookie(final Cookie cookie);

	/**
	 * Set a header to the string value in the servlet response stream.
	 * 
	 * @param name
	 * @param value
	 */
	public abstract void setHeader(String name, String value);

	/**
	 * Add a value to the servlet response stream.
	 * 
	 * @param name
	 * @param value
	 */
	public abstract void addHeader(String name, String value);

	/**
	 * Set a header to the date value in the servlet response stream.
	 * 
	 * @param name
	 * @param date
	 */
	public abstract void setDateHeader(String name, Time date);

	/**
	 * Set the content length on the response, if appropriate in the subclass. This default
	 * implementation does nothing.
	 * 
	 * @param length
	 *            The length of the content
	 */
	public abstract void setContentLength(final long length);

	/**
	 * Set the content type on the response, if appropriate in the subclass. This default
	 * implementation does nothing.
	 * 
	 * @param mimeType
	 *            The mime type
	 */
	public abstract void setContentType(final String mimeType);

	/**
	 * Set the contents last modified time, if appropriate in the subclass.
	 * 
	 * @param time
	 *            The last modified time
	 */
	public void setLastModifiedTime(final Time time)
	{
		setDateHeader("Last-Modified", time);
	}

	/**
	 * Convenience method for setting the content-disposition:attachment header. This header is used
	 * if the response should prompt the user to download it as a file instead of opening in a
	 * browser.
	 * <p>
	 * The file name will be <a href="http://greenbytes.de/tech/tc2231/">encoded</a>
	 *
	 * @param filename
	 *            file name of the attachment
	 */
	public void setAttachmentHeader(final String filename)
	{
		setHeader("Content-Disposition", "attachment" + encodeDispositionHeaderValue(filename));
	}

	/**
	 * Convenience method for setting the content-disposition:inline header. This header is used if
	 * the response should be shown embedded in browser window while having custom file name when
	 * user saves the response. browser.
	 * <p>
	 * The file name will be <a href="http://greenbytes.de/tech/tc2231/">encoded</a>
	 *
	 * @param filename
	 *            file name of the attachment
	 */
	public void setInlineHeader(final String filename)
	{
		setHeader("Content-Disposition", "inline" + encodeDispositionHeaderValue(filename));
	}

	/**
	 * <a href="http://greenbytes.de/tech/tc2231/">Encodes</a> the value of the filename
	 * used in "Content-Disposition" response header
	 *
	 * @param filename
	 *          the non-encoded file name
	 * @return encoded filename
	 */
	private String encodeDispositionHeaderValue(final String filename)
	{
		return 	(Strings.isEmpty(filename) ?
						"" :
						String.format("; filename=\"%1$s\"; filename*=UTF-8''%1$s",
								UrlEncoder.PATH_INSTANCE.encode(filename, "UTF-8")));
	}

	/**
	 * Sets the status code for this response.
	 * 
	 * @param sc
	 *            status code
	 */
	public abstract void setStatus(int sc);

	/**
	 * Send error status code with optional message.
	 * 
	 * @param sc
	 * @param msg
	 * @throws IOException
	 */
	public abstract void sendError(int sc, String msg);

	/**
	 * Encodes urls used to redirect. Sometimes rules for encoding URLs for redirecting differ from
	 * encoding URLs for links, so this method is broken out away form
	 * {@link #encodeURL(CharSequence)}.
	 * 
	 * @param url
	 * @return encoded URL
	 */
	public abstract String encodeRedirectURL(CharSequence url);

	/**
	 * Redirects the response to specified URL. The implementation is responsible for properly
	 * encoding the URL. Implementations of this method should run passed in {@code url} parameters
	 * throu the {@link #encodeRedirectURL(CharSequence)} method.
	 * 
	 * @param url
	 */
	public abstract void sendRedirect(String url);

	/**
	 * @return <code>true</code> is {@link #sendRedirect(String)} was called, <code>false</code>
	 *         otherwise.
	 */
	public abstract boolean isRedirect();

	/**
	 * Flushes the response.
	 */
	public abstract void flush();

	/**
	 * Make this response non-cacheable
	 */
	public void disableCaching()
	{
		setDateHeader("Date", Time.now());
		setDateHeader("Expires", Time.START_OF_UNIX_TIME);
		setHeader("Pragma", "no-cache");
		setHeader("Cache-Control", "no-cache, no-store");
	}

	/**
	 * Make this response cacheable
	 * <p/> 
	 * when trying to enable caching for web pages check this out: 
	 * <a href="https://issues.apache.org/jira/browse/WICKET-4357">WICKET-4357</a>
	 * 
	 * @param duration
	 *            maximum duration before the response must be invalidated by any caches. It should
	 *            not exceed one year, based on <a
	 *            href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">RFC-2616</a>.
	 * @param scope
	 *            controls which caches are allowed to cache the response
	 *            
	 * @see WebResponse#MAX_CACHE_DURATION
	 */
	public void enableCaching(Duration duration, final WebResponse.CacheScope scope)
	{
		Args.notNull(duration, "duration");
		Args.notNull(scope, "scope");

		// do not exceed the maximum recommended value from RFC-2616
		if (duration.compareTo(MAX_CACHE_DURATION) > 0)
		{
			duration = MAX_CACHE_DURATION;
		}

		// Get current time
		Time now = Time.now();

		// Time of message generation
		setDateHeader("Date", now);

		// Time for cache expiry = now + duration
		setDateHeader("Expires", now.add(duration));

		// Set cache scope
		setHeader("Cache-Control", scope.cacheControl);
		
		// Set maximum age for caching in seconds (rounded)
		addHeader("Cache-Control", "max-age=" + Math.round(duration.seconds()));

		// Though 'cache' is not an official value it will eliminate an eventual 'no-cache' header
		setHeader("Pragma", "cache");
	}

	/**
	 * caching scope for data
	 * <p/>
	 * Unless the data is confidential, session-specific or user-specific the general advice is to
	 * prefer value <code>PUBLIC</code> for best network performance.
	 * <p/>
	 * This value will basically affect the header [Cache-Control]. Details can be found <a
	 * href="http://palisade.plynt.com/issues/2008Jul/cache-control-attributes">here</a> or in <a
	 * href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">RFC-2616</a>.
	 */
	public static enum CacheScope {
		/**
		 * use all caches (private + public)
		 * <p/>
		 * Use this value for caching if the data is not confidential or session-specific. It will
		 * allow public caches to cache the data. In some versions of Firefox this will enable
		 * caching of resources over SSL (details can be found <a
		 * href="http://blog.pluron.com/2008/07/why-you-should.html">here</a>).
		 */
		PUBLIC("public"),
		/**
		 * only use non-public caches
		 * <p/>
		 * Use this setting if the response is session-specific or confidential and you don't want
		 * it to be cached on public caches or proxies. On some versions of Firefox this will
		 * disable caching of any resources in over SSL connections.
		 */
		PRIVATE("private");

		// value for Cache-Control header
		private final String cacheControl;

		CacheScope(final String cacheControl)
		{
			this.cacheControl = cacheControl;
		}
	}
}
