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
package org.apache.wicket.util.parse.metapattern.parsers;

import org.apache.wicket.util.parse.metapattern.Group;
import org.apache.wicket.util.parse.metapattern.IntegerGroup;
import org.apache.wicket.util.parse.metapattern.MetaPattern;

/**
 * Parses integer variable assignments, such as "x = 9" or "x=9".
 * 
 * @author Jonathan Locke
 */
public final class IntegerVariableAssignmentParser extends MetaPatternParser
{
	/** Parse "variable = &lt;number&gt;". */
	private static final Group variable = new Group(MetaPattern.VARIABLE_NAME);

	/** Group value. */
	private static final IntegerGroup value = new IntegerGroup();

	/** Meta pattern. */
	private static final MetaPattern pattern = new MetaPattern(variable,
		MetaPattern.OPTIONAL_WHITESPACE, MetaPattern.EQUALS, MetaPattern.OPTIONAL_WHITESPACE, value);

	/**
	 * Construct.
	 * 
	 * @param input
	 *            to parse
	 */
	public IntegerVariableAssignmentParser(final CharSequence input)
	{
		super(pattern, input);
	}

	/**
	 * Gets the variable part (eg the 'x' from 'x = 9').
	 * 
	 * @return the variable part
	 */
	public String getVariable()
	{
		return variable.get(matcher());
	}

	/**
	 * Gets the int part (eg the '9' from 'x = 9').
	 * 
	 * @return the int part.
	 */
	public int getIntValue()
	{
		return value.getInt(matcher());
	}

	/**
	 * Gets the int part as a long.
	 * 
	 * @return the int part as a long
	 */
	public long getLongValue()
	{
		return value.getLong(matcher());
	}
}
