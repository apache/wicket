/*
 * $Id$ $Revision:
 * 1.26 $ $Date$
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

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.markup.MarkupStream;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.link.Link;
import wicket.model.IModel;
import wicket.model.Model;

/**
 * A ListView holds ListItems of information. The listItem can be re-ordered and
 * deleted, either one at a time or many at a time.
 * <p>
 * Example:
 * 
 * <pre>
 *       &lt;tbody&gt;
 *         &lt;tr id=&quot;wicket-rows&quot; class=&quot;even&quot;&gt;
 *             &lt;td&gt;&lt;span id=&quot;wicket-id&quot;&gt;Test ID&lt;/span&gt;&lt;/td&gt;
 *         ...    
 * </pre>
 * 
 * <p>
 * Though this example is about a HTML table, ListView is not at all limited to
 * HTML tables. Any kind of list can be rendered using ListView.
 * <p>
 * And the related Java code:
 * 
 * <pre>
 * add(new ListView(&quot;rows&quot;, listData)
 * {
 * 	public void populateItem(final ListItem item)
 * 	{
 * 		final UserDetails user = (UserDetails)item.getModelObject();
 * 		cell.add(new Label(&quot;id&quot;, user.getId()));
 * 	}
 * });
 * </pre>
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 */
public abstract class ListView extends WebMarkupContainer
{
	/** Log. */
	private static Log log = LogFactory.getLog(ListView.class);

	/** Index of the first listItem to show */
	private int firstIndex = 0;

	/**
	 * If true, re-rendering the list view is more efficient if the window
	 * doesn't get changed at all or if it gets scrolled (compared to paging).
	 * But if you modify the listView model object, than you must manually call
	 * listView.removeAll() in order to rebuild the ListItems.
	 * 
	 * TODO this could go away if we were able to automatically detect changes
	 * to the underlying list. May be a delegate List could do?
	 */
	private boolean optimizeRenderProcess = false;

	/** Max number (not index) of listItems to show */
	private int viewSize = Integer.MAX_VALUE;

	/**
	 * @see wicket.Component#Component(String, IModel)
	 */
	public ListView(final String id, final IModel model)
	{
		super(id, model);

		if (model == null)
		{
			throw new IllegalArgumentException(
					"null models are not allowed. You may a Loop type instead");
		}

		// A reasonable default for viewSize can not be determined right now,
		// because list items might be added or removed until ListView
		// gets rendered.
	}

	/**
	 * @param id
	 *            See Component
	 * @param list
	 *            List to cast to Serializable
	 * @see wicket.Component#Component(String, IModel)
	 */
	public ListView(final String id, final List list)
	{
		this(id, new Model((Serializable)list));
	}

	/**
	 * Gets the list of items in the listView. This method is final because it
	 * is not designed to be overridden. If it were allowed to be overridden,
	 * the values returned by getModelObject() and getList() might not coincide.
	 * 
	 * @return The list of items in this list view.
	 */
	public final List getList()
	{
		final List list = (List)getModelObject();
		if (list == null)
		{
			return Collections.EMPTY_LIST;
		}
		return list;
	}

	/**
	 * Get index of first cell in page. Default is: 0.
	 * 
	 * @return Index of first cell in page. Default is: 0
	 */
	public final int getStartIndex()
	{
		return this.firstIndex;
	}

	/**
	 * Based on the model object's list size, firstIndex and view size,
	 * determine what the view size really will be. E.g. default for viewSize is
	 * Integer.MAX_VALUE, if not set via setViewSize(). If the underlying list
	 * has 10 elements, the value returned by getViewSize() will be 10 if
	 * startIndex = 0.
	 * 
	 * @return The number of listItems to be populated and rendered.
	 */
	public int getViewSize()
	{
		int size = this.viewSize;

		final Object modelObject = getModelObject();
		if (modelObject == null)
		{
			return size == Integer.MAX_VALUE ? 0 : size;
		}

		// Adjust view size to model object's list size
		final int modelSize = getList().size();
		if (firstIndex > modelSize)
		{
			return 0;
		}

		if ((size == Integer.MAX_VALUE) || ((firstIndex + size) > modelSize))
		{
			size = modelSize - firstIndex;
		}

		// firstIndex + size must be smaller than Integer.MAX_VALUE
		if ((Integer.MAX_VALUE - size) < firstIndex)
		{
			throw new IllegalStateException(
					"firstIndex + size must be smaller than Integer.MAX_VALUE");
		}

		return size;
	}

	/**
	 * If true re-rendering the list view is more efficient if the windows
	 * doesn't get changed at all or if it gets scrolled (compared to paging).
	 * But if you modify the listView model object, than you must manually call
	 * listView.removeAll() in order to rebuild the ListItems.
	 * 
	 * @return Returns the optimizeRenderProcess.
	 */
	public boolean getOptimizeRenderProcess()
	{
		return optimizeRenderProcess;
	}

	/**
	 * Returns a link that will move the given listItem "down" (towards the end)
	 * in the listView.
	 * 
	 * @param id
	 *            Name of move-down link component to create
	 * @param item
	 * @return The link component
	 */
	public final Link moveDownLink(final String id, final ListItem item)
	{
		final int index = getList().indexOf(item.getModelObject());
		final Link link = new Link(id)
		{
			public void onClick()
			{
				// Swap list items and invalidate listView
				Collections.swap(getList(), index, index + 1);

				// Make sure you re-render the list properly
				ListView.this.removeAll();
			}
		};

		if (index == (getList().size() - 1))
		{
			link.setVisible(false);
		}

		return link;
	}

