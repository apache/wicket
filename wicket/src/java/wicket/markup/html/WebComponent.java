/*
 * $Id$ $Revision:
 * 1.15 $ $Date$
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

import java.util.Iterator;
import java.util.List;

import wicket.Component;
import wicket.IBehaviour;
import wicket.markup.html.ajax.IBodyOnloadContributor;
import wicket.model.IModel;

/**
 * Base class for simple HTML components which do not hold nested components. If
 * you need to support nested components, see WebMarkupContainer or use Panel if
 * the component will have its own associated markup.
 * 
 * @see wicket.markup.html.WebMarkupContainer
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public class WebComponent extends Component implements IHeaderContributor
{
	private static final long serialVersionUID = 1L;

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
	 * THIS IS NOT PART OF WICKETS PUBLIC API. DO NOT CALL IT YOURSELF Print to
	 * the web response what ever the component wants to contribute to the head
	 * section. Does nothing by default.
	 * 
	 * @param container
	 *            The HtmlHeaderContainer
	 * @see wicket.markup.html.IHeaderContributor#renderHead(wicket.markup.html.HtmlHeaderContainer)
	 */
	public void renderHead(final HtmlHeaderContainer container)
	{
		// get head and body contributions in one loop
		// NOTE: THIS CODE MUST BE IN SYNC WITH SAME PIECE OF CODE in WEBMARKUPCONTAINER
		List behaviours = getBehaviours();
		for (Iterator i = behaviours.iterator(); i.hasNext();)
		{
			IBehaviour behaviour = (IBehaviour)i.next();
			if (behaviour instanceof IHeaderContributor)
			{
				((IHeaderContributor)behaviour).renderHead(container);
			}

			if (behaviour instanceof IBodyOnloadContributor)
			{
				String stmt = ((IBodyOnloadContributor)behaviour).getBodyOnload();
				if (stmt != null)
				{
					((WebPage)getPage()).appendToBodyOnLoad(stmt);
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