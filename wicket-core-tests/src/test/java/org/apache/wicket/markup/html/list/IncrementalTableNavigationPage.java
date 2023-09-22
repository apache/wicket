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
package org.apache.wicket.markup.html.list;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;


/**
 * Dummy page used for resource testing.
 */
public class IncrementalTableNavigationPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct. page parameters.
	 */
	public IncrementalTableNavigationPage()
	{
		super();
		List<String> list = new ArrayList<String>();
		list.add("one");
		list.add("two");
		list.add("three");
		list.add("four");
		list.add("five");
		list.add("six");
		list.add("seven");
		list.add("eight");

		PageableListView<String> table = new PageableListView<String>("table", list, 2)
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected void populateItem(ListItem<String> listItem)
			{
				String txt = listItem.getModelObject();
				listItem.add(new Label("txt", txt));
			}
		};

		add(table);
		PagingNavigationIncrementLink<Void> prev = new PagingNavigationIncrementLink<Void>("prev",
			table, -1);
		add(prev);
		PagingNavigationIncrementLink<Void> nextNext = new PagingNavigationIncrementLink<Void>(
			"nextNext", table, +2);
		add(nextNext);
	}
}
