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
import org.apache.wicket.markup.html.link.DisabledAttributeLinkBehavior;
import org.apache.wicket.markup.html.link.Link;

/**
 * An incremental link to a page of a PageableListView. Assuming your list view navigation looks
 * like
 * 
 * <pre>
 * 
 *   	 [first / &lt;&lt; / &lt;] 1 | 2 | 3 [&gt; / &gt;&gt; /last]
 * 
 * </pre>
 * 
 * <p>
 * and "&lt;" meaning the previous and "&lt;&lt;" goto the "current page - 5", than it is this kind
 * of incremental page links which can easily be created.
 * 
 * @author Juergen Donnerstag
 * @author Martijn Dashorst
 * @param <T>
 *            type of model object
 */
public class PagingNavigationIncrementLink<T> extends Link<T>
{
	private static final long serialVersionUID = 1L;

	/** The increment. */
	private final int increment;

	/** The PageableListView the page links are referring to. */
	protected final IPageable pageable;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to
	 * @param increment
	 *            increment by
	 */
	public PagingNavigationIncrementLink(final String id, final IPageable pageable,
		final int increment)
	{
		super(id);
		setAutoEnable(true);
		this.increment = increment;
		this.pageable = pageable;
		
		add(new DisabledAttributeLinkBehavior());
	}

	/**
	 * @see org.apache.wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public void onClick()
	{
		// Tell the PageableListView which page to print next
		pageable.setCurrentPage(getPageNumber());

		// Return the current page.
		setResponsePage(getPage());
	}

	/**
	 * Determines the next page number for the pageable component.
	 * 
	 * @return the new page number
	 */
	public final long getPageNumber()
	{
		// Determine the page number based on the current
		// PageableListView page and the increment
		long idx = pageable.getCurrentPage() + increment;

		// make sure the index lies between 0 and the last page
		return Math.max(0, Math.min(pageable.getPageCount() - 1, idx));
	}

	/**
	 * @return True if it is referring to the first page of the underlying PageableListView.
	 */
	public boolean isFirst()
	{
		return pageable.getCurrentPage() <= 0;
	}

	/**
	 * @return True if it is referring to the last page of the underlying PageableListView.
	 */
	public boolean isLast()
	{
		return pageable.getCurrentPage() >= (pageable.getPageCount() - 1);
	}

	/**
	 * Returns true if the page link links to the given page.
	 * 
	 * @param page
	 *            ignored
	 * @return True if this link links to the given page
	 * @see org.apache.wicket.markup.html.link.BookmarkablePageLink#linksTo(org.apache.wicket.Page)
	 */
	@Override
	public boolean linksTo(final Page page)
	{
		pageable.getCurrentPage();
		return ((increment < 0) && isFirst()) || ((increment > 0) && isLast());
	}
}