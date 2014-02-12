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
package org.apache.wicket.stateless;

import org.apache.wicket.Page;
import org.apache.wicket.WicketTestCase;
import org.apache.wicket.mock.MockApplication;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.stateless.pages.HomePage;
import org.apache.wicket.stateless.pages.LoginPage;
import org.apache.wicket.util.tester.FormTester;
import org.junit.Test;

/**
 * 
 * @author marrink
 */
public class StatelessFormTest extends WicketTestCase
{
	private final Class<? extends Page> HOME = HomePage.class;
	private final Class<? extends Page> LOGIN = LoginPage.class;

	@Override
	protected WebApplication newApplication()
	{
		return new MockApplication()
		{
			@Override
			public Class<? extends Page> getHomePage()
			{
				return HOME;
			}
		};
	}

	/**
	 * Login through the login page.
	 */
	@Test
	public void login()
	{
		tester.startPage(LOGIN);
		tester.assertRenderedPage(LOGIN);
		FormTester form = tester.newFormTester("signInPanel:signInForm");
		form.setValue("username", "test");
		form.setValue("password", "test");
		form.submit();
		tester.assertRenderedPage(HOME);
	}

	/**
	 * test initialization of component on stateless components
	 */
	@Test
	public void onInitializationForStatelessComponents()
	{
		LoginPage page = new LoginPage();
		assertFalse(page.isPageInitialized());
		assertFalse(page.isPanelInitialized());

		tester.startPage(LOGIN);
		tester.assertRenderedPage(LOGIN);
		page = (LoginPage)tester.getLastRenderedPage();
		assertTrue(page.isPageInitialized());
		assertTrue(page.isPanelInitialized());

		FormTester form = tester.newFormTester("signInPanel:signInForm");
		form.setValue("username", "test");
		form.setValue("password", "invalid");
		form.submit();

		tester.assertRenderedPage(LOGIN);
		page = (LoginPage)tester.getLastRenderedPage();
		assertTrue(page.isPageInitialized());
		assertTrue(page.isPanelInitialized());
	}

	/**
	 * Tests if a stateless form renders an action url containing the pageparameters
	 * 
	 * @throws Exception
	 */
	@Test
	public void pageWithParameters() throws Exception
	{
		PageParameters parameters = new PageParameters();
		parameters.add("first", "foo");
		parameters.add("second", "bar");
		executeTest(StatelessPageWithForm.class, parameters, "StatelessPageWithForm_expected.html");
	}

	/**
	 * Tests that the session remains temporary (i.e. not bound) after submitting
	 * stateless form
	 */
	@Test
	public void submitStatelessFormDoesntBindSession()
	{
		StatelessPageWithForm.FORM_SUBMITTED.set(false);
		try
		{
			tester.startPage(StatelessPageWithForm.class);
			tester.assertRenderedPage(StatelessPageWithForm.class);
			FormTester form = tester.newFormTester("form");
			assertTrue(tester.getSession().isTemporary());
			form.submit();
			tester.assertRenderedPage(StatelessPageWithForm.class);
			assertTrue(StatelessPageWithForm.FORM_SUBMITTED.get());
			assertTrue(tester.getSession().isTemporary());
		}
		finally
		{
			StatelessPageWithForm.FORM_SUBMITTED.set(false);
		}
	}
}
