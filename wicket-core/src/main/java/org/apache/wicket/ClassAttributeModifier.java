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

import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Pattern;

import org.apache.wicket.behavior.AttributeAppender;
import org.apache.wicket.util.string.Strings;

/**
 * An AttributeModifier specialized in managing the <em>CSS class</em>
 * attribute
 */
public abstract class ClassAttributeModifier extends AttributeAppender
{
	private static final Pattern SPLITTER = Pattern.compile("\\s+");

	/**
	 * Constructor.
	 */
	public ClassAttributeModifier()
	{
		super("class", null, " ");
	}

	@Override
	protected String newValue(String currentValue, String appendValue)
	{
		String[] classes;
		if (Strings.isEmpty(currentValue))
		{
			classes = new String[0];
		}
		else
		{
			classes = SPLITTER.split(currentValue.trim());
		}
		Set<String> oldClasses = new TreeSet<>();
		Collections.addAll(oldClasses, classes);

		Set<String> newClasses = update(oldClasses);

		String separator = getSeparator();
		StringBuilder result = new StringBuilder();
		for (String cls : newClasses)
		{
			if (result.length() > 0)
			{
				result.append(separator);
			}
			result.append(cls);
		}
		return result.length() > 0 ? result.toString() : VALUELESS_ATTRIBUTE_REMOVE;
	}

	/**
	 * Callback to update the CSS class values for a tag.
	 *
	 * @param oldClasses
	 *          A set with the old class values
	 * @return A set with the new class values
	 */
	protected abstract Set<String> update(Set<String> oldClasses);
}
