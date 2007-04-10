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
package org.apache.wicket.protocol.http;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.ajax.AjaxEventBehavior;
import org.apache.wicket.ajax.markup.html.AjaxLink;
import org.apache.wicket.protocol.http.MockHttpServletRequest;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.settings.IExceptionSettings;
import org.apache.wicket.settings.IRequestCycleSettings;


/**
 * Test exceptions thrown during request
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class WebResponseExceptionsTest extends WicketTestCase
{
	/**
	 * Tests buffered exception error page.
	 */
	public void testBufferedExceptionErrorPage()
	{
		tester.getApplication().getRequestCycleSettings().setRenderStrategy(
				IRequestCycleSettings.REDIRECT_TO_BUFFER);
		tester.getApplication().getExceptionSettings().setUnexpectedExceptionDisplay(
				IExceptionSettings.SHOW_EXCEPTION_PAGE);
		testInternalErrorPage();
	}

	/**
	 * Tests exception error page.
	 */
	public void testExceptionErrorPage()
	{
		tester.getApplication().getExceptionSettings().setUnexpectedExceptionDisplay(
				IExceptionSettings.SHOW_EXCEPTION_PAGE);
		testInternalErrorPage();
	}

	/**
	 * Tests page expired.
	 */
	public void testExpirePage()
	{
		tester.startPage(TestExpirePage.class);
		AjaxLink link = (AjaxLink)tester.getComponentFromLastRenderedPage("link");

		// Cannot use executeAjaxEvent or onClick because WicketTester creates
		// an AjaxRequestTarget from scratch
		// tester.executeAjaxEvent(link, "onclick");
		// tester.clickLink("link");

		// FIXME should not be needed
		tester.createRequestCycle();

		// Clear the session to remove the pages
		tester.getWicketSession().invalidateNow();

		// Invoke the call back URL of the ajax event behavior
		String callbackUrl = ((AjaxEventBehavior)link.getBehaviors().get(0)).getCallbackUrl()
				.toString();
		tester.setupRequestAndResponse();

		// Fake an Ajax request
		((MockHttpServletRequest)tester.getServletRequest()).addHeader("Wicket-Ajax", "Yes");
		// Set ajax mode again, as it is done in setupRequestAndResponse() only
		tester.getWicketResponse().setAjax(tester.getWicketRequest().isAjax());

		tester.getServletRequest().setURL(callbackUrl);

		// Do not call tester.processRequestCycle() because it throws an
		// exception when getting an error page
		WebRequestCycle cycle = tester.createRequestCycle();
		cycle.request();

		assertAjaxLocation();
	}

	/**
	 * Tests internal error page.
	 */
	public void testInternalErrorPage()
	{
		tester.startPage(TestErrorPage.class);
		AjaxLink link = (AjaxLink)tester.getComponentFromLastRenderedPage("link");

		// Cannot use executeAjaxEvent or onClick because WicketTester creates
		// an AjaxRequestTarget from scratch
		// tester.executeAjaxEvent(link, "onclick");
		// tester.clickLink("link");

		// FIXME should not be needed
		tester.createRequestCycle();

		// Invoke the call back URL of the ajax event behavior
		String callbackUrl = ((AjaxEventBehavior)link.getBehaviors().get(0)).getCallbackUrl()
				.toString();
		tester.setupRequestAndResponse();
		// Fake an Ajax request
		((MockHttpServletRequest)tester.getServletRequest()).addHeader("Wicket-Ajax", "Yes");
		tester.getServletRequest().setURL(callbackUrl);

		// Do not call tester.processRequestCycle() because it throws an
		// exception when getting an error page
		WebRequestCycle cycle = tester.createRequestCycle();
		cycle.request();

		assertAjaxLocation();
	}
}
