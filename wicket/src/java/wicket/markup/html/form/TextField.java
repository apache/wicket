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
	/** Model type for conversions */
	private Class type;

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
		this.type = type;
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
		this.type = type;
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
	protected void updateModel()
	{
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