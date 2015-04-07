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
package org.apache.wicket.util.tester;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.IValueMap;


/**
 * Tag tester is used to test that a generated markup tag contains the correct attributes, values
 * etc. This can be done instead of comparing generated markup with some expected markup. The
 * advantage of this is that a lot of tests don't fail when the generated markup changes just a
 * little bit.
 * <p>
 * It also gives a more programmatic way of testing the generated output, by not having to worry
 * about precisely how the markup looks instead of which attributes exists on the given tags, and
 * what values they have.
 * <p>
 * Example:
 * 
 * <pre>
 *  ...
 *  TagTester tagTester = application.getTagByWicketId(&quot;form&quot;);
 *  assertTrue(tag.hasAttribute(&quot;action&quot;));
 *  ...
 * </pre>
 * 
 * @since 1.2.6
 */
public class TagTester
{
	private static final Pattern AJAX_COMPONENT_CDATA_OPEN = Pattern.compile("<component.*?><!\\[CDATA\\[");
	private static final Pattern AJAX_COMPONENT_CDATA_CLOSE = Pattern.compile("\\]\\]></component>");

	private final XmlTag openTag;

	private final XmlTag closeTag;

	private final XmlPullParser parser;

	/**
	 * Constructor.
	 * 
	 * @param parser
	 *            an <code>XmlPullParser</code>
	 * @param openTag
	 *            an opening XML tag
	 * @param closeTag
	 *            a closing XML tag
	 */
	private TagTester(XmlPullParser parser, XmlTag openTag, XmlTag closeTag)
	{
		this.parser = parser;
		this.openTag = openTag;
		this.closeTag = closeTag;
	}

	/**
	 * Gets the tag's name.
	 * 
	 * @return the tag name
	 */
	public String getName()
	{
		return openTag.getName();
	}

	/**
	 * Tests if the tag contains the given attribute. Please note that this is non case-sensitive,
	 * because attributes in HTML may be non case-sensitive.
	 * 
	 * @param attribute
	 *            an attribute to look for in the tag
	 * @return <code>true</code> if the tag has the attribute, <code>false</code> if not.
	 */
	public boolean hasAttribute(String attribute)
	{
		boolean hasAttribute = false;

		if (getAttribute(attribute) != null)
		{
			hasAttribute = true;
		}

		return hasAttribute;
	}

	/**
	 * Gets the value for a given attribute. Please note that this is non case-sensitive, because
	 * attributes in HTML may be non case-sensitive.
	 * 
	 * @param attribute
	 *            an attribute to look for in the tag
	 * @return the value of the attribute or <code>null</code> if it isn't found.
	 */
	public String getAttribute(String attribute)
	{
		String value = null;

		IValueMap attributeMap = openTag.getAttributes();

		if (attributeMap != null)
		{
			for (String attr : attributeMap.keySet())
			{
				if (attr.equalsIgnoreCase(attribute))
				{
					value = attributeMap.getString(attr);
				}
			}
		}

		return value;
	}

	/**
	 * Checks if an attribute contains the specified partial value.
	 * <p>
	 * For example:
	 * 
	 * <p>
	 * <b>Markup:</b>
	 * 
	 * <pre>
	 *  &lt;span wicket:id=&quot;helloComp&quot; class=&quot;style1 style2&quot;&gt;Hello&lt;/span&gt;
	 * </pre>
	 * 
	 * <p>
	 * <b>Test:</b>
	 * 
	 * <pre>
	 * TagTester tester = application.getTagByWicketId(&quot;helloComp&quot;);
	 * assertTrue(tester.getAttributeContains(&quot;class&quot;, &quot;style2&quot;));
	 * </pre>
	 * 
	 * @param attribute
	 *            the attribute to test on
	 * @param partialValue
	 *            the partial value to test if the attribute value contains it
	 * @return <code>true</code> if the attribute value contains the partial value
	 */
	public boolean getAttributeContains(String attribute, String partialValue)
	{
		boolean contains = false;

		if (partialValue != null)
		{
			String value = getAttribute(attribute);

			if (value != null)
			{
				if (value.contains(partialValue))
				{
					contains = true;
				}
			}
		}

		return contains;
	}

