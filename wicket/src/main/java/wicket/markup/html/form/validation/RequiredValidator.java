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

import wicket.markup.html.form.FormComponent;
import wicket.util.string.Strings;

/**
 * This validator has been deprecated in favor of
 * {@link FormComponent#setRequired(boolean)}
 * 
 * Validator that ensures a component has a non-null and non-empty value. If the
 * component's value is null or empty (a value is considered empty if it just
 * contains whitespace) when its containing form is submitted then the
 * errorMessage() method will be called by the framework.
 * 
 * @author Jonathan Locke
 * @deprecated
 */
public class RequiredValidator extends AbstractValidator
{
	private static final long serialVersionUID = 1L;

	/** Singleton instance */
	private static final RequiredValidator instance = new RequiredValidator();

	/**
	 * @return Instance of required validator
	 */
	public static RequiredValidator getInstance()
	{
		return instance;
	}

	/**
	 * Protectected constructor to force use of static singleton accessor
	 * method. Or override it to implement resourceKey(Component)
	 */
	protected RequiredValidator()
	{
	}

	/**
	 * @see wicket.markup.html.form.validation.IValidator#validate(wicket.markup.html.form.FormComponent)
	 */
	public void validate(FormComponent component)
	{
		onValidate(component, component.getInput());
	}
	
	/**
	 * Validates whether the input value is not-null or empty. Validation is
	 * only executed when the component is enabled. If the value is null and the
	 * form component is not input nullable ({@link FormComponent#isInputNullable()},
	 * that is interpreted as the component being disabled too. If that is the
	 * case, validation will not be executed.
	 * 
	 * @param formComponent The form component that is checked 
	 * @param value The input value of the formcomponent (not converted value)
	 * 
	 * @see wicket.markup.html.form.validation.StringValidator#onValidate(wicket.markup.html.form.FormComponent,
	 *      java.lang.String)
	 */
	public final void onValidate(final FormComponent formComponent, final String value)
	{
		// Check value only if form component is enabled
		if (!formComponent.isEnabled())
		{
			// do not perform validation
			return;
		}

		// when null, check whether this is natural for that component, or
		// whether - as is the case with text fields - this can only happen
		// when the component was disabled
		if (value == null && (!formComponent.isInputNullable()))
		{
			// this value must have come from a disabled field
			// do not perform validation
			return;
		}

		// peform validation by looking whether the value is null or empty
		if (Strings.isEmpty(value))
		{
			error(formComponent);
		}
	}

	/**
	 * @see Object#toString()
	 */
	public String toString()
	{
		return "[RequiredValidator]";
	}
}
