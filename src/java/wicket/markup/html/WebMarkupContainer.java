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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.model.IModel;

/**
 * A container of HTML markup and components. It is very similar to the base
 * class MarkupContainer, except that the markup type is defined to be HTML.
 * 
 * @author Jonathan Locke
 */
public class WebMarkupContainer extends MarkupContainer
{
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
		
//		// get the component from the cache by its class
//		MarkupContainer comp = getApplication().getHeadComponentCache().get(this, getClass());
//		// if the component is not found create it:
//		if(comp == null)
//		{
//			// create new panel
//			final MarkupStream ms = getApplication().getMarkupCache().getHeadMarkupStream(this, getClass());
//			if(ms != null)
//			{
//				// get the header markup portion of the component markup file and set it as the components markup:
//				comp = new MarkupContainer("header:" + getClass()) 
//				{
//					/* (non-Javadoc)
//					 * @see wicket.MarkupContainer#getMarkupStream()
//					 */
//					protected MarkupStream getMarkupStream()
//					{
//						return ms;
//					}
//				};
//			}
//			else
//			{
//				comp = emptyHeader;
//			}
//			getApplication().getHeadComponentCache().put(this,getClass(), comp);
//		}
//		return comp;
		
		return null;
	}
}