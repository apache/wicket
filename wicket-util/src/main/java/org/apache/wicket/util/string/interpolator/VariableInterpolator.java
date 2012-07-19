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
package org.apache.wicket.util.string.interpolator;

/**
 * Base class for variable interpolators. An interpolator substitutes values into a
 * <code>String</code>. So, a variable interpolator substitutes the values of one or more variables
 * into a <code>String</code>.
 * <p>
 * The <code>String</code> to interpolate (substitute into) is passed to the
 * <code>VariableInterpolator</code>'s constructor. Variables are denoted in this string by the
 * syntax <code>${variableName}</code>. A subclass provides an implementation for the abstract
 * method <code>getValue(String variableName)</code>. The <code>toString()</code> method then
 * performs an interpolation by replacing each variable of the form <code>${variableName}</code>
 * with the value returned by <code>getValue("variableName")</code>.
 * <p>
 * "$" is the escape char. Thus "$${text}" can be used to escape it (ignore interpretation). If
 * '$3.24' is needed then '$$${amount}' should be used. The first $ sign escapes the second, and the
 * third is used to interpolate the variable.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
public abstract class VariableInterpolator
{
	/** The <code>String</code> to interpolate into */
	protected final String string;

	private boolean exceptionOnNullVarValue = false;

	/**
	 * Constructor.
	 * 
	 * @param string
	 *            a <code>String</code> to interpolate with variable values
	 */
	public VariableInterpolator(final String string)
	{
		this.string = string;
	}

	/**
	 * Constructor.
	 * 
	 * @param string
	 *            a <code>String</code> to interpolate with variable values
	 * @param exceptionOnNullVarValue
	 *            if <code>true</code> an {@link IllegalStateException} will be thrown if
	 *            {@link #getValue(String)} returns <code>null</code>, otherwise the
	 *            <code>${varname}</code> string will be left in the <code>String</code> so that
	 *            multiple interpolators can be chained
	 */
	public VariableInterpolator(final String string, final boolean exceptionOnNullVarValue)
	{
		this.string = string;
		this.exceptionOnNullVarValue = exceptionOnNullVarValue;
	}

	/**
	 * Retrieves a value for a variable name during interpolation.
	 * 
	 * @param variableName
	 *            a variable name
	 * @return the value
	 */
	protected abstract String getValue(String variableName);

	private int lowerPositive(final int i1, final int i2)
	{
		if (i2 < 0)
		{
			return i1;
		}
		else if (i1 < 0)
		{
			return i2;
		}
		else
		{
			return i1 < i2 ? i1 : i2;
		}
	}

	/**
	 * Interpolates using variables.
	 * 
	 * @return the interpolated <code>String</code>
	 */
	@Override
	public String toString()
	{
		// If there's any reason to go to the expense of property expressions
		if (!string.contains("${"))
		{
			return string;
		}

		// Result buffer
		final StringBuilder buffer = new StringBuilder();

		// For each occurrences of "${"or "$$"
		int start;
		int pos = 0;

		while ((start = lowerPositive(string.indexOf("$$", pos), string.indexOf("${", pos))) != -1)
		{
			// Append text before possible variable
			buffer.append(string.substring(pos, start));

			if (string.charAt(start + 1) == '$')
			{
				buffer.append("$");
				pos = start + 2;
				continue;
			}


			// Position is now where we found the "${"
			pos = start;

			// Get start and end of variable name
			final int startVariableName = start + 2;
			final int endVariableName = string.indexOf('}', startVariableName);

			// Found a close brace?
			if (endVariableName != -1)
			{
				// Get variable name inside brackets
				final String variableName = string.substring(startVariableName, endVariableName);

				// Get value of variable
				final String value = getValue(variableName);

				// If there's no value
				if (value == null)
				{
					if (exceptionOnNullVarValue)
					{
						throw new IllegalArgumentException("Value of variable [[" + variableName +
							"]] could not be resolved while interpolating [[" + string + "]]");
					}
					else
					{
						// Leave variable uninterpolated, allowing multiple
						// interpolators to
						// do their work on the same string
						buffer.append("${").append(variableName).append("}");
					}
				}
				else
				{
					// Append variable value
					buffer.append(value);
				}

				// Move past variable
				pos = endVariableName + 1;
			}
			else
			{
				break;
			}
		}

		// Append anything that might be left
		if (pos < string.length())
		{
			buffer.append(string.substring(pos));
		}

		// Convert result to String
		return buffer.toString();
	}
}
