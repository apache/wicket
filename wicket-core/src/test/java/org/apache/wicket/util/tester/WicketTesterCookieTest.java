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
package org.apache.wicket.util.tester;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.Cookie;

import org.apache.wicket.protocol.http.mock.Cookies;
import org.apache.wicket.util.tester.apps_1.CreateBook;
import org.apache.wicket.util.tester.cookies.CollectAllRequestCookiesPage;
import org.apache.wicket.util.tester.cookies.EndPage;
import org.apache.wicket.util.tester.cookies.SetCookiePage;
import org.junit.Assert;
import org.junit.Test;

/**
 * test code for wicket tester cookie handling
 * 
 * @author mosmann
 */
public class WicketTesterCookieTest extends WicketTestCase
{
	/**
	 *
	 */
	@Test
	public void cookieIsFoundWhenAddedToRequest()
	{
		tester.getRequest().addCookie(new Cookie("name", "value"));
		assertEquals("value", tester.getRequest().getCookie("name").getValue());
	}

	/**
	 *
	 */
	@Test
	public void cookieIsFoundWhenAddedToResponse()
	{
		tester.startPage(CreateBook.class);
		tester.getLastResponse().addCookie(new Cookie("name", "value"));
		Collection<Cookie> cookies = tester.getLastResponse().getCookies();
		assertEquals(cookies.iterator().next().getValue(), "value");
	}
	
	/**
	 * Tests that setting a cookie with age > 0 before creating the page will survive after the
	 * rendering of the page and it will be used for the next request cycle.
	 */
	@Test
	public void transferCookies()
	{
		String cookieName = "wicket4289Name";
		String cookieValue = "wicket4289Value";
		int cookieAge = 1; // age > 0 => the cookie will be preserved for the the next request cycle

		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(cookieAge);
		tester.getRequest().addCookie(cookie);

		CookiePage page = new CookiePage(cookieName, cookieValue);

		tester.startPage(page);

		// assert that the cookie was in the response
		List<Cookie> cookies = tester.getLastResponse().getCookies();
		assertEquals(1, cookies.size());
		Cookie cookie2 = cookies.get(0);
		assertEquals(cookieName, cookie2.getName());
		assertEquals(cookieValue, cookie2.getValue());
		assertEquals(cookieAge, cookie2.getMaxAge());

		// assert that the cookie will be preserved for the next request
		assertEquals(cookieValue, tester.getRequest().getCookie(cookieName).getValue());
	}

	/**
	 * Tests that setting a cookie with age == 0 will not be stored after the request cycle.
	 */
	@Test
	public void dontTransferCookiesWithNegativeAge()
	{
		String cookieName = "wicket4289Name";
		String cookieValue = "wicket4289Value";
		int cookieAge = 0; // age = 0 => do not store it

		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(cookieAge);
		tester.getRequest().addCookie(cookie);

		CookiePage page = new CookiePage(cookieName, cookieValue);

		tester.startPage(page);

		// assert that the cookie is not preserved for the next request cycle
		assertNull(tester.getRequest().getCookies());
	}

	/**
	 * Tests that setting a cookie with age < 0 will not be stored after the request cycle.
	 */
	@Test
	public void dontTransferCookiesWithZeroAge()
	{
		String cookieName = "wicket4289Name";
		String cookieValue = "wicket4289Value";
		int cookieAge = 0; // age == 0 => delete the cookie

		Cookie cookie = new Cookie(cookieName, cookieValue);
		cookie.setMaxAge(cookieAge);
		tester.getRequest().addCookie(cookie);

		CookiePage page = new CookiePage(cookieName, cookieValue);

		tester.startPage(page);

		// assert that the cookie is not preserved for the next request cycle
		assertNull(tester.getRequest().getCookies());
	}

