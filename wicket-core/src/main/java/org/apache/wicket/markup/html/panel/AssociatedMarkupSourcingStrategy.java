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
package org.apache.wicket.markup.html.panel;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.TagUtils;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.HeaderPartContainer;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.util.lang.Classes;

/**
 * Boiler plate for markup sourcing strategy which retrieve the markup from associated markup files.
 * 
 * @author Juergen Donnerstag
 */
public abstract class AssociatedMarkupSourcingStrategy extends AbstractMarkupSourcingStrategy
{
	/** <wicket:head> is only allowed before <body>, </head>, <wicket:panel> etc. */
	private boolean noMoreWicketHeadTagsAllowed = false;

	/**
	 * Constructor.
	 */
	public AssociatedMarkupSourcingStrategy()
	{
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
		super.onComponentTag(component, tag);

		// Copy attributes from <wicket:XXX> to the "calling" tag
		IMarkupFragment markup = ((MarkupContainer)component).getMarkup(null);
		String namespace = markup.getMarkupResourceStream().getWicketNamespace() + ":";

		MarkupElement elem = markup.get(0);
		if (elem instanceof ComponentTag)
		{
			ComponentTag panelTag = (ComponentTag)elem;
			for (String key : panelTag.getAttributes().keySet())
			{
				// exclude "wicket:XX" attributes
				if (key.startsWith(namespace) == false)
				{
					tag.append(key, panelTag.getAttribute(key), ", ");
				}
			}
		}
		else
		{
			throw new MarkupException(markup.getMarkupResourceStream(),
				"Expected a Tag but found raw markup: " + elem.toString());
		}
	}

	/**
	 * Search the child's markup in the header section of the markup
	 * 
	 * @param container
	 * @param child
	 * @return Null, if not found
	 */
	public IMarkupFragment findMarkupInAssociatedFileHeader(final MarkupContainer container,
		final Component child)
	{
		// Get the associated markup
		IMarkupFragment markup = container.getAssociatedMarkup();
		IMarkupFragment childMarkup = null;

		// MarkupStream is good at searching markup
		MarkupStream stream = new MarkupStream(markup);
		while (stream.skipUntil(ComponentTag.class) && (childMarkup == null))
		{
			ComponentTag tag = stream.getTag();
			if (TagUtils.isWicketHeadTag(tag))
			{
				if (tag.getMarkupClass() == null)
				{
					// find() can still fail an return null => continue the search
					childMarkup = stream.getMarkupFragment().find(child.getId());
				}
			}
			else if (TagUtils.isHeadTag(tag))
			{
				// find() can still fail an return null => continue the search
				childMarkup = stream.getMarkupFragment().find(child.getId());
			}

			// Must be a direct child. We are not interested in grand children
			if (tag.isOpen() && !tag.hasNoCloseTag())
			{
				stream.skipToMatchingCloseTag(tag);
			}
			stream.next();
		}

		return childMarkup;
	}

	/**
	 * Render the header from the associated markup file
	 */
	@Override
	public void renderHead(final Component component, HtmlHeaderContainer container)
	{
		if (!(component instanceof WebMarkupContainer))
		{
			throw new WicketRuntimeException(component.getClass().getSimpleName() +
				" can only be associated with WebMarkupContainer.");
		}

		renderHeadFromAssociatedMarkupFile((WebMarkupContainer)component, container);
	}

	/**
	 * Called by components like Panel and Border which have associated Markup and which may have a
	 * &lt;wicket:head&gt; tag.
	 * <p>
	 * Whereas 'this' might be a Panel or Border, the HtmlHeaderContainer parameter has been added
	 * to the Page as a container for all headers any of its components might wish to contribute to.
	 * <p>
	 * The headers contributed are rendered in the standard way.
	 * 
	 * @param container
	 * @param htmlContainer
	 *            The HtmlHeaderContainer added to the Page
	 */
	public final void renderHeadFromAssociatedMarkupFile(final WebMarkupContainer container,
		final HtmlHeaderContainer htmlContainer)
	{
		// reset for each render in case the strategy is re-used
		noMoreWicketHeadTagsAllowed = false;

		// Gracefully getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream markupStream = container.getAssociatedMarkupStream(false);
		if (markupStream == null)
		{
			return;
		}

		// Position pointer at current (first) header
		noMoreWicketHeadTagsAllowed = false;
		while (nextHeaderMarkup(markupStream) != -1)
		{
			// found <wicket:head>
			String headerId = getHeaderId(container, markupStream);

			// Create a HeaderPartContainer and associate the markup
			HeaderPartContainer headerPart = getHeaderPart(container, headerId,
				markupStream.getMarkupFragment());
			if (headerPart != null)
			{
				// A component's header section must only be added once,
				// no matter how often the same Component has been added
				// to the page or any other container in the hierarchy.
				if (htmlContainer.okToRenderComponent(headerPart.getScope(), headerPart.getId()))
				{
					// make sure the Page is accessible
					headerPart.setParent(htmlContainer);
					headerPart.render();
				}
			}

			// Position the stream after <wicket:head>
			markupStream.skipComponent();
		}
	}

