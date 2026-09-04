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
package org.apache.wicket.authroles.authentication.panel;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import jakarta.servlet.http.Cookie;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.FormTester;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Tests for {@link UsernamePasswordPanel}
 */
public class UsernamePasswordPanelTest
{
	private static final String USERNAME = "user";

	private static final String PASSWORD = "secret";

	/**
	 * https://issues.apache.org/jira/browse/WICKET-3980
	 */
	@Test
	public void instantiateJustOnce()
	{
		final AtomicInteger constructorsCalls = new AtomicInteger(0);

		WicketTester tester = new WicketTester(new TestApplication());

		assertEquals(0, constructorsCalls.get());

		tester.startPage(new TestPage(constructorsCalls));

		assertEquals(1, constructorsCalls.get());

		tester.assertRenderedPage(TestPage.class);

		assertEquals(1, constructorsCalls.get());
	}

	/**
	 * The panel ships localized markup; every variant has to resolve and render, which is what
	 * catches a missing component or a stray {@code wicket:id} in one of them.
	 * 
	 * @param languageTag
	 *            the locale to render in
	 */
	@ParameterizedTest
	@ValueSource(strings = { "de", "fr", "hu", "ja", "ko", "nl", "pl", "ru", "zh-CN" })
	public void rendersInEveryLocale(String languageTag)
	{
		WicketTester tester = new WicketTester(new TestApplication());
		tester.getSession().setLocale(Locale.forLanguageTag(languageTag));

		tester.startPage(new TestPage(new AtomicInteger(0)));

		tester.assertRenderedPage(TestPage.class);
		tester.assertComponent("signInPanel:signInForm:username",
			org.apache.wicket.markup.html.form.TextField.class);
	}

	/**
	 * This panel keeps nothing on the client: a successful sign in must leave no cookie behind, and
	 * in particular not the one the removed authentication strategy used.
	 */
	@Test
	public void signInPersistsNothingOnTheClient()
	{
		WicketTester tester = new WicketTester(new TestApplication());
		tester.startPage(new TestPage(new AtomicInteger(0)));

		FormTester form = tester.newFormTester("signInPanel:signInForm");
		form.setValue("username", USERNAME);
		form.setValue("password", PASSWORD);
		form.submit();

		assertTrue(AuthenticatedWebSession.get().isSignedIn(), "should be signed in");
		assertTrue(tester.getLastResponse().getCookies().stream().map(Cookie::getName)
			.noneMatch("LoggedIn"::equals), "no credentials may be persisted on the client");
	}

	/**
	 * The page hosting the panel. It doubles as the home page, so it needs a default constructor for
	 * the redirect after a successful sign in.
	 */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		/**
		 * Construct.
		 */
		public TestPage()
		{
			this(new AtomicInteger(0));
		}

		private TestPage(AtomicInteger constructorCalls)
		{
			super();
			constructorCalls.incrementAndGet();

			add(new UsernamePasswordPanel("signInPanel"));
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream(
				"<html><body><div wicket:id='signInPanel'></div></body></html>");
		}
	}

	/**
	 * A {@link org.apache.wicket.Session session} for the test
	 */
	public static class TestSession extends AuthenticatedWebSession
	{
		/**
		 * Construct.
		 * 
		 * @param request
		 *            the current web request
		 */
		public TestSession(Request request)
		{
			super(request);
		}

		@Override
		public Roles getRoles()
		{
			return null;
		}

		@Override
		public boolean authenticate(String username, String password)
		{
			return USERNAME.equals(username) && PASSWORD.equals(password);
		}
	}

	private static class TestApplication extends AuthenticatedWebApplication
	{

		@Override
		public Class<TestPage> getHomePage()
		{
			return TestPage.class;
		}

		@Override
		protected Class<? extends AuthenticatedWebSession> getWebSessionClass()
		{
			return TestSession.class;
		}

		@Override
		protected Class<? extends WebPage> getSignInPageClass()
		{
			return TestPage.class;
		}
	}
}
