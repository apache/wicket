/*
 * $Id: ListView.java 5343 2006-04-12 00:58:25 -0700 (Wed, 12 Apr 2006)
 * gwynevans $ $Revision$ $Date: 2006-04-12 00:58:25 -0700 (Wed, 12 Apr
 * 2006) $
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

import wicket.Component;
import wicket.MarkupContainer;
import wicket.markup.MarkupStream;
import wicket.markup.html.link.Link;
import wicket.model.IModel;
import wicket.model.Model;
import wicket.version.undo.Change;

/**
 * A ListView holds ListItem children. Items can be re-ordered and deleted,
 * either one at a time or many at a time.
 * <p>
 * Example:
 * 
 * <pre>
 *                   &lt;tbody&gt;
 *                     &lt;tr wicket:id=&quot;rows&quot; class=&quot;even&quot;&gt;
 *                         &lt;td&gt;&lt;span wicket:id=&quot;id&quot;&gt;Test ID&lt;/span&gt;&lt;/td&gt;
 *                     ...    
 * </pre>
 * 
 * <p>
 * Though this example is about a HTML table, ListView is not at all limited to
 * HTML tables. Any kind of list can be rendered using ListView.
 * <p>
 * The related Java code:
 * 
 * <pre>
 * add(new ListView(&quot;rows&quot;, listData)
 * {
 * 	public void populateItem(final ListItem item)
 * 	{
 * 		final UserDetails user = (UserDetails)item.getModelObject();
 * 		item.add(new Label(&quot;id&quot;, user.getId()));
 * 	}
 * });
 * </pre>
 * 
 * <p>
 * WARNING: though you can nest ListViews within Forms, you HAVE to set the
 * setReuseItems property to true in order to have validation work properly. By
 * default, reuseItems is false, which has the effect that ListView replaces all
 * child components by new instances. The idea behind this is that you always
 * render the fresh data, and as people usually use ListViews for displaying
 * read-only lists (at least, that's what we think), this is good default
 * behavior. <br />
 * However, as the components are replaced before the rendering starts, the
 * search for specific messages for these components fails as they are replaced
 * with other instances. Another problem is that 'wrong' user input is kept as
 * (temporary) instance data of the components. As these components are replaced
 * by new ones, your user will never see the wrong data when reuseItems is
 * false.
 * </p>
 * 
 * @param <T>
 *            Type of model object this component holds
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Eelco Hillenius
 */
public abstract class ListView<T> extends AbstractRepeater<List<T>>
{
	/** Index of the first item to show */
	private int firstIndex = 0;

	/**
	 * If true, re-rendering the list view is more efficient if the window
	 * doesn't get changed at all or if it gets scrolled (compared to paging).
	 * But if you modify the listView model object, than you must manually call
	 * listView.removeAll() in order to rebuild the ListItems. If you nest a
	 * ListView in a Form, ALWAYS set this property to true, as otherwise
	 * validation will not work properly.
	 */
	private boolean reuseItems = false;

	/** Max number (not index) of items to show */
	private int viewSize = Integer.MAX_VALUE;

	/**
	 * @see wicket.Component#Component(MarkupContainer,String)
	 */
	public ListView(MarkupContainer parent, final String id)
	{
		super(parent, id);
	}

	/**
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public ListView(MarkupContainer parent, final String id, final IModel<List<T>> model)
	{
		super(parent, id, model);

		if (model == null)
		{
			throw new IllegalArgumentException(
					"Null models are not allowed. If you have no model, you may prefer a Loop instead");
		}

		// A reasonable default for viewSize can not be determined right now,
		// because list items might be added or removed until ListView
		// gets rendered.
	}

	/**
	 * @param parent
	 *            The parent of this component
	 * @param id
	 *            See Component
	 * @param list
	 *            List to cast to Serializable
	 * @see wicket.Component#Component(MarkupContainer,String, IModel)
	 */
	public ListView(MarkupContainer parent, final String id, final List<T> list)
	{
		this(parent, id, new Model<List<T>>(list));
	}