	/**
	 * A cookie set in the request headers should not be
	 * expected in the response headers unless the page
	 * sets it explicitly.
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4989
	 */
	@Test
	public void cookieSetInRequestShouldNotBeInResponse()
	{
		//start and render the test page
		tester.getRequest().addCookie(new Cookie("dummy", "sample"));
		tester.startPage(tester.getApplication().getHomePage());

		//assert rendered page class
		tester.assertRenderedPage(tester.getApplication().getHomePage());

		Assert.assertEquals("The cookie should not be in the response unless explicitly set",
				0, tester.getLastResponse().getCookies().size());

		// The cookie should be in each following request unless the server code
		// schedules it for removal it with cookie.setMaxAge(0)
		Assert.assertEquals("The cookie should be in each following request",
				1, tester.getRequest().getCookies().length);
	}

	/**
	 * The response cookie should not be the same instance as the request
	 * cookie.
	 *
	 * https://issues.apache.org/jira/browse/WICKET-4989
	 */
	@Test
	public void doNotReuseTheSameInstanceOfTheCookieForRequestAndResponse()
	{
		//start and render the test page
		String cookieName = "cookieName";
		String cookieValue = "cookieValue";
		Cookie requestCookie = new Cookie(cookieName, cookieValue);
		tester.getRequest().addCookie(requestCookie);
		tester.startPage(new CookiePage(cookieName, cookieValue));

		//assert rendered page class
		tester.assertRenderedPage(CookiePage.class);

		Cookie responseCookie = tester.getLastResponse().getCookies().get(0);
		requestCookie.setValue("valueChanged");

		Assert.assertEquals(cookieValue, responseCookie.getValue());
	}

	/**
	 * @see WicketTester
	 * 
	 * TODO add a cookie to request, which should override cookie from last response and last request
	 * https://issues.apache.org/jira/browse/WICKET-5147
	 */
	@Test
	public void wicketTesterCookieHandlingWithoutRedirect() {
		// no cookies set
		CollectAllRequestCookiesPage collectingPage = collectAllRequestCookiesOnThisPage();
		Assert.assertTrue("no cookie in first request",collectingPage.getCookies().isEmpty());
		lastResponseDoesNotHaveAnyCookies();
		responseDoesNotHaveAnyCookies();
		requestDoesNotHaveAnyCookies();
		
		// set cookie on request
		Cookie firstCookie = newCookie("a","firstValue",1);
		tester.getRequest().addCookie(firstCookie);
		collectingPage = collectAllRequestCookiesOnThisPage();
		requestOnPageShouldHaveTheseCookies(collectingPage, firstCookie);
		lastResponseDoesNotHaveAnyCookies();
		requestShouldHaveTheseCookies(firstCookie);
		responseDoesNotHaveAnyCookies();

		// cookies from last request should appear on following requests
		collectingPage = collectAllRequestCookiesOnThisPage();
		requestOnPageShouldHaveTheseCookies(collectingPage, firstCookie);
		lastResponseDoesNotHaveAnyCookies();
		requestShouldHaveTheseCookies(firstCookie);
		responseDoesNotHaveAnyCookies();

		// cookie will be overwritten if response will do so
		Cookie cookieSetInResponse = newCookie("a","overwriteWithNewValue",1);
		setCookieInResponse(cookieSetInResponse);
		lastResponseShouldHaveTheseCookies(cookieSetInResponse);
		requestShouldHaveTheseCookies(cookieSetInResponse);
		
		// cookies from last response then should appear on following requests
		collectingPage = collectAllRequestCookiesOnThisPage();
		requestOnPageShouldHaveTheseCookies(collectingPage, cookieSetInResponse);
		lastResponseDoesNotHaveAnyCookies();
		requestShouldHaveTheseCookies(cookieSetInResponse);
		
		// cookies from requests will be deleted if the response will do so
		Cookie expiredCookieSetInResponse = newCookie("a","removeMe",0);
		setCookieInResponse(expiredCookieSetInResponse);
		lastResponseShouldHaveTheseCookies(expiredCookieSetInResponse);
		responseDoesNotHaveAnyCookies();
		requestDoesNotHaveAnyCookies();
		
		// no cookies in next request while last cookie was deleted
		collectingPage = collectAllRequestCookiesOnThisPage();
		requestOnPageShouldHaveTheseCookies(collectingPage);
		lastResponseDoesNotHaveAnyCookies();
		requestDoesNotHaveAnyCookies();
		responseDoesNotHaveAnyCookies();
	}

