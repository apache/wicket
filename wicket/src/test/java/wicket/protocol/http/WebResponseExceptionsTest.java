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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WicketTestCase;
import wicket.ajax.AjaxEventBehavior;
import wicket.ajax.markup.html.AjaxLink;
import wicket.util.tester.WicketTester;

/**
 * Test exceptions thrown during request
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class WebResponseExceptionsTest extends WicketTestCase
{
	private static final Log log = LogFactory.getLog(WebResponseExceptionsTest.class);

	public void testErrorPage()
	{
		tester.startPage(TestErrorPage.class);
		AjaxLink link = (AjaxLink)tester.getComponentFromLastRenderedPage("link");
		
		// Cannot use executeAjaxEvent or onClick because WicketTester creates an AjaxRequestTarget from scratch
		//tester.executeAjaxEvent(link, "onclick");
		//tester.clickLink("link");

		// FIXME should not be needed
		tester.createRequestCycle();

		// Invoke the call back URL of the ajax event behavior
		String callbackUrl = ((AjaxEventBehavior)link.getBehaviors().get(0)).getCallbackUrl().toString();
		tester.setupRequestAndResponse();
		// Fake an Ajax request
		((MockHttpServletRequest)tester.getServletRequest()).addHeader("Wicket-Ajax", "Yes");
		tester.getServletRequest().setURL(callbackUrl);

		// Do not call tester.processRequestCycle() because it throws an exception when getting an error page
		WebRequestCycle cycle = tester.createRequestCycle();
		cycle.request();

		assertAjaxLocation(tester);
	}

	public void testExpirePage()
	{
		tester.startPage(TestExpirePage.class);
		AjaxLink link = (AjaxLink)tester.getComponentFromLastRenderedPage("link");
		
		// Cannot use executeAjaxEvent or onClick because WicketTester creates an AjaxRequestTarget from scratch
		//tester.executeAjaxEvent(link, "onclick");
		//tester.clickLink("link");

		// FIXME should not be needed
		tester.createRequestCycle();
		
		// Clear the session to remove the pages
		tester.getWicketSession().invalidateNow();

		// Invoke the call back URL of the ajax event behavior
		String callbackUrl = ((AjaxEventBehavior)link.getBehaviors().get(0)).getCallbackUrl().toString();
		tester.setupRequestAndResponse();

		// Fake an Ajax request
		((MockHttpServletRequest)tester.getServletRequest()).addHeader("Wicket-Ajax", "Yes");
		// Set ajax mode again, as it is done in setupRequestAndResponse() only
		tester.getWicketResponse().setAjax(tester.getWicketRequest().isAjax());

		tester.getServletRequest().setURL(callbackUrl);

		// Do not call tester.processRequestCycle() because it throws an exception when getting an error page
		WebRequestCycle cycle = tester.createRequestCycle();
		cycle.request();

		assertAjaxLocation(tester);
	}

	private void assertAjaxLocation(WicketTester tester) {
		assertNull("Location header should *not* be present when using Ajax", ((MockHttpServletResponse)tester.getWicketResponse().getHttpServletResponse()).getRedirectLocation());
		String ajaxLocation = ((MockHttpServletResponse)tester.getWicketResponse().getHttpServletResponse()).getHeader("Ajax-Location");
		log.debug(ajaxLocation);
		assertNotNull("Ajax-Location header should be present when using Ajax", ajaxLocation);
	}
}
