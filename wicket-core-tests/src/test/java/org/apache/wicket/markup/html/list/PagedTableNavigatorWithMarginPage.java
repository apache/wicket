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
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;


/**
 * Dummy page used for resource testing.
 */
public class PagedTableNavigatorWithMarginPage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Construct. page parameters.
	 */
	public PagedTableNavigatorWithMarginPage()
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
		list.add("nine");
		list.add("ten");
		list.add("eleven");
		list.add("twelve");
		list.add("thirteen");
		list.add("fourteen");

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
		add(new PagingNavigator("navigator", table)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.navigation.paging.PagingNavigator#newNavigation(java.lang.String,
			 *      org.apache.wicket.markup.html.navigation.paging.IPageable,
			 *      org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider)
			 */
			@Override
			protected PagingNavigation newNavigation(final String id, IPageable pageable,
				IPagingLabelProvider labelProvider)
			{
				PagingNavigation nav = new PagingNavigation(id, pageable);
				nav.setMargin(2);
				if (nav.getViewSize() > 5)
				{
					nav.setViewSize(5);
				}

				nav.setSeparator(", ");
				return nav;
			}
		});
	}

	/**
	 * @see org.apache.wicket.Component#isVersioned()
	 */
	@Override
	public boolean isVersioned()
	{
		// for testing we set versioning off, because it gets too difficult to maintain otherwise
		return false;
	}
}
