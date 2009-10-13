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

import org.apache.wicket.ng.Application;
import org.apache.wicket.ng.page.PageManager;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.cycle.RequestCycleContext;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.render.RenderPageRequestHandlerDelegate;
import org.apache.wicket.ng.request.mapper.BookmarkableMapper;
import org.apache.wicket.ng.request.mapper.PageInstanceMapper;
import org.apache.wicket.ng.request.mapper.ResourceReferenceMapper;
import org.apache.wicket.ng.session.SessionStore;

public class MockApplication extends Application
{

	public MockApplication()
	{
	}

	@Override
	protected void registerDefaultEncoders()
	{
		registerEncoder(new PageInstanceMapper());
		registerEncoder(new BookmarkableMapper());
		registerEncoder(new ResourceReferenceMapper());
	}

	@Override
	public Class<? extends RequestablePage> getHomePage()
	{
		return null;
	}

	@Override
	protected MockRequestCycle newRequestCycle(RequestCycleContext context)
	{
		return new MockRequestCycle(context);
	}

	@Override
	public RenderPageRequestHandlerDelegate getRenderPageRequestHandlerDelegate(
		RenderPageRequestHandler renderPageRequestHandler)
	{
		return new MockRenderPageRequestHandlerDelegate(renderPageRequestHandler);
	}

	@Override
	protected PageManager newPageManager()
	{
		return new MockPageManager();
	}

	@Override
	protected SessionStore newSessionStore()
	{
		return new MockSessionStore();
	}
}
