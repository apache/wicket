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

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.wicket.IClusterable;

/**
 * A versatile implementation of {@link IValidationError} that supports message
 * resolution from {@link IErrorMessageSource}, default message (if none of the
 * keys matched), and variable substitution.
 * 
 * The final error message is constructed via the following process:
 * <ol>
 * <li>Try all keys added by calls to {@link #addMessageKey(String)} via
 * provided {@link IErrorMessageSource}</li>
 * <li>If none of the keys yielded a message, use the message set by
 * {@link #setMessage(String)} if any</li>
 * <li>Perform variable subsitution on the message if any</li>
 * </ol>
 * 
 * @author ivaynberg
 */
public class ValidationError implements IValidationError, IClusterable
{
	private static final long serialVersionUID = 1L;

	// XXX 2.0: optimization - keys can be null by default until a key is added
	/** List of message keys to try against the {@link IErrorMessageSource} */
	private final List keys = new ArrayList(1);

	/** Variable map to use in variable substitution */
	private Map vars;

	/** Default message used when all keys yield no message */
	private String message;

	/**
	 * Constructor
	 */
	public ValidationError()
	{

	}

	/**
	 * Adds a key to the list of keys that will be tried against
	 * {@link IErrorMessageSource} to locate the error message string
	 * 
	 * @param key
	 * @return this for chaining
	 */
	public ValidationError addMessageKey(String key)
	{
		if (key == null || key.trim().length() == 0)
		{
			throw new IllegalArgumentException("Argument [[key]] cannot be null or an empty string");
		}
		keys.add(key);
		return this;
	}

	/**
	 * Sets a variable that will be used in substitution
	 * 
	 * @param name
	 *            variable name
	 * @param value
	 *            variable value
	 * @return this for chaining
	 */
	public ValidationError setVariable(String name, Object value)
	{
		if (name == null || name.trim().length() == 0)
		{
			throw new IllegalArgumentException(
					"Argument [[name]] cannot be null or an empty string");
		}
		if (value == null)
		{
			throw new IllegalArgumentException(
					"Argument [[value]] cannot be null or an empty string");
		}

		getVariables().put(name, value);

		return this;
	}

	/**
	 * Returns the map of variables for this error. User is free to modify the
	 * contents.
	 * 
	 * @return map of variables for this error
	 */
	public final Map getVariables()
	{
		if (vars == null)
		{
			vars = new HashMap(2);
		}
		return vars;
	}

	/**
	 * Sets the variable map for this error
	 * 
	 * @param vars
	 *            variable map
	 * @return this for chaining
	 */
	public final ValidationError setVariables(Map vars)
	{
		if (vars == null)
		{
			throw new IllegalArgumentException("Argument [[vars]] cannot be null");
		}
		this.vars = vars;
		return this;
	}

	/**
	 * @see org.apache.wicket.validation.IValidationError#getErrorMessage(org.apache.wicket.validation.IErrorMessageSource)
	 */
	public final String getErrorMessage(IErrorMessageSource messageSource)
	{
		String errorMessage = null;

		// try any message keys ...
		for (Iterator iterator = keys.iterator(); iterator.hasNext();)
		{
			errorMessage = messageSource.getMessage((String)iterator.next());
			if (errorMessage != null)
			{
				break;
			}
		}

		// ... if no keys matched try the default
		if (errorMessage == null && this.message != null)
		{
			errorMessage = this.message;
		}

		// if a message was found perform variable substitution
		if (errorMessage != null)
		{
			final Map p = (vars == null) ? Collections.EMPTY_MAP : vars;
			errorMessage = messageSource.substitute(errorMessage, p);
		}
		return errorMessage;
	}


	/**
	 * Gets message that will be used when no message could be located via
	 * message keys
	 * 
	 * @return message
	 */
	public final String getMessage()
	{
		return message;
	}

	/**
	 * Sets message that will be used when no message could be located via
	 * message keys
	 * 
	 * @param message
	 *            the message
	 * 
	 * @return this for chaining
	 */
	public final ValidationError setMessage(String message)
	{
		if (message == null)
		{
			throw new IllegalArgumentException("Argument [[defaultMessage]] cannot be null");
		}
		this.message = message;
		return this;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer tostring = new StringBuffer();
		tostring.append("[").append(getClass().getName());

		tostring.append(" message=[").append(message);

		tostring.append("], keys=[");
		if (keys != null)
		{
			Iterator i = keys.iterator();
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
			Iterator i = vars.entrySet().iterator();
			while (i.hasNext())
			{
				final Map.Entry e = (Entry)i.next();
				tostring.append("[").append(e.getKey()).append("=").append(e.getValue())
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
