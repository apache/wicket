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
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;


/**
 * Test of WebRequest.
 * 
 * @author Frank Bille (billen)
 */
public class WebRequestTest extends WicketTestCase
{
	/**
	 * Tests passing in an empty parameter.
	 */
	public void testEmptyParam()
	{
		MockHttpServletRequest mockRequest = tester.getServletRequest();
		mockRequest.setRequestToRedirectString("?a=");
		String value = mockRequest.getParameter("a");
		assertEquals("", value);

		mockRequest.setRequestToRedirectString("?a");
		value = mockRequest.getParameter("a");
		assertEquals("", value);
	}

	/**
	 * Test that ajax is true when the ajax header is present in the request
	 */
	public void testIsAjax_1()
	{
		assertWithHeader("Wicket-Ajax", "true", true);
	}

	/**
	 * Test that it also works when there are other "positive" values than true.
	 */
	public void testIsAjax_2()
	{
		assertWithHeader("Wicket-Ajax", "yes", true);
		assertWithHeader("Wicket-Ajax", "1", true);
		assertWithHeader("Wicket-Ajax", "on", true);
		assertWithHeader("Wicket-Ajax", "y", true);
	}

	/**
	 * Test that it's not ajax.
	 */
	public void testIsAjax_3()
	{
		assertWithHeader("dummyheader", "true", false);
		assertWithHeader("Wicket-Ajax", "false", false);
		assertWithHeader("Wicket-Ajax", "0", false);
		assertWithHeader("Wicket-Ajax", "off", false);
		assertWithHeader("Wicket-Ajax", "no", false);
		assertWithHeader("Wicket-Ajax", "Wicket-Ajax", false);
		assertWithHeader("true", "Wicket-Ajax", false);
		assertWithHeader("WicketAjax", "true", false);
		assertWithHeader("wicketajax", "true", false);
	}

	/**
	 * Tests passing in a string array.
	 */
	public void testStringArray()
	{
		MockHttpServletRequest mockRequest = tester.getServletRequest();
		mockRequest.setRequestToRedirectString("?a=1&a=2");
		Object obj = mockRequest.getParameterMap().get("a");
		assertTrue("Expected " + new String[0].getClass() + ", got " + obj.getClass(),
			obj instanceof String[]);
	}

	/**
	 * Tests encoded string.
	 */
	public void testStringEncoding()
	{
		MockHttpServletRequest mockRequest = tester.getServletRequest();
		mockRequest.setRequestToRedirectString("?a=%20");
		String value = mockRequest.getParameter("a");
		assertEquals(" ", value);
	}

	private void assertWithHeader(String header, String value, boolean isAjax)
	{
		MockHttpServletRequest mockRequest = tester.getServletRequest();
		mockRequest.addHeader(header, value);

		WebRequest webRequest = new ServletWebRequest(mockRequest);

		assertEquals(isAjax, webRequest.isAjax());
	}

	/**
	 * Test handling of null parameter values.
	 */
	public void testNullHandling()
	{
		MockHttpServletRequest mockRequest = tester.getServletRequest();
		mockRequest.setParameter("a", null);
		assertNull(mockRequest.getAttribute("a"));
		assertEquals("a=", mockRequest.getQueryString());
	}

	/**
	 * Test handling of null parameter keys.
	 */
	public void testNullHandling2()
	{
		MockHttpServletRequest mockRequest = tester.getServletRequest();
		mockRequest.setRequestToRedirectString("?=m"); // key is encoded as empty string
		assertEquals("=m", mockRequest.getQueryString());
		mockRequest.setParameter(null, "m2"); // force null string
		assertEquals("=m&=m2", mockRequest.getQueryString());

	}
}
