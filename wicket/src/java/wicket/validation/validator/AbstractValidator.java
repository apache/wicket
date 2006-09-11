/*
 * $Id: AbstractValidator.java 5798 2006-05-20 15:55:29 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision: 7269 $ $Date: 2006-05-20 15:55:29 +0000 (Sat, 20 May
 * 2006) $
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
package wicket.validation.validator;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import wicket.model.IModel;
import wicket.model.Model;
import wicket.util.lang.Classes;
import wicket.validation.IValidatable;
import wicket.validation.IValidator;
import wicket.validation.ValidationError;

/**
 * FIXME 2.0: ivaynberg: cleanup javadoc
 * 
 * FIXME 2.0: ivaynberg: explain validate on null value
 * 
 * Base class for form component validators. This class is thread-safe and
 * therefore it is safe to share validators across sessions/threads.
 * <p>
 * Error messages can be registered on a component by calling one of the
 * error(FormComponent ...) overloads. The error message will be retrieved using
 * the Localizer for the form component. Normally, this localizer will find the
 * error message in a string resource bundle (properties file) associated with
 * the page in which this validator is contained. The key that is used to get
 * the message defaults to the pattern:
 * <code>[form-name].[component-name].[validator-class]</code>. For example:
 * <p>
 * MyForm.name.RequiredValidator=A name is required.
 * <p>
 * Error message string resources can contain optional property variable
 * interpolations from the component, such as:
 * <p>
 * editBook.name.LengthValidator='${input}' is too short a name.
 * <p>
 * Available variables for interpolation are:
 * <ul>
 * <li>${input} - The user's input</li>
 * <li>${name} - The name of the component</li>
 * <li>${label} - the label of the component - either comes from
 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
 * that order</li>
 * </ul>
 * but specific validator subclasses may add more values.
 * </p>
 * 
 * @param <T>
 *            type of value being validated
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynbeg)
 * 
 */
public abstract class AbstractValidator<T> implements IValidator<T>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Whether or not to validate the value if it is null. We usually want to
	 * skip validation if the value is null - unless we want to make sure the
	 * value is in fact null which is a rare usecases. Validators that extend
	 * this and wish to validate that the value is null should override this
	 * method and return tru.
	 * 
	 * @return true to validate on null value, false to skip validation on null
	 *         value
	 */
	public boolean validateOnNullValue()
	{
		return false;
	}

	protected abstract void onValidate(IValidatable<T> validatable);

	/**
	 * @see wicket.validation.IValidator#validate(wicket.validation.IValidatable)
	 */
	public final void validate(IValidatable<T> validatable)
	{
		if (validatable.getValue() != null || validateOnNullValue())
		{
			onValidate(validatable);
		}
	}


	/**
	 * Sets an error on the component being validated using the map returned by
	 * messageModel() for variable interpolations.
	 * <p>
	 * See class comments for details about how error messages are loaded and
	 * formatted.
	 * 
	 * @param validatable
	 *            validatble being validated
	 * 
	 */
	public void error(final IValidatable<T> validatable)
	{
		error(validatable, resourceKey(), messageModel(validatable));
	}

	/**
	 * Sets an error on the component being validated using the given map for
	 * variable interpolations.
	 * 
	 * @param validatable
	 *            validatble being validated
	 * @param resourceKey
	 *            The resource key to use
	 * @param map
	 *            The model for variable interpolation
	 */
	public void error(final IValidatable<T> validatable, final String resourceKey,
			final Map<String, Serializable> map)
	{
		error(validatable, resourceKey, (map == null)
				? new Model<Map<String, Serializable>>()
				: Model.valueOf(map));
	}

	/**
	 * Sets an error on the component being validated using the given map for
	 * variable interpolations.
	 * 
	 * @param validatable
	 *            validatble being validated
	 * @param map
	 *            The model for variable interpolation
	 */
	public void error(final IValidatable<T> validatable, final Map<String, Serializable> map)
	{
		error(validatable, resourceKey(), (map == null)
				? new Model<Map<String, Serializable>>()
				: Model.valueOf(map));
	}

	/**
	 * Returns a formatted validation error message for a given component. The
	 * error message is retrieved from a message bundle associated with the page
	 * in which this validator is contained using the given resource key. The
	 * resourceModel is used for variable interpolation. If that one is null the
	 * default one is created from messageModel(formComponent)
	 * 
	 * @param validatable
	 *            validatble being validated
	 * @param resourceKey
	 *            The resource key to use
	 * @param resourceModel
	 *            The model for variable interpolation, it needs to have a map
	 *            inside it.
	 */
	public void error(final IValidatable<T> validatable, final String resourceKey,
			IModel<Map<String, Serializable>> resourceModel)
	{
		if (validatable == null)
		{
			throw new IllegalArgumentException("Argument [[validatable]] cannot be null");
		}
		if (resourceModel == null)
		{
			resourceModel = Model.valueOf(messageModel(validatable));
		}
		if (resourceKey == null)
		{
			throw new IllegalArgumentException("Argument [[resourceKey]] cannot be null");
		}


		ValidationError error = new ValidationError(resourceKey);
		final String defaultKey = Classes.simpleName(getClass());
		if (!resourceKey.equals(defaultKey))
		{
			error.addKey(defaultKey);
		}

		Map<String, Serializable> args = resourceModel.getObject();
		error.getParams().putAll(args);
		validatable.error(error);
	}

	/**
	 * Gets the resource key for validator's error message from the
	 * ApplicationSettings class.
	 * 
	 * @return the resource key for the validator
	 */
	protected String resourceKey()
	{
		return Classes.simpleName(getClass());
	}

	/**
	 * FIXME 2.0: ivaynberg: clean up javadoc - defaults come from wicket's
	 * messagesource
	 * 
	 * Gets the default variables for interpolation. These are:
	 * <ul>
	 * <li>${input}: the user's input</li>
	 * <li>${name}: the name of the component</li>
	 * <li>${label}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @param validatable
	 *            validatable being validated
	 * 
	 * @param formComponent
	 *            form component
	 * @return a map with the variables for interpolation
	 */
	protected Map<String, Serializable> messageModel(IValidatable<T> validatable)
	{
		final Map<String, Serializable> resourceModel = new HashMap<String, Serializable>(1);
		return resourceModel;
	}

}
