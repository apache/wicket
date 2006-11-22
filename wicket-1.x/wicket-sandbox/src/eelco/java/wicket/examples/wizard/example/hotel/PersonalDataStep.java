/*
 * $Id$ $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.examples.wizard.example.hotel;

import java.util.Date;

import wicket.examples.wizard.framework.TransitionLabel;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.Panel;
import wicket.model.CompoundPropertyModel;
import wicket.util.string.Strings;

/**
 * Step 1, Personal Data, of the hotel preferences wizard.
 * 
 * @author Eelco Hillenius
 */
public class PersonalDataStep extends AbstractHotelPrefStep
{
	/** fields of this step. */
	// private BeanFields fields;
	/**
	 * Construct.
	 * 
	 * @param model
	 *            the model
	 */
	public PersonalDataStep(HotelPreferencesModel model)
	{
		super(model);
	}

	/**
	 * @see wicket.examples.wizard.framework.Step#newEditor(String)
	 */
	public Panel newEditor(String id)
	{
		return new Editor(id);
	}

	/**
	 * @see wicket.examples.wizard.framework.Step#next(wicket.markup.html.form.Form)
	 */
	public TransitionLabel next(Form form)
	{
		if (validateNext(form))
		{
			return TransitionLabel.NEXT;
		}

		return TransitionLabel.CURRENT;
	}

	/**
	 * Validates whether the current state is complete enough to go on.
	 * 
	 * @param form
	 *            message receiving component
	 * @return true when valid
	 */
	private boolean validateNext(Form form)
	{
		boolean valid = true;
		HotelPreferences preferences = getPreferences();
		if (Strings.isEmpty(preferences.getFirstName()))
		{
			valid = false;
			form.error("first name is a required field");
		}
		if (Strings.isEmpty(preferences.getLastName()))
		{
			valid = false;
			form.error("last name is a required field");
		}
		if (Strings.isEmpty(preferences.getPassportNumber()))
		{
			valid = false;
			form.error("passport number is a required field");
		}
		return valid;
	}

	/**
	 * Custom editor for this step.
	 */
	private final class Editor extends Panel
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public Editor(String id)
		{
			super(id, new CompoundPropertyModel(PersonalDataStep.this.getModel()));

			add(new TextField("firstName"));
			add(new TextField("lastName"));
			add(new TextField("passportNumber"));
			add(new TextField("dateOfBirth", Date.class));
		}
	}
}
