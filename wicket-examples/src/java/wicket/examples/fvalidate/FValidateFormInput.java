/*
 * $Id$ $Revision:
 * 1.9 $ $Date$
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
package wicket.examples.fvalidate;

import java.util.Date;
import java.util.Locale;

import wicket.PageParameters;
import wicket.Session;
import wicket.contrib.markup.html.form.fvalidate.FValidateTextField;
import wicket.examples.WicketExamplePage;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.TextField;
import wicket.markup.html.form.validation.IValidationFeedback;
import wicket.markup.html.form.validation.RequiredValidator;
import wicket.markup.html.form.validation.TypeValidator;
import wicket.markup.html.panel.FeedbackPanel;

/**
 * Example for form input.
 * 
 * @author Eelco Hillenius
 */
public class FValidateFormInput extends WicketExamplePage
{
	/**
	 * Constructor
	 * @param parameters Page parameters
	 */
	public FValidateFormInput(final PageParameters parameters)
	{
		Session.get().setLocale(Locale.ENGLISH);
		FeedbackPanel feedback = new FeedbackPanel("feedback");
		add(feedback);
		add(new InputForm("inputForm", feedback));
	}

	/** Form for input. */
	private static class InputForm extends Form
	{
		/** object to apply input on. */
		private TestInputObject input = new TestInputObject();

		/**
		 * Construct.
		 * @param name componentnaam
		 * @param validationFeedback error handler
		 */
		public InputForm(String name, IValidationFeedback validationFeedback)
		{
			super(name, validationFeedback);

			FValidateTextField stringInput =
				new FValidateTextField("stringInput", input, "stringProperty");
			stringInput.add(new RequiredValidator());
			FValidateTextField integerInput =
				new FValidateTextField("integerInput", input, "integerProperty");
			integerInput.add(new RequiredValidator());
			integerInput.add(new TypeValidator(Integer.class));
			FValidateTextField doubleInput =
				new FValidateTextField("doubleInput", input, "doubleProperty");
			doubleInput.add(new RequiredValidator());
			doubleInput.add(new TypeValidator(Double.class));
			FValidateTextField dateInput =
				new FValidateTextField("dateInput", input, "dateProperty");
			dateInput.add(new RequiredValidator());
			dateInput.add(new TypeValidator(Date.class));
			add(stringInput);
			add(integerInput);
			add(doubleInput);
			add(dateInput);

			TextField integerInRangeInput = new
				TextField("integerInRangeInput", input, "integerInRangeProperty");
			integerInRangeInput.add(new RequiredValidator());
			add(integerInRangeInput);
		}

		/**
		 * @see wicket.markup.html.form.Form#handleSubmit()
		 */
		public void handleSubmit()
		{
		}
	}
}
