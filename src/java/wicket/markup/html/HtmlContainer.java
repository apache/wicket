/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import java.io.Serializable;

import wicket.Component;
import wicket.Container;
import wicket.model.IModel;

/**
 * A container of HTML markup and components. It is very similar to the base
 * class Container, except that the markup type is defined to be HTML.
 * 
 * @author Jonathan Locke
 */
public class HtmlContainer extends Container
{
	/** Serial Version ID. */
	private static final long serialVersionUID = 4704928946724566158L;

	/**
	 * @see Component#Component(String)
	 */
	public HtmlContainer(final String name)
	{
		super(name);
	}

	/**
	 * @see Component#Component(String, IModel)
	 */
	public HtmlContainer(final String name, final IModel model)
	{
		super(name, model);
	}

	/**
	 * @see Component#Component(String, IModel, String)
	 */
	public HtmlContainer(final String name, final IModel model, final String expression)
	{
		super(name, model, expression);
	}

	/**
	 * @see Component#Component(String, Serializable)
	 */
	public HtmlContainer(final String name, final Serializable object)
	{
		super(name, object);
	}

	/**
	 * @see Component#Component(String, Serializable, String)
	 */
	public HtmlContainer(final String name, final Serializable object, final String expression)
	{
		super(name, object, expression);
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
	protected void handleRender()
	{
		renderComponent(findMarkupStream());
	}
}