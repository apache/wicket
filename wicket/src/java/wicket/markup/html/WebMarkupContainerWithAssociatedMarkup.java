/*
 * $Id: WebMarkupContainerWithAssociatedMarkup.java 5861 2006-05-25 20:55:07
 * +0000 (Thu, 25 May 2006) eelco12 $ $Revision$ $Date: 2006-05-25
 * 20:55:07 +0000 (Thu, 25 May 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Response;
import wicket.WicketRuntimeException;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.html.internal.HeaderPartContainer;
import wicket.markup.html.internal.HeaderContainer;
import wicket.model.IModel;
import wicket.response.NullResponse;
import wicket.util.lang.Classes;

/**
 * A WebMarkupContainer, such as Panel or Border, with an associated markup 
 * file. 
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Juergen Donnerstag
 */
public class WebMarkupContainerWithAssociatedMarkup<T> extends WebMarkupContainer<T>
{
	private static final long serialVersionUID = 1L;

	/** True if body onLoad attribute modifiers have been attached */
	private boolean checkedBody = false;

	/** <wicket:head> is only allowed before <body>, </head>, <wicket:panel> etc. */
	private boolean noMoreWicketHeadTagsAllowed = false;

	/**
	 * @see Component#Component(MarkupContainer,String)
	 */
	public WebMarkupContainerWithAssociatedMarkup(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public WebMarkupContainerWithAssociatedMarkup(MarkupContainer parent, final String id,
			IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Create a new HeaderPartContainer. Users may wish to do that to
	 * implemented more sophisticated header scoping stragegies.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            The header component's id
	 * @param scope
	 *            The default scope of the header
	 * @return The new HeaderPartContainer
	 */
	public HeaderPartContainer newHeaderPartContainer(MarkupContainer parent, final String id,
			final String scope)
	{
		return new HeaderPartContainer(parent, id, this, scope);
	}

	/**
	 * Called by components like Panel and Border which have associated Markup
	 * and which may have a &lt;wicket:head&gt; tag.
	 * <p>
	 * Whereas 'this' might be a Panel or Border, the HtmlHeaderContainer
	 * parameter has been added to the Page as a container for all headers any
	 * of its components might wish to contribute.
	 * <p>
	 * The headers contributed are rendered in the standard way.
	 * 
	 * @param htmlContainer
	 *            The HtmlHeaderContainer added to the Page
	 */
	protected final void renderHeadFromAssociatedMarkupFile(final HeaderContainer htmlContainer)
	{
		// Gracefully getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream markupStream = getAssociatedMarkupStream(false);

		// No associated markup => no header section
		if (markupStream == null)
		{
			return;
		}

		// Position pointer at current (first) header
		this.noMoreWicketHeadTagsAllowed = false;
		while (nextHeaderMarkup(markupStream) != -1)
		{
			Class markupClass = markupStream.getTag().getMarkupClass();
			if (markupClass == null)
			{
				markupClass = markupStream.getContainerClass();
			}
			// Create a HeaderPartContainer and associate the markup
			final HeaderPartContainer headerPart = getHeaderPart(htmlContainer, markupClass,
					markupStream.getCurrentIndex());
			if (headerPart != null)
			{
				// A component's header section must only be added once,
				// no matter how often the same Component has been added
				// to the page or any other container in the hierachy.
				if (htmlContainer.okToRenderComponent(headerPart.getScope(), headerPart.getId()))
				{
					headerPart.autoAdded();

					// Check if the Panel/Border requires some <body
					// onload=".."> attribute to be copied to the page's body
					// tag.
					if (checkedBody == false)
					{
						checkedBody = true;
						checkBodyOnLoad();
					}
				}
				else
				{
					// TODO Performance: I haven't found a more efficient
					// solution yet.
					// Already added but all the components in this header part
					// must be touched (that they are rendered)
					Response response = getRequestCycle().getResponse();
					try
					{
						getRequestCycle().setResponse(NullResponse.getInstance());
						headerPart.autoAdded();
					}
					finally
					{
						getRequestCycle().setResponse(response);
					}
				}
			}

			// Position the stream after <wicket:head>
			markupStream.skipComponent();
		}
	}

	/**
	 * Check if the Panel/Border requires some <body onload=".."> attribute to
	 * be copied to the page's body tag.
	 */
	private void checkBodyOnLoad()
	{
		// Gracefully getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream associatedMarkupStream = getAssociatedMarkupStream(false);

		// No associated markup => no body tag
		if (associatedMarkupStream == null)
		{
			return;
		}

		// Remember the current position within markup, where we need to
		// go back to, at the end.
		int index = associatedMarkupStream.getCurrentIndex();

		try
		{
			associatedMarkupStream.setCurrentIndex(0);
			while (associatedMarkupStream.hasMoreComponentTags())
			{
				final ComponentTag tag = associatedMarkupStream.getTag();
				if (tag.isBodyTag())
				{
					final CharSequence onLoad = tag.getString("onload");
					if (onLoad != null)
					{
						// Attach an AttributeModifier to the body container
						// which appends the new value to the onLoad
						// attribute
						getWebPage().getBodyContainer().addOnLoadModifier(
								onLoad, this);
					}

					// There can only be one body tag
					break;
				}
			}
		}
		finally
		{
			// Make sure we return to the orginal position in the markup
			associatedMarkupStream.setCurrentIndex(index);
		}
	}

	/**
	 * Gets the header part of the Panel/Border. Returns null if it doesn't have
	 * a header tag.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param index
	 *            A unique index
	 * @param markupClass
	 *            The java class the wicket:head tag is directly associated with
	 * @return the header part for this panel/border or null if it doesn't have
	 *         a wicket:head tag.
	 */
	private final HeaderPartContainer getHeaderPart(MarkupContainer parent,
			final Class markupClass, final int index)
	{
		// Gracefully getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream markupStream = getAssociatedMarkupStream(false);

		// Position markup stream at beginning of header tag
		markupStream.setCurrentIndex(index);

		// Create a HtmlHeaderContainer for the header tag found
		final ComponentTag tag = markupStream.getTag(false);
		if ((tag != null) && tag.isWicketHeadTag())
		{
			// found <wicket:head>. Create a unique id for the 
			// HtmlHeaderContainer to be created
			final String headerId = Component.AUTO_COMPONENT_PREFIX
					+ Classes.simpleName(markupClass) + getVariation()
					+ "Header" + index;

			// Create the header container and associate the markup with
			// it
			String scope = tag.getAttributes().getString(
					markupStream.getWicketNamespace() + ":scope");
			final HeaderPartContainer headerContainer = newHeaderPartContainer(
					parent, headerId, scope);
			headerContainer.setMyMarkupStream(markupStream);
			headerContainer.setRenderBodyOnly(true);

			// The container does have a header component
			return headerContainer;
		}

		throw new WicketRuntimeException("Programming error: expected a WicketTag: "
				+ markupStream.toString());
	}

	/**
	 * 
	 * @param associatedMarkupStream
	 * @return xxx
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
			if (elem instanceof ComponentTag)
			{
				ComponentTag tag = (ComponentTag)elem;
				if (tag.isOpen() && tag.isWicketHeadTag())
				{
					if (this.noMoreWicketHeadTagsAllowed == true)
					{
						throw new MarkupException(
								"<wicket:head> tags are only allowed before <body>, </head>, <wicket:panel> etc. tag");
					}
					return associatedMarkupStream.getCurrentIndex();
				}
				else if (this.noMoreWicketHeadTagsAllowed == false)
				{
					// wicket:head must be before border, panel or extend
					if (tag.isOpen()
							&& (tag.isPanelTag() || tag.isBorderTag() || tag.isExtendTag()))
					{
						this.noMoreWicketHeadTagsAllowed = true;
					}
					// wicket:head must be before </head>
					else if (tag.isClose() && tag.isHeadTag())
					{
						this.noMoreWicketHeadTagsAllowed = true;
					}
					// wicket:head must be before <body>
					else if (tag.isOpen() && tag.isBodyTag())
					{
						this.noMoreWicketHeadTagsAllowed = true;
					}
				}
			}
			elem = associatedMarkupStream.next();
		}

		// No (more) wicket:head found
		return -1;
	}
}