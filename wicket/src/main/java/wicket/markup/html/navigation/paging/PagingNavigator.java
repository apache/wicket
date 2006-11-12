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
package wicket.markup.html.navigation.paging;

import wicket.MarkupContainer;
import wicket.markup.html.link.Link;
import wicket.markup.html.panel.Panel;

/**
 * A Wicket panel component to draw and maintain a complete page navigator,
 * meant to be easily added to any PageableListView. A navigation which contains
 * links to the first and last page, the current page +- some increment and
 * which supports paged navigation bars (@see
 * PageableListViewNavigationWithMargin).
 * 
 * @author Juergen Donnerstag
 */
public class PagingNavigator extends Panel
{
	private static final long serialVersionUID = 1L;

	/** The navigation bar to be printed, e.g. 1 | 2 | 3 etc. */
	private final PagingNavigation pagingNavigation;

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to.
	 */
	public PagingNavigator(MarkupContainer parent, final String id, final IPageable pageable)
	{
		this(parent, id, pageable, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to.
	 * @param labelProvider
	 *            The label provider for the link text.
	 */
	public PagingNavigator(MarkupContainer parent, final String id, final IPageable pageable,
			final IPagingLabelProvider labelProvider)
	{
		super(parent, id);


		// Get the navigation bar and add it to the hierarchy
		this.pagingNavigation = newNavigation(this, pageable, labelProvider);
		// Add additional page links
		newPagingNavigationLink(this, "first", pageable, 0);
		newPagingNavigationIncrementLink(this, "prev", pageable, -1);
		newPagingNavigationIncrementLink(this, "next", pageable, 1);
		newPagingNavigationLink(this, "last", pageable, -1);
	}

	/**
	 * Create a new increment link. May be subclassed to make use of specialized
	 * links, e.g. Ajaxian links.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            the link id
	 * @param pageable
	 *            the pageable to control
	 * @param increment
	 *            the increment
	 * @return the increment link
	 */
	protected Link newPagingNavigationIncrementLink(MarkupContainer parent, String id,
			IPageable pageable, int increment)
	{
		return new PagingNavigationIncrementLink(parent, id, pageable, increment);
	}

	/**
	 * Create a new pagenumber link. May be subclassed to make use of
	 * specialized links, e.g. Ajaxian links.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param id
	 *            the link id
	 * @param pageable
	 *            the pageable to control
	 * @param pageNumber
	 *            the page to jump to
	 * @return the pagenumber link
	 */
	protected Link newPagingNavigationLink(MarkupContainer parent, String id, IPageable pageable,
			int pageNumber)
	{
		return new PagingNavigationLink(parent, id, pageable, pageNumber);
	}

	/**
	 * Create a new PagingNavigation. May be subclassed to make us of
	 * specialized PagingNavigation.
	 * 
	 * @param parent
	 *            The parent of this component
	 * 
	 * @param pageable
	 *            the pageable component
	 * @param labelProvider
	 *            The label provider for the link text.
	 * @return the navigation object
	 */
	protected PagingNavigation newNavigation(MarkupContainer parent, final IPageable pageable,
			final IPagingLabelProvider labelProvider)
	{
		return new PagingNavigation(parent, "navigation", pageable, labelProvider);
	}

	/**
	 * Gets the pageable navigation component for configuration purposes.
	 * 
	 * @return the associated pageable navigation.
	 */
	public final PagingNavigation getPagingNavigation()
	{
		return pagingNavigation;
	}
}