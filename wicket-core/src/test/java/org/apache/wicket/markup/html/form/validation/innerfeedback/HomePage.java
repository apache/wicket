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
package org.apache.wicket.markup.html.form.validation.innerfeedback;

import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Homepage
 */
public class HomePage extends BasePage
{

	private static final long serialVersionUID = 1L;

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	@SuppressWarnings("serial")
	public HomePage(final PageParameters parameters)
	{
		Form<Void> form = new Form<Void>("form")
		{
			@Override
			protected void onSubmit()
			{
				super.onSubmit();
				info("form submitted");
			}
		};
		LocalizedFeedbackBorder brdr = new LocalizedFeedbackBorder("fieldborder");
		TextField<String> field = new TextField<String>("field", new Model<String>(""));
		field.info("info on field");
		brdr.add(field.setRequired(true));
		form.add(brdr);
		add(form);
	}

	@Override
	protected void onBeforeRender()
	{
		info("page onbeforerender");
		super.onBeforeRender();
	}
}
