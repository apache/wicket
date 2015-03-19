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
package org.apache.wicket.markup.parser;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.WicketParseException;
import org.apache.wicket.markup.parser.filter.HtmlHandler;
import org.apache.wicket.util.collections.ArrayListStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Stack to push and pop HTML elements asserting its structure.
 */
public class TagStack
{
	private static final Logger log = LoggerFactory.getLogger(HtmlHandler.class);

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

	/** Tag stack to find balancing tags */
	final private ArrayListStack<ComponentTag> stack = new ArrayListStack<ComponentTag>();
	private boolean debug;

	/**
	 * Assert that tag has no mismatch error. If the parameter is an open tag, just push it on stack
	 * to be tested latter.
	 * 
	 * @param tag
	 * @throws ParseException
	 */
	public void assertValidInStack(ComponentTag tag) throws ParseException
	{
		// Get the next tag. If null, no more tags are available
		if (tag == null)
		{
			validate();
			return;
		}

		if (log.isDebugEnabled() && debug)
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
			assertOpenTagFor(tag);
		}
		else if (tag.isOpenClose())
		{
			// Tag closes itself
			tag.setOpenTag(tag);
		}
	}

	/**
	 * Bind close tag with its open tag and pop it from the stack.
	 * 
	 * @param closeTag
	 * @throws ParseException
	 */
	private void assertOpenTagFor(ComponentTag closeTag) throws ParseException
	{
		// Check that there is something on the stack
		if (stack.size() > 0)
		{
			// Pop the top tag off the stack
			ComponentTag top = stack.pop();

			// If the name of the current close tag does not match the
			// tag on the stack then we may have a mismatched close tag
			boolean mismatch = !hasEqualTagName(top, closeTag);

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
					mismatch = !hasEqualTagName(top, closeTag);
				}

				// If adjusting for simple tags did not fix the problem,
				// it must be a real mismatch.
				if (mismatch)
				{
					throw new ParseException("Tag " + top.toUserDebugString() +
						" has a mismatched close tag at " + closeTag.toUserDebugString(),
						top.getPos());
				}
			}

			// Tag matches, so add pointer to matching tag
			closeTag.setOpenTag(top);
		}
		else
		{
			throw new WicketParseException("Tag does not have a matching open tag:", closeTag);
		}
	}

	private void validate() throws WicketParseException
	{
		ComponentTag top = getNotClosedTag();
		if (top != null)
		{
			throw new WicketParseException("Tag does not have a close tag:", top);
		}
	}

	/**
	 * @return not closed tag
	 */
	public ComponentTag getNotClosedTag()
	{
		// No more tags from the markup.
		// If there's still a non-simple tag left, it's an error
		if (stack.size() > 0)
		{
			for (int i = 0; i < stack.size(); i++)
			{
				ComponentTag tag = stack.get(i);
				if (!requiresCloseTag(tag.getName()))
				{
					stack.pop();
				}
				else
				{
					return tag;
				}
			}
		}
		return null;
	}

	/**
	 * Configure this stack to call log.debug at operations
	 */
	public void debug()
	{
		debug = true;
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
