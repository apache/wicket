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

import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebMarkupContainer;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.junit.Test;

/**
 * Verifies that each Component's onMarkupAttached() is called exactly once
 * for its lifecycle.
 *
 * https://issues.apache.org/jira/browse/WICKET-4361
 *
 * @since 1.5.5
 */
public class OnMarkupAttachedTest extends WicketTestCase
{
	@Test
	public void onMarkupAttached()
	{
		AtomicInteger counter = new AtomicInteger(0);
		OnMarkupAttachedPage page = new OnMarkupAttachedPage(counter);
		tester.startPage(page);

		assertEquals(3, counter.get());
	}
	
	private static class OnMarkupAttachedPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private final AtomicInteger counter;

		private OnMarkupAttachedPage(AtomicInteger counter)
		{
			this.counter = counter;

			WebMarkupContainer comp1 = new WebMarkupContainer("one")
			{
				@Override
				protected void onMarkupAttached()
				{
					super.onMarkupAttached();
					assertEquals(1, getCounter().getAndIncrement());
				}
			};
			
			WebMarkupContainer comp2 = new WebMarkupContainer("two")
			{
				@Override
				protected void onMarkupAttached()
				{
					super.onMarkupAttached();
					assertEquals(2, getCounter().getAndIncrement());
				}
			};
			comp1.add(comp2);
			add(comp1);
		}

		@Override
		protected void onMarkupAttached()
		{
			super.onMarkupAttached();
			assertEquals(0, counter.getAndIncrement());
		}

		private AtomicInteger getCounter()
		{
			return counter;
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container, Class<?> containerClass)
		{
			return new StringResourceStream("<html><body><div wicket:id='one'><div wicket:id='two'></div></div></body></html>");
		}
	}
}
