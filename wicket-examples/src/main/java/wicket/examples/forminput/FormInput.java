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
package wicket.examples.forminput;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Check;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.CheckGroup;
import wicket.markup.html.form.ChoiceRenderer;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.ImageButton;
import wicket.markup.html.form.ListMultipleChoice;
import wicket.markup.html.form.Radio;
import wicket.markup.html.form.RadioChoice;
import wicket.markup.html.form.RadioGroup;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.NumberValidator;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;
import wicket.model.PropertyModel;
import wicket.protocol.http.WebRequest;
import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;
import wicket.util.convert.MaskConverter;
import wicket.util.convert.SimpleConverterAdapter;

/**
 * Example for form input.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class FormInput extends WicketExamplePage
{
	/** Relevant locales wrapped in a list. */
	private static final List LOCALES = Arrays.asList(new Locale[] { Locale.ENGLISH,
			new Locale("nl"), Locale.GERMAN, Locale.SIMPLIFIED_CHINESE, Locale.JAPANESE,
			new Locale("pt", "BR"), new Locale("fa", "IR"), new Locale("da", "DK")});

	/** available numbers for the radio selection. */
	static final List NUMBERS = Arrays.asList(new String[] { "1", "2", "3" });

	/** available sites for the multiple select. */
	private static final List SITES = Arrays.asList(new String[] { "The Server Side", "Java Lobby",
			"Java.Net" });

	/**
	 * Constructor
	 */
	public FormInput()
	{
		// Construct form and feedback panel and hook them up
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		add(new InputForm("inputForm"));
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
		if (locale != null)
		{
			getSession().setLocale(locale);
		}
	}

	/**
	 * Form for collecting input.
	 */
	private class InputForm extends Form
	{
		/**
		 * Construct.
		 * 
		 * @param name
		 *            Component name
		 */
		public InputForm(String name)
		{
			super(name, new CompoundPropertyModel(new FormInputModel()));

			// Dropdown for selecting locale
			add(new LocaleDropDownChoice("localeSelect"));

			// Link to return to default locale
			add(new Link("defaultLocaleLink")
			{
				public void onClick()
				{
					WebRequest request = (WebRequest)getRequest();
					setLocale(request.getLocale());
				}
			});

			RequiredTextField stringTextField = new RequiredTextField("stringProperty");
			stringTextField.setLabel(new Model("String"));
			add(stringTextField);
			RequiredTextField integerTextField = new RequiredTextField("integerProperty",
					Integer.class);
			add(integerTextField.add(NumberValidator.POSITIVE));
			add(new RequiredTextField("doubleProperty", Double.class));
			// we have a component attached to the label here, as we want to
			// synchronize the id's of the label, textfield and datepicker. Note 
			// that you can perfectly do without labels

			add(new RequiredTextField("integerInRangeProperty", Integer.class).add(NumberValidator
					.range(0, 100)));
			add(new CheckBox("booleanProperty"));
			RadioChoice rc = new RadioChoice("numberRadioChoice", NUMBERS).setSuffix("");
			rc.setLabel(new Model("number"));
			rc.setRequired(true);
			add(rc);

			RadioGroup group = new RadioGroup("numbersGroup");
			add(group);
			ListView persons = new ListView("numbers", NUMBERS)
			{
				protected void populateItem(ListItem item)
				{
					item.add(new Radio("radio", item.getModel()));
					item.add(new Label("number", item.getModelObjectAsString()));
				};
			};
			group.add(persons);

			CheckGroup checks = new CheckGroup("numbersCheckGroup");
			add(checks);
			ListView checksList = new ListView("numbers", NUMBERS)
			{
				protected void populateItem(ListItem item)
				{
					item.add(new Check("check", item.getModel()));
					item.add(new Label("number", item.getModelObjectAsString()));
				};
			};
			checks.add(checksList);

			add(new ListMultipleChoice("siteSelection", SITES));

			// TextField using a custom converter.
			add(new TextField("urlProperty", URL.class)
			{
				public IConverter getConverter()
				{
					return new SimpleConverterAdapter()
					{
						public String toString(Object value)
						{
							return value != null ? value.toString() : null;
						}

						public Object toObject(String value)
						{
							try
							{
								return new URL(value.toString());
							}
							catch (MalformedURLException e)
							{
								throw new ConversionException("'" + value + "' is not a valid URL");
							}
						}
					};
				}
			});

			// TextField using a mask converter
			add(new TextField("phoneNumberUS", UsPhoneNumber.class)
			{
				public IConverter getConverter()
				{
					// US telephone number mask
					return new MaskConverter("(###) ###-####", UsPhoneNumber.class);
				}
			});

			// and this is to show we can nest ListViews in Forms too
			add(new LinesListView("lines"));

			add(new ImageButton("saveButton"));

			add(new Link("resetButtonLink")
			{
				public void onClick()
				{
					// just call modelChanged so that any invalid input is
					// cleared.
					InputForm.this.modelChanged();
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

	/**
	 * Dropdown with Locales.
	 */
	private final class LocaleDropDownChoice extends DropDownChoice
	{
		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public LocaleDropDownChoice(String id)
		{
			super(id, LOCALES, new LocaleChoiceRenderer());

			// set the model that gets the current locale, and that is used for
			// updating the current locale to property 'locale' of FormInput
			setModel(new PropertyModel(FormInput.this, "locale"));
		}

		/**
		 * @see wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
		 */
		protected boolean wantOnSelectionChangedNotifications()
		{
			// we want roundtrips when a the user selects another item
			return true;
		}

		/**
		 * @see wicket.markup.html.form.DropDownChoice#onSelectionChanged(java.lang.Object)
		 */
		public void onSelectionChanged(Object newSelection)
		{
			// note that we don't have to do anything here, as our property
			// model allready calls FormInput.setLocale when the model is
			// updated
			// setLocale((Locale)newSelection); // so we don't need to do this
		}
	}

	/**
	 * Choice for a locale.
	 */
	private final class LocaleChoiceRenderer extends ChoiceRenderer
	{
		/**
		 * Constructor.
		 */
		public LocaleChoiceRenderer()
		{
		}

		/**
		 * @see wicket.markup.html.form.IChoiceRenderer#getDisplayValue(Object)
		 */
		public Object getDisplayValue(Object object)
		{
			Locale locale = (Locale)object;
			String display = locale.getDisplayName(getLocale());
			return display;
		}
	}

	/** list view to be nested in the form. */
	private static final class LinesListView extends ListView
	{

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public LinesListView(String id)
		{
			super(id);
			// always do this in forms!
			setReuseItems(true);
		}

		protected void populateItem(ListItem item)
		{
			// add a text field that works on each list item model (returns
			// objects of
			// type FormInputModel.Line) using property text.
			item.add(new TextField("lineEdit", new PropertyModel(item.getModel(), "text")));
		}
	}
}