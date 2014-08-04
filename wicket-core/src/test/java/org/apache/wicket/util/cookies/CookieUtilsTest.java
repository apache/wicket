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

import static org.hamcrest.Matchers.arrayContaining;
import static org.hamcrest.Matchers.emptyArray;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.util.cookies.CookieValuePersisterTestPage.TestForm;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Juergen Donnerstag
 */
public class CookieUtilsTest extends WicketTestCase
{
	@Before
	public void before()
	{
		tester.startPage(CookieValuePersisterTestPage.class);
	}

	/**
	 * 
	 * @throws Exception
	 */
	@SuppressWarnings({ "unchecked" })
	@Test
	public void test1() throws Exception
	{
		// How does the test work: Make sure you have a page, form and form component properly set
		// up (getRelativePath() etc.). See #before().
		final Page page = tester.getLastRenderedPage();

		// Get the form and form component created
		final TestForm form = (TestForm)page.get("form");
		final TextField<String> textField = (TextField<String>)form.get("input");

		// Right after init, the requests and responses cookie lists must be empty
		assertEquals(0, getRequestCookies().size());
		assertEquals(0, getResponseCookies().size());

		// Create a persister for the test
		final CookieUtils persister = new CookieUtils();

		// See comment in CookieUtils on how removing a Cookies works. As no cookies in the request,
		// no "delete" cookie will be added to the response.
		persister.remove(textField);
		assertEquals(0, getRequestCookies().size());
		assertEquals(0, getResponseCookies().size());

		// Save the input field's value (add it to the response's cookie list)
		persister.save(textField);
		assertTrue(getRequestCookies().isEmpty());
		assertEquals(1, getResponseCookies().size());
		assertEquals("test", (getResponseCookies().get(0)).getValue());
		assertEquals("form.input", (getResponseCookies().get(0)).getName());
		assertEquals(tester.getRequest().getContextPath() + tester.getRequest().getServletPath(),
			(getResponseCookies().get(0)).getPath());

		// To remove a cookie means to add a cookie with maxAge=0. Provided a cookie with the same
		// name has been provided in the request. Thus, no changes in our test case
		persister.remove(textField);
		assertEquals(0, getRequestCookies().size());
		assertEquals(1, getResponseCookies().size());
		assertEquals("test", (getResponseCookies().get(0)).getValue());
		assertEquals("form.input", (getResponseCookies().get(0)).getName());
		assertEquals(tester.getRequest().getContextPath() + tester.getRequest().getServletPath(),
			(getResponseCookies().get(0)).getPath());

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
		copyCookieFromResponseToRequest();
		assertEquals(1, getRequestCookies().size());
		assertEquals(1, getResponseCookies().size());

		persister.load(textField);
		assertEquals("test", textField.getDefaultModelObjectAsString());
		assertEquals(1, getRequestCookies().size());
		assertEquals(1, getResponseCookies().size());

		// remove all cookies from mock response. Because I'll find the cookie to be removed in the
		// request, the persister will create a "delete" cookie to remove the cookie on the client
		// and add it to the response. The already existing Cookie from the previous test gets
		// removed from response since it is the same.
		persister.remove(textField);
		assertEquals(1, getRequestCookies().size());
		assertEquals(1, getResponseCookies().size());
		assertEquals("form.input", (getResponseCookies().get(0)).getName());
		assertEquals(0, (getResponseCookies().get(0)).getMaxAge());
	}

	@Test
	public void splitValuesNullString()
	{
		CookieUtils utils = new CookieUtils();
		String[] values = utils.splitValue(null);
		assertArrayEquals(new String[0], values);
	}

	@Test
	public void splitValuesEmptyString()
	{
		CookieUtils utils = new CookieUtils();
		String[] values = utils.splitValue("");
		assertThat(values, is(emptyArray()));
	}

	@Test
	public void splitValuesSingleValue()
	{
		CookieUtils utils = new CookieUtils();
		String value1 = "value one";
		String[] values = utils.splitValue(value1);
		assertThat(values, is(arrayContaining(value1)));
	}

	@Test
	public void splitValuesManyValues()
	{
		CookieUtils utils = new CookieUtils();
		String value1 = "value one";
		String value2 = "value two";
		String value = value1 + FormComponent.VALUE_SEPARATOR + value2;
		String[] values = utils.splitValue(value);
		assertThat(values, is(arrayContaining(value1, value2)));
	}

	@Test
	public void joinValues()
	{
		CookieUtils utils = new CookieUtils();
		String value1 = "value one";
		String value2 = "value two";
		String joined = utils.joinValues(value1, value2);
		assertThat(joined, is(equalTo(value1 + FormComponent.VALUE_SEPARATOR + value2)));
	}

	@Test
	public void saveLoadValue()
	{
		CookieUtils utils = new CookieUtils();
		String value1 = "value one";
		String key = "key";
		utils.save(key, value1);
		before(); // execute a request cycle, so the response cookie is send with the next request
		String result = utils.load(key);
		assertThat(result, is(equalTo(value1)));
	}

	@Test
	public void defaults()
	{
		CookieDefaults defaults = new CookieDefaults();
		defaults.setComment("A comment");
		defaults.setDomain("A domain");
		defaults.setMaxAge(123);
		defaults.setSecure(true);
		defaults.setVersion(456);
		CookieUtils utils = new CookieUtils(defaults);
		String value1 = "value one";
		String key = "key";
		utils.save(key, value1);
		before(); // execute a request cycle, so the response cookie is send with the next request
		Cookie result = utils.getCookie(key);
		assertThat(result.getComment(), is(equalTo(defaults.getComment())));
	}

	private void copyCookieFromResponseToRequest()
	{
		tester.getRequest().addCookie(getResponseCookies().iterator().next());
	}

	private Collection<Cookie> getRequestCookies()
	{
		if (tester.getRequest().getCookies() == null)
		{
			return Collections.emptyList();
		}
		else
		{
			return Arrays.asList(tester.getRequest().getCookies());
		}
	}

	private List<Cookie> getResponseCookies()
	{
		return tester.getResponse().getCookies();
	}
}
