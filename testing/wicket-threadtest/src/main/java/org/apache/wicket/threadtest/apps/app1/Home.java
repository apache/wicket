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
package org.apache.wicket.threadtest.apps.app1;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.AttributeModifier;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Check;
import org.apache.wicket.markup.html.form.CheckBox;
import org.apache.wicket.markup.html.form.CheckGroup;
import org.apache.wicket.markup.html.form.ChoiceRenderer;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.ImageButton;
import org.apache.wicket.markup.html.form.ListMultipleChoice;
import org.apache.wicket.markup.html.form.Radio;
import org.apache.wicket.markup.html.form.RadioChoice;
import org.apache.wicket.markup.html.form.RadioGroup;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.model.AbstractReadOnlyModel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.threadtest.apps.app1.FormInputModel.Line;
import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.MaskConverter;
import org.apache.wicket.validation.validator.NumberValidator;
import org.apache.wicket.version.undo.Change;

/**
 */
public class Home extends WebPage<Void>
{

	private class ActionPanel extends Panel<Contact>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @param id
		 *            component id
		 * @param model
		 *            model for contact
		 */
		public ActionPanel(String id, IModel<Contact> model)
		{
			super(id, model);
			add(new Link<Void>("select")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					selected = ActionPanel.this.getModelObject();
				}
			});
		}
	}

	/**
	 * Form for collecting input.
	 */
	private class InputForm extends Form<FormInputModel>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param name
		 *            Component name
		 */
		public InputForm(String name)
		{
			super(name, new CompoundPropertyModel<FormInputModel>(new FormInputModel()));
			add(new LocaleDropDownChoice("localeSelect"));
			add(new Link<Void>("defaultLocaleLink")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					WebRequest request = (WebRequest)getRequest();
					setLocale(request.getLocale());
				}
			});
			TextField<String> stringTextField = new TextField<String>("stringProperty");
			stringTextField.setLabel(new Model<String>("String"));
			add(stringTextField);
			TextField<Integer> integerTextField = new TextField<Integer>("integerProperty",
				Integer.class);
			add(integerTextField.add(NumberValidator.POSITIVE));
			add(new TextField<Double>("doubleProperty", Double.class));
			WebMarkupContainer<?> dateLabel = new WebMarkupContainer<Void>("dateLabel");
			add(dateLabel);
			TextField<Date> datePropertyTextField = new TextField<Date>("dateProperty", Date.class);
			add(datePropertyTextField);
			add(new TextField<Integer>("integerInRangeProperty", Integer.class).add(NumberValidator.range(
				0, 100)));
			add(new CheckBox("booleanProperty"));
			RadioChoice<String> rc = new RadioChoice<String>("numberRadioChoice", NUMBERS).setSuffix("");
			rc.setLabel(new Model<String>("number"));
			add(rc);

			RadioGroup<String> group = new RadioGroup<String>("numbersGroup");
			add(group);
			ListView<String> numbers = new ListView<String>("numbers", NUMBERS)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<String> item)
				{
					item.add(new Radio<String>("radio", item.getModel()));
					item.add(new Label<String>("number", item.getModelObjectAsString()));
				};
			};
			group.add(numbers);

			CheckGroup<String> checks = new CheckGroup<String>("numbersCheckGroup");
			add(checks);
			ListView<String> checksList = new ListView<String>("numbers", NUMBERS)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void populateItem(ListItem<String> item)
				{
					item.add(new Check<String>("check", item.getModel()));
					item.add(new Label<String>("number", item.getModelObjectAsString()));
				};
			};
			checks.add(checksList);

			add(new ListMultipleChoice<String>("siteSelection", SITES));

			add(new TextField<URL>("urlProperty", URL.class)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public IConverter<URL> getConverter(Class<URL> clazz)
				{
					return new IConverter<URL>()
					{
						private static final long serialVersionUID = 1L;

						/**
						 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,
						 *      java.util.Locale)
						 */
						public URL convertToObject(String value, Locale locale)
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

						/**
						 * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object,
						 *      java.util.Locale)
						 */
						public String convertToString(URL value, Locale locale)
						{
							return value != null ? value.toString() : null;
						}
					};
				}
			});

			add(new TextField<UsPhoneNumber>("phoneNumberUS", UsPhoneNumber.class)
			{
				private static final long serialVersionUID = 1L;

				@Override
				public IConverter<UsPhoneNumber> getConverter(Class<UsPhoneNumber> clazz)
				{
					return new MaskConverter<UsPhoneNumber>("(###) ###-####", UsPhoneNumber.class);
				}
			});

			add(new LinesListView("lines"));

			add(new ImageButton<Void>("saveButton"));

			add(new Link<Void>("resetButtonLink")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					InputForm.this.modelChanged();
				}
			}.add(new Image<Void>("resetButtonImage")));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.Form#onSubmit()
		 */
		@Override
		public void onSubmit()
		{
			info("Saved model " + getModelObject());
		}
	}

	/** list view to be nested in the form. */
	private static final class LinesListView extends ListView<Line>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public LinesListView(String id)
		{
			super(id);
			setReuseItems(true);
		}

		@Override
		protected void populateItem(ListItem<Line> item)
		{
			item.add(new TextField<String>("lineEdit", new PropertyModel<String>(item.getModel(),
				"text")));
		}
	}

	/**
	 * Choice for a locale.
	 */
	private final class LocaleChoiceRenderer extends ChoiceRenderer<Locale>
	{
		private static final long serialVersionUID = 1L;

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
		public Object getDisplayValue(Locale object)
		{
			Locale locale = object;
			String display = locale.getDisplayName(getLocale());
			return display;
		}
	}

	/**
	 * Dropdown with Locales.
	 */
	private final class LocaleDropDownChoice extends DropDownChoice<Locale>
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public LocaleDropDownChoice(String id)
		{
			super(id, LOCALES, new LocaleChoiceRenderer());
			setModel(new PropertyModel<Locale>(Home.this, "locale"));
		}

		/**
		 * @see org.apache.wicket.markup.html.form.DropDownChoice#onSelectionChanged(java.lang.Object)
		 */
		@Override
		public void onSelectionChanged(Locale newSelection)
		{
		}

		/**
		 * @see org.apache.wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
		 */
		@Override
		protected boolean wantOnSelectionChangedNotifications()
		{
			return true;
		}
	}

	/** Relevant locales wrapped in a list. */
	private static final List<Locale> LOCALES = Arrays.asList(new Locale[] { Locale.ENGLISH,
			new Locale("nl"), Locale.GERMAN, Locale.SIMPLIFIED_CHINESE, Locale.JAPANESE,
			new Locale("pt", "BR"), new Locale("fa", "IR"), new Locale("da", "DK") });

	/** available sites for the multiple select. */
	private static final List<String> SITES = Arrays.asList(new String[] { "The Server Side",
			"Java Lobby", "Java.Net" });

	/** available numbers for the radio selection. */
	static final List<String> NUMBERS = Arrays.asList(new String[] { "1", "2", "3" });

	private Contact selected;

	/**
	 * Construct.
	 */
	public Home()
	{

		add(new Link<Void>("link")
		{
			private static final long serialVersionUID = 1L;
			int i = 0;

			@Override
			public void onClick()
			{
				i++;
				addStateChange(new Change()
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void undo()
					{
					}
				});
			}
		});

		add(new Label<Contact>("selectedLabel", new PropertyModel<Contact>(this,
			"selectedContactLabel")));

		add(new DataView<Contact>("simple", new ContactDataProvider())
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(final Item<Contact> item)
			{
				Contact contact = item.getModelObject();
				item.add(new ActionPanel("actions", item.getModel()));
				item.add(new Label<String>("contactid", String.valueOf(contact.getId())));
				item.add(new Label<String>("firstname", contact.getFirstName()));
				item.add(new Label<String>("lastname", contact.getLastName()));
				item.add(new Label<String>("homephone", contact.getHomePhone()));
				item.add(new Label<String>("cellphone", contact.getCellPhone()));

				item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel<String>()
				{
					private static final long serialVersionUID = 1L;

					@Override
					public String getObject()
					{
						return (item.getIndex() % 2 == 1) ? "even" : "odd";
					}
				}));
			}
		});

		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		add(new InputForm("inputForm"));
	}

	/**
	 * @return string representation of selceted contact property
	 */
	public String getSelectedContactLabel()
	{
		if (selected == null)
		{
			return "No Contact Selected";
		}
		else
		{
			return selected.getFirstName() + " " + selected.getLastName();
		}
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