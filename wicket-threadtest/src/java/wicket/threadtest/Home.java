/*
 * $Id: HelloWorld.java 5394 2006-04-16 15:36:52 +0200 (Sun, 16 Apr 2006)
 * jdonnerstag $ $Revision: 5394 $ $Date: 2006-04-16 15:36:52 +0200 (Sun, 16 Apr
 * 2006) $
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
package wicket.threadtest;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import wicket.AttributeModifier;
import wicket.Component;
import wicket.extensions.markup.html.datepicker.DatePicker;
import wicket.extensions.markup.html.repeater.data.DataView;
import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
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
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.NumberValidator;
import wicket.markup.html.image.Image;
import wicket.markup.html.link.Link;
import wicket.markup.html.list.ListItem;
import wicket.markup.html.list.ListView;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.markup.html.panel.Panel;
import wicket.model.AbstractReadOnlyModel;
import wicket.model.CompoundPropertyModel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;
import wicket.protocol.http.WebRequest;
import wicket.util.convert.ConversionException;
import wicket.util.convert.IConverter;
import wicket.util.convert.MaskConverter;
import wicket.util.convert.SimpleConverterAdapter;

public class Home extends WebPage {

	private class ActionPanel extends Panel {
		/**
		 * @param id
		 *            component id
		 * @param model
		 *            model for contact
		 */
		public ActionPanel(String id, IModel model) {
			super(id, model);
			add(new Link("select") {
				public void onClick() {
					Home.this.selected = (Contact) getParent().getModelObject();
				}
			});
		}
	}

	/**
	 * Form for collecting input.
	 */
	private class InputForm extends Form {
		/**
		 * Construct.
		 * 
		 * @param name
		 *            Component name
		 */
		public InputForm(String name) {
			super(name, new CompoundPropertyModel(new FormInputModel()));
			add(new LocaleDropDownChoice("localeSelect"));
			add(new Link("defaultLocaleLink") {
				public void onClick() {
					WebRequest request = (WebRequest) getRequest();
					setLocale(request.getLocale());
				}
			});
			TextField stringTextField = new TextField("stringProperty");
			stringTextField.setLabel(new Model("String"));
			add(stringTextField);
			TextField integerTextField = new TextField("integerProperty", Integer.class);
			add(integerTextField.add(NumberValidator.POSITIVE));
			add(new TextField("doubleProperty", Double.class));
			WebMarkupContainer dateLabel = new WebMarkupContainer("dateLabel");
			add(dateLabel);
			TextField datePropertyTextField = new TextField("dateProperty", Date.class);
			add(datePropertyTextField);
			add(new DatePicker("datePicker", dateLabel, datePropertyTextField));
			add(new TextField("integerInRangeProperty", Integer.class).add(NumberValidator.range(0, 100)));
			add(new CheckBox("booleanProperty"));
			RadioChoice rc = new RadioChoice("numberRadioChoice", NUMBERS).setSuffix("");
			rc.setLabel(new Model("number"));
			add(rc);

			RadioGroup group = new RadioGroup("numbersGroup");
			add(group);
			ListView persons = new ListView("numbers", NUMBERS) {
				protected void populateItem(ListItem item) {
					item.add(new Radio("radio", item.getModel()));
					item.add(new Label("number", item.getModelObjectAsString()));
				};
			};
			group.add(persons);

			CheckGroup checks = new CheckGroup("numbersCheckGroup");
			add(checks);
			ListView checksList = new ListView("numbers", NUMBERS) {
				protected void populateItem(ListItem item) {
					item.add(new Check("check", item.getModel()));
					item.add(new Label("number", item.getModelObjectAsString()));
				};
			};
			checks.add(checksList);

			add(new ListMultipleChoice("siteSelection", SITES));

			add(new TextField("urlProperty", URL.class) {
				public IConverter getConverter() {
					return new SimpleConverterAdapter() {
						public Object toObject(String value) {
							try {
								return new URL(value.toString());
							} catch (MalformedURLException e) {
								throw new ConversionException("'" + value + "' is not a valid URL");
							}
						}

						public String toString(Object value) {
							return value != null ? value.toString() : null;
						}
					};
				}
			});

			add(new TextField("phoneNumberUS", UsPhoneNumber.class) {
				public IConverter getConverter() {
					return new MaskConverter("(###) ###-####", UsPhoneNumber.class);
				}
			});

			add(new LinesListView("lines"));

			add(new ImageButton("saveButton"));

			add(new Link("resetButtonLink") {
				public void onClick() {
					InputForm.this.modelChanged();
				}
			}.add(new Image("resetButtonImage")));
		}

		/**
		 * @see wicket.markup.html.form.Form#onSubmit()
		 */
		public void onSubmit() {
			info("Saved model " + getModelObject());
		}
	}

	/** list view to be nested in the form. */
	private static final class LinesListView extends ListView {

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public LinesListView(String id) {
			super(id);
			setReuseItems(true);
		}

		protected void populateItem(ListItem item) {
			item.add(new TextField("lineEdit", new PropertyModel(item.getModel(), "text")));
		}
	}

	/**
	 * Choice for a locale.
	 */
	private final class LocaleChoiceRenderer extends ChoiceRenderer {
		/**
		 * Constructor.
		 */
		public LocaleChoiceRenderer() {
		}

		/**
		 * @see wicket.markup.html.form.IChoiceRenderer#getDisplayValue(Object)
		 */
		public Object getDisplayValue(Object object) {
			Locale locale = (Locale) object;
			String display = locale.getDisplayName(getLocale());
			return display;
		}
	}

	/**
	 * Dropdown with Locales.
	 */
	private final class LocaleDropDownChoice extends DropDownChoice {
		/**
		 * Construct.
		 * 
		 * @param id
		 *            component id
		 */
		public LocaleDropDownChoice(String id) {
			super(id, LOCALES, new LocaleChoiceRenderer());
			setModel(new PropertyModel(Home.this, "locale"));
		}

		/**
		 * @see wicket.markup.html.form.DropDownChoice#onSelectionChanged(java.lang.Object)
		 */
		public void onSelectionChanged(Object newSelection) {
		}

		/**
		 * @see wicket.markup.html.form.DropDownChoice#wantOnSelectionChangedNotifications()
		 */
		protected boolean wantOnSelectionChangedNotifications() {
			return true;
		}
	}

	/** available numbers for the radio selection. */
	static final List NUMBERS = Arrays.asList(new String[] { "1", "2", "3" });

	/** Relevant locales wrapped in a list. */
	private static final List LOCALES = Arrays.asList(new Locale[] { Locale.ENGLISH, new Locale("nl"), Locale.GERMAN,
			Locale.SIMPLIFIED_CHINESE, Locale.JAPANESE, new Locale("pt", "BR"), new Locale("fa", "IR"),
			new Locale("da", "DK") });

	/** available sites for the multiple select. */
	private static final List SITES = Arrays.asList(new String[] { "The Server Side", "Java Lobby", "Java.Net" });

	private Contact selected;

	public Home() {

		add(new Link("link") {

			@Override
			public void onClick() {
				//System.out.println("click received for session " + Session.get());
			}
		});

		add(new Label("selectedLabel", new PropertyModel(this, "selectedContactLabel")));

		add(new DataView("simple", new ContactDataProvider()) {
			protected void populateItem(final Item item) {
				Contact contact = (Contact) item.getModelObject();
				item.add(new ActionPanel("actions", item.getModel()));
				item.add(new Label("contactid", String.valueOf(contact.getId())));
				item.add(new Label("firstname", contact.getFirstName()));
				item.add(new Label("lastname", contact.getLastName()));
				item.add(new Label("homephone", contact.getHomePhone()));
				item.add(new Label("cellphone", contact.getCellPhone()));

				item.add(new AttributeModifier("class", true, new AbstractReadOnlyModel() {
					public Object getObject(Component component) {
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
	public String getSelectedContactLabel() {
		if (selected == null) {
			return "No Contact Selected";
		} else {
			return selected.getFirstName() + " " + selected.getLastName();
		}
	}

	/**
	 * Sets locale for the user's session (getLocale() is inherited from
	 * Component)
	 * 
	 * @param locale
	 *            The new locale
	 */
	public void setLocale(Locale locale) {
		if (locale != null) {
			getSession().setLocale(locale);
		}
	}
}