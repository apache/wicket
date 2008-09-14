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
package org.apache._wicket.request.request;

import java.util.List;

import javax.servlet.http.Cookie;


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
	public abstract Cookie[] getCookies();

	/**
	 * @param cookieName
	 * @return cookie with specified name or <code>null</code> if the cookie does not exist
	 */
	public Cookie getCookie(String cookieName)
	{
		Cookie[] cookies = getCookies();
		if (cookies != null && cookies.length > 0)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				if (cookies[i].getName().equals(cookieName))
				{
					return cookies[i];
				}
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
	 * represents a <code>Date</code> object. Use this method with headers that contain dates,
	 * such as <code>If-Modified-Since</code>.
	 * 
	 * @param name
	 * @return date value of request header
	 */
	public abstract long getDateHeader(String name);

	/**
	 * Marker parameter for AjaxRequest.
	 */
	public static final String PARAM_AJAX = "wicket:ajax";

	/**
	 * Returns whether this request is an Ajax request. This implementation only checks for value of
	 * wicket:ajax url parameter. Subclasses can use other approach.
	 * 
	 * @return <code>true</code> if this request is an ajax request, <code>false</codE>
	 *         otherwise.
	 */
	public boolean isAjax()
	{
		return getRequestParameters().getParameterValue(PARAM_AJAX).toBoolean(false);
	}

}

