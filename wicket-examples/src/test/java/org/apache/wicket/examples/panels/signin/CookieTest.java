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
package org.apache.wicket.examples.panels.signin;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import java.io.IOException;
import java.util.Collection;

import junit.framework.Assert;
import junit.framework.TestCase;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.protocol.http.WebRequestCycle;
import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.util.crypt.ICrypt;
import org.apache.wicket.util.crypt.NoCryptFactory;
import org.apache.wicket.util.tester.WicketTester;


/**
 * Test cases for Cookie handling
 * 
 * @author Juergen Donnerstag
 */
public class CookieTest extends TestCase
{
	private WicketTester tester;
	private SignInPanel panel;
	private Form<?> form;
	private Cookie cookieUsername;
	private WebPage page;

	/**
	 * Create the test case.
	 * 
	 * @param name
	 *            The test name
	 */
	public CookieTest(String name)
	{
		super(name);
	}

	@Override
	protected void setUp() throws Exception
	{
		super.setUp();

		tester = new WicketTester(MockPage.class);
		tester.setupRequestAndResponse();

		final ISecuritySettings settings = tester.getApplication().getSecuritySettings();
		settings.setCryptFactory(new NoCryptFactory());

		panel = new SignInPanel("panel")
		{
			@Override
			public boolean signIn(final String username, final String password)
			{
				return true;
			}
		};

		panel.setPersistent(true);
		form = (Form<?>)panel.get("signInForm");

		final ICrypt crypt = tester.getApplication().getSecuritySettings().getCryptFactory()
				.newCrypt();
		final String encryptedPassword = crypt.encryptUrlSafe("test");
		assertNotNull(encryptedPassword);
		cookieUsername = new Cookie("panel:signInForm:username", "juergen");
		Cookie cookiePassword = new Cookie("panel:signInForm:password", encryptedPassword);
		Cookie[] cookies = new Cookie[]{cookieUsername, cookiePassword};

		tester.getServletRequest().setCookies(cookies);

		new WebRequestCycle(tester.getApplication(), tester.getWicketRequest(), tester.getWicketResponse());

		page = new MockPage(null);
		page.add(panel);

		new WebRequestCycle(tester.getApplication(), tester.getWicketRequest(), tester.getWicketResponse());
	}

	public void testSetCookieOnForm() throws IOException, ServletException
	{
		// initialize
		form.loadPersistentFormComponentValues();

		// validate
		FormComponent<?> username = (FormComponent<?>)panel.get("signInForm:username");

		Assert.assertNotNull(username);

		Assert.assertNotNull(cookieUsername);

		Assert.assertEquals(cookieUsername.getValue(), username.getDefaultModelObjectAsString());
	}

	public void testPersistCookieWithPersistenceDisabled() throws IOException, ServletException
	{
		// test will call persistFromComponentData(), which is private
		panel.setPersistent(false);
		form.onFormSubmitted();

		// validate
		Collection<Cookie> cookies = tester.getServletResponse().getCookies();
		for (Cookie cooky : cookies)
		{
			Assert.assertEquals(0, (cooky).getMaxAge());
		}
	}

	/**
	 * 
	 * @throws IOException
	 * @throws ServletException
	 */
	public void testPersistCookie() throws IOException, ServletException
	{
		panel.setPersistent(true);

		// test will call persistFromComponentData(), which is private
		form.onFormSubmitted();

		// validate
		Collection<Cookie> cookies = tester.getServletResponse().getCookies();
		Assert.assertEquals(2, cookies.size());
		for (Cookie cooky : cookies)
		{
			Assert.assertNotNull(page.get(cooky.getName()));
			// Skip "deleted" cookies
			if (page.get(cooky.getName()).getDefaultModelObjectAsString() != "")
			{
				Assert.assertEquals(cooky.getValue(), page.get(cooky.getName())
						.getDefaultModelObjectAsString());
			}
		}
	}

	public void testRemoveFromPage() throws IOException, ServletException
	{
		panel.setPersistent(true);

		// test
		page.removePersistedFormData(SignInPanel.SignInForm.class, true);

		// validate
		Collection<Cookie> cookieCollection = tester.getServletResponse().getCookies();
		// Cookies are remove by setting maxAge == 0
		Assert.assertEquals(2, cookieCollection.size());

		// initialize
		final Cookie cookieUsername = new Cookie("panel:signInForm:username", "juergen");
		final Cookie cookiePassword = new Cookie("panel:signInForm:password", "test");
		final Cookie[] cookies = new Cookie[] { cookieUsername, cookiePassword };

		tester.getServletRequest().setCookies(cookies);

		// test
		page.removePersistedFormData(SignInPanel.SignInForm.class, true);

		// validate
		cookieCollection = tester.getServletResponse().getCookies();
		Assert.assertEquals(4, cookieCollection.size());
		for (Cookie cookie : cookieCollection)
		{
			Assert.assertNotNull(page.get(cookie.getName()));
			Assert.assertEquals(cookie.getMaxAge(), 0);
		}
	}
}