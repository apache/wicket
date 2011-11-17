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
package org.apache.wicket.ajax.markup.html.navigation.paging;

import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.navigation.paging.IPageable;
import org.apache.wicket.markup.html.navigation.paging.IPagingLabelProvider;
import org.apache.wicket.markup.html.navigation.paging.PagingNavigation;

/**
 * An ajaxified navigation for a PageableListView that holds links to other pages of the
 * PageableListView.
 * <p>
 * Please
 * 
 * @see org.apache.wicket.markup.html.navigation.paging.PagingNavigation
 * 
 * @since 1.2
 * 
 * @author Martijn Dashorst
 */
public class AjaxPagingNavigation extends PagingNavigation
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The underlying pageable component to navigate
	 */
	public AjaxPagingNavigation(final String id, final IPageable pageable)
	{
		this(id, pageable, null);
	}

	/**
	 * Constructor.
	 * 
	 * @param id
	 *            See Component
	 * @param pageable
	 *            The underlying pageable component to navigate
	 * @param labelProvider
	 *            The label provider for the text that the links should be displaying.
	 */
	public AjaxPagingNavigation(final String id, final IPageable pageable,
		final IPagingLabelProvider labelProvider)
	{
		super(id, pageable, labelProvider);
	}

	/**
	 * Factory method for creating ajaxian page number links.
	 * 
	 * @param id
	 *            link id
	 * @param pageable
	 *            the pageable
	 * @param pageIndex
	 *            the index the link points to
	 * @return the ajaxified page number link.
	 */
	@Override
	protected Link<?> newPagingNavigationLink(String id, IPageable pageable, long pageIndex)
	{
		return new AjaxPagingNavigationLink(id, pageable, pageIndex);
	}
}
