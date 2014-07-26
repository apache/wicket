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

import java.util.List;

import org.apache.wicket.markup.html.navigation.paging.IPageableItems;
import org.apache.wicket.model.IModel;


/**
 * PageableListView is similar to ListView but provides in addition pageable views. A
 * PageableListView holds pageable rows of information. The rows can be re-ordered and deleted,
 * either one at a time or many at a time.
 * 
 * @author Jonathan Locke
 * @param <T>
 *            Model object type
 */
public abstract class PageableListView<T> extends ListView<T> implements IPageableItems
{
	private static final long serialVersionUID = 1L;

	/** The page to show. */
	private long currentPage;

	/** Number of rows per page of the list view. */
	private long itemsPerPage;

	/**
	 * Constructor
	 * 
	 * @param id
	 *            See Component
	 * @param model
	 *            See Component
	 * @param itemsPerPage
	 *            Number of rows to show on a page
	 */
	public PageableListView(final String id, final IModel<? extends List<T>> model,
		int itemsPerPage)
	{
		super(id, model);
		this.itemsPerPage = itemsPerPage;
	}

	/**
	 * Creates a pageable list view having the given number of rows per page that uses the provided
	 * object as a simple model.
	 * 
	 * @param id
	 *            See Component
	 * @param list
	 *            See Component
	 * @param itemsPerPage
	 *            Number of rows to show on a page
	 * @see ListView#ListView(String, List)
	 */
	public PageableListView(final String id, final List<T> list, final int itemsPerPage)
	{
		super(id, list);
		this.itemsPerPage = itemsPerPage;
	}

	/**
	 * Gets the index of the current page being displayed by this list view.
	 * 
	 * @return Returns the currentPage.
	 */
	@Override
	public final long getCurrentPage()
	{
		// If first cell is out of range, bring page back into range
		while ((currentPage > 0) && ((currentPage * itemsPerPage) >= getItemCount()))
		{
			currentPage--;
		}

		return currentPage;
	}

	/**
	 * Gets the number of pages in this list view.
	 * 
	 * @return The number of pages in this list view
	 */
	@Override
	public final long getPageCount()
	{
		return ((getItemCount() + itemsPerPage) - 1) / itemsPerPage;
	}

	/**
	 * Gets the maximum number of rows on each page.
	 * 
	 * @return the maximum number of rows on each page.
	 */
	@Override
	public final long getItemsPerPage()
	{
		return itemsPerPage;
	}

	/**
	 * Sets the maximum number of rows on each page.
	 * 
	 * @param itemsPerPage
	 *            the maximum number of rows on each page.
	 */
	public final void setItemsPerPage(long itemsPerPage)
	{
		if (itemsPerPage < 0)
		{
			itemsPerPage = 0;
		}

		addStateChange();
		this.itemsPerPage = itemsPerPage;
	}

	/**
	 * @return offset of first item
	 */
	public long getFirstItemOffset()
	{
		return getCurrentPage() * getItemsPerPage();
	}

	/**
	 * @see org.apache.wicket.markup.html.navigation.paging.IPageableItems#getItemCount()
	 */
	@Override
	public long getItemCount()
	{
		return getList().size();
	}

	/**
	 * @see org.apache.wicket.markup.html.list.ListView#getViewSize()
	 */
	@Override
	public int getViewSize()
	{
		if (getDefaultModelObject() != null)
		{
			super.setStartIndex((int)getFirstItemOffset());
			super.setViewSize((int)getItemsPerPage());
		}

		return super.getViewSize();
	}

	/**
	 * Sets the current page that this list view should show.
	 * 
	 * @param currentPage
	 *            The currentPage to set.
	 */
	@Override
	public final void setCurrentPage(long currentPage)
	{
		if (currentPage < 0)
		{
			currentPage = 0;
		}

		long pageCount = getPageCount();
		if ((currentPage > 0) && (currentPage >= pageCount))
		{
			currentPage = pageCount - 1;
		}

		addStateChange();
		this.currentPage = currentPage;
	}


	/**
	 * Prevent users from accidentally using it.
	 * 
	 * @see org.apache.wicket.markup.html.list.ListView#setStartIndex(int)
	 * @throws UnsupportedOperationException
	 *             always
	 */
	@Override
	public ListView<T> setStartIndex(int startIndex) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(
			"You must not use setStartIndex() with PageableListView");
	}

	/**
	 * Prevent users from accidentally using it.
	 * 
	 * @param size
	 *            the view size
	 * @return This
	 * @throws UnsupportedOperationException
	 *             always
	 * @see org.apache.wicket.markup.html.list.ListView#setStartIndex(int)
	 */
	@Override
	public ListView<T> setViewSize(int size) throws UnsupportedOperationException
	{
		throw new UnsupportedOperationException(
			"You must not use setViewSize() with PageableListView");
	}

}
