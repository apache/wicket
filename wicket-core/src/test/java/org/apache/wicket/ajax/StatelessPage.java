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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.markup.html.AjaxFallbackLink;
import org.apache.wicket.ajax.markup.html.form.AjaxSubmitLink;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.StatelessForm;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.request.mapper.parameter.PageParameters;


/**
 * Homepage
 */
public class StatelessPage extends WebPage
{
	public static final String AJAX_SUBMIT = "AJAX submit";

	public static final String FORM_SUBMIT = "form submit";

	static int itemCount = 0;

	private static final long serialVersionUID = 1L;

	static List<String> getList()
	{
		final ArrayList<String> list = new ArrayList<String>(itemCount);
		final int count = ++itemCount;

		for (int idx = 1; idx <= count; idx++)
		{
			list.add(Integer.toString(idx));
		}

		return list;
	}

	/**
	 * Constructor that is invoked when page is invoked without a session.
	 * 
	 * @param parameters
	 *            Page parameters
	 */
	public StatelessPage(final PageParameters parameters)
	{
		super(parameters);

		final MarkupContainer list = new WebMarkupContainer("list");
		final List<String> data = getList();
		final ListView<String> listView = new ListView<String>("item", data)
		{
			private static final long serialVersionUID = 200478523599165606L;

			@Override
			protected void populateItem(final ListItem<String> item)
			{
				final String _item = item.getModelObject();

				item.add(new Label("value", _item));
			}
		};
		final Link<String[]> moreLink = new AjaxFallbackLink<String[]>("more")
		{
			private static final long serialVersionUID = -1023445535126577565L;

			@Override
			public void onClick(final AjaxRequestTarget target)
			{
				final List<String> _data = getList();

				System.out.println(_data);

				listView.setModelObject(_data);

				if (target != null)
				{
					target.add(list, "new");
				}
			}
			
			@Override
			protected boolean getStatelessHint()
			{
				return true;
			}
		};
		final Link<String> homeLink = new BookmarkablePageLink<String>("home", StatelessPage.class);

		add(homeLink);
		list.add(listView);
		add(list);
		add(moreLink);

		// add form
		TextField<String> name = new TextField<String>("name", new Model<String>("name"));
		TextField<String> surname = new TextField<String>("surname", new Model<String>("surname"));

		Form<String> form = new StatelessForm<String>("inputForm")
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = -6554405700693024016L;

			@Override
			protected void onSubmit()
			{
				super.onSubmit();
				info(FORM_SUBMIT);
			}
		};

		form.add(name, surname);

		final FeedbackPanel feedback;
		form.add(feedback = new FeedbackPanel("feedback"));
		feedback.setOutputMarkupId(true);

		form.add(new AjaxSubmitLink("submit")
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = -7296676299335203926L;

			@Override
			protected void onSubmit(AjaxRequestTarget target, Form<?> form)
			{
				super.onSubmit(target, form);
				info(AJAX_SUBMIT);
				target.add(feedback);
			}
			
			@Override
			protected boolean getStatelessHint()
			{
				return true;
			}
		});
		add(form);
	}
}
