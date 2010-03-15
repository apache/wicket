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

import junit.framework.TestCase;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;


/**
 * @author marrink
 * 
 */
public class InterceptTest extends TestCase
{
	private WicketTester application;


	/**
	 * @see TestCase#setUp()
	 */
	@Override
	protected void setUp() throws Exception
	{
		application = new WicketTester(new MyMockWebApplication());
	}

	/**
	 * @see TestCase#tearDown()
	 */
	@Override
	protected void tearDown() throws Exception
	{
		application.destroy();
	}

	/**
	 * 
	 */
	public void testFormSubmit()
	{
		// same as above but uses different technique to login
		application.startPage(application.getApplication().getHomePage());
		MockLoginPage loginPage = (MockLoginPage)application.getLastRenderedPage();
		assertEquals(((MyMockWebApplication)application.getApplication()).getLoginPage(),
			loginPage.getClass());
		FormTester form = application.newFormTester("form");
		form.setValue("username", "admin");
		form.submit();
		assertEquals(application.getApplication().getHomePage(), application.getLastRenderedPage()
			.getClass());
	}

	/**
	 * 
	 */
	public void testClickLink()
	{
		application.startPage(application.getApplication().getHomePage());
		MockLoginPage loginPage = (MockLoginPage)application.getLastRenderedPage();
		assertEquals(((MyMockWebApplication)application.getApplication()).getLoginPage(),
			loginPage.getClass());

		FormTester form = application.newFormTester("form");
		form.setValue("username", "admin");
		form.submit();

		assertEquals(application.getApplication().getHomePage(), application.getLastRenderedPage()
			.getClass());

		application.clickLink(application.getLastRenderedPage().get("link"));
		assertEquals(PageA.class, application.getLastRenderedPage().getClass());
	}

	/**
	 * 
	 */
	public void testClickLink2()
	{
		// same as above but uses different technique to login
		application.startPage(application.getApplication().getHomePage());
		MockLoginPage loginPage = (MockLoginPage)application.getLastRenderedPage();
		assertEquals(((MyMockWebApplication)application.getApplication()).getLoginPage(),
			loginPage.getClass());

		// bypass form completely to login but continue to intercept page
		assertTrue(((MockLoginPage)application.getLastRenderedPage()).login("admin"));
		application.startPage(application.getApplication().getHomePage());

		assertEquals(application.getApplication().getHomePage(), application.getLastRenderedPage()
			.getClass());

		application.clickLink(application.getLastRenderedPage().get("link"));
		assertEquals(PageA.class, application.getLastRenderedPage().getClass());
	}

	/**
	 * 
	 * @author
	 */
	private static class MyMockWebApplication extends MockApplication
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Class<? extends Page> getHomePage()
		{
			return MockHomePage.class;
		}

		@Override
		protected void init()
		{
			super.init();
			getSecuritySettings().setAuthorizationStrategy(new MyAuthorizationStrategy());
		}

		/**
		 * 
		 * @return Class
		 */
		public Class<? extends Page> getLoginPage()
		{
			return MockLoginPage.class;
		}

		/**
		 * 
		 * @see org.apache.wicket.ISessionFactory#newSession(Request, Response)
		 */
		@Override
		public Session newSession(Request request, Response response)
		{
			return new MySession(request);
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
		protected MySession(Request request)
		{
			super(request);
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
			if (MockHomePage.class.equals(componentClass) &&
				!((MySession)Session.get()).isLoggedIn())
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
