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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Homepage
 */
public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/** */
	public String textfieldForm1;

	/** */
	public String textfieldForm2;

	/** */
	public String textfield1;

	/** */
	public String textfield2;

	/** */
	public int formSubmitted = 0;

	/** */
	public static int AJAX = 2;
	/** */
	public static int NORMAL = 4;

	boolean hitOnSubmit = false;
	boolean hitOnError = false;

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
				hitOnSubmit = true;
				target.add(form);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				error("onError");
				hitOnError = true;
				target.add(form);
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

		// --------------------

		Form<Void> form3 = new Form<Void>("form3");
		MyPanel panel = new MyPanel("panel");
		form3.add(panel);
		form3.add(new AjaxSubmitLink("submit")
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				formSubmitted = formSubmitted | AJAX;
				target.add(form);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
			}

		});
		form3.add(new SubmitLink("submit2")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void onSubmit()
			{
				formSubmitted = formSubmitted | NORMAL;
			}
		});
		add(form3);
	}

	/**
	 * @return formSubmitted
	 */
	public int getFormSubmitted()
	{
		return formSubmitted;
	}

	/**
	 * @see org.apache.wicket.Page#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		hitOnSubmit = false;
		hitOnError = false;

		super.onBeforeRender();
	}
}
