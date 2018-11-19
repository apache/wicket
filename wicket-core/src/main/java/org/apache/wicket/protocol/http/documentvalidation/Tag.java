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
package org.apache.wicket.protocol.http.documentvalidation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Class representing an element in a document.
 * 
 * @author Chris Turner
 */
public class Tag implements DocumentElement
{
	private final Map<String, String> expectedAttributes = new HashMap<String, String>();

	private final List<DocumentElement> expectedChildren = new ArrayList<DocumentElement>();

	private final Set<String> illegalAttributes = new HashSet<String>();

	private final String tag;

	/**
	 * Create the tag element.
	 * 
	 * @param tag
	 *            The tag name
	 */
	public Tag(final String tag)
	{
		this.tag = tag.toLowerCase(Locale.ROOT);
	}

	/**
	 * Add an expected attribute to this tag. The second parameter is a regexp pattern on which to
	 * match the value of the tag.
	 * 
	 * @param name
	 *            The name of the attribute
	 * @param pattern
	 *            The pattern to match
	 */
	public void addExpectedAttribute(final String name, final String pattern)
	{
		expectedAttributes.put(name.toLowerCase(Locale.ROOT), pattern);
	}

	/**
	 * Add an expected child to this tag. Children must be added in the order they are expected to
	 * appear.
	 * 
	 * @param e
	 *            The element to add
	 * @return This
	 */
	public Tag addExpectedChild(final DocumentElement e)
	{
		expectedChildren.add(e);
		return this;
	}

	/**
	 * Add the name of an attribute that is NOT expected for this tag.
	 * 
	 * @param name
	 *            The name of the attribute
	 */
	public void addIllegalAttribute(final String name)
	{
		illegalAttributes.add(name.toLowerCase(Locale.ROOT));
	}

	/**
	 * Get the map of expected attributes.
	 * 
	 * @return The expected attribute map
	 */
	public Map<String, String> getExpectedAttributes()
	{
		return expectedAttributes;
	}

	/**
	 * Get the list of expected children.
	 * 
	 * @return The expected children
	 */
	public List<DocumentElement> getExpectedChildren()
	{
		return expectedChildren;
	}

	/**
	 * Get the set of illegal attributes.
	 * 
	 * @return The illegal attributes
	 */
	public Set<String> getIllegalAttributes()
	{
		return illegalAttributes;
	}

	/**
	 * Get the tag that this element represents.
	 * 
	 * @return The tag
	 */
	public String getTag()
	{
		return tag;
	}

	/**
	 * Output a descriptive string.
	 * 
	 * @return The string
	 */
	@Override
	public String toString()
	{
		return "[tag = '" + tag + "']";
	}

}