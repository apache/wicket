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
 * 
 * @author Juergen Donnerstag
 */
public class BodyOnLoadContainer extends WebMarkupContainer implements IComponentResolver
{
	/**
	 * Constructor used by HtmlHeaderResolver. The id is fix "_header"
	 * and the markup stream will be provided by the parent component.
	 */
	public BodyOnLoadContainer()
	{
		// There is only one HtmlHeaderContainer allowed. That is we
		// don't have to worry about creating a unique id.
		super("_body");
	}

	/**
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
	    final String onLoad = tag.getAttributes().getString("onload");
	    if (onLoad != null)
	    {
	        ((WebPage)this.getPage()).addBodyOnLoad(onLoad);
	    }
	    
		super.onComponentTag(tag);
	}

	/**
	 * @see wicket.IComponentResolver#resolve(wicket.MarkupContainer, wicket.markup.MarkupStream, wicket.markup.ComponentTag)
	 */
	public boolean resolve(MarkupContainer container, MarkupStream markupStream, ComponentTag tag)
	{
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
