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

import wicket.AjaxHandler;
import wicket.Component;
import wicket.IComponentResolver;
import wicket.MarkupContainer;
import wicket.Response;
import wicket.markup.ComponentTag;
import wicket.markup.Markup;
import wicket.markup.MarkupElement;
import wicket.markup.MarkupStream;
import wicket.markup.WicketTag;
import wicket.markup.html.ajax.IBodyOnloadContributor;
import wicket.model.IModel;
import wicket.response.NullResponse;
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
	private static final long serialVersionUID = 1L;
	
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
	 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.markup.html.HtmlHeaderContainer)
	 * 
	 * @param container The HtmlHeaderContainer
	 */
	public void renderHead(final HtmlHeaderContainer container)
	{
		// Ask the child component if it has something to contribute
		WebMarkupContainer headerPart = getHeaderPart();

		// If the child component has something to contribute to 
		// the header and in case the very same Component has not 
		// contributed to the page, than ...
		// A component's header section must only be added once, 
		// no matter how often the same Component has been added 
		// to the page or any other container in the hierachy.
		if ((headerPart != null) && (container.get(headerPart.getId()) == null))
		{
			container.autoAdd(headerPart);
			
			// Check if the component requires some <body onload="..">
			// attribute to be copied to the page's body tag. 
			checkBodyOnLoad();
		}
		else if (headerPart != null)
		{
			// already added but all the components in this header part must be touched (that they are rendered)
			Response response = getRequestCycle().getResponse();
			try
			{
				getRequestCycle().setResponse(NullResponse.getInstance());
				container.autoAdd(headerPart);
			} 
			finally
			{
				getRequestCycle().setResponse(response);
			}
		}

		// get head and body contributions in one loop
		AjaxHandler[] handlers = getAjaxHandlers();
		if (handlers != null)
		{
			for (int i = 0; i < handlers.length; i++)
			{
				((IHeaderContributor)handlers[i]).renderHead(container);

				String stmt = ((IBodyOnloadContributor)handlers[i]).getBodyOnload();
				if (stmt != null)
				{
					((WebPage)getPage()).appendToBodyOnLoad(stmt);
				}
			}	
		}
	}
	
	/**
	 * Check if the component requires some <body onload=".."> attribute to 
	 * be copied to the page's body tag.
	 */
	private void checkBodyOnLoad()
	{
		// gracefull getAssociateMarkupStream. Throws no exception in case
		// markup is not found
		final MarkupStream associatedMarkupStream = getApplication().getMarkupCache()
				.getMarkupStream(this, false);

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
		    
			// Iterate the markup and find <body onload="...">
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
				.getMarkupStream(this, false);

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
		    
		    // Create a HtmlHeaderContainer for the header tag found and
		    // add all components from addToHeader list
			final MarkupElement element = associatedMarkupStream.get();
			if (element instanceof WicketTag)
			{
				final WicketTag wTag = (WicketTag)element;
				if ((wTag.isHeadTag() == true) && (wTag.getNamespace() != null))
				{
				    // found <wicket:head>
				    // create a unique id for the HtmlHeaderContainer to be created
					final String headerId = "_" + Classes.name(this.getClass())+ this.getVariation() + "Header";
					
					// Create the header container and associate the markup with it
					WebMarkupContainer headerContainer = new TransparentWebMarkupContainer(headerId, this);
					headerContainer.setMarkupStream(associatedMarkupStream);
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
	 * Autolink component delegate component resolution to their parent
	 * components. Reason: autolink tags don't have wicket:id and users wouldn't
	 * know where to add the component to.
	 */
	private final class TransparentWebMarkupContainer extends WebMarkupContainer
		implements
			IComponentResolver
	{
		private static final long serialVersionUID = 1L;
		
		private final MarkupContainer container;
		
		/**
		 * @param id
		 * @param container
		 */
		public TransparentWebMarkupContainer(final String id, final MarkupContainer container)
		{
			super(id);
			this.container = container;
		}

		/**
		 * Because the autolink component is not able to resolve any inner
		 * component, it'll passed it down to its parent.
		 * 
		 * @param container
		 *            The container parsing its markup
		 * @param markupStream
		 *            The current markupStream
		 * @param tag
		 *            The current component tag while parsing the markup
		 * @return True if componentId was handled by the resolver, false
		 *         otherwise.
		 */
		public final boolean resolve(final MarkupContainer container,
				final MarkupStream markupStream, final ComponentTag tag)
		{
			Component component = this.container.get(tag.getId());
			if (component != null)
			{
				if (component.isVisible() == true)
				{
					component.renderComponent(markupStream);
					component.rendered();
				}
				else
				{
					findMarkupStream().skipComponent();
				}
				return true;
			}
			
			// Delegate the request to the parent component
			final MarkupContainer parent = getParent();
			component = parent.get(tag.getId());
			if (component != null)
			{
				component.render();
				return true;
			}

			if (parent instanceof IComponentResolver)
			{
				return ((IComponentResolver)parent).resolve(container, markupStream, tag);
			}
			
			return false;
		}
	}
}