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
import wicket.IComponentResolver;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;

/**
 * THIS IS PART OF JS AND CSS SUPPORT AND IT IS CURRENTLY EXPERIMENTAL ONLY.
 * <p>
 *  
 * @author Juergen Donnerstag
 */
public class HtmlHeaderContainer extends WebMarkupContainer implements IComponentResolver
{
	/** The open tag for this container. */
	private transient ComponentTag openTag;
	
	/**
     * Construct.
	 */
	public HtmlHeaderContainer()
	{
		super("_header");
		
		// TODO if you wish to remove <wicket:head> from output 
		//setRenderComponentTag(false);
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
	 * Render the body of &lt;wicket:extend&gt; First get both markups involved
	 * and switch between both if &lt;wicket:child&gt; is found in the 
	 * base class' markup.
	 * 
	 * @see wicket.Component#onRender()
	 */
	protected final void onRender()
	{
		// go one rendering the component
		super.onRender();
	
		// render the header section only, if we are on a Page
		// Panels and Border do not need to render the header section
		if (getParent() instanceof WebPage)
		{
		    renderHeadSections();
		}
	}

	/**
	 * @see wicket.IComponentResolver#resolve(wicket.MarkupContainer, wicket.markup.MarkupStream, wicket.markup.ComponentTag)
	 */
	public boolean resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
		Component parent = getParent().get(tag.getId());
		if (parent != null)
		{
		    parent.render();
		    return true;
		}
		
		return false;
	}
}
