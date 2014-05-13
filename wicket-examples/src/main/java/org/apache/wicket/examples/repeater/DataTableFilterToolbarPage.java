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

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterForm;
import org.apache.wicket.extensions.markup.html.repeater.data.table.filter.FilterToolbar;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.model.Model;
import org.apache.wicket.model.PropertyModel;


/**
 * demo page for the datatable component
 * 
 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable
 * @author igor
 * 
 */
public class DataTableFilterToolbarPage extends ExamplePage
{
	/**
	 * constructor
	 */
	public DataTableFilterToolbarPage()
	{
		List<IColumn<Contact, String>> columns = new ArrayList<>();

		columns.add(new PropertyColumn<Contact, String>(new Model<>("ID"), "id")
		{
			@Override
			public String getCssClass()
			{
				return "numeric";
			}
		});

		columns.add(new PropertyColumn<Contact, String>(new Model<>("First Name"), "firstName", "firstName"));

		columns.add(new PropertyColumn<Contact, String>(new Model<>("Last Name"), "lastName", "lastName")
		{
			@Override
			public String getCssClass()
			{
				return "last-name";
			}
		});

		columns.add(new PropertyColumn<Contact, String>(new Model<>("Home Phone"), "homePhone"));
		columns.add(new PropertyColumn<Contact, String>(new Model<>("Cell Phone"), "cellPhone"));

		
		SortableContactDataProvider dataProvider = new SortableContactDataProvider();
		columns = new ArrayList<>(columns);
		columns.add(new PropertyColumn<Contact, String>(new Model<>("Born Date"), "bornDate"));
		
		DataTable<Contact, String> tableWithFilterForm = new DataTable<>("tableWithFilterForm", columns,
			dataProvider, 8);
		
		tableWithFilterForm.setOutputMarkupId(true);
				
		FilterForm<ContactFilter> filterForm = new FilterForm<>("filterForm", dataProvider);
		
		filterForm.add(new TextField<>("dateFrom", PropertyModel.of(dataProvider, "filterState.dateFrom")));
		filterForm.add(new TextField<>("dateTo", PropertyModel.of(dataProvider, "filterState.dateTo")));
		
		add(filterForm);
		
		FilterToolbar filterToolbar = new FilterToolbar(tableWithFilterForm, filterForm);
		
		tableWithFilterForm.addTopToolbar(filterToolbar);
		tableWithFilterForm.addTopToolbar(new NavigationToolbar(tableWithFilterForm));
		tableWithFilterForm.addTopToolbar(new HeadersToolbar<>(tableWithFilterForm, dataProvider));
		filterForm.add(tableWithFilterForm);
	}
}
