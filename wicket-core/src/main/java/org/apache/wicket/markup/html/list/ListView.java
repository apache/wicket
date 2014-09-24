/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.markup.html.list;

import java.io.Serializable;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.apache.wicket.Component;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.collections.ReadOnlyIterator;


/**
 * A ListView is a repeater that makes it easy to display/work with {@link List}s. However, there
 * are situations where it is necessary to work with other collection types, for repeaters that
 * might work better with non-list or database-driven collections see the
 * org.apache.wicket.markup.repeater package.
 * 
 * Also notice that in a list the item's uniqueness/primary key/id is identified as its index in the
 * list. If this is not the case you should either override {@link #getListItemModel(IModel, int)}
 * to return a model that will work with the item's true primary key, or use a different repeater
 * that does not rely on the list index.
 * 
 * A ListView holds ListItem children. Items can be re-ordered and deleted, either one at a time or
 * many at a time.
 * <p>
 * Example:
 * 
 * <pre>
 * &lt;tbody&gt;
 *   &lt;tr wicket:id=&quot;rows&quot; class=&quot;even&quot;&gt;
 *     &lt;td&gt;&lt;span wicket:id=&quot;id&quot;&gt;Test ID&lt;/span&gt;&lt;/td&gt;
 *     ...
 * </pre>
 * 
 * <p>
 * Though this example is about a HTML table, ListView is not at all limited to HTML tables. Any
 * kind of list can be rendered using ListView.
 * <p>
 * The related Java code:
 * 
 * <pre>
 * add(new ListView&lt;UserDetails&gt;(&quot;rows&quot;, listData)
 * {
 * 	public void populateItem(final ListItem&lt;UserDetails&gt; item)
 * 	{
 * 		final UserDetails user = item.getModelObject();
 * 		item.add(new Label(&quot;id&quot;, user.getId()));
 * 	}
 * });
 * </pre>
 * 
 * <p>
 * <strong>NOTE:</strong>
 * 
 * When you want to change the default generated markup it is important to realize that the ListView
 * instance itself does not correspond to any markup, however, the generated ListItems do.<br/>
 * 
 * This means that methods like {@link #setRenderBodyOnly(boolean)} and
 * {@link #add(org.apache.wicket.behavior.Behavior...)} should be invoked on the {@link ListItem}
 * that is given in {@link #populateItem(ListItem)} method.
 * </p>
 * 
 * <p>
 * <strong>WARNING:</strong> though you can nest ListViews within Forms, you HAVE to set the
 * setReuseItems property to true in order to have validation work properly. By default,
 * setReuseItems is false, which has the effect that ListView replaces all child components by new
 * instances. The idea behind this is that you always render the fresh data, and as people usually
 * use ListViews for displaying read-only lists (at least, that's what we think), this is good
 * default behavior. <br />
 * However, as the components are replaced before the rendering starts, the search for specific
 * messages for these components fails as they are replaced with other instances. Another problem is
 * that 'wrong' user input is kept as (temporary) instance data of the components. As these
 * components are replaced by new ones, your user will never see the wrong data when setReuseItems
 * is false.
 * </p>
 * 
 * @author Jonathan Locke
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Eelco Hillenius
 * 
 * @param <T>
 *            type of elements contained in the model's list
 */
public abstract class ListView<T> extends AbstractRepeater
{
	private static final long serialVersionUID = 1L;

	/** Index of the first item to show */
	private int firstIndex = 0;

	/**
	 * If true, re-rendering the list view is more efficient if the window doesn't get changed at
	 * all or if it gets scrolled (compared to paging). But if you modify the listView model object,
	 * than you must manually call listView.removeAll() in order to rebuild the ListItems. If you
	 * nest a ListView in a Form, ALWAYS set this property to true, as otherwise validation will not
	 * work properly.
	 */
	private boolean reuseItems = false;

	/** Max number (not index) of items to show */
	private int viewSize = Integer.MAX_VALUE;

	/**
	 * @see org.apache.wicket.Component#Component(String)
	 */
	public ListView(final String id)
	{
		super(id);
	}

