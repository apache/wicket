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

import java.io.Serializable;

import wicket.markup.ComponentTag;
import wicket.markup.html.form.validation.TypeValidator;

/**
 * A simple text field.
 * 
 * @author Jonathan Locke
 */
public class TextField extends AbstractTextComponent
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -2913294206388017417L;

	/** Model type for conversions */
	private Class type;

	/**
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public TextField(String name, Serializable object)
	{
		super(name, object);
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param type
	 *            The type to use when updating the model for this text field
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public TextField(String name, Serializable object, Class type)
	{
		super(name, object);
		this.type = type;
		add(new TypeValidator(type));
	}

	/**
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public TextField(String name, Serializable object, String expression)
	{
		super(name, object, expression);
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param expression
	 *            See Component constructor
	 * @param type
	 *            The type to use when updating the model for this text field
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public TextField(String name, Serializable object, String expression, Class type)
	{
		super(name, object, expression);
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

		// Default handling for component tag
		super.onComponentTag(tag);

		if (getInvalidInput() == null)
		{
			// No validation errors
			tag.put("value", getModelObjectAsString());
		}
		else
		{
			// Invalid input detected
			tag.put("value", getInvalidInput());
		}
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