	/**
	 * Gets the list of items in the listView. This method is final because it
	 * is not designed to be overridden. If it were allowed to be overridden,
	 * the values returned by getModelObject() and getList() might not coincide.
	 * 
	 * @return The list of items in this list view.
	 */
	public final List<T> getList()
	{
		final List<T> list = getModelObject();
		if (list == null)
		{
			return Collections.emptyList();
		}
		return list;
	}

	/**
	 * If true re-rendering the list view is more efficient if the windows
	 * doesn't get changed at all or if it gets scrolled (compared to paging).
	 * But if you modify the listView model object, than you must manually call
	 * listView.removeAll() in order to rebuild the ListItems. If you nest a
	 * ListView in a Form, ALLWAYS set this property to true, as otherwise
	 * validation will not work properly.
	 * 
	 * @return Whether to reuse items
	 */
	public boolean getReuseItems()
	{
		return reuseItems;
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
	 * @return The number of items to be populated and rendered.
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
	 * Returns a link that will move the given item "down" (towards the end) in
	 * the listView.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            Name of move-down link component to create
	 * @param item
	 * @return The link component
	 */
	public final Link moveDownLink(MarkupContainer parent, final String id, final ListItem<T> item)
	{
		return new Link(item, id)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see wicket.Component#onAttach()
			 */
			@Override
			protected void onAttach()
			{
				setAutoEnable(false);
				if (getList().indexOf(item.getModelObject()) == (getList().size() - 1))
				{
					setEnabled(false);
				}
			}

			/**
			 * @see wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				final int index = getList().indexOf(item.getModelObject());
				if (index != -1)
				{
					addStateChange(new Change()
					{
						private static final long serialVersionUID = 1L;

						final int oldIndex = index;

						@Override
						public void undo()
						{
							Collections.swap(getList(), oldIndex + 1, oldIndex);
						}

					});

					// Swap list items and invalidate listView
					Collections.swap(getList(), index, index + 1);
				}
			}
		};
	}

	/**
	 * Returns a link that will move the given item "up" (towards the beginning)
	 * in the listView.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            Name of move-up link component to create
	 * @param item
	 * @return The link component
	 */
	public final Link moveUpLink(MarkupContainer parent, final String id, final ListItem<T> item)
	{
		return new Link(item, id)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see wicket.Component#onAttach()
			 */
			@Override
			protected void onAttach()
			{
				setAutoEnable(false);
				if (getList().indexOf(item.getModelObject()) == 0)
				{
					setEnabled(false);
				}
			}

			/**
			 * @see wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				final int index = getList().indexOf(item.getModelObject());
				if (index != -1)
				{

					addStateChange(new Change()
					{
						private static final long serialVersionUID = 1L;

						final int oldIndex = index;

						@Override
						public void undo()
						{
							Collections.swap(getList(), oldIndex - 1, oldIndex);
						}

					});

					// Swap items and invalidate listView
					Collections.swap(getList(), index, index - 1);
				}
			}
		};
	}

	/**
	 * Returns a link that will remove this ListItem from the ListView that
	 * holds it.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            Name of remove link component to create
	 * @param item
	 * @return The link component
	 */
	public final Link removeLink(MarkupContainer parent, final String id, final ListItem<T> item)
	{
		return new Link(item, id)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				addStateChange(new Change()
				{
					private static final long serialVersionUID = 1L;

					final T removedObject = item.getModelObject();
					final int oldIndex = getList().indexOf(item.getModelObject());

					@Override
					public void undo()
					{
						getList().add(oldIndex, removedObject);
					}

				});

				item.modelChanging();

				// Remove item and invalidate listView
				getList().remove(item.getModelObject());

				ListView.this.modelChanged();
			}
		};
	}

	/**
	 * If true re-rendering the list view is more efficient if the windows
	 * doesn't get changed at all or if it gets scrolled (compared to paging).
	 * But if you modify the listView model object, than you must manually call
	 * listView.removeAll() in order to rebuild the ListItems. If you nest a
	 * ListView in a Form, ALLWAYS set this property to true, as otherwise
	 * validation will not work properly.
	 * 
	 * @param reuseItems
	 *            Whether to reuse the child items.
	 * @return this
	 */
	public ListView setReuseItems(boolean reuseItems)
	{
		this.reuseItems = reuseItems;
		return this;
	}

	/**
	 * Set the index of the first item to render
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
	 * Define the maximum number of items to render. Default: render all.
	 * 
	 * @param size
	 *            Number of items to display
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
	 * Sets the model as the provided list and removes all children, so that the
	 * next render will be using the contents of the model.
	 * 
	 * @param list
	 *            The list for the new model. The list must implement
	 *            {@link Serializable}.
	 * @return This for chaining
	 */
	public Component setList(List<T> list)
	{
		return setModel(new Model<List<T>>(list));
	}

	/**
	 * Sets the model and removes all current children, so that the next render
	 * will be using the contents of the model.
	 * 
	 * @param model
	 *            The new model
	 * @return This for chaining
	 * 
	 * @see wicket.MarkupContainer#setModel(wicket.model.IModel)
	 */
	@Override
	public Component setModel(IModel<List<T>> model)
	{
		// remove all children; this has no effect when the list
		// didn't render before, as in that case the list view
		// does not yet have any children
		removeAll();
		return super.setModel(model);
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
	protected IModel<T> getListItemModel(final IModel<List<T>> listViewModel, final int index)
	{
		return new ListItemModel<T>(this, index);
	}

	/**
	 * @see wicket.MarkupContainer#internalOnAttach()
	 */
	@Override
	@SuppressWarnings("unchecked")
	protected void internalOnAttach()
	{
		// Get number of items to be displayed
		final int size = getViewSize();
		if (size > 0)
		{
			if (getReuseItems())
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
			else
			{
				// Automatically rebuild all ListItems before rendering the
				// list view
				removeAll();
			}

			// Loop through the markup in this container for each item
			for (int i = 0; i < size; i++)
			{
				// Get index
				final int index = firstIndex + i;

				// If this component does not already exist, populate it
				ListItem<T> item = (ListItem<T>)get(Integer.toString(index));
				if (item == null)
				{
					// Create item for index
					item = newItem(index);

					// Populate the list item
					onBeginPopulateItem(item);
					populateItem(item);
				}
			}
		}
		else
		{
			removeAll();
		}
	}

	/**
	 * Create a new ListItem for list item at index.
	 * 
	 * @param index
	 * @return ListItem
	 */
	protected ListItem<T> newItem(final int index)
	{
		return new ListItem<T>(this, index, getListItemModel(getModel(), index));
	}

	/**
	 * Comes handy for ready made ListView based components which must implement
	 * populateItem() but you don't want to lose compile time error checking
	 * reminding the user to implement abstract populateItem().
	 * 
	 * @param item
	 */
	protected void onBeginPopulateItem(final ListItem item)
	{
	}

	/**
	 * 
	 * @see wicket.Component#onRender(wicket.markup.MarkupStream)
	 */
	@Override
	protected void onRender(final MarkupStream markupStream)
	{
		// Save position in markup stream
		final int markupStart = markupStream.getCurrentIndex();

		// Get number of items to be displayed
		final int size = getViewSize();
		if (size > 0)
		{
			// Loop through the markup in this container for each item
			for (int i = 0; i < size; i++)
			{
				// Get index
				final int index = firstIndex + i;

				// Get list item for index
				ListItem item = (ListItem)get(Integer.toString(index));

				// Rewind to start of markup for kids
				markupStream.setCurrentIndex(markupStart);

				// Render
				renderItem(item);
			}
		}
		else
		{
			markupStream.skipComponent();
		}
	}

	/**
	 * Populate a given item.
	 * <p>
	 * <b>be carefull</b> to add any components to the list item. So, don't do:
	 * 
	 * <pre>
	 * add(new Label(&quot;foo&quot;, &quot;bar&quot;));
	 * </pre>
	 * 
	 * but:
	 * 
	 * <pre>
	 * item.add(new Label(&quot;foo&quot;, &quot;bar&quot;));
	 * </pre>
	 * 
	 * </p>
	 * 
	 * @param item
	 *            The item to populate
	 */
	protected abstract void populateItem(final ListItem<T> item);

	/**
	 * Render a single item.
	 * 
	 * @param item
	 *            The item to be rendered
	 */
	protected void renderItem(final ListItem item)
	{
		item.render(getMarkupStream());
	}

}
