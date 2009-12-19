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
package org.apache.wicket.ng.request.handler.impl.render;

import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.IPageProvider;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler.RedirectPolicy;

/**
 * Delegate responsible for rendering the page. Depending on the implementation (web, test, portlet,
 * etc.) the delegate may or may not support the redirect policy set in the
 * {@link RenderPageRequestHandler}.
 * 
 * @author Matej Knopp
 */
public abstract class RenderPageRequestHandlerDelegate
{
	private final RenderPageRequestHandler renderPageRequestHandler;

	/**
	 * Construct.
	 * 
	 * @param renderPageRequestHandler
	 */
	public RenderPageRequestHandlerDelegate(RenderPageRequestHandler renderPageRequestHandler)
	{
		this.renderPageRequestHandler = renderPageRequestHandler;
	}

	/**
	 * @return page provider
	 */
	public IPageProvider getPageProvider()
	{
		return renderPageRequestHandler.getPageProvider();
	}

	/**
	 * @return redirect policy
	 */
	public RedirectPolicy getRedirectPolicy()
	{
		return renderPageRequestHandler.getRedirectPolicy();
	}

	/**
	 * @return the request handler
	 */
	public RenderPageRequestHandler getRenderPageRequestHandler()
	{
		return renderPageRequestHandler;
	}

	/**
	 * Render the response using give {@link RequestCycle}.
	 * 
	 * @param requestCycle
	 */
	public abstract void respond(RequestCycle requestCycle);
}
