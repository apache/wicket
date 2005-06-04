/*
 * $Id$ $Revision$
 * $Date$
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
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;

/**
 * THIS IS PART OF JS AND CSS SUPPORT AND IT IS CURRENTLY EXPERIMENTAL ONLY.
 * <p>
 *  
 * @author Juergen Donnerstag
 */
public class HtmlHeaderContainer extends WebMarkupContainer
{
	/** The open tag for this container. */
	private transient ComponentTag openTag;
	
	/**
     * Construct.
	 */
	public HtmlHeaderContainer()
	{
		super("_header");
		setRenderBodyOnly(true);
	}
	
	/**
     * Construct.
     * 
     * @param id
     * @param associatedMarkupStream
	 */
	public HtmlHeaderContainer(final String id, final MarkupStream associatedMarkupStream)
	{
	    super(id);
	    setMarkupStream(associatedMarkupStream);
	}

	/**
	 * Render the header container component.
	 */
	private void renderHeadSections()
	{
		// collect all header parts and render them
		getParent().visitChildren(WebMarkupContainer.class, new IVisitor()
        {
		    private int nbrOfContributions;
		    
			/**
			 * @see wicket.Component.IVisitor#component(wicket.Component)
			 */
			public Object component(Component component)
			{
				if (component.isVisible())
				{
					WebMarkupContainer webMarkupContainer = (WebMarkupContainer)component;
					WebMarkupContainer headerPart = webMarkupContainer.getHeaderPart(nbrOfContributions);

					// In case a Component with header has been added to the page 
					// multiple times, the header must only be added once.
					if ((headerPart != null) && (get(headerPart.getId()) == null))
					{
						autoAdd(headerPart);
						nbrOfContributions++;
					}
				}
				return IVisitor.CONTINUE_TRAVERSAL;
			}
        });
	}
	
	/**
	 * First render the body of component. And if it is the header component 
	 * of a Page (compared to a Panel or Border), than get the header sections
	 * from all component in the hierachie and append them.
	 * 
	 * @see wicket.MarkupContainer#onComponentTagBody(wicket.markup.MarkupStream, wicket.markup.ComponentTag)
	 */
	protected void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
	{
		super.onComponentTagBody(markupStream, openTag);
		
		// render the header section only, if we are on a Page
		// Panels and Border do not need to render the header section
		if (getParent() instanceof WebPage)
		{
		    renderHeadSections();
		}
	}
}
