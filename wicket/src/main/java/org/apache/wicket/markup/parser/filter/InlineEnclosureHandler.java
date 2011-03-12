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
import java.util.Stack;

import org.apache.wicket.Session;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.internal.InlineEnclosure;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.resolver.EnclosureResolver;
import org.apache.wicket.util.string.Strings;


/**
 * This is a markup inline filter. It identifies enclosures as attribute, for example: &lt;tr
 * wicket:enclosure=""&gt;. The &lt;tr&gt; tag used in the example can be replaced with any html tag
 * that can contain child elements. If the 'child' attribute is empty it determines the wicket:id of
 * the child component automatically by analyzing the wicket component (in this case one wicket
 * component is allowed) in between the open and close tags. If the enclosure tag has a 'child'
 * attribute like <code>&lt;tr
 * wicket:enclosure="controllingChildId"&gt;</code> than more than just one wicket component inside
 * the enclosure tags are allowed and the child component which determines the visibility of the
 * enclosure is identified by the 'child' attribute value which must be equal to the relative child
 * id path.
 * 
 * @see EnclosureResolver
 * @see InlineEnclosure
 * 
 * @author Joonas Hamalainen
 */
public final class InlineEnclosureHandler extends AbstractMarkupFilter
{
	private final static String INLINE_ENCLOSURE_ID_PREFIX = "inlineEnclosure-";
	private final static String INLINE_ENCLOSURE_ATTRIBUTE_NAME = "wicket:enclosure";

	private Stack<ComponentTag> enclosures;

	/**
	 * Construct.
	 */
	public InlineEnclosureHandler()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	public final MarkupElement nextTag() throws ParseException
	{
		// Get the next tag from the next MarkupFilter in the chain.
		// If null, no more tags are available
		final ComponentTag tag = nextComponentTag();
		if ((tag == null) || (tag instanceof WicketTag))
		{
			return tag;
		}

		// Has wicket:enclosure attribute?
		String enclosureAttr = getInlineEnclosureAttribute(tag);
		if (enclosureAttr != null)
		{
			if (tag.isOpen())
			{
				String htmlId = tag.getAttribute("id");
				if ((tag.getId() != null) && !Strings.isEmpty(htmlId) &&
					!htmlId.equals(tag.getId()))
				{
					throw new ParseException(
						"Make sure the 'id' and 'wicket:id' should be the same if both are used. Tag:" +
							tag.toString(), tag.getPos());
				}

				// if it doesn't have a wicket-id already, than assign one now.
				if (Strings.isEmpty(tag.getId()))
				{
					if (Strings.isEmpty(htmlId))
					{
						String id = INLINE_ENCLOSURE_ID_PREFIX + Session.get().nextSequenceValue();
						tag.setId(id);
					}
					else
					{
						tag.setId(htmlId);
					}

					tag.setAutoComponentTag(true);
					tag.setModified(true);
				}

				tag.getAttributes().put("id", tag.getId());

				if (enclosures == null)
				{
					enclosures = new Stack<ComponentTag>();
				}
				enclosures.push(tag);
			}
			else
			{
				throw new ParseException(
					"Open-close tags don't make sense for InlineEnclosure. Tag:" + tag.toString(),
					tag.getPos());
			}
		}
		else if ((enclosures != null) && (enclosures.size() > 0))
		{
			if (tag.isOpen() && (tag.getId() != null) && !(tag instanceof WicketTag) &&
				!tag.isAutoComponentTag())
			{
				for (int i = enclosures.size() - 1; i >= 0; i--)
				{
					ComponentTag lastEnclosure = enclosures.get(i);
					String attr = getInlineEnclosureAttribute(lastEnclosure);
					if (Strings.isEmpty(attr) == true)
					{
						lastEnclosure.getAttributes().put(INLINE_ENCLOSURE_ATTRIBUTE_NAME,
							tag.getId());
						lastEnclosure.setModified(true);
					}
				}
			}
			else if (tag.isClose() && tag.closes(enclosures.peek()))
			{
				ComponentTag lastEnclosure = enclosures.pop();
				String attr = getInlineEnclosureAttribute(lastEnclosure);
				if (Strings.isEmpty(attr) == true)
				{
					throw new ParseException("Did not find any child for InlineEnclosure. Tag:" +
						lastEnclosure.toString(), tag.getPos());
				}
			}
		}

		return tag;
	}

	/**
	 * @param tag
	 * @return The wicket:enclosure attribute or null if not found
	 */
	public final static String getInlineEnclosureAttribute(final ComponentTag tag)
	{
		return tag.getAttributes().getString(INLINE_ENCLOSURE_ATTRIBUTE_NAME);
	}
}
