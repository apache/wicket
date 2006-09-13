/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
package wicket.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A versatile implementation of {@link IValidationError} that supports message
 * resolution from {@link IMessageSource}, default message (if none of the keys
 * matched), and variable substitution.
 * 
 * The final error message is constructed via the following process:
 * <ol>
 * <li>Try all keys added by calls to {@link #addMessageKey(String)} via
 * provided {@link IMessageSource}</li>
 * <li>If none of the keys yielded a message, use the message set by
 * {@link #setMessage(String)} if any</li>
 * <li>Perform variable subsitution on the message if any</li>
 * </ol>
 * 
 * @author ivaynberg
 */
public final class ValidationError implements IValidationError
{
	// XXX 2.0: optimization - keys can be null by default until a key is added
	/** List of message keys to try against the {@link IMessageSource} */
	private List<String> keys = new ArrayList<String>(1);

	/** Variable map to use in variable substitution */
	private Map<String, Object> vars;

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
	 * {@link IMessageSource} to locate the error message string
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
	public ValidationError setVar(String name, Object value)
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

		getVars().put(name, value);

		return this;
	}

	/**
	 * Returns the map of variables for this error. User is free to modify the
	 * contents.
	 * 
	 * @return map of variables for this error
	 */
	public Map<String, Object> getVars()
	{
		if (vars == null)
		{
			vars = new HashMap<String, Object>(2);
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
	public ValidationError setVars(Map<String, Object> vars)
	{
		if (vars == null)
		{
			throw new IllegalArgumentException("Argument [[vars]] cannot be null");
		}
		this.vars = vars;
		return this;
	}

	/**
	 * @see wicket.validation.IValidationError#getErrorMessage(wicket.validation.IMessageSource)
	 */
	@SuppressWarnings("unchecked")
	public String getErrorMessage(IMessageSource messageSource)
	{
		String errorMessage = null;

		// try any message keys ...
		for (String key : keys)
		{
			errorMessage = messageSource.getMessage(key);
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
			final Map<String, Object> p = (vars == null) ? Collections.EMPTY_MAP : vars;
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
	public String getMessage()
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
	public ValidationError setMessage(String message)
	{
		if (message == null)
		{
			throw new IllegalArgumentException("Argument [[defaultMessage]] cannot be null");
		}
		this.message = message;
		return this;
	}

	@Override
	public String toString()
	{
		// FIXME 2.0: ivaynberg: implement this - specifically show resource
		// keys
		return super.toString();
	}

}
