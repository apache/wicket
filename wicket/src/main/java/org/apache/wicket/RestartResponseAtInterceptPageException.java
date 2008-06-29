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
package org.apache.wicket;

import org.apache.wicket.request.RequestParameters;

/**
 * Causes Wicket to interrupt current request processing and immediately redirect to an intercept
 * page.
 * <p>
 * Similar to calling redirectToInteceptPage(Page) with the difference that this exception will
 * interrupt processing of the current request.
 * 
 * @see org.apache.wicket.PageMap#redirectToInterceptPage(Page)
 * @see org.apache.wicket.Component#redirectToInterceptPage(Page)
 * 
 * @author Igor Vaynberg (ivaynberg)
 * @author Jonathan Locke
 * @author Philip Chapman
 */
public class RestartResponseAtInterceptPageException extends AbstractRestartResponseException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Redirects to the specified intercept page.
	 * 
	 * @param interceptPage
	 *            redirect page
	 */
	public RestartResponseAtInterceptPageException(final Page interceptPage)
	{
		if (interceptPage == null)
		{
			throw new IllegalStateException("Argument interceptPage cannot be null");
		}

		redirectToInterceptPage(interceptPage);
	}

	/**
	 * Redirects to the specified intercept page, this will result in a bookmarkable redirect.
	 * 
	 * @param <C> The page type
	 * 
	 * @param interceptPageClass
	 *            Class of intercept page to instantiate
	 */
	public <C extends Page> RestartResponseAtInterceptPageException(
		final Class<C> interceptPageClass)
	{
		if (interceptPageClass == null)
		{
			throw new IllegalStateException("Argument pageClass cannot be null");
		}
		redirectToInterceptPage(interceptPageClass);
	}

	/**
	 * Redirects to intercept page using the page map for the current request
	 * 
	 * @param interceptPage
	 *            The intercept page to redirect to
	 */
	private void redirectToInterceptPage(final Page interceptPage)
	{
		final Page requestPage = RequestCycle.get().getRequest().getPage();

		/*
		 * requestPage can be null if we throw the restart response exception before any page is
		 * instantiated in user's session. if this happens we switch to the pagemap of the
		 * interceptPage
		 */
		final IPageMap pageMap;
		if (requestPage != null)
		{
			pageMap = requestPage.getPageMap();
		}
		else
		{
			pageMap = interceptPage.getPageMap();
		}

		pageMap.redirectToInterceptPage(interceptPage);
	}

	/**
	 * Redirects to intercept page using the page map for the current request
	 * 
	 * @param <C> The page type
	 * 
	 * @param interceptPageClass
	 *            The intercept page class to redirect to
	 */
	private <C extends Page> void redirectToInterceptPage(final Class<C> interceptPageClass)
	{
		final RequestCycle cycle = RequestCycle.get();
		final Page requestPage = cycle.getRequest().getPage();

		/*
		 * requestPage can be null if we throw the restart response exception before any page is
		 * instantiated in user's session. if this happens we switch to the pagemap of the request.
		 */
		final IPageMap pageMap;
		if (requestPage != null)
		{
			pageMap = requestPage.getPageMap();
		}
		else
		{
			RequestParameters parameters = cycle.getRequest().getRequestParameters();
			pageMap = PageMap.forName(parameters.getPageMapName());
		}

		pageMap.redirectToInterceptPage(interceptPageClass);
	}
}
