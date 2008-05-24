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
package org.apache.wicket.request.target.coding;

import org.apache.wicket.protocol.http.WicketURLEncoder;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * {@link AppendingStringBuffer}-based query string encoder, handles String[] and String properly,
 * and properly URL-encodes the values
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class WebRequestEncoder
{
	boolean firstParameter = true;
	AppendingStringBuffer url;

	/**
	 * Construct.
	 * 
	 * @param url
	 *            the {@link AppendingStringBuffer} where to append query string
	 */
	public WebRequestEncoder(AppendingStringBuffer url)
	{
		this.url = url;
	}

	/**
	 * Add an {@link Object}
	 * 
	 * @param key
	 * @param value
	 */
	public void addValue(String key, Object value)
	{
		if (value instanceof String[])
		{
			String[] values = (String[])value;
			for (int i = 0; i < values.length; i++)
			{
				addValue(key, values[i]);
			}
		}
		else if (value instanceof String)
		{
			addValue(key, (String)value);
		}
		else
		{
			throw new IllegalArgumentException("PageParameters can only contain String or String[]");
		}
	}

	/**
	 * Add a {@link String}
	 * 
	 * @param key
	 * @param value
	 */
	public void addValue(String key, String value)
	{
        if (!firstParameter)
		{
			url.append('&');
		}
		else
		{
			firstParameter = false;
			url.append('?');
		}
		url.append(WicketURLEncoder.QUERY_INSTANCE.encode(key));
		url.append('=');
		url.append(WicketURLEncoder.QUERY_INSTANCE.encode(value));
	}

}
