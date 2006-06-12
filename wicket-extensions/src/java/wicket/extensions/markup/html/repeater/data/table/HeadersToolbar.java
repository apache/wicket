/*
 * $Id: HeadersToolbar.java 5279 2006-04-06 15:37:06 +0000 (Thu, 06 Apr 2006)
 * ivaynberg $ $Revision$ $Date: 2006-04-06 15:37:06 +0000 (Thu, 06 Apr
 * 2006) $
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.extensions.markup.html.repeater.data.table;

import java.util.Iterator;

import wicket.extensions.markup.html.repeater.RepeatingView;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.extensions.markup.html.repeater.refreshing.Item;
import wicket.extensions.markup.html.repeater.refreshing.RefreshingView;
import wicket.extensions.markup.html.repeater.util.ArrayIteratorAdapter;
import wicket.markup.html.WebMarkupContainer;
import wicket.model.IModel;
import wicket.model.Model;

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


		RefreshingView headers = new RefreshingView("headers")
		{
			private static final long serialVersionUID = 1L;

			protected Iterator getItemModels()
			{
				return new ArrayIteratorAdapter(table.getColumns())
				{

					protected IModel model(Object object)
					{
						return new Model((IColumn)object);
					}

				};
			}

			protected void populateItem(Item item)
			{
				IColumn column = (IColumn)item.getModelObject();
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

		};
		add(headers);
	}

	protected WebMarkupContainer newSortableHeader(String borderId, String property,
			ISortStateLocator locator)
	{
		return new OrderByBorder("header", property, locator)
		{

			private static final long serialVersionUID = 1L;

			protected void onSortChanged()
			{
				getTable().setCurrentPage(0);
			}
		};

	}

}
