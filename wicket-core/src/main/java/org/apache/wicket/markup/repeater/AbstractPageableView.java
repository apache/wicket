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
package org.apache.wicket.markup.repeater;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;

import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.model.IModel;


/**
 * An abstract repeater view that provides paging functionality to its subclasses.
 * <p>
 * The view is populated by overriding the <code>getItemModels(int offset, int count)</code> method
 * and providing an iterator that returns models for items in the current page. The
 * AbstractPageableView builds the items that will be rendered by looping over the models and
 * calling the <code>newItem(String id, int index, IModel model)</code> to generate the child item
 * container followed by <code>populateItem(Component item)</code> to let the user populate the
 * newly created item container with with custom components.
 * </p>
 * 
 * @see org.apache.wicket.markup.repeater.RefreshingView
 * @see org.apache.wicket.markup.html.navigation.paging.IPageable
 * 
 * @author Igor Vaynberg (ivaynberg)
 * 
 * @param <T>
 *            type of elements contained in the model's list
 */
public abstract class AbstractPageableView<T> extends RefreshingView<T> implements IPageableItems
{
	/** */
	private static final long serialVersionUID = 1L;

	/**
	 * Keeps track of the number of items we show per page. The default is Integer.MAX_VALUE which
	 * effectively disables paging.
	 */
	private long itemsPerPage = Long.MAX_VALUE;

	/**
	 * Keeps track of the current page number.
	 */
	private long currentPage;

	/**
	 * <code>cachedItemCount</code> is used to cache the call to <code>internalGetItemCount()</code>
	 * for the duration of the request because that call can potentially be expensive ( a select
	 * count query ) and so we do not want to execute it multiple times.
	 */
	private transient long cachedItemCount;

	/**
	 * Constructor
	 * 
	 * @param id
	 * @param model
	 * @see org.apache.wicket.Component#Component(String, IModel)
	 */
	public AbstractPageableView(String id, IModel<? extends Collection<? extends T>> model)
	{
		super(id, model);
		clearCachedItemCount();
	}

	/** @see org.apache.wicket.Component#Component(String) */
	public AbstractPageableView(String id)
	{
		super(id);
		clearCachedItemCount();
	}

	/**
	 * This method retrieves the subset of models for items in the current page and allows
	 * RefreshingView to generate items.
	 * 
	 * @return iterator over models for items in the current page
	 */
	@Override
	protected Iterator<IModel<T>> getItemModels()
	{
		long offset = getFirstItemOffset();
		long size = getViewSize();

		Iterator<IModel<T>> models = getItemModels(offset, size);

		models = new CappedIteratorAdapter<T>(models, size);

		return models;
	}

	/**
	 * @see org.apache.wicket.markup.repeater.AbstractRepeater#onBeforeRender()
	 */
	@Override
	protected void onBeforeRender()
	{
		clearCachedItemCount();
		super.onBeforeRender();
	}

	/**
	 * Returns an iterator over models for items in the current page
	 * 
	 * @param offset
	 *            index of first item in this page
	 * @param size
	 *            number of items that will be shown in the current page
	 * @return an iterator over models for items in the current page
	 */
	protected abstract Iterator<IModel<T>> getItemModels(long offset, long size);

	// /////////////////////////////////////////////////////////////////////////
	// ITEM COUNT CACHE
	// /////////////////////////////////////////////////////////////////////////

	private void clearCachedItemCount()
	{
		cachedItemCount = -1;
	}

	private void setCachedItemCount(long itemCount)
	{
		cachedItemCount = itemCount;
	}

	private long getCachedItemCount()
	{
		if (cachedItemCount < 0)
		{
			throw new IllegalStateException("getItemCountCache() called when cache was not set");
		}
		return cachedItemCount;
	}

	private boolean isItemCountCached()
	{
		return cachedItemCount >= 0;
	}

	// /////////////////////////////////////////////////////////////////////////
	// PAGING
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * @return maximum number of items that will be shown per page
	 */
	@Override
	public long getItemsPerPage()
	{
		return itemsPerPage;
	}

