/*
 * $Id$
 * $Revision$ $Date$
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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import wicket.Component;
import wicket.EventRequestHandler;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.Markup;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupException;
import wicket.markup.MarkupStream;
import wicket.markup.WicketTag;
import wicket.model.IModel;
import wicket.util.lang.Classes;

/**
 * A container of HTML markup and components. It is very similar to the base
 * class MarkupContainer, except that the markup type is defined to be HTML.
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class WebMarkupContainer extends MarkupContainer implements IHeaderContributor
{
	private transient List headerComponents;

	/**
	 * @see Component#Component(String)
	 */
	public WebMarkupContainer(final String id)
	{
		super(id);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public WebMarkupContainer(final String id, IModel model)
	{
		super(id, model);
	}

	/**
	 * Gets the markup type for this component.
	 * 
	 * @return Markup type of HTML
	 */
	public final String getMarkupType()
	{
		return "html";
	}

	/**
	 * Renders this component. This implementation just calls renderComponent.
	 */
	protected void onRender()
	{
		renderComponent(findMarkupStream());
	}

	/**
	 * Print to the web response what ever the component wants
	 * to contribute to the head section.
	 * 
	 * @see wicket.markup.html.IHeaderContributor#printHead(wicket.markup.html.HtmlHeaderContainer)
	 * 
	 * @param container The HtmlHeaderContainer
	 */
	public void printHead(final HtmlHeaderContainer container)
	{
		// Ask the child component if it has something to contribute
		WebMarkupContainer headerPart = getHeaderPart();

		// If the child component has something to contribute to 
		// the header and in case the very same Component has not 
		// contributed to the page, than ...
		// A component's header section must only be added once, 
		// no matter how often the same Component has been added 
		// to the page or any other container in the hierachie.
		if ((headerPart != null) && (container.get(headerPart.getId()) == null))
		{
			container.autoAdd(headerPart);
			
			// Check if the component requires some <body onLoad="..">
			// attribute to be copied to the page's body tag. 
			checkBodyOnLoad();
		}

		EventRequestHandler[] handlers = getEventRequestHandlers();
		if (handlers != null) for (int i = 0; i < handlers.length; i++)
		{
			if (handlers[i] instanceof IHeaderContributor)
			{
				((IHeaderContributor)handlers[i]).printHead(container);
			}
		}
	}
	
	/**
	 * Check if the component requires some <body onLoad=".."> attribute to 
	 * be copied to the page's body tag.
	 */
	private void checkBodyOnLoad()
	{
		// gracefull getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream associatedMarkupStream = getApplication().getMarkupCache()
				.getMarkupStream(this, null, false);

		// No associated markup => no body tag
		if (associatedMarkupStream == null)
		{
			return;
		}

		// Remember the current position within markup, where we need to 
		// back to, at the end.
		int index = associatedMarkupStream.getCurrentIndex();
		
		try
		{
		    // Start at the beginning
		    associatedMarkupStream.setCurrentIndex(0);
		    
			// Iterate the markup and find <body onLoad="...">
			do
			{
				final MarkupElement element = associatedMarkupStream.get();
				if (element instanceof ComponentTag)
				{
					final ComponentTag tag = (ComponentTag)element;
					if ("body".equalsIgnoreCase(tag.getName()))
					{
					    final String onLoad = tag.getAttributes().getString("onload");
					    if (onLoad != null)
					    {
					        ((WebPage)getPage()).appendToBodyOnLoad(onLoad);
					    }
					    
					    // There can only be one body tag
					    break;
					}
				}
			}
			while (associatedMarkupStream.next() != null);
		}
		finally
		{
		    // Make sure we return to the orginal position in the markup
		    associatedMarkupStream.setCurrentIndex(index);
		}
	}
	
	/**
	 * Gets the header part for the markup container. Returns null if it doesn't
	 * contribute to a header.
	 * 
	 * @return the header part for this markup container or null if it doesn't
	 *         contribute anything.
	 */
	private final WebMarkupContainer getHeaderPart()
	{
		// gracefull getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream associatedMarkupStream = getApplication().getMarkupCache()
				.getMarkupStream(this, null, false);

		// No associated markup => no header section
		if (associatedMarkupStream == null)
		{
			return null;
		}

		// Lazy scan the markup for a header component tag, if necessary
		// 'index' will be where <wicket:head> resides in the markup 
		int index = -1;
		if (associatedMarkupStream.getHeaderIndex() == Markup.HEADER_NOT_YET_EVALUATED)
		{
		    // Markup has not yet been scanned
			// Iterate the markup and find <wicket:head>
			do
			{
				final MarkupElement element = associatedMarkupStream.get();
				if (element instanceof WicketTag)
				{
					final WicketTag wTag = (WicketTag)element;
					if (wTag.isHeadTag() == true)
					{
					    // ok, found header. Remember the position
					    index = associatedMarkupStream.getCurrentIndex();
					    break;
					}
				}
			}
			while (associatedMarkupStream.next() != null);
		}
		else if (associatedMarkupStream.getHeaderIndex() == Markup.HEADER_NO_HEADER_FOUND)
		{
		    // The markup has been scanned already, but does not contain any
		    // header tag
		    ; // Don't do anything
		}
		else
		{
		    // The markup has been scanned already. Get the index where the 
		    // header tag resides from the markup
		    index = associatedMarkupStream.getHeaderIndex();
		}
		
		// Ok, finished scanning the markup for header tag
		// If markup contains a header section, handle it now.
		if (index >= 0)
		{
		    // Position markup stream at beginning of header tag
		    associatedMarkupStream.setCurrentIndex(index);
		    
		    // Create a HtmlHeaderContainer for the header tag found and
		    // add all components from addToHeader list
			final MarkupElement element = associatedMarkupStream.get();
			if (element instanceof WicketTag)
			{
				final WicketTag wTag = (WicketTag)element;
				if (wTag.isHeadTag() == true)
				{
				    associatedMarkupStream.setHeaderIndex(index);
				    
				    // found <wicket:head>
				    // create a unique id for the HtmlHeaderContainer to be created
					final String headerId = "_" + Classes.name(this.getClass()) + "Header";
					
					// Create the header container and associate the markup with it
					WebMarkupContainer headerContainer = new HtmlHeaderContainer(headerId,
							associatedMarkupStream);
					
					// In case components are part of the region, the user must 
					// have provided the component objects by means of addToHeader().
					// All the component provided by the user, must now be added
					// to the newly created header container.
					if (this.headerComponents != null)
					{
						for (Iterator iter = headerComponents.iterator(); iter.hasNext();)
						{
					        headerContainer.add((Component)iter.next());
						}
					}
					
					// The container does have a header component
					return headerContainer;
				}
			}
		}

		if (this.headerComponents != null)
		{
			throw new MarkupException(
					"You have added header components but did not specify a <wicket:head> region in your Page markup: "
							+ this.toString());
		}
		
		// Though the container does have markup, it does not have a 
		// <wicket:head> region.
		return null;
	}

	/**
	 * Components which are part of a wicket header region, must be added by
	 * means of addToHeader() instead of add().
	 * 
	 * @param child
	 *            The component to be added to the header region.
	 */
	public final void addToHeader(final Component child)
	{
		if (this.headerComponents == null)
		{
			this.headerComponents = new ArrayList();
		}

		this.headerComponents.add(child);
	}
}