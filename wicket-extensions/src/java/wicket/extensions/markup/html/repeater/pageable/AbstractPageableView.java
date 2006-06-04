/*
 * $Id: AbstractPageableView.java 5840 2006-05-24 20:49:09 +0000 (Wed, 24 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-24 20:49:09 +0000 (Wed, 24
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
package wicket.extensions.markup.html.repeater.pageable;

import java.util.Iterator;
import java.util.NoSuchElementException;

import wicket.MarkupContainer;
import wicket.extensions.markup.html.repeater.refreshing.RefreshingView;
import wicket.markup.html.navigation.paging.IPageable;
import wicket.model.IModel;
import wicket.version.undo.Change;

/**
 * An abstract repeater view that provides paging functionality to its
 * subclasses.
 * <p>
 * The view is populated by overriding the
 * <code>getItemModels(int offset, int count)</code> method and providing an
 * iterator that returns models for items in the current page. The
 * AbstractPageableView builds the items that will be rendered by looping over
 * the models and calling the
 * <code>newItem(MarkupContainer parent,final String id, int index, IModel model)</code>
 * to generate the child item container followed by
 * <code>populateItem(Component item)</code> to let the user populate the
 * newly created item container with with custom components.
 * </p>
 * 
 * @see wicket.extensions.markup.html.repeater.refreshing.RefreshingView
 * @see wicket.markup.html.navigation.paging.IPageable
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public abstract class AbstractPageableView extends RefreshingView implements IPageable
{
	/**
	 * Keeps track of the number of items we show per page. The default is
	 * Integer.MAX_VALUE which effectively disables paging.
	 */
	private int itemsPerPage = Integer.MAX_VALUE;

	/**
	 * Keeps track of the current page number.
	 */
	private int currentPage;

	/**
	 * <code>cachedItemCount</code> is used to cache the call to
	 * <code>internalGetItemCount()</code> for the duration of the request
	 * because that call can potentially be expensive ( a select count query )
	 * and so we do not want to execute it multiple times.
	 */
	private int cachedItemCount;


	/** @see wicket.Component#Component(MarkupContainer,String, IModel) */
	public AbstractPageableView(MarkupContainer parent, final String id, IModel model)
	{
		super(parent, id, model);
		clearCachedItemCount();
	}


	/** @see wicket.Component#Component(MarkupContainer,String) */
	public AbstractPageableView(MarkupContainer parent, final String id)
	{
		super(parent, id);
		clearCachedItemCount();
	}


	/**
	 * This method retrieves the subset of models for items in the current page
	 * and allows RefreshingView to generate items.
	 * 
	 * @return iterator over models for items in the current page
	 */
	@Override
	protected Iterator getItemModels()
	{
		int offset = getViewOffset();
		int size = getViewSize();

		Iterator models = getItemModels(offset, size);

		models = new CappedIteratorAdapter(models, size);

		return models;
	}


	@Override
	protected void internalOnDetach()
	{
		super.internalOnDetach();
		clearCachedItemCount();
	}

	/**
	 * Returns an iterator over models for items in the current page
	 * 
	 * @param offset
	 *            index of first item in this page
	 * @param size
	 *            number of items that will be showin in the current page
	 * @return an iterator over models for items in the current page
	 */
	protected abstract Iterator getItemModels(int offset, int size);


	// /////////////////////////////////////////////////////////////////////////
	// ITEM COUNT CACHE
	// /////////////////////////////////////////////////////////////////////////


	private void clearCachedItemCount()
	{
		cachedItemCount = -1;
	}

	private void setCachedItemCount(int itemCount)
	{
		cachedItemCount = itemCount;
	}

	private int getCachedItemCount()
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
	protected final int internalGetRowsPerPage()
	{
		return itemsPerPage;
	}

	/**
	 * Sets the maximum number of items to show per page. The current page will
	 * also be set to zero
	 * 
	 * @param items
	 */
	protected final void internalSetRowsPerPage(int items)
	{
		if (items < 1)
		{
			throw new IllegalArgumentException("Argument [itemsPerPage] cannot be less then 1");
		}

		if (itemsPerPage != items)
		{
			if (isVersioned())
			{
				addStateChange(new Change()
				{
					private static final long serialVersionUID = 1L;

					final int old = itemsPerPage;

					@Override
					public void undo()
					{
						itemsPerPage = old;
					}

					@Override
					public String toString()
					{
						return "ItemsPerPageChange[component: " + getPath() + ", itemsPerPage: "
								+ old + "]";
					}
				});
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
	protected abstract int internalGetItemCount();

	/**
	 * @return total item count
	 */
	public final int getRowCount()
	{
		if (!isVisibleInHierarchy())
		{
			return 0;
		}

		if (isItemCountCached())
		{
			return getCachedItemCount();
		}

		int count = internalGetItemCount();

		setCachedItemCount(count);

		return count;
	}

	/**
	 * @see wicket.markup.html.navigation.paging.IPageable#getCurrentPage()
	 */
	public final int getCurrentPage()
	{
		int page = currentPage;

		/*
		 * trim current page if its out of bounds this can happen if items are
		 * added/deleted between requests
		 */

		if (page >= getPageCount())
		{
			page = Math.max(getPageCount() - 1, 0);
			setCurrentPage(page);
			return page;
		}

		return page;
	}

	/**
	 * @see wicket.markup.html.navigation.paging.IPageable#setCurrentPage(int)
	 */
	public final void setCurrentPage(int page)
	{
		if (page < 0 || (page >= getPageCount() && getPageCount() > 0))
		{
			throw new IndexOutOfBoundsException("argument [page]=" + page + ", must be 0<=page<"
					+ getPageCount());
		}

		if (currentPage != page)
		{
			if (isVersioned())
			{
				addStateChange(new Change()
				{
					private static final long serialVersionUID = 1L;

					private final int old = currentPage;

					@Override
					public void undo()
					{
						currentPage = old;
					}

					@Override
					public String toString()
					{
						return "CurrentPageChange[component: " + getPath() + ", currentPage: "
								+ old + "]";
					}
				});

			}
		}
		currentPage = page;
	}

	/**
	 * @see wicket.markup.html.navigation.paging.IPageable#getPageCount()
	 */
	public final int getPageCount()
	{
		int total = getRowCount();
		int page = internalGetRowsPerPage();
		int count = total / page;

		if (page * count < total)
		{
			count++;
		}

		return count;

	}

	/**
	 * @return the index of the first visible item
	 */
	protected int getViewOffset()
	{
		return getCurrentPage() * internalGetRowsPerPage();
	}


	/**
	 * @return the number of items visible
	 */
	protected int getViewSize()
	{
		return Math.min(internalGetRowsPerPage(), getRowCount() - getViewOffset());
	}

	// /////////////////////////////////////////////////////////////////////////
	// HELPER CLASSES
	// /////////////////////////////////////////////////////////////////////////

	/**
	 * Iterator adapter that makes sure only the specified max number of items
	 * can be accessed from its delegate.
	 */
	private static class CappedIteratorAdapter implements Iterator
	{
		private int max;
		private int index;
		private Iterator delegate;

		/**
		 * Constructor
		 * 
		 * @param delegate
		 *            delegate iterator
		 * @param max
		 *            maximum number of items that can be accessed.
		 */
		public CappedIteratorAdapter(Iterator delegate, int max)
		{
			this.delegate = delegate;
			this.max = max;
		}

		/**
		 * @see java.util.Iterator#remove()
		 */
		public void remove()
		{
			throw new UnsupportedOperationException();
		}

		/**
		 * @see java.util.Iterator#hasNext()
		 */
		public boolean hasNext()
		{
			return (index < max) && delegate.hasNext();
		}

		/**
		 * @see java.util.Iterator#next()
		 */
		public Object next()
		{
			if (index >= max)
			{
				throw new NoSuchElementException();
			}
			index++;
			return delegate.next();
		}

	};


}
