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
package org.apache.wicket;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.application.IComponentOnConfigureListener;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * Tests {@link Component#onConfigure()}
 */
public class ComponentOnConfigureTest extends WicketTestCase
{
	private static final AtomicInteger COUNTER = new AtomicInteger(0);
	private static final AtomicInteger PAGE = new AtomicInteger(0);
	private static final AtomicInteger COMPONENT = new AtomicInteger(0);
	private static final AtomicInteger BEHAVIOR = new AtomicInteger(0);
	private static final AtomicInteger APPLICATION_LISTENER = new AtomicInteger(0);

	/**
	 * testOrder()
	 */
	@Test
	public void order()
	{
		tester.getApplication().getComponentOnConfigureListeners().add(new TestInitListener());

		tester.startPage(new TestPage());

		assertEquals("Page must be configured first!", 0, PAGE.get());
//		assertEquals("Application listener for page must be configured second!", 1, APPLICATION_LISTENER.get());
		assertEquals("Component must be configured third!", 2, COMPONENT.get());
		assertEquals("The behavior must be configured fourth!", 3, BEHAVIOR.get());
//		assertEquals("Application listener for HtmlHeaderContainer must be configured second!",
//          4, APPLICATION_LISTENER.get());
		assertEquals("The application listener for the component must be configured fifth!",
				5, APPLICATION_LISTENER.get());
	}

	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private TestPage()
		{
			add(new TestComponent("c"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><span wicket:id='c'></span></body></html>");
		}

		@Override
		protected void onConfigure()
		{
			super.onConfigure();
			PAGE.set(COUNTER.getAndIncrement());
		}
	}

	private static class TestComponent extends WebMarkupContainer
	{
		public TestComponent(String id)
		{
			super(id);
			add(new TestBehavior());
		}

		@Override
		protected void onConfigure()
		{
			super.onConfigure();
			COMPONENT.set(COUNTER.getAndIncrement());
		}
	}

	private static class TestBehavior extends Behavior
	{
		@Override
		public void onConfigure(Component component)
		{
			super.onConfigure(component);
			BEHAVIOR.set(COUNTER.getAndIncrement());
		}
	}

	private static class TestInitListener implements IComponentOnConfigureListener
	{
		@Override
		public void onConfigure(Component component)
		{
			APPLICATION_LISTENER.set(COUNTER.getAndIncrement());
		}
	}
}
