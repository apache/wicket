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
package org.apache.wicket.ajax.markup.html;

import java.util.Arrays;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.ListView;
import org.apache.wicket.markup.html.panel.EmptyPanel;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * Test page for triggering the component is not on page error from AjaxRequestHandler.
 */
public class ComponentNotOnPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	@Override
	protected void onInitialize()
	{
		super.onInitialize();

		add(new EmptyPanel("refresher"));

		add(new ListView<Integer>("listview", Arrays.asList(1, 2, 3, 4, 5))
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<Integer> item)
			{
				item.setOutputMarkupId(true);

				item.add(new AjaxLink<Void>("link")
				{
					private static final long serialVersionUID = 1L;

					@Override
					public void onClick(AjaxRequestTarget target)
					{
						LastSelectedPanel lastSelected = new LastSelectedPanel("refresher", item);
						ComponentNotOnPage.this.replace(lastSelected);
						target.add(lastSelected);
					}
				});
			}
		});
	}

	public static class LastSelectedPanel extends Panel
	{
		private static final long serialVersionUID = 1L;

		LastSelectedPanel(String id, WebMarkupContainer refresher)
		{
			super(id);

			setOutputMarkupId(true);
			add(new AjaxLink<Void>("refresh")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick(AjaxRequestTarget target)
				{
					target.add(refresher);
				}
			});
		}
	}
}
