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
package wicket.markup.html.form;

import wicket.markup.html.form.validation.RequiredValidator;
import wicket.model.IModel;

/**
 * A text field which automatically adds a RequiredValidator. This is mainly for
 * convenience, since you can always add(new RequiredValidator()) manually.
 * 
 * @author Jonathan Locke
 */
public class RequiredTextField extends TextField
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -2913294206388017417L;

	/**
	 * @see wicket.Component#Component(String)
	 */
	public RequiredTextField(final String name)
	{
		super(name);
		add(RequiredValidator.getInstance());
	}

	/**
	 * @see TextField#TextField(String, Class)
	 */
	public RequiredTextField(final String name, final Class type)
	{
		super(name, type);
		add(RequiredValidator.getInstance());
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public RequiredTextField(final String name, final IModel model)
	{
		super(name, model);
		add(RequiredValidator.getInstance());
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param model
	 *            See Component constructor
	 * @param type
	 *            The type to use when updating the model for this text field
	 * @see wicket.Component#Component(String, IModel)
	 */
	public RequiredTextField(String name, IModel model, Class type)
	{
		super(name, model, type);
		add(RequiredValidator.getInstance());
	}
}