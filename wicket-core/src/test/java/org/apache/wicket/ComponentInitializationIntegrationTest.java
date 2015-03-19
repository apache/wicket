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
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 */
public class ComponentInitializationIntegrationTest extends WicketTestCase
{
	/**
	 * initialization()
	 */
	@Test
	public void initialization()
	{
		TestPage page = new TestPage();
		TestComponent t1 = new TestComponent("t1");
		page.add(t1);

		assertEquals(0, page.getCount());
		assertEquals(0, t1.getCount());
		tester.startPage(page);
		assertEquals(1, page.getCount());
		assertEquals(1, t1.getCount());
	}


	static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private int count;

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><div wicket:id='t1'></div></body></html>");
		}

		@Override
		protected void onInitialize()
		{
			super.onInitialize();
			count++;
		}

		public int getCount()
		{
			return count;
		}

	}

	private static class TestComponent extends WebMarkupContainer
	{
		private static final long serialVersionUID = 1L;

		private int count = 0;

		public TestComponent(String id)
		{
			super(id);
		}

		@Override
		protected void onInitialize()
		{
			super.onInitialize();
			count++;
		}

		public int getCount()
		{
			return count;
		}


	}

}
