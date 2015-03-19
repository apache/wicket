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
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.internal.InlineEnclosure;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.resolver.IComponentResolver;
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
 * @see InlineEnclosure
 * 
 * @author Joonas Hamalainen
 * @author Juergen Donnerstag
 */
public final class InlineEnclosureHandler extends AbstractMarkupFilter
	implements
		IComponentResolver
{
	private static final long serialVersionUID = 1L;

	/** The Component id prefix. */
	public final static String INLINE_ENCLOSURE_ID_PREFIX = "InlineEnclosure-";

	/** Attribute to identify inline enclosures */
	public final static String INLINE_ENCLOSURE_ATTRIBUTE_NAME = "enclosure";

	/** enclosures inside enclosures */
	private Deque<ComponentTag> enclosures;

	/**
	 * InlineEnclosures are not removed after render as other auto-components,
	 * thus they have to have a stable id.
	 */
	private int counter;

	/**
	 * Construct.
	 */
	public InlineEnclosureHandler()
	{
		this(null);
	}

	public InlineEnclosureHandler(MarkupResourceStream resourceStream)
	{
		super(resourceStream);
	}

	@Override
	protected MarkupElement onComponentTag(final ComponentTag tag) throws ParseException
	{
		// We only need ComponentTags
		if (tag instanceof WicketTag)
		{
			return tag;
		}

		// Has wicket:enclosure attribute?
		String enclosureAttr = getAttribute(tag, null);
		if (enclosureAttr != null)
		{
			if (tag.isOpen())
			{
				// Make sure 'wicket:id' and 'id' are consistent
				String htmlId = tag.getAttribute("id");
				if ((tag.getId() != null) && !Strings.isEmpty(htmlId) &&
					!htmlId.equals(tag.getId()))
				{
					throw new ParseException(
						"Make sure that 'id' and 'wicket:id' are the same if both are provided. Tag:" +
							tag.toString(), tag.getPos());
				}

				// if it doesn't have a wicket-id already, then assign one now.
				if (Strings.isEmpty(tag.getId()))
				{
					if (Strings.isEmpty(htmlId))
					{
						String id = getWicketNamespace() + "_" + INLINE_ENCLOSURE_ID_PREFIX + (counter++);
						tag.setId(id);
					}
					else
					{
						tag.setId(htmlId);
					}

					tag.setAutoComponentTag(true);
					tag.setAutoComponentFactory(new ComponentTag.IAutoComponentFactory()
					{
						@Override
						public Component newComponent(MarkupContainer container, ComponentTag tag)
						{
							String attributeName = getInlineEnclosureAttributeName(null);
							String childId = tag.getAttribute(attributeName);
							return new InlineEnclosure(tag.getId(), childId);
						}
					});
					tag.setModified(true);
				}

				// Put the enclosure on the stack. The most current one will be on top
				if (enclosures == null)
				{
					enclosures = new ArrayDeque<>();
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
		// Are we within an enclosure?
		else if ((enclosures != null) && (enclosures.size() > 0))
		{
			// In case the enclosure tag did not provide a child component id, then assign the
			// first ComponentTag's id found as the controlling child to the enclosure.
			if (tag.isOpen() && (tag.getId() != null) && !(tag instanceof WicketTag) &&
				!tag.isAutoComponentTag())
			{
				Iterator<ComponentTag> componentTagIterator = enclosures.descendingIterator();
				while (componentTagIterator.hasNext())
				{
					ComponentTag lastEnclosure = componentTagIterator.next();
					String attr = getAttribute(lastEnclosure, null);
					if (Strings.isEmpty(attr) == true)
					{
						lastEnclosure.getAttributes().put(getInlineEnclosureAttributeName(null),
							tag.getId());
						lastEnclosure.setModified(true);
					}
				}
			}
			else if (tag.isClose() && tag.closes(enclosures.peek()))
			{
				ComponentTag lastEnclosure = enclosures.pop();
				String attr = getAttribute(lastEnclosure, null);
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
	 *      The ComponentTag of the markup element with wicket:enclosure attribute
	 * @return The value of wicket:enclosure attribute or null if not found
	 */
	private String getAttribute(final ComponentTag tag, MarkupStream markupStream)
	{
		return tag.getAttributes().getString(getInlineEnclosureAttributeName(markupStream));
	}

	@Override
	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		String inlineEnclosureChildId = getAttribute(tag, markupStream);
		if (Strings.isEmpty(inlineEnclosureChildId) == false)
		{
			String id = tag.getId();

			// Yes, we handled the tag
			return new InlineEnclosure(id, inlineEnclosureChildId);
		}

		// We were not able to handle the tag
		return null;
	}

	private String getInlineEnclosureAttributeName(MarkupStream markupStream) {
		return getWicketNamespace(markupStream) + ':' + INLINE_ENCLOSURE_ATTRIBUTE_NAME;
	}

}