	/**
	 * Checks if an attribute's value is the exact same as the given value.
	 * 
	 * @param attribute
	 *            an attribute to test
	 * @param expected
	 *            the value which should be the same at the attribute's value
	 * @return <code>true</code> if the attribute's value is the same as the given value
	 */
	public boolean getAttributeIs(String attribute, String expected)
	{
		boolean is = false;

		String val = getAttribute(attribute);

		if (val == null && expected == null || expected != null && expected.equals(val))
		{
			is = true;
		}

		return is;
	}

	/**
	 * Checks if an attribute's value ends with the given parameter.
	 * 
	 * @param attribute
	 *            an attribute to test
	 * @param expected
	 *            the expected value
	 * @return <code>true</code> if the attribute's value ends with the expected value
	 */
	public boolean getAttributeEndsWith(String attribute, String expected)
	{
		boolean endsWith = false;

		if (expected != null)
		{
			String val = getAttribute(attribute);

			if (val != null)
			{
				if (val.endsWith(expected))
				{
					endsWith = true;
				}
			}
		}

		return endsWith;
	}

	/**
	 * Checks if the tag has a child with the given <code>tagName</code>.
	 *
	 * @param tagName
	 *            the tag name to search for
	 * @return <code>true</code> if this tag has a child with the given <code>tagName</code>.
	 */
	public boolean hasChildTag(String tagName)
	{
		Args.notEmpty(tagName, "tagName");

		boolean hasChild = false;

		if (openTag.isOpen())
		{
			try
			{
				// Get the content of the tag
				int startPos = openTag.getPos() + openTag.getLength();
				int endPos = closeTag.getPos();
				String markup = parser.getInput(startPos, endPos).toString();

				if (Strings.isEmpty(markup) == false)
				{
					XmlPullParser p = new XmlPullParser();
					p.parse(markup);

					XmlTag tag;
					while ((tag = p.nextTag()) != null)
					{
						if (tagName.equalsIgnoreCase(tag.getName()))
						{
							hasChild = true;
							break;
						}
					}
				}
			}
			catch (Exception e)
			{
				throw new WicketRuntimeException(e);
			}
		}

		return hasChild;
	}

	/**
	 * Checks if the tag has a child with the given <code>tagName</code>.
	 *
	 * @param tagName
	 *            the tag name to search for
	 * @return <code>true</code> if this tag has a child with the given <code>tagName</code>.
	 */
	public TagTester getChild(String tagName)
	{
		Args.notNull(tagName, "tagName");

		TagTester childTagTester = null;

		if (openTag.isOpen())
		{
			// Get the content of the tag
			int startPos = openTag.getPos() + openTag.getLength();
			int endPos = closeTag.getPos();
			String markup = parser.getInput(startPos, endPos).toString();

			childTagTester = createTagByAttribute(markup, tagName);
		}

		return childTagTester;
	}

	/**
	 * Gets a child tag for testing. If this tag contains child tags, you can get one of them as a
	 * {@link TagTester} instance.
	 * 
	 * @param attribute
	 *            an attribute on the child tag to search for
	 * @param value
	 *            a value that the attribute must have
	 * @return the <code>TagTester</code> for the child tag
	 */
	public TagTester getChild(String attribute, String value)
	{
		TagTester childTag = null;

		if (openTag.isOpen())
		{
			// Generate the markup for this tag
			String markup = getMarkup();

			if (Strings.isEmpty(markup) == false)
			{
				childTag = TagTester.createTagByAttribute(markup, attribute, value);
			}
		}

		return childTag;
	}

	/**
	 * Gets the markup for this tag. This includes all markup between the open tag and the close
	 * tag.
	 * 
	 * @return all the markup between the open tag and the close tag
	 */
	public String getMarkup()
	{
		int openPos = openTag.getPos();
		int closePos = closeTag.getPos() + closeTag.getLength();

		return parser.getInput(openPos, closePos).toString();
	}

	/**
	 * Returns the value for this tag. This includes all data between the open tag and the close
	 * tag.
	 * 
	 * @return all the data between the open tag and the close tag
	 * @since 1.3
	 */
	public String getValue()
	{
		int openPos = openTag.getPos() + openTag.getLength();
		int closePos = closeTag.getPos();

		return parser.getInput(openPos, closePos).toString();
	}

