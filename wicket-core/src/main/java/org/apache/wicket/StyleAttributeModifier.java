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
package org.apache.wicket;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.util.string.Strings;

/**
 * An AttributeModifier specialized in managing the <em>CSS style</em>
 * attribute
 */
public abstract class StyleAttributeModifier extends AttributeAppender
{
	private static final Pattern STYLES_SPLITTER = Pattern.compile("\\s*;\\s*");
	private static final Pattern KEY_VALUE_SPLITTER = Pattern.compile("\\s*:\\s*");

	/**
	 * Constructor.
	 */
	public StyleAttributeModifier()
	{
		super("style", null, ";");
	}

	@Override
	protected final String newValue(String currentValue, String appendValue)
	{
		String[] styles;
		if (Strings.isEmpty(currentValue))
		{
			styles = new String[0];
		}
		else
		{
			styles = STYLES_SPLITTER.split(currentValue.trim());
		}
		Map<String, String> oldStyles = new LinkedHashMap<>();
		for (String style : styles)
		{
			String[] keyValue = KEY_VALUE_SPLITTER.split(style, 2);
			oldStyles.put(keyValue[0], keyValue[1]);
		}

		Map<String, String> newStyles = update(oldStyles);

		StringBuilder result = new StringBuilder();
		for (Map.Entry<String, String> entry : newStyles.entrySet())
		{
			result
				.append(entry.getKey())
				.append(':')
				.append(entry.getValue())
				.append(getSeparator());
		}
		return result.length() > 0 ? result.toString() : VALUELESS_ATTRIBUTE_REMOVE;
	}

	/**
	 * Callback to update the CSS class values for a tag.
	 *
	 * @param oldStyles
	 *          A map with the old style key/values
	 * @return A map with the new style key/values
	 */
	protected abstract Map<String, String> update(Map<String, String> oldStyles);
}
