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
import wicket.IEventRequestHandler;
import wicket.model.IModel;

/**
 * Base class for simple HTML components which do not hold nested components. If
 * you need to support nested components, see WebMarkupContainer or use Panel if the
 * component will have its own associated markup.
 * 
 * @see wicket.markup.html.WebMarkupContainer
 * @author Jonathan Locke
 */
public class WebComponent extends Component implements IHeaderContributor
{
	/**
	 * @see Component#Component(String)
	 */
	public WebComponent(final String id)
	{
		super(id);
	}

	/**
	 * @see Component#Component(String, IModel)
	 */
	public WebComponent(final String id, final IModel model)
	{
		super(id, model);
	}

	/**
	 * Print to the web response what ever the component wants
	 * to contribute to the head section. Does nothing by default.
	 *
	 * @param container The HtmlHeaderContainer
	 * @see wicket.markup.html.IHeaderContributor#printHead(wicket.markup.html.HtmlHeaderContainer)
	 */
	public void printHead(final HtmlHeaderContainer container)
	{
		IEventRequestHandler[] handlers = getEventRequestHandlers();
		if (handlers != null)
		{
			for (int i = 0; i < handlers.length; i++)
			{
				if (handlers[i] instanceof IHeaderContributor)
				{
					((IHeaderContributor)handlers[i]).printHead(container);
				}
			}
		}
	}

	/**
	 * Renders this component.
	 */
	protected void onRender()
	{
		renderComponent(findMarkupStream());
	}
}