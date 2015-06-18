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

import java.io.IOException;
import java.lang.Thread.UncaughtExceptionHandler;
import java.util.Enumeration;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.session.HttpSessionStore;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.util.IProvider;
import org.apache.wicket.util.tester.WicketTestCase;
import org.apache.wicket.util.tester.WicketTester;
import org.apache.wicket.util.time.Duration;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link WicketSessionFilter}
 */
public class WicketSessionFilterTest extends WicketTestCase
{

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			protected void init()
			{
				super.init();

				// use HttpSessionStore because we need to test it
				setSessionStoreProvider(new IProvider<ISessionStore>()
				{
					@Override
					public ISessionStore get()
					{
						return new HttpSessionStore();
					}
				});
			}
		};
	}

	/**
	 * <a href="https://issues.apache.org/jira/browse/WICKET-3769">WICKET-3769</a>
	 * 
	 * @throws Exception
	 */
	@Test
	public void applicationAndSessionAreExported() throws Exception
	{
		// bind the session so it can be found in TestSessionFilter
		tester.getSession().bind();

		// execute TestSessionFilter in different thread so that the Application and the Session are
		// not set by WicketTester
		Thread testThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				try
				{
					TestSessionFilter sessionFilter = new TestSessionFilter(tester);

					Assert.assertFalse(Application.exists());
					Assert.assertFalse(Session.exists());

					sessionFilter.doFilter(tester.getRequest(), tester.getResponse(),
						new TestFilterChain());

					Assert.assertFalse(Application.exists());
					Assert.assertFalse(Session.exists());

				}
				catch (Exception e)
				{
					throw new RuntimeException(e.getMessage(), e);
				}
			}
		});

		final StringBuilder failMessage = new StringBuilder();
		final AtomicBoolean passed = new AtomicBoolean(true);

		testThread.setUncaughtExceptionHandler(new UncaughtExceptionHandler()
		{
			@Override
			public void uncaughtException(Thread t, Throwable e)
			{
				failMessage.append(e.getMessage());
				passed.set(false);
			}
		});
		testThread.start();
		testThread.join(Duration.seconds(1).getMilliseconds());

		Assert.assertTrue(failMessage.toString(), passed.get());
	}

	/**
	 * A {@link WicketSessionFilter} that uses current application's name for "filterName" attribute
	 * value
	 */
	private static class TestSessionFilter extends WicketSessionFilter
	{
		public TestSessionFilter(final WicketTester tester) throws ServletException
		{
			init(new FilterConfig()
			{
				@Override
				public ServletContext getServletContext()
				{
					return tester.getServletContext();
				}

				@Override
				@SuppressWarnings("rawtypes")
				public Enumeration getInitParameterNames()
				{
					return null;
				}

				@Override
				public String getInitParameter(String name)
				{
					if ("filterName".equals(name))
					{
						return tester.getApplication().getName();
					}

					return null;
				}

				@Override
				public String getFilterName()
				{
					return "session-filter";
				}
			});
		}

		@Override
		public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
		{
			super.doFilter(request, response, chain);
		}
	}

	/**
	 * A mock for a Servlet
	 */
	private static class TestFilterChain implements FilterChain
	{
		@Override
		public void doFilter(ServletRequest request, ServletResponse response) throws IOException,
			ServletException
		{
			if (Application.exists() == false)
			{
				throw new AssertionError("The application is not available!");
			}

			if (Session.exists() == false)
			{
				throw new AssertionError("The session is not available!");
			}
		}

	}
}
