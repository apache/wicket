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
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * @since 1.5.8
 */
public class PageRequestHandlerTrackerTest extends WicketTestCase
{
	/**
	 * Uses PageRequestHandlerTracker to track the requested page and the response page
	 * https://issues.apache.org/jira/browse/WICKET-4624
	 */
	@Test
	public void trackPages()
	{
		tester.getApplication().getRequestCycleListeners().add(new PageRequestHandlerTracker());
		tester.startPage(new PageA());

	}

	/**
	 * The requested page
	 */
	private static class PageA extends WebPage implements IMarkupResourceStreamProvider
	{
		@Override
		protected void onBeforeRender()
		{
			super.onBeforeRender();
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
		protected void onAfterRender()
		{
			super.onAfterRender();

			RequestCycle cycle = getRequestCycle();

			IPageRequestHandler firstHandler = PageRequestHandlerTracker.getFirstHandler(cycle);
			assertEquals(PageA.class, firstHandler.getPageClass());

			IPageRequestHandler lastHandler = PageRequestHandlerTracker.getLastHandler(cycle);
			assertEquals(PageB.class, lastHandler.getPageClass());
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html/>");
		}
	}
}
