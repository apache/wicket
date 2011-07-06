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
package org.apache.wicket.devutils.diskstore.browser;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.model.Model;

/**
 * A panel that shows the data about pages in the data store
 */
public class BrowserPanel extends Panel
{

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 */
	public BrowserPanel(String id)
	{
		super(id);

		BrowserTable table = createTable("table");
		add(table);
	}

	private BrowserTable createTable(String id)
	{
		PageWindowProvider provider = new PageWindowProvider();

		List<IColumn<PageWindowDescription>> columns = new ArrayList<IColumn<PageWindowDescription>>();

		PageWindowColumn pageIdColumn = new PageWindowColumn(Model.of("Id"), "id");
		columns.add(pageIdColumn);

		PageWindowColumn pageNameColumn = new PageWindowColumn(Model.of("Name"), "name");
		columns.add(pageNameColumn);

		PageWindowColumn pageSizeColumn = new PageWindowColumn(Model.of("Size"), "size");
		columns.add(pageSizeColumn);

		return new BrowserTable(id, columns, provider);
	}

}
