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

import java.util.List;

import org.apache.wicket.extensions.markup.html.repeater.data.table.DefaultDataTable;
import org.apache.wicket.extensions.markup.html.repeater.data.table.IColumn;

/**
 * A data table that shows the attributes of the recently stored pages in the data store. The last
 * used pages are rendered first.
 */
class BrowserTable extends DefaultDataTable<PageWindowDescription, String>
{

	/**
	 * Construct.
	 * 
	 * @param id
	 *            the component id
	 * @param columns
	 *            the columns that show the page attributes
	 * @param provider
	 *            the provider of page descriptions
	 */
	public BrowserTable(String id, List<IColumn<PageWindowDescription, String>> columns,
		PageWindowProvider provider)
	{
		super(id, columns, provider, 20);
	}

}