	/**
	 * Static factory method for creating a <code>TagTester</code> based on a tag name. Please note
	 * that it will return the first tag which matches the criteria.
	 *
	 * @param markup
	 *            the markup to look for the tag to create the <code>TagTester</code> from
	 *            the value which the attribute must have
	 * @return the <code>TagTester</code> which matches the tag by name in the markup
	 */
	public static TagTester createTagByAttribute(String markup, String tagName)
	{
		TagTester tester = null;

		if (Strings.isEmpty(markup) == false && Strings.isEmpty(tagName) == false)
		{
			try
			{
				// remove the CDATA and
				// the id attribute of the component because it is often the same as the element's id
				markup = AJAX_COMPONENT_CDATA_OPEN.matcher(markup).replaceAll("<component>");
				markup = AJAX_COMPONENT_CDATA_CLOSE.matcher(markup).replaceAll("</component>");

				XmlPullParser parser = new XmlPullParser();
				parser.parse(markup);

				XmlTag elm;
				XmlTag openTag = null;
				XmlTag closeTag = null;
				int level = 0;
				while ((elm = parser.nextTag()) != null && closeTag == null)
				{
					XmlTag xmlTag = elm;

					String xmlTagName = xmlTag.getName();
					if (openTag == null && xmlTagName.equalsIgnoreCase(tagName))
					{
						if (xmlTag.isOpen())
						{
							openTag = xmlTag;
						}
						else if (xmlTag.isOpenClose())
						{
							openTag = xmlTag;
							closeTag = xmlTag;
						}
					}
					else if (openTag != null)
					{
						String openTagName = openTag.getName();
						if (xmlTag.isOpen() && xmlTagName.equals(openTagName))
						{
							level++;
						}

						if (xmlTag.isClose())
						{
							if (xmlTagName.equals(openTagName))
							{
								if (level == 0)
								{
									closeTag = xmlTag;
									closeTag.setOpenTag(openTag);
								}
								else
								{
									level--;
								}
							}
						}
					}
				}

				if (openTag != null && closeTag != null)
				{
					tester = new TagTester(parser, openTag, closeTag);
				}
			}
			catch (Exception e)
			{
				throw new WicketRuntimeException(e);
			}
		}

		return tester;
	}

	/**
	 * Static factory method for creating a <code>TagTester</code> based on a tag found by an
	 * attribute with a specific value. Please note that it will return the first tag which matches
	 * the criteria. It's therefore good for attributes such as "id" or "wicket:id", but only if
	 * "wicket:id" is unique in the specified markup.
	 * 
	 * @param markup
	 *            the markup to look for the tag to create the <code>TagTester</code> from
	 * @param attribute
	 *            the attribute which should be on the tag in the markup
	 * @param value
	 *            the value which the attribute must have
	 * @return the <code>TagTester</code> which matches the tag in the markup, that has the given
	 *         value on the given attribute
	 */
	public static TagTester createTagByAttribute(String markup, String attribute, String value)
	{
		TagTester tester = null;

		if (Strings.isEmpty(markup) == false && Strings.isEmpty(attribute) == false &&
			Strings.isEmpty(value) == false)
		{
			try
			{
				// remove the CDATA and
				// the id attribute of the component because it is often the same as the element's id
				markup = AJAX_COMPONENT_CDATA_OPEN.matcher(markup).replaceAll("<component>");
				markup = AJAX_COMPONENT_CDATA_CLOSE.matcher(markup).replaceAll("</component>");

				XmlPullParser parser = new XmlPullParser();
				parser.parse(markup);

				XmlTag elm;
				XmlTag openTag = null;
				XmlTag closeTag = null;
				int level = 0;
				while ((elm = parser.nextTag()) != null && closeTag == null)
				{
					XmlTag xmlTag = elm;

					if (openTag == null)
					{
						IValueMap attributeMap = xmlTag.getAttributes();

						for (Map.Entry<String, Object> entry : attributeMap.entrySet())
						{
							String attr = entry.getKey();
							if (attr.equals(attribute) && value.equals(entry.getValue()))
							{
								if (xmlTag.isOpen())
								{
									openTag = xmlTag;
								}
								else if (xmlTag.isOpenClose())
								{
									openTag = xmlTag;
									closeTag = xmlTag;
								}
							}
						}
					}
					else
					{
						if (xmlTag.isOpen() && xmlTag.getName().equals(openTag.getName()))
						{
							level++;
						}

						if (xmlTag.isClose())
						{
							if (xmlTag.getName().equals(openTag.getName()))
							{
								if (level == 0)
								{
									closeTag = xmlTag;
									closeTag.setOpenTag(openTag);
								}
								else
								{
									level--;
								}
							}
						}
					}
				}

				if (openTag != null && closeTag != null)
				{
					tester = new TagTester(parser, openTag, closeTag);
				}
				else if (openTag != null)
				{
					tester = new TagTester(parser, openTag, openTag);
				}
			}
			catch (Exception e)
			{
				throw new WicketRuntimeException(e);
			}
		}

		return tester;
	}

