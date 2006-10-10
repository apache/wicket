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
import wicket.ajax.ClientEvent;
import wicket.ajax.form.AjaxFormValidatingBehavior;
import wicket.ajax.markup.html.form.AjaxSubmitButton;
import wicket.markup.html.form.Form;
import wicket.markup.html.form.FormComponent;
import wicket.markup.html.form.RequiredTextField;
import wicket.markup.html.form.SimpleFormComponentLabel;
import wicket.markup.html.panel.FeedbackPanel;
import wicket.model.CompoundPropertyModel;
import wicket.model.ResourceModel;
import wicket.util.time.Duration;
import wicket.validation.validator.EmailAddressPatternValidator;
import wicket.validation.validator.StringValidator;

/**
 * Page to demonstrate instant ajax validaion feedback. Validation is triggered
 * as the user is typing, but is throttled so that only one ajax call is made to
 * the server per second.
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
		// create feedback panel to show errors
		final FeedbackPanel feedback = new FeedbackPanel(this, "feedback");
		feedback.setOutputMarkupId(true);

		// add form with markup id setter so it can be updated via ajax
		Form<Bean> form = new Form<Bean>(this, "form", new CompoundPropertyModel<Bean>(bean));
		form.setOutputMarkupId(true);

		FormComponent fc;

		// add form components to the form as usual

		fc = new RequiredTextField(form, "name");
		fc.add(StringValidator.minimumLength(4));
		fc.setLabel(new ResourceModel("label.name"));

		new SimpleFormComponentLabel(form, "name-label", fc);

		fc = new RequiredTextField(form, "email");
		fc.add(EmailAddressPatternValidator.getInstance());
		fc.setLabel(new ResourceModel("label.email"));

		new SimpleFormComponentLabel(form, "email-label", fc);

		// attach an ajax validation behavior to all form component's onkeydown
		// event and throttle it down to once per second

		AjaxFormValidatingBehavior.addToAllFormComponents(form, ClientEvent.KEYUP,
				Duration.ONE_SECOND);

		// add a button that can be used to submit the form via ajax
		new AjaxSubmitButton(form, "ajax-submit-button", form)
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form form)
			{
				target.addComponent(feedback);
			}
		};
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