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
package wicket.markup.html.form.validation;

import java.util.HashMap;
import java.util.Map;

import wicket.markup.html.form.FormComponent;
import wicket.model.IModel;
import wicket.util.lang.Classes;

/**
 * Base class for {@link wicket.markup.html.form.validation.IFormValidator}s.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractFormValidator implements IFormValidator
{
	/**
	 * Gets the default variables for interpolation. These are for every
	 * component:
	 * <ul>
	 * <li>${input(n)}: the user's input</li>
	 * <li>${name}: the name of the component</li>
	 * <li>${label(n)}: the label of the component - either comes from
	 * FormComponent.labelModel or resource key [form-id].[form-component-id] in
	 * that order</li>
	 * </ul>
	 * 
	 * @return a map with the variables for interpolation
	 */
	protected Map messageModel()
	{
		FormComponent[] formComponents = getDependentFormComponents();

		if (formComponents != null && formComponents.length > 0)
		{
			Map args = new HashMap(formComponents.length * 3);
			for (int i = 0; i < formComponents.length; i++)
			{
				final FormComponent formComponent = formComponents[i];

				String arg = "label" + i;
				IModel label = formComponent.getLabel();
				if (label != null)
				{
					args.put(arg, label.getObject(formComponent));
				}
				else
				{
					args.put(arg, formComponent.getLocalizer().getString(formComponent.getId(),
							formComponent.getParent(), formComponent.getId()));
				}

				args.put("input" + i, formComponent.getInput());
				args.put("name" + i, formComponent.getId());
			}
			return args;
		}
		else
		{
			return new HashMap(2);
		}
	}

	/**
	 * Gets the resource key for validator's error message from the
	 * ApplicationSettings class.
	 * 
	 * @param components
	 *            form components being validated
	 * 
	 * @return the resource key based on the form component
	 */
	protected String resourceKey(FormComponent[] components)
	{
		return Classes.simpleName(getClass());
	}
}