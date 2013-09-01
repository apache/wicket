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
package org.apache.wicket.markup.html.form.validation;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.model.IModel;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.validation.ValidationError;


/**
 * Base class for {@link org.apache.wicket.markup.html.form.validation.IFormValidator}s.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractFormValidator extends Behavior implements IFormValidator
{
	private static final long serialVersionUID = 1L;

	/**
	 * Can be bound to {@link Form}s only.
	 * 
	 * @throws WicketRuntimeException
	 *             if component is not a form
	 */
	@Override
	public void bind(Component component)
	{
		if (!(component instanceof Form))
		{
			throw new WicketRuntimeException("Behavior " + getClass().getName()
				+ " can only be added to an instance of a Form");
		}
	}

	/**
	 * Reports an error against validatable using the map returned by {@link #variablesMap()}for
	 * variable interpolations and message key returned by {@link #resourceKey()}.
	 * 
	 * @param fc
	 *            form component against which the error is reported
	 * 
	 */
	public void error(FormComponent<?> fc)
	{
		error(fc, resourceKey(), variablesMap());
	}

	/**
	 * Reports an error against the validatable using the given resource key
	 * 
	 * @param fc
	 *            form component against which the error is reported
	 * @param resourceKey
	 *            The message resource key to use
	 */
	public void error(FormComponent<?> fc, final String resourceKey)
	{
		if (resourceKey == null)
		{
			throw new IllegalArgumentException("Argument [[resourceKey]] cannot be null");
		}
		error(fc, resourceKey, variablesMap());
	}

	/**
	 * Reports an error against the validatable using the given map for variable interpolations and
	 * message resource key provided by {@link #resourceKey()}
	 * 
	 * @param fc
	 *            form component against which the error is reported
	 * @param vars
	 *            variables for variable interpolation
	 */
	public void error(FormComponent<?> fc, final Map<String, Object> vars)
	{
		if (vars == null)
		{
			throw new IllegalArgumentException("Argument [[vars]] cannot be null");
		}
		error(fc, resourceKey(), vars);
	}

	/**
	 * Reports an error against the validatable using the specified resource key and variable map
	 * 
	 * @param fc
	 *            form component against which the error is reported
	 * @param resourceKey
	 *            The message resource key to use
	 * @param vars
	 *            The model for variable interpolation
	 */
	public void error(FormComponent<?> fc, final String resourceKey, Map<String, Object> vars)
	{
		if (fc == null)
		{
			throw new IllegalArgumentException("Argument [[fc]] cannot be null");
		}
		if (vars == null)
		{
			throw new IllegalArgumentException("Argument [[vars]] cannot be null");
		}
		if (resourceKey == null)
		{
			throw new IllegalArgumentException("Argument [[resourceKey]] cannot be null");
		}


		ValidationError error = new ValidationError().addKey(resourceKey);
		final String defaultKey = Classes.simpleName(getClass());
		if (!resourceKey.equals(defaultKey))
		{
			error.addKey(defaultKey);
		}

		error.setVariables(vars);
		fc.error(error);
	}

	/**
	 * Gets the default variables for interpolation. These are for every component:
	 * <ul>
	 * <li>${input(n)}: the user's input</li>
	 * <li>${name(n)}: the name of the component</li>
	 * <li>${label(n)}: the label of the component - either comes from FormComponent.labelModel or
	 * resource key [form-id].[form-component-id] in that order</li>
	 * </ul>
	 * 
	 * @return a map with the variables for interpolation
	 */
	protected Map<String, Object> variablesMap()
	{
		FormComponent<?>[] formComponents = getDependentFormComponents();

		if (formComponents != null && formComponents.length > 0)
		{
			Map<String, Object> args = new HashMap<String, Object>(formComponents.length * 3);
			for (int i = 0; i < formComponents.length; i++)
			{
				final FormComponent<?> formComponent = formComponents[i];

				String arg = "label" + i;
				IModel<?> label = formComponent.getLabel();
				if (label != null)
				{
					args.put(arg, label.getObject());
				}
				else
				{
					args.put(
						arg,
						formComponent.getLocalizer().getString(formComponent.getId(),
							formComponent.getParent(), formComponent.getId()));
				}

				args.put("input" + i, formComponent.getInput());
				args.put("name" + i, formComponent.getId());
			}
			return args;
		}
		else
		{
			return new HashMap<String, Object>(2);
		}
	}

	/**
	 * Gets the resource key for validator's error message from the ApplicationSettings class.
	 * 
	 * @return the resource key based on the form component
	 */
	protected String resourceKey()
	{
		return Classes.simpleName(getClass());
	}
}