	/**
	 * Returns a link that will move the given listItem "up" (towards the
	 * beginning) in the listView.
	 * 
	 * @param id
	 *            Name of move-up link component to create
	 * @param item
	 * @return The link component
	 */
	public final Link moveUpLink(final String id, final ListItem item)
	{
		final int index = getList().indexOf(item.getModelObject());
		final Link link = new Link(id)
		{
			public void onClick()
			{
				// Swap listItems and invalidate listView
				Collections.swap(getList(), index, index - 1);

				// Make sure you re-render the list properly
				ListView.this.removeAll();
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
	 * @param id
	 *            Name of remove link component to create
	 * @param item
	 * @return The link component
	 */
	public final Link removeLink(final String id, final ListItem item)
	{
		return new Link(id)
		{
			public void onClick()
			{
				// Remove listItem and invalidate listView
				getList().remove(item.getModelObject());

				// Make sure you re-render the list properly
				ListView.this.removeAll();
			}
		};
	}

	/**
	 * @see #getOptimizeRenderProcess()
	 * @param optimizeRenderProcess
	 *            The optimizeRenderProcess to set.
	 */
	public void setOptimizeRenderProcess(boolean optimizeRenderProcess)
	{
		this.optimizeRenderProcess = optimizeRenderProcess;
	}

	/**
	 * Set the index of the first listItem to render
	 * 
	 * @param startIndex
	 *            First index of model object's list to display
	 * @return This
	 */
	public ListView setStartIndex(final int startIndex)
	{
		this.firstIndex = startIndex;

		if (firstIndex < 0)
		{
			firstIndex = 0;
		}
		else if (firstIndex > getList().size())
		{
			firstIndex = 0;
		}

		return this;
	}

	/**
	 * Define the maximum number of listItems to render. Default: render all.
	 * 
	 * @param size
	 *            Number of listItems to display
	 * @return This
	 */
	public ListView setViewSize(final int size)
	{
		this.viewSize = size;

		if (viewSize < 0)
		{
			viewSize = Integer.MAX_VALUE;
		}

		return this;
	}

	/**
	 * Subclasses may provide their own ListItemModel with extended
	 * functionality. The default ListItemModel works fine with mostly static
	 * lists where index remains valid. In cases where the underlying list
	 * changes a lot (many users using the application), it may not longer be
	 * appropriate. In that case your own ListItemModel implementation should
	 * use an id (e.g. the database' record id) to identify and load the list
	 * item model object.
	 * 
	 * @param listViewModel
	 *            The ListView's model
	 * @param index
	 *            The list item index
	 * @return The ListItemModel created
	 */
	protected IModel getListItemModel(final IModel listViewModel, final int index)
	{
		return new ListItemModel(listViewModel, index);
	}

	/**
	 * @see wicket.Component#initModel()
	 */
	protected IModel initModel()
	{
		return new Model(null);
	}

	/**
	 * Create a new ListItem for list item at index.
	 * 
	 * @param index
	 * @return ListItem
	 */
	protected ListItem newItem(final int index)
	{
		return new ListItem(index, getListItemModel(getModel(), index));
	}

	/**
	 * Comes handy for ready made ListView based components which must implement
	 * populateItem() but you don't want to lose compile time error checking
	 * reminding the user to implement abstract populateItem().
	 * 
	 * @param listItem
	 */
	protected void onBeginPopulateItem(final ListItem listItem)
	{
	}

	/**
	 * Renders this ListView (container).
	 */
	protected void onRender()
	{
		// Ask parents for markup stream to use
		final MarkupStream markupStream = findMarkupStream();

		// Save position in markup stream
		final int markupStart = markupStream.getCurrentIndex();

		// Get number of listItems to be displayed
		final int size = getViewSize();
		if (size > 0)
		{
			if (optimizeRenderProcess == false)
			{
				// Automatically rebuild all ListItems before rendering the
				// list view
				removeAll();
			}
			else
			{
				// Remove all ListItems no longer required
				final int maxIndex = firstIndex + size;
				for (final Iterator iterator = iterator(); iterator.hasNext();)
				{
					// Get next child component
					final ListItem child = (ListItem)iterator.next();
					if (child != null)
					{
						final int index = child.getIndex();
						if (index < firstIndex || index >= maxIndex)
						{
							iterator.remove();
						}
					}
				}
			}

			// Loop through the markup in this container for each item 
			for (int i = 0; i < size; i++)
			{
				// Get index
				final int index = firstIndex + i;

				// If this component does not already exist, populate it
				ListItem listItem = (ListItem)get(Integer.toString(index));
				if (listItem == null)
				{
					// Create listItem for index
					listItem = newItem(index);

					// Populate the list item
					onBeginPopulateItem(listItem);
					populateItem(listItem);

					// Add list item
					add(listItem);
				}

				// Rewind to start of markup for kids
				markupStream.setCurrentIndex(markupStart);

				// Render
				renderItem(listItem, i >= (size - 1));
			}
		}
		else
		{
			removeAll();
			markupStream.skipComponent();
		}
	}

	/**
	 * Populate a given listItem.
	 * 
	 * @param listItem
	 *            The listItem to populate
	 */
	protected abstract void populateItem(final ListItem listItem);

	// TODO Remove lastItem boolean?
	
	/**
	 * Render a single listItem.
	 * 
	 * @param listItem
	 *            The listItem to be rendered
	 * @param lastItem
	 *            True, if item is last listItem in listView
	 */
	protected void renderItem(final ListItem listItem, final boolean lastItem)
	{
		listItem.render();
	}
}
