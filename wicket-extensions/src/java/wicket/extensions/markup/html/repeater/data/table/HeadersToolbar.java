/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.extensions.markup.html.repeater.data.table;

import wicket.extensions.markup.html.repeater.OrderedRepeatingView;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.markup.html.WebMarkupContainer;

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

		OrderedRepeatingView headers = new OrderedRepeatingView("headers");
		add(headers);
		IColumn[] cols = table.getColumns();

		for (int i = 0; i < cols.length; i++)
		{
			// TODO is this extra component really necessary? can we not simply
			// use the repeater's body without the need for the id in the
			// markup?
			WebMarkupContainer item = new WebMarkupContainer(headers.newChildId());
			headers.add(item);

			IColumn column = cols[i];
			WebMarkupContainer header = null;
			if (column.isSortable())
			{
				header = new OrderByBorder("header", column.getSortProperty(), stateLocator)
				{

					private static final long serialVersionUID = 1L;

					protected void onSortChanged()
					{
						table.setCurrentPage(0);
					}
				};

			}
			else
			{
				header = new WebMarkupContainer("header");
			}
			item.add(header);
			header.add(column.getHeader("label"));
		}

	}

}
