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
package org.apache.wicket.examples.ajax.builtin;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.RequiredTextField;
import org.apache.wicket.markup.html.form.SimpleFormComponentLabel;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.ResourceModel;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.apache.wicket.validation.validator.StringValidator;

/**
 * Page to demonstrate instant ajax validaion feedback. Validation is triggered as the user is
 * typing, but is throttled so that only one ajax call is made to the server per second.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class FormPage extends BasePage
{

	/**
	 * Constructor
	 */
	public FormPage()
	{
		// create feedback panel to show errors
		final FeedbackPanel feedback = new FeedbackPanel("feedback");
		feedback.setOutputMarkupId(true);
		add(feedback);

		// add form with markup id setter so it can be updated via ajax
		Bean bean = new Bean();
		Form<Bean> form = new Form<>("form", new CompoundPropertyModel<>(bean));
		add(form);
		form.setOutputMarkupId(true);

		FormComponent fc;

		// add form components to the form as usual

		fc = new RequiredTextField<>("name");
		fc.add(new StringValidator(4, null));
		fc.setLabel(new ResourceModel("label.name"));

		form.add(fc);
		form.add(new SimpleFormComponentLabel("name-label", fc));

		fc = new RequiredTextField<>("email");
		fc.add(EmailAddressValidator.getInstance());
		fc.setLabel(new ResourceModel("label.email"));

		form.add(fc);
		form.add(new SimpleFormComponentLabel("email-label", fc));

		// attach an ajax validation behavior to all form component's keydown
		// event and throttle it down to once per second

		form.add(new AjaxFormValidatingBehavior("keydown", Duration.ONE_SECOND));

		// add a button that can be used to submit the form via ajax
		form.add(new AjaxButton("ajax-button", form)
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				// repaint the feedback panel so that it is hidden
				target.add(feedback);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				// repaint the feedback panel so errors are shown
				target.add(feedback);
			}
		});
	}

	/** simple java bean. */
	public static class Bean implements IClusterable
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
