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

import wicket.protocol.http.servlet.ServletWebRequest;
import junit.framework.TestCase;

/**
 * Test of WebRequest.
 * 
 * @author Frank Bille (billen)
 */
public class WebRequestTest extends TestCase
{
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

	private void assertWithHeader(String header, String value, boolean isAjax)
	{
		MockHttpServletRequest mockRequest = new MockWebApplication(null).getServletRequest();
		mockRequest.addHeader(header, value);

		WebRequest webRequest = new ServletWebRequest(mockRequest);

		assertEquals(isAjax, webRequest.isAjax());
	}
}
