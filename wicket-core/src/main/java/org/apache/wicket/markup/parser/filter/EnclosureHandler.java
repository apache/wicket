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
import java.util.concurrent.atomic.AtomicLong;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.ComponentTag.IAutoComponentFactory;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupResourceStream;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.WicketParseException;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.internal.Enclosure;
import org.apache.wicket.markup.parser.AbstractMarkupFilter;
import org.apache.wicket.markup.resolver.IComponentResolver;


/**
 * This is a markup inline filter. It identifies &lt;wicket:enclosure&gt; tags. If the 'child'
 * attribute is empty it determines the wicket:id of the child component automatically by analyzing
 * the wicket component (in this case on one wicket component is allowed) in between the open and
 * close tags. If the enclosure tag has a 'child' attribute like
 * <code>&lt;wicket:enclosure child="xxx"&gt;</code> than more than just one wicket component inside
 * the enclosure tags are allowed and the child component which determines the visibility of the
 * enclosure is identified by the 'child' attribute value which must be equal to the relative child
 * id path.
 * 
 * @see EnclosureResolver
 * @see Enclosure
 * 
 * @author Juergen Donnerstag
 */
public final class EnclosureHandler extends AbstractMarkupFilter implements IComponentResolver
{
	private static final long serialVersionUID = 1L;

	private static final IAutoComponentFactory FACTORY = new IAutoComponentFactory()
	{


		@Override
		public Component newComponent(ComponentTag tag)
		{
			return new Enclosure(tag.getId(), tag
				.getAttribute(EnclosureHandler.CHILD_ATTRIBUTE));
		}
	};

	/** */
	public static final String ENCLOSURE = "enclosure";

	/** The child attribute */
	public static final String CHILD_ATTRIBUTE = "child";

	/** Stack of <wicket:enclosure> tags */
	private Deque<ComponentTag> stack;

	/**
	 * Used to assign unique ids to enclosures
	 * 
	 * TODO queueing: there has to be a better way of doing this, perhaps some merged-markup-unique
	 * counter
	 */
	private static final AtomicLong index = new AtomicLong();

	/** The id of the first wicket tag inside the enclosure */
	private String childId;

	/**
	 * Construct.
	 */
	public EnclosureHandler()
	{
		this(null);
	}

	public EnclosureHandler(MarkupResourceStream resourceStream)
	{
		super(resourceStream);
	}

	@Override
	protected final MarkupElement onComponentTag(ComponentTag tag) throws ParseException
	{
		final boolean isWicketTag = tag instanceof WicketTag;
		final boolean isEnclosureTag = isWicketTag && ((WicketTag)tag).isEnclosureTag();

		// If wicket:enclosure
		if (isEnclosureTag)
		{
			// If open tag, than put the tag onto the stack
			if (tag.isOpen())
			{
				tag.setId(tag.getId() + index.getAndIncrement());
				tag.setModified(true);
				tag.setAutoComponentFactory(FACTORY);

				if (stack == null)
				{
					stack = new ArrayDeque<>();
				}
				stack.push(tag);
			}
			// If close tag, then remove the tag from the stack and update
			// the child attribute of the open tag if required
			else if (tag.isClose())
			{
				if (stack == null)
				{
					throw new WicketParseException("Missing open tag for Enclosure:", tag);
				}

				// Remove the open tag from the stack
				ComponentTag lastEnclosure = stack.pop();

				// If the child attribute has not been given by the user,
				// then ...
				if (childId != null)
				{
					lastEnclosure.put(CHILD_ATTRIBUTE, childId);
					lastEnclosure.setModified(true);
					childId = null;
				}

				if (stack.size() == 0)
				{
					stack = null;
				}
			}
			else
			{
				throw new WicketParseException("Open-close tag not allowed for Enclosure:", tag);
			}
		}
		// Are we inside a wicket:enclosure tag?
		else if (stack != null)
		{
			ComponentTag lastEnclosure = stack.getLast();

			// If the enclosure tag has NO child attribute, then ...
			if (lastEnclosure.getAttribute(CHILD_ATTRIBUTE) == null)
			{
				String id = tag.getAttribute(getWicketNamespace() + ":id");
				if (id != null)
				{
					// We encountered more than one child component inside
					// the enclosure and are not able to automatically
					// determine the child component to delegate the
					// isVisible() to => Exception
					if (childId != null)
					{
						throw new WicketParseException("Use <" + getWicketNamespace() +
							":enclosure child='xxx'> to name the child component:", tag);
					}
					// Remember the child id. The open tag will be updated
					// once the close tag is found. See above.
					childId = id;
				}
			}
		}

		return tag;
	}

	@Override
	public Component resolve(final MarkupContainer container, final MarkupStream markupStream,
		final ComponentTag tag)
	{
		if ((tag instanceof WicketTag) && ((WicketTag)tag).isEnclosureTag())
		{
			// Yes, we handled the tag
			return new Enclosure(tag.getId() + container.getPage().getAutoIndex(),
				tag.getAttribute(EnclosureHandler.CHILD_ATTRIBUTE));
		}

		// We were not able to handle the tag
		return null;
	}
}
