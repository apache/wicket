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

import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.flow.ResetResponseException;
import org.apache.wicket.request.handler.IPageProvider;
import org.apache.wicket.request.handler.PageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Causes Wicket to interrupt current request processing and immediately respond with the specified
 * page.
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class RestartResponseException extends ResetResponseException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Redirects to the specified bookmarkable page
	 * 
	 * @param <C>
	 *            The page type
	 * 
	 * @param pageClass
	 *            class of bookmarkable page
	 */
	public <C extends Page> RestartResponseException(final Class<C> pageClass)
	{
		this(pageClass, null);
	}

	/**
	 * Redirects to the specified bookmarkable page with the given page parameters
	 * 
	 * @param <C>
	 *            The page type
	 * 
	 * @param pageClass
	 *            class of bookmarkable page
	 * @param params
	 *            bookmarkable page parameters
	 */
	public <C extends Page> RestartResponseException(final Class<C> pageClass,
		final PageParameters params)
	{
		this(new PageProvider(pageClass, params), RedirectPolicy.ALWAYS_REDIRECT);
	}

	/**
	 * Redirects to the specified page
	 * 
	 * @param page
	 *            redirect page
	 */
	public RestartResponseException(final IRequestablePage page)
	{
		this(new PageProvider(makeStateful(page)), RedirectPolicy.AUTO_REDIRECT);
	}

	/**
	 * Redirects to the page provided by the passed {@code pageProvider} using the explicit
	 * {@code redirectPolicy}
	 * 
	 * @param pageProvider
	 *            the provider for the page
	 * @param redirectPolicy
	 *            the redirect policy to use
	 */
	public RestartResponseException(final IPageProvider pageProvider,
		final RedirectPolicy redirectPolicy)
	{
		super(new RenderPageRequestHandler(pageProvider, redirectPolicy));
	}

	/**
	 * Marks the page as stateful so that it is still available after a redirect.
	 * See WICKET-5078 and WICKET-3965
	 *
	 * @param page
	 *      The page to mark stateful
	 * @return The passed page.
	 *
	 */
	private static IRequestablePage makeStateful(IRequestablePage page)
	{
		if (page instanceof Page)
		{
			((Page)page).setStatelessHint(false);
		}
		return page;
	}
}
