/*
 * $Id: SortableListViewHeader.java 5838 2006-05-24 20:44:49 +0000 (Wed, 24 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:44:49 +0000 (Wed, 24
 * May 2006) $
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
package wicket.examples.displaytag.list;

import java.util.Collections;
import java.util.Comparator;

import wicket.MarkupContainer;
import wicket.markup.ComponentTag;
import wicket.markup.html.border.Border;
import wicket.markup.html.link.Link;


/**
 * Sortable list view header component for a single list view column.
 * Functionality provided includes sorting the underlying list view and changing
 * the colours (style) of the header.
 * 
 * @see SortableListViewHeaderGroup
 * @see SortableListViewHeaders
 * @author Juergen Donnerstag
 */
public abstract class SortableListViewHeader extends Border
{
	/** Sort ascending or descending */
	private boolean ascending;

	/** All sortable columns of a single list view are grouped */
	private final SortableListViewHeaderGroup group;

	/**
	 * Construct.
	 * 
	 * @param parent
	 * @param id
	 *            The component's id
	 * @param group
	 *            The group of headers the new one will be added to
	 */
	public SortableListViewHeader(MarkupContainer parent, final String id,
			final SortableListViewHeaderGroup group)
	{
		super(parent, id);

		// Default to descending.
		this.ascending = false;
		this.group = group;

		// If user clicks on the header, sorting will reverse
		new Link(this, "actionLink")
		{
			@Override
			public void onClick()
			{
				// call SortableTableHeaders implementation
				SortableListViewHeader.this.linkClicked();
			}
		};
	}

	/**
	 * Compare two objects (list elements of list view's model object). Both
	 * objects must implement Comparable. In order to compare basic types like
	 * int or double, simply subclass the method.
	 * 
	 * @param o1
	 *            first object
	 * @param o2
	 *            second object
	 * @return comparision result
	 */
	protected int compareTo(Object o1, Object o2)
	{
		Comparable obj1 = getObjectToCompare(o1);
		Comparable obj2 = getObjectToCompare(o2);
		return obj1.compareTo(obj2);
	}

	/**
	 * Get CSS style for the header based on ascending / descending. Delegate to
	 * the header group to determine the style.
	 * 
	 * @return css class
	 */
	protected final String getCssClass()
	{
		// TODO General: This needs to be integrated with our CSS design
		return group.getCssClass(getId(), ascending);
	}

	/**
	 * Returns the comparable object of the list view the header/column is
	 * referring to, e.g. obj.getId();
	 * 
	 * @param object
	 *            the ListItems model object
	 * @return The object to compare
	 */
	protected Comparable getObjectToCompare(Object object)
	{
		return (Comparable)object;
	}

	/**
	 * Header has been clicked. Define what to do.
	 */
	protected void linkClicked()
	{
		// change sorting order: ascending <-> descending
		ascending = !ascending;

		// Tell the header group that something has changed
		group.setSortedColumn(getId());

		// sort the list view's model data accordingly
		sort();
	}

	/**
	 * Delegate to the header group to handle the tag.
	 * 
	 * @see wicket.Component#onComponentTag(wicket.markup.ComponentTag)
	 * @param tag
	 *            The current ComponentTag to handle
	 */
	@Override
	protected void onComponentTag(final ComponentTag tag)
	{
		group.handleComponentTag(tag, getCssClass());
	}

	/**
	 * Sort list view's model object based on column data
	 */
	protected void sort()
	{
		Collections.sort(group.getListViewModelObject(), new Comparator()
		{
			public int compare(Object o1, Object o2)
			{
				if (ascending)
				{
					return compareTo(o1, o2);
				}
				else
				{
					return compareTo(o2, o1);
				}
			}
		});
	}
}
