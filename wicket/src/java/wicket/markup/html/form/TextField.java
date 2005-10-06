/*
 * $Id$ $Revision:
 * 1.10 $ $Date$
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

import wicket.markup.ComponentTag;
import wicket.markup.html.form.validation.TypeValidator;
import wicket.model.IModel;

/**
 * A simple text field.
 * 
 * @author Jonathan Locke
 */
public class TextField extends AbstractTextComponent
{
	private static final long serialVersionUID = 1L;
	
	/**
	 * @see wicket.Component#Component(String)
	 */
	public TextField(final String id)
	{
		super(id);
	}

	/**
	 * @param id
	 *            See Component
	 * @param type
	 *            Type for field validation
	 */
	public TextField(final String id, final Class type)
	{
		super(id);
		add(new TypeValidator(type));
	}

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public TextField(final String id, final IModel object)
	{
		super(id, object);
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
	public TextField(final String id, IModel model, Class type)
	{
		super(id, model);
		add(new TypeValidator(type));
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#onComponentTag(ComponentTag)
	 */
	protected void onComponentTag(final ComponentTag tag)
	{
		// Must be attached to an input tag
		checkComponentTag(tag, "input");

		// If this is not a subclass (PasswordTextField)
		if (getClass() == TextField.class)
		{
			// check for text type
			checkComponentTagAttribute(tag, "type", "text");
		}

		// No validation errors
		tag.put("value", getValue());
		
		// Default handling for component tag
		super.onComponentTag(tag);
	}

	/**
	 * @see wicket.markup.html.form.AbstractTextComponent#updateModel()
	 */
	public void updateModel()
	{
		String input = getInput();
		// if input was null then value was not submitted (disabled field), ignore it
		if (input != null)
		{
			// Get any validation type
			final Class type = getValidationType();
			if (type != null)
			{
				// Set model to request string converted to the appropriate type
				setModelObject(getConverter().convert(getInput(), type));
			}
			else
			{
				// Update String model
				super.updateModel();
			}
		}
	}
}
