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
package org.apache.wicket.util.value;

import java.util.Map;

import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

/**
 * Map of values, extending {@link ValueMap} with methods for generating (HTML) markup attributes.
 * 
 * @author Eelco Hillenius
 * @since 1.2.6
 * 
 * @see Strings#escapeMarkup(CharSequence)
 */
public final class AttributeMap extends ValueMap
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty <code>AttributeMap</code>.
	 */
	public AttributeMap()
	{
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param map
	 *            a <code>Map</code> to be copied
	 */
	public AttributeMap(final Map<String, Object> map)
	{
		super(map);
	}

	/**
	 * Put a boolean attribute, removing it if {@code value} is false or using the key as value otherwise,
	 * i.e. {@code value="value"}. 
	 * 
	 * @param key
	 *            key of attribute
	 * @param value
	 * @return previous value
	 */
	public boolean putAttribute(final String key, final boolean value)
	{
		Object previous = get(key);
		if (value)
		{
			put(key, key);
		}
		else
		{
			remove(key);
		}
		return key.equals(previous);
	}

	/**
	 * Put a string attribute, removing it if the string is empty (nor null).
	 * 
	 * @param key
	 *            key of attribute
	 * @param value
	 * @return previous value
	 */
	public String putAttribute(String key, CharSequence value)
	{
		if (Strings.isEmpty(value))
		{
			return (String)remove(key);
		}
		else
		{
			return (String)put(key, value);
		}
	}

	/**
	 * Representation as encoded markup attributes.
	 * 
	 * @see Strings#escapeMarkup(CharSequence)
	 */
	public String toString()
	{
		return toCharSequence().toString();
	}

	/**
	 * Representation as encoded markup attributes.
	 * 
	 * @see Strings#escapeMarkup(CharSequence)
	 */
	public CharSequence toCharSequence()
	{
		final AppendingStringBuffer buffer = new AppendingStringBuffer();

		for (String key : keySet())
		{
			if (key != null) {
				buffer.append(' ');
				buffer.append(Strings.escapeMarkup(key));
				
				CharSequence value = getCharSequence(key);
				if (value != null) {
					buffer.append("=\"");
					buffer.append(Strings.escapeMarkup(value));
					buffer.append('"');
				}
			}
		}

		return buffer;
	}
}
