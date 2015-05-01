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
package org.apache.wicket.queueing.transparentresolvers;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * This page causes a {@code StackOverflowError} when trying to update the component {@code label}
 * from an Ajax request. The page renders normally in normal page requests or fallback requests.
 *
 * Things of note: the test passes when you add the {@code TransparentWebMarkupContainer} as the
 * first component to the page, instead of it being the last component to be added.
 *
 * It appears that the {@code src} attribute of the {@code <img>} tag inside the {@code group}
 * {@code WebMarkupContainer} is significant in triggering this bug. Removing the {@code group} or
 * the {@code src} attribute lets the test pass.
 */
public class TestWicket5898
{
	private WicketTester tester;

	/**
	 * Sets up the tester.
	 */
	@Before
	public void setUp()
	{
		tester = new WicketTester();
	}

	/**
	 * This test should pass, it is just here to validate that the page renders initially, and using
	 * a normal, non-AJAX request cycle.
	 */
	@Test
	public void normalRequestDoesntCauseStackOverflow()
	{
		tester.startPage(Wicket5898Page.class);

		// the page renders normally using normal web requests
		tester.assertRenderedPage(Wicket5898Page.class);

		// the page renders normally when clicking on a link without using AJAX
		tester.clickLink("link", false);
		tester.assertRenderedPage(Wicket5898Page.class);
	}

	/**
	 * Tests the WICKET-5898 issue of triggering a StackOverflowError when a component inside nested
	 * TransparentWebMarkupContainers is updated. This particular test case is caused by Wicket's
	 * insertion of a TransparentWebMarkupContainer automatically due to a {@code src} attribute
	 * that might need rewriting.
	 */
	@Test
	@Ignore("This test fails, should be enabled to trigger WICKET-5898")
	public void ajaxRequestForComponentInTransparentWebMarkupContainerShouldntCauseStackOverflow()
	{
		tester.startPage(Wicket5898Page.class);

		// the page renders normally using normal web requests
		tester.assertRenderedPage(Wicket5898Page.class);

		// without WICKET-5898 fixed the statement below causes a StackOverflowError
		tester.clickLink("link", true);
		tester.assertComponentOnAjaxResponse("label");
	}

	/**
	 * Tests the WICKET-5898 issue of triggering a StackOverflowError when a component inside nested
	 * TransparentWebMarkupContainers is updated. This particular test case is caused by having two
	 * TransparentWebMarkupContainers nested and trying to update a label that was added to the
	 * outer TWMC.
	 */
	@Test
	@Ignore("This test fails, should be enabled to trigger WICKET-5898")
	public void ajaxRequestForComponentInTransparentWebMarkupContainerShouldntCauseStackOverflow2()
	{
		tester.startPage(Wicket5898Page2.class);

		// the page renders normally using normal web requests
		tester.assertRenderedPage(Wicket5898Page2.class);

		// without WICKET-5898 fixed the statement below causes a StackOverflowError
		tester.clickLink("link", true);
		tester.assertComponentOnAjaxResponse("label");
	}
}
