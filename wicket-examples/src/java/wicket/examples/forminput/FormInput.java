/*
 * $Id$ $Revision:
 * 1.20 $ $Date$
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
package wicket.examples.forminput;

import java.util.Arrays;
import java.util.Date;
import java.util.Locale;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.ImageButton;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.markup.html.form.validation.IntegerValidator;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.markup.html.link.OnClickLink;
import wicket.markup.html.panel.FeedbackPanel;

/**
 * Example for form input.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class FormInput extends WicketExamplePage
{
	/**
	 * Constructor
	 */
	public FormInput()
	{
		// Construct form and feedback panel and hook them up
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		add(new InputForm("inputForm", feedback));

		// Dropdown for selecting locale
		add(new DropDownChoice("localeSelect", this, "locale", Arrays.asList(Locale
				.getAvailableLocales())) 
		{
			public void onSelectionChanged(Object newSelection)
			{
				setLocale((Locale)newSelection);
			}
		});

		// Link to return to default locale
		add(new Link("defaultLocaleLink")
		{
			public void onClick()
			{
				setLocale(Locale.getDefault());
			}
		});
	}

	/**
	 * Sets locale for the user's session (getLocale() is inherited from
	 * Component)
	 * 
	 * @param locale
	 *            The new locale
	 */
	public void setLocale(Locale locale)
	{
		getSession().setLocale(locale);
	}

	/** Form for input. */
	private static class InputForm extends Form
	{
		/**
		 * Construct.
		 * 
		 * @param name
		 *            Component name
		 * @param validationFeedback
		 *            Feedback display for form
		 */
		public InputForm(String name, IValidationFeedback validationFeedback)
		{
			super(name, new FormInputModel(), validationFeedback);

			add(new RequiredTextField("stringInput", getModel(), "stringProperty"));
			add(new RequiredTextField("integerInput", getModel(), "integerProperty", Integer.class));
			add(new RequiredTextField("doubleInput", getModel(), "doubleProperty", Double.class));
			add(new RequiredTextField("dateInput", getModel(), "dateProperty", Date.class));
			add(new RequiredTextField("integerInRangeInput", getModel(), "integerInRangeProperty")
					.add(IntegerValidator.range(0, 100)));

			add(new ImageButton("saveButton"));

			add(new OnClickLink("resetButtonLink")
			{
				public void onClick()
				{
					InputForm.this.getModel().setObject(new FormInputModel());
				}
			}.add(new Image("resetButtonImage")));
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		public void onSubmit()
		{
			// Form validation successful. Display message showing edited model.
			info("Saved model " + getModelObject());
		}
	}
}