	/**
	 * Static factory method for creating a <code>TagTester</code> based on a tag found by an
	 * attribute with a specific value. Please note that it will return the first tag which matches
	 * the criteria. It's therefore good for attributes such as "id" or "wicket:id", but only if
	 * "wicket:id" is unique in the specified markup.
	 * 
	 * @param markup
	 *            the markup to look for the tag to create the <code>TagTester</code> from
	 * @param attribute
	 *            the attribute which should be on the tag in the markup
	 * @param value
	 *            the value which the attribute must have
	 * @return the <code>TagTester</code> which matches the tag in the markup, that has the given
	 *         value on the given attribute
	 */
	public static TagTester createTagsByAttribute(String markup, String attribute, String value)
	{
		List<TagTester> tester = createTagsByAttribute(markup, attribute, value, true);
		if ((tester == null) || (tester.size() == 0))
		{
			return null;
		}
		return tester.get(0);
	}

	/**
	 * Static factory method for creating a <code>TagTester</code> based on a tag found by an
	 * attribute with a specific value. Please note that it will return the first tag which matches
	 * the criteria. It's therefore good for attributes suck as "id" or "wicket:id", but only if
	 * "wicket:id" is unique in the specified markup.
	 * 
	 * @param markup
	 *            the markup to look for the tag to create the <code>TagTester</code> from
	 * @param attribute
	 *            the attribute which should be on the tag in the markup
	 * @param value
	 *            the value which the attribute must have
	 * @param stopAfterFirst
	 *            if true search will stop after the first match
	 * @return the <code>TagTester</code> which matches the tag in the markup, that has the given
	 *         value on the given attribute
	 */
	public static List<TagTester> createTagsByAttribute(String markup, String attribute,
		String value, boolean stopAfterFirst)
	{
		List<TagTester> testers = new ArrayList<>();

		if ((Strings.isEmpty(markup) == false) && (Strings.isEmpty(attribute) == false) &&
			(Strings.isEmpty(value) == false))
		{
			try
			{
				// remove the CDATA and
				// the id attribute of the component because it is often the same as the element's id
				markup = AJAX_COMPONENT_CDATA_OPEN.matcher(markup).replaceAll("<component>");
				markup = AJAX_COMPONENT_CDATA_CLOSE.matcher(markup).replaceAll("</component>");

				XmlPullParser parser = new XmlPullParser();
				parser.parse(markup);

				XmlTag elm = null;
				XmlTag openTag = null;
				XmlTag closeTag = null;
				int level = 0;
				while ((elm = parser.nextTag()) != null)
				{
					XmlTag xmlTag = elm;
					if (openTag == null)
					{
						IValueMap attributeMap = xmlTag.getAttributes();
						for (Map.Entry<String, Object> entry : attributeMap.entrySet())
						{
							if (entry.getKey().equals(attribute) && value.equals(entry.getValue()))
							{
								if (xmlTag.isOpen())
								{
									openTag = xmlTag;
								}
								else if (xmlTag.isOpenClose())
								{
									openTag = xmlTag;
									closeTag = xmlTag;
								}
							}
						}
					}
					else
					{
						if (xmlTag.isOpen() && xmlTag.getName().equals(openTag.getName()))
						{
							level++;
						}

						if (xmlTag.isClose())
						{
							if (xmlTag.getName().equals(openTag.getName()))
							{
								if (level == 0)
								{
									closeTag = xmlTag;
									closeTag.setOpenTag(openTag);
								}
								else
								{
									level--;
								}
							}
						}
					}

					if ((openTag != null) && (closeTag != null) && (level == 0))
					{
						TagTester tester = new TagTester(parser, openTag, closeTag);
						testers.add(tester);
						openTag = null;
						closeTag = null;
					}

					if (stopAfterFirst && (closeTag != null))
					{
						break;
					}
				}
			}
			catch (Exception e)
			{
				// NOTE: IllegalStateException(Throwable) only exists since Java 1.5
				throw new WicketRuntimeException(e);
			}
		}

		return testers;
	}
}
