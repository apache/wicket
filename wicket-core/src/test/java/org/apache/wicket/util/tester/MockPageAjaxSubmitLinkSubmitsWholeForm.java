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
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * WICKET-3053
 * 
 * Clicking AjaxSubmitLink does not preserve the values of the form components and they get
 * null-ified
 * 
 * 
 * The code is borrowed from Wicket+Guice LegUp (http://jweekend.com/dev/LegUp)
 * 
 * @author Richard Wilkinson - richard.wilkinson@jweekend.com
 */
public class MockPageAjaxSubmitLinkSubmitsWholeForm extends WebPage
{
	private static final long serialVersionUID = 1L;

	private String text;

	private String name;

	/**
	 * Constructor that is invoked when page is invoked without a em.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public MockPageAjaxSubmitLinkSubmitsWholeForm(final PageParameters parameters)
	{
		final Label label = new Label("text", new PropertyModel<String>(this, "text"));
		label.setOutputMarkupId(true);

		add(label);

		Form<Void> form = new Form<Void>("form");
		form.add(new TextField<String>("name", new PropertyModel<String>(this, "name")));

		add(form);

		add(new AjaxSubmitLink("helloSubmit", form)
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				text = "Hello " + name;
				target.add(label);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				throw new RuntimeException("Unexpected error occurred.");
			}
		});

		add(new AjaxSubmitLink("goodbyeSubmit", form)
		{

			private static final long serialVersionUID = 1L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				text = "Goodbye " + name;
				target.add(label);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
				throw new RuntimeException("Unexpected error occurred.");
			}
		});

	}
}
