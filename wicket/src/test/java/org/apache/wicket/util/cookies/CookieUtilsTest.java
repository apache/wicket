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
package org.apache.wicket.util.cookies;

import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.cookies.CookieValuePersisterTestPage.TestForm;

/**
 * 
 * @author Juergen Donnerstag
 */
public class CookieUtilsTest extends WicketTestCase
{
	@Override
	protected void setUp() throws Exception
	{
		super.setUp();
		tester.startPage(CookieValuePersisterTestPage.class);
	}


	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings( { "unchecked" })
	public void test1() throws Exception
	{
		// How does the test work: Make sure you have a page, form and form component properly set
		// up (getRelativePath() etc.). See setUp().
		final Page page = tester.getLastRenderedPage();

		// Get the form and form component created
		final TestForm form = (TestForm)page.get("form");
		final TextField<String> textField = (TextField<String>)form.get("input");

		// Make sure a valid cycle is available via RequestCycle.get(). Attached to this cycle must
		// be a valid request and response.
		final WebRequestCycle cycle = tester.createRequestCycle();

		// Right after init, the requests and responses cookie lists must be empty
		assertNull(getRequestCookies(cycle));
		assertEquals(0, getResponseCookies(cycle).size());

		// Create a persister for the test
		final CookieUtils persister = new CookieUtils();

		// See comment in CookieUtils on how removing a Cookies works. As no cookies in the request,
		// no "delete" cookie will be added to the response.
		persister.remove(textField);
		assertNull(getRequestCookies(cycle));
		assertEquals(0, getResponseCookies(cycle).size());

		// Save the input field's value (add it to the response's cookie list)
		persister.save(textField);
		assertNull(getRequestCookies(cycle));
		assertEquals(1, getResponseCookies(cycle).size());
		assertEquals("test", (getResponseCookies(cycle).get(0)).getValue());
		assertEquals("form.input", (getResponseCookies(cycle).get(0)).getName());
		assertEquals("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication",
			(getResponseCookies(cycle).get(0)).getPath());

		// To remove a cookie means to add a cookie with maxAge=0. Provided a cookie with the same
		// name has been provided in the request. Thus, no changes in our test case
		persister.remove(textField);
		assertNull(getRequestCookies(cycle));
		assertEquals(1, getResponseCookies(cycle).size());
		assertEquals("test", (getResponseCookies(cycle).get(0)).getValue());
		assertEquals("form.input", (getResponseCookies(cycle).get(0)).getName());
		assertEquals("/WicketTester$DummyWebApplication/WicketTester$DummyWebApplication",
			(getResponseCookies(cycle).get(0)).getPath());

		// Try to load it. Because there is no Cookie matching the textfield's name the model's
		// value remains unchanged
		persister.load(textField);
		assertEquals("test", textField.getDefaultModelObjectAsString());

		// Simulate loading a textfield. Initialize textfield with a new (default) value, copy the
		// cookie from response to request (simulating a browser), than load the textfield from
		// cookie and voala the textfield's value should change.
		// save means: add it to the response
		// load means: take it from request
		assertEquals("test", textField.getDefaultModelObjectAsString());
		textField.setDefaultModelObject("new text");
		assertEquals("new text", textField.getDefaultModelObjectAsString());
		copyCookieFromResponseToRequest(cycle);
		assertEquals(1, getRequestCookies(cycle).length);
		assertEquals(1, getResponseCookies(cycle).size());

		persister.load(textField);
		assertEquals("test", textField.getDefaultModelObjectAsString());
		assertEquals(1, getRequestCookies(cycle).length);
		assertEquals(1, getResponseCookies(cycle).size());

		// remove all cookies from mock response. Because I'll find the cookie to be removed in the
		// request, the persister will create a "delete" cookie to remove the cookie on the client
		// and add it to the response. The already existing Cookie from the previous test gets
		// removed from response since it is the same.
		persister.remove(textField);
		assertEquals(1, getRequestCookies(cycle).length);
		assertEquals(1, getResponseCookies(cycle).size());
		assertEquals("form.input", (getResponseCookies(cycle).get(0)).getName());
		assertEquals(0, (getResponseCookies(cycle).get(0)).getMaxAge());
	}

	private void copyCookieFromResponseToRequest(final RequestCycle cycle)
	{
		((MockHttpServletRequest)((WebRequest)cycle.getRequest()).getHttpServletRequest()).addCookie(getResponseCookies(
			cycle).get(0));
	}

	private Cookie[] getRequestCookies(final RequestCycle cycle)
	{
		return ((WebRequest)cycle.getRequest()).getHttpServletRequest().getCookies();
	}

	private List<Cookie> getResponseCookies(final RequestCycle cycle)
	{
		MockHttpServletResponse response = (MockHttpServletResponse)((WebResponse)cycle.getResponse()).getHttpServletResponse();
		return (List<Cookie>)response.getCookies();
	}
}
