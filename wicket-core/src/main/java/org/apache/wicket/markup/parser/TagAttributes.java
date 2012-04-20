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
package org.apache.wicket.markup.parser;

import java.util.Map;

import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.IValueMap;
import org.apache.wicket.util.value.ValueMap;

/**
 * 
 */
public class TagAttributes extends ValueMap
{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructs empty <code>ValueMap</code>.
	 */
	public TagAttributes()
	{
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param map
	 *            the <code>ValueMap</code> to copy
	 */
	public TagAttributes(final IValueMap map)
	{
		super();
		putAll(map);
	}

	@Override
	public final Object put(String key, Object value)
	{

		return super.put(key, unescapeHtml(value));
	}

	@Override
	public final void putAll(Map<? extends String, ?> map)
	{
		for (Map.Entry<? extends String, ?> entry : map.entrySet())
		{
			String key = entry.getKey();
			Object value = entry.getValue();
			put(key, value);
		}
	}

	/**
	 * Unescapes the HTML entities from the <code>value</code> if it is a {@link CharSequence} and
	 * there are any
	 * 
	 * @param value
	 *            the attribute value
	 * @return the HTML unescaped value or the non-modified input
	 */
	private static final Object unescapeHtml(Object value)
	{
		if (value instanceof CharSequence)
		{
			return Strings.unescapeMarkup(value.toString());
		}
		else
		{
			return value;
		}
	}
}