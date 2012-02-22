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

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Page;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link ServletWebRequest}
 */
public class ServletWebRequestTest extends Assert
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
		assertEquals("request/Uri?some=parameter", clientUrl.toString());

		// simulates a request that has errors metadata
		httpRequest.setAttribute("javax.servlet.error.request_uri",
			"/" + httpRequest.getContextPath() + "/any/source/of/error");
		ServletWebRequest errorWebRequest = new ServletWebRequest(httpRequest, "/");
		Url errorClientUrl = errorWebRequest.getClientUrl();

		assertEquals("/any/source/of/error", errorClientUrl.toString());
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-4168">WICKET-4168</a>
	 */
	@Test
	public void testClientURLIsContextRelativeInErrorResponses()
	{
		MockHttpServletRequest httpRequest = new MockHttpServletRequest(null, null, null);
		httpRequest.setURL(httpRequest.getContextPath() + "/request/Uri");

		String problematiURI = httpRequest.getContextPath() + "/any/source/of/error";

		httpRequest.setAttribute("javax.servlet.error.request_uri", problematiURI);

		ServletWebRequest errorWebRequest = new ServletWebRequest(httpRequest, "");

		Url errorClientUrl = errorWebRequest.getClientUrl();

		assertEquals("any/source/of/error", errorClientUrl.toString());

	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4138
	 * 
	 * Relative Urls should be calculated against 'javax.servlet.forward.request_uri'
	 */
	@Test
	public void parseForwardAttributes()
	{
		MockHttpServletRequest httpRequest = new MockHttpServletRequest(null, null, null);
		httpRequest.setURL(httpRequest.getContextPath() + "/request/Uri");

		String forwardedURI = httpRequest.getContextPath() + "/some/forwarded/url";

		httpRequest.setAttribute("javax.servlet.forward.request_uri", forwardedURI);

		ServletWebRequest forwardWebRequest = new ServletWebRequest(httpRequest, "");

		Url forwardClientUrl = forwardWebRequest.getClientUrl();

		assertEquals("some/forwarded/url", forwardClientUrl.toString());


	}

	/**
	 * https://issues.apache.org/jira/browse/WICKET-4123
	 */
	@Test
	public void useCustomServletWebRequest()
	{
		WebApplication application = new WebApplication()
		{
			@Override
			public Class<? extends Page> getHomePage()
			{
				return CustomRequestPage.class;
			}

			@Override
			protected WebRequest newWebRequest(HttpServletRequest servletRequest, String filterPath)
			{
				return new CustomServletWebRequest(servletRequest, filterPath);
			}
		};

		WicketTester tester = new WicketTester(application);
		tester.startPage(new CustomRequestPage());
	}

	private static class CustomRequestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		private CustomRequestPage()
		{
			assertTrue(getRequest() instanceof CustomServletWebRequest);
		}

		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html></html>");
		}
	}

	private static class CustomServletWebRequest extends ServletWebRequest
	{
		public CustomServletWebRequest(HttpServletRequest httpServletRequest, String filterPrefix)
		{
			super(httpServletRequest, filterPrefix);
		}
	}
}
