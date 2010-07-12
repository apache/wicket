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

import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;

/**
 * Tests {@link Component#onInitialize()} contract
 * 
 * @author igor
 */
public class ComponentInitializationTest extends WicketTestCase
{
	public void testPropagation()
	{
		TestPage page = new TestPage();

		TestComponent t1 = new TestComponent("t1");
		TestComponent t2 = new TestComponent("t2");
		TestComponent t3 = new TestComponent("t3");
		TestComponent t4 = new TestComponent("t4");

		// as soon as we add to page child should be initialized
		page.add(t1);
		assertEquals(1, t1.getCount());

		// unless the page is available no initialization takes place
		t2.add(t3);
		assertEquals(0, t2.getCount());
		assertEquals(0, t3.getCount());

		// initialization cascades from initialized
		t1.add(t2);
		assertEquals(1, t1.getCount());
		assertEquals(1, t2.getCount());
		assertEquals(1, t3.getCount());

		// test intialization when adding to removed components
		page.remove(t1);
		t3.add(t4);
		assertEquals(0, t4.getCount());

		// test initialization when readding a component with uninitialized children
		page.add(t1);
		assertEquals(1, t4.getCount());

		// test page was initialized
		assertEquals(1, page.getCount());

	}

	public void testAtomicity()
	{
		TestPage page = new TestPage();

		TestComponent t1 = new TestComponent("t1");
		TestComponent t2 = new TestComponent("t2");
		TestComponent t3 = new TestComponent("t3");

		t1.add(t2);
		t2.add(t3);

		page.add(t1);

		assertEquals(1, t1.getCount());
		assertEquals(1, t2.getCount());
		assertEquals(1, t3.getCount());

		// test moving
		page.add(t3);
		assertEquals(1, t3.getCount());

		// test removal and readdition
		page.remove(t1);
		assertEquals(1, t1.getCount());
		page.add(t1);
		assertEquals(1, t1.getCount());
		assertEquals(1, t2.getCount());

		// test page was only initialized once
		assertEquals(1, page.getCount());
	}


	private static class TestPage extends WebPage
	{
		private int count = 0;

		@Override
		protected void onInitialize()
		{
			count++;
		}

		public int getCount()
		{
			return count;
		}
	}

	private static class TestComponent extends WebMarkupContainer
	{
		private int count = 0;

		public TestComponent(String id)
		{
			super(id);
		}

		@Override
		protected void onInitialize()
		{
			count++;
		}

		public int getCount()
		{
			return count;
		}


	}
}
