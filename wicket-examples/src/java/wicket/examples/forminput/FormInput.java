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
import java.util.List;
import java.util.Locale;

import wicket.IFeedback;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.ImageButton;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.form.validation.IntegerValidator;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;
import wicket.model.PropertyModel;

/**
 * Example for form input.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class FormInput extends WicketExamplePage
{
	/** all available locales wrapped in a list. */
	private static final List ALL_LOCALES = Arrays.asList(Locale.getAvailableLocales());

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
		add(new DropDownChoice("localeSelect", new PropertyModel(this, "locale"), ALL_LOCALES) 
		{
			protected boolean wantOnSelectionChangedNotifications()
			{
				return true;
			}

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

	/**
	 * Form for collecting input. 
	 */
	private static class InputForm extends Form
	{
		/**
		 * Construct.
		 * 
		 * @param name
		 *            Component name
		 * @param feedback
		 *            Feedback display for form
		 */
		public InputForm(String name, IFeedback feedback)
		{
			super(name, new CompoundPropertyModel(new FormInputModel()), feedback);

			add(new RequiredTextField("stringProperty"));
			add(new RequiredTextField("integerProperty", Integer.class));
			add(new RequiredTextField("doubleProperty", Double.class));
			add(new RequiredTextField("dateProperty", Date.class));
			add(new RequiredTextField("integerInRangeProperty", Integer.class)
					.add(IntegerValidator.range(0, 100)));
			add(new CheckBox("booleanProperty"));

			add(new ImageButton("saveButton"));

			add(new Link("resetButtonLink")
			{
				public void onClick()
				{
					InputForm.this.setModelObject(new FormInputModel());
				}
			}.add(new Image("resetButtonImage")));
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		public void onSubmit()
		{
			// Form validation successful. Display message showing edited model.
			info("Saved model " + getRootModel());
		}
	}
}