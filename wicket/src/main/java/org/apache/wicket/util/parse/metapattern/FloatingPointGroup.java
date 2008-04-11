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
 * A Group that captures floating point values (doubles and floats).
 * 
 * @author Jonathan Locke
 */
public final class FloatingPointGroup extends Group
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an FloatingPointGroup that parses Strings that match the FLOATING_POINT_NUMBER
	 * pattern.
	 * 
	 * @see MetaPattern#FLOATING_POINT_NUMBER
	 */
	public FloatingPointGroup()
	{
		super(FLOATING_POINT_NUMBER);
	}

	/**
	 * @param matcher
	 *            The matcher
	 * @return The value
	 */
	public float getFloat(final Matcher matcher)
	{
		return getFloat(matcher, -1);
	}

	/**
	 * Gets float by parsing the String matched by this capturing group.
	 * 
	 * @param matcher
	 *            The matcher
	 * @param defaultValue
	 *            The default value to use if this group is omitted because it is optional
	 * @return The parsed value
	 */
	public float getFloat(final Matcher matcher, final float defaultValue)
	{
		final String value = get(matcher);
		return value == null ? defaultValue : Float.parseFloat(value);
	}

	/**
	 * @param matcher
	 *            The matcher
	 * @return The value
	 */
	public double getDouble(final Matcher matcher)
	{
		return getDouble(matcher, -1);
	}

	/**
	 * Gets double by parsing the String matched by this capturing group.
	 * 
	 * @param matcher
	 *            The matcher
	 * @param defaultValue
	 *            The default value to use if this group is omitted because it is optional
	 * @return The parsed value
	 */
	public double getDouble(final Matcher matcher, final double defaultValue)
	{
		final String value = get(matcher);
		return value == null ? defaultValue : Double.parseDouble(value);
	}
}
