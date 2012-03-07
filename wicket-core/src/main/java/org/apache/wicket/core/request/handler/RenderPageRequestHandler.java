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
package org.apache.wicket.core.request.handler;

import org.apache.wicket.Application;
import org.apache.wicket.core.request.handler.logger.PageLogData;
import org.apache.wicket.request.ILoggableRequestHandler;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.render.PageRenderer;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link IRequestHandler} that renders page instance. Depending on the <code>redirectPolicy</code>
 * flag and current request strategy the handler either just renders the page to the response, or
 * redirects to render the page. <code>REDIRECT_TO_BUFFER</code> strategy is also supported.
 * <p>
 *
 * @author Matej Knopp
 */
public class RenderPageRequestHandler
	implements
		IPageRequestHandler,
		IPageClassRequestHandler,
		ILoggableRequestHandler
{
	private static final Logger logger = LoggerFactory.getLogger(RenderPageRequestHandler.class);

	private final IPageProvider pageProvider;

	private final RedirectPolicy redirectPolicy;

	private PageLogData logData;

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
	}

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
		Args.notNull(pageProvider, "pageProvider");
		Args.notNull(redirectPolicy, "redirectPolicy");

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

	@Override
	public Class<? extends IRequestablePage> getPageClass()
	{
		return pageProvider.getPageClass();
	}

	@Override
	public Integer getPageId()
	{
		return pageProvider.getPageId();
	}

	@Override
	public PageParameters getPageParameters()
	{
		return pageProvider.getPageParameters();
	}

	@Override
	public void detach(IRequestCycle requestCycle)
	{
		if (logData == null)
			logData = new PageLogData(pageProvider);
		pageProvider.detach();
	}

	@Override
	public PageLogData getLogData()
	{
		return logData;
	}

	@Override
	public IRequestablePage getPage()
	{
		return pageProvider.getPageInstance();
	}

	@Override
	public void respond(IRequestCycle requestCycle)
	{
		PageRenderer renderer = Application.get().getPageRendererProvider().get(this);
		renderer.respond((RequestCycle)requestCycle);
	}

	@Override
	public final boolean isPageInstanceCreated()
	{
		return pageProvider.hasPageInstance();
	}

	@Override
	public final Integer getRenderCount()
	{
		return pageProvider.getRenderCount();
	}
}