	/**
	 * 
	 * @param container
	 * @param markupStream
	 * @return The header id
	 */
	private String getHeaderId(final Component container, final MarkupStream markupStream)
	{
		Class<?> markupClass = markupStream.getTag().getMarkupClass();
		if (markupClass == null)
		{
			markupClass = markupStream.getContainerClass();
		}

		// create a unique id for the HtmlHeaderContainer
		StringBuilder builder = new StringBuilder(100);
		builder.append("_");
		builder.append(Classes.simpleName(markupClass));
		if (container.getVariation() != null)
		{
			builder.append(container.getVariation());
		}
		builder.append("Header");
		builder.append(markupStream.getCurrentIndex());
		return builder.toString();
	}

	/**
	 * Gets the header part of the Panel/Border. Returns null if it doesn't have a header tag.
	 * 
	 * @param container
	 * @param id
	 * @param markup
	 * @return the header part for this panel/border or null if it doesn't have a wicket:head tag.
	 */
	private final HeaderPartContainer getHeaderPart(final WebMarkupContainer container,
		final String id, final IMarkupFragment markup)
	{
		// Create a HtmlHeaderContainer for the header tag found
		final MarkupElement element = markup.get(0);
		if (element instanceof WicketTag)
		{
			final WicketTag wTag = (WicketTag)element;
			if ((wTag.isHeadTag() == true) && (wTag.getNamespace() != null))
			{
				// Create the header container and associate the markup with it
				return new HeaderPartContainer(id, container, markup);
			}
		}

		throw new WicketRuntimeException("Programming error: expected a WicketTag: " +
			markup.toString());
	}

	/**
	 * Process next header markup fragment.
	 * 
	 * @param associatedMarkupStream
	 * @return index or -1 when done
	 */
	private final int nextHeaderMarkup(final MarkupStream associatedMarkupStream)
	{
		// No associated markup => no header section
		if (associatedMarkupStream == null)
		{
			return -1;
		}

		// Scan the markup for <wicket:head>.
		MarkupElement elem = associatedMarkupStream.get();
		while (elem != null)
		{
			if (elem instanceof WicketTag)
			{
				WicketTag tag = (WicketTag)elem;
				if (tag.isOpen() && tag.isHeadTag())
				{
					if (noMoreWicketHeadTagsAllowed == true)
					{
						throw new MarkupException(
							"<wicket:head> tags are only allowed before <body>, </head>, <wicket:panel> etc. tag");
					}
					return associatedMarkupStream.getCurrentIndex();
				}
				// wicket:head must be before border, panel or extend
				// @TODO why is that? Why can't it be anywhere? (except insight wicket:fragment
				else if (tag.isOpen() &&
					(tag.isPanelTag() || tag.isBorderTag() || tag.isExtendTag()))
				{
					noMoreWicketHeadTagsAllowed = true;
				}
			}
			else if (elem instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag)elem;
				// wicket:head must be before </head>
				// @TODO why??
				if (tag.isClose() && TagUtils.isHeadTag(tag))
				{
					noMoreWicketHeadTagsAllowed = true;
				}
				// wicket:head must be before <body>
				// @TODO why??
				else if (tag.isOpen() && TagUtils.isBodyTag(tag))
				{
					noMoreWicketHeadTagsAllowed = true;
				}
			}
			elem = associatedMarkupStream.next();
		}

		// No (more) wicket:head found
		return -1;
	}
}
