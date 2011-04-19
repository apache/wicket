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
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

import org.apache.wicket.util.tester.WicketTester;
import org.junit.Before;
import org.junit.Test;

/**
 * Checks whether multiple registered requestcycle listeners are called in the right order:
 * similarly to servlet filters.
 */
@SuppressWarnings("javadoc")
public class MultiRequestCycleListenerCallOrderTest
{
	private WicketTester tester;
	private MultiRequestCycleListenerCallOrderApplication application;

	@Before
	public void setUp()
	{
		application = new MultiRequestCycleListenerCallOrderApplication();
		tester = new WicketTester(application);
	}

	@Test
	public void callSequenceIsFirstInLastOut()
	{
		// start and render the test page
		tester.startPage(new MultiRequestCycleListenerCallOrderPage());
		// assert rendered page class
		tester.assertRenderedPage(MultiRequestCycleListenerCallOrderPage.class);

		assertThat(
			application.callSequence,
			is(equalTo(asList("first.onBeginRequest", "second.onBeginRequest",
				"first.onRequestHandlerResolved", "second.onRequestHandlerResolved",
				"second.onEndRequest", "first.onEndRequest", "second.onDetach", "first.onDetach"))));
	}
}