	/**
	 * @see WicketTesterCookieTest#wicketTesterCookieHandlingWithoutRedirect()
	 *  
	 * https://issues.apache.org/jira/browse/WICKET-5147
	 */
	@Test
	public void wicketTesterCookieHandlingWithRedirect() {
		// set cookie in response then redirect to other page
		Cookie firstCookie = newCookie("a","firstValue",1);
		setCookieInResponseAndRedirect(firstCookie);
		lastResponseShouldHaveTheseCookies(firstCookie);
		requestShouldHaveTheseCookies(firstCookie);

		// cookie in response after redirect should appear in next request
		CollectAllRequestCookiesPage collectingPage = collectAllRequestCookiesOnThisPage();
		requestOnPageShouldHaveTheseCookies(collectingPage,firstCookie);
		lastResponseDoesNotHaveAnyCookies();
		requestShouldHaveTheseCookies(firstCookie);
		responseDoesNotHaveAnyCookies();
		
		// set cookie on request and overwrite in response then redirect to other page
		Cookie cookieSetInRequest = newCookie("a","valueFromRequest",1);
		Cookie cookieSetInResponse = newCookie("a","overwriteInResponse",1);
		tester.getRequest().addCookie(cookieSetInRequest);
		setCookieInResponseAndRedirect(cookieSetInResponse);
		lastResponseShouldHaveTheseCookies(cookieSetInResponse);
		requestShouldHaveTheseCookies(cookieSetInResponse);
		
		// cookie in response after redirect should appear in next request
		collectingPage = collectAllRequestCookiesOnThisPage();
		requestOnPageShouldHaveTheseCookies(collectingPage,cookieSetInResponse);
		lastResponseDoesNotHaveAnyCookies();
		requestShouldHaveTheseCookies(cookieSetInResponse);
		responseDoesNotHaveAnyCookies();
		
		// set cookie on request and remove it in response then redirect to other page
		Cookie nextCookieSetInRequest = newCookie("a","nextValueFromRequest",1);
		Cookie nextCookieSetInResponse = newCookie("a","newValue",0);
		tester.getRequest().addCookie(nextCookieSetInRequest);
		setCookieInResponseAndRedirect(nextCookieSetInResponse);
		lastResponseShouldHaveTheseCookies(nextCookieSetInResponse);
		requestDoesNotHaveAnyCookies();
		responseDoesNotHaveAnyCookies();
		
		// no cookies left
		collectingPage = collectAllRequestCookiesOnThisPage();
		requestOnPageShouldHaveTheseCookies(collectingPage);
		lastResponseDoesNotHaveAnyCookies();
		requestDoesNotHaveAnyCookies();
		responseDoesNotHaveAnyCookies();
	}
	
	/**
	 * creates a new cookie with maxAge set
	 * @param name name
	 * @param value value
	 * @param maxAge maxAge
	 * @return a cookie
	 */
	private static Cookie newCookie(String name,String value, int maxAge) {
		Cookie cookie = new Cookie(name,value);
		cookie.setMaxAge(maxAge);
		return cookie;
	}
	
	/**
	 * start a page which collects all cookies from request 
	 * @return the page
	 */
	private CollectAllRequestCookiesPage collectAllRequestCookiesOnThisPage()
	{
		return tester.startPage(CollectAllRequestCookiesPage.class);
	}

	/**
	 * start a page which set a cookie in response
	 * @param cookie cookie
	 */
	private void setCookieInResponse(Cookie cookie)
	{
		tester.startPage(new SetCookiePage(cookie));
	}

