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

import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.protocol.http.IMetaDataBufferingWebResponse;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.RequestHandlerStack;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.http.handler.RedirectRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Causes Wicket to interrupt current request processing and immediately respond with the specified
 * page. Does not reset the header meta data (e.g. cookies).
 *
 * @see RestartResponseException
 */
public class NonResettingRestartException extends RequestHandlerStack.ReplaceHandlerException
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor.
	 *
	 * @param pageClass
	 *      the class of the new page that should be rendered
	 */
	public NonResettingRestartException(final Class<? extends Page> pageClass)
	{
		this(pageClass, null);
	}

	/**
	 * Constructor.
	 *
	 * @param pageClass
	 *      the class of the new page that should be rendered
	 * @param params
	 *      the page parameters to use for the new page
	 */
	public NonResettingRestartException(final Class<? extends Page> pageClass, final PageParameters params)
	{
		this(pageClass, params, RenderPageRequestHandler.RedirectPolicy.AUTO_REDIRECT);
	}

	/**
	 * Constructor.
	 *
	 * @param pageClass
	 *      the class of the new page that should be rendered
	 * @param params
	 *      the page parameters to use for the new page
	 * @param redirectPolicy
	 *      the policy that mandates whether to do a redirect
	 */
	public NonResettingRestartException(final Class<? extends Page> pageClass,
		final PageParameters params, RenderPageRequestHandler.RedirectPolicy redirectPolicy)
	{
		this(createRequestHandler(pageClass, params, redirectPolicy), true);
	}

	/**
	 * Constructor.
	 *
	 * @param redirectUrl
	 *      URL to redirect to.
	 */
	public NonResettingRestartException(final String redirectUrl)
	{
		this(createUrlRequestHandler(redirectUrl), true);
	}

	/**
	 * Constructor.
	 *
	 * @param handler
	 *      the IRequestHandler to use
	 * @param removeAll
	 *      a flag indicating whether to ignore all already scheduled IRequestHandlers before throwing
	 *      this exception
	 */
	public NonResettingRestartException(final IRequestHandler handler, boolean removeAll)
	{
		super(handler, removeAll);

		transferResponseMetaData();
	}

	private void transferResponseMetaData()
	{
		RequestCycle cycle = RequestCycle.get();
		Response response = cycle.getResponse();
		if (response instanceof IMetaDataBufferingWebResponse)
		{
			WebResponse originalResponse = (WebResponse) cycle.getOriginalResponse();
			if (originalResponse != response)
			{
				IMetaDataBufferingWebResponse bufferingWebResponse = (IMetaDataBufferingWebResponse) response;
				bufferingWebResponse.writeMetaData(originalResponse);
			}
		}
	}

	private static IRequestHandler createRequestHandler(Class<? extends Page> pageClass, PageParameters params,
			RenderPageRequestHandler.RedirectPolicy redirectPolicy)
	{
		return new RenderPageRequestHandler(new PageProvider(pageClass, params), redirectPolicy);
	}

	private static IRequestHandler createUrlRequestHandler(final String redirectUrl)
	{
		return new RedirectRequestHandler(redirectUrl, HttpServletResponse.SC_MOVED_TEMPORARILY);
	}
}
