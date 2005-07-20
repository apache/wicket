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
import wicket.model.Model;
import wicket.util.lang.Classes;

/**
 * Base class for form component validators. This class is thread-safe and therefore it is
 * safe to share validators across sessions/threads.
 * <p>
 * Error messages can be registered on a component by calling one of the error(FormComponent ...)
 * overloads. The error message will be retrieved using the Localizer for the form
 * component. Normally, this localizer will find the error message in a string resource
 * bundle (properties file) associated with the page in which this validator is contained.
 * The key that is used to get the message can be set explicity by calling setResourceKey.
 * If that key is not explicitly set, it default to the pattern:
 * <code>[form-name].[component-name].[validator-class]</code>.
 * For example:
 * <p>
 * MyForm.name.RequiredValidator=A name is required.
 * <p>
 * Error message string resources can contain optional ognl variable interpolations from
 * the component, such as:
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
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class AbstractValidator implements IValidator
{
	/**
	 * Any set resource key. When it is not set, the validator uses it's default
	 * message key.
	 */
	private String resourceKey;

	/**
	 * Sets an error on the component being validated using the map returned by
	 * messageModel() for variable interpolations.
	 * <p>
	 * See class comments for details about how error messages are loaded and formatted.
	 * @param formComponent form component
	 */
	public void error(FormComponent formComponent)
	{
		error(formComponent, messageModel(formComponent));
	}

	/**
	 * Returns a formatted validation error message for a given component. The error
	 * message is retrieved from a message bundle associated with the page in which this
	 * validator is contained using the given resource key. The resourceModel is used for
	 * variable interpolation.
	 * @param formComponent form component
	 * @param resourceKey The resource key to use
	 * @param resourceModel The model for variable interpolation
	 */
	public void error(FormComponent formComponent, final String resourceKey,
			final IModel resourceModel)
	{
		// Return formatted error message
		Localizer localizer = formComponent.getLocalizer();
		String message = localizer.getString(resourceKey, formComponent, resourceModel);
		formComponent.error(message);
	}

	/**
	 * Sets an error on the component being validated using the given map for variable
	 * interpolations.
	 * @param formComponent form component
	 * @param resourceKey The resource key to use
	 * @param map The model for variable interpolation
	 */
	public void error(FormComponent formComponent, final String resourceKey, final Map map)
	{
		error(formComponent, resourceKey, Model.valueOf(map));
	}

	/**
	 * Sets an error on the component being validated using the given map for variable
	 * interpolations.
	 * @param formComponent form component
	 * @param map The model for variable interpolation
	 */
	public void error(FormComponent formComponent, final Map map)
	{
		error(formComponent, getResourceKey(formComponent), Model.valueOf(map));
	}

	/**
	 * Sets the resource key that should be used.
	 * @param resourceKey the resource key
	 */
	public final void setResourceKey(String resourceKey)
	{
		this.resourceKey = resourceKey;
	}

	/**
	 * Returns the explicitly set resource key or null when it was not set explicitly.
	 * @return the explicitly set resource key or null
	 */
	protected final String getResourceKey()
	{
		return resourceKey;
	}

	/**
	 * Gets the resource key based on the form component.
	 * If the resource key is set explicitly with setResourceKey, that is used.
	 * Otherwise it will default to the form:
	 * <code>[form-name].[component-name].[validator-class]</code>
	 * @param formComponent form component
	 * @return the resource key based on the form component
	 */
	protected final String getResourceKey(FormComponent formComponent)
	{
		// in case the key was set explicitly
		if (resourceKey != null)
		{
			return resourceKey; // return that
		}

		// otherwise use a default pattern of form
		// <form-id>.<component-name>.<validator-class>
		return formComponent.getForm().getId() + "." + formComponent.getId() + "."
				+ Classes.name(getClass());
	}

	/**
	 * Gets the default variables for interpolation. These are:
	 * <ul>
	 * <li>${input}: the user's input</li>
	 * <li>${name}: the name of the component</li>
	 * </ul>
	 * @param formComponent form component
	 * @return a map with the variables for interpolation
	 */
	protected Map messageModel(FormComponent formComponent)
	{
		final Map resourceModel = new HashMap(4);
		resourceModel.put("input", formComponent.getInput());
		resourceModel.put("name", formComponent.getId());
		return resourceModel;
	}
}
