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

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;

/**
 * A Wicket panel component to draw and maintain a complete page navigator, meant to be easily added
 * to any PageableListView. A navigation which contains links to the first and last page, the
 * current page +- some increment and which supports paged navigation bars (@see
 * PageableListViewNavigationWithMargin).
 * 
 * @author Juergen Donnerstag
 */
public class PagingNavigator extends Panel
{
	private static final long serialVersionUID = 1L;

	/** The navigation bar to be printed, e.g. 1 | 2 | 3 etc. */
	private PagingNavigation pagingNavigation;
	private final IPageable pageable;
	private final IPagingLabelProvider labelProvider;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to.
	 */
	public PagingNavigator(final String id, final IPageable pageable)
	{
		this(id, pageable, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The pageable component the page links are referring to.
	 * @param labelProvider
	 *            The label provider for the link text.
	 */
	public PagingNavigator(final String id, final IPageable pageable,
		final IPagingLabelProvider labelProvider)
	{
		super(id);
		this.pageable = pageable;
		this.labelProvider = labelProvider;
	}


	/**
	 * {@link IPageable} this navigator is linked with
	 * 
	 * @return {@link IPageable} instance
	 */
	public final IPageable getPageable()
	{
		return pageable;
	}


	@Override
	protected void onBeforeRender()
	{

		if (get("first") == null)
		{
			// Get the navigation bar and add it to the hierarchy
			pagingNavigation = newNavigation(pageable, labelProvider);
			add(pagingNavigation);

			// Add additional page links
			add(newPagingNavigationLink("first", pageable, 0));
			add(newPagingNavigationIncrementLink("prev", pageable, -1));
			add(newPagingNavigationIncrementLink("next", pageable, 1));
			add(newPagingNavigationLink("last", pageable, -1));
		}
		super.onBeforeRender();
	}

	/**
	 * Create a new increment link. May be subclassed to make use of specialized links, e.g. Ajaxian
	 * links.
	 * 
	 * @param id
	 *            the link id
	 * @param pageable
	 *            the pageable to control
	 * @param increment
	 *            the increment
	 * @return the increment link
	 */
	protected Link<?> newPagingNavigationIncrementLink(String id, IPageable pageable, int increment)
	{
		return new PagingNavigationIncrementLink<Void>(id, pageable, increment)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled()
			{
				return super.isEnabled() && PagingNavigator.this.isEnabled() &&
					PagingNavigator.this.isEnableAllowed();
			}
		};
	}

	/**
	 * Create a new pagenumber link. May be subclassed to make use of specialized links, e.g.
	 * Ajaxian links.
	 * 
	 * @param id
	 *            the link id
	 * @param pageable
	 *            the pageable to control
	 * @param pageNumber
	 *            the page to jump to
	 * @return the pagenumber link
	 */
	protected Link<?> newPagingNavigationLink(String id, IPageable pageable, int pageNumber)
	{
		return new PagingNavigationLink<Void>(id, pageable, pageNumber)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled()
			{
				return super.isEnabled() && PagingNavigator.this.isEnabled() &&
					PagingNavigator.this.isEnableAllowed();
			}
		};

	}

	/**
	 * Create a new PagingNavigation. May be subclassed to make us of specialized PagingNavigation.
	 * 
	 * @param pageable
	 *            the pageable component
	 * @param labelProvider
	 *            The label provider for the link text.
	 * @return the navigation object
	 */
	protected PagingNavigation newNavigation(final IPageable pageable,
		final IPagingLabelProvider labelProvider)
	{
		return new PagingNavigation("navigation", pageable, labelProvider)
		{
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isEnabled()
			{
				return super.isEnabled() && PagingNavigator.this.isEnabled() &&
					PagingNavigator.this.isEnableAllowed();
			}
		};
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