	/**
	 * @param id component id
	 * @param model model containing a list of
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public ListView(final String id, final IModel<? extends List<? extends T>> model)
	{
		super(id, model);

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
	 * @param id
	 *            See Component
	 * @param list
	 *            List to cast to Serializable
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public ListView(final String id, final List<? extends T> list)
	{
		this(id, Model.ofList(list));
	}

	/**
	 * Gets the list of items in the listView. This method is final because it is not designed to be
	 * overridden. If it were allowed to be overridden, the values returned by getModelObject() and
	 * getList() might not coincide.
	 * 
	 * @return The list of items in this list view.
	 */
	@SuppressWarnings("unchecked")
	public final List<? extends T> getList()
	{
		final List<? extends T> list = (List<? extends T>)getDefaultModelObject();
		if (list == null)
		{
			return Collections.emptyList();
		}
		return list;
	}

	/**
	 * If true re-rendering the list view is more efficient if the windows doesn't get changed at
	 * all or if it gets scrolled (compared to paging). But if you modify the listView model object,
	 * then you must manually call listView.removeAll() in order to rebuild the ListItems. If you
	 * nest a ListView in a Form, ALLWAYS set this property to true, as otherwise validation will
	 * not work properly.
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
		return firstIndex;
	}

	/**
	 * Based on the model object's list size, firstIndex and view size, determine what the view size
	 * really will be. E.g. default for viewSize is Integer.MAX_VALUE, if not set via setViewSize().
	 * If the underlying list has 10 elements, the value returned by getViewSize() will be 10 if
	 * startIndex = 0.
	 * 
	 * @return The number of items to be populated and rendered.
	 */
	public int getViewSize()
	{
		int size = viewSize;

		final Object modelObject = getDefaultModelObject();
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
	 * Returns a link that will move the given item "down" (towards the end) in the listView.
	 * 
	 * @param id
	 *            Name of move-down link component to create
	 * @param item
	 * @return The link component
	 */
	public final Link<Void> moveDownLink(final String id, final ListItem<T> item)
	{
		return new Link<Void>(id)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				final int index = item.getIndex();
				if (index != -1)
				{
					addStateChange();

					// Swap list items and invalidate listView
					Collections.swap(getList(), index, index + 1);
					ListView.this.removeAll();
				}
			}

			@Override
			public boolean isEnabled()
			{
				return item.getIndex() != (getList().size() - 1);
			}

		};
	}

	/**
	 * Returns a link that will move the given item "up" (towards the beginning) in the listView.
	 * 
	 * @param id
	 *            Name of move-up link component to create
	 * @param item
	 * @return The link component
	 */
	public final Link<Void> moveUpLink(final String id, final ListItem<T> item)
	{
		return new Link<Void>(id)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				final int index = item.getIndex();
				if (index != -1)
				{

					addStateChange();

					// Swap items and invalidate listView
					Collections.swap(getList(), index, index - 1);
					ListView.this.removeAll();
				}
			}

			@Override
			public boolean isEnabled()
			{
				return item.getIndex() != 0;
			}
		};
	}

	/**
	 * Returns a link that will remove this ListItem from the ListView that holds it.
	 * 
	 * @param id
	 *            Name of remove link component to create
	 * @param item
	 * @return The link component
	 */
	public final Link<Void> removeLink(final String id, final ListItem<T> item)
	{
		return new Link<Void>(id)
		{
			private static final long serialVersionUID = 1L;

			/**
			 * @see org.apache.wicket.markup.html.link.Link#onClick()
			 */
			@Override
			public void onClick()
			{
				addStateChange();

				item.modelChanging();

				// Remove item and invalidate listView
				getList().remove(item.getIndex());

				ListView.this.modelChanged();
				ListView.this.removeAll();
			}
		};
	}

	/**
	 * Sets the model as the provided list and removes all children, so that the next render will be
	 * using the contents of the model.
	 * 
	 * @param list
	 *            The list for the new model. The list must implement {@link Serializable}.
	 * @return This for chaining
	 */
	public ListView<T> setList(List<? extends T> list)
	{
		setDefaultModel(Model.ofList(list));
		return this;
	}

	/**
	 * If true re-rendering the list view is more efficient if the windows doesn't get changed at
	 * all or if it gets scrolled (compared to paging). But if you modify the listView model object,
	 * than you must manually call listView.removeAll() in order to rebuild the ListItems. If you
	 * nest a ListView in a Form, <strong>always</strong> set this property to true,
	 * as otherwise validation will not work properly.
	 * 
	 * @param reuseItems
	 *            Whether to reuse the child items.
	 * @return this
	 */
	public ListView<T> setReuseItems(boolean reuseItems)
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
	public ListView<T> setStartIndex(final int startIndex)
	{
		firstIndex = startIndex;

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
	public ListView<T> setViewSize(final int size)
	{
		viewSize = size;

		if (viewSize < 0)
		{
			viewSize = Integer.MAX_VALUE;
		}

		return this;
	}

	/**
	 * Subclasses may provide their own ListItemModel with extended functionality. The default
	 * ListItemModel works fine with mostly static lists where index remains valid. In cases where
	 * the underlying list changes a lot (many users using the application), it may not longer be
	 * appropriate. In that case your own ListItemModel implementation should use an id (e.g. the
	 * database' record id) to identify and load the list item model object.
	 * 
	 * @param listViewModel
	 *            The ListView's model
	 * @param index
	 *            The list item index
	 * @return The ListItemModel created
	 */
	protected IModel<T> getListItemModel(final IModel<? extends List<T>> listViewModel,
		final int index)
	{
		return new ListItemModel<>(this, index);
	}

	/**
	 * Create a new ListItem for list item at index.
	 * 
	 * @param index
	 * @param itemModel
	 *            object in the list that the item represents
	 * @return ListItem
	 */
	protected ListItem<T> newItem(final int index, IModel<T> itemModel)
	{
		return new ListItem<>(index, itemModel);
	}

	/**
	 * @see org.apache.wicket.markup.repeater.AbstractRepeater#onPopulate()
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected final void onPopulate()
	{
		// Get number of items to be displayed
		final int size = getViewSize();
		if (size > 0)
		{
			if (getReuseItems())
			{
				// Remove all ListItems no longer required
				final int maxIndex = firstIndex + size;
				for (final Iterator<Component> iterator = iterator(); iterator.hasNext();)
				{
					// Get next child component
					final ListItem<?> child = (ListItem<?>)iterator.next();
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

			boolean hasChildren = size() != 0;
			// Loop through the markup in this container for each item
			for (int i = 0; i < size; i++)
			{
				// Get index
				final int index = firstIndex + i;

				ListItem<T> item = null;
				if (hasChildren)
				{
					// If this component does not already exist, populate it
					item = (ListItem<T>)get(Integer.toString(index));
				}
				if (item == null)
				{
					// Create item for index
					item = newItem(index, getListItemModel(getModel(), index));

					// Add list item
					add(item);

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
	 * Comes handy for ready made ListView based components which must implement populateItem() but
	 * you don't want to lose compile time error checking reminding the user to implement abstract
	 * populateItem().
	 * 
	 * @param item
	 */
	protected void onBeginPopulateItem(final ListItem<T> item)
	{
	}

	/**
	 * Populate a given item.
	 * <p>
	 * <b>be careful</b> to add any components to the list item. So, don't do:
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
	 * @see org.apache.wicket.markup.repeater.AbstractRepeater#renderChild(org.apache.wicket.Component)
	 */
	@Override
	protected final void renderChild(Component child)
	{
		renderItem((ListItem<?>)child);
	}

	/**
	 * Render a single item.
	 * 
	 * @param item
	 *            The item to be rendered
	 */
	protected void renderItem(final ListItem<?> item)
	{
		item.render();
	}

	/**
	 * @see org.apache.wicket.markup.repeater.AbstractRepeater#renderIterator()
	 */
	@Override
	protected Iterator<Component> renderIterator()
	{
		final int size = size();
		return new ReadOnlyIterator<Component>()
		{
			private int index = 0;

			@Override
			public boolean hasNext()
			{
				return index < size;
			}

			@Override
			public Component next()
			{
				final String id = Integer.toString(firstIndex + index);
				index++;
				return get(id);
			}
		};
	}

	/**
	 * Gets model
	 * 
	 * @return model
	 */
	@SuppressWarnings("unchecked")
	public final IModel<? extends List<T>> getModel()
	{
		return (IModel<? extends List<T>>)getDefaultModel();
	}

	/**
	 * Sets model
	 * 
	 * @param model
	 */
	public final void setModel(IModel<? extends List<T>> model)
	{
		setDefaultModel(model);
	}

	/**
	 * Gets model object
	 * 
	 * @return model object
	 */
	@SuppressWarnings("unchecked")
	public final List<? extends T> getModelObject()
	{
		return (List<? extends T>)getDefaultModelObject();
	}

	/**
	 * Sets model object
	 * 
	 * @param object
	 */
	public final void setModelObject(List<T> object)
	{
		setDefaultModelObject(object);
	}
}
