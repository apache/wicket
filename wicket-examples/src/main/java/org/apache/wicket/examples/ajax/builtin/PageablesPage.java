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
package org.apache.wicket.examples.ajax.builtin;

import java.util.Arrays;

import org.apache.wicket.ajax.markup.html.navigation.paging.AjaxPagingNavigator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.list.ListItem;
import org.apache.wicket.markup.html.list.PageableListView;


/**
 * Shows an ajaxian paging navigator in action.
 * 
 * @author Martijn Dashorst
 */
public class PageablesPage extends BasePage
{
	private static final String[] names = { "Doe, John", "Presley, Elvis", "Presly, Priscilla",
			"John, Elton", "Jackson, Michael", "Bush, George", "Baker, George",
			"Stallone, Sylvester", "Murphy, Eddie", "Potter, Harry", "Balkenende, Jan Peter",
			"Two Shoes, Goody", "Goodman, John", "Candy, John", "Belushi, James",
			"Jones, James Earl", "Kelly, Grace", "Osborne, Kelly", "Cartman", "Kenny",
			"Schwarzenegger, Arnold", "Pitt, Brad", "Richie, Nicole", "Richards, Denise",
			"Sheen, Charlie", "Sheen, Martin", "Esteves, Emilio", "Baldwin, Alec",
			"Knowles, Beyonce", "Affleck, Ben", "Lavigne, Avril", "Cuthbert, Elisha",
			"Longoria, Eva", "Clinton, Bill", "Willis, Bruce", "Farrell, Colin",
			"Hasselhoff, David", "Moore, Demi", };

	/**
	 * Constructor.
	 */
	public PageablesPage()
	{
		WebMarkupContainer datacontainer = new WebMarkupContainer("data");
		datacontainer.setOutputMarkupId(true);
		add(datacontainer);

		PageableListView<String> listview = new PageableListView<String>("rows", Arrays.asList(names), 10)
		{
			@Override
			protected void populateItem(ListItem<String> item)
			{
				item.add(new Label("name", item.getModelObject()));
			}
		};

		datacontainer.add(listview);
		datacontainer.add(new AjaxPagingNavigator("navigator", listview));
		datacontainer.setVersioned(false);
	}
}