	/**
	 * Sets the maximum number of items to show per page. The current page will also be set to zero
	 * 
	 * @param items
	 */
	public final void setItemsPerPage(long items)
	{
		if (items < 1)
		{
			throw new IllegalArgumentException("Argument [itemsPerPage] cannot be less than 1");
		}

		if (itemsPerPage != items)
		{
			if (isVersioned())
			{
				addStateChange();
			}
		}

		itemsPerPage = items;

		// because items per page can effect the total number of pages we always
		// reset the current page back to zero
		setCurrentPage(0);
	}

	/**
	 * @return total item count
	 */
	protected abstract long internalGetItemCount();

	/**
	 * Get the row count.
	 * 
	 * @see #getItemCount()
	 * 
	 * @return total item count, but 0 if not visible in the hierarchy
	 */
	public final long getRowCount()
	{
		if (!isVisibleInHierarchy())
		{
			return 0;
		}

		return getItemCount();
	}

	/**
	 * Get the item count. Since dataprovider.size() could potentially be expensive, the item count
	 * is cached.
	 * 
	 * @see #getRowCount()
	 * 
	 * @return the item count
	 */
	@Override
	public final long getItemCount()
	{
		if (isItemCountCached())
		{
			return getCachedItemCount();
		}

		long count = internalGetItemCount();

		setCachedItemCount(count);
		return count;
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageable#getCurrentPage()
	 */
	@Override
	public final long getCurrentPage()
	{
		long page = currentPage;

		/*
		 * trim current page if its out of bounds this can happen if items are added/deleted between
		 * requests
		 */

		if (page > 0 && page >= getPageCount())
		{
			page = Math.max(getPageCount() - 1, 0);
			currentPage = page;
			return page;
		}

		return page;
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageable#setCurrentPage(long)
	 */
	@Override
	public final void setCurrentPage(long page)
	{
		if (currentPage != page)
		{
			if (isVersioned())
			{
				addStateChange();

			}
		}
		currentPage = page;
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageable#getPageCount()
	 */
	@Override
	public long getPageCount()
	{
		long total = getRowCount();
		long itemsPerPage = getItemsPerPage();
		long count = total / itemsPerPage;

		if (itemsPerPage * count < total)
		{
			count++;
		}

		return count;

	}

	/**
	 * @return the index of the first visible item in the view
	 */
	public long getFirstItemOffset()
	{
		return getCurrentPage() * getItemsPerPage();
	}


	/**
	 * @return the number of items visible
	 */
	public long getViewSize()
	{
		return Math.min(getItemsPerPage(), getRowCount() - getFirstItemOffset());
	}

	// /////////////////////////////////////////////////////////////////////////
	// HELPER CLASSES
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Iterator adapter that makes sure only the specified max number of items can be accessed from
	 * its delegate.
	 * 
	 * @param <T>
	 *            Model object type
	 */
	private static class CappedIteratorAdapter<T> implements Iterator<IModel<T>>
	{
		private final long max;
		private long index;
		private final Iterator<IModel<T>> delegate;

		/**
		 * Constructor
		 * 
		 * @param delegate
		 *            delegate iterator
		 * @param max
		 *            maximum number of items that can be accessed.
		 */
		public CappedIteratorAdapter(Iterator<IModel<T>> delegate, long max)
		{
			this.delegate = delegate;
			this.max = max;
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		@Override
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		@Override
		public boolean hasNext()
		{
			return (index < max) && delegate.hasNext();
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		@Override
		public IModel<T> next()
		{
			if (index >= max)
			{
				throw new NoSuchElementException();
			}
			index++;
			return delegate.next();
		}

	}

	/**
	 * @see org.apache.wicket.Component#onDetach()
	 */
	@Override
	protected void onDetach()
	{
		clearCachedItemCount();
		super.onDetach();
	}

	private void readObject(java.io.ObjectInputStream s) throws java.io.IOException,
		ClassNotFoundException
	{
		// Read in all fields
		s.defaultReadObject();
		clearCachedItemCount();
	}
}
