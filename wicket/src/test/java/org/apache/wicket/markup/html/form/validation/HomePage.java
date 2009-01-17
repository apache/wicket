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
package org.apache.wicket.markup.html.form.validation;

import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;

/**
 * Homepage
 */
public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/** */
	public String textfieldForm1, textfieldForm2;

	/** */
	public String textfield1, textfield2;

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public HomePage(final PageParameters parameters)
	{

		// Add the simplest type of label
		add(new Label("message",
			"If you see this message wicket is properly configured and running"));

		final Form<Void> form = new Form<Void>("form");
		form.setOutputMarkupId(true);
		add(form);

		form.add(new FeedbackPanel("feedback"));

		form.add(new TextField<String>("textfield1", new PropertyModel<String>(this,
			"textfieldForm1")).setRequired(true));
		form.add(new Label("lbltextfield1", new PropertyModel<String>(this, "textfieldForm1")));

		form.add(new TextField<String>("textfield2", new PropertyModel<String>(this,
			"textfieldForm2")));
		form.add(new Label("lbltextfield2", new PropertyModel<String>(this, "textfieldForm2")));

		form.add(new AjaxSubmitLink("submit")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				info("onSubmit");
				target.addComponent(form);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				error("onError");
				target.addComponent(form);
			}
		});

		// --------------------

		MyBorder border = new MyBorder("border");
		add(border);

		border.add(new TextField<String>("textfield1",
			new PropertyModel<String>(this, "textfield1")).setRequired(true));
		border.add(new Label("lbltextfield1", new PropertyModel<String>(this, "textfield1")));

		border.add(new TextField<String>("textfield2",
			new PropertyModel<String>(this, "textfield2")));
		border.add(new Label("lbltextfield2", new PropertyModel<String>(this, "textfield2")));
	}
}
