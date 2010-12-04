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
package org.apache.wicket.markup.html;

import org.apache.wicket.Component;
import org.apache.wicket.IClusterable;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.IMarkupFragment;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.TagUtils;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.util.lang.Classes;


/**
 * A Wicket internal helper class to handle wicket:head tags.
 * 
 * @author Juergen Donnerstag
 */
public class ContainerWithAssociatedMarkupHelper implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/** <wicket:head> is only allowed before <body>, </head>, <wicket:panel> etc. */
	private boolean noMoreWicketHeadTagsAllowed = false;

	/** The markup container the helper is associated with */
	private final WebMarkupContainer container;

	/**
	 * @param container
	 */
	public ContainerWithAssociatedMarkupHelper(final WebMarkupContainer container)
	{
		this.container = container;
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
	 * @param htmlContainer
	 *            The HtmlHeaderContainer added to the Page
	 */
	public final void renderHeadFromAssociatedMarkupFile(final HtmlHeaderContainer htmlContainer)
	{
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
			HeaderPartContainer headerPart = getHeaderPart(headerId,
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
	 * @param id
	 * @param markup
	 * @return the header part for this panel/border or null if it doesn't have a wicket:head tag.
	 */
	private final HeaderPartContainer getHeaderPart(final String id, final IMarkupFragment markup)
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