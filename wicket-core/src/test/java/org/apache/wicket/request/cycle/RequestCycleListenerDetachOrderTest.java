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

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;


/**
 * Tests that pages are detached before {@link IRequestCycleListener#onDetach(RequestCycle)} are
 * invoked
 * 
 * WICKET-4181
 * 
 * @author igor
 */
public class RequestCycleListenerDetachOrderTest
{
	@Test
	public void pageDetachedBeforeListener()
	{
		List<Event> events = new ArrayList<Event>();

		WicketTester tester = new WicketTester();
		tester.getApplication().getRequestCycleListeners().add(new TestListener(events));
		tester.startPage(new TestPage(events));

		assertEquals(Event.PAGE_DETACHED, events.get(0));
		assertEquals(Event.LISTENER_DETACHED, events.get(1));
	}

	private class TestListener extends AbstractRequestCycleListener
	{
		private final List<Event> events;

		public TestListener(List<Event> events)
		{
			this.events = events;
		}

		@Override
		public void onDetach(RequestCycle cycle)
		{
			events.add(Event.LISTENER_DETACHED);
		}
	}

	private class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private final List<Event> events;

		public TestPage(List<Event> events)
		{
			this.events = events;
		}

		@Override
		protected void onDetach()
		{
			super.onDetach();
			events.add(Event.PAGE_DETACHED);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html></html>");
		}
	}

	private static enum Event {
		PAGE_DETACHED, LISTENER_DETACHED
	};
}
