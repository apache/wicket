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
package org.apache.wicket.examples.forminput;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.examples.WicketExamplePage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.MaskConverter;
import org.apache.wicket.validation.validator.NumberValidator;


/**
 * Example for form input.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class FormInput extends WicketExamplePage<Void>
{
	/**
	 * Form for collecting input.
	 */
	private class InputForm extends Form<FormInputModel>
	{
		/**
		 * Construct.
		 * 
		 * @param name
		 *            Component name
		 */
		public InputForm(String name)
		{
			super(name, new CompoundPropertyModel<FormInputModel>(new FormInputModel()));

			// Dropdown for selecting locale
			add(new LocaleDropDownChoice("localeSelect"));

			// Link to return to default locale
			add(new Link("defaultLocaleLink")
			{
				@Override
				public void onClick()
				{
					WebRequest request = (WebRequest)getRequest();
					setLocale(request.getLocale());
				}
			});

			RequiredTextField<String> stringTextField = new RequiredTextField<String>(
				"stringProperty");
			stringTextField.setLabel(new Model<String>("String"));
			add(stringTextField);
			RequiredTextField<Integer> integerTextField = new RequiredTextField<Integer>(
				"integerProperty");
			add(integerTextField.add(NumberValidator.POSITIVE));
			add(new RequiredTextField<Double>("doubleProperty"));

			add(new RequiredTextField<Integer>("integerInRangeProperty").add(NumberValidator.range(
				0, 100)));
			add(new CheckBox("booleanProperty"));
			add(new Multiply("multiply"));
			// display the multiply result
			Label multiplyLabel = new Label("multiplyLabel", new PropertyModel(getModel(),
				"multiply"));
			// just for fun, add a border so that our result will be displayed
			// as '[ x ]'
			multiplyLabel.setComponentBorder(new BeforeAndAfterBorder());
			add(multiplyLabel);
			RadioChoice rc = new RadioChoice("numberRadioChoice", NUMBERS).setSuffix("");
			rc.setLabel(new Model("number"));
			rc.setRequired(true);
			add(rc);

			RadioGroup group = new RadioGroup("numbersGroup");
			add(group);
			ListView persons = new ListView("numbers", NUMBERS)
			{
				@Override
				protected void populateItem(ListItem item)
				{
					item.add(new Radio("radio", item.getModel()));
					item.add(new Label("number", item.getModelObjectAsString()));
				};
			}.setReuseItems(true);
			group.add(persons);

			CheckGroup checks = new CheckGroup("numbersCheckGroup");
			add(checks);
			ListView checksList = new ListView("numbers", NUMBERS)
			{
				@Override
				protected void populateItem(ListItem item)
				{
					item.add(new Check("check", item.getModel()));
					item.add(new Label("number", item.getModelObjectAsString()));
				};
			}.setReuseItems(true);
			checks.add(checksList);

			add(new ListMultipleChoice("siteSelection", SITES));

			// TextField using a custom converter.
			add(new TextField("urlProperty", URL.class)
			{
				@Override
				public IConverter getConverter(final Class type)
				{
					return new IConverter()
					{
						public Object convertToObject(String value, Locale locale)
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

						public String convertToString(Object value, Locale locale)
						{
							return value != null ? value.toString() : null;
						}
					};
				}
			});

			// TextField using a mask converter
			add(new TextField("phoneNumberUS", UsPhoneNumber.class)
			{
				@Override
				public IConverter getConverter(final Class/* <?> */type)
				{
					// US telephone number mask
					return new MaskConverter("(###) ###-####", UsPhoneNumber.class);
				}
			});

			// and this is to show we can nest ListViews in Forms too
			add(new LinesListView("lines"));

			add(new Button("saveButton"));

			add(new Button("resetButton")
			{
				@Override
				public void onSubmit()
				{
					// just set a new instance of the page
					setResponsePage(FormInput.class);
				}
			}.setDefaultFormProcessing(false));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		public void onSubmit()
		{
			// Form validation successful. Display message showing edited model.
			info("Saved model " + getModelObject());
		}
	}

	/** list view to be nested in the form. */
	private static final class LinesListView extends ListView<String>
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

		@Override
		protected void populateItem(ListItem<String> item)
		{
			// add a text field that works on each list item model (returns
			// objects of
			// type FormInputModel.Line) using property text.
			item.add(new TextField<String>("lineEdit", new PropertyModel<String>(item.getModel(),
				"text")));
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
		 * @see org.apache.wicket.markup.html.form.IChoiceRenderer#getDisplayValue(Object)
		 */
		@Override
		public Object getDisplayValue(Locale locale)
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
		 * @param id
		 *            component id
		 */
		public LocaleDropDownChoice(String id)
		{
			super(id, FormInputApplication.LOCALES, new LocaleChoiceRenderer());

			// set the model that gets the current locale, and that is used for
			// updating the current locale to property 'locale' of FormInput
			setModel(new PropertyModel(FormInput.this, "locale"));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.DropDownChoice#onSelectionChanged(java.lang.Object)
		 */
		@Override
		public void onSelectionChanged(Locale newSelection)
		{
			// note that we don't have to do anything here, as our property
			// model allready calls FormInput.setLocale when the model is
			// updated

			// force re-render by setting the page to render to the bookmarkable
			// instance, so that the page will be rendered from scratch,
			// re-evaluating the input patterns etc
			setResponsePage(FormInput.class);
		}

		/**
		 * @see org.apache.wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
		 */
		@Override
		protected boolean wantOnSelectionChangedNotifications()
		{
			// we want roundtrips when a the user selects another item
			return true;
		}
	}

	/** available sites for the multiple select. */
	private static final List SITES = Arrays.asList(new String[] { "The Server Side", "Java Lobby",
			"Java.Net" });

	/** available numbers for the radio selection. */
	static final List NUMBERS = Arrays.asList(new String[] { "1", "2", "3" });

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
	 * Sets locale for the user's session (getLocale() is inherited from Component)
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
