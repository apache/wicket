/*
 * $Id$
 * $Revision$
 * $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.tester;

import java.util.Iterator;

import wicket.markup.MarkupElement;
import wicket.markup.parser.XmlPullParser;
import wicket.markup.parser.XmlTag;
import wicket.util.string.Strings;
import wicket.util.value.AttributeMap;

/**
 * Tag tester is used to test that a generated markup tag contains the correct
 * attributes, values etc. This can be done instead of comparing generated
 * markup with some expected markup. The advantage of this is that a lot of
 * tests doesn't fail, when the generated markup changes just a little bit.
 * <p>
 * It also gives a more programmatic way of testing the generated output, by not
 * having to worry about how the markup looks precisely instead of which
 * attributes exists on the given tags and what values they have.
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
 * @author Frank Bille (billen)
 */
public class TagTester
{
	private XmlTag openTag;

	private XmlTag closeTag;

	private XmlPullParser parser;

	/**
	 * Construct.
	 * 
	 * @param parser
	 * @param openTag
	 * @param closeTag
	 */
	private TagTester(XmlPullParser parser, XmlTag openTag, XmlTag closeTag)
	{
		this.parser = parser;
		this.openTag = openTag;
		this.closeTag = closeTag;
	}

	/**
	 * Get the tag name.
	 * 
	 * @return Tag name.
	 */
	public String getName()
	{
		return openTag.getName();
	}

	/**
	 * Does the tag contain the attribute. Please note that this is case
	 * in-sensitive, because attributes in HTML may be case in-sensitive.
	 * 
	 * @param attribute
	 *            The attribute to look for in the tag.
	 * @return True if the tag has an attribute, false if not.
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
	 * Get the attribute value for the given attribute. Please note that this is
	 * case in-sensitive, because attributes in HTML may be case in-sensitive.
	 * 
	 * @param attribute
	 *            The attribute to look for in the tag.
	 * @return The value of the attribute or null if it isn't found.
	 */
	public String getAttribute(String attribute)
	{
		String value = null;

		AttributeMap attributeMap = openTag.getAttributes();

		if (attributeMap != null) 
		{
			for (Iterator iter = attributeMap.keySet().iterator(); iter.hasNext();)
			{
				String attr = (String) iter.next();

				if (attr.equalsIgnoreCase(attribute))
				{
					value = attributeMap.getString(attr);
				}
			}
		}

		return value;
	}

	/**
	 * Check if an attribute contains the specified partial value.
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
	 * <b>Test</b>
	 * 
	 * <pre>
	 * TagTester tester = application.getTagByWicketId(&quot;helloComp&quot;);
	 * assertTrue(tester.getAttributeContains(&quot;class&quot;, &quot;style2&quot;));
	 * </pre>
	 * 
	 * @param attribute
	 *            The attribute to test on
	 * @param partialValue
	 *            The partial value to test if the attribute value contains.
	 * @return True if the attribute value contains the partial value.
	 */
	public boolean getAttributeContains(String attribute, String partialValue)
	{
		boolean contains = false;

		if (partialValue != null)
		{
			String value = getAttribute(attribute);

			if (value != null)
			{
				if (value.indexOf(partialValue) > -1)
				{
					contains = true;
				}
			}
		}

		return contains;
	}

	/**
	 * Check if an attributes value is the exact same as the given parameter.
	 * 
	 * @param attribute
	 *            The attribute to test.
	 * @param expected
	 *            The value which should be the same at the attributes value
	 * @return True if the attributes value is the same as the parameter.
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
	 * Check if an attributes value ends with the given parameter.
	 * 
	 * @param attribute
	 * @param expected
	 * @return True if the attributes value ends with the expected value
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
	 * Check if the tag has a child with the tagName.
	 * 
	 * @param tagName
	 *            The tag name to search for.
	 * @return True if this tag has a child with the given tagName.
	 */
	public boolean hasChildTag(String tagName)
	{
		boolean hasChild = false;

		if (Strings.isEmpty(tagName))
		{
			throw new IllegalArgumentException("You need to provide a not empty/not null argument.");
		}
		
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

					XmlTag tag = null;
					while((tag = (XmlTag) p.nextTag()) != null)
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
				throw new IllegalStateException();
			}

		}

		return hasChild;
	}

	/**
	 * Get a child tag for testing. If this tag contains child tags, you can get
	 * one of them as a TagTester.
	 * 
	 * @param attribute
	 *            The attribute on the child tag to search for
	 * @param value
	 *            The value that the attribute must have.
	 * @return The TagTester for the child tag.
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
	 * Get markup for this tag. This includes every markup which is between the
	 * open tag and the close tag.
	 * 
	 * @return The entire markup between the open tag and the close tag.
	 */
	public String getMarkup()
	{
		int openPos = openTag.getPos();
		int closePos = closeTag.getPos() + closeTag.getLength();
		String markup = parser.getInput(openPos, closePos).toString();

		return markup;
	}

	/**
	 * Static factory method for creating a TagTester based on a tag found by an
	 * attribute with a specific value. Please note that it will return the
	 * first tag which matches the criteria. It's therefore good for attributes
	 * suck as "id" or "wicket:id", but only if "wicket:id" is unique in the
	 * specified markup.
	 * 
	 * @param markup
	 *            The markup to look for the tag to create the TagTester from.
	 * @param attribute
	 *            The attribute which should be on the tag in the markup.
	 * @param value
	 *            The value which the attribute must have.
	 * @return The TagTester which matches the tag in the markup, that has the
	 *         given value on the given attribute.
	 */
	public static TagTester createTagByAttribute(String markup, String attribute, String value)
	{
		TagTester tester = null;

		if (Strings.isEmpty(markup) == false && Strings.isEmpty(attribute) == false
				&& Strings.isEmpty(value) == false)
		{
			try
			{
				XmlPullParser parser = new XmlPullParser();
				parser.parse(markup);

				MarkupElement elm = null;
				XmlTag openTag = null;
				XmlTag closeTag = null;
				int level = 0;
				while ((elm = parser.nextTag()) != null && closeTag == null)
				{
					if (elm instanceof XmlTag)
					{
						XmlTag xmlTag = (XmlTag)elm;

						if (openTag == null)
						{
							AttributeMap attributeMap = xmlTag.getAttributes();

							for (Iterator iter = attributeMap.keySet().iterator(); iter.hasNext();)
							{
								String attr = (String) iter.next();
								
								if (attr.equals(attribute) && value.equals(attributeMap.get(attr)))
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
				}

				if (openTag != null && closeTag != null)
				{
					tester = new TagTester(parser, openTag, closeTag);
				}
			}
			catch (Exception e)
			{
				throw new IllegalStateException();
			}
		}

		return tester;
	}
}
