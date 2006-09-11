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

import java.util.HashMap;
import java.util.Map;

import wicket.util.lang.Classes;
import wicket.validation.IValidatable;
import wicket.validation.IValidator;
import wicket.validation.ValidationError;

/**
 * Convinience base class for {@link IValidator}s. This class is thread-safe
 * and therefore it is safe to share validators across sessions/threads.
 * <p>
 * Error messages can be registered by calling one of the error(IValidatable
 * ...) overloads.
 * <p>
 * By default this class will skip validation if the
 * {@link IValidatable#getValue()} returns null, validators that wish to
 * validate the null value need to override {@link #validateOnNullValue()} and
 * return <code>true</code>.
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
	 * Whether or not to validate the value if it is null. It is usually
	 * desirable to skip validation if the value is null - unless we want to
	 * make sure the value is in fact null which is a rare usecase. Validators
	 * that extend this and wish to validate that the value is null should
	 * override this method and return <code>true</code>.
	 * 
	 * @return true to validate on null value, false to skip validation on null
	 *         value
	 */
	public boolean validateOnNullValue()
	{
		return false;
	}

	/**
	 * Method used to validate the validatable instance
	 * 
	 * @param validatable
	 */
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
	 * Reports an error against validatable using the map returned by
	 * {@link #variablesMap(IValidatable)}for variable interpolations and
	 * message key returned by {@link #resourceKey()}.
	 * 
	 * @param validatable
	 *            validatble being validated
	 * 
	 */
	public void error(final IValidatable<T> validatable)
	{
		error(validatable, resourceKey(), variablesMap(validatable));
	}

	/**
	 * Reports an error against the validatalbe using the given map for variable
	 * interpolations and message resource key provided by
	 * {@link #resourceKey()}
	 * 
	 * @param validatable
	 *            validatble being validated
	 * @param vars
	 *            variables for variable interpolation
	 */
	public void error(final IValidatable<T> validatable, final Map<String, Object> vars)
	{
		if (vars == null)
		{
			throw new IllegalArgumentException("Argument [[vars]] cannot be null");
		}
		error(validatable, resourceKey(), vars);
	}

	/**
	 * Reports an error against the validatable using the specified resource key
	 * and variable map
	 * 
	 * @param validatable
	 *            validatble being validated
	 * @param resourceKey
	 *            The message resource key to use
	 * @param vars
	 *            The model for variable interpolation
	 */
	public void error(final IValidatable<T> validatable, final String resourceKey,
			Map<String, Object> vars)
	{
		if (validatable == null)
		{
			throw new IllegalArgumentException("Argument [[validatable]] cannot be null");
		}
		if (vars == null)
		{
			throw new IllegalArgumentException("Argument [[vars]] cannot be null");
		}
		if (resourceKey == null)
		{
			throw new IllegalArgumentException("Argument [[resourceKey]] cannot be null");
		}


		ValidationError error = new ValidationError().addMessageKey(resourceKey);
		final String defaultKey = Classes.simpleName(getClass());
		if (!resourceKey.equals(defaultKey))
		{
			error.addMessageKey(defaultKey);
		}

		error.setVars(vars);
		validatable.error(error);
	}

	/**
	 * Gets the resource key for validator's error message from the
	 * ApplicationSettings class.
	 * 
	 * <strong>NOTE</strong>: THIS METHOD SHOULD NEVER RETURN NULL
	 * 
	 * @return the resource key for the validator
	 */
	protected String resourceKey()
	{
		return Classes.simpleName(getClass());
	}

	/**
	 * Gets the default variable map
	 * 
	 * <strong>NOTE</strong>: THIS METHOD SHOULD NEVER RETURN NULL
	 * 
	 * @param validatable
	 *            validatable being validated
	 * 
	 * @return a map with the variables for interpolation
	 */
	protected Map<String, Object> variablesMap(IValidatable<T> validatable)
	{
		final Map<String, Object> resourceModel = new HashMap<String, Object>(1);
		return resourceModel;
	}

}
