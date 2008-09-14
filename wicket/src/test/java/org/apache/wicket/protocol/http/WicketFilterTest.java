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

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.wicket.Application;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.util.tester.WicketTester.DummyWebApplication;

public class WicketFilterTest extends TestCase
{
	private static WebApplication application;
	private final DateFormat fullDateFormat = DateFormat.getDateInstance(DateFormat.FULL);
	private final DateFormat headerDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",
		Locale.UK);

	@Override
	protected void tearDown() throws Exception
	{
		application = null;
	}

	public void testFilterPath1()
	{
		InputStream in = WicketFilterTest.class.getResourceAsStream("web1.xml");
		String filterPath = getFilterPath("FilterTestApplication", in);
		assertEquals("filtertest/", filterPath);
	}

	public void testFilterPath2()
	{
		InputStream in = WicketFilterTest.class.getResourceAsStream("web2.xml");
		String filterPath = getFilterPath("FilterTestApplication", in);
		assertEquals("filtertest/", filterPath);
	}

	public void testNotModifiedResponseIncludesExpiresHeader() throws IOException,
		ServletException, ParseException
	{
		application = new DummyWebApplication();
		WicketFilter filter = new WicketFilter();
		filter.init(new FilterTestingConfig());
		Application.set(application);
		DynamicImageResource resource = new DynamicImageResource()
		{
			private static final long serialVersionUID = 1L;

			@Override
			protected byte[] getImageData()
			{
				throw new UnsupportedOperationException("Not implemented");
			}
		};
		resource.setCacheable(true);
		application.getSharedResources().add("foo.gif", resource);
		MockHttpServletRequest request = new MockHttpServletRequest(application, null, null);
		request.setURL(request.getContextPath() + "/app/" + "resources/" +
			Application.class.getName() + "/foo.gif");
		setIfModifiedSinceToNextWeek(request);
		MockHttpServletResponse response = new MockHttpServletResponse(request);
		filter.doFilter(request, response, new FilterChain()
		{
			public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
				throws IOException, ServletException
			{
			}
		});
		assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatus());
		String responseExpiresHeader = response.getHeader("Expires");
		assertNotNull("Expires header must be set on not modified response", responseExpiresHeader);

		Date responseExpires = headerDateFormat.parse(responseExpiresHeader);
		assertTrue("Expected later than current date but was " + responseExpires,
			responseExpires.after(new Date()));
	}

	private void setIfModifiedSinceToNextWeek(MockHttpServletRequest request)
	{
		Calendar nextWeek = Calendar.getInstance();
		nextWeek.add(Calendar.DATE, 7);
		nextWeek.setTimeZone(TimeZone.getTimeZone("GMT"));
		String ifModifiedSince = fullDateFormat.format(nextWeek.getTime());
		request.addHeader("If-Modified-Since", ifModifiedSince);
	}

	private String getFilterPath(String string, InputStream in)
	{
		try
		{
			Method method = WicketFilter.class.getDeclaredMethod("getFilterPath", String.class,
				InputStream.class);
			method.setAccessible(true);
			return method.invoke(new WicketFilter(), string, in).toString();
		}
		catch (SecurityException e)
		{
			throw new RuntimeException(e);
		}
		catch (NoSuchMethodException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalArgumentException e)
		{
			throw new RuntimeException(e);
		}
		catch (IllegalAccessException e)
		{
			throw new RuntimeException(e);
		}
		catch (InvocationTargetException e)
		{
			throw new RuntimeException(e);
		}
	}

	private static class FilterTestingConfig implements FilterConfig
	{
		private final Map<String, String> initParameters = new HashMap<String, String>();

		public FilterTestingConfig()
		{
			initParameters.put(WicketFilter.APP_FACT_PARAM,
				FilterTestingApplicationFactory.class.getName());
			initParameters.put(WicketFilter.FILTER_MAPPING_PARAM, "/app/*");
			initParameters.put(ContextParamWebApplicationFactory.APP_CLASS_PARAM,
				DummyWebApplication.class.getName());
		}

		public String getFilterName()
		{
			return getClass().getName();
		}

		public ServletContext getServletContext()
		{
			return new MockServletContext(null, null);
		}

		public String getInitParameter(String s)
		{
			return initParameters.get(s);
		}

		public Enumeration<String> getInitParameterNames()
		{
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	public static class FilterTestingApplicationFactory implements IWebApplicationFactory
	{
		public WebApplication createApplication(WicketFilter filter)
		{
			return application;
		}
	}
}
