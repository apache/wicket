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
package org.apache.wicket.examples.signin2;

import java.util.Collection;

import javax.servlet.http.Cookie;

import org.apache.wicket.examples.authentication2.Home;
import org.apache.wicket.examples.authentication2.SignIn2;
import org.apache.wicket.examples.authentication2.SignIn2Application;
import org.apache.wicket.examples.authentication2.SignOut;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Test;


/**
 * jWebUnit test for Hello World.
 */
public class Signin2Test extends Assert
{
	/**
	 * Test page.
	 */
	@Test
	public void testSignIn2()
	{
		WicketTester tester = new WicketTester(new SignIn2Application());
		try
		{
			tester.startPage(Home.class);

			tester.assertRenderedPage(SignIn2.class);

			FormTester formTester = tester.newFormTester("signInPanel:signInForm");
			formTester.setValue("username", "wicket");
			formTester.setValue("password", "wicket");
			formTester.setValue("rememberMeContainer:rememberMe", "true");
			formTester.submit();
			tester.assertRenderedPage(Home.class);

			// a) With wicket submitting a form will result in a temporary redirect,
			// with the redirect setting the Cookie.
			// b) jWebUnits Cookie test methods are all using the http response
			// object only
			// c) Like a browser, jwebunit will automatically handle the redirect
			// request
			// Hence dumpCookie will not print an Cookie and assertCookiePresent
			// will
			// fail.
			// The only mean available is to indirectly test the cookies. Indirectly
			// because
			// the screen flow depends on the cookies.
			// this.dumpCookies(System.err);
			// this.assertCookiePresent("signInPanel.signInForm.username");
			// this.assertCookiePresent("signInPanel.signInForm.password");

			Collection<Cookie> cookies = tester.getLastResponse().getCookies();
			for (Cookie cookie : cookies)
			{
				if ("signInPanel.signInForm.username".equals(cookie.getName()))
				{
					assertEquals("wicket", cookie.getValue());
				}
			}

			tester.startPage(SignOut.class);
			tester.assertRenderedPage(SignOut.class);

			tester.startPage(Home.class);
			tester.assertRenderedPage(SignIn2.class);
		}
		finally
		{
			tester.destroy();
		}
	}
}
