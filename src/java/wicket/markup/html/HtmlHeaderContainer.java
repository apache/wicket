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

import wicket.MarkupContainer;
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
		MarkupContainer parent = getParent();
		if (parent instanceof IHeaderRenderer)
		{
		    ((IHeaderRenderer)parent).renderHeadSections(this);
		}
	}
}
