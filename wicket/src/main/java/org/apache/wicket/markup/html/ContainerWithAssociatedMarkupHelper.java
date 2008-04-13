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

import org.apache.wicket.Response;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.AbstractBehavior;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupElement;
import org.apache.wicket.markup.MarkupException;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.TagUtils;
import org.apache.wicket.markup.WicketTag;
import org.apache.wicket.markup.html.internal.HtmlHeaderContainer;
import org.apache.wicket.response.NullResponse;
import org.apache.wicket.util.lang.Classes;


/**
 * A Wicket internal helper class to handle wicket:head tags.
 * 
 * @author Juergen Donnerstag
 */
public class ContainerWithAssociatedMarkupHelper extends AbstractBehavior
{
	private static final long serialVersionUID = 1L;

	/** <wicket:head> is only allowed before <body>, </head>, <wicket:panel> etc. */
	private boolean noMoreWicketHeadTagsAllowed = false;

	/** The markup container the helper is associated with */
	private final WebMarkupContainer< ? > container;

	/**
	 * @param container
	 */
	public ContainerWithAssociatedMarkupHelper(final IHeaderPartContainerProvider container)
	{
		this.container = (WebMarkupContainer< ? >)container;
	}

	/**
	 * Called by components like Panel and Border which have associated Markup and which may have a
	 * &lt;wicket:head&gt; tag.
	 * <p>
	 * Whereas 'this' might be a Panel or Border, the HtmlHeaderContainer parameter has been added
	 * to the Page as a container for all headers any of its components might wish to contribute.
	 * <p>
	 * The headers contributed are rendered in the standard way.
	 * 
	 * @param htmlContainer
	 *            The HtmlHeaderContainer added to the Page
	 */
	public final void renderHeadFromAssociatedMarkupFile(
		final HtmlHeaderContainer< ? > htmlContainer)
	{
		// Gracefully getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream markupStream = container.getAssociatedMarkupStream(false);

		// No associated markup => no header section
		if (markupStream == null)
		{
			return;
		}

		// Position pointer at current (first) header
		noMoreWicketHeadTagsAllowed = false;
		while (nextHeaderMarkup(markupStream) != -1)
		{
			Class< ? > markupClass = ((WicketTag)markupStream.getTag()).getMarkupClass();
			if (markupClass == null)
			{
				markupClass = markupStream.getContainerClass();
			}
			// Create a HeaderPartContainer and associate the markup
			final HeaderPartContainer headerPart = getHeaderPart(markupClass,
				markupStream.getCurrentIndex());
			if (headerPart != null)
			{
				// A component's header section must only be added once,
				// no matter how often the same Component has been added
				// to the page or any other container in the hierarchy.
				if (htmlContainer.okToRenderComponent(headerPart.getScope(), headerPart.getId()))
				{
					htmlContainer.autoAdd(headerPart, null);
				}
				else
				{
					// TODO Performance: I haven't found a more efficient
					// solution yet.
					// Already added but all the components in this header part
					// must be touched (that they are rendered)
					Response response = container.getRequestCycle().getResponse();
					try
					{
						container.getRequestCycle().setResponse(NullResponse.getInstance());
						htmlContainer.autoAdd(headerPart, null);
					}
					finally
					{
						container.getRequestCycle().setResponse(response);
					}
				}
			}

			// Position the stream after <wicket:head>
			markupStream.skipComponent();
		}
	}

	/**
	 * Gets the header part of the Panel/Border. Returns null if it doesn't have a header tag.
	 * 
	 * @param index
	 *            A unique index
	 * @param markupClass
	 *            The java class the wicket:head tag is directly associated with
	 * @return the header part for this panel/border or null if it doesn't have a wicket:head tag.
	 */
	private final HeaderPartContainer getHeaderPart(final Class< ? > markupClass, final int index)
	{
		// Gracefully getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream markupStream = container.getAssociatedMarkupStream(false);

		// Position markup stream at beginning of header tag
		markupStream.setCurrentIndex(index);

		// Create a HtmlHeaderContainer for the header tag found
		final MarkupElement element = markupStream.get();
		if (element instanceof WicketTag)
		{
			final WicketTag wTag = (WicketTag)element;
			if ((wTag.isHeadTag() == true) && (wTag.getNamespace() != null))
			{
				// found <wicket:head>
				// create a unique id for the HtmlHeaderContainer to be
				// created
				final String headerId = "_" + Classes.simpleName(markupClass) +
					container.getVariation() + "Header" + index;

				// Create the header container and associate the markup with
				// it
				String scope = wTag.getAttributes().getString(
					markupStream.getWicketNamespace() + ":scope");
				final HeaderPartContainer headerContainer = ((IHeaderPartContainerProvider)container).newHeaderPartContainer(
					headerId, scope);
				headerContainer.setMyMarkupStream(markupStream);
				headerContainer.setRenderBodyOnly(true);

				// The container does have a header component
				return headerContainer;
			}
		}

		throw new WicketRuntimeException("Programming error: expected a WicketTag: " +
			markupStream.toString());
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
				if (tag.isClose() && TagUtils.isHeadTag(tag))
				{
					noMoreWicketHeadTagsAllowed = true;
				}
				// wicket:head must be before <body>
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