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

/**
 * 
 * 
 * @author Frank Bille (billen)
 */
public class WebResponseTest extends TestCase
{
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
}
