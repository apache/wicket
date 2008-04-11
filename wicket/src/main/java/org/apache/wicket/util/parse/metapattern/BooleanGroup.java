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
package org.apache.wicket.util.parse.metapattern;

import java.util.regex.Matcher;

/**
 * A Group that captures case-sensitive boolean values "true" or "false".
 * 
 * @author Jonathan Locke
 */
public final class BooleanGroup extends Group
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an IntegerGroup that parses Strings that match the INTEGER pattern in base 10.
	 * 
	 * @see MetaPattern#INTEGER
	 */
	public BooleanGroup()
	{
		super(new MetaPattern("true|false"));
	}

	/**
	 * @param matcher
	 *            The matcher
	 * @return The value
	 * @see BooleanGroup#getBoolean(java.util.regex.Matcher, boolean)
	 */
	public boolean getBoolean(final Matcher matcher)
	{
		return getBoolean(matcher, false);
	}

	/**
	 * Gets a boolean by parsing the String matched by this capturing group.
	 * 
	 * @param matcher
	 *            The matcher
	 * @param defaultValue
	 *            The default value to use if this group is omitted because it is optional
	 * @return The parsed int value
	 */
	public boolean getBoolean(final Matcher matcher, final boolean defaultValue)
	{
		final String value = get(matcher);
		return value == null ? defaultValue : value.equals("true");
	}
}
