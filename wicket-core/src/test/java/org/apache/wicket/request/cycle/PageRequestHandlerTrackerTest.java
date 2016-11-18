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
package org.apache.wicket.request.cycle;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.core.request.handler.RequestSettingRequestHandler;
import org.apache.wicket.core.request.mapper.CryptoMapper;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.IRequestMapperDelegate;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * @since 1.5.8
 */
public class PageRequestHandlerTrackerTest extends WicketTestCase
{
	
	@Before
	public void before()
	{
		tester.getApplication().getRequestCycleListeners().add(new PageRequestHandlerTracker());
	}
	
	/**
	 * Uses PageRequestHandlerTracker to track the requested page and the response page
	 * https://issues.apache.org/jira/browse/WICKET-4624
	 */
	@Test
	public void trackPages()
	{
		tester.getApplication().getRequestCycleListeners().add(new IRequestCycleListener()
		{
			@Override
			public void onDetach(RequestCycle cycle)
			{
				IPageRequestHandler firstHandler = PageRequestHandlerTracker.getFirstHandler(cycle);
				if (firstHandler != null)
				{
					assertEquals(PageA.class, firstHandler.getPageClass());
				}

				IPageRequestHandler lastHandler = PageRequestHandlerTracker.getLastHandler(cycle);
				if (lastHandler != null)
				{
					assertEquals(PageB.class, lastHandler.getPageClass());
				}
			}
		});

		tester.startPage(new PageA());
	}
	
	/**
	 * Uses PageRequestHandlerTracker to check last IRequestHandler encapsulated in IRequestHandlerDelegate 
	 */
	@Test
	public void trackPagesWithMapperDelegate()
	{
		IRequestMapper mapper = tester.getApplication().getRootRequestMapper();
		tester.getApplication().setRootRequestMapper(new MapperDelegate(mapper));
		tester.startPage(new PageA());
		tester.getApplication().setRootRequestMapper(mapper);
	}
	

	/**
	 * The requested page
	 */
	private static class PageA extends WebPage implements IMarkupResourceStreamProvider
	{
		public PageA()
		{
			// make stateful so it is rendered in first requestCycle (without redirect) 
			setStatelessHint(false);
		}

		@Override
		protected void onConfigure()
		{
			super.onConfigure();


			IPageRequestHandler lastHandler = PageRequestHandlerTracker.getLastHandler(getRequestCycle());
			assertNotNull("Last handler is null: probably issue in IRequestHandlerDelegate support", lastHandler);
			
			setResponsePage(new PageB());
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html/>");
		}
	}

	/**
	 * The response page
	 */
	private static class PageB extends WebPage implements IMarkupResourceStreamProvider
	{
		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html/>");
		}
	}
	
	/**
	 * Mapper which imitate behavior of {@link CryptoMapper#mapRequest(Request)}
	 */
	private static class MapperDelegate implements IRequestMapperDelegate 
	{
		IRequestMapper mapper;
		
		public MapperDelegate(IRequestMapper mapper)
		{
			this.mapper = mapper;
		}

		@Override
		public IRequestHandler mapRequest(Request request)
		{
			Request decryptedRequest = request.cloneWithUrl(request.getUrl());
			IRequestHandler handler = mapper.mapRequest(decryptedRequest);

			if (handler != null)
			{
				handler = new RequestSettingRequestHandler(decryptedRequest, handler);
			}

			return handler;
		}

		@Override
		public int getCompatibilityScore(Request request)
		{
			return mapper.getCompatibilityScore(request);
		}

		@Override
		public Url mapHandler(IRequestHandler requestHandler)
		{
			return mapper.mapHandler(requestHandler);
		}

		@Override
		public IRequestMapper getDelegateMapper()
		{
			return mapper;
		}
		
	}
}
