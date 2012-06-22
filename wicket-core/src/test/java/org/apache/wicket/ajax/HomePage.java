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
package org.apache.wicket.ajax;

import java.util.LinkedList;
import java.util.List;

import org.apache.wicket.ajax.markup.html.form.AjaxFallbackButton;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.model.AbstractReadOnlyModel;
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
	@SuppressWarnings("serial")
	public HomePage(boolean enableInputField, int newPageId)
	{
		// Add the simplest type of label
		add(new Label("message",
			"If you see this message wicket is properly configured and running"));

		Form<Void> form = new Form<Void>("form")
		{

		};
		add(form);
		form.add(new TextField<String>("textfield", new Model<String>()));
		form.add(new Button("submit"));

		final WebMarkupContainer listViewContainer = new WebMarkupContainer("listViewContainer");
		form.add(listViewContainer);
		listViewContainer.setOutputMarkupId(true);

		final ListView<Object> listView;
		listViewContainer.add(listView = new ListView<Object>("listView",
			new AbstractReadOnlyModel<List<Object>>()
			{
				@Override
				public List<Object> getObject()
				{
					List<Object> objects = new LinkedList<Object>();
					for (int i = 0; i < rows; i++)
					{
						objects.add(new Object());
					}

					return objects;
				}
			})
		{
			@Override
			protected void populateItem(ListItem<Object> item)
			{
				item.add(new Label("label", Long.toString(item.getIndex())));
			}
		});

		form.add(new AjaxFallbackButton("addButton", form)
		{
			@Override
			protected void onSubmitBeforeForm(AjaxRequestTarget target, Form<?> form)
			{
				rows++;
				listView.removeAll();
				target.add(listViewContainer);
			}

			@Override
			protected void onError(AjaxRequestTarget target, Form<?> form)
			{
			}
		}.setDefaultFormProcessing(false));
	}
}
