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

import wicket.model.IModel;

/**
 * A text field which automatically adds a RequiredValidator. This is mainly for
 * convenience, since you can always add(new RequiredValidator()) manually.
 * 
 * @author Jonathan Locke
 */
public class RequiredTextField extends TextField
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see wicket.Component#Component(String)
	 */
	public RequiredTextField(final String id)
	{
		super(id);
		setRequired(true);
	}

	/**
	 * @see TextField#TextField(String, Class)
	 */
	public RequiredTextField(final String id, final Class type)
	{
		super(id, type);
		setRequired(true);
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public RequiredTextField(final String id, final IModel model)
	{
		super(id, model);
		setRequired(true);
	}

	/**
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param type
	 *            The type to use when updating the model for this text field
	 * @see wicket.Component#Component(String, IModel)
	 */
	public RequiredTextField(final String id, IModel model, Class type)
	{
		super(id, model, type);
		setRequired(true);
	}
}