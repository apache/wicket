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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.collections.ArrayListStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Simple class that provides a convenient programmatic way to define what an expected HTML document
 * should look like and then to validate a supplied document against this template. Note that this
 * validator expects very clean HTML (which should not be a problem during testing). In particular
 * it expects tags to be matched and that the following tags with optional close tags are actually
 * closed: p, td, th, li and option.
 * 
 * @author Chris Turner
 * @deprecated Will be removed in Wicket 9.0. Use {@link org.apache.wicket.util.tester.TagTester} or
 * {@link org.apache.wicket.util.tester.WicketTestCase#executeTest(Class, PageParameters, String)} instead
 */
@Deprecated
public class HtmlDocumentValidator
{
	private static final Logger log = LoggerFactory.getLogger(HtmlDocumentValidator.class);

	private final List<DocumentElement> elements = new ArrayList<DocumentElement>();

	private boolean skipComments = true;

	private Tag workingTag;

	/**
	 * Create the validator.
	 */
	public HtmlDocumentValidator()
	{
	}

	/**
	 * Add a root element to the validator. This will generally be the HTML element to which all
	 * children are added. However, it may also be other elements to represent comments or similar.
	 * 
	 * @param e
	 *            The element to add
	 */
	public void addRootElement(final DocumentElement e)
	{
		elements.add(e);
	}

	/**
	 * Check whether the supplied document is valid against the spec that has been built up within
	 * the validator.
	 * 
	 * @param document
	 *            The document to validate
	 * @return Whether the document is valid or not
	 */
	public boolean isDocumentValid(final String document)
	{
		HtmlDocumentParser parser = new HtmlDocumentParser(document);
		Iterator<DocumentElement> expectedElements = elements.iterator();
		ArrayListStack<Iterator<DocumentElement>> iteratorStack = new ArrayListStack<Iterator<DocumentElement>>();
		ArrayListStack<String> tagNameStack = new ArrayListStack<String>();

		boolean end = false;
		boolean valid = true;
		while (!end)
		{
			int token = parser.getNextToken();
			switch (token)
			{
				case HtmlDocumentParser.UNKNOWN :
					// Error is already recorded by the parser
					return false;
				case HtmlDocumentParser.END :
					end = true;
					break;
				case HtmlDocumentParser.COMMENT :
					valid = validateComment(expectedElements, parser);
					if (!valid)
					{
						end = true;
					}
					break;
				case HtmlDocumentParser.OPEN_TAG :
					valid = validateTag(expectedElements, parser);
					if (!valid)
					{
						end = true;
					}
					else
					{
						expectedElements = saveOpenTagState(iteratorStack, expectedElements,
							tagNameStack);
					}
					break;
				case HtmlDocumentParser.OPENCLOSE_TAG :
					valid = validateTag(expectedElements, parser);
					if (valid)
					{
						valid = checkOpenCloseTag();
					}
					if (!valid)
					{
						end = true;
					}
					break;
				case HtmlDocumentParser.CLOSE_TAG :
					expectedElements = validateCloseTag(tagNameStack, parser, expectedElements,
						iteratorStack);
					if (expectedElements == null)
					{
						valid = false;
						end = true;
					}
					break;
				case HtmlDocumentParser.TEXT :
					valid = validateText(expectedElements, parser);
					if (!valid)
					{
						end = true;
					}
					break;
			}
		}

		// Return the valid result
		return valid;
	}

	/**
	 * Set whether to skip comments of not when validating. The default is true. If this is set to
	 * false then Comment elements must be added to represent each comment to be validated.
	 * 
	 * @param skipComments
	 *            Whether to skip comments or not
	 */
	public void setSkipComments(final boolean skipComments)
	{
		this.skipComments = skipComments;
	}

	/**
	 * Check whether the open close tag was actually expected to have children.
	 * 
	 * @return Whether valid or not
	 */
	private boolean checkOpenCloseTag()
	{
		boolean valid = true;
		if (!workingTag.getExpectedChildren().isEmpty())
		{
			log.error("Found tag <" + workingTag.getTag() + "/> was expected to have " +
				workingTag.getExpectedChildren().size() + " child elements");
			valid = false;
		}
		return valid;
	}

	/**
	 * Check if the supplied tag is one that expects to be closed or not.
	 * 
	 * @param tag
	 *            The tag
	 * @return Whether the tag requires closing or not
	 */
	private boolean isNonClosedTag(String tag)
	{
		tag = workingTag.getTag().toLowerCase(Locale.ROOT);
		if (tag.equals("area"))
		{
			return true;
		}
		if (tag.equals("base"))
		{
			return true;
		}
		if (tag.equals("basefont"))
		{
			return true;
		}
		if (tag.equals("bgsound"))
		{
			return true;
		}
		if (tag.equals("br"))
		{
			return true;
		}
		if (tag.equals("col"))
		{
			return true;
		}
		if (tag.equals("frame"))
		{
			return true;
		}
		if (tag.equals("hr"))
		{
			return true;
		}
		if (tag.equals("img"))
		{
			return true;
		}
		if (tag.equals("input"))
		{
			return true;
		}
		if (tag.equals("isindex"))
		{
			return true;
		}
		if (tag.equals("keygen"))
		{
			return true;
		}
		if (tag.equals("link"))
		{
			return true;
		}
		if (tag.equals("meta"))
		{
			return true;
		}
		if (tag.equals("param"))
		{
			return true;
		}
		if (tag.equals("spacer"))
		{
			return true;
		}
		if (tag.equals("wbr"))
		{
			return true;
		}
		return false;
	}

	/**
	 * Save the new open tag state and find the iterator to continue to use for processing.
	 * 
	 * @param iteratorStack
	 *            The current stack of iterators
	 * @param expectedElements
	 *            The current iterator of elements
	 * @param tagNameStack
	 *            The stack of open tags
	 * @return The iterator to continue to use
	 */
	private Iterator<DocumentElement> saveOpenTagState(
		ArrayListStack<Iterator<DocumentElement>> iteratorStack,
		Iterator<DocumentElement> expectedElements, ArrayListStack<String> tagNameStack)
	{
		if (!isNonClosedTag(workingTag.getTag()))
		{
			iteratorStack.push(expectedElements);
			expectedElements = workingTag.getExpectedChildren().iterator();
			tagNameStack.push(workingTag.getTag());
		}
		return expectedElements;
	}

	/**
	 * Validate the close tag that was found.
	 * 
	 * @param tagNameStack
	 *            The stack of tag names
	 * @param parser
	 *            The parser
	 * @param expectedElements
	 *            The current iterator of expected elements
	 * @param iteratorStack
	 *            The stack of previous iterators
	 * @return The next iterator to use, or null
	 */
	private Iterator<DocumentElement> validateCloseTag(ArrayListStack<String> tagNameStack,
		HtmlDocumentParser parser, Iterator<DocumentElement> expectedElements,
		ArrayListStack<Iterator<DocumentElement>> iteratorStack)
	{
		if (tagNameStack.isEmpty())
		{
			log.error("Found closing tag </" + parser.getTag() + "> when there are no " +
				"tags currently open");
			expectedElements = null;
		}
		else
		{
			String expectedTag = tagNameStack.pop();
			if (!expectedTag.equals(parser.getTag()))
			{
				log.error("Found closing tag </" + parser.getTag() + "> when we expecting " +
					"the closing tag </" + expectedTag + "> instead");
				expectedElements = null;
			}
			else
			{
				if (expectedElements.hasNext())
				{
					DocumentElement e = expectedElements.next();
					log.error("Found closing tag </" + parser.getTag() + "> but we were " +
						"expecting to find another child element: " + e.toString());
					expectedElements = null;
				}
				else
				{
					if (iteratorStack.isEmpty())
					{
						log.error("Unexpected parsing error");
						expectedElements = null;
					}
					else
					{
						expectedElements = iteratorStack.pop();
					}
				}
			}
		}
		return expectedElements;
	}

	/**
	 * Validate the comment token that was found.
	 * 
	 * @param expectedElements
	 *            The iterator of expected elements
	 * @param parser
	 *            The parser
	 * @return Whether the comment is valid or not
	 */
	private boolean validateComment(Iterator<DocumentElement> expectedElements,
		HtmlDocumentParser parser)
	{
		boolean valid = true;
		if (!skipComments)
		{
			if (expectedElements.hasNext())
			{
				DocumentElement e = expectedElements.next();
				if (e instanceof Comment)
				{
					if (!((Comment)e).getText().equals(parser.getComment()))
					{
						log.error("Found comment '" + parser.getComment() + "' does not match " +
							"expected comment '" + ((Comment)e).getText() + "'");
						valid = false;
					}
				}
				else
				{
					log.error("Found comment '" + parser.getComment() + "' was not expected. " +
						"We were expecting: " + e.toString());
					valid = false;
				}
			}
			else
			{
				log.error("Found comment '" + parser.getComment() + "' was not expected. " +
					"We were not expecting any more elements within the current tag");
				valid = false;
			}
		}
		return valid;
	}

	/**
	 * Validate the tag token that was found.
	 * 
	 * @param expectedElements
	 *            The iterator of expected elements
	 * @param parser
	 *            The parser
	 * @return Whether the tag is valid or not
	 */
	private boolean validateTag(Iterator<DocumentElement> expectedElements,
		HtmlDocumentParser parser)
	{
		boolean valid = true;
		if (expectedElements.hasNext())
		{
			DocumentElement e = expectedElements.next();
			if (e instanceof Tag)
			{
				workingTag = (Tag)e;
				if (!workingTag.getTag().equals(parser.getTag()))
				{
					log.error("Found tag <" + parser.getTag() + "> does not match " +
						"expected tag <" + workingTag.getTag() + ">");
					valid = false;
				}
				else
				{
					Map<String, String> actualAttributes = parser.getAttributes();

					Map<String, String> expectedAttributes = workingTag.getExpectedAttributes();
					for (Map.Entry<String, String> entry : expectedAttributes.entrySet())
					{
						String name = entry.getKey();
						String pattern = entry.getValue();
						if (!actualAttributes.containsKey(name))
						{
							log.error("Tag <" + workingTag.getTag() + "> was expected to have a '" +
								name + "' attribute " + "but this was not present");
							valid = false;
						}

						String value = actualAttributes.get(name);
						if (value == null)
						{
							log.error("Attribute " + name + " was expected but not found");
							valid = false;
						}
						else
						{
							if (!value.matches(pattern))
							{
								log.error("The value '" + value + "' of attribute '" + name +
									"' of tag <" + workingTag.getTag() +
									"> was expected to match the pattern '" + pattern +
									"' but it does not");
								valid = false;
							}
						}
					}

					for (String name : workingTag.getIllegalAttributes())
					{
						if (actualAttributes.containsKey(name))
						{
							log.error("Tag <" + workingTag.getTag() +
								"> should not have an attributed named '" + name + "'");
							valid = false;
						}
					}
				}
			}
			else
			{
				log.error("Found tag <" + parser.getTag() + "> was not expected. " +
					"We were expecting: " + e.toString());
				valid = false;
			}
		}
		else
		{
			log.error("Found tag <" + parser.getTag() + "> was not expected. " +
				"We were not expecting any more elements within the current tag");
			valid = false;
		}
		return valid;
	}

	/**
	 * Validate the text token that was found.
	 * 
	 * @param expectedElements
	 *            The iterator of expected elements
	 * @param parser
	 *            The parser
	 * @return Whether the text is valid or not
	 */
	private boolean validateText(Iterator<DocumentElement> expectedElements,
		HtmlDocumentParser parser)
	{
		boolean valid = true;
		if (expectedElements.hasNext())
		{
			DocumentElement e = expectedElements.next();
			if (e instanceof TextContent)
			{
				if (!parser.getText().matches(((TextContent)e).getValue()))
				{
					log.error("Found text '" + parser.getText() + "' does not match " +
						"expected text '" + ((TextContent)e).getValue() + "'");
					valid = false;
				}
			}
			else
			{
				log.error("Found text '" + parser.getText() + "' was not expected. " +
					"We were expecting: " + e.toString());
				valid = false;
			}
		}
		else
		{
			log.error("Found text '" + parser.getText() + "' was not expected. " +
				"We were not expecting any more elements within the current tag");
			valid = false;
		}
		return valid;
	}

}
