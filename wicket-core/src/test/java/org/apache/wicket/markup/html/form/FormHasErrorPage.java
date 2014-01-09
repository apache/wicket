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

import org.junit.Assert;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * FormHasErrorPage
 */
public class FormHasErrorPage extends WebPage
{
	/**
	 */
	public static final class InvalidPanel extends Panel
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Construct.
		 * 
		 * @param id
		 */
		public InvalidPanel(String id)
		{
			super(id);
		}
	}

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public FormHasErrorPage(final PageParameters parameters)
	{

		final IModel<String> labelModel = new Model<String>("");
		Label passFail = new Label("passFail", labelModel);
		add(passFail);

		final SubmitLink submitComponent = new SubmitLink("submitComponent");
		final SubmitLink submitFormComponent = new SubmitLink("submitFormComponent");
		final SubmitLink submitForm = new SubmitLink("submitForm");

		final InvalidPanel invalidPanel = new InvalidPanel("invalidPanel");
		final CheckBox formComponent = new CheckBox("formComponent", new Model<Boolean>());

		Form<Void> form = new Form<Void>("form")
		{
			private static final long serialVersionUID = 1L;

			private transient IFormSubmitter submittingComponent;

			@Override
			protected void onError()
			{
				super.onError();
				labelModel.setObject("Test PASSED - an error was expected.");
			}

			@Override
			protected void onSubmit()
			{
				super.onSubmit();
				labelModel.setObject("Test FAILED - an error was expected");
				Assert.fail("A validation error should've been detected by the Form processing");
			}

			@Override
			public void process(IFormSubmitter submittingComponent)
			{
				// keep submitting component for #onValidate()
				this.submittingComponent = submittingComponent;

				super.process(submittingComponent);
			}

			@Override
			protected void onValidate()
			{
				// set the error based on which link submitted the form
				if (submittingComponent == submitFormComponent)
				{
					formComponent.error("FormComponent validation error");
				}
				else if (submittingComponent == submitComponent)
				{
					invalidPanel.error("Panel validation error");
				}
				else
				{
					error("Form validation error");
				}
			}
		};
		add(form);
		form.add(submitComponent);
		form.add(submitFormComponent);
		form.add(submitForm);

		form.add(invalidPanel);
		form.add(formComponent);

		add(new FeedbackPanel("feedback"));
	}
}
