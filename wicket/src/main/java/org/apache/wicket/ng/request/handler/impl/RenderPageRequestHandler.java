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

import org.apache.wicket.ng.Application;
import org.apache.wicket.ng.request.RequestHandler;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.PageClassRequestHandler;
import org.apache.wicket.ng.request.handler.PageProvider;
import org.apache.wicket.ng.request.handler.PageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.render.RenderPageRequestHandlerDelegate;
import org.apache.wicket.util.lang.Checks;

/**
 * {@link RequestHandler} that renders page instance. Depending on the <code>redirectPolicy</code>
 * flag and current request strategy the handler either just renders the page to the response, or
 * redirects to render the page. <code>REDIRECT_TO_BUFFER</code> strategy is also supported.
 * <p>
 * 
 * @author Matej Knopp
 */
public class RenderPageRequestHandler implements PageRequestHandler, PageClassRequestHandler
{
	private final PageProvider pageProvider;
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
	public RenderPageRequestHandler(PageProvider pageProvider)
	{
		this(pageProvider, RedirectPolicy.AUTO_REDIRECT);
	}

	/**
	 * Construct.
	 * 
	 * @param pageProvider
	 * @param redirectPolicy
	 */
	public RenderPageRequestHandler(PageProvider pageProvider, RedirectPolicy redirectPolicy)
	{
		Checks.argumentNotNull(pageProvider, "pageProvider");
		Checks.argumentNotNull(redirectPolicy, "redirectPolicy");

		this.redirectPolicy = redirectPolicy;
		this.pageProvider = pageProvider;
	}

	/**
	 * @return page provider
	 */
	public PageProvider getPageProvider()
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

	public Class<? extends RequestablePage> getPageClass()
	{
		return pageProvider.getPageClass();
	}

	public PageParameters getPageParameters()
	{
		return pageProvider.getPageParameters();
	}

	public void detach(RequestCycle requestCycle)
	{
		pageProvider.detach();
	}

	public RequestablePage getPage()
	{
		return pageProvider.getPageInstance();
	}

	public void respond(RequestCycle requestCycle)
	{
		RenderPageRequestHandlerDelegate delegate = Application.get()
			.getRenderPageRequestHandlerDelegate(this);
		delegate.respond(requestCycle);
	}
}
