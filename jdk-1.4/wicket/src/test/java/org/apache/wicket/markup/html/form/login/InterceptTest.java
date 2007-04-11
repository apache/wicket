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
package org.apache.wicket.markup.html.form.login;

import javax.servlet.http.HttpServletResponse;

import junit.framework.TestCase;

import org.apache.wicket.Component;
import org.apache.wicket.ISessionFactory;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.WicketTester;


/**
 * @author marrink
 * 
 */
public class InterceptTest extends TestCase
{
	private WicketTester application;

	/**
	 * Constructor for InterceptTest.
	 * 
	 * @param arg0
	 */
	public InterceptTest(String arg0)
	{
		super(arg0);
	}

	/**
	 * @see TestCase#setUp()
	 */
	protected void setUp() throws Exception
	{
		application = new WicketTester(new MyMockWebApplication());
	}

	/**
	 * @see TestCase#tearDown()
	 */
	protected void tearDown() throws Exception
	{
		application.destroy();
	}

	/**
	 * 
	 */
	public void testClickLink()
	{
		application.setupRequestAndResponse();
		application.processRequestCycle();
		MockLoginPage loginPage = (MockLoginPage)application.getLastRenderedPage();
		assertEquals(((MyMockWebApplication)application.getApplication()).getLoginPage(), loginPage.getClass());

		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(loginPage.getForm());
		application.getServletRequest().setParameter(loginPage.getTextField().getInputName(),
				"admin");
		application.processRequestCycle();

		// continueToInterceptPage seems to return the same call, causing it to
		// login twice as a result the lastrendered page is null
		assertEquals(application.getApplication().getHomePage(), application.getLastRenderedPage().getClass());

		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(
				application.getLastRenderedPage().get("link"));
		application.processRequestCycle();
		assertEquals(PageA.class, application.getLastRenderedPage().getClass());
	}

	/**
	 * 
	 */
	public void testClickLink2()
	{
		// same as above but uses different technique to login
		application.setupRequestAndResponse();
		application.processRequestCycle();
		MockLoginPage loginPage = (MockLoginPage)application.getLastRenderedPage();
		assertEquals(((MyMockWebApplication)application.getApplication()).getLoginPage(), loginPage.getClass());

		// bypass form completely to login but continue to intercept page
		application.setupRequestAndResponse();
		WebRequestCycle requestCycle = application.createRequestCycle();
		assertTrue(((MockLoginPage)application.getLastRenderedPage()).login("admin"));
		application.processRequestCycle(requestCycle);
		assertEquals(application.getApplication().getHomePage(), application.getLastRenderedPage().getClass());

		application.setupRequestAndResponse();
		application.getServletRequest().setRequestToComponent(
				application.getLastRenderedPage().get("link"));
		application.processRequestCycle();
		assertEquals(PageA.class, application.getLastRenderedPage().getClass());
	}

	/**
	 * 
	 * @author
	 */
	private static class MyMockWebApplication extends WebApplication implements ISessionFactory
	{
		private static final long serialVersionUID = 1L;
		public Class getHomePage()
		{
			return MockHomePage.class;
		}

		protected void init()
		{
			getSecuritySettings().setAuthorizationStrategy(new MyAuthorizationStrategy());
		}

		/**
		 * 
		 * @return Class
		 */
		public Class getLoginPage()
		{
			return MockLoginPage.class;
		}

		/**
		 * 
		 * @see org.apache.wicket.ISessionFactory#newSession(Request, Response)
		 */
		public Session newSession(Request request, Response response)
		{
			return new MySession(this, request, response);
		}
		
		protected WebResponse newWebResponse(HttpServletResponse servletResponse)
		{
			return new WebResponse(servletResponse);
		}
	}

	/**
	 * 
	 */
	public static class MySession extends WebSession
	{
		private static final long serialVersionUID = 1L;

		private String username;

		/**
		 * @param application
		 * @param request
		 */
		protected MySession(WebApplication application, Request request, Response response)
		{
			super(application, request, response);
		}

		protected final String getUsername()
		{
			return username;
		}

		protected final void setUsername(String username)
		{
			this.username = username;
		}

		/**
		 * 
		 * @return boolean
		 */
		public boolean isLoggedIn()
		{
			return !Strings.isEmpty(username);
		}
	}

	/**
	 * 
	 */
	private static class MyAuthorizationStrategy implements IAuthorizationStrategy
	{
		/**
		 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
		 */
		public boolean isInstantiationAuthorized(Class componentClass)
		{
			if (MockHomePage.class.equals(componentClass)
					&& !((MySession)Session.get()).isLoggedIn())
			{
				throw new RestartResponseAtInterceptPageException(MockLoginPage.class);
			}
			return true;
		}

		/**
		 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
		 *      org.apache.wicket.authorization.Action)
		 */
		public boolean isActionAuthorized(Component component, Action action)
		{
			return true;
		}
	}
}
