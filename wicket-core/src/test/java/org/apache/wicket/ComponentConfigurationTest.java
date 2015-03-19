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

import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * Tests {@link Component#onInitialize()} contract
 * 
 * @author igor
 */
public class ComponentConfigurationTest extends WicketTestCase
{
	/**
	 * testOnlyOncePerRequest()
	 */
	@Test
	public void onlyOncePerRequest()
	{
		TestComponent t1 = new TestComponent("t1");
		assertEquals(0, t1.getTotalCount());
		assertEquals(0, t1.getRequestCount());
		t1.configure();
		t1.configure();
		assertEquals(1, t1.getTotalCount());
		assertEquals(1, t1.getRequestCount());
		t1.detach();
		assertEquals(1, t1.getTotalCount());
		assertEquals(0, t1.getRequestCount());
		t1.configure();
		t1.configure();
		assertEquals(2, t1.getTotalCount());
		assertEquals(1, t1.getRequestCount());

	}

	/**
	 * testConfiguration()
	 */
	@Test
	public void configuration()
	{
		tester.startPage(TestPage.class);
		TestPage page = (TestPage)tester.getLastRenderedPage();

		// 1st render
		assertEquals(0, page.getT1().getRequestCount()); // cleaned up by detach
		assertEquals(0, page.getT2().getRequestCount()); // cleaned up by detach
		assertEquals(1, page.getT1().getTotalCount());
		assertEquals(1, page.getT2().getTotalCount());
		assertEquals(1, page.getT1().getBeforeRenderCount());
		assertEquals(1, page.getT2().getBeforeRenderCount());

		tester.clickLink(page.getLink().getPageRelativePath());

		// t1 is now invisible, make sure onConfigure is still called
		assertFalse(page.getT1().isVisible());
		assertEquals(2, page.getT1().getTotalCount());
		assertEquals(2, page.getT2().getTotalCount());
		assertEquals(1, page.getT1().getBeforeRenderCount()); // stays at 1
		assertEquals(2, page.getT2().getBeforeRenderCount()); // up to two

	}

	/**
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private final TestComponent t1;
		private final TestComponent t2;
		private final Link<?> link;

		/**
		 * Construct.
		 */
		public TestPage()
		{
			add(t1 = new TestComponent("t1"));
			add(t2 = new TestComponent("t2"));
			add(link = new Link<Void>("link")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					t1.setVisible(!t1.isVisible());
				}
			});
		}

		TestComponent getT1()
		{
			return t1;
		}

		TestComponent getT2()
		{
			return t2;
		}

		Link<?> getLink()
		{
			return link;
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><div wicket:id=\"t1\"></div><div wicket:id=\"t2\"></div><a wicket:id=\"link\"></a></body></html>");
		}
	}

	private static class TestComponent extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		private int requestCount = 0;
		private int totalCount = 0;
		private int beforeRenderCount = 0;

		public TestComponent(String id)
		{
			super(id);
		}

		@Override
		protected void onConfigure()
		{
			requestCount++;
			totalCount++;
		}

		@Override
		protected void onBeforeRender()
		{
			beforeRenderCount++;
			super.onBeforeRender();
		}

		public int getBeforeRenderCount()
		{
			return beforeRenderCount;
		}

		public int getRequestCount()
		{
			return requestCount;
		}


		public int getTotalCount()
		{
			return totalCount;
		}

		@Override
		protected void onDetach()
		{
			requestCount = 0;
			super.onDetach();
		}

	}
}
