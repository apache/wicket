/*
 * $Id$ $Revision$ $Date$
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
package wicket.util.string.interpolator;

import wicket.util.lang.PropertyResolver;

/**
 * Interpolates values into strings that are produced by interpreting property
 * expressions against a beans model.
 * <p>
 * The interpolate(String string, Object model) method takes a string such as
 * "My name is ${name}" and a beans model such as a Person, and reflects on the
 * object using any property expressions found inside ${} markers in the string.
 * In this case, if the Person model had a getName() method, the results of
 * calling that method would be substituted for ${name}. If getName() returned
 * Jonathan, interpolate() would then return "My name is Jonathan".
 * 
 * @author Jonathan Locke
 */
public final class PropertyVariableInterpolator extends VariableInterpolator
{
	/** The model to introspect on */
	private final Object model;

	/**
	 * Private constructor to force use of static interpolate method
	 * 
	 * @param string
	 *            The string to interpolate into
	 * @param model
	 *            The root model to apply property expressions to
	 */
	private PropertyVariableInterpolator(final String string, final Object model)
	{
		super(string);
		this.model = model;
	}

	/**
	 * Interpolates string, substituting values for property expressions
	 * 
	 * @param string
	 *            String containing property expressions like ${xyz}
	 * @param object
	 *            The object to reflect on
	 * @return The interpolated string
	 */
	public static String interpolate(final String string, final Object object)
	{
		// If there's any reason to go to the expense of property expressions
		if (string.indexOf("${") != -1)
		{
			// Do property expression interpolation
			return new PropertyVariableInterpolator(string, object).toString();
		}

		// Return simple string
		return string;
	}

	/**
	 * Gets a value for a variable name during interpolation
	 * 
	 * @param variableName
	 *            The variable
	 * @return The value
	 */
	@Override
	protected String getValue(final String variableName)
	{
		Object value = PropertyResolver.getValue(variableName, model);
		return (value != null) ? value.toString() : null;
	}
}
