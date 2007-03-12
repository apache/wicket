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
package wicket.extensions.markup.html.repeater.data.table;

import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.repeater.RepeatingView;

/**
 * Toolbars that displays column headers. If the column is sortable a sortable
 * header will be displayed.
 * 
 * @see DefaultDataTable
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class HeadersToolbar extends AbstractToolbar
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 * @param stateLocator
	 *            locator for the ISortState implementation used by sortable
	 *            headers
	 */
	public HeadersToolbar(final DataTable table, final ISortStateLocator stateLocator)
	{
		super(table);


		RepeatingView headers = new RepeatingView("headers");
		add(headers);

		final IColumn[] columns = table.getColumns();
		for (int i = 0; i < columns.length; i++)
		{
			final IColumn column = columns[i];

			WebMarkupContainer item = new WebMarkupContainer(headers.newChildId());
			headers.add(item);

			WebMarkupContainer header = null;
			if (column.isSortable())
			{
				header = newSortableHeader("header", column.getSortProperty(), stateLocator);
			}
			else
			{
				header = new WebMarkupContainer("header");
			}
			item.add(header);
			item.setRenderBodyOnly(true);
			header.add(column.getHeader("label"));

		}
	}

	/**
	 * Factory method for sortable header components. A sortable header
	 * component must have id of <code>headerId</code> and conform to markup
	 * specified in <code>HeadersToolbar.html</code>
	 * 
	 * @param headerId
	 *            header component id
	 * @param property
	 *            propert this header represents
	 * @param locator
	 *            sort state locator
	 * @return created header component
	 */
	protected WebMarkupContainer newSortableHeader(String headerId, String property,
			ISortStateLocator locator)
	{
		return new OrderByBorder(headerId, property, locator)
		{

			private static final long serialVersionUID = 1L;

			protected void onSortChanged()
			{
				getTable().setCurrentPage(0);
			}
		};

	}

}
