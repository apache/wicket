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
package wicket.markup.html.form.validation;

import java.util.HashMap;
import java.util.Map;

import wicket.Localizer;
import wicket.markup.html.form.FormComponent;
import wicket.model.IModel;
import wicket.model.MapModel;
import wicket.util.lang.Classes;

/**
 * Base class for form component validators. This class is thread-safe and
 * therefore it is safe to share validators across sessions/threads.
 * <p>
 * Error messages can be registered on a component by calling one of the error()
 * overloads. The error message will be retrieved using the Localizer for the
 * form component. Normally, this localizer will find the error message in a
 * string resource bundle (properties file) associated with the page in which
 * this validator is contained. The resource key must be of the form:
 * [form-name].[component-name].[validator-class]. For example:
 * <p>
 * MyForm.name.RequiredValidator=A name is required.
 * <p>
 * Error message string resources can contain optional ognl variable
 * interpolations from the component, such as:
 * <p>
 * editBook.name.LengthValidator='${input}' is too short a name.
 * <p>
 * Available variables for interpolation are:
 * <ul>
 * <li>${input} - The user's input</li>
 * <li>${name} - The name of the component</li>
 * </ul>
 * but specific validator subclasses may add more values.
 * </p>
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class AbstractValidator implements IValidator
{
	/** The form component being validated */
	private FormComponent formComponent;

	/**
	 * Sets an error on the component being validated using the map returned by
	 * messageModel() for variable interpolations.
	 * <p>
	 * See class comments for details about how error messages are loaded and
	 * formatted.
	 */
	public void error()
	{
		error(messageModel());
	}

	/**
	 * Returns a formatted validation error message for a given component. The
	 * error message is retrieved from a message bundle associated with the page
	 * in which this validator is contained using the given resource key. The
	 * resourceModel is used for variable interpolation.
	 * 
	 * @param resourceKey
	 *            The resource key to use
	 * @param resourceModel
	 *            The model for variable interpolation
	 */
	public void error(final String resourceKey, final IModel resourceModel)
	{
		// Return formatted error message
		Localizer localizer = formComponent.getLocalizer();
		String message = localizer.getString(resourceKey, formComponent, resourceModel);
		formComponent.error(message);
	}

	/**
	 * Sets an error on the component being validated using the given map for
	 * variable interpolations.
	 * 
	 * @param resourceKey
	 *            The resource key to use
	 * @param map
	 *            The model for variable interpolation
	 */
	public void error(final String resourceKey, final Map map)
	{
		error(resourceKey, MapModel.valueOf(map));
	}

	/**
	 * Sets an error on the component being validated using the given map for
	 * variable interpolations.
	 * 
	 * @param map
	 *            The model for variable interpolation
	 */
	public void error(final Map map)
	{
		error(resourceKey(), MapModel.valueOf(map));
	}

	/**
	 * @return Returns the component.
	 */
	public FormComponent getFormComponent()
	{
		return formComponent;
	}

	/**
	 * @return The string value being validated
	 */
	public String getInput()
	{
		return formComponent.getInput();
	}

	/**
	 * Implemented by subclasses to validate component
	 */
	public abstract void onValidate();

	/**
	 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
	 */
	public synchronized final void validate(final FormComponent formComponent)
	{
		// Save component
		this.formComponent = formComponent;

		// Cause validation to happen
		onValidate();
	}

	/**
	 * Gets the default variables for interpolation. These are:
	 * <ul>
	 * <li>${input}: the user's input</li>
	 * <li>${name}: the name of the component</li>
	 * </ul>
	 * 
	 * @return a map with the variables for interpolation
	 */
	protected Map messageModel()
	{
		final Map resourceModel = new HashMap(4);
		resourceModel.put("input", getInput());
		resourceModel.put("name", formComponent.getId());
		return resourceModel;
	}

	/**
	 * Gets the resource key based on the form component. It will have the form:
	 * <code>[form-name].[component-name].[validator-class]</code>
	 * 
	 * @return the resource key based on the form component
	 */
	protected String resourceKey()
	{
		// Resource key must be <form-name>.<component-name>.<validator-class>
		return formComponent.getForm().getId() + "." + formComponent.getId() + "."
				+ Classes.name(getClass());
	}
}
