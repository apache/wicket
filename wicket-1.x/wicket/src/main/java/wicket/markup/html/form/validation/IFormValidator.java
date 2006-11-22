/*
 * $Id$
 * $Revision$
 * $Date$
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

import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;

/**
 * Interface that represents validators that check multiple components. These
 * validators are added to the form and only executed if all form components
 * returned by {@link IFormValidator#getDependentFormComponents()} have been
 * successfully validated before this validator runs.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public interface IFormValidator extends Serializable
{
	/**
	 * @return array of {@link FormComponent}s that this validator depends on
	 */
	FormComponent[] getDependentFormComponents();

	/**
	 * This method is ran if all components returned by
	 * {@link IFormValidator#getDependentFormComponents()} are valid.
	 * 
	 * @param form
	 *            form this validator is added to
	 */
	void validate(Form form);
}