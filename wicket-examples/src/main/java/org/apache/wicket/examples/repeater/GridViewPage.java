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
package org.apache.wicket.examples.repeater;

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigator;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.GridView;
import org.apache.wicket.markup.repeater.data.IDataProvider;

/**
 * page for demonstrating the gridview component
 * 
 * @author igor
 */
public class GridViewPage extends BasePage
{
	/**
	 * Constructor
	 */
	public GridViewPage()
	{
		IDataProvider<Contact> dataProvider = new ContactDataProvider();
		GridView<Contact> gridView = new GridView<Contact>("rows", dataProvider)
		{
            @Override
            protected void populateRowItem(Item<?> item) {
                super.populateRowItem(item);
                item.add(new Label("rowHeader", "Category " + (item.getIndex() + 1)));
            }

            @Override
			protected void populateItem(Item<Contact> item)
			{
				final Contact contact = item.getModelObject();
				item.add(new Label("firstName", contact.getFirstName() + " " +
					contact.getLastName()));
			}

			@Override
			protected void populateEmptyItem(Item<Contact> item)
			{
				item.add(new Label("firstName", "*empty*"));
			}
		};

		gridView.setRows(4);
		gridView.setColumns(3);

		add(gridView);
		add(new PagingNavigator("navigator", gridView));
	}
}
