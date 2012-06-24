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
package org.apache.wicket.markup.html.form;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.ComponentTag;
import org.apache.wicket.markup.MarkupStream;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.validation.EqualInputValidator;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.CompoundPropertyModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Gerolf Seitz
 */
public class NestedFormsPage extends WebPage
{
	private static final long serialVersionUID = 1L;
	static Logger logger = LoggerFactory.getLogger(NestedFormsPage.class);

	private final FeedbackPanel feedback;

	public String submitOrder = "";

	public String errorOrder = "";

	/**
	 * Construct.
	 */
	public NestedFormsPage()
	{
		feedback = new FeedbackPanel("feedback");
		add(feedback.setOutputMarkupId(true));

		Form<?> outerForm = new NestableForm("outerForm");
		add(outerForm.setOutputMarkupId(true));

		Form<?> middleForm = new NestableForm("middleForm");
		outerForm.add(middleForm.setOutputMarkupId(true));

		Form<?> innerForm = new NestableForm("innerForm");
		middleForm.add(innerForm.setOutputMarkupId(true));
	}

	/**
	 * @author Gerolf Seitz
	 */
	public class NestableForm extends Form<NestableForm>
	{
		private static final long serialVersionUID = 1L;

		private final String first = "test";

		private final String second = "test";

		/** */
		public boolean onSubmitCalled = false;

		/** */
		public boolean onErrorCalled = false;

		/**
		 * Construct.
		 * 
		 * @param id
		 *            the form's id
		 */
		public NestableForm(String id)
		{
			super(id);
			setDefaultModel(new CompoundPropertyModel<NestableForm>(this));

			TextField<String> firstField = new RequiredTextField<String>("first");
			TextField<String> secondField = new TextField<String>("second");
			add(firstField);
			add(secondField);

			add(new EqualInputValidator(firstField, secondField));
			add(new AjaxSubmitLink("ajaxSubmit", this)
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmitBeforeForm(AjaxRequestTarget target, Form<?> form)
				{
					target.add(feedback);
				}

				@Override
				protected void onError(AjaxRequestTarget target, Form<?> form)
				{
					target.add(feedback);
				}
			});
			add(new ToggleLink("toggle", this));
			add(new SubmitLink("submit"));
		}

		@Override
		protected void onSubmit()
		{
			super.onSubmit();
			onSubmitCalled = true;
			logger.info(getId() + ".onSubmit");
			submitOrder += getId();
		}

		@Override
		protected void onError()
		{
			super.onError();
			onErrorCalled = true;
			logger.info(getId() + ".onError");
			errorOrder += getId();
		}
	}

	private class ToggleLink extends Link<Void>
	{
		private static final long serialVersionUID = 1L;

		private final Form<?> form;

		public ToggleLink(String id, Form<?> form)
		{
			super(id);
			this.form = form;
		}

		@Override
		public void onClick()
		{
			form.setEnabled(!form.isEnabled());
			form.info(form.getId() + ".isEnabled() == " + form.isEnabled());
		}

		@Override
		public void onComponentTagBody(MarkupStream markupStream, ComponentTag openTag)
		{
			String state = form.isEnabled() ? "enabled" : "disabled";
			replaceComponentTagBody(markupStream, openTag, "form is " + state);
		}
	}

}
