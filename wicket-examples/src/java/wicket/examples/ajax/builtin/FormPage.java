/*
 * $Id: FormPage.java 4916 2006-03-13 23:15:39 -0800 (Mon, 13 Mar 2006)
 * ivaynberg $ $Revision$ $Date: 2006-03-13 23:15:39 -0800 (Mon, 13 Mar
 * 2006) $
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
package wicket.examples.ajax.builtin;

import java.io.Serializable;

import wicket.ajax.AjaxRequestTarget;
import wicket.ajax.IAjaxCallDecorator;
import wicket.ajax.calldecorator.AjaxCallThrottlingDecorator;
import wicket.ajax.form.AjaxFormValidatingBehavior;
import wicket.ajax.markup.html.form.AjaxSubmitButton;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.form.SimpleFormComponentLabel;
import wicket.markup.html.form.validation.EmailAddressPatternValidator;
import wicket.markup.html.form.validation.StringValidator;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;
import wicket.model.ResourceModel;
import wicket.util.time.Duration;

/**
 * Page to demonstrate instant ajax validaion feedback. Validation is trigger in
 * onblur javascript event handler in every form input.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class FormPage extends BasePage
{
	private Bean bean = new Bean();

	/**
	 * Constructor
	 */
	public FormPage()
	{
		// add feedback panel with a markup id setter so it can be updated via
		// ajax
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);

		// add form with markup id setter so it can be updated via ajax
		Form form = new Form("form", new CompoundPropertyModel(bean));
		add(form);
		form.setOutputMarkupId(true);

		FormComponent fc;

		// add form components to the form as usual

		fc = new RequiredTextField("name");
		fc.add(StringValidator.minimumLength(4));
		fc.setLabel(new ResourceModel("label.name"));


		form.add(fc);
		form.add(new SimpleFormComponentLabel("name-label", fc));

		fc = new RequiredTextField("email");
		fc.add(EmailAddressPatternValidator.getInstance());
		fc.setLabel(new ResourceModel("label.email"));

		form.add(fc);
		form.add(new SimpleFormComponentLabel("email-label", fc));

		AjaxFormValidatingBehavior.addToAllFormComponents(form, "onkeypress", Duration.ONE_SECOND);

		form.add(new AjaxSubmitButton("ajax-submit-button", form)
		{

			protected void onSubmit(AjaxRequestTarget target)
			{
				target.addComponent(feedback);
			}

		});

	}

	/** simple java bean. */
	public static class Bean implements Serializable
	{
		private String name, email;

		/**
		 * Gets email.
		 * 
		 * @return email
		 */
		public String getEmail()
		{
			return email;
		}

		/**
		 * Sets email.
		 * 
		 * @param email
		 *            email
		 */
		public void setEmail(String email)
		{
			this.email = email;
		}

		/**
		 * Gets name.
		 * 
		 * @return name
		 */
		public String getName()
		{
			return name;
		}

		/**
		 * Sets name.
		 * 
		 * @param name
		 *            name
		 */
		public void setName(String name)
		{
			this.name = name;
		}
	}
}