/*
 * $Id$ $Revision:
 * 1.7 $ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
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

import java.util.Collections;

import wicket.WicketRuntimeException;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.link.Link;
import wicket.model.AbstractModel;
import wicket.model.IModel;

/**
 * Items of the ListView.
 * 
 * @author Jonathan Locke
 */
public class ListItem extends WebMarkupContainer
{
	/** The index of the ListItem in the parent listView */
	private final int index;

	/** The parent ListView, the ListItem is part of. */
	private ListView listView;
	
	/**
	 * Model for list items.
	 */
	private class ListItemModel extends AbstractModel
	{
		/**
		 * @see IModel#getObject()
		 */
		public Object getObject()
		{
			return listView.getListObject(index);
		}

		/**
		 * @see IModel#setObject(Object)
		 */
		public void setObject(Object object)
		{
			throw new WicketRuntimeException("Can't set an object through a ListItem");
		}
	}

	/**
	 * A constructor which uses the index and the list provided to create a
	 * ListItem. This constructor is the default one.
	 * 
	 * @param listView
	 *            The listView that holds this listItem
	 * @param index
	 *            The listItem number
	 */
	protected ListItem(final ListView listView, final int index)
	{
		super(Integer.toString(index));
		this.listView = listView;
		this.index = index;
	}

	/**
	 * Gets the index of the listItem in the parent listView.
	 * 
	 * @return The index of this listItem in the parent listView
	 */
	public final int getIndex()
	{
		return index;
	}

	/**
	 * Convenience method for ListViews with alternating style for colouring.
	 * 
	 * @return True, if index is even index % 2 == 0
	 */
	public final boolean isEvenIndex()
	{
		return getIndex() % 2 == 0;
	}

	/**
	 * Gets if this listItem is the first listItem in the containing listView.
	 * 
	 * @return True if this listItem is the first listItem in the containing
	 *         listView
	 */
	public final boolean isFirst()
	{
		return index == 0;
	}

	/**
	 * Gets whether this listItem is the last listItem in the containing
	 * listView.
	 * 
	 * @return True if this listItem is the last listItem in the containing
	 *         listView.
	 */
	public final boolean isLast()
	{
		return index == listView.getList().size() - 1;
	}

	/**
	 * Returns a link that will move the given listItem "down" (towards the end)
	 * in the listView.
	 * 
	 * @param componentName
	 *            Name of move-down link component to create
	 * @return The link component
	 */
	public final Link moveDownLink(final String componentName)
	{
		final Link link = new Link(componentName)
		{
			public void onClick()
			{
				// Swap list items and invalidate listView
				Collections.swap(listView.getList(), index, index + 1);
				listView.modelChangedStructure();
			}
		};

		if (index == (listView.getList().size() - 1))
		{
			link.setVisible(false);
		}

		return link;
	}

	/**
	 * Returns a link that will move the given listItem "up" (towards the
	 * beginning) in the listView.
	 * 
	 * @param componentName
	 *            Name of move-up link component to create
	 * @return The link component
	 */
	public final Link moveUpLink(final String componentName)
	{
		final Link link = new Link(componentName)
		{
			public void onClick()
			{
				// Swap listItems and invalidate listView
				Collections.swap(listView.getList(), index, index - 1);
				listView.modelChangedStructure();
			}
		};

		if (index == 0)
		{
			link.setVisible(false);
		}

		return link;
	}

	/**
	 * Returns a link that will remove this ListItem from the ListView that
	 * holds it.
	 * 
	 * @param componentName
	 *            Name of remove link component to create
	 * @return The link component
	 */
	public final Link removeLink(final String componentName)
	{
		return new Link(componentName)
		{
			public void onClick()
			{
				// Remove listItem and invalidate listView
				listView.getList().remove(index);
				listView.modelChangedStructure();
			}
		};
	}

	/**
	 * Get the listView that holds this cell.
	 * 
	 * @return Returns the list view.
	 */
	protected final ListView getListView()
	{
		return listView;
	}
	
	/**
	 * @see wicket.Component#initModel()
	 */
	protected IModel initModel()
	{
		return new ListItemModel();
	}
}
