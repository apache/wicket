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
package org.apache.wicket.markup.parser.filter;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.Markup;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.WicketParseException;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.util.collections.ArrayListStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is a markup inline filter. It identifies HTML specific issues which make HTML not 100% xml
 * compliant. E.g. tags like &lt;p&gt; often are missing the corresponding close tag.
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
		doesNotRequireCloseTag.put("p", Boolean.TRUE);
		doesNotRequireCloseTag.put("br", Boolean.TRUE);
		doesNotRequireCloseTag.put("img", Boolean.TRUE);
		doesNotRequireCloseTag.put("input", Boolean.TRUE);
		doesNotRequireCloseTag.put("hr", Boolean.TRUE);
		doesNotRequireCloseTag.put("link", Boolean.TRUE);
		doesNotRequireCloseTag.put("meta", Boolean.TRUE);
	}

	/**
	 * Construct.
	 */
	public HtmlHandler()
	{
	}

	@Override
	public void postProcess(final Markup markup)
	{
		// If there's still a non-simple tag left, it's an error
		while (stack.size() > 0)
		{
			final ComponentTag top = stack.peek();

			if (!requiresCloseTag(top.getName()))
			{
				stack.pop();
				top.setHasNoCloseTag(true);
			}
			else
			{
				throw new MarkupException(markup, "Tag does not have a close tag", null);
			}
		}
	}

	@Override
	protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException
	{
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
				boolean mismatch = !hasEqualTagName(top, tag);

				if (mismatch)
				{
					top.setHasNoCloseTag(true);

					// Pop any simple tags off the top of the stack
					while (mismatch && !requiresCloseTag(top.getName()))
					{
						top.setHasNoCloseTag(true);

						// Pop simple tag
						if (stack.isEmpty())
						{
							break;
						}
						top = stack.pop();

						// Does new top of stack mismatch too?
						mismatch = !hasEqualTagName(top, tag);
					}

					// If adjusting for simple tags did not fix the problem,
					// it must be a real mismatch.
					if (mismatch)
					{
						throw new ParseException("Tag " + top.toUserDebugString() +
							" has a mismatched close tag at " + tag.toUserDebugString(),
							top.getPos());
					}
				}

				// Tag matches, so add pointer to matching tag
				tag.setOpenTag(top);
			}
			else
			{
				throw new WicketParseException("Tag does not have a matching open tag:", tag);
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
		return doesNotRequireCloseTag.get(name.toLowerCase()) == null;
	}

	/**
	 * Compare tag name including namespace
	 * 
	 * @param tag1
	 * @param tag2
	 * @return true if name and namespace are equal
	 */
	public static boolean hasEqualTagName(final ComponentTag tag1, final ComponentTag tag2)
	{
		if (!tag1.getName().equalsIgnoreCase(tag2.getName()))
		{
			return false;
		}

		if ((tag1.getNamespace() == null) && (tag2.getNamespace() == null))
		{
			return true;
		}

		if ((tag1.getNamespace() != null) && (tag2.getNamespace() != null))
		{
			return tag1.getNamespace().equalsIgnoreCase(tag2.getNamespace());
		}

		return false;
	}
}
