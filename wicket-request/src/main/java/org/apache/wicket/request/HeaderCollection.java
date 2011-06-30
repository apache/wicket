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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.wicket.util.lang.Args;

/**
 * a collection of headers with name and value suitable for 
 * processing request and response headers.
 *
 * @author Peter Ertl
 *
 * @since 1.5
 */
public class HeaderCollection implements Iterable<HeaderCollection.Entry>
{
	private final Map<String, String> headers;

	public HeaderCollection()
	{
		headers = new HashMap<String, String>();
	}

	/**
	 * set header value
	 *
	 * @param name
	 *          header name
	 * @param value
	 *          header value
	 */
	public void setHeader(String name, String value)
	{
		// be lenient and strip leading / trailing blanks
		name = Args.notEmpty(name, "name").trim();
		value = Args.notEmpty(value, "value").trim();

		// remove previous value (required since headers are handled case-sensitive)
		// for example adding 'Content-Type' will overwrite 'content-type' 
		removeHeader(name);

		// add new value
		headers.put(name, value);
	}

	/**
	 * remove header value
	 *
	 * @param name
	 *          header name
	 */
	public void removeHeader(String name)
	{
		name = Args.notEmpty(name, "name").trim();

		final Iterator<Map.Entry<String, String>> it = headers.entrySet().iterator();

		while (it.hasNext())
		{
			Map.Entry<String, String> header = it.next();

			if (header.getKey().equalsIgnoreCase(name))
			{
				it.remove();
			}
		}
	}

	/**
	 * get header value
	 *
	 * @param name
	 *          header name
	 *
	 * @return header value or <code>null</code> if not found
	 */
	public String getValue(String name)
	{
		Args.notEmpty(name, "name");

		// get the header value (case might differ)
		for (Map.Entry<String, String> header : headers.entrySet())
		{
			if (header.getKey().equalsIgnoreCase(name))
			{
				return header.getValue();
			}
		}
		return null;
	}

	/**
	 * get iterator over header values
	 *
	 * @return iterator
	 */
	public Iterator<Entry> iterator()
	{
		final Iterator<Map.Entry<String, String>> iterator = headers.entrySet().iterator();

		return new Iterator<Entry>()
		{
			public boolean hasNext()
			{
				return iterator.hasNext();
			}

			public Entry next()
			{
				return new Entry(iterator.next());
			}

			public void remove()
			{
				throw new UnsupportedOperationException();
			}
		};
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
	 * read-only header entry
	 */
	public static class Entry
	{
		private final Map.Entry<String, String> header;

		public Entry(Map.Entry<String, String> header)
		{
			this.header = header;
		}

		public String getName()
		{
			return header.getKey();
		}

		public String getValue()
		{
			return header.getValue();
		}
	}
}
