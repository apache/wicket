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

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.form.AjaxButton;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.SubmitLink;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;

/**
 */
public class MockFormSubmitsPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	public Form<Void> form;

	public String text = "a text value";

	/**
	 * Construct.
	 */
	public MockFormSubmitsPage()
	{
		form = new Form<Void>("form");
		add(form);

		form.add(new TextField("text", new PropertyModel(this, "text")));

		form.add(new Button("button"));

		form.add(new AjaxButton("ajaxButton")
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				MockFormSubmitsPage.this.onAjaxButtonSubmit(target, form);
			}
		});

		form.add(new SubmitLink("link"));

		form.add(new AjaxSubmitLink("ajaxlink")
		{
			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				MockFormSubmitsPage.this.onAjaxSubmitLinkSubmit(target, form);
			}
		});
	}

	protected void onAjaxSubmitLinkSubmit(AjaxRequestTarget target, Form<?> form)
	{
	}

	protected void onAjaxButtonSubmit(AjaxRequestTarget target, Form<?> form)
	{
	}
}
