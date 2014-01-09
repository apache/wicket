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
package org.apache.wicket.response.filter;

import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.apache.wicket.MockPageWithLink;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.resource.DummyApplication;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.tester.DummyHomePage;
import org.junit.After;
import org.junit.Test;

/**
 * Test case for <a href="https://issues.apache.org/jira/browse/WICKET-3280">WICKET-3280</a>
 * 
 * {@link IResponseFilter}s must be called for both Ajax and non-Ajax responses
 */
@SuppressWarnings("serial")
public class ResponseFilterTest extends WicketTestCase
{
	private final AtomicInteger counter = new AtomicInteger(0);

	@Override
	protected WebApplication newApplication()
	{
		final IResponseFilter responseFilter = new IResponseFilter()
		{
			@Override
			public AppendingStringBuffer filter(AppendingStringBuffer responseBuffer)
			{
				counter.getAndIncrement();
				return responseBuffer;
			}
		};

		final WebApplication application = new DummyApplication()
		{
			@Override
			protected void init()
			{
				super.init();
				getRequestCycleSettings().addResponseFilter(responseFilter);
				getRequestCycleSettings().addResponseFilter(AppendCommentFilter.INSTANCE);
			}
		};

		return application;
	}

	/**
	 * after()
	 */
	@After
	public void after()
	{
		counter.set(0);
	}

	/**
	 * WICKET-3620
	 */
	@Test
	public void filterAddCommentFilter()
	{
		tester.startPage(DummyHomePage.class);
		Assert.assertTrue(tester.getLastResponseAsString().contains(AppendCommentFilter.COMMENT));
	}

	/**
	 * WICKET-3620
	 */
	@Test
	public void addCommentFilterInAjaxResponse()
	{
		DummyHomePage testPage = new DummyHomePage();
		testPage.getTestPageLink().add(new AjaxEventBehavior("event")
		{
			@Override
			protected void onEvent(AjaxRequestTarget target)
			{
			}
		});
		tester.startPage(testPage);
		tester.executeAjaxEvent(testPage.getTestPageLink(), "event");
		Assert.assertTrue(tester.getLastResponseAsString().contains(AppendCommentFilter.COMMENT));
	}

	private static class AppendCommentFilter implements IResponseFilter
	{
		static final AppendCommentFilter INSTANCE = new AppendCommentFilter();
		static final String COMMENT = "<!-- comment -->";

		@Override
		public AppendingStringBuffer filter(AppendingStringBuffer responseBuffer)
		{
			return new AppendingStringBuffer(responseBuffer).append(COMMENT);
		}
	}

	/**
	 * normalRequest()
	 */
	@Test
	public void normalRequest()
	{
		tester.startPage(DummyHomePage.class);

		assertEquals(1, counter.get());
	}

	/**
	 * ajaxRequest()
	 */
	@Test
	public void ajaxRequest()
	{
		AjaxPage page = new AjaxPage(counter);
		tester.startPage(page);
		// normal page response
		assertEquals(1, counter.get());

		tester.clickLink("link", true);
		assertEquals(2, counter.get());
		assertTrue(page.ajaxCalled);

	}

	/**
	 * Test page for ajax request
	 */
	public static class AjaxPage extends MockPageWithLink
	{
		boolean ajaxCalled = false;

		/**
		 * Construct.
		 * 
		 * @param counter
		 */
		public AjaxPage(final AtomicInteger counter)
		{
			add(new AjaxLink<Void>(MockPageWithLink.LINK_ID)
			{
				@Override
				public void onClick(AjaxRequestTarget target)
				{
					ajaxCalled = true;
				}
			});
		}
	}
}
