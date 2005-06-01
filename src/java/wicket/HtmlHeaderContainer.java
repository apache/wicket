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
package wicket;

import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;

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
	 * 
	 */
	public final void renderHeadSections()
	{
		// TODO
		// We probably have to embed our magical head part children in a seperate container,
		// so that we can remove and re-add them on each render
		// Also, we have to somehow dynamically insert that component into the markup stream
		// just as we have to generate a head part in any HTML/Web markup when it doesn't
		// exist yet.
		// The problem is when and where to do it. Juergen, any idea how to go on from this
		// point? I think I have got the markup part going ok. Now it has to all add up...
		
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
