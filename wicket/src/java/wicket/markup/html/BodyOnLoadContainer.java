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

import wicket.AttributeModifier;
import wicket.Component;
import wicket.IComponentResolver;
import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.model.Model;

/**
 * Handle &lt;body&gt; tags. The reason why this is a component is because
 * of JavaScript and CSS support which requires to append body onload 
 * attributes from child component markup to the page's body tag.
 * 
 * @author Juergen Donnerstag
 */
public class BodyOnLoadContainer extends WebMarkupContainer implements IComponentResolver
{
	/**
	 * Constructor used by BodyOnLoadContainer. The id is fix "_body"
	 * and the markup stream will be provided by the parent component.
	 * 
	 * @param id Componen id
	 */
	public BodyOnLoadContainer(final String id)
	{
		super(id);
	}

	/**
	 * If parent is WebPage append onload attribute values from all components
	 * in the hierarchie with onload attribute in there own markup.
	 *  
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
	    // If WebPage ...
	    // If not WebPage, than just be a WebMarkupContainer
	    if (getParent() instanceof WebPage)
	    {
	        // The consolidated onload of all child components
	        String onLoad = ((WebPage)this.getPage()).getBodyOnLoad();
	        
	        // Get the page's onLoad attribute value. Null if not given
	        String pageOnLoad = tag.getAttributes().getString("onload");

	        // Add an AttributeModifier if onLoad must be changed
	        if ((pageOnLoad != null) && (onLoad != null))
	        {
	            onLoad = pageOnLoad + onLoad;
	        }
	        
            if (onLoad != null)
            {
		        add(new AttributeModifier("onload", true, new Model(onLoad)));
            }
	    }
	    
	    // go on with default implementation
		super.onComponentTag(tag);
	}

	/**
	 * BodyOnLoadContainer has been autoAdded, it has been injected similiar
	 * to an AOP interceptor. Thus BodyOnLoadContainer must forward any request
	 * to find a component based on an ID to its parent container.
	 *  
	 * @see wicket.IComponentResolver#resolve(wicket.MarkupContainer, wicket.markup.MarkupStream, wicket.markup.ComponentTag)
	 */
	public boolean resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
	    // Try to find the component with the parent component.
		MarkupContainer parent = getParent();
		if (parent != null)
		{
		    if (parent.getId().equals(tag.getId()))
		    {
		        parent.render();
		        return true;
		    }
		    
		    Component component = parent.get(tag.getId());
		    if (component != null)
		    {
		        component.render();
		        return true;
		    }
		}
		
		return false;
	}
}
