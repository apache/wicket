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
 * A Group that captures integer values (positive and negative whole numbers, not Java ints).
 * 
 * @author Jonathan Locke
 */
public final class IntegerGroup extends Group
{
	private static final long serialVersionUID = 1L;

	/** The radix to use when converting Strings captured by this group. */
	private final int radix;

	/**
	 * Constructs an IntegerGroup that parses Strings that match the INTEGER pattern in base 10.
	 * 
	 * @see MetaPattern#INTEGER
	 */
	public IntegerGroup()
	{
		this(INTEGER);
	}

	/**
	 * Constructs an IntegerGroup that parses Strings that match the given pattern in base 10.
	 * 
	 * @param pattern
	 *            The capturing pattern
	 */
	public IntegerGroup(final MetaPattern pattern)
	{
		this(pattern, 10);
	}

	/**
	 * Constructs an IntegerGroup that parses Strings that match the given pattern in the given
	 * radix.
	 * 
	 * @param pattern
	 *            The capturing pattern
	 * @param radix
	 *            The radix to use when parsing captured Strings
	 */
	public IntegerGroup(final MetaPattern pattern, final int radix)
	{
		super(pattern);
		this.radix = radix;
	}

	/**
	 * @param matcher
	 *            The matcher
	 * @return The value
	 * @see IntegerGroup#getInt(Matcher, int)
	 */
	public int getInt(final Matcher matcher)
	{
		return getInt(matcher, -1);
	}

	/**
	 * Gets an int by parsing the String matched by this capturing group. The IntegerGroup's radix
	 * is used in the conversion.
	 * 
	 * @param matcher
	 *            The matcher
	 * @param defaultValue
	 *            The default value to use if this group is omitted because it is optional
	 * @return The parsed int value
	 */
	public int getInt(final Matcher matcher, final int defaultValue)
	{
		final String value = get(matcher);
		return value == null ? defaultValue : Integer.parseInt(value, radix);
	}

	/**
	 * @param matcher
	 *            The matcher
	 * @return The value
	 * @see IntegerGroup#getLong(Matcher)
	 */
	public long getLong(final Matcher matcher)
	{
		return getLong(matcher, -1L);
	}

	/**
	 * Gets a long by parsing the String matched by this capturing group. The IntegerGroup's radix
	 * is used in the conversion. parsing radix.
	 * 
	 * @param defaultValue
	 *            The default value to use if this group is omitted because it is optional
	 * @param matcher
	 *            The matcher
	 * @return The parsed long value
	 */
	public long getLong(final Matcher matcher, final long defaultValue)
	{
		final String value = get(matcher);
		return value == null ? defaultValue : Long.parseLong(value, radix);
	}
}
