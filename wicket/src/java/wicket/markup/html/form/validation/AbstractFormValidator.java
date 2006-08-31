/*
 * $Id: AbstractFormValidator.java 5771 2006-05-19 12:04:06 +0000 (Fri, 19 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-19 12:04:06 +0000 (Fri, 19
 * May 2006) $
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
package wicket.markup.html.form.validation;

import java.io.Serializable;
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
	protected Map<String, Serializable> messageModel()
	{
		FormComponent[] formComponents = getDependentFormComponents();

		if (formComponents != null && formComponents.length > 0)
		{
			Map<String, Serializable> args = new HashMap<String, Serializable>(
					formComponents.length * 3);
			for (int i = 0; i < formComponents.length; i++)
			{
				final FormComponent formComponent = formComponents[i];

				String arg = "label" + i;
				IModel label = formComponent.getLabel();
				if (label != null)
				{
					args.put(arg, (Serializable)label.getObject());
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
			return new HashMap<String, Serializable>(2);
		}
	}

	/**
	 * Gets the resource key for validator's error message from the
	 * ApplicationSettings class.
	 * 
	 * @return the resource key based on the form component
	 */
	protected String resourceKey()
	{
		return Classes.simpleName(getClass());
	}
}