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

import java.util.Locale;

import org.apache.wicket.util.parse.metapattern.Group;
import org.apache.wicket.util.parse.metapattern.MetaPattern;
import org.apache.wicket.util.parse.metapattern.OptionalMetaPattern;

/**
 * Parses XML tag names and attribute names which may include optional namespaces like
 * "namespace:name" or "name". Both ":name" and "namespace:" are not allowed. Both, the namespace
 * and the name have to follow naming rules for variable names (identifier).
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public final class TagNameParser extends MetaPatternParser
{
	/** Namespaces must comply with variable name guidelines */
	private static final Group namespaceGroup = new Group(MetaPattern.VARIABLE_NAME);

	/** Tag names must comply with XML NCName guidelines */
	private static final Group nameGroup = new Group(MetaPattern.XML_ELEMENT_NAME);

	/** Pattern for tag names with optional namespace: (namespace:)?name */
	private static final MetaPattern pattern = new MetaPattern(new OptionalMetaPattern(
		new MetaPattern[] { namespaceGroup, MetaPattern.COLON }), nameGroup);

	/**
	 * Constructs a tag name parser for a given input character sequence.
	 * 
	 * @param input
	 *            The input to parse
	 */
	public TagNameParser(final CharSequence input)
	{
		super(pattern, input);
	}

	/**
	 * Get the namespace part (eg 'html' in 'html:form') converted to all lower case characters.
	 * 
	 * @return the namespace part. Will be null, if optional namespace was not found
	 */
	public String getNamespace()
	{
		final String namespace = namespaceGroup.get(matcher());
		if (namespace != null)
		{
			return namespace.toLowerCase(Locale.ROOT);
		}
		return namespace;
	}

	/**
	 * Gets the tag name part (eg 'form' in 'html:form' or 'form')
	 * 
	 * @return the name part
	 */
	public String getName()
	{
		return nameGroup.get(matcher());
	}
}
