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
package org.apache.wicket.ng.mock;

import org.apache.wicket.ng.protocol.http.WebApplication;
import org.apache.wicket.ng.request.component.IRequestablePage;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.cycle.RequestCycleContext;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.render.RenderPageRequestHandlerDelegate;
import org.apache.wicket.ng.request.handler.impl.render.WebRenderPageRequestHandlerDelegate;
import org.apache.wicket.ng.session.ISessionStore;
import org.apache.wicket.pageStore.IPageManager;
import org.apache.wicket.pageStore.IPageManagerContext;

public class MockApplication extends WebApplication
{

	public MockApplication()
	{
	}

	@Override
	public Class<? extends IRequestablePage> getHomePage()
	{
		return null;
	}

	@Override
	protected MockRequestCycle newRequestCycle(RequestCycleContext context)
	{
		return new MockRequestCycle(context);
	}

	private IRequestablePage lastRenderedPage;

	public IRequestablePage getLastRenderedPage()
	{
		return lastRenderedPage;
	}

	public void clearLastRenderedPage()
	{
		lastRenderedPage = null;
	}

	@Override
	public RenderPageRequestHandlerDelegate getRenderPageRequestHandlerDelegate(
		RenderPageRequestHandler renderPageRequestHandler)
	{
		return new WebRenderPageRequestHandlerDelegate(renderPageRequestHandler)
		{
			@Override
			public void respond(RequestCycle requestCycle)
			{
				lastRenderedPage = getPageProvider().getPageInstance();
				super.respond(requestCycle);
			}
		};
	}

	@Override
	protected IPageManager newPageManager(IPageManagerContext context)
	{
		return new MockPageManager(context);
	}

	@Override
	protected ISessionStore newSessionStore()
	{
		return new MockSessionStore();
	}
}
