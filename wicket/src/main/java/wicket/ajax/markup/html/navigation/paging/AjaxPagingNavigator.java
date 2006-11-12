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
package wicket.ajax.markup.html.navigation.paging;

import wicket.MarkupContainer;
import wicket.markup.html.link.Link;
import wicket.markup.html.navigation.paging.IPageable;
import wicket.markup.html.navigation.paging.IPagingLabelProvider;
import wicket.markup.html.navigation.paging.PagingNavigation;
import wicket.markup.html.navigation.paging.PagingNavigator;

/**
 * A Wicket panel component to draw and maintain a complete page navigator,
 * meant to be easily added to any PageableListView. A navigation which contains
 * links to the first and last page, the current page +- some increment and
 * which supports paged navigation bars (@see
 * PageableListViewNavigationWithMargin).
 * <p>
 * <strong>NOTE</strong> To use the, you <i>have</i> to put your listview in a
 * webmarkupcontainer, otherwise it is not possible to update the contents of
 * the listview using Ajax.
 * 
 * @since 1.2
 * 
 * @author Martijn Dashorst
 */
public class AjaxPagingNavigator extends PagingNavigator
{
	private static final long serialVersionUID = 1L;

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
	public AjaxPagingNavigator(MarkupContainer parent, final String id, final IPageable pageable)
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
	public AjaxPagingNavigator(MarkupContainer parent, final String id, final IPageable pageable,
			final IPagingLabelProvider labelProvider)
	{
		super(parent, id, pageable, labelProvider);
		setOutputMarkupId(true);
	}

	/**
	 * Create a new increment link. May be subclassed to make use of specialized
	 * links, e.g. Ajaxian links.
	 * 
	 * @param id
	 *            the link id
	 * @param pageable
	 *            the pageable to control
	 * @param increment
	 *            the increment
	 * @return the increment link
	 */
	@Override
	protected Link newPagingNavigationIncrementLink(MarkupContainer parent, String id,
			IPageable pageable, int increment)
	{
		return new AjaxPagingNavigationIncrementLink(parent, id, pageable, increment);
	}

	/**
	 * Create a new pagenumber link. May be subclassed to make use of
	 * specialized links, e.g. Ajaxian links.
	 * 
	 * @param id
	 *            the link id
	 * @param pageable
	 *            the pageable to control
	 * @param pageNumber
	 *            the page to jump to
	 * @return the pagenumber link
	 */
	@Override
	protected Link newPagingNavigationLink(MarkupContainer parent, String id, IPageable pageable,
			int pageNumber)
	{
		return new AjaxPagingNavigationLink(parent, id, pageable, pageNumber);
	}

	/**
	 * Create a new PagingNavigation. May be subclassed to make us of
	 * specialized PagingNavigation.
	 * 
	 * @param pageable
	 *            the pageable component
	 * @param labelProvider
	 *            The label provider for the link text.
	 * @return the navigation object
	 */
	@Override
	protected PagingNavigation newNavigation(MarkupContainer parent, final IPageable pageable,
			final IPagingLabelProvider labelProvider)
	{
		return new AjaxPagingNavigation(parent, "navigation", pageable, labelProvider);
	}

}