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
package org.apache.wicket.request.handler.render;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.IPageProvider;
import org.apache.wicket.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.settings.IRequestCycleSettings.RenderStrategy;

/**
 * Delegate responsible for rendering the page. Depending on the implementation (web, test, portlet,
 * etc.) the delegate may or may not support the redirect policy set in the
 * {@link RenderPageRequestHandler}.
 * 
 * @author Matej Knopp
 */
public abstract class PageRenderer
{
	private final RenderPageRequestHandler renderPageRequestHandler;

	/**
	 * Construct.
	 * 
	 * @param renderPageRequestHandler
	 */
	public PageRenderer(RenderPageRequestHandler renderPageRequestHandler)
	{
		this.renderPageRequestHandler = renderPageRequestHandler;
	}

	/**
	 * @return page provider
	 */
	protected IPageProvider getPageProvider()
	{
		return renderPageRequestHandler.getPageProvider();
	}

	/**
	 * @return redirect policy
	 */
	protected RedirectPolicy getRedirectPolicy()
	{
		return renderPageRequestHandler.getRedirectPolicy();
	}

	/**
	 * @return the request handler
	 */
	protected RenderPageRequestHandler getRenderPageRequestHandler()
	{
		return renderPageRequestHandler;
	}

	/**
	 * @return page instance
	 */
	protected IRequestablePage getPage()
	{
		return getPageProvider().getPageInstance();
	}

	protected boolean isOnePassRender()
	{
		return Application.get().getRequestCycleSettings().getRenderStrategy() == RenderStrategy.ONE_PASS_RENDER;
	}

	protected boolean isRedirectToRender()
	{
		return Application.get().getRequestCycleSettings().getRenderStrategy() == RenderStrategy.REDIRECT_TO_RENDER;
	}

	protected boolean isRedirectToBuffer()
	{
		return Application.get().getRequestCycleSettings().getRenderStrategy() == RenderStrategy.REDIRECT_TO_BUFFER;
	}

	/**
	 * @return the current session id for stateful pages and <code>null</code> for stateless pages
	 */
	protected String getSessionId()
	{
		return Session.get().getId();
	}

	protected boolean isSessionTemporary()
	{
		return Session.get().isTemporary();
	}


	/**
	 * Render the response using give {@link RequestCycle}.
	 * 
	 * @param requestCycle
	 */
	public abstract void respond(RequestCycle requestCycle);
}
