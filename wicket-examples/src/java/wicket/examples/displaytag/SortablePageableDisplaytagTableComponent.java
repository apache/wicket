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

import wicket.MarkupContainer;
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
 * 
 * @param <T> 
 */
public class SortablePageableDisplaytagTableComponent<T> extends Panel
{
	// Model data
	final private List<T> data;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 * @param id
	 *            Name of component
	 * @param list
	 *            List of data to display
	 */
	public SortablePageableDisplaytagTableComponent(MarkupContainer parent, final String id,
			final List<T> list)
	{
		super(parent, id);

		// Get an internal copy of the model data
		this.data = new ArrayList<T>();
		this.data.addAll(list);

		// Add a table
		final SimplePageableListView table = new SimplePageableListView(this, "rows", list, 10);

		// Add a sortable header to the table
		new SortableListViewHeaders(this, "header", table)
		{
			@Override
			protected int compareTo(SortableListViewHeader header, Object o1, Object o2)
			{
				if (header.getId().equals("id"))
				{
					return ((ListObject)o1).getId() - ((ListObject)o2).getId();
				}

				return super.compareTo(header, o1, o2);
			}

			@Override
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
		};

		// Add a headline
		new TableHeaderLabel(this, "headline", table);

		// Add navigation
		new PagingNavigation(this, "navigation", table);

		// Add some navigation links
		new PagingNavigationLink(this, "first", table, 0);
		new PagingNavigationIncrementLink(this, "prev", table, -1);
		new PagingNavigationIncrementLink(this, "next", table, 1);
		new PagingNavigationLink(this, "last", table, table.getPageCount() - 1);
	}

	/**
	 * 
	 */
	public static class TableHeaderLabel extends Label
	{
		private final PageableListView listView;

		/**
		 * 
		 * @param parent
		 * @param id
		 * @param table
		 */
		public TableHeaderLabel(MarkupContainer parent, final String id,
				final PageableListView table)
		{
			super(parent, id, (IModel)null);
			this.listView = table;
		}

		@Override
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
