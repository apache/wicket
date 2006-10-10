/*
 * $Id: WebComponent.java 5861 2006-05-25 20:55:07 +0000 (Thu, 25 May 2006)
 * eelco12 $ $Revision$ $Date: 2006-05-25 20:55:07 +0000 (Thu, 25 May
 * 2006) $
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
import wicket.markup.MarkupStream;
import wicket.model.IModel;

/**
 * Base class for simple HTML components which do not hold nested components. If
 * you need to support nested components, see WebMarkupContainer or use Panel if
 * the component will have its own associated markup.
 * 
 * @see wicket.markup.html.WebMarkupContainer
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Eelco Hillenius
 */
public class WebComponent<T> extends Component<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * @see Component#Component(MarkupContainer,String)
	 */
	public WebComponent(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see Component#Component(MarkupContainer,String, IModel)
	 */
	public WebComponent(MarkupContainer parent, final String id, final IModel<T> model)
	{
		super(parent, id, model);
	}

	/**
	 * 
	 * @see wicket.Component#onRender(wicket.markup.MarkupStream)
	 */
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		renderComponent(markupStream);
	}
}