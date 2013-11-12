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

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.pages.ExceptionErrorPage;
import org.apache.wicket.markup.html.pages.InternalErrorPage;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.resource.DummyApplication;
import org.apache.wicket.settings.def.ExceptionSettings;
import org.apache.wicket.util.tester.BaseWicketTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Test;

/**
 * Tests that for internal errors in Ajax requests Wicket will send the error response immediately
 * (RedirectPolicy.NEVER_REDIRECT). Since WicketTester initializes new MockHttpServletResponse after
 * a request the response with the error is the last one in
 * {@link BaseWicketTester#getLastResponse()}
 * 
 * See WICKET-3143 No Exception page are rendered when using ajax
 * 
 */
public class InternalErrorCallsAjaxOnFailureTest extends WicketTestCase
{

	/**
	 * The default {@link org.apache.wicket.settings.def.ExceptionSettings#getAjaxErrorHandlingStrategy()} is
	 * {@link org.apache.wicket.settings.def.ExceptionSettings.AjaxErrorStrategy#REDIRECT_TO_ERROR_PAGE}
	 */
	@Test
	public void showsInternalErrorPage()
	{

		tester.setExposeExceptions(false);
		tester.startPage(InternalErrorCallsAjaxOnFailurePage.class);

		tester.clickLink("failure-link", true);

		// the response before current should holds the error page markup
		MockHttpServletResponse errorPageResponse = tester.getLastResponse();
		assertEquals(500, errorPageResponse.getStatus());
		assertTrue(errorPageResponse.getDocument().contains(
			InternalErrorCallsAjaxOnFailurePage.ERROR_MESSAGE));

		// assert the page with detailed error explanation is rendered
		tester.assertRenderedPage(ExceptionErrorPage.class);
	}


	/**
	 * Setup {@link org.apache.wicket.settings.def.ExceptionSettings.AjaxErrorStrategy#INVOKE_FAILURE_HANDLER}
	 * so Wicket will not redirect to the configured {@link InternalErrorPage}/{@link ExceptionErrorPage}
	 * but will preserve the current page and send http status 500 to wicket-ajax.js
	 */
	@Test
	public void callsOnFailure()
	{

		WicketTester tester = new WicketTester(new DummyApplication()
		{

			/**
			 * @see org.apache.wicket.protocol.http.WebApplication#init()
			 */
			@Override
			protected void init()
			{
				super.init();

				getExceptionSettings().setAjaxErrorHandlingStrategy(
					ExceptionSettings.AjaxErrorStrategy.INVOKE_FAILURE_HANDLER);
			}

		});
		tester.setExposeExceptions(false);
		tester.startPage(InternalErrorCallsAjaxOnFailurePage.class);

		tester.clickLink("failure-link", true);

		MockHttpServletResponse errorPageResponse = tester.getLastResponse();
		assertEquals(500, errorPageResponse.getStatus());

		// assert that the original page is still the last rendered one
		tester.assertRenderedPage(InternalErrorCallsAjaxOnFailurePage.class);
		tester.destroy();
	}
}
