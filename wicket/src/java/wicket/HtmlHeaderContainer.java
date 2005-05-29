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
	}
	
	/**
	 * Render the body of &lt;wicket:extend&gt; First get both markups involved
	 * and switch between both if &lt;wicket:child&gt; is found in the 
	 * base class' markup.
	 * 
	 * @see wicket.Component#onComponentTagBody(wicket.markup.MarkupStream,
	 *      wicket.markup.ComponentTag)
	 */
	protected final void onComponentTagBody(final MarkupStream markupStream, final ComponentTag openTag)
	{
	    this.openTag = openTag;

		// go one rendering the component
		super.onComponentTagBody(markupStream, openTag);
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
