/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.examples.forminput;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import wicket.PageParameters;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.IOnChangeListener;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.markup.html.form.validation.IntegerValidator;
import wicket.markup.html.link.Link;
import wicket.markup.html.link.OnClickLink;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Example for form input.
 * 
 * @author Eelco Hillenius
 */
public class FormInput extends WicketExamplePage
{
	/** the current locale. */
	private Locale currentLocale;

	/**
	 * All available locales.
	 */
	private static final List ALL_LOCALES = Arrays.asList(Locale.getAvailableLocales());

	/**
	 * Constructor
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public FormInput(final PageParameters parameters)
	{
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		add(new InputForm("inputForm", feedback));
		currentLocale = getSession().getLocale();
		add(new LocaleSelect("localeSelect", this, "currentLocale", ALL_LOCALES));
		add(new Link("defaultLocaleLink")
		{
			public void linkClicked()
			{
				// Get locale of request
				final Locale requestLocale = getRequest().getLocale();

				// Set current locale
				FormInput.this.currentLocale = requestLocale;

				// Change session locale
				getSession().setLocale(requestLocale);
			}
		});
	}

	/**
	 * Gets currentLocale.
	 * 
	 * @return currentLocale
	 */
	public Locale getCurrentLocale()
	{
		return currentLocale;
	}

	/**
	 * Sets currentLocale.
	 * 
	 * @param currentLocale
	 *            currentLocale
	 */
	public void setCurrentLocale(Locale currentLocale)
	{
		this.currentLocale = currentLocale;
	}

	/** Form for input. */
	private static class InputForm extends Form
	{
		/** object to apply input on. */
		private TestInputObject testInputObject = new TestInputObject();

		/**
		 * Construct.
		 * 
		 * @param name
		 *            componentnaam
		 * @param validationErrorHandler
		 *            error handler
		 */
		public InputForm(String name, IValidationFeedback validationErrorHandler)
		{
			super(name, validationErrorHandler);

			// model that allways gets the current input property so that can
			// we change the property itself and have our other models still
			// work
			IModel inputModel = new Model()
			{
				public Object getObject()
				{
					return testInputObject;
				}
			};

			add(new RequiredTextField("stringInput", inputModel, "stringProperty"));
			add(new RequiredTextField("integerInput", inputModel, "integerProperty", Integer.class));
			add(new RequiredTextField("doubleInput", inputModel, "doubleProperty", Double.class));
			add(new RequiredTextField("dateInput", inputModel, "dateProperty", Date.class));

			TextField integerInRangeInput = new TextField("integerInRangeInput", testInputObject,
					"integerInRangeProperty");
			integerInRangeInput.add(IntegerValidator.range(0, 100));            
			add(integerInRangeInput);
            
			add(new OnClickLink("resetButton")
			{
				public void linkClicked()
				{
					testInputObject = new TestInputObject();
				}
			});
		}

		/**
		 * @see wicket.markup.html.form.Form#handleValidSubmit()
		 */
		public void handleValidSubmit()
		{
			// Everything went well; just display a message
			info("Saved model " + testInputObject);
		}
	}

	/**
	 * Dropdown for selecting the locale.
	 */
	private static class LocaleSelect extends DropDownChoice implements IOnChangeListener
	{
		/**
		 * Construct.
		 * 
		 * @param name
		 *            componentname
		 * @param object
		 *            object
		 * @param expression
		 *            ognl expression
		 * @param values
		 *            list of values
		 */
		public LocaleSelect(String name, Serializable object, String expression, Collection values)
		{
			super(name, new PropertyModel(new Model(object), expression), values);
		}

		/**
		 * @see wicket.markup.html.form.DropDownChoice#selectionChanged(java.lang.Object)
		 */
		public void selectionChanged(Object newSelection)
		{
			getSession().setLocale((Locale)newSelection);
		}
	}
}