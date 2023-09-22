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
package org.apache.wicket.ajax;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * @since 6.0.1
 */
class AjaxEventBehaviorTest extends WicketTestCase
{
	/**
	 * Tests execution of the second configured event
	 * https://issues.apache.org/jira/browse/WICKET-4748
	 */
	@Test
	void executeSecondEvent()
	{
		AtomicInteger counter = new AtomicInteger(0);
		SecondEventTestPage page = new SecondEventTestPage(counter);
		tester.startPage(page);

		assertEquals(0, counter.get());

		// execute the first event
		tester.executeAjaxEvent("comp", "eventOne");
		assertEquals(1, counter.get());

		// execute the second event
		tester.executeAjaxEvent("comp", "eventTwo");
		assertEquals(2, counter.get());
	}

	@Test
	void nullName()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			new EventNamesBehavior(null);

		});
	}

	@Test
	void emptyName()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			new EventNamesBehavior("");
		});
	}

	@Test
	void spacesName()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			new EventNamesBehavior("  ");
		});
	}

	@Test
	void tabName()
	{
		assertThrows(IllegalArgumentException.class, () -> {
			new EventNamesBehavior("\t");
		});
	}

	/**
	 * Tests execution of the 'load' event
	 * https://issues.apache.org/jira/browse/WICKET-7055
	 */
	@Test
	void executeLoadEvent()
	{
		AtomicInteger counter = new AtomicInteger(0);
		LoadEventTestPage page = new LoadEventTestPage(counter);
		tester.startPage(page);

		assertEquals(0, counter.get());

		// execute the first event
		tester.executeAjaxEvent("comp", "load");
		assertEquals(1, counter.get());
		String responseAsString = tester.getLastResponseAsString();
		System.err.println(responseAsString);
		assertTrue(responseAsString.contains("function(){Wicket.Ajax.ajax({\"u\":\"./page?0-1.0-comp\",\"c\":\"comp1\",\"e\":\"load\"});"));
	}

	private static class EventNamesBehavior extends AjaxEventBehavior
	{
		/**
		 * Construct.
		 *
		 * @param event
		 *            the event this behavior will be attached to
		 */
		EventNamesBehavior(String event)
		{
			super(event);
		}

		@Override
		protected void onEvent(AjaxRequestTarget target)
		{
		}
	}

	/**
	 * Test page for #executeSecondEvent()
	 */
	private static class SecondEventTestPage extends TestPage
	{
		private SecondEventTestPage(final AtomicInteger counter)
		{
			// register a behavior that listens on two events
			comp.add(new AjaxEventBehavior("eventOne eventTwo")
			{
				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
					counter.incrementAndGet();
				}
			});
		}
	}

	/**
	 * Test page for #executeLoadEvent()
	 */
	private static class LoadEventTestPage extends TestPage
	{
		private LoadEventTestPage(final AtomicInteger counter)
		{
			super();

			// register a behavior that listens on two events
			comp.add(new AjaxEventBehavior("load")
			{
				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
					counter.incrementAndGet();
					target.add(getComponent());
				}
			});
		}
	}

	private static class TestPage extends WebPage
			implements
			IMarkupResourceStreamProvider
	{
		protected final WebComponent comp;

		private TestPage()
		{
			comp = new WebComponent("comp");
			comp.setOutputMarkupId(true);
			add(comp);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
													   Class<?> containerClass)
		{
			return new StringResourceStream(
					"<html><body><span wicket:id='comp'></span></body></html>");
		}
	}
}
