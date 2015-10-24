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

import java.util.Map;

import org.apache.wicket.util.string.Strings;


/**
 * Interpolates variables into a <code>String</code> from a <code>Map</code>.
 * 
 * @author Jonathan Locke
 * @since 1.2.6
 */
@SuppressWarnings("serial")
public class MapVariableInterpolator extends VariableInterpolator
{
	/** Map of variables */
	private Map<?, ?> variables;

	/**
	 * Constructor.
	 * 
	 * @param string
	 *            a <code>String</code> to interpolate into
	 * @param variables
	 *            the variables to substitute
	 */
	public MapVariableInterpolator(final String string, final Map<?, ?> variables)
	{
		super(string);
		this.variables = variables;
	}

	/**
	 * Constructor.
	 * 
	 * @param string
	 *            a <code>String</code> to interpolate into
	 * @param variables
	 *            the variables to substitute
	 * @param exceptionOnNullVarValue
	 *            if <code>true</code> an {@link IllegalStateException} will be thrown if
	 *            {@link #getValue(String)} returns <code>null</code>, otherwise the
	 *            <code>${varname}</code> string will be left in the <code>String</code> so that
	 *            multiple interpolators can be chained
	 */
	public MapVariableInterpolator(final String string, final Map<?, ?> variables,
		final boolean exceptionOnNullVarValue)
	{
		super(string, exceptionOnNullVarValue);
		this.variables = variables;
	}

	/**
	 * Sets the <code>Map</code> of variables.
	 * 
	 * @param variables
	 *            the <code>Map</code> of variables
	 */
	public final void setVariables(final Map<?, ?> variables)
	{
		this.variables = variables;
	}

	/**
	 * Retrieves a value for a variable name during interpolation.
	 * 
	 * @param variableName
	 *            the variable name
	 * @return the value
	 */
	@Override
	protected String getValue(final String variableName)
	{
		return Strings.toString(variables.get(variableName));
	}

	/**
	 * Interpolates a <code>String</code> with the arguments defined in the given <code>Map</code>.
	 * 
	 * @param string
	 *            a <code>String</code> to interpolate into
	 * @param variables
	 *            the variables to substitute
	 * @return the interpolated <code>String</code>
	 */
	public static String interpolate(final String string, final Map<?, ?> variables)
	{
		return new MapVariableInterpolator(string, variables).toString();
	}

}
