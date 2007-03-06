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
package wicket.protocol.http;

import junit.framework.TestCase;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.markup.html.AjaxLink;
import wicket.util.tester.WicketTester;

/**
 * 
 * 
 * @author Frank Bille (billen)
 */
public class WebResponseTest extends TestCase
{
	private static final Logger log = LoggerFactory.getLogger(WebResponseTest.class);

	/**
	 * Test that redirect works correctly when not using ajax
	 */
	public void testRedirect_normal()
	{
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();

		WebResponse webResponse = new WebResponse(mockResponse);

		webResponse.redirect("/?wicket:interface=:4::");

		assertEquals("/?wicket:interface=:4::", mockResponse.getRedirectLocation());
		assertFalse(mockResponse.containsHeader("Ajax-Location"));
	}

	/**
	 * Test that redirect works correctly when using ajax
	 */
	public void testRedirect_ajax()
	{
		MockHttpServletResponse mockResponse = new MockHttpServletResponse();

		WebResponse webResponse = new WebResponse(mockResponse);
		webResponse.setAjax(true);

		webResponse.redirect("/?wicket:interface=:4::");

		assertNull(mockResponse.getRedirectLocation());
		assertTrue(mockResponse.containsHeader("Ajax-Location"));
	}

	public void testErrorPage()
	{
		WicketTester tester = new WicketTester();
		tester.startPage(TestPage.class);
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

		assertNull(((MockHttpServletResponse)tester.getWicketResponse().getHttpServletResponse())
				.getRedirectLocation());
		String ajaxLocation = ((MockHttpServletResponse)tester.getWicketResponse()
				.getHttpServletResponse()).getHeader("Ajax-Location");
		log.debug(ajaxLocation);
		assertNotNull(ajaxLocation);

		tester.destroy();
	}
}
