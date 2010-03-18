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

import java.nio.charset.Charset;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.Cookie;

import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;


/**
 * Base class for request that provides additional web-related information.
 * 
 * @author Matej Knopp
 */
public abstract class WebRequest extends Request
{
	/**
	 * @return request cookies
	 */
	public abstract List<Cookie> getCookies();

	/**
	 * @param cookieName
	 * @return cookie with specified name or <code>null</code> if the cookie does not exist
	 */
	public Cookie getCookie(String cookieName)
	{
		for (Cookie cookie : getCookies())
		{
			if (cookie.getName().equals(cookieName))
			{
				return cookie;
			}
		}
		return null;
	}

	/**
	 * Returns all the values of the specified request header.
	 * 
	 * @param name
	 * @return unmodifiable list of header values
	 */
	public abstract List<String> getHeaders(String name);

	/**
	 * Returns the value of the specified request header as a <code>String</code>
	 * 
	 * @param name
	 * @return string value of request header
	 */
	public abstract String getHeader(String name);

	/**
	 * Returns the value of the specified request header as a <code>long</code> value that
	 * represents a <code>Date</code> object. Use this method with headers that contain dates, such
	 * as <code>If-Modified-Since</code>.
	 * 
	 * @param name
	 * @return date value of request header
	 */
	public abstract long getDateHeader(String name);

	/**
	 * Convenience method for retrieving If-Modified-Since header.
	 * 
	 * @return date representing the header or <code>null</code> if not set
	 */
	public final Date getIfModifiedSinceHeader()
	{
		final long header = getDateHeader("If-Modified-Since");
		if (header >= 0)
		{
			return new Date(header);
		}
		else
		{
			return null;
		}
	}

	/**
	 * Marker parameter for AjaxRequest.
	 */
	private static final String PARAM_AJAX = "wicket:ajax";
	private static final String HEADER_AJAX = "Wicket-Ajax";

	/**
	 * Returns whether this request is an Ajax request. This implementation only checks for value of
	 * wicket:ajax url parameter. Subclasses can use other approach.
	 * 
	 * @return <code>true</code> if this request is an ajax request, <code>false</code> otherwise.
	 */
	public boolean isAjax()
	{
		return "true".equalsIgnoreCase(getHeader(HEADER_AJAX)) ||
			getRequestParameters().getParameterValue(PARAM_AJAX).toBoolean(false);
	}

	/**
	 * Returns request with specified URL and same POST parameters as this request.
	 * 
	 * @param url
	 *            Url instance
	 * @return request with specified URL.
	 */
	@Override
	public WebRequest requestWithUrl(final Url url)
	{
		return new WebRequest()
		{
			@Override
			public Url getUrl()
			{
				return url;
			}

			@Override
			public IRequestParameters getPostParameters()
			{
				return WebRequest.this.getPostParameters();
			}

			@Override
			public List<Cookie> getCookies()
			{
				return WebRequest.this.getCookies();
			}

			@Override
			public long getDateHeader(String name)
			{
				return WebRequest.this.getDateHeader(name);
			}

			@Override
			public Locale getLocale()
			{
				return WebRequest.this.getLocale();
			}

			@Override
			public String getHeader(String name)
			{
				return WebRequest.this.getHeader(name);
			}

			@Override
			public List<String> getHeaders(String name)
			{
				return WebRequest.this.getHeaders(name);
			}

			@Override
			public Charset getCharset()
			{
				return WebRequest.this.getCharset();
			}
		};
	}

}
