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
package org.apache.wicket.extensions.markup.html.repeater.data.table;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * demo page for the datatable component
 * 
 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable
 * @author igor
 * 
 */
public class DataTablePage extends WebPage
{
	private static final long serialVersionUID = 1L;

	/**
	 * constructor
	 */
	public DataTablePage()
	{
		List<IColumn<Contact, String>> columns = new ArrayList<>();

		columns.add(new AbstractColumn<Contact, String>(new Model<>("Actions"))
		{
			private static final long serialVersionUID = 1L;

			@Override
			public void populateItem(final Item<ICellPopulator<Contact>> cellItem,
				final String componentId, final IModel<Contact> rowModel)
			{
				cellItem.add(new WebMarkupContainer(componentId, rowModel));
			}
		});

		columns.add(new PropertyColumn<Contact, String>(new Model<>("ID"), "id")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getCssClass()
			{
				return "numeric";
			}
		});

		columns.add(new PropertyColumn<Contact, String>(new Model<>("First Name"), "firstName",
			"firstName"));

		columns.add(new PropertyColumn<Contact, String>(new Model<>("Last Name"), "lastName",
			"lastName")
		{
			private static final long serialVersionUID = 1L;

			@Override
			public String getCssClass()
			{
				return "last-name";
			}
		});

		columns.add(new PropertyColumn<Contact, String>(new Model<>("Home Phone"), "homePhone"));
		columns.add(new PropertyColumn<Contact, String>(new Model<>("Cell Phone"), "cellPhone"));

		@SuppressWarnings({ "rawtypes", "unchecked" })
		DefaultDataTable defaultDataTable = new DefaultDataTable("table", columns,
			new SortableContactDataProvider(), 8)
		{

			@Override
			protected IModel getCaptionModel()
			{
				return DataTablePage.this.getCaptionModel();
			}

		};
		add(defaultDataTable);
	}

	protected IModel<String> getCaptionModel()
	{
		return null;
	}
}
