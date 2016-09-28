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
import org.apache.wicket.model.Model;
import org.junit.Assert;
import org.junit.Test;

/**
 * Test case for the component tag attribute modifer test.
 * 
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public class AttributeModifierTest extends Assert
{
	/**
	 * Test constructors.
	 */
	@Test(expected = IllegalArgumentException.class)
	public void nullAttributeFailsConstruction()
	{
		new AttributeModifier(null, new Model<>("model"));
	}

	/**
	 * Test constructors.
	 */
	@Test
	public void nullValueDoesntFailConstruction()
	{
		new AttributeModifier("test", null);
	}

	/**
	 * Test that a null model does not throw null pointers.
	 */
	@Test
	public void nullModelDoesNotThrowNullPointerExceptions()
	{
		AttributeModifier modifier = new AttributeModifier("test", null);
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("foo");
		tag.setName("test");
		modifier.replaceAttributeValue(null, tag);
		Map<String, Object> attributes = tag.getAttributes();
		assertTrue(attributes.isEmpty());
	}

	/**
	 * Test overriding newValue (and using a null model).
	 */
	@Test
	public void testNewValue()
	{
		AttributeModifier modifier = new AttributeModifier("test", null)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String newValue(String currentValue, String replacementValue)
			{
				return "the replacement";
			}
		};
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		modifier.replaceAttributeValue(null, tag);
		Map<String, Object> attributes = tag.getAttributes();
		assertTrue(!attributes.isEmpty());
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("the replacement", replacement);
	}

	/**
	 * Test simple model replacement.
	 */
	@Test
	public void testModelReplacement()
	{
		AttributeModifier modifier = new AttributeModifier("test", Model.of("Ellioth Smith Rocks"));
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		modifier.replaceAttributeValue(null, tag);
		Map<String, Object> attributes = tag.getAttributes();
		assertTrue(!attributes.isEmpty());
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("Ellioth Smith Rocks", replacement);
	}

	/**
	 * Test that the current attribute is overwritten by the one that the model provides.
	 */
	@Test
	public void testModelReplacementOverwritingExistingAttributeValue()
	{
		AttributeModifier modifier = new AttributeModifier("test", Model.of("Ellioth Smith Rocks"));
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		Map<String, Object> attributes = tag.getAttributes();
		attributes.put("test", "My mother rocks");
		modifier.replaceAttributeValue(null, tag);
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("Ellioth Smith Rocks", replacement);
	}

	/**
	 * Test that that the attribute modifier does nothing with not enabled.
	 */
	@Test
	public void testNoNewValueWhenNotEnabled()
	{
		AttributeModifier modifier = new AttributeModifier("test", Model.of("Ellioth Smith Rocks"))
		{
			@Override
			public boolean isEnabled(Component component)
			{
				return false;
			}
		};

		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		Map<String, Object> attributes = tag.getAttributes();
		attributes.put("test", "My mother rocks");
		modifier.replaceAttributeValue(null, tag);
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("My mother rocks", replacement);
	}

	/**
	 * Test using newValue for appending to the model value.
	 */
	@Test
	public void testNewValueForModelValue()
	{
		AttributeModifier modifier = new AttributeModifier("test", Model.of("happy"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String newValue(String currentValue, String replacementValue)
			{
				return replacementValue + " together";
			}
		};
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		modifier.replaceAttributeValue(null, tag);
		Map<String, Object> attributes = tag.getAttributes();
		assertTrue(!attributes.isEmpty());
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("happy together", replacement);
	}

	/**
	 * Test using newValue for appending to the current attribute value.
	 */
	@Test
	public void testNewValueForAttributeValue()
	{
		AttributeModifier modifier = new AttributeModifier("test", null)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected String newValue(String currentValue, String replacementValue)
			{
				return currentValue + " two";
			}
		};
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		Map<String, Object> attributes = tag.getAttributes();
		attributes.put("test", "one");
		modifier.replaceAttributeValue(null, tag);
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("one two", replacement);
	}

	/**
	 * Test
	 */
	@Test
	public void testNewValue1Append()
	{
		AttributeModifier appender = AttributeModifier.append("attr", null);
		assertEquals("oldvalue newvalue", appender.newValue("oldvalue", "newvalue"));
		assertEquals("newvalue", appender.newValue("", "newvalue"));
		assertEquals("newvalue", appender.newValue(null, "newvalue"));
		assertEquals("oldvalue", appender.newValue("oldvalue", ""));
		assertEquals("oldvalue", appender.newValue("oldvalue", null));
		assertNull(appender.newValue(null, null));
	}

	/**
	 * Test
	 */
	@Test
	public void testNewValue1Prepend()
	{
		AttributeModifier prepender = AttributeModifier.prepend("attr", null);
		assertEquals("newvalue oldvalue", prepender.newValue("oldvalue", "newvalue"));
		assertEquals("newvalue", prepender.newValue("", "newvalue"));
		assertEquals("newvalue", prepender.newValue(null, "newvalue"));
		assertEquals("oldvalue", prepender.newValue("oldvalue", ""));
		assertEquals("oldvalue", prepender.newValue("oldvalue", null));
		assertNull(prepender.newValue(null, null));
	}

	/**
	 * Test
	 */
	@Test
	public void testNewValue2Append()
	{
		AttributeModifier appender = AttributeModifier.append("attr", null).setSeparator(";");
		assertEquals("oldvalue;newvalue", appender.newValue("oldvalue", "newvalue"));
		assertEquals("newvalue", appender.newValue("", "newvalue"));
		assertEquals("newvalue", appender.newValue(null, "newvalue"));
		assertEquals("oldvalue", appender.newValue("oldvalue", ""));
		assertEquals("oldvalue", appender.newValue("oldvalue", null));
		assertNull(appender.newValue(null, null));
	}

	/**
	 * Test
	 */
	@Test
	public void testNewValue2Prepend()
	{
		AttributeModifier appender = AttributeModifier.prepend("attr", null).setSeparator(";");
		assertEquals("newvalue;oldvalue", appender.newValue("oldvalue", "newvalue"));
		assertEquals("newvalue", appender.newValue("", "newvalue"));
		assertEquals("newvalue", appender.newValue(null, "newvalue"));
		assertEquals("oldvalue", appender.newValue("oldvalue", ""));
		assertEquals("oldvalue", appender.newValue("oldvalue", null));
		assertNull(appender.newValue(null, null));
	}

	/**
	 * Test that a null model does not append an empty attribute
	 * https://issues.apache.org/jira/browse/WICKET-3884
	 */
	@Test
	public void nullModelDoesNotAppendEmptyAttribute()
	{
		AttributeModifier appender = AttributeModifier.append("class", null);
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		appender.replaceAttributeValue(null, tag);
		Map<String, Object> attributes = tag.getAttributes();
		assertTrue(attributes.isEmpty());
	}

	/**
	 * Tests {@link AttributeModifier#remove(String)}
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-3934
	 */
	@Test
	public void removeAttribute()
	{
		AttributeModifier appender = AttributeModifier.remove("class");
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		Map<String, Object> attributes = tag.getAttributes();
		attributes.put("class", "someValue");
		appender.replaceAttributeValue(null, tag);
		assertTrue(attributes.isEmpty());
	}

	/**
	 * Add an attribute with name equal (Object#equals()) to the special
	 * {@link AttributeModifier#VALUELESS_ATTRIBUTE_REMOVE} but not identity equal
	 * 
	 * https://issues.apache.org/jira/browse/WICKET-3934
	 */
	@Test
	public void appendSpecialAttribute()
	{
		String attrName = "attrName";
		AttributeModifier appender = AttributeModifier.append(attrName, "VA_REMOVE");
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		Map<String, Object> attributes = tag.getAttributes();
		attributes.put(attrName, "VA_REMOVE");
		appender.replaceAttributeValue(null, tag);
		assertFalse(attributes.isEmpty());
		assertEquals("VA_REMOVE VA_REMOVE", attributes.get(attrName));
	}
}
