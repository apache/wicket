/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.examples.forminput;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import wicket.FeedbackMessages;
import wicket.PageParameters;
import wicket.examples.util.NavigationPanel;
import wicket.markup.html.HtmlPage;
import wicket.markup.html.form.DropDownChoice;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.IOnChangeListener;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.AbstractValidator;
import wicket.markup.html.form.validation.IValidationErrorHandler;
import wicket.markup.html.form.validation.RequiredValidator;
import wicket.markup.html.form.validation.TypeValidator;
import wicket.markup.html.form.validation.ValidationErrorMessage;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.Model;
import wicket.model.PropertyModel;

/**
 * Example for form input.
 *
 * @author Eelco Hillenius
 */
public class FormInput extends HtmlPage
{
	/** the current locale. */
	private Locale currentLocale;

	/**
	 * All available locales.
	 */
	private static final List ALL_LOCALES = Arrays.asList(Locale.getAvailableLocales());

    /**
     * Constructor
     * @param parameters Page parameters
     */
    public FormInput(final PageParameters parameters)
    {
        add(new NavigationPanel("mainNavigation", "Form Input Example"));
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
	 * @return currentLocale
	 */
	public Locale getCurrentLocale()
	{
		return currentLocale;
	}

	/**
	 * Sets currentLocale.
	 * @param currentLocale currentLocale
	 */
	public void setCurrentLocale(Locale currentLocale)
	{
		this.currentLocale = currentLocale;
	}

    /** Form for input. */
    private static class InputForm extends Form
    {
    	/** object to apply input on. */
    	private TestInputObject input = new TestInputObject();

		/**
		 * Construct.
		 * @param name componentnaam
		 * @param validationErrorHandler error handler
		 */
		public InputForm(String name, IValidationErrorHandler validationErrorHandler)
		{
			super(name, validationErrorHandler);
			RequiredValidator requiredValidator = new RequiredValidator();

			TextField stringInput = new TextField("stringInput", input, "stringProperty");
			stringInput.add(requiredValidator);
			TextField integerInput = new TextField("integerInput", input, "integerProperty");
			integerInput.add(requiredValidator);
			integerInput.add(new TypeValidator(Integer.class));
			TextField doubleInput = new TextField("doubleInput", input, "doubleProperty");
			doubleInput.add(requiredValidator);
			doubleInput.add(new TypeValidator(Double.class));
			TextField dateInput = new TextField("dateInput", input, "dateProperty");
			dateInput.add(requiredValidator);
			dateInput.add(new TypeValidator(Date.class));
			add(stringInput);
			add(integerInput);
			add(doubleInput);
			add(dateInput);

			TextField integerInRangeInput =
				new TextField("integerInRangeInput", input, "integerInRangeProperty");
			integerInRangeInput.add(requiredValidator);
			// an example of how to create a custom validator; extends AbstractValidator
			// for the convenience error messages
			integerInRangeInput.add(new AbstractValidator(){

				public ValidationErrorMessage validate(FormComponent component)
				{
                    final String input = component.getRequestString();
					int value = Integer.parseInt(input.toString());
					if((value < 0) || (value > 100))
					{
						Map vars = new HashMap();
						vars.put("input", input);
						vars.put("lower", "0");
						vars.put("upper", "100");
						return errorMessage("error.outOfRange", vars, input, component);
					}
					return ValidationErrorMessage.NO_MESSAGE; // same as null
				}
				
			});
			add(integerInRangeInput);
		}

		/**
		 * @see wicket.markup.html.form.Form#handleSubmit()
		 */
		public void handleSubmit()
		{
			// everything went well; just display a message
			FeedbackMessages.info(this, "form saved");
		}
    }

    /**
     * Dropdown for selecting the locale.
     */
    private static class LocaleSelect extends DropDownChoice implements IOnChangeListener
    {
		/**
		 * Construct.
		 * @param name componentname
		 * @param object object
		 * @param expression ognl expression
		 * @param values list of values
		 */
		public LocaleSelect(String name, Serializable object,
				String expression, Collection values)
		{
			// Construct a property model WITHOUT formatting
			super(name, new PropertyModel(new Model(object), expression, false), values);
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
