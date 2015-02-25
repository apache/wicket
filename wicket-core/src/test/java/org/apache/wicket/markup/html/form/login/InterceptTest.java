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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;


/**
 * @author marrink
 * 
 */
public class InterceptTest extends WicketTestCase
{
	@Override
	protected WebApplication newApplication()
	{
		return new MyMockWebApplication();
	}

	/**
	 * 
	 */
	@Test
	public void formSubmit()
	{
		// same as above but uses different technique to login
		tester.startPage(tester.getApplication().getHomePage());
		MockLoginPage loginPage = (MockLoginPage)tester.getLastRenderedPage();
		assertEquals(((MyMockWebApplication)tester.getApplication()).getLoginPage(),
			loginPage.getClass());
		FormTester form = tester.newFormTester("form");
		form.setValue("username", "admin");
		form.submit();
		assertEquals(tester.getApplication().getHomePage(), tester.getLastRenderedPage().getClass());
	}

	/**
	 * 
	 */
	@Test
	public void clickLink()
	{
		tester.startPage(tester.getApplication().getHomePage());
		MockLoginPage loginPage = (MockLoginPage)tester.getLastRenderedPage();
		assertEquals(((MyMockWebApplication)tester.getApplication()).getLoginPage(),
			loginPage.getClass());

		FormTester form = tester.newFormTester("form");
		form.setValue("username", "admin");
		form.submit();

		assertEquals(tester.getApplication().getHomePage(), tester.getLastRenderedPage().getClass());

		tester.clickLink(tester.getLastRenderedPage().get("link"));
		assertEquals(PageA.class, tester.getLastRenderedPage().getClass());
	}

	/**
	 * 
	 */
	@Test
	public void clickLink2()
	{
		// same as above but uses different technique to login
		tester.startPage(tester.getApplication().getHomePage());
		MockLoginPage loginPage = (MockLoginPage)tester.getLastRenderedPage();
		assertEquals(((MyMockWebApplication)tester.getApplication()).getLoginPage(),
			loginPage.getClass());

		// bypass formTester completely to login but continue to intercept page
		FormTester formTester = tester.newFormTester("form");
		formTester.setValue("username", "admin");
		formTester.submit();
		tester.startPage(tester.getApplication().getHomePage());

		assertEquals(tester.getApplication().getHomePage(), tester.getLastRenderedPage().getClass());

		tester.clickLink(tester.getLastRenderedPage().get("link"));
		assertEquals(PageA.class, tester.getLastRenderedPage().getClass());
	}

	/**
	 * 
	 * @author
	 */
	private static class MyMockWebApplication extends MockApplication
	{
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
		 * @param tester
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
		@Override
		public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
			Class<T> componentClass)
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
		@Override
		public boolean isActionAuthorized(Component component, Action action)
		{
			return true;
		}
	}
}
