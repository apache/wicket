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
package org.apache.wicket.request.cycle;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.util.tester.WicketTestCase;
import org.junit.Before;
import org.junit.Test;

/**
 * Checks whether multiple registered requestcycle listeners are called in the right order:
 * similarly to servlet filters.
 */
public class MultiRequestCycleListenerCallOrderTest extends WicketTestCase
{
	private MultiRequestCycleListenerCallOrderApplication application;

	/**
	 * 
	 */
	@Before
	public void before()
	{
		application = (MultiRequestCycleListenerCallOrderApplication)tester.getApplication();
		application.callSequence.clear();
	}

	@Override
	protected WebApplication newApplication()
	{
		return new MultiRequestCycleListenerCallOrderApplication();
	}

	/**
	 */
	@Test
	public void bookmarkableCallSequenceIsFirstInLastOut()
	{
		// start and render the test page
		tester.startPage(MultiRequestCycleListenerCallOrderPage.class);

		// assert rendered page class
		tester.assertRenderedPage(MultiRequestCycleListenerCallOrderPage.class);

		List<String> primaryRequest = asList("first.onBeginRequest", "second.onBeginRequest",
			"first.onRequestHandlerResolved", "second.onRequestHandlerResolved",
			"first.onRequestHandlerExecuted", "second.onRequestHandlerExecuted",
			"second.onEndRequest", "first.onEndRequest", "second.onDetach", "first.onDetach");
		List<String> redirectRequest = asList("first.onBeginRequest", "second.onBeginRequest",
			"first.onRequestHandlerResolved", "second.onRequestHandlerResolved",
			"first.onRequestHandlerExecuted", "second.onRequestHandlerExecuted",
			"second.onEndRequest", "first.onEndRequest", "second.onDetach", "first.onDetach");

		List<String> expected = new ArrayList<String>();
		expected.addAll(primaryRequest);
		expected.addAll(redirectRequest);
		assertEquals(expected.toString(), application.callSequence.toString());
	}

	/**
	 */
	@Test
	public void sessionRelativePageRequestCallSequenceIsFirstInLastOut()
	{
		// start and render the test page
		tester.startPage(new MultiRequestCycleListenerCallOrderPage());
		// assert rendered page class
		tester.assertRenderedPage(MultiRequestCycleListenerCallOrderPage.class);

		List<String> primaryRequest = asList("first.onBeginRequest", "second.onBeginRequest",
			"first.onRequestHandlerResolved", "second.onRequestHandlerResolved",
			"first.onRequestHandlerExecuted", "second.onRequestHandlerExecuted",
			"second.onEndRequest", "first.onEndRequest", "second.onDetach", "first.onDetach");
		List<String> redirectRequest = asList("first.onBeginRequest", "second.onBeginRequest",
			"first.onRequestHandlerResolved", "second.onRequestHandlerResolved",
			"first.onRequestHandlerExecuted", "second.onRequestHandlerExecuted",
			"second.onEndRequest", "first.onEndRequest", "second.onDetach", "first.onDetach");

		List<String> expected = new ArrayList<String>();
		expected.addAll(primaryRequest);
		expected.addAll(redirectRequest);
		assertEquals(expected.toString(), application.callSequence.toString());
	}

	/**
	 */
	@Test
	public void linkListenerCallSequenceIsFirstInLastOut()
	{
		// start and render the test page
		tester.startPage(new MultiRequestCycleListenerCallOrderPage());
		// assert rendered page class
		tester.assertRenderedPage(MultiRequestCycleListenerCallOrderPage.class);

		application.callSequence.clear();

		tester.clickLink("link");

		List<String> primaryRequest = asList("first.onBeginRequest", "second.onBeginRequest",
			"first.onRequestHandlerResolved", "second.onRequestHandlerResolved",
			"first.onRequestHandlerScheduled", "second.onRequestHandlerScheduled",
			"first.onRequestHandlerExecuted", "second.onRequestHandlerExecuted",
			"second.onEndRequest", "first.onEndRequest", "second.onDetach", "first.onDetach");
		List<String> redirectRequest = asList("first.onBeginRequest", "second.onBeginRequest",
			"first.onRequestHandlerResolved", "second.onRequestHandlerResolved",
			"first.onRequestHandlerExecuted", "second.onRequestHandlerExecuted",
			"second.onEndRequest", "first.onEndRequest", "second.onDetach", "first.onDetach");

		List<String> expected = new ArrayList<String>();
		expected.addAll(primaryRequest);
		expected.addAll(redirectRequest);

		assertEquals(expected.toString(), application.callSequence.toString());
	}

	/**
	 */
	@Test
	public void ajaxlinkListenerCallSequenceIsFirstInLastOut()
	{
		// start and render the test page
		tester.startPage(new MultiRequestCycleListenerCallOrderPage());
		// assert rendered page class
		tester.assertRenderedPage(MultiRequestCycleListenerCallOrderPage.class);

		application.callSequence.clear();

		tester.clickLink("ajax", true);

		List<String> primaryRequest = asList("first.onBeginRequest", "second.onBeginRequest",
			"first.onRequestHandlerResolved", "second.onRequestHandlerResolved",
			"first.onRequestHandlerScheduled", "second.onRequestHandlerScheduled",
			"first.onRequestHandlerExecuted", "second.onRequestHandlerExecuted",
			"second.onEndRequest", "first.onEndRequest", "second.onDetach", "first.onDetach");

		// with ajax requests we don't expect a redirect
		List<String> expected = new ArrayList<String>();
		expected.addAll(primaryRequest);

		assertEquals(expected.toString(), application.callSequence.toString());
	}
}
