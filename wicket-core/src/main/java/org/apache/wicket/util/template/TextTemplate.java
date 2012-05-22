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
package org.apache.wicket.util.template;

import java.util.Map;

import org.apache.wicket.util.resource.AbstractStringResourceStream;
import org.apache.wicket.util.string.interpolator.MapVariableInterpolator;


/**
 * Represents a text template that can do variable interpolation.
 * 
 * @see org.apache.wicket.util.string.interpolator.VariableInterpolator
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * @since 1.2.6
 */
public abstract class TextTemplate extends AbstractStringResourceStream
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public TextTemplate()
	{
	}

	/**
	 * Constructor.
	 * 
	 * @param contentType
	 *            the mime type of this resource, such as "<code>image/jpeg</code>" or "
	 *            <code>text/html</code>"
	 */
	public TextTemplate(String contentType)
	{
		super(contentType);
	}

	/**
	 * Interpolates the <code>Map</code> of variables with the content and returns the resulting
	 * <code>String</code> without replacing the content. Variables are denoted in this string by
	 * the syntax <code>${variableName}</code>. The contents will be altered by replacing each
	 * variable of the form <code>${variableName}</code> with the value returned by
	 * <code>variables.getValue("variableName")</code>.
	 * 
	 * @param variables
	 *            the variables to interpolate
	 * @return the result of the interpolation
	 */
	public String asString(Map<String, ?> variables)
	{
		if (variables != null)
		{
			return new MapVariableInterpolator(getString(), variables).toString();
		}
		return getString();
	}

	/**
	 * @see org.apache.wicket.util.resource.AbstractStringResourceStream#asString()
	 */
	@Override
	public String asString()
	{
		return getString();
	}

	/**
	 * Retrieves the <code>String</code> resource.
	 * 
	 * @return the <code>String</code> resource
	 */
	@Override
	public abstract String getString();

	/**
	 * Interpolates values into this <code>TextTemplate</code>.
	 * 
	 * @param variables
	 *            variables to interpolate into this <code>TextTemplate</code>
	 * @return <code>this</code>, for chaining purposes
	 */
	public abstract TextTemplate interpolate(Map<String, ?> variables);
}
