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
		String document = tester.getServletResponse().getDocument();
		assertTrue(document.contains("Click me to get an error"));

		AjaxLink link = (AjaxLink)tester.getComponentFromLastRenderedPage("link");

		// Clear the session to remove the pages
		// tester.getWicketSession().invalidateNow();
		//
		// tester.setCreateAjaxRequest(true);
		// tester.executeAjaxEvent(link, "onclick");
		// tester.clickLink("link");
		//
		// document = tester.getServletResponse().getDocument();
		// assertTrue(document.contains("-"));
		// tester.assertAjaxLocation();

		WebRequestCycle cycle = tester.setupRequestAndResponse(true);
		tester.getWicketSession().invalidateNow();

		// Clear the session to remove the pages
		tester.getWicketSession().invalidateNow();

		// Invoke the call back URL of the ajax event behavior
		String callbackUrl = ((AjaxEventBehavior)link.getBehaviors().get(0)).getCallbackUrl()
			.toString();
		tester.getServletRequest().setURL(callbackUrl);

		// Do not call processRequestCycle() because it throws an
		// exception when getting an error page
		cycle.request();

		document = tester.getServletResponse().getDocument();
		assertTrue(document.equals("-"));
		tester.assertAjaxLocation();
	}

	/**
	 * Tests internal error page.
	 */
	public void testInternalErrorPage()
	{
		tester.startPage(TestErrorPage.class);
		AjaxLink link = (AjaxLink)tester.getComponentFromLastRenderedPage("link");

		try
		{
			tester.executeAjaxEvent(link, "onclick");
			assertTrue("Excepted an error message to be thrown", false);
		}
		catch (IllegalStateException ex)
		{
			// expected exception
			tester.assertAjaxLocation();
		}

		tester.startPage(TestErrorPage.class);
		link = (AjaxLink)tester.getComponentFromLastRenderedPage("link");

		try
		{
			tester.clickLink("link");
			assertTrue("Excepted an error message to be thrown", false);
		}
		catch (IllegalStateException ex)
		{
			// expected exception
			tester.assertAjaxLocation();
		}
	}
}
