/*
 * $Id: ContainerWithAssociatedMarkupHelper.java,v 1.1 2006/03/10 22:20:42 jdonnerstag Exp $
 * $Revision: 1.1 $ $Date: 2006/03/10 22:20:42 $
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

import java.util.Iterator;

import wicket.Component;
import wicket.MarkupContainer;
import wicket.Response;
import wicket.behavior.AbstractBehavior;
import wicket.markup.ComponentTag;
import wicket.markup.Markup;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupStream;
import wicket.markup.WicketTag;
import wicket.markup.html.internal.HtmlHeaderContainer;
import wicket.markup.resolver.IComponentResolver;
import wicket.response.NullResponse;
import wicket.util.lang.Classes;

/**
 * A Wicket internal helper class to handle wicket:head tags.
 * 
 * @author Juergen Donnerstag
 */
class ContainerWithAssociatedMarkupHelper extends AbstractBehavior
{
	private static final long serialVersionUID = 1L;

	/** True if body onLoad attribute modifiers have been attached */
	private boolean checkedBody = false;

	private final WebMarkupContainer container;
	
	/**
	 * @param container
	 */
	ContainerWithAssociatedMarkupHelper(final WebMarkupContainer container)
	{
		this.container = container;
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
	protected final void renderHeadFromAssociatedMarkupFile(final HtmlHeaderContainer htmlContainer)
	{
		// Ask the Panel/Border if it has something to contribute to the header
		final HeaderPartContainer headerPart = getHeaderPart();

		// If the panel/border component has something to contribute to
		// the header ...
		if (headerPart != null)
		{
			// A component's header section must only be added once,
			// no matter how often the same Component has been added
			// to the page or any other container in the hierachy.
			if (htmlContainer.get(headerPart.getId()) == null)
			{
				htmlContainer.autoAdd(headerPart);

				// Check if the Panel/Border requires some <body onload="..">
				// attribute to be copied to the page's body tag.
				if (checkedBody == false)
				{
					checkedBody = true;
					checkBodyOnLoad();
				}
			}
			else
			{
				// TODO Performance: I haven't found a more efficient solution yet
				// Already added but all the components in this header part must
				// be touched (that they are rendered)
				Response response = container.getRequestCycle().getResponse();
				try
				{
					container.getRequestCycle().setResponse(NullResponse.getInstance());
					htmlContainer.autoAdd(headerPart);
				}
				finally
				{
					container.getRequestCycle().setResponse(response);
				}
			}
		}
	}

	/**
	 * Check if the Panel/Border requires some <body onload=".."> attribute to be
	 * copied to the page's body tag.
	 */
	private void checkBodyOnLoad()
	{
		// Gracefully getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream associatedMarkupStream = container.getAssociatedMarkupStream(false);

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
			final Iterator iter = associatedMarkupStream.componentTagIterator();
			while (iter.hasNext())
			{
				final ComponentTag tag = (ComponentTag)iter.next();
				if ("body".equalsIgnoreCase(tag.getName()))
				{
					final String onLoad = tag.getAttributes().getString("onload");
					if (onLoad != null)
					{
						// Attach an AttributeModifier to the body container
						// which appends the new value to the onLoad
						// attribute
						container.getWebPage().getBodyContainer().addOnLoadModifier(onLoad);
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
	 * Gets the header part of the Panel/Border. Returns null if it doesn't
	 * have a header tag.
	 * 
	 * @return the header part for this panel/border or null if it doesn't
	 *         have a wicket:head tag.
	 */
	private final HeaderPartContainer getHeaderPart()
	{
		// Gracefully getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream associatedMarkupStream = container.getAssociatedMarkupStream(false);

		// No associated markup => no header section
		if (associatedMarkupStream == null)
		{
			return null;
		}

		// Lazy scan the markup for a header component tag, if necessary
		// 'index' will be where <wicket:head> resides in the markup
		int index = Markup.NO_HEADER_FOUND;
		if (associatedMarkupStream.getHeaderIndex() != Markup.NO_HEADER_FOUND)
		{
			// The markup has been scanned already. Get the index where the
			// header tag resides from the markup
			index = associatedMarkupStream.getHeaderIndex();
		}

		// Ok, finished scanning the markup for header tag
		// If markup contains a header section, handle it now.
		if (index != Markup.NO_HEADER_FOUND)
		{
			// Position markup stream at beginning of header tag
			associatedMarkupStream.setCurrentIndex(index);

			// Create a HtmlHeaderContainer for the header tag found
			final MarkupElement element = associatedMarkupStream.get();
			if (element instanceof WicketTag)
			{
				final WicketTag wTag = (WicketTag)element;
				if ((wTag.isHeadTag() == true) && (wTag.getNamespace() != null))
				{
					// found <wicket:head>
					// create a unique id for the HtmlHeaderContainer to be
					// created
					final String headerId = "_" + Classes.simpleName(container.getClass())
							+ container.getVariation() + "Header";

					// Create the header container and associate the markup with
					// it
					HeaderPartContainer headerContainer = new HeaderPartContainer(headerId, 
							container, wTag.getAttributes().getString(
									associatedMarkupStream.getWicketNamespace() + ":scope"));
					headerContainer.setMyMarkupStream(associatedMarkupStream);
					headerContainer.setRenderBodyOnly(true);

					// The container does have a header component
					return headerContainer;
				}
			}
		}

		// Though the container does have markup, it does not have a
		// <wicket:head> region.
		return null;
	}

	/**
	 * For each wicket:head tag a HeaderPartContainer is created and added to
	 * the HtmlHeaderContainer which has been added to the Page.
	 */
	private static final class HeaderPartContainer extends WebMarkupContainer
			implements
				IComponentResolver
	{
		private static final long serialVersionUID = 1L;

		/** The panel or bordered page the header part is associated with */
		private final MarkupContainer container;

		/** <wicket:head scope="...">. A kind of namespace */
		private final String scope;

		/**
		 * @param id
		 *            The component id
		 * @param container
		 *            The Panel (or bordered page) the header part is associated
		 *            with
		 * @param scope
		 *            The scope of the wicket:head tag
		 */
		public HeaderPartContainer(final String id, final MarkupContainer container,
				final String scope)
		{
			super(id);
			this.container = container;
			this.scope = scope;
		}

		/**
		 * Get the scope of the header part
		 * 
		 * @return The scope name
		 */
		public final String getScope()
		{
			return this.scope;
		}

		/**
		 * @see IComponentResolver#resolve(MarkupContainer, MarkupStream,
		 *      ComponentTag)
		 */
		public final boolean resolve(final MarkupContainer container,
				final MarkupStream markupStream, final ComponentTag tag)
		{
			// The tag must be resolved against the panel and not against the
			// page
			Component component = this.container.get(tag.getId());
			if (component != null)
			{
				component.render(markupStream);
				return true;
			}

			return false;
		}
		
		/**
		 * @see #setMarkupStream(MarkupStream)
		 * 
		 * @param markupStream
		 */
		private void setMyMarkupStream(final MarkupStream markupStream)
		{
			super.setMarkupStream(markupStream);
		}
	}
}