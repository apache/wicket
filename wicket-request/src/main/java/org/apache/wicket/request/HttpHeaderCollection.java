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
package org.apache.wicket.request;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.time.Time;

/**
 * a multivalue map of headers names and header values suitable for 
 * processing http request and response headers.
 * 
 * @author Peter Ertl
 * 
 * @since 1.5
 */
public class HttpHeaderCollection
{
	private final Map<HeaderKey, List<Object>> headers;

	/** returned in case no header values were found */
	private static final String[] NO_VALUES = new String[0];

	public HttpHeaderCollection()
	{
		headers = new HashMap<HeaderKey, List<Object>>();
	}

	/**
	 * internally add new object to header values
	 * 
	 * @param name
	 *            header name
	 * @param object
	 *            header value (can be a string or a {@link Time} object
	 */
	private void internalAdd(String name, Object object)
	{
		final HeaderKey key = new HeaderKey(name);

		List<Object> values = headers.get(key);

		if (values == null)
		{
			values = new ArrayList<Object>();
			headers.put(key, values);
		}
		values.add(object);
	}

	/**
	 * set header value (and remove previous values)
	 * 
	 * @param name
	 *            header name
	 * @param value
	 *            header value
	 */
	public void setHeader(String name, String value)
	{
		// remove previous values
		removeHeader(name);

		// add new values
		addHeader(name, value);
	}

	/**
	 * add header value
	 * 
	 * @param name
	 *            header name
	 * @param value
	 *            header value
	 */
	public void addHeader(String name, String value)
	{
		// be lenient and strip leading / trailing blanks
		value = Args.notNull(value, "value").trim();

		internalAdd(name, value);
	}

	/**
	 * add date header value
	 * 
	 * @param name
	 *            header name
	 * @param time
	 *            timestamp
	 */
	public void addDateHeader(String name, Time time)
	{
		internalAdd(name, time);
	}

	/**
	 * add date header value
	 * 
	 * @param name
	 *            header name
	 * @param time
	 *            timestamp
	 */
	public void setDateHeader(String name, Time time)
	{
		// remove previous values
		removeHeader(name);

		// add time object to values
		addDateHeader(name, time);
	}

	/**
	 * remove header values for header name
	 * 
	 * @param name
	 *            header name
	 */
	public void removeHeader(String name)
	{
		final HeaderKey key = new HeaderKey(name);
		final Iterator<Map.Entry<HeaderKey, List<Object>>> it = headers.entrySet().iterator();

		while (it.hasNext())
		{
			final Map.Entry<HeaderKey, List<Object>> header = it.next();

			if (header.getKey().equals(key))
			{
				it.remove();
			}
		}
	}

	private String valueToString(Object value)
	{
		if (value instanceof Time)
		{
			return ((Time)value).toRfc1123TimestampString();
		}
		else
		{
			return value.toString();
		}
	}

	/**
	 * check if header is defined
	 * 
	 * @param name
	 *            header name
	 * @return <code>true</code> if header has one or more values
	 */
	public boolean containsHeader(String name)
	{
		final HeaderKey searchKey = new HeaderKey(name);

		// get the header value (case might differ)
		for (HeaderKey key : headers.keySet())
		{
			if (key.equals(searchKey))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * returns names of headers
	 * 
	 * @return set of header names
	 */
	public Set<String> getHeaderNames()
	{
		if (headers.isEmpty())
		{
			return Collections.emptySet();
		}

		final Set<String> names = new HashSet<String>(headers.size());

		for (HeaderKey key : headers.keySet())
		{
			names.add(key.getName());
		}
		return names;
	}

	/**
	 * get header values (dates will be converted into strings)
	 * 
	 * @param name
	 *            header name
	 * 
	 * @return array of header values or empty array if not found
	 */
	public String[] getHeaderValues(String name)
	{
		final List<Object> objects = headers.get(new HeaderKey(name));

		if (objects == null)
		{
			return NO_VALUES;
		}

		final String[] values = new String[objects.size()];

		for (int i = 0; i < values.length; i++)
		{
			values[i] = valueToString(objects.get(i));
		}
		return values;
	}

	public String getHeader(String name)
	{
		final List<Object> objects = headers.get(new HeaderKey(name));

		if (objects == null || objects.isEmpty())
		{
			return null;
		}
		return valueToString(objects.get(0));
	}

	public Time getDateHeader(String name)
	{
		final List<Object> objects = headers.get(new HeaderKey(name));

		if (objects.isEmpty())
		{
			return null;
		}
		Object object = objects.get(0);

		if ((object instanceof Time) == false)
		{
			throw new IllegalStateException("header value is not of type date");
		}
		return (Time)object;
	}

	/**
	 * check if collection is empty
	 * 
	 * @return <code>true</code> if collection is empty, <code>false</code> otherwise
	 */
	public boolean isEmpty()
	{
		return headers.isEmpty();
	}

	/**
	 * get number of headers
	 * 
	 * @return count
	 */
	public int getCount()
	{
		return headers.size();
	}

	/**
	 * clear all headers
	 */
	public void clear()
	{
		headers.clear();
	}

	/**
	 * key for header collection
	 */
	private static class HeaderKey
	{
		private final String key;
		private final String name;

		private HeaderKey(String name)
		{
			this.name = Args.notEmpty(name, "name").trim();
			this.key = this.name.toLowerCase(Locale.US);
		}

		public String getName()
		{
			return name;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o)
				return true;

			if (!(o instanceof HeaderKey))
				return false;

			HeaderKey that = (HeaderKey)o;

			if (!key.equals(that.key))
				return false;

			return true;
		}

		@Override
		public int hashCode()
		{
			return key.hashCode();
		}
	}
}
