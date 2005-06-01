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
import wicket.HtmlHeaderContainer;
import wicket.MarkupContainer;
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
 */
public class WebMarkupContainer extends MarkupContainer
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
	 * Gets the header part for this markup container. Returns null if it doesn't contribute
	 * a header part.
	 * @param index TODO
	 * @return the header part for this markup container or null
	 * 	if it doesn't contribute anything.
	 */
	public WebMarkupContainer getHeaderPart(int index)
	{
	    // gracefull getAssociateMarkupStream. Throws no exception in case
	    // markup is not found
		final MarkupStream associatedMarkupStream = 
		    	getApplication().getMarkupCache().getMarkupStream(this, null, false);
		
		if (associatedMarkupStream == null)
		{
		    return null;
		}

		do
		{
		    final MarkupElement element = associatedMarkupStream.get();
		    if (element instanceof WicketTag)
		    {
		        final WicketTag wTag = (WicketTag) element;
		        if (wTag.isHeadTag() == true)
		        {
		            final String headerId = "_" + Classes.name(this.getClass()) + "Header";
		            WebMarkupContainer headerContainer = new HtmlHeaderContainer(headerId, associatedMarkupStream);
		            if (this.headerComponents != null)
		            {
		                for (Iterator iter = headerComponents.iterator(); iter.hasNext(); )
		                {
		                    headerContainer.add((Component) iter.next());
		                }
		                
		                // Cleanup; no longer needed.
		                this.headerComponents = null;
		            }
		            return headerContainer;
		        }
		    }
		} 
		while (associatedMarkupStream.next() != null);

		if (this.headerComponents == null)
		{
		    throw new MarkupException("You have added header components but did not specific a <wicket:head> region in your markup");
		}
    	return null;
	}
	
	/**
	 * Add component to &lt;wicket:head&gt; instead of panel region. 
	 * 
	 * @param child
	 */
	public void addToHeader(final Component child)
	{
	    if (this.headerComponents == null)
	    {
	        this.headerComponents = new ArrayList();
	    }
	    
	    this.headerComponents.add(child);
	}
}