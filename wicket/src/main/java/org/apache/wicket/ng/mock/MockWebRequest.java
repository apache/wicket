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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.lang.Bytes;

/**
 * Mutable mock {@link WebRequest}.
 * 
 * @author Matej Knopp
 */
public class MockWebRequest extends WebRequest
{
	private final Url url;

	/**
	 * Construct.
	 * 
	 * @param url
	 */
	public MockWebRequest(Url url)
	{
		this.url = url;
	}

	@Override
	public MockWebRequest requestWithUrl(Url url)
	{
		return new MockWebRequest(url);
	}

	@Override
	public Url getUrl()
	{
		return url;
	}

	@Override
	public String toString()
	{
		return "MockWebRequest [url=" + url + "]";
	}

	private Cookie cookies[];

	/**
	 * Sets cookies for current request.
	 * 
	 * @param cookies
	 */
	public void setCookies(Cookie[] cookies)
	{
		this.cookies = cookies;
	}

	@Override
	public Cookie[] getCookies()
	{
		return cookies;
	}

	private final Map<String, List<Object>> headers = new HashMap<String, List<Object>>();

	@Override
	public long getDateHeader(String name)
	{
		List<Object> dates = headers.get(name);
		if (dates == null || dates.isEmpty())
		{
			throw new WicketRuntimeException("Date header with name '" + name + "' does not exist.");
		}

		Object date = dates.get(0);

		if (date instanceof Long == false)
		{
			throw new WicketRuntimeException("Date header with name '" + name +
				"' is not a valid long.");
		}
		return (Long)date;
	}

	private void addHeaderObject(String name, Object value)
	{
		List<Object> values = headers.get(name);
		if (values == null)
		{
			values = new ArrayList<Object>();
			headers.put(name, values);
		}
		values.add(value);
	}

	/**
	 * Sets date header for given name.
	 * 
	 * @param name
	 * @param value
	 */
	public void setDateHeader(String name, long value)
	{
		removeHeader(name);
		addHeaderObject(name, value);
	}

	/**
	 * Adds date header for given name.
	 * 
	 * @param name
	 * @param value
	 */
	public void addDateHeader(String name, long value)
	{
		addHeaderObject(name, value);
	}

	@Override
	public String getHeader(String name)
	{
		List<Object> h = headers.get(name);
		return (h == null || h.isEmpty()) ? null : h.get(0).toString();
	}

	/**
	 * Sets header for given name.
	 * 
	 * @param name
	 * @param value
	 */
	public void setHeader(String name, String value)
	{
		removeHeader(name);
		addHeader(name, value);
	}

	/**
	 * Adds header for given name.
	 * 
	 * @param name
	 * @param value
	 */
	public void addHeader(String name, String value)
	{
		addHeaderObject(name, value);
	}

	private Locale locale = Locale.getDefault();

	/**
	 * Sets request locale.
	 * 
	 * @param locale
	 */
	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	@Override
	public Locale getLocale()
	{
		return locale;
	}

	@Override
	public List<String> getHeaders(String name)
	{
		List<String> res = new ArrayList<String>();
		List<Object> values = headers.get(name);
		if (values != null)
		{
			for (Object value : values)
			{
				if (value != null)
				{
					res.add(value.toString());
				}
			}
		}
		return res;
	}

	/**
	 * Removes header with specified name.
	 * 
	 * @param header
	 */
	public void removeHeader(String header)
	{
		headers.remove(header);
	}

	private final MockRequestParameters postRequestParameters = new MockRequestParameters();

	@Override
	public MockRequestParameters getPostRequestParameters()
	{
		return postRequestParameters;
	}

	@Override
	// TODO WICKET-NG implement?
	public HttpServletRequest getHttpServletRequest()
	{
		return null;
	}

	@Override
	// TODO WICKET-NG implement?
	public String getParameter(String key)
	{
		return null;
	}

	@Override
	// TODO WICKET-NG implement?
	public Map<String, String[]> getParameterMap()
	{
		return null;
	}

	@Override
	// TODO WICKET-NG implement?
	public String[] getParameters(String key)
	{
		return null;
	}

	@Override
	// TODO WICKET-NG implement?
	public String getServletPath()
	{
		return null;
	}

	@Override
	// TODO WICKET-NG implement?
	public boolean isAjax()
	{
		return false;
	}

	@Override
	// TODO WICKET-NG implement?
	public WebRequest newMultipartWebRequest(Bytes maxSize)
	{
		return null;
	}

	@Override
	// TODO WICKET-NG implement?
	public String getPath()
	{
		return null;
	}

	@Override
	// TODO WICKET-NG implement?
	public String getQueryString()
	{
		return null;
	}

	@Override
	// TODO WICKET-NG implement?
	public String getRelativePathPrefixToContextRoot()
	{
		return null;
	}

	@Override
	// TODO WICKET-NG implement?
	public String getRelativePathPrefixToWicketHandler()
	{
		return null;
	}
}
