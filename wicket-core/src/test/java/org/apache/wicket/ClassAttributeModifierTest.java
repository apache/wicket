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

import java.util.Map;
import java.util.Set;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.parser.XmlTag;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for ClassAttributeModifier
 */
public class ClassAttributeModifierTest extends Assert
{
	/**
	 * Adds two values
	 */
	@Test
	public void addCssClasses()
	{
		ClassAttributeModifier cam = new ClassAttributeModifier()
		{
			@Override
			protected Set<String> update(Set<String> oldClasses)
			{
				oldClasses.add("one");
				oldClasses.add("two");
				return oldClasses;
			}
		};
		ComponentTag tag = createTag();

		Map<String, Object> attributes = tag.getAttributes();

		cam.replaceAttributeValue(null, tag);

		String classes = (String) attributes.get(cam.getAttribute());
		assertEquals("one two", classes);
	}

	/**
	 * Adds 'three' and removes 'two'
	 */
	@Test
	public void addRemoveCssClasses()
	{
		ClassAttributeModifier cam = new ClassAttributeModifier()
		{
			@Override
			protected Set<String> update(Set<String> oldClasses)
			{
				oldClasses.add("one");
				oldClasses.remove("two");
				oldClasses.add("three");
				return oldClasses;
			}
		};
		ComponentTag tag = createTag();

		Map<String, Object> attributes = tag.getAttributes();
		attributes.put(cam.getAttribute(), "one two");

		cam.replaceAttributeValue(null, tag);

		String classes = (String) attributes.get(cam.getAttribute());
		assertEquals("one three", classes);
	}

	/**
	 * Removes all CSS class values
	 */
	@Test
	public void removeAllCssClasses()
	{
		ClassAttributeModifier cam = new ClassAttributeModifier()
		{
			@Override
			protected Set<String> update(Set<String> oldClasses)
			{
				oldClasses.remove("one");
				oldClasses.remove("two");
				return oldClasses;
			}
		};
		ComponentTag tag = createTag();

		Map<String, Object> attributes = tag.getAttributes();
		attributes.put(cam.getAttribute(), "two one");

		cam.replaceAttributeValue(null, tag);

		String classes = (String) attributes.get(cam.getAttribute());
		assertNull(classes);
	}

	private ComponentTag createTag()
	{
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("ClassAttributeModifier");
		tag.setName("test");
		return tag;
	}
}
