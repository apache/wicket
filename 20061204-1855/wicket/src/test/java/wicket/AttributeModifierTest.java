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
package wicket;

import java.util.Map;

import junit.framework.Assert;
import junit.framework.TestCase;
import wicket.markup.ComponentTag;
import wicket.markup.parser.XmlTag;
import wicket.model.Model;

/**
 * Test case for the component tag attribute modifer test.
 * @author Chris Turner
 * @author Eelco Hillenius
 */
public class AttributeModifierTest extends TestCase
{

	/**
	 * Create a test case instance.
	 * @param name The test name
	 */
	public AttributeModifierTest(final String name)
	{
		super(name);
	}

	/**
	 * Test constructors
	 */
	public void testConstructor()
	{
		try
		{
			new AttributeModifier(null, new Model("model"));
			Assert.fail("IllegalArgumentException should be thrown on null attribute name");
		}
		catch (IllegalArgumentException e)
		{
			// Expected result
		}

		try
		{
			new AttributeModifier("test", null);
		}
		catch (IllegalArgumentException e)
		{
			Assert.fail("IllegalArgumentException should not be thrown on null replace model");
		}
	}

	/**
	 * Test that a null model does not throw null pointers.
	 */
	public void testNullModel()
	{
		AttributeModifier modifier = new AttributeModifier("test", null);
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("foo");
		tag.setName("test");
		modifier.replaceAttibuteValue(null, tag);
		Map attributes = tag.getAttributes();
		assertTrue(attributes.isEmpty());
	}

	/**
	 * Test overriding newValue (and using a null model).
	 */
	public void testNewValue()
	{
		AttributeModifier modifier = new AttributeModifier("test", true, null)
		{
			private static final long serialVersionUID = 1L;
			
			protected String newValue(String currentValue, String replacementValue)
			{
				return "the replacement";
			}
		};
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		modifier.replaceAttibuteValue(null, tag);
		Map attributes = tag.getAttributes();
		assertTrue(!attributes.isEmpty());
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("the replacement", replacement);
	}

	/**
	 * Test simple model replacement.
	 */
	public void testModelReplacement()
	{
		AttributeModifier modifier = new AttributeModifier("test", true, new Model(
				"Ellioth Smith Rocks"));
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		modifier.replaceAttibuteValue(null, tag);
		Map attributes = tag.getAttributes();
		assertTrue(!attributes.isEmpty());
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("Ellioth Smith Rocks", replacement);
	}

	/**
	 * Test that an attribute is not added if we didn't want that
	 * (addAttributeIfNotPresent).
	 */
	public void testNoModelReplacementForNonExistingAttributeValue()
	{
		AttributeModifier modifier = new AttributeModifier("test", false, new Model(
				"Ellioth Smith Rocks"));
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		modifier.replaceAttibuteValue(null, tag);
		Map attributes = tag.getAttributes();
		assertTrue("attribute should not be added, as it didn't exist yet", attributes.isEmpty());
	}

	/**
	 * Test that the current attribute is overwritten by the one that the model provides.
	 */
	public void testModelReplacementOverwritingExistingAttributeValue()
	{
		AttributeModifier modifier = new AttributeModifier("test", new Model("Ellioth Smith Rocks"));
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		Map attributes = tag.getAttributes();
		attributes.put("test", "My mother rocks");
		modifier.replaceAttibuteValue(null, tag);
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("Ellioth Smith Rocks", replacement);
	}

	/**
	 * Test that that the attribute modifier does nothing with not enabled.
	 */
	public void testNoNewValueWhenNotEnabled()
	{
		AttributeModifier modifier = new AttributeModifier("test", new Model("Ellioth Smith Rocks"));
		modifier.setEnabled(false);
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		Map attributes = tag.getAttributes();
		attributes.put("test", "My mother rocks");
		modifier.replaceAttibuteValue(null, tag);
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("My mother rocks", replacement);
	}

	/**
	 * Test using newValue for appending to the model value.
	 */
	public void testNewValueForModelValue()
	{
		AttributeModifier modifier = new AttributeModifier("test", true, new Model("happy"))
		{
			private static final long serialVersionUID = 1L;
			
			protected String newValue(String currentValue, String replacementValue)
			{
				return replacementValue + " together";
			}
		};
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		modifier.replaceAttibuteValue(null, tag);
		Map attributes = tag.getAttributes();
		assertTrue(!attributes.isEmpty());
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("happy together", replacement);
	}

	/**
	 * Test using newValue for appending to the current attribute value.
	 */
	public void testNewValueForAttributeValue()
	{
		AttributeModifier modifier = new AttributeModifier("test", null)
		{
			private static final long serialVersionUID = 1L;
			
			protected String newValue(String currentValue, String replacementValue)
			{
				return currentValue + " two";
			}
		};
		XmlTag xmlTag = new XmlTag();
		ComponentTag tag = new ComponentTag(xmlTag);
		tag.setId("test");
		tag.setName("id");
		Map attributes = tag.getAttributes();
		attributes.put("test", "one");
		modifier.replaceAttibuteValue(null, tag);
		String replacement = (String)attributes.get("test");
		assertNotNull(replacement);
		assertEquals("one two", replacement);
	}
}
