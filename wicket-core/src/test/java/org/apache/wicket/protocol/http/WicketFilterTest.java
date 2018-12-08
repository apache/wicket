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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.io.InputStream;
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
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.commons.io.filefilter.AgeFileFilter;
import org.apache.wicket.Application;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.mock.MockHttpServletRequest;
import org.apache.wicket.protocol.http.mock.MockHttpServletResponse;
import org.apache.wicket.protocol.http.mock.MockServletContext;
import org.apache.wicket.protocol.http.servlet.ResponseIOException;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.resource.AbstractResource;
import org.apache.wicket.request.resource.ContentDisposition;
import org.apache.wicket.request.resource.DynamicImageResource;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.AbstractResource.ResourceResponse;
import org.apache.wicket.request.resource.AbstractResource.WriteCallback;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.util.WicketTestTag;
import org.apache.wicket.util.file.WebXmlFile;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.DummyHomePage;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.xml.sax.SAXException;

/**
 */
public class WicketFilterTest
{
	private static WebApplication application;
	private final DateFormat headerDateFormat = new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss z",
		Locale.UK);

	/**
	 * @throws Exception
	 */
	@AfterEach
	void after() throws Exception
	{
		if (application != null)
		{
			application.internalDestroy();
			application = null;
		}
	}

	/**
	 * Test for WICKET-5980 When using Servlet 3.0 filter Wicket calculates filter path wrong.
	 * 
	 * When using a servlet 3.0 filter with annotations Wicket calculates the filter path wrong
	 * causing it to not match any pages other than the home page. e.g.
	 * 
	 * <pre>
	 * &#64;WebFilter(value = "/web/*", initParams = {
	 * 		&#64;WebInitParam(name = "applicationClassName", value = "com.example.CheesrApplication") })
	 * public class CheesrFilter extends WicketFilter
	 * {
	 * }
	 * </pre>
	 * 
	 * @throws Exception
	 */
	@Test
	void parsingOfAnnotatedServlet3FiltersWorks() throws Exception
	{
		FilterTestingConfig config = new FilterTestingConfig();
		config.initParameters.clear();
		config.initParameters.put("applicationClassName", "org.apache.wicket.mock.MockApplication");

		WicketFilter filter = new AnnotatedServlet3Filter();
		// creates an Application
		filter.init(config);

		// get a reference to the application, so that @AfterEach is able to clean it up
		application = filter.getApplication();

		// assert that the filter path is not /web/*/
		assertEquals("web/", filter.getFilterPath());
	}

	/**
	 * testFilterPath1()
	 */
	@Test
	@Tag(WicketTestTag.SLOW)
	void filterPath1()
	{
		InputStream in = WicketFilterTest.class.getResourceAsStream("web1.xml");
		String filterPath = getFilterPath("FilterTestApplication", in);
		assertEquals("filtertest/", filterPath);
	}

	/**
	 * testFilterPath2()
	 */
	@Test
	@Tag(WicketTestTag.SLOW)
	void filterPath2()
	{
		InputStream in = WicketFilterTest.class.getResourceAsStream("web2.xml");
		String filterPath = getFilterPath("FilterTestApplication", in);
		assertEquals("filtertest/", filterPath);
	}

	/**
	 * @throws IOException
	 * @throws ServletException
	 * @throws ParseException
	 */
	@Test
	void notModifiedResponseIncludesExpiresHeader() throws IOException, ServletException,
		ParseException
	{
		try
		{
			application = new MockApplication();
			WicketFilter filter = new WicketFilter();
			filter.init(new FilterTestingConfig());
			ThreadContext.setApplication(application);
			DynamicImageResource resource = new DynamicImageResource()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected byte[] getImageData(Attributes attributes)
				{
					throw new UnsupportedOperationException("Not implemented");
				}

				@Override
				protected ResourceResponse newResourceResponse(Attributes attributes)
				{
					ResourceResponse response = super.newResourceResponse(attributes);
					response.setCacheDurationToMaximum();
					return response;
				}
			};
			application.getSharedResources().add("foo.gif", resource);
			MockHttpServletRequest request = new MockHttpServletRequest(application, null, null);
			request.setURL(request.getContextPath() + request.getServletPath() +
				"/wicket/resource/" + Application.class.getName() + "/foo.gif");
			setIfModifiedSinceToNextWeek(request);
			MockHttpServletResponse response = new MockHttpServletResponse(request);
			filter.doFilter(request, response, new FilterChain()
			{
				@Override
				public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
					throws IOException, ServletException
				{
				}
			});
			assertEquals(HttpServletResponse.SC_NOT_MODIFIED, response.getStatus());
			String responseExpiresHeader = response.getHeader("Expires");
			assertNotNull(responseExpiresHeader, "Expires header must be set on not modified response");

			Date responseExpires = headerDateFormat.parse(responseExpiresHeader);
			assertTrue(responseExpires.after(new Date()), "Expected later than current date but was " + responseExpires);
		}
		finally
		{
			ThreadContext.detach();
		}
	}

	/**
	 * @throws IOException
	 * @throws ServletException
	 * @throws ParseException
	 */
	@Test
	public void ioExceptionNotWrapped() throws IOException, ServletException, ParseException
	{
		assertThrows(IOException.class, this::doIOExceptionNotWrapped);
	}

	private void doIOExceptionNotWrapped() throws IOException, ServletException, ParseException
	{
		try
		{
			application = new MockApplication();
			WicketFilter filter = new WicketFilter();
			filter.init(new FilterTestingConfig());
			ThreadContext.setApplication(application);
			IResource resource = new AbstractResource()
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected ResourceResponse newResourceResponse(Attributes attributes)
				{
					return new ResourceResponse();
				}
			};
			application.getSharedResources().add("foo.txt", resource);
			MockHttpServletRequest request = new MockHttpServletRequest(application, null, null);
			request.setURL(request.getContextPath() + request.getServletPath() +
				"/wicket/resource/" + Application.class.getName() + "/foo.txt");
			setIfModifiedSinceToNextWeek(request);
			MockHttpServletResponse response = new MockHttpServletResponse(request)
			{
				@Override
				public void flushBuffer() throws IOException
				{
					throw new IOException("caused by test");
				}
			};
			filter.doFilter(request, response, new FilterChain()
			{
				@Override
				public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
					throws IOException, ServletException
				{
				}
			});
		}
		finally
		{
			ThreadContext.detach();
		}
	}

	@Test
	void options() throws IOException, ServletException, ParseException
	{
		try
		{
			application = new MockApplication();
			WicketFilter filter = new WicketFilter();
			filter.init(new FilterTestingConfig());
			ThreadContext.setApplication(application);
			final String failure = "Should never get here when an OPTIONS request is issued";
			IResource resource = new AbstractResource()
			{
				@Override
				protected ResourceResponse newResourceResponse(Attributes attributes)
				{

					fail(failure);
					return null;
				}
			};
			application.getSharedResources().add("foo.txt", resource);

			// check OPTIONS request is processed correctly

			MockHttpServletRequest request = new MockHttpServletRequest(application, null, null);
			request.setURL(request.getContextPath() + request.getServletPath() +
				"/wicket/resource/" + Application.class.getName() + "/foo.txt");
			request.setMethod("OPtioNS"); // test that we do not care about case
			MockHttpServletResponse response = new MockHttpServletResponse(request);
			filter.doFilter(request, response, new FilterChain()
			{
				@Override
				public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse)
					throws IOException, ServletException
				{
				}
			});

			assertEquals(HttpServletResponse.SC_OK, response.getStatus());
			assertEquals("0", response.getHeader("Content-Length"));
			assertFalse(Strings.isEmpty(response.getHeader("Allow")));
			assertTrue(response.getHeader("Allow").toUpperCase(Locale.ROOT).contains("GET"));
			assertTrue(response.getHeader("Allow").toUpperCase(Locale.ROOT).contains("POST"));

			// try with a GET request to make sure we fail correctly

			request = new MockHttpServletRequest(application, null, null);
			request.setURL(request.getContextPath() + request.getServletPath() +
				"/wicket/resource/" + Application.class.getName() + "/foo.txt");
			response = new MockHttpServletResponse(request);
			try
			{
				filter.doFilter(request, response, new FilterChain()
				{
					@Override
					public void doFilter(ServletRequest servletRequest,
						ServletResponse servletResponse) throws IOException, ServletException
					{
					}
				});
			}
			catch (AssertionError e)
			{
				assertTrue(failure.equals(e.getMessage()));
			}

		}
		finally
		{
			ThreadContext.detach();
		}
	}


	private void setIfModifiedSinceToNextWeek(MockHttpServletRequest request)
	{
		Calendar nextWeek = Calendar.getInstance();
		nextWeek.add(Calendar.DATE, 7);
		nextWeek.setTimeZone(TimeZone.getTimeZone("GMT"));
		request.addDateHeader("If-Modified-Since", nextWeek.getTimeInMillis());
	}

	private String getFilterPath(String filterName, InputStream in)
	{
		try
		{
			return new WebXmlFile().getUniqueFilterPath(false, filterName, in);
		}
		catch (ParserConfigurationException | IOException | SAXException ex)
		{
			throw new RuntimeException(ex);
		}
	}

    public static class FilterTestingConfig implements FilterConfig
	{
		private final Map<String, String> initParameters = new HashMap<>();

        public FilterTestingConfig()
		{
			initParameters.put(WicketFilter.APP_FACT_PARAM,
				FilterTestingApplicationFactory.class.getName());
			initParameters.put(WicketFilter.FILTER_MAPPING_PARAM, "/servlet/*");
			initParameters.put(ContextParamWebApplicationFactory.APP_CLASS_PARAM,
				MockApplication.class.getName());
			initParameters.put(WicketFilter.IGNORE_PATHS_PARAM, "/css,/js,images");
		}

		@Override
		public String getFilterName()
		{
			return getClass().getName();
		}

		@Override
		public ServletContext getServletContext()
		{
			return new MockServletContext(null, null);
		}

		@Override
		public String getInitParameter(String s)
		{
			return initParameters.get(s);
		}

		@Override
		public Enumeration<String> getInitParameterNames()
		{
			throw new UnsupportedOperationException("Not implemented");
		}
	}

	/**
	 */
    public static class FilterTestingApplicationFactory implements IWebApplicationFactory
	{
		@Override
		public WebApplication createApplication(WicketFilter filter)
		{
			return application;
		}

		/** {@inheritDoc} */
		@Override
		public void destroy(WicketFilter filter)
		{
		}
	}

	/**
	 * testCheckRedirect_1()
	 */
	@Test
	void checkRedirect_1()
	{
		WicketFilter filter = new WicketFilter();

		// Simulate url-pattern = "/*" and request = http://localhost:8080 => null == no redirect
		filter.setFilterPath("");
		assertNull(filter.checkIfRedirectRequired("/", ""));
	}

	private static class CheckRedirectWorker implements Runnable
	{
		private final WicketFilter filter;
		private final CountDownLatch startLatch;
		private final CountDownLatch finishLatch;
		private final AtomicInteger successCount;

		CheckRedirectWorker(WicketFilter filter, CountDownLatch startLatch,
							CountDownLatch finishLatch, AtomicInteger successCount)
		{
			this.filter = filter;
			this.startLatch = startLatch;
			this.finishLatch = finishLatch;
			this.successCount = successCount;
		}

		@Override
		public void run()
		{
			try
			{
				try
				{
					startLatch.await(2, TimeUnit.SECONDS);
				}
				catch (InterruptedException e)
				{
					fail();
				}
				assertEquals("/filter/", filter.checkIfRedirectRequired("/filter", ""));
				successCount.incrementAndGet();
			}
			finally
			{
				finishLatch.countDown();
			}
		}
	}

	/**
	 * Starts {@code threadCount} threads which try to check whether a redirect is required and
	 * initialize {@link WicketFilter#filterPathLength}
	 * 
	 * @param threadCount
	 *            the number of simultaneous threads
	 */
	private void parallelCheckRedirect(int threadCount)
	{
		WicketFilter filter = new WicketFilter();
		filter.setFilterPath("filter/");
		AtomicInteger successCount = new AtomicInteger(0);
		CountDownLatch startLatch = new CountDownLatch(1);
		CountDownLatch finishLatch = new CountDownLatch(threadCount);
		for (int i = 0; i < threadCount; i++)
		{
			new Thread(new CheckRedirectWorker(filter, startLatch, finishLatch, successCount)).start();
		}
		startLatch.countDown();
		try
		{
			finishLatch.await(2, TimeUnit.SECONDS);
		}
		catch (InterruptedException e)
		{
			fail();
		}
		assertEquals(0, finishLatch.getCount(), "all threads finished");
		assertEquals(threadCount, successCount.get(), "all threads finished");
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3544">WICKET-3544</a>
	 * <p>
	 * Runs 1000 times 8 simultaneous threads which try to initialize WicketFilter#filterPathLength
	 */
	@Test
	void repeatedParallelCheckRedirect()
	{
		int threadCount = 8;
		int repeatCount = 1000;
		for (int i = 0; i < repeatCount; i++)
		{
			parallelCheckRedirect(threadCount);
		}
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3750">WICKET-3750</a>
	 * 
	 * @throws Exception
	 */
	@Test
	void ignorePaths() throws Exception
	{
		application = spy(new MockApplication());
		WicketFilter filter = new WicketFilter();
		filter.init(new FilterTestingConfig());

		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getLocale()).thenReturn(new Locale("bg", "BG"));
		when(request.getRequestURI()).thenReturn("/contextPath/js/bla.js")
			.thenReturn("/contextPath/css/bla.css")
			.thenReturn("/contextPath/images/bla.img")
			.thenReturn("/contextPath/servlet/wicket/bookmarkable/" + DummyHomePage.class.getName());
		when(request.getContextPath()).thenReturn("/contextPath");
		when(request.getMethod()).thenReturn("POST");
		HttpServletResponse response = mock(HttpServletResponse.class);
		when(response.encodeURL(ArgumentMatchers.anyString())).thenAnswer(new Answer<String>()
		{
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable
			{
				return (String)invocation.getArguments()[0];
			}
		});
		when(response.encodeRedirectURL(ArgumentMatchers.anyString())).thenAnswer(new Answer<String>()
		{
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable
			{
				return (String)invocation.getArguments()[0];
			}
		});
		FilterChain chain = mock(FilterChain.class);

		// execute 3 requests - 1 for bla.js, 1 for bla.css and 1 for bla.img
		for (int i = 0; i < 3; i++)
		{
			boolean isProcessed = filter.processRequest(request, response, chain);
			assertFalse(isProcessed);
			verify(application, Mockito.never()).newWebRequest(ArgumentMatchers.eq(request),
				ArgumentMatchers.anyString());
			verify(application, Mockito.never()).newWebResponse(ArgumentMatchers.any(WebRequest.class),
				ArgumentMatchers.eq(response));
			verify(chain, Mockito.times(i + 1)).doFilter(request, response);
		}

		// execute the request to /something/real
		boolean isProcessed = filter.processRequest(request, response, chain);
		assertTrue(isProcessed);
		verify(application).newWebRequest(ArgumentMatchers.eq(request), ArgumentMatchers.anyString());
		verify(application).newWebResponse(ArgumentMatchers.any(WebRequest.class), ArgumentMatchers.eq(response));
		// the request is processed so the chain is not executed
		verify(chain, Mockito.times(3)).doFilter(request, response);
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-4626">WICKET-4626</a>
	 * <p>
	 * Test method WicketFilter#canonicaliseFilterPath(String)
	 * </p>
	 */
	@Test
	void canonicaliseFilterPath()
	{
		String s;

		s = WicketFilter.canonicaliseFilterPath("");
		assertEquals("", s);

		s = WicketFilter.canonicaliseFilterPath("/");
		assertEquals("", s);

		s = WicketFilter.canonicaliseFilterPath("//");
		assertEquals("", s);

		s = WicketFilter.canonicaliseFilterPath("///");
		assertEquals("", s);

		s = WicketFilter.canonicaliseFilterPath("/wicket");
		assertEquals("wicket/", s);

		s = WicketFilter.canonicaliseFilterPath("/wicket/");
		assertEquals("wicket/", s);

		s = WicketFilter.canonicaliseFilterPath("wicket/");
		assertEquals("wicket/", s);

		s = WicketFilter.canonicaliseFilterPath("wicket");
		assertEquals("wicket/", s);

		s = WicketFilter.canonicaliseFilterPath("///wicket");
		assertEquals("wicket/", s);

		s = WicketFilter.canonicaliseFilterPath("///wicket///");
		assertEquals("wicket/", s);

		s = WicketFilter.canonicaliseFilterPath("wicket///");
		assertEquals("wicket/", s);

		s = WicketFilter.canonicaliseFilterPath("/wicket/foobar");
		assertEquals("wicket/foobar/", s);

		s = WicketFilter.canonicaliseFilterPath("/wicket/foobar/");
		assertEquals("wicket/foobar/", s);

		s = WicketFilter.canonicaliseFilterPath("wicket/foobar/");
		assertEquals("wicket/foobar/", s);

		s = WicketFilter.canonicaliseFilterPath("/wicket///foobar/");
		assertEquals("wicket///foobar/", s); // ok we're not perfect!
	}

}
