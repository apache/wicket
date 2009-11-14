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

import org.apache.wicket.ng.WicketRuntimeException;
import org.apache.wicket.ng.protocol.http.WebRequest;
import org.apache.wicket.ng.request.Url;

public class MockWebRequest extends WebRequest
{
	private final Url url;

	public MockWebRequest(Url url)
	{
		this.url = url;
	}

	@Override
	public WebRequest requestWithUrl(Url url)
	{
		return new MockWebRequest(url);
	}

	@Override
	public Url getUrl()
	{
		return url;
	}

	@Override
	public int hashCode()
	{
		final int prime = 31;
		int result = 1;
		result = prime * result + ((url == null) ? 0 : url.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MockWebRequest other = (MockWebRequest)obj;
		if (url == null)
		{
			if (other.url != null)
				return false;
		}
		else if (!url.equals(other.url))
			return false;
		return true;
	}

	@Override
	public String toString()
	{
		return "MockRequest [url=" + url + "]";
	}

	private Cookie cookies[];

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

	public void setHeader(String name, String value)
	{
		addHeaderObject(name, value);
	}

	private Locale locale = Locale.getDefault();

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

	public void removeHeader(String header)
	{
		headers.remove(header);
	}
}
