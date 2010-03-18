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
package org.apache.wicket.request.handler.impl;

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.handler.IPageClassRequestHandler;
import org.apache.wicket.request.handler.IPageProvider;
import org.apache.wicket.request.mapper.parameters.PageParameters;
import org.apache.wicket.util.lang.Checks;

/**
 * Request handler for bookmarkable pages. This handler is only used to generate URLs. Rendering is
 * always handled by {@link RenderPageRequestHandler}.
 * 
 * @author Matej Knopp
 */
public class BookmarkablePageRequestHandler implements IPageClassRequestHandler
{
	private final IPageProvider pageProvider;

	/**
	 * Construct.
	 * 
	 * @param pageProvider
	 */
	public BookmarkablePageRequestHandler(IPageProvider pageProvider)
	{

		Checks.argumentNotNull(pageProvider, "pageProvider");

		this.pageProvider = pageProvider;
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageClassRequestHandler#getPageClass()
	 */
	public Class<? extends IRequestablePage> getPageClass()
	{
		return pageProvider.getPageClass();
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageClassRequestHandler#getPageParameters()
	 */
	public PageParameters getPageParameters()
	{
		return pageProvider.getPageParameters();
	}

	/**
	 * @see org.apache.org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.cycle.RequestCycle)
	 */
	public void respond(IRequestCycle requestCycle)
	{
		// not used as BookmarkablePageRequestHandler is only used when generating URLs.
		// However URL will never be resolved to BookmarkablePageRequestTarget
	}

	/**
	 * @see org.apache.org.apache.wicket.request.IRequestHandler#detach(org.apache.wicket.request.cycle.RequestCycle)
	 */
	public void detach(IRequestCycle requestCycle)
	{
	}
}
