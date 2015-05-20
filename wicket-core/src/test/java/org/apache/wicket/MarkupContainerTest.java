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
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;


/**
 * 
 * @author Juergen Donnerstag
 */
public class MarkupContainerTest extends WicketTestCase
{
	/**
	 * Make sure components are iterated in the order they were added. Required e.g. for Repeaters
	 */
	@Test
	public void iteratorOrder()
	{
		MarkupContainer container = new WebMarkupContainer("component");
		for (int i = 0; i < 10; i++)
		{
			container.add(new WebComponent(Integer.toString(i)));
		}
		int i = 0;
		for (Component component : container)
		{
			assertEquals(Integer.toString(i++), component.getId());
		}
	}

	/**
	 * @throws Exception
	 */
	@Test
	public void markupId() throws Exception
	{
		executeTest(MarkupIdTestPage.class, "MarkupIdTestPageExpectedResult.html");
	}

	/**
	 * 
	 */
	@Test
	public void get()
	{
		WebMarkupContainer a = new WebMarkupContainer("a");
		WebMarkupContainer b = new WebMarkupContainer("b");
		WebMarkupContainer c = new WebMarkupContainer("c");
		WebMarkupContainer d = new WebMarkupContainer("d");
		WebMarkupContainer e = new WebMarkupContainer("e");
		WebMarkupContainer f = new WebMarkupContainer("f");

		// ....A
		// ...B....C
		// .......D..E
		// ...........F

		a.add(b);
		a.add(c);
		c.add(d);
		c.add(e);
		e.add(f);

		// basic gets

		assertTrue(a.get(null) == a);
		assertTrue(a.get("") == a);
		assertTrue(a.get("b") == b);
		assertTrue(a.get("c") == c);
		assertTrue(a.get("c:d") == d);
		assertTrue(a.get("c:e:f") == f);

		// parent path gets

		assertTrue(b.get("..") == a);
		assertTrue(e.get("..:..") == a);
		assertTrue(d.get("..:..:c:e:f") == f);
		assertTrue(e.get("..:d:..:e:f") == f);
		assertTrue(e.get("..:d:..:..") == a);

		// invalid gets

		assertNull(a.get(".."));
		assertNull(a.get("..:a"));
		assertNull(b.get("..|.."));
		assertNull(a.get("q"));
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4006
	 */
	@Test(expected = IllegalArgumentException.class)
	public void addMyself()
	{
		WebMarkupContainer me = new WebMarkupContainer("a");
		me.add(me);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-5911
	 */
	@Test
	public void rerenderAfterRenderFailure()
	{
		FirstRenderFailsPage page = new FirstRenderFailsPage();
		try {
			tester.startPage(page);
		} catch (WicketRuntimeException expected) {
		}

		tester.startPage(page);

		// rendering flags where properly reset, so second rendering works properly
		assertEquals(2, page.beforeRenderCalls);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4012
	 */
	@Test
	public void afterRenderJustOnce()
	{
		AfterRenderJustOncePage page = new AfterRenderJustOncePage();
		tester.startPage(page);

		assertEquals(1, page.afterRenderCalls);
	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4016
	 */
	@Test
	public void callToStringFromConstructor()
	{
		ToStringComponent page = new ToStringComponent();
	}

	private static class ToStringComponent extends WebMarkupContainer
	{
		private ToStringComponent()
		{
			super("id");
			toString(true);
		}
	}

	private static class AfterRenderJustOncePage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		private int afterRenderCalls = 0;

		private AfterRenderJustOncePage()
		{

			WebMarkupContainer a1 = new WebMarkupContainer("a1");
			add(a1);

			WebMarkupContainer a2 = new WebMarkupContainer("a2");
			a1.add(a2);

			WebMarkupContainer a3 = new WebMarkupContainer("a3")
			{

				@Override
				protected void onAfterRender()
				{
					super.onAfterRender();
					afterRenderCalls++;
				}

			};
			a2.add(a3);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><div wicket:id='a1'><div wicket:id='a2'><div wicket:id='a3'></div></div></div></body></html>");
		}
	}

	private static class FirstRenderFailsPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private boolean firstRender = true;

		private int beforeRenderCalls = 0;

		private FirstRenderFailsPage()
		{

			WebMarkupContainer a1 = new WebMarkupContainer("a1") {
				@Override
				protected void onBeforeRender() {
					super.onBeforeRender();

					beforeRenderCalls++;

					if (firstRender) {
						firstRender = false;
						throw new WicketRuntimeException();
					}
				}
			};
			add(a1);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><div wicket:id='a1'></div></body></html>");
		}
	}
}
