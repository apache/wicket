/*
 * $Id: WebMarkupContainer.java 5861 2006-05-25 20:55:07 +0000 (Thu, 25 May
 * 2006) eelco12 $ $Revision$ $Date: 2006-05-25 20:55:07 +0000 (Thu, 25
 * May 2006) $
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
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public class WebMarkupContainer<T> extends MarkupContainer<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Component#Component(MarkupContainer,String)
	 */
	public WebMarkupContainer(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public WebMarkupContainer(MarkupContainer parent, final String id, IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * Gets the markup type for this component.
	 * 
	 * @return Markup type of HTML
	 */
	@Override
	public final String getMarkupType()
	{
		return "html";
	}

	/**
	 * A convinience method to return the WebPage. Same as getPage().
	 * 
	 * @return WebPage
	 */
	public final WebPage getWebPage()
	{
		return (WebPage)getPage();
	}
}