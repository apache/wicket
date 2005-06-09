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

import java.util.Map;

import wicket.Localizer;
import wicket.markup.html.form.Form;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * Base class for form level (oposed to form component level validators).
 * This class is thread-safe and therefore it is safe to share validators
 * across sessions/threads.
 * <p>
 * Error messages can be registered on the form by calling one of the error()
 * overloads. The error message will be retrieved using the Localizer for the
 * form component. Normally, this localizer will find the error message in a
 * string resource bundle (properties file) associated with the page in which
 * this validator is contained.
 * </p>
 * <p>
 * Form validators should be used to validate anything that can not be related to just
 * one field.
 * </p>
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class AbstractFormValidator implements IFormValidator
{
	/** The form component being validated */
	private Form form;

	/**
	 * Construct.
	 */
	public AbstractFormValidator()
	{		
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
		Localizer localizer = form.getLocalizer();
		String message = localizer.getString(resourceKey, form, resourceModel);
		form.error(message);
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
		error(resourceKey, Model.valueOf(map));
	}

	/**
	 * @return Returns the component.
	 */
	public Form getForm()
	{
		return form;
	}

	/**
	 * Implemented by subclasses to validate form.
	 */
	public abstract void onValidate();

	/**
	 * @see wicket.markup.html.form.validation.IFormValidator#validate(wicket.markup.html.form.Form)
	 */
	public synchronized final void validate(final Form form)
	{
		// Save component
		this.form = form;

		// Cause validation to happen
		onValidate();
	}
}
