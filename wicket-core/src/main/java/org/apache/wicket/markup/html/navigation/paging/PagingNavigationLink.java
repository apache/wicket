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
package org.apache.wicket.markup.html.navigation.paging;

import org.apache.wicket.Page;
import org.apache.wicket.markup.html.link.Link;

/**
 * A link to a page of a PageableListView.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 * @author Martijn Dashorst
 * @param <T>
 *            type of model object
 */
public class PagingNavigationLink<T> extends Link<T>
{
	private static final long serialVersionUID = 1L;

	/** The pageable list view. */
	protected final IPageable pageable;

	/** The page of the PageableListView this link is for. */
	private final long pageNumber;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component for this page link
	 * @param pageNumber
	 *            The page number in the PageableListView that this link links to. Negative
	 *            pageNumbers are relative to the end of the list.
	 */
	public PagingNavigationLink(final String id, final IPageable pageable, final long pageNumber)
	{
		super(id);
		setAutoEnable(true);
		this.pageNumber = pageNumber;
		this.pageable = pageable;
		
		add(new DisabledLinkPagingBehavior());
	}

	/**
	 * @see org.apache.wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public void onClick()
	{
		pageable.setCurrentPage(getPageNumber());
	}

	/**
	 * Get pageNumber.
	 * 
	 * @return pageNumber.
	 */
	public final long getPageNumber()
	{
		return cullPageNumber(pageNumber);
	}

	/**
	 * Allows the link to cull the page number to the valid range before it is retrieved from the
	 * link
	 * 
	 * @param pageNumber
	 * @return culled page number
	 */
	protected long cullPageNumber(long pageNumber)
	{
		long idx = pageNumber;
		if (idx < 0)
		{
			idx = pageable.getPageCount() + idx;
		}

		if (idx > (pageable.getPageCount() - 1))
		{
			idx = pageable.getPageCount() - 1;
		}

		if (idx < 0)
		{
			idx = 0;
		}

		return idx;
	}

	/**
	 * @return True if this page is the first page of the containing PageableListView
	 */
	public final boolean isFirst()
	{
		return getPageNumber() == 0;
	}

	/**
	 * @return True if this page is the last page of the containing PageableListView
	 */
	public final boolean isLast()
	{
		return getPageNumber() == (pageable.getPageCount() - 1);
	}

	/**
	 * Returns true if this PageableListView navigation link links to the given page.
	 * 
	 * @param page
	 *            The page
	 * @return True if this link links to the given page
	 * @see org.apache.wicket.markup.html.link.PageLink#linksTo(org.apache.wicket.Page)
	 */
	@Override
	public final boolean linksTo(final Page page)
	{
		return getPageNumber() == pageable.getCurrentPage();
	}
}