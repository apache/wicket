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

import org.apache.wicket.ng.request.component.PageParametersNg;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.PageClassRequestHandler;
import org.apache.wicket.ng.request.handler.PageProvider;
import org.apache.wicket.util.lang.Checks;

/**
 * Request handler for bookmarkable pages. This handler is only used to generate URLs. Rendering is
 * always handled by {@link RenderPageRequestHandler}.
 * 
 * @author Matej Knopp
 */
public class BookmarkablePageRequestHandler implements PageClassRequestHandler
{
	private final PageProvider pageProvider;
  
	/**
	 * Construct.
	 * 
	 * @param pageProvider
	 */
	public BookmarkablePageRequestHandler(PageProvider pageProvider)
	{
		
		Checks.argumentNotNull(pageProvider, "pageProvider");
		
		this.pageProvider = pageProvider;
	}

	public Class<? extends RequestablePage> getPageClass()
	{
		return pageProvider.getPageClass();
	}

	public PageParametersNg getPageParameters()
	{
		return pageProvider.getPageParameters();
	}

	public void respond(RequestCycle requestCycle)
	{
		// not used as BookmarkablePageRequestHandler is only used when generating URLs.
		// However URL will never be resolved to BookmarkablePageRequestTarget
	}

	public void detach(RequestCycle requestCycle)
	{
	}

}
