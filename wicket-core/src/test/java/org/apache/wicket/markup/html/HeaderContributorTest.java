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

import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.attributes.AjaxCallListener;
import org.apache.wicket.ajax.attributes.AjaxRequestAttributes;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.head.IHeaderResponse;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests that {@link Component}, {@link Behavior} and {@link org.apache.wicket.ajax.attributes.IAjaxCallListener} that implements
 * {@link IHeaderContributor} actually contributes to the header
 */
public class HeaderContributorTest extends WicketTestCase
{

	/**
	 * @see <a href="https://issues.apache.org/jira/browse/WICKET-3059">WICKET-3059</a>
	 */
	@Test
	public void testHeaderContribution()
	{
		HeaderContributorTestPage page = new HeaderContributorTestPage();
		tester.startPage(page);

		Assert.assertTrue("component", page.component.get());
		Assert.assertTrue("behavior", page.behavior.get());
		Assert.assertTrue("callDecorator", page.callDecorator.get());
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
		public HeaderContributorTestPage()
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
		public HeaderContributingCallDecorator(AtomicBoolean callDecorator)
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
