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
package org.apache.wicket.ajax.form;

import java.util.Collections;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.model.Model;

/**
 * Homepage
 */
public class HomePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	int rows = 1;

	/**
	 */
	public HomePage()
	{
		this(true, 0);
	}

	/**
	 * @param enableInputField
	 * @param newPageId
	 */
	@SuppressWarnings({ "serial", "rawtypes", "unchecked" })
	public HomePage(boolean enableInputField, int newPageId)
	{
		add(new Label("message",
			"If you see this message wicket is properly configured and running"));

		Form<Void> form = new Form<Void>("form");
		// WebMarkupContainer form = new WebMarkupContainer("form"); Both ways do not work
		add(form);
		DropDownChoice<Void> select;
		form.add(select = new DropDownChoice<Void>("select", new Model(), Collections.EMPTY_LIST));
		select.add(new OnChangeAjaxBehavior()
		{
			@Override
			protected void onUpdate(AjaxRequestTarget target)
			{
				setResponsePage(SecondPage.class);
			}
		});
	}
}
