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
package org.apache.wicket.request.target.component;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.PageId;
import org.apache.wicket.RequestCycle;

/**
 * Target that navigates to a page pointed to by its id. The great benefit of this target over the
 * PageRequestTarget is that no reference to the actual page is needed, which greatly facilitates
 * navigational usecases where a list or a stack of page references is needed (ie breadcrumbs).
 * 
 * @see PageId
 * 
 * @author igor.vaynberg
 */
public class PageIdRequestTarget implements IRequestTarget
{
	private final PageId id;

	/**
	 * Constructor
	 * 
	 * Even though a page is passed in, only a reference to its {@link PageId} is kept
	 * 
	 * @param page
	 */
	public PageIdRequestTarget(Page page)
	{
		if (page == null)
		{
			throw new IllegalArgumentException("Argument `page` cannot be null");
		}
		id = page.getPageId();
	}

	/**
	 * Constructor
	 * 
	 * @param pageId
	 */
	public PageIdRequestTarget(PageId pageId)
	{
		if (pageId == null)
		{
			throw new IllegalArgumentException("Argument `pageId` cannot be null");
		}

		id = pageId;
	}


	/**
	 * @return id page id
	 */
	public final PageId getPageId()
	{
		return id;
	}

	/** {@inheritDoc} */
	public void respond(RequestCycle requestCycle)
	{
		Page page = requestCycle.getSession().getPage(id.getPageMapName(), "" + id.getPageNumber(),
			id.getPageVersion());

		// Should page be redirected to?
		if (requestCycle.isRedirect())
		{
			// Redirect to the page
			requestCycle.redirectTo(page);
		}
		else
		{
			// Let page render itself
			page.renderPage();
		}
	}

	/** {@inheritDoc} */
	public void detach(RequestCycle requestCycle)
	{
	}

}
