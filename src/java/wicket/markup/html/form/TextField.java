/*
 * $Id$ $Revision:
 * 1.10 $ $Date$
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
package wicket.markup.html.form;

import java.io.Serializable;

import wicket.markup.ComponentTag;

/**
 * A simple text field.
 * 
 * @author Jonathan Locke
 */
public class TextField extends FormComponent
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -2913294206388017417L;

	/**
	 * When the user input does not validate, this is a temporary store for the
	 * input he/she provided. We have to store it somewhere as we loose the
	 * request parameter when redirecting.
	 */
	private String invalidInput;

	/**
     * @see wicket.Component#Component(String, Serializable)
	 */
	public TextField(String name, Serializable object)
	{
		super(name, object);
	}

	/**
     * @see wicket.Component#Component(String, Serializable, String)
	 */
	public TextField(String name, Serializable object, String expression)
	{
		super(name, object, expression);
	}

	/**
	 * @see FormComponent#supportsPersistence()
	 */
	public final boolean supportsPersistence()
	{
		return true;
	}

	/**
	 * Updates this components' model from the request.
	 * 
	 * @see wicket.markup.html.form.FormComponent#updateModel()
	 */
	public void updateModel()
	{
        // Component validated, so clear the input
		invalidInput = null; 
		setModelObject(getRequestString());
	}

	/**
	 * Processes the component tag.
	 * 
	 * @param tag
	 *            Tag to modify
	 * @see wicket.Component#handleComponentTag(ComponentTag)
	 */
	protected final void handleComponentTag(final ComponentTag tag)
	{
		checkComponentTag(tag, "input");
		checkComponentTagAttribute(tag, "type", "text");
		super.handleComponentTag(tag);
		if (invalidInput == null)
		{
			// No validation errors
			tag.put("value", getModelObjectAsString());
		}
		else
		{
			// Invalid input detected
			tag.put("value", invalidInput);
		}
	}

	/**
	 * Handle a validation error.
	 * 
	 * @see wicket.markup.html.form.FormComponent#invalid()
	 */
	protected void invalid()
	{
		// Store the user input
		invalidInput = getRequestString();
	}
}