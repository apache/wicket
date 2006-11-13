/*
 * $Id: FormInput.java 6427 2006-07-08 09:17:31 +0000 (Sat, 08 Jul 2006)
 * jdonnerstag $ $Revision$ $Date: 2006-07-08 09:17:31 +0000 (Sat, 08
 * Jul 2006) $
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

import wicket.MarkupContainer;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.basic.Label;
import wicket.markup.html.form.Button;
import wicket.markup.html.form.Check;
import wicket.markup.html.form.CheckBox;
import wicket.markup.html.form.CheckGroup;
import wicket.markup.html.form.ChoiceRenderer;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.ListMultipleChoice;
import wicket.markup.html.form.Radio;
import wicket.markup.html.form.RadioChoice;
import wicket.markup.html.form.RadioGroup;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.form.TextField;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;
import wicket.model.Model;
import wicket.model.PropertyModel;
import wicket.model.ResourceModel;
import wicket.protocol.http.WebRequest;
import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;
import wicket.util.convert.MaskConverter;
import wicket.util.convert.SimpleConverterAdapter;
import wicket.validation.validator.NumberValidator;

/**
 * Example for form input.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class FormInput extends WicketExamplePage
{
	/**
	 * Form for collecting input.
	 */
	private class InputForm extends Form<FormInputModel>
	{
		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param name
		 *            Component name
		 */
		public InputForm(MarkupContainer parent, String name)
		{
			super(parent, name, new CompoundPropertyModel<FormInputModel>(new FormInputModel()));

			// Dropdown for selecting locale
			new LocaleDropDownChoice(this, "localeSelect");

			// Link to return to default locale
			new Link(this, "defaultLocaleLink")
			{
				@Override
				public void onClick()
				{
					WebRequest request = (WebRequest)getRequest();
					setLocale(request.getLocale());
				}
			};

			RequiredTextField stringTextField = new RequiredTextField<String>(this,
					"stringProperty");
			stringTextField.setLabel(new Model<String>("String"));
			RequiredTextField integerTextField = new RequiredTextField<Integer>(this,
					"integerProperty", Integer.class);
			integerTextField.add(NumberValidator.POSITIVE);
			new RequiredTextField<Double>(this, "doubleProperty", Double.class);
			new RequiredTextField<Integer>(this, "integerInRangeProperty", Integer.class)
					.add(NumberValidator.range(0, 100));
			new CheckBox(this, "booleanProperty");
			RadioChoice<String> rc = new RadioChoice<String>(this, "numberRadioChoice", NUMBERS)
					.setSuffix("");
			rc.setLabel(new Model<String>("number"));
			rc.setRequired(true);

			RadioGroup group = new RadioGroup(this, "numbersGroup");
			new ListView<String>(group, "numbers", NUMBERS)
			{
				@Override
				protected void populateItem(ListItem<String> item)
				{
					new Radio<String>(item, "radio", item.getModel());
					new Label(item, "number", item.getModelObject());
				};
			};

			CheckGroup checks = new CheckGroup(this, "numbersCheckGroup");
			new ListView<String>(checks, "numbers", NUMBERS)
			{
				@Override
				protected void populateItem(ListItem<String> item)
				{
					new Check<String>(item, "check", item.getModel());
					new Label(item, "number", item.getModelObject());
				};
			};

			new ListMultipleChoice<String>(this, "siteSelection", SITES);

			// TextField using a custom converter.
			new TextField<URL>(this, "urlProperty", URL.class)
			{
				/**
				 * @see wicket.Component#getConverter(java.lang.Class)
				 */
				@Override
				public IConverter getConverter(Class type)
				{
					return new SimpleConverterAdapter()
					{
						@Override
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

						@Override
						public String toString(Object value)
						{
							return value != null ? value.toString() : null;
						}
					};
				}
			};

			// TextField using a mask converter
			new TextField<UsPhoneNumber>(this, "phoneNumberUS", UsPhoneNumber.class)
			{
				/**
				 * @see wicket.Component#getConverter(java.lang.Class)
				 */
				@Override
				public IConverter getConverter(Class type)
				{
					// US telephone number mask
					return new MaskConverter("(###) ###-####", UsPhoneNumber.class);
				}
			};

			// and this is to show we can nest ListViews in Forms too
			new LinesListView(this, "lines");

			new Button<String>(this, "saveButton", new ResourceModel("save"))
			{
				@Override
				public void onSubmit()
				{
					// need to do nothing for this example here
				}
			};

			new Button<String>(this, "resetButton", new ResourceModel("reset"))
			{
				@Override
				public void onSubmit()
				{
					// trigger reload
					InputForm.this.modelChanged();
				}
			}.setDefaultFormProcessing(false);
			// set default form processing so that only this button's
			// submit method will be called
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		public void onSubmit()
		{
			// Form validation successful. Display message showing edited model.
			info("Saved model " + getModelObject());
		}
	}

	/** list view to be nested in the form. */
	private static final class LinesListView extends ListView
	{

		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 */
		public LinesListView(MarkupContainer parent, String id)
		{
			super(parent, id);
			// always do this in forms!
			setReuseItems(true);
		}

		@Override
		protected void populateItem(ListItem item)
		{
			// add a text field that works on each list item model (returns
			// objects of
			// type FormInputModel.Line) using property text.
			new TextField<String>(item, "lineEdit", new PropertyModel<String>(item.getModel(),
					"text"));
		}
	}

	/**
	 * Choice for a locale.
	 */
	private final class LocaleChoiceRenderer extends ChoiceRenderer<Locale>
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
		@Override
		public String getDisplayValue(Locale locale)
		{
			String display = locale.getDisplayName(getLocale());
			return display;
		}
	}

	/**
	 * Dropdown with Locales.
	 */
	private final class LocaleDropDownChoice extends DropDownChoice<Locale>
	{
		/**
		 * Construct.
		 * 
		 * @param parent
		 * @param id
		 *            component id
		 */
		public LocaleDropDownChoice(MarkupContainer parent, String id)
		{
			super(parent, id, LOCALES, new LocaleChoiceRenderer());

			// set the model that gets the current locale, and that is used for
			// updating the current locale to property 'locale' of FormInput
			setModel(new PropertyModel<Locale>(FormInput.this, "locale"));
		}

		/**
		 * @see wicket.markup.html.form.DropDownChoice#onSelectionChanged(java.lang.Object)
		 */
		@Override
		public void onSelectionChanged(Object newSelection)
		{
			// note that we don't have to do anything here, as our property
			// model allready calls FormInput.setLocale when the model is
			// updated
			// setLocale((Locale)newSelection); // so we don't need to do this
		}

		/**
		 * @see wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
		 */
		@Override
		protected boolean wantOnSelectionChangedNotifications()
		{
			// we want roundtrips when a the user selects another item
			return true;
		}
	}

	/** available numbers for the radio selection. */
	static final List<String> NUMBERS = Arrays.asList(new String[] { "1", "2", "3" });

	/** Relevant locales wrapped in a list. */
	private static final List<Locale> LOCALES = Arrays.asList(new Locale[] { Locale.ENGLISH,
			new Locale("nl"), Locale.GERMAN, Locale.SIMPLIFIED_CHINESE, Locale.JAPANESE,
			new Locale("pt", "BR"), new Locale("fa", "IR"), new Locale("da", "DK"),
			new Locale("th") });

	/** available sites for the multiple select. */
	private static final List<String> SITES = Arrays.asList(new String[] { "The Server Side",
			"Java Lobby", "Java.Net" });

	/**
	 * Constructor
	 */
	public FormInput()
	{
		// Construct form and feedback panel and hook them up
		new FeedbackPanel(this, "feedback");
		new InputForm(this, "inputForm");
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
}