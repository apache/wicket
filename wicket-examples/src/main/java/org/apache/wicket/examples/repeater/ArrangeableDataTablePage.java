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

import org.apache.wicket.extensions.markup.html.repeater.data.table.ArrangeableDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.HeadersToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IMovableColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NavigationToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.NoRecordsToolbar;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.model.ResourceModel;

/**
 * Demo page of the {@link ArrangeableDataTable}, a DataTable whose columns the user can resize and
 * move.
 */
public class ArrangeableDataTablePage extends BasePage
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 */
	public ArrangeableDataTablePage()
	{
		List<IColumn<Contact, String>> columns = new ArrayList<>();
		columns.add(new PropertyColumn<>(new ResourceModel("id"), "id", "id"));
		columns.add(new MovableColumn("firstName"));
		columns.add(new MovableColumn("lastName"));
		columns.add(new MovableColumn("homePhone"));
		columns.add(new MovableColumn("cellPhone"));
		columns.add(new MovableColumn("bornDate"));

		SortableContactDataProvider provider = new SortableContactDataProvider();
		ArrangeableDataTable<Contact, String> table = new ArrangeableDataTable<>("table",
			columns, provider, 10);
		table.addTopToolbar(new NavigationToolbar(table));
		table.addTopToolbar(new HeadersToolbar<>(table, provider));
		table.addBottomToolbar(new NoRecordsToolbar(table));
		add(table);
	}

	/**
	 * A sortable column of a property of a contact, which the user can move.
	 */
	private static class MovableColumn extends PropertyColumn<Contact, String>
		implements
			IMovableColumn
	{
		private static final long serialVersionUID = 1L;

		MovableColumn(String property)
		{
			super(new ResourceModel(property), property, property);
		}
	}
}
