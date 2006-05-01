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

import wicket.examples.wizard.framework.TransitionLabel;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.panel.Panel;
import wicket.model.CompoundPropertyModel;

/**
 * Step 4, Review, of the hotel preferences wizard.
 * 
 * @author Eelco Hillenius
 */
public class ReviewStep extends AbstractHotelPrefStep
{
	/**
	 * Construct.
	 * 
	 * @param model
	 *            the model
	 */
	public ReviewStep(HotelPreferencesModel model)
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

		return null;
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
		if (preferences.getRoom() == null)
		{
			valid = false;
			form.error("you must select a room");
		}
		return valid;
	}

	/**
	 * @see wicket.examples.wizard.framework.Step#previous(wicket.markup.html.form.Form)
	 */
	public TransitionLabel previous(Form form)
	{
		return TransitionLabel.PREVIOUS;
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
			super(id, new CompoundPropertyModel(ReviewStep.this.getModel()));

			add(new CheckBox("wantsWakeUpCall")
			{
				protected boolean wantOnSelectionChangedNotifications()
				{
					return true;
				}
			});

			// we want to show the wake up call input fields (time) only when
			// the customer
			// wants to get a wake up call
			WebMarkupContainer wakeUpCallContainer = new WebMarkupContainer("wakeUpCallContainer")
			{
				public boolean isVisible()
				{
					return getPreferences().getWantsWakeUpCall();
				}
			};
			// note that required is only checked when the field is visible
			wakeUpCallContainer.add(new TextField("wakeUpCallHours", Integer.class));
			wakeUpCallContainer.add(new TextField("wakeUpCallMinutes", Integer.class));
			add(wakeUpCallContainer);

			add(new CheckBox("wantsBreakFast"));
		}
	}
}
