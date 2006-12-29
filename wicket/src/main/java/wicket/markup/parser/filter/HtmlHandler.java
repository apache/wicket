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
package wicket.markup.parser.filter;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.parser.AbstractMarkupFilter;
import wicket.util.collections.ArrayListStack;

/**
 * This is a markup inline filter. It identifies HTML specific issues which make
 * HTML not 100% xml compliant. E.g. tags like &lt;p&gt; often are missing the
 * corresponding close tag.
 * 
 * @author Juergen Donnerstag
 */
public final class HtmlHandler extends AbstractMarkupFilter
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(HtmlHandler.class);

	/** Tag stack to find balancing tags */
	final private ArrayListStack<ComponentTag> stack = new ArrayListStack<ComponentTag>();

	/** Map of simple tags. */
	private static final Map<String, Boolean> doesNotRequireCloseTag = new HashMap<String, Boolean>();

	static
	{
		// Tags which are allowed not be closed in HTML
		registerHtmlTag("p");
		registerHtmlTag("br");
		registerHtmlTag("img");
		registerHtmlTag("input");
		registerHtmlTag("hr");
		registerHtmlTag("link");
		registerHtmlTag("meta");
	}

	/**
	 * Construct.
	 * 
	 */
	public HtmlHandler()
	{
	}

	/**
	 * Register additional HTML tags which presumable don't need a close tag.
	 * However, instead of doing that, we recommend anyone to use proper XHTML
	 * instead which is XML compliant.
	 * 
	 * @param tagName
	 */
	public static final void registerHtmlTag(final String tagName)
	{
		doesNotRequireCloseTag.put(tagName, Boolean.TRUE);
	}

	/**
	 * Get the next MarkupElement from the parent MarkupFilter and handle it if
	 * the specific filter criteria are met. Depending on the filter, it may
	 * return the MarkupElement unchanged, modified or it remove by asking the
	 * parent handler for the next tag.
	 * 
	 * @see wicket.markup.parser.IMarkupFilter#nextTag()
	 * @return Return the next eligible MarkupElement
	 */
	public MarkupElement nextTag() throws ParseException
	{
		// Get the next tag. If null, no more tags are available
		final ComponentTag tag = nextComponentTag();
		if (tag == null)
		{
			// No more tags from the markup.
			// If there's still a non-simple tag left, it's an error
			while (stack.size() > 0)
			{
				final ComponentTag top = stack.peek();

				if (!requiresCloseTag(top.getName()))
				{
					stack.pop();
				}
				else
				{
					throw new ParseException("Tag " + top + " does not have a close tag.", top
							.getPos());
				}
			}

			return tag;
		}

		if (log.isDebugEnabled())
		{
			log.debug("tag: " + tag.toUserDebugString() + ", stack: " + stack);
		}

		// Check tag type
		if (tag.isOpen())
		{
			// Push onto stack
			stack.push(tag);
		}
		else if (tag.isClose())
		{
			// Check that there is something on the stack
			if (stack.size() > 0)
			{
				// Pop the top tag off the stack
				ComponentTag top = stack.pop();

				// If the name of the current close tag does not match the
				// tag on the stack then we may have a mismatched close tag
				if (top.hasEqualTagName(tag) == false)
				{
					boolean mismatch = true;
					
					// Pop any simple tags off the top of the stack
					while (mismatch && !requiresCloseTag(top.getName()))
					{
						top.setHasNoCloseTag(true);

						// Pop simple tag
						top = stack.pop();

						// Does new top of stack mismatch too?
						mismatch = (top.hasEqualTagName(tag) == false);
					}

					// If adjusting for simple tags did not fix the problem,
					// it must be a real mismatch.
					if (mismatch)
					{
						throw new ParseException("Tag " + top.toUserDebugString()
								+ " has a mismatched close tag at " + tag.toUserDebugString(), top
								.getPos());
					}
				}

				// Tag matches, so add pointer to matching tag
				tag.setOpenTag(top);
			}
			else
			{
				throw new ParseException("Tag " + tag.toUserDebugString()
						+ " does not have a matching open tag", tag.getPos());
			}
		}
		else if (tag.isOpenClose())
		{
			// Tag closes itself
			tag.setOpenTag(tag);
		}

		return tag;
	}

	/**
	 * Gets whether this tag does not require a closing tag.
	 * 
	 * @param name
	 *            The tag's name, e.g. a, br, div, etc.
	 * @return True if this tag does not require a closing tag
	 */
	public static boolean requiresCloseTag(final String name)
	{
		return doesNotRequireCloseTag.get(name) == null;
	}
}
