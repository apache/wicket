/*
 * $Id: SortablePageableDisplaytagTableComponent.java 5389 2006-04-16 09:24:09Z
 * jdonnerstag $ $Revision$ $Date: 2006-04-16 11:24:09 +0200 (So, 16 Apr
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
package wicket.examples.displaytag;

import java.util.ArrayList;
import java.util.List;

import wicket.examples.displaytag.list.SortableListViewHeader;
import wicket.examples.displaytag.list.SortableListViewHeaders;
import wicket.examples.displaytag.utils.ListObject;
import wicket.examples.displaytag.utils.SimplePageableListView;
import wicket.markup.ComponentTag;
import wicket.markup.MarkupStream;
import wicket.markup.html.basic.Label;
import wicket.markup.html.list.PageableListView;
import wicket.markup.html.navigation.paging.PagingNavigation;
import wicket.markup.html.navigation.paging.PagingNavigationIncrementLink;
import wicket.markup.html.navigation.paging.PagingNavigationLink;
import wicket.markup.html.panel.Panel;
import wicket.model.IModel;

/**
 * Sortable + pageable table example
 * 
 * @author Juergen Donnerstag
 */
public class SortablePageableDisplaytagTableComponent extends Panel
{
	// Model data
	final private List data;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            Name of component
	 * @param list
	 *            List of data to display
	 */
	public SortablePageableDisplaytagTableComponent(final String id, final List list)
	{
		super(id);

		// Get an internal copy of the model data
		this.data = new ArrayList();
		this.data.addAll(list);

		// Add a table
		final SimplePageableListView table = new SimplePageableListView("rows", list, 10);
		add(table);

		// Add a sortable header to the table
		add(new SortableListViewHeaders("header", table)
		{
			protected int compareTo(SortableListViewHeader header, Object o1, Object o2)
			{
				if (header.getId().equals("id"))
				{
					return ((ListObject)o1).getId() - ((ListObject)o2).getId();
				}

				return super.compareTo(header, o1, o2);
			}

			protected Comparable getObjectToCompare(final SortableListViewHeader header,
					final Object object)
			{
				final String name = header.getId();
				if (name.equals("name"))
				{
					return ((ListObject)object).getName();
				}
				if (name.equals("email"))
				{
					return ((ListObject)object).getEmail();
				}
				if (name.equals("status"))
				{
					return ((ListObject)object).getStatus();
				}
				if (name.equals("comment"))
				{
					return ((ListObject)object).getDescription();
				}

				return "";
			}
		});

		// Add a headline
		add(new TableHeaderLabel("headline", table));

		// Add navigation
		add(new PagingNavigation("navigation", table));

		// Add some navigation links
		add(new PagingNavigationLink("first", table, 0));
		add(new PagingNavigationIncrementLink("prev", table, -1));
		add(new PagingNavigationIncrementLink("next", table, 1));
		add(new PagingNavigationLink("last", table, table.getPageCount() - 1));
	}

	/**
	 * 
	 */
	public static class TableHeaderLabel extends Label
	{
		private final PageableListView listView;

		/**
		 * 
		 * @param id
		 * @param table
		 */
		public TableHeaderLabel(final String id, final PageableListView table)
		{
			super(id, (IModel)null);
			this.listView = table;
		}

		protected void onComponentTagBody(final MarkupStream markupStream,
				final ComponentTag openTag)
		{
			int firstCell = listView.getCurrentPage() * listView.getRowsPerPage();

			String text = String.valueOf(listView.size()) + " items found, displaying "
					+ String.valueOf(firstCell + 1) + " to "
					+ String.valueOf(firstCell + listView.getRowsPerPage()) + ".";

			replaceComponentTagBody(markupStream, openTag, text);
		}
	}
}
