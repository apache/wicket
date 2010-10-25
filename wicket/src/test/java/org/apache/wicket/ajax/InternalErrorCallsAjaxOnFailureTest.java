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

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.util.tester.BaseWicketTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

/**
 * Tests that for internal errors in Ajax requests Wicket will send the error response immediately
 * (RedirectPolicy.NEVER_REDIRECT). Since WicketTester initializes new MockHttpServletResponse after
 * a request the response with the error is the last one in
 * {@link BaseWicketTester#getPreviousResponses()}
 */
public class InternalErrorCallsAjaxOnFailureTest
{

	@Test
	public void callsOnFailure()
	{

		WicketTester tester = new WicketTester();
		tester.setExposeExceptions(false);
		tester.startPage(InternalErrorCallsAjaxOnFailurePage.class);

		tester.clickLink("failure-link", true);

		// the response before current should holds the error page markup
		List<MockHttpServletResponse> previousResponses = tester.getPreviousResponses();
		MockHttpServletResponse errorPageResponse = previousResponses.get(previousResponses.size() - 1);
		assertTrue(errorPageResponse.getDocument().contains(
			InternalErrorCallsAjaxOnFailurePage.ERROR_MESSAGE));
	}
}
