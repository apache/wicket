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
package org.apache.wicket.core.util.string.interpolator;

import org.apache.wicket.Application;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.Session;
import org.apache.wicket.core.util.lang.PropertyResolver;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.string.interpolator.VariableInterpolator;

/**
 * Interpolates values into <code>String</code>s that are produced by interpreting property
 * expressions against a beans model.
 * <p>
 * The <code>interpolate(String string, Object model)</code> method takes a string such as "
 * <code>My name is ${name}</code>" and a beans model such as a <code>Person</code>, and reflects on
 * the object using any property expressions found inside <code>${}</code> markers in the
 * <code>String</code>. In this case, if the <code>Person</code> model has a <code>getName()</code>
 * method. The results of calling that method would be substituted for <code>${name}</code>. If
 * <code>getName()</code> returned <code>"Jonathan"</code>, then <code>interpolate()</code> would
 * return <code>"My name is Jonathan"</code>.
 * <p>
 * "$" is the escape char. Thus "$${text}" can be used to escape it (ignore interpretation). If
 * '$3.24' is needed then '$$${amount}' should be used. The first $ sign escapes the second, and the
 * third is used to interpolate the variable.
 *
 * @author Jonathan Locke
 * @since 1.2.6
 */
public final class PropertyVariableInterpolator extends VariableInterpolator
	implements
		IConverterLocator
{
	private static final long serialVersionUID = 1L;

	/** The model to introspect on */
	private final Object model;

	/**
	 * Private constructor to force use of static interpolate method.
	 *
	 * @param string
	 *            a <code>String</code> to interpolate into
	 * @param model
	 *            the model to apply property expressions to
	 */
	private PropertyVariableInterpolator(final String string, final Object model)
	{
		super(string);
		this.model = model;
	}

	/**
	 * Interpolates the given <code>String</code>, substituting values for property expressions.
	 *
	 * @param string
	 *            a <code>String</code> containing property expressions like <code>${xyz}</code>
	 * @param object
	 *            the <code>Object</code> to reflect on
	 * @return the interpolated <code>String</code>
	 */
	public static String interpolate(final String string, final Object object)
	{
		// If there's any reason to go to the expense of property expressions
		if (string.contains("${"))
		{
			// Do property expression interpolation
			return new PropertyVariableInterpolator(string, object).toString();
		}

		// Return simple string
		return string;
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
		Object value = PropertyResolver.getValue(variableName, model);

		if (value != null)
		{
			final IConverter converter = getConverter(value.getClass());
			if (converter != null)
			{
				return converter.convertToString(value, Session.get().getLocale());
			}
			else
			{
				return value.toString();
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <C> IConverter<C> getConverter(Class<C> type)
	{
		if (Application.exists())
		{
			return Application.get().getConverterLocator().getConverter(type);
		}
		return null;
	}
}