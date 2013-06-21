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

import org.apache.wicket.extensions.markup.html.repeater.data.grid.DataGridView;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.PropertyPopulator;

/**
 * demo page for the datatable component
 * 
 * @see org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable
 * 
 * @author igor
 */
public class DataGridPage extends BasePage
{
	/**
	 * constructor
	 */
	public DataGridPage()
	{
		List<ICellPopulator<Contact>> columns = new ArrayList<>();

		columns.add(new PropertyPopulator<Contact>("id"));
		columns.add(new PropertyPopulator<Contact>("firstName"));
		columns.add(new PropertyPopulator<Contact>("lastName"));
		columns.add(new PropertyPopulator<Contact>("homePhone"));
		columns.add(new PropertyPopulator<Contact>("cellPhone"));

		add(new DataGridView<>("rows", columns, new SortableContactDataProvider()));
	}
}
