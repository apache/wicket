/*
 * $Id: SortableListViewHeaderGroup.java,v 1.2 2005/02/10 18:01:38 jonathanlocke
 * Exp $ $Revision$ $Date$
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
package wicket.markup.html.list;

import java.util.List;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.util.collections.MicroMap;

/**
 * Maintains a group of sortable list view headers. By means of this group you
 * change and maintain the information about which column shall be sorted. It
 * also provides support for the list view to change the style of the header
 * depending on its status.
 * 
 * @see SortableListViewHeaderGroup
 * @see SortableListViewHeaders
 * @author Juergen Donnerstag
 */
public class SortableListViewHeaderGroup
{
	/** The html container the header must be added to */
	private MarkupContainer container;

	/** The underlying listView to be sorted */
	private ListView listView;
	
	/** contains the name of SortableTableHeader to be sorted */
	final private MicroMap sorted = new MicroMap();

	/**
	 * Maintain a group SortableTableHeader
	 * 
	 * @param container
	 *            The html container the header will be added to
	 * @param listView
	 *            The underlying ListView
	 */
	public SortableListViewHeaderGroup(final MarkupContainer container, final ListView listView)
	{
		this.listView = listView;
		this.container = container;
	}

	/**
	 * Get CSS style for a header. May be subclassed for company standards
	 * 
	 * @param name
	 *            The headers component name
	 * @param ascending
	 *            Sorting order
	 * @return The CSS style to be applied to the tag's class attribute
	 */
	protected final String getCssClass(final String name, final boolean ascending)
	{
		// TODO Integrate this with whatever CSS solution we come up with
		if (isSorted(name))
		{
			return (ascending ? "order2" : "order1") + " sortable sorted";
		}

		return null;
	}

	/**
	 * Get the list views's model data
	 * 
	 * @return the list view's underlying list
	 */
	protected List getList()
	{
		return listView.getList();
	}

	/**
	 * True if column with name shall be sorted
	 * 
	 * @param name
	 *            column name
	 * @return True, if column must be sorted
	 */
	protected final boolean isSorted(final String name)
	{
		return sorted.containsKey(name);
	}

	/**
	 * Called by SortableListViewHeader and may be subclassed for company
	 * standards.
	 * 
	 * @param tag
	 *            component tag
	 * @param style
	 *            CSS style
	 */
	protected void onComponentTag(final ComponentTag tag, final String style)
	{
		if (style != null)
		{
			tag.put("class", style);
		}
	}

	/**
	 * Set the column to be sorted
	 * 
	 * @param name
	 *            SortableTableHeader component name
	 */
	protected final void setSortedColumn(final String name)
	{
		sorted.clear();
		sorted.put(name, null);
		listView.invalidateModel();
	}
}
