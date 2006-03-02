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
package wicket.markup.html.form.validation;

import java.util.Collections;

import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.util.lang.Classes;
import wicket.util.lang.Objects;

/**
 * Validates that the input of two form components is identical. Errors are
 * reported on the second form component with key equal to simple class name of
 * this validator
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class EqualInputValidator extends AbstractFormValidator
{
	private final FormComponent[] comps;


	/**
	 * Construct.
	 * 
	 * @param fc1
	 * @param fc2
	 */
	public EqualInputValidator(FormComponent fc1, FormComponent fc2)
	{
		if (fc1 == null)
		{
			throw new IllegalArgumentException("fc1 cannot be null");
		}
		if (fc2 == null)
		{
			throw new IllegalArgumentException("fc2 cannot be null");
		}
		comps = new FormComponent[] { fc1, fc2 };
	}

	/**
	 * @see wicket.markup.html.form.validation.IFormValidator#getDependentFormComponents()
	 */
	public FormComponent[] getDependentFormComponents()
	{
		return comps;
	}

	/**
	 * @see wicket.markup.html.form.validation.IFormValidator#validate(wicket.markup.html.form.Form)
	 */
	public void validate(Form form)
	{
		// we have a choice to validate the type converted values or the raw
		// input values, we validate the raw input

		final FormComponent fc0 = comps[0];
		final FormComponent fc1 = comps[1];

		if (!Objects.equal(fc0.getInput(), fc1.getInput()))
		{
			final String key=Classes.simpleName(getClass());
			fc1.error(Collections.singletonList(key), messageModel());
		}
	}

}
