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

import org.apache.wicket.extensions.ajax.markup.html.repeater.data.table.AjaxFallbackDefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.grid.ICellPopulator;
import org.apache.wicket.extensions.markup.html.repeater.data.table.AbstractColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.extensions.markup.html.repeater.data.table.PropertyColumn;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;


/**
 * @author Martijn Dashorst
 */
public class AjaxDataTablePage extends BasePage
{
	/**
	 * Constructor.
	 */
	public AjaxDataTablePage()
	{
		List<IColumn<Contact>> columns = new ArrayList<IColumn<Contact>>();

		columns.add(new AbstractColumn<Contact>(new Model<String>("Actions"))
		{
			public void populateItem(Item<ICellPopulator<Contact>> cellItem, String componentId,
				IModel<Contact> model)
			{
				cellItem.add(new ActionPanel(componentId, model));
			}
		});

		columns.add(new PropertyColumn<Contact>(new Model<String>("ID"), "id"));
		columns.add(new PropertyColumn<Contact>(new Model<String>("First Name"), "firstName",
			"firstName"));
		columns.add(new PropertyColumn<Contact>(new Model<String>("Last Name"), "lastName",
			"lastName"));
		columns.add(new PropertyColumn<Contact>(new Model<String>("Home Phone"), "homePhone"));
		columns.add(new PropertyColumn<Contact>(new Model<String>("Cell Phone"), "cellPhone"));

		add(new AjaxFallbackDefaultDataTable<Contact, String>("table", columns,
			new SortableContactDataProvider(), 8));
	}
}
