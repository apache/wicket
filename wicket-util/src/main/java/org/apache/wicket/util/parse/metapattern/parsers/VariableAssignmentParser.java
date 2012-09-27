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
import org.apache.wicket.util.parse.metapattern.MetaPattern;
import org.apache.wicket.util.parse.metapattern.OptionalMetaPattern;

/**
 * Parses key value assignment statements like "foo=bar" but also supporting namespaces like
 * "wicket:foo=bar". However the 'key' value returned will contain "wicket:foo". It does not
 * separate namespace and name.
 * 
 * @author Jonathan Locke
 */
public final class VariableAssignmentParser extends MetaPatternParser
{
	/** The optional namespace like "namespace:*[:*]" */
	private static final MetaPattern namespace = new OptionalMetaPattern(new MetaPattern[] {
			MetaPattern.VARIABLE_NAME, MetaPattern.COLON, new OptionalMetaPattern(new MetaPattern[] {MetaPattern.VARIABLE_NAME, MetaPattern.COLON })});

	/** The key (lvalue) like "name" or "namespace:name" or "namespace:name:subname" */
	private final Group key = new Group(new MetaPattern(namespace, MetaPattern.XML_ATTRIBUTE_NAME));

	/** The rvalue of the assignment */
	private final Group value;

	/**
	 * Construct a variable assignment parser against a given input character sequence
	 * 
	 * @param input
	 *            The input to parse
	 */
	public VariableAssignmentParser(final CharSequence input)
	{
		this(input, MetaPattern.STRING);
	}

	/**
	 * Construct a variable assignment parser against a given input character sequence
	 * 
	 * @param input
	 *            The input to parse
	 * @param valuePattern
	 *            Value pattern
	 */
	public VariableAssignmentParser(final CharSequence input, final MetaPattern valuePattern)
	{
		super(input);

		// Create group for value pattern
		value = new Group(valuePattern);

		// Pattern for =<value>
		final MetaPattern variableAssignment = new MetaPattern(MetaPattern.OPTIONAL_WHITESPACE,
			MetaPattern.EQUALS, MetaPattern.OPTIONAL_WHITESPACE, value);

		// Set parse pattern to <key>=<value>?
		setPattern(new MetaPattern(MetaPattern.OPTIONAL_WHITESPACE, key, new OptionalMetaPattern(
			variableAssignment), MetaPattern.OPTIONAL_WHITESPACE));
	}

	/**
	 * Gets the key part (eg 'foo' in 'foo=bar'). The key will include the optional namespace (eg
	 * 'html:foo' in 'html:foo=bar').
	 * 
	 * @return The key part
	 */
	public String getKey()
	{
		return key.get(matcher());
	}

	/**
	 * Gets the value part (eg 'bar' in 'foo=bar').
	 * 
	 * @return The value part
	 */
	public String getValue()
	{
		return value.get(matcher());
	}
}
