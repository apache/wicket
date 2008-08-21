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

import org.apache.wicket.IClusterable;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.util.lang.Classes;
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
 * returns <code>null</code>. Validators that wish to validate the <code>null</code> value need
 * to override {@link #validateOnNullValue()} and return <code>true</code>.
 * 
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Igor Vaynberg (ivaynberg)
 * @since 1.2.6
 */
public abstract class AbstractValidator implements INullAcceptingValidator, IClusterable
{
	private static final long serialVersionUID = 1L;

	/**
	 * Indicates whether or not to validate the value if it is <code>null</code>. It is usually
	 * desirable to skip validation if the value is <code>null</code>, unless we want to make
	 * sure the value is in fact <code>null</code> (a rare use case). Validators that extend this
	 * and wish to ensure the value is <code>null</code> should override this method and return
	 * <code>true</code>.
	 * 
	 * @return <code>true</code> to validate on <code>null</code> value, <code>false</code> to
	 *         skip validation on <code>null</code> value
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
	protected abstract void onValidate(IValidatable validatable);

	/**
	 * @see IValidator#validate(IValidatable)
	 */
	public final void validate(IValidatable validatable)
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
	public void error(final IValidatable validatable)
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
	public void error(final IValidatable validatable, String resourceKey)
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
	public void error(final IValidatable validatable, final Map<String, Object> vars)
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
	public void error(final IValidatable validatable, final String resourceKey,
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
		return Classes.simpleName(getClass());
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
	protected Map<String, Object> variablesMap(IValidatable validatable)
	{
		final Map<String, Object> resourceModel = new HashMap<String, Object>(1);
		return resourceModel;
	}

	// deprecated methods

	/**
	 * DEPRECATED/UNSUPPORTED
	 * 
	 * Gets the default variables for interpolation. These are:
	 * <ul>
	 * <li>${input}: the user's input</li>
	 * <li>${name}: the name of the component</li>
	 * <li>${label}: the label of the <code>Component</code> - either comes from
	 * <code>FormComponent.labelModel</code> or resource key [form-id].[form-component-id] in that
	 * order</li>
	 * </ul>
	 * 
	 * @param formComponent
	 *            form component
	 * @return a map with the variables for interpolation
	 * 
	 * @deprecated use {@link #variablesMap(IValidatable)} instead
	 * @throws UnsupportedOperationException
	 * 
	 * 
	 * FIXME 2.0: remove asap
	 */
	@Deprecated
	protected final Map<String, Object> messageModel(final FormComponent formComponent)
	{
		throw new UnsupportedOperationException("THIS METHOD IS DEPRECATED, SEE JAVADOC");
	}

	/**
	 * DEPRECATED/UNSUPPORTED
	 * 
	 * Gets the resource key for validator's error message from the ApplicationSettings class.
	 * 
	 * @param formComponent
	 *            form component that is being validated
	 * 
	 * @return the resource key based on the form component
	 * 
	 * @deprecated use {@link #resourceKey()} instead
	 * @throws UnsupportedOperationException
	 * 
	 * 
	 * FIXME 2.0: remove asap
	 * 
	 */
	@Deprecated
	protected final String resourceKey(final FormComponent formComponent)
	{
		throw new UnsupportedOperationException("THIS METHOD IS DEPRECATED, SEE JAVADOC");
	}

	/**
	 * DEPRECATED/UNSUPPORTED
	 * 
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT IMPLEMENT IT.
	 * <p>
	 * Instead of subclassing IValidator, you should use one of the existing validators, which cover
	 * a huge number of cases, or if none satisfies your need, subclass AbstractValidator.
	 * <p>
	 * Validates the given input. The input corresponds to the input from the request for a
	 * component.
	 * 
	 * @param component
	 *            Component to validate
	 * 
	 * @deprecated use {@link #variablesMap(IValidatable)} instead
	 * @throws UnsupportedOperationException
	 * 
	 * 
	 * FIXME 2.0: remove asap
	 */
	@Deprecated
	public final void validate(final FormComponent component)
	{
		throw new UnsupportedOperationException("THIS METHOD IS DEPRECATED, SEE JAVADOC");
	}
}
