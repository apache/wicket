/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.markup.html.form;

import java.io.Serializable;

import wicket.markup.html.form.validation.RequiredValidator;

/**
 * A text field which automatically adds a RequiredValidator. This is mainly for
 * convenience, since you can always add(new RequiredValidator()) manually.
 * 
 * @author Jonathan Locke
 */
public class RequiredTextField extends TextField
{
	/** Serial Version ID. */
	private static final long serialVersionUID = -2913294206388017417L;

	/**
	 * @see wicket.Component#Component(String)
	 */
	public RequiredTextField(final String name)
	{
		super(name);
		add(RequiredValidator.getInstance());
	}

	/**
	 * @see TextField#TextField(String, Class)
	 */
	public RequiredTextField(final String name, final Class type)
	{
		super(name, type);
		add(RequiredValidator.getInstance());
	}

	/**
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public RequiredTextField(final String name, final Serializable object)
	{
		super(name, object);
		add(RequiredValidator.getInstance());
	}

	/**
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public RequiredTextField(String name, Serializable object, String expression)
	{
		super(name, object, expression);
		add(RequiredValidator.getInstance());
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param type
	 *            The type to use when updating the model for this text field
	 * @see wicket.Component#Component(String, Serializable)
	 */
	public RequiredTextField(String name, Serializable object, Class type)
	{
		super(name, object, type);
		add(RequiredValidator.getInstance());
	}

	/**
	 * @param name
	 *            See Component constructor
	 * @param object
	 *            See Component constructor
	 * @param expression
	 *            See Component constructor
	 * @param type
	 *            The type to use when updating the model for this text field
	 * @see wicket.Component#Component(String, Serializable, String)
	 */
	public RequiredTextField(String name, Serializable object, String expression, Class type)
	{
		super(name, object, expression, type);
		add(RequiredValidator.getInstance());
	}
}