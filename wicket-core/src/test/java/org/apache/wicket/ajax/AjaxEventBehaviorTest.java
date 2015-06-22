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

import static org.hamcrest.Matchers.is;

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebComponent;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Test;

/**
 * @since 6.0.1
 */
public class AjaxEventBehaviorTest extends WicketTestCase
{
	/**
	 * Tests execution of the second configured event
	 * https://issues.apache.org/jira/browse/WICKET-4748
	 */
	@Test
	public void executeSecondEvent()
	{
		AtomicInteger counter = new AtomicInteger(0);
		SecondEventTestPage page = new SecondEventTestPage(counter);
		tester.startPage(page);

		assertEquals(0, counter.get());

		// execute the first event (without the leading 'on')
		tester.executeAjaxEvent("comp", "eventOne");
		assertEquals(1, counter.get());

		// execute the first event (with the leading 'on')
		tester.executeAjaxEvent("comp", "oneventOne");
		assertEquals(2, counter.get());

		// execute the second event (without the leading 'on')
		tester.executeAjaxEvent("comp", "eventTwo");
		assertEquals(3, counter.get());

		// execute the second event (with the leading 'on')
		tester.executeAjaxEvent("comp", "oneventTwo");
		assertEquals(4, counter.get());
	}

	@Test(expected = IllegalArgumentException.class)
	public void nullName()
	{
		new EventNamesBehavior(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void emptyName()
	{
		new EventNamesBehavior("");
	}

	@Test(expected = IllegalArgumentException.class)
	public void spacesName()
	{
		new EventNamesBehavior("  ");
	}

	@Test(expected = IllegalArgumentException.class)
	public void tabName()
	{
		new EventNamesBehavior("\t");
	}

	@Test
	public void cutLeadingOnPrefix() {
		AjaxEventBehavior behavior = new EventNamesBehavior("onevent");
		assertThat(behavior.getEvent(), is("event"));

		behavior = new EventNamesBehavior("event");
		assertThat(behavior.getEvent(), is("event"));

		behavior = new EventNamesBehavior("onevent onevent2");
		assertThat(behavior.getEvent(), is("event event2"));

		behavior = new EventNamesBehavior("event onevent2");
		assertThat(behavior.getEvent(), is("event event2"));

		behavior = new EventNamesBehavior("event event2");
		assertThat(behavior.getEvent(), is("event event2"));

		behavior = new EventNamesBehavior("event  onevent2on");
		assertThat(behavior.getEvent(), is("event event2on"));

		behavior = new EventNamesBehavior("  event   onevent2on    \t  onevent3on  onon ");
		assertThat(behavior.getEvent(), is("event event2on event3on on"));
	}

	private static class EventNamesBehavior extends AjaxEventBehavior
	{
		/**
		 * Construct.
		 *
		 * @param event the event this behavior will be attached to
		 */
		public EventNamesBehavior(String event)
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
	private static class SecondEventTestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private SecondEventTestPage(final AtomicInteger counter)
		{
			WebComponent comp = new WebComponent("comp");
			add(comp);

			// register a behavior that listens on two events and the
			// tested one also has a prefix 'on'
			comp.add(new AjaxEventBehavior("eventOne oneventTwo")
			{
				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
					counter.incrementAndGet();
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><span wicket:id='comp'></span></body></html>");
		}
	}
}
