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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.PropertyModel;

/**
 * 
 */
public class MyPanel extends Panel
{
	private static final long serialVersionUID = 1L;

	private String textfield1;

	/**
	 * Construct.
	 * 
	 * @param id
	 */
	public MyPanel(String id)
	{
		super(id);

		Form<Void> form = new Form<Void>("form3");
		form.setOutputMarkupId(true);
		add(form);

		form.add(new FeedbackPanel("feedback"));
		form.add(new TextField<String>("textfield1", new PropertyModel<String>(this, "textfield1")));
		form.add(new Label("lbltextfield1", new PropertyModel<String>(this, "textfield1")));

		form.add(new AbstractFormValidator()
		{
			private static final long serialVersionUID = 1L;

			@Override
			public FormComponent<?>[] getDependentFormComponents()
			{
				return null;
			}

			@Override
			public void validate(Form<?> form)
			{
				form.error("form validation error");
			}
		});
	}
}
