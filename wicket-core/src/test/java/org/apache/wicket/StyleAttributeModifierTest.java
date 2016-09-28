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

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.parser.XmlTag;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for StyleAttributeModifier
 */
public class StyleAttributeModifierTest extends Assert
{
	/**
	 * Adds two style properties
	 */
	@Test
	public void addCssStyles()
	{
		StyleAttributeModifier cam = new StyleAttributeModifier()
		{
			@Override
			protected Map<String, String> update(Map<String, String> oldStyles)
			{
				oldStyles.put("color", "white");
				oldStyles.put("font-size", "9px");
				return oldStyles;
			}
		};
		ComponentTag tag = createTag();

		Map<String, Object> attributes = tag.getAttributes();

		cam.replaceAttributeValue(null, tag);

		String styles = (String) attributes.get(cam.getAttribute());
		assertEquals("color:white;font-size:9px;", styles);
	}

	/**
	 * Modifies one style, removes another and adds a new style
	 */
	@Test
	public void addRemoveCssStyles()
	{
		StyleAttributeModifier cam = new StyleAttributeModifier()
		{
			@Override
			protected Map<String, String> update(Map<String, String> oldStyles)
			{
				oldStyles.put("color", "black");           // modify the value
				oldStyles.remove("font-size");             // remove
				oldStyles.put("background-color", "red");  // add
				return oldStyles;
			}
		};
		ComponentTag tag = createTag();

		Map<String, Object> attributes = tag.getAttributes();
		attributes.put(cam.getAttribute(), "color:white;font-size:9px;");

		cam.replaceAttributeValue(null, tag);

		String classes = (String) attributes.get(cam.getAttribute());
		assertEquals("color:black;background-color:red;", classes);
	}

	/**
	 * Removes all CSS style values and the attribute itself
	 */
	@Test
	public void removeAllCssStyles()
	{
		StyleAttributeModifier cam = new StyleAttributeModifier()
		{
			@Override
			protected Map<String, String> update(Map<String, String> oldStyles)
			{
				oldStyles.remove("color");
				oldStyles.remove("font-size");
				return oldStyles;
			}
		};
		ComponentTag tag = createTag();

		Map<String, Object> attributes = tag.getAttributes();
		attributes.put(cam.getAttribute(), "color:white ;   font-size:99999px; ");

		cam.replaceAttributeValue(null, tag);

		String classes = (String) attributes.get(cam.getAttribute());
		assertNull(classes);
	}

	private ComponentTag createTag()
	{
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("StyleAttributeModifier");
		tag.setName("test");
		return tag;
	}
}
