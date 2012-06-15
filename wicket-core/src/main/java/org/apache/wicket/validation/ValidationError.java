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
package org.apache.wicket.validation;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.markup.html.form.ValidationErrorFeedback;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * A versatile implementation of {@link IValidationError} that supports message resolution from
 * {@link IErrorMessageSource}, default message (if none of the keys matched), and variable
 * substitution.
 * 
 * The final error message is constructed via the following process:
 * <ol>
 * <li>Try all keys added by calls to {@link #addKey(String)} via the provided
 * <code>IErrorMessageSource</code>.</li>
 * <li>If none of the keys yielded a message, use the message set by {@link #setMessage(String)}, if
 * any.</li>
 * <li>Perform variable substitution on the message, if any.</li>
 * </ol>
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @since 1.2.6
 */
public final class ValidationError implements IValidationError
{
	private static final long serialVersionUID = 1L;

	private static final Map<String, Object> EMPTY_VARS = Collections.emptyMap();

	/** list of message keys to try against the <code>IErrorMessageSource</code> */
	private List<String> keys;

	/** variables map to use in variable substitution */
	private Map<String, Object> vars;

	/** default message used when all keys yield no message */
	private String message;

	/**
	 * Constructs an empty error
	 */
	public ValidationError()
	{

	}

	/**
	 * Constructs a validation error with the validator's standard key. Equivalent to calling
	 * {@link #addKey(IValidator)}
	 * 
	 * @param validator
	 *            validator
	 */
	public ValidationError(IValidator<?> validator)
	{
		addKey(validator);
	}

	/**
	 * Constructs a validation error with a variation of validator's standard key. Equivalent to
	 * calling {@link #addKey(IValidator, String)}
	 * 
	 * @param validator
	 *            validator
	 * @param variation
	 *            key variation
	 * 
	 * 
	 */
	public ValidationError(IValidator<?> validator, String variation)
	{
		addKey(validator, variation);
	}

	/**
	 * Constructs a validation error with the specified message. Equivalent to calling
	 * {@link #setMessage(String)}
	 * 
	 * @param message
	 *            message
	 */
	public ValidationError(String message)
	{
		setMessage(message);
	}

	/**
	 * Adds a key to the list of keys that will be tried against <code>IErrorMessageSource</code> to
	 * locate the error message string.
	 * 
	 * @deprecated use {@link #addKey(String)}
	 * 
	 * @param key
	 * @return this <code>ValidationError</code> for chaining purposes
	 */
	@Deprecated
	public ValidationError addMessageKey(String key)
	{
		return addKey(key);
	}

	/**
	 * Adds a key to the list of keys that will be tried against <code>IErrorMessageSource</code> to
	 * locate the error message string.
	 * 
	 * @param key
	 *            a message key to be added
	 * @return this <code>ValidationError</code> for chaining purposes
	 */
	public ValidationError addKey(String key)
	{
		Args.notEmpty(key, "key");

		if (keys == null)
		{
			keys = new ArrayList<String>(1);
		}
		keys.add(key);
		return this;
	}


	/**
	 * Shortcut for adding a standard message key which is the simple name of the validator' class
	 * 
	 * @param validator
	 *            validator
	 * @return {@code this}
	 */
	public ValidationError addKey(IValidator<?> validator)
	{
		Args.notNull(validator, "validator");
		addKey(validator.getClass().getSimpleName());
		return this;
	}

	/**
	 * Shortcut for adding a standard message key variation which is the simple name of the
	 * validator class followed by a dot and the {@literal variation}
	 * <p>
	 * If the variation is empty only the validator's simple class name is used
	 * </p>
	 * 
	 * @param validator
	 *            validator
	 * @param variation
	 *            key variation
	 * @return {@code this}
	 */
	public ValidationError addKey(IValidator<?> validator, String variation)
	{
		Args.notNull(validator, "validator");
		String key = validator.getClass().getSimpleName();
		if (!Strings.isEmpty(variation))
		{
			key = key + "." + variation.trim();
		}
		addKey(key);
		return this;
	}

	/**
	 * Sets a key and value in the variables map for use in substitution.
	 * 
	 * @param name
	 *            a variable name
	 * @param value
	 *            a variable value
	 * @return this <code>ValidationError</code> for chaining purposes
	 */
	public ValidationError setVariable(String name, Object value)
	{
		Args.notEmpty(name, "name");

		getVariables().put(name, value);

		return this;
	}

	/**
	 * Retrieves the variables map for this error. The caller is free to modify the contents.
	 * 
	 * @return a <code>Map</code> of variables for this error
	 */
	public final Map<String, Object> getVariables()
	{
		if (vars == null)
		{
			vars = new HashMap<String, Object>(2);
		}
		return vars;
	}

	/**
	 * Sets the variables map for this error.
	 * 
	 * @param vars
	 *            a variables map
	 * @return this <code>ValidationError</code> for chaining purposes
	 */
	public final ValidationError setVariables(Map<String, Object> vars)
	{
		Args.notNull(vars, "vars");

		this.vars = vars;
		return this;
	}

	/**
	 * @see IValidationError#getErrorMessage(IErrorMessageSource)
	 */
	@Override
	public final Serializable getErrorMessage(IErrorMessageSource messageSource)
	{

		final Map<String, Object> p = (vars != null) ? vars : EMPTY_VARS;

		String errorMessage = null;

		if (keys != null)
		{
			// try any message keys ...
			for (String key : keys)
			{
				errorMessage = messageSource.getMessage(key, vars);
				if (errorMessage != null)
				{
					break;
				}
			}
		}

		// ... if no keys matched try the default
		if (errorMessage == null && message != null)
		{
			errorMessage = message;
		}

		return new ValidationErrorFeedback(this, errorMessage);
	}

	/**
	 * Gets the default message that will be used when no message could be located via message keys.
	 * 
	 * @return message the default message used when all keys yield no message
	 */
	public final String getMessage()
	{
		return message;
	}

	/**
	 * Sets message that will be used when no message could be located via message keys.
	 * 
	 * @param message
	 *            a default message to be used when all keys yield no message
	 * 
	 * @return this <code>ValidationError</code> for chaining purposes
	 */
	public final ValidationError setMessage(String message)
	{
		Args.notNull(message, "message");

		this.message = message;
		return this;
	}


	/**
	 * Gets error keys
	 * 
	 * @return keys
	 */
	public List<String> getKeys()
	{
		if (keys == null)
		{
			keys = new ArrayList<String>();
		}
		return keys;
	}

	/**
	 * Sets error keys
	 * 
	 * @param keys
	 */
	public void setKeys(List<String> keys)
	{
		this.keys = keys;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder tostring = new StringBuilder();
		tostring.append("[").append(getClass().getSimpleName());

		tostring.append(" message=[").append(message);

		tostring.append("], keys=[");
		if (keys != null)
		{
			Iterator<String> i = keys.iterator();
			while (i.hasNext())
			{
				tostring.append(i.next());
				if (i.hasNext())
				{
					tostring.append(", ");
				}
			}
		}
		else
		{
			tostring.append("null");
		}
		tostring.append("], variables=[");

		if (vars != null)
		{
			Iterator<Entry<String, Object>> i = vars.entrySet().iterator();
			while (i.hasNext())
			{
				final Entry<String, Object> e = i.next();
				tostring.append("[")
					.append(e.getKey())
					.append("=")
					.append(e.getValue())
					.append("]");
				if (i.hasNext())
				{
					tostring.append(",");
				}
			}
		}
		else
		{
			tostring.append("null");
		}
		tostring.append("]");

		tostring.append("]");

		return tostring.toString();
	}

}
