/*
 * $Id: StringBufferResourceStream.java 3307 2005-11-30 15:57:34 -0800 (Wed, 30
 * Nov 2005) ivaynberg $ $Revision: 3307 $ $Date: 2005-11-30 15:57:34 -0800
 * (Wed, 30 Nov 2005) $
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
package wicket.util.resource;

import java.util.Map;

import wicket.util.string.interpolator.MapVariableInterpolator;

/**
 * Represents a text template that can do variable interpolation.
 * 
 * @author Eelco Hillenius
 */
public abstract class TextTemplate extends AbstractStringResourceStream
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public TextTemplate()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param contentType
	 *            The mime type of this resource, such as "image/jpeg" or
	 *            "text/html".
	 */
	public TextTemplate(String contentType)
	{
		super(contentType);
	}

	/**
	 * Interpolate the map of variables with the content and return the
	 * resulting string without replacing the content. Variables are denoted in
	 * this string by the syntax ${variableName}. The contents will be altered
	 * by replacing each variable of the form ${variableName} with the value
	 * returned by variables.getValue("variableName").
	 * 
	 * @param variables
	 *            The variables to interpolate
	 * @return the result of the interpolation
	 */
	public String asString(Map variables)
	{
		if (variables != null)
		{
			return new MapVariableInterpolator(getString(), variables).toString();
		}
		return getString();
	}

	/**
	 * @see wicket.util.resource.AbstractResourceStream#asString()
	 */
	@Override
	public String asString()
	{
		return getString();
	}

	/**
	 * Gets the string resource.
	 * 
	 * @return The string resource
	 */
	@Override
	public abstract String getString();
}
