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
package org.apache.wicket.util.tester;

import java.io.Serializable;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.form.AjaxFormValidatingBehavior;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.validation.validator.StringValidator;

/**
 * Web page that contains a form with ajax functionality.
 * 
 * @author Kare Nuorteva
 */
public class MockAjaxFormPage extends WebPage
{
	private static final class MockDomainObject implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private String text;

		/**
		 * Gets text.
		 * 
		 * @return text Text entered in the text field
		 */
		public String getText()
		{
			return text;
		}

		/**
		 * Sets text.
		 * 
		 * @param text
		 *            New value for the text field
		 */
		public void setText(String text)
		{
			this.text = text;
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Construct.
	 */
	public MockAjaxFormPage()
	{
		Form<MockDomainObject> form = new Form<>("form",
			new CompoundPropertyModel<>(new MockDomainObject()));
		add(form);
		final Button submit = new Button("submit");
		submit.setOutputMarkupId(true);
		submit.setEnabled(false);
		form.add(submit);
		final TextField<String> text = new TextField<>("text");
		form.add(text);
		text.setRequired(true);
		text.add(new StringValidator(4, null));
		text.add(new AjaxFormValidatingBehavior("keyup")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target)
			{
				super.onSubmit(target);

				text.validate();
				submit.setEnabled(text.isValid());
				target.add(submit);
			}

			@Override
			protected void onError(AjaxRequestTarget target)
			{
				super.onError(target);

				text.validate();
				submit.setEnabled(text.isValid());
				target.add(submit);
			}
		});
	}
}
