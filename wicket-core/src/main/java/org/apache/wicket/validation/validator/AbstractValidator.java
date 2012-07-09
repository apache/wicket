/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.validation.validator;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.validation.INullAcceptingValidator;
import org.apache.wicket.validation.IValidatable;
import org.apache.wicket.validation.IValidator;
import org.apache.wicket.validation.ValidationError;


/**
 * Convenience base class for {@link IValidator}s. This class is thread-safe and therefore it is
 * safe to share validators across sessions/threads.
 * <p>
 * Error messages can be registered by calling one of the error (<code>IValidatable</code>)
 * overloads. By default this class will skip validation if the {@link IValidatable#getValue()}
 * returns <code>null</code>. Validators that wish to validate the <code>null</code> value need to
 * override {@link #validateOnNullValue()} and return <code>true</code>.
 * 
 * FIXME 7.0 remove
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 * @param <T>
 *            type of validatable
 * @since 1.2.6
 * @deprecated with changes to {@link ValidationError} in 6.0 this class serves very little purpose.
 *             Validators should implement {@link IValidator} directly and extend {@link Behavior}
 *             where needed.
 * 
 * 
 */
@Deprecated
public abstract class AbstractValidator<T> extends Behavior
	implements
		INullAcceptingValidator<T>,
		IClusterable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Indicates whether or not to validate the value if it is <code>null</code>. It is usually
	 * desirable to skip validation if the value is <code>null</code>, unless we want to make sure
	 * the value is in fact <code>null</code> (a rare use case). Validators that extend this and
	 * wish to ensure the value is <code>null</code> should override this method and return
	 * <code>true</code>.
	 * 
	 * @return <code>true</code> to validate on <code>null</code> value, <code>false</code> to skip
	 *         validation on <code>null</code> value
	 */
	public boolean validateOnNullValue()
	{
		return false;
	}

	/**
	 * Validates the <code>IValidatable</code> instance.
	 * 
	 * @param validatable
	 *            the given <code>IValidatable</code> instance
	 */
	protected abstract void onValidate(IValidatable<T> validatable);

	/**
	 * @see IValidator#validate(IValidatable)
	 */
	@Override
	public final void validate(IValidatable<T> validatable)
	{
		if (validatable.getValue() != null || validateOnNullValue())
		{
			onValidate(validatable);
		}
	}

	/**
	 * Reports an error against an <code>IValidatable</code> instance using the <code>Map</code>
	 * returned by {@link #variablesMap(IValidatable)} for variable interpolations and the message
	 * resource key returned by {@link #resourceKey()}.
	 * 
	 * @param validatable
	 *            the <code>IValidatable</code> instance being validated
	 * 
	 */
	public void error(final IValidatable<T> validatable)
	{
		error(validatable, resourceKey(), variablesMap(validatable));
	}

	/**
	 * Reports an error against an <code>IValidatable</code> instance using the <code>Map</code>
	 * returned by {@link #variablesMap(IValidatable)} for variable interpolations and the given
	 * message resource key.
	 * 
	 * @param validatable
	 *            the <code>IValidatable</code> instance being validated
	 * @param resourceKey
	 *            the message resource key to use
	 * 
	 */
	public void error(final IValidatable<T> validatable, String resourceKey)
	{
		if (resourceKey == null)
		{
			throw new IllegalArgumentException("Argument [[resourceKey]] cannot be null");
		}
		error(validatable, resourceKey, variablesMap(validatable));
	}

	/**
	 * Reports an error against an <code>IValidatable</code> instance using the given
	 * <code>Map</code> for variable interpolations and message resource key provided by
	 * {@link #resourceKey()}.
	 * 
	 * @param validatable
	 *            the <code>IValidatable</code> instance being validated
	 * @param vars
	 *            <code>Map</code> of variables for variable interpolation
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
	 * Reports an error against an <code>IValidatable</code> instance using the given message
	 * resource key and <code>Map</code> for variable interpolations.
	 * 
	 * @param validatable
	 *            the <code>IValidatable</code> instance being validated
	 * @param resourceKey
	 *            the message resource key to use
	 * @param vars
	 *            <code>Map</code> of variables for variable interpolation
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
		final String defaultKey = getClass().getSimpleName();
		if (!resourceKey.equals(defaultKey))
		{
			error.addMessageKey(defaultKey);
		}

		error.setVariables(vars);
		validatable.error(error);
	}

	/**
	 * Gets the message resource key for this validator's error message from the
	 * <code>ApplicationSettings</code> class.
	 * 
	 * <strong>NOTE</strong>: THIS METHOD SHOULD NEVER RETURN <code>null</code>.
	 * 
	 * @return the message resource key for this validator
	 */
	protected String resourceKey()
	{
		return getClass().getSimpleName();
	}

	/**
	 * Gets the default <code>Map</code> of variables.
	 * 
	 * <strong>NOTE</strong>: THIS METHOD SHOULD NEVER RETURN <code>null</code>.
	 * 
	 * @param validatable
	 *            the <code>IValidatable</code> instance being validated
	 * 
	 * @return a <code>Map</code> of variables for variable interpolation
	 */
	protected Map<String, Object> variablesMap(IValidatable<T> validatable)
	{
		return new HashMap<String, Object>(1);
	}
}
