/*
 * $Id: HeadersToolbar.java 5840 2006-05-24 13:49:09 -0700 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-24 23:52:28 +0000 (Wed, 24 May
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

import wicket.MarkupContainer;
import wicket.extensions.markup.html.repeater.RepeatingView;
import wicket.extensions.markup.html.repeater.data.sort.ISortStateLocator;
import wicket.extensions.markup.html.repeater.data.sort.OrderByBorder;
import wicket.extensions.markup.html.repeater.data.sort.OrderByLink;
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
	 * @param parent
	 *            parent component
	 * @param id
	 *            component id
	 * 
	 * @param table
	 *            data table this toolbar will be attached to
	 * @param stateLocator
	 *            locator for the ISortState implementation used by sortable
	 *            headers
	 */
	public HeadersToolbar(MarkupContainer parent, final String id, final DataTable table,
			final ISortStateLocator stateLocator)
	{
		super(parent, id, table);

		RepeatingView headers = new RepeatingView(this, "headers");
		IColumn[] cols = table.getColumns();

		for (IColumn column : cols)
		{
			// TODO Post 1.2: General: Is this extra component really necessary?
			// can we
			// not simply use the repeater's body without the need for the id in
			// the markup?
			WebMarkupContainer item = new WebMarkupContainer(headers, headers.newChildId());

			WebMarkupContainer header = null;
			if (column.isSortable())
			{
				header = new OrderByBorder(item, "header", column.getSortProperty(), stateLocator)
				{

					private static final long serialVersionUID = 1L;

					@Override
					protected void onSortChanged()
					{
						table.setCurrentPage(0);
					}

					@Override
					protected void onLinkCreated(OrderByLink link)
					{
						HeadersToolbar.this.onLinkCreated(link);
					}
				};

			}
			else
			{
				header = new WebMarkupContainer(item, "header");
			}
			item.setRenderBodyOnly(true);
			column.getHeader(header, "label");
		}

	}

	/**
	 * Callback method for when an {@link OrderByLink} object has been created
	 * by this toolbar. This callback can be used to, for example, add an ajax
	 * behavior to the link.
	 * 
	 * @param link
	 *            created link component
	 */
	protected void onLinkCreated(OrderByLink link)
	{
		// noop
	}


}