	/**
	 * start a page which set a cookie in response and then redirect to different page
	 * @param cookie cookie
	 */
	private void setCookieInResponseAndRedirect(Cookie cookie)
	{
		tester.startPage(new SetCookiePage(cookie,EndPage.class));
	}

	/**
	 * check cookies collected by page
	 * @param page page
	 * @param cookies cookies
	 */
	private void requestOnPageShouldHaveTheseCookies(CollectAllRequestCookiesPage page, Cookie...cookies) {
		listShouldMatchAll(page.getCookies(), cookies);
	}

	/**
	 * check cookies in current request
	 * @param cookies cookies
	 */
	private void requestShouldHaveTheseCookies(Cookie...cookies) {
		Cookie[] cookieFromRequest = tester.getRequest().getCookies();
		listShouldMatchAll(cookieFromRequest!=null ? Arrays.asList(cookieFromRequest) : new ArrayList<Cookie>(), cookies);
	}
	
	/**
	 * check if every cookie is found in the list and no cookie is left 
	 * @param cookieList cookie list
	 * @param cookies cookies to check
	 */
	private void listShouldMatchAll(List<Cookie> cookieList, Cookie... cookies)
	{
		Map<String, Cookie> cookieMap = cookiesFromList(cookieList);
		for (Cookie cookie : cookies) {
			Cookie removed = cookieMap.remove(cookie.getName());
			Assert.assertNotNull("Cookie "+cookie.getName(),removed);
			Assert.assertTrue("Cookie "+cookie.getName()+" matches",Cookies.isEqual(cookie, removed));
		}
		Assert.assertTrue("no cookies left "+asString(cookieMap),cookieMap.isEmpty());
	}
	
	/**
	 * make cookie map more readable
	 * @param cookieMap cookie map
	 * @return string
	 */
	private static String asString(Map<String, Cookie> cookieMap)
	{
		StringBuilder sb=new StringBuilder();
		sb.append('{');
		for (Map.Entry<String, Cookie> e : cookieMap.entrySet()) {
			sb.append(e.getKey()).append('=').append(asString(e.getValue()));
			sb.append(",");
		}
		sb.append('}');
		return sb.toString();
	}

	/**
	 * make cookie more readable
	 * @param c cookie
	 * @return string
	 */
	private static String asString(Cookie c)
	{
		StringBuilder sb=new StringBuilder();
		sb.append('[');
		sb.append("name=").append(c.getName()).append(',');
		sb.append("value=").append(c.getValue()).append(',');
		sb.append("maxAge=").append(c.getMaxAge());
		sb.append(']');
		return sb.toString();
	}

	/**
	 * check last response cookies
	 * @param cookies cookies
	 */
	private void lastResponseShouldHaveTheseCookies(Cookie...cookies) {
		listShouldMatchAll(tester.getLastResponse().getCookies(), cookies);
	}
	
	/**
	 * response should not have any cookies
	 */
	private void lastResponseDoesNotHaveAnyCookies()
	{
		listShouldMatchAll(tester.getLastResponse().getCookies());
	}
	
	/**
	 * response should not have any cookies
	 */
	private void responseDoesNotHaveAnyCookies()
	{
		listShouldMatchAll(tester.getResponse().getCookies());
	}
	
	/**
	 * request should not have any cookies
	 */
	private void requestDoesNotHaveAnyCookies()
	{
		requestShouldHaveTheseCookies();
	}
	
	/**
	 * create a cookie map based on cookie name
	 * @param cookies cookie list
	 * @return as map
	 * @throws RuntimeException if more than one cookie with the same name
	 */
	private static Map<String,Cookie> cookiesFromList(List<Cookie> cookies) {
		Map<String, Cookie> ret = new LinkedHashMap<String, Cookie>();
		for (Cookie cookie : cookies)
		{
			Cookie oldValue = ret.put(cookie.getName(), cookie);
			if (oldValue != null)
			{
				throw new RuntimeException(String.format("Cookie with name '%s' ('%s') already in map %s",
						cookie.getName(), asString(oldValue), asString(ret)));
			}
		}
		return ret;
	}

}
