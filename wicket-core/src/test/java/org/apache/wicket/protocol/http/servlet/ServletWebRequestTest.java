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
package org.apache.wicket.protocol.http.servlet;

import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.Url;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ServletWebRequest}
 */
public class ServletWebRequestTest
{

	/**
	 * Tests that {@link ServletWebRequest#getClientUrl()} returns the current url + the query
	 * string when this is not error dispatched request. When the request is error dispatched it
	 * returns just the request uri to the error page without the query string
	 */
	@Test
	public void wicket3599()
	{
		MockHttpServletRequest httpRequest = new MockHttpServletRequest(null, null, null);
		httpRequest.setURL("/" + httpRequest.getContextPath() + "/request/Uri");
		httpRequest.setParameter("some", "parameter");

		ServletWebRequest webRequest = new ServletWebRequest(httpRequest, "/");
		Url clientUrl = webRequest.getClientUrl();
		Assert.assertEquals("request/Uri?some=parameter", clientUrl.toString());

		// error dispatched
		httpRequest.setAttribute("javax.servlet.error.request_uri", "/some/error/url");
		ServletWebRequest errorWebRequest = new ServletWebRequest(httpRequest, "/");
		Url errorClientUrl = errorWebRequest.getClientUrl();

		Assert.assertEquals("/some/error/url", errorClientUrl.toString());
	}
}
