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
package org.apache.wicket.markup.html;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.markup.head.internal.HeaderResponse;
import org.apache.wicket.request.Response;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.jupiter.api.Test;

/**
 * Tests that {@link Component}, {@link Behavior} and {@link org.apache.wicket.ajax.attributes.IAjaxCallListener} that implements
 * {@link IHeaderContributor} actually contributes to the header
 */
class HeaderContributorTest extends WicketTestCase
{

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3059">WICKET-3059</a>
	 */
	@Test
	void testHeaderContribution()
	{
		HeaderContributorTestPage page = new HeaderContributorTestPage();
		tester.startPage(page);

		assertTrue(page.component.get(), "component");
		assertTrue(page.behavior.get(), "behavior");
		assertTrue(page.callDecorator.get(), "callDecorator");
	}

	/**
	 * WICKET-6821 ensure correct ordering of header decorators
	 */
	@Test
	void testHeaderContributorOrder()
	{
		final AtomicInteger counter = new AtomicInteger();
		
		class AssertOrder implements IHeaderResponseDecorator {
			
			private int order;
			
			AssertOrder(int order)
			{
				this.order = order;
			}
			
			@Override
			public IHeaderResponse decorate(IHeaderResponse response)
			{
				assertEquals(order, counter.getAndIncrement());
				
				return response;
			}
		}
		
		HeaderResponseDecoratorCollection decorators = tester.getApplication().getHeaderResponseDecorators();
		decorators.add(new AssertOrder(2));
		decorators.add(new AssertOrder(1));
		decorators.addPreResourceAggregationDecorator(new AssertOrder(3));
		decorators.add(new AssertOrder(0));
		decorators.addPostProcessingDecorator(new AssertOrder(4));
		
		tester.getApplication().decorateHeaderResponse(new HeaderResponse()
		{
			
			@Override
			protected Response getRealResponse()
			{
				return null;
			}
		});
	}

	/**
	 * A page for the test
	 */
	public static class HeaderContributorTestPage extends WebPage
		implements
			IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		AtomicBoolean component = new AtomicBoolean(false);
		AtomicBoolean behavior = new AtomicBoolean(false);
		AtomicBoolean callDecorator = new AtomicBoolean(false);

		/**
		 * Construct.
		 */
		HeaderContributorTestPage()
		{
			add(new AjaxEventBehavior("dummy")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onEvent(AjaxRequestTarget target)
				{
				}

				@Override
				public void renderHead(Component component, IHeaderResponse response)
				{
					super.renderHead(component, response);
					behavior.set(true);
				}

				@Override
				protected void updateAjaxAttributes(AjaxRequestAttributes attributes)
				{
					super.updateAjaxAttributes(attributes);
					attributes.getAjaxCallListeners().add(new HeaderContributingCallDecorator(callDecorator));
				}
			});
		}

		@Override
		public void renderHead(IHeaderResponse response)
		{
			super.renderHead(response);
			component.set(true);
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body></body></html>");
		}
	}

	/**
	 * 
	 */
	public static class HeaderContributingCallDecorator
		extends
			AjaxCallListener
		implements
			IComponentAwareHeaderContributor
	{
		private static final long serialVersionUID = 1L;

		private final AtomicBoolean callDecorator;

		/**
		 * Construct.
		 * 
		 * @param callDecorator
		 */
		HeaderContributingCallDecorator(AtomicBoolean callDecorator)
		{
			this.callDecorator = callDecorator;
		}

		@Override
		public void renderHead(Component component, IHeaderResponse response)
		{
			callDecorator.set(true);
		}
	}
}
