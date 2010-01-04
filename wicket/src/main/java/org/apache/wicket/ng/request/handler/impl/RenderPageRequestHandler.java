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
package org.apache.wicket.ng.request.handler.impl;

import org.apache.wicket.Application;
import org.apache.wicket.IRequestHandler;
import org.apache.wicket.ng.request.component.IRequestablePage;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.IPageClassRequestHandler;
import org.apache.wicket.ng.request.handler.IPageProvider;
import org.apache.wicket.ng.request.handler.IPageRequestHandler;
import org.apache.wicket.util.lang.Checks;

/**
 * {@link IRequestHandler} that renders page instance. Depending on the <code>redirectPolicy</code>
 * flag and current request strategy the handler either just renders the page to the response, or
 * redirects to render the page. <code>REDIRECT_TO_BUFFER</code> strategy is also supported.
 * <p>
 * 
 * @author Matej Knopp
 */
public class RenderPageRequestHandler implements IPageRequestHandler, IPageClassRequestHandler
{
	private final IPageProvider pageProvider;

	private final RedirectPolicy redirectPolicy;

	/**
	 * Determines whether Wicket does a redirect when rendering a page
	 * 
	 * @author Matej Knopp
	 */
	public enum RedirectPolicy {
		/**
		 * Always redirect if current request URL is different than page URL.
		 */
		ALWAYS_REDIRECT,

		/**
		 * Never redirect - always render the page to current response.
		 */
		NEVER_REDIRECT,

		/**
		 * Redirect if necessary. The redirect will happen when all of the following conditions are
		 * met:
		 * <ul>
		 * <li>current request URL is different than page URL
		 * <li>page is not stateless or (page is stateless and session is not temporary)
		 * <li>render strategy is either REDIRECT_TO_BUFFER or REDIRECT_TO_RENDER
		 * </ul>
		 */
		AUTO_REDIRECT
	};

	/**
	 * Construct. Renders the page with a redirect if necessary.
	 * 
	 * @param pageProvider
	 */
	public RenderPageRequestHandler(IPageProvider pageProvider)
	{
		this(pageProvider, RedirectPolicy.AUTO_REDIRECT);
	}

	/**
	 * Construct.
	 * 
	 * @param pageProvider
	 * @param redirectPolicy
	 */
	public RenderPageRequestHandler(IPageProvider pageProvider, RedirectPolicy redirectPolicy)
	{
		Checks.argumentNotNull(pageProvider, "pageProvider");
		Checks.argumentNotNull(redirectPolicy, "redirectPolicy");

		this.redirectPolicy = redirectPolicy;
		this.pageProvider = pageProvider;
	}

	/**
	 * @return page provider
	 */
	public IPageProvider getPageProvider()
	{
		return pageProvider;
	}

	/**
	 * @return redirect policy
	 */
	public RedirectPolicy getRedirectPolicy()
	{
		return redirectPolicy;
	}

	/**
	 * @see org.apache.wicket.ng.request.handler.IPageClassRequestHandler#getPageClass()
	 */
	public Class<? extends IRequestablePage> getPageClass()
	{
		return pageProvider.getPageClass();
	}

	/**
	 * @see org.apache.wicket.ng.request.handler.IPageClassRequestHandler#getPageParameters()
	 */
	public PageParameters getPageParameters()
	{
		return pageProvider.getPageParameters();
	}

	/**
	 * @see org.apache.wicket.ng.request.IRequestHandler#detach(org.apache.wicket.ng.request.cycle.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
	{
		pageProvider.detach();
	}

	/**
	 * @see org.apache.wicket.ng.request.handler.IPageRequestHandler#getPage()
	 */
	public IRequestablePage getPage()
	{
		return pageProvider.getPageInstance();
	}

	/**
	 * @see org.apache.wicket.ng.request.IRequestHandler#respond(org.apache.wicket.ng.request.cycle.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		Application.get().getRenderPageRequestHandlerDelegate(this).respond(requestCycle);
	}
}
