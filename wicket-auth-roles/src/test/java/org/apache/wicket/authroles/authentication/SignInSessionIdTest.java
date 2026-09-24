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
package org.apache.wicket.authroles.authentication;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authentication.pages.UsernamePasswordPage;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.mock.MockHttpSession;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.tester.DummyHomePage;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests that {@link AuthenticatedWebSession#signIn(String, String)} renews the session id, so that
 * an id that was known before the authentication cannot be used afterwards.
 */
public class SignInSessionIdTest
{
	private static final String PASSWORD = "secret";

	private WicketTester tester;

	private TestSession session;

	/**
	 * Gives the session a container session of its own, so that it is not temporary and
	 * {@link org.apache.wicket.Session#changeSessionId()} is not a no-op.
	 */
	@Before
	public void bindTheSession()
	{
		tester = new WicketTester(new TestApplication());

		MockHttpSession httpSession = (MockHttpSession)tester.getRequest().getSession();
		httpSession.setTemporary(false);

		session = (TestSession)tester.getSession();
		session.bind();
	}

	@After
	public void destroyTester()
	{
		tester.destroy();
	}

	/**
	 * The first authentication of a session renews its id.
	 */
	@Test
	public void firstSignInRenewsTheSessionId()
	{
		String anonymousId = session.getId();
		assertNotNull("the session has to be bound for its id to mean anything", anonymousId);

		assertTrue(session.signIn("user", PASSWORD));

		assertEquals("user", session.getUser());
		assertNotEquals("the first authentication has to renew the session id", anonymousId,
			session.getId());
	}

	/**
	 * So does every authentication after it. A session that is already signed in keeps its id
	 * otherwise, and whoever knew that id keeps a usable one for the session the second
	 * authentication signed in.
	 */
	@Test
	public void everyFurtherSignInRenewsTheSessionIdAgain()
	{
		assertTrue(session.signIn("first", PASSWORD));
		String firstId = session.getId();

		assertTrue(session.signIn("second", PASSWORD));

		assertEquals("second", session.getUser());
		assertTrue(session.isSignedIn());
		assertNotEquals("authenticating again has to renew the session id as well", firstId,
			session.getId());
	}

	/**
	 * An authentication that fails still signs the session out, whatever it was before.
	 */
	@Test
	public void failedSignInSignsTheSessionOut()
	{
		assertTrue(session.signIn("user", PASSWORD));

		assertFalse(session.signIn("user", "wrong"));

		assertFalse(session.isSignedIn());
	}

	/**
	 * A session that remembers who it authenticated last.
	 */
	public static class TestSession extends AuthenticatedWebSession
	{
		private static final long serialVersionUID = 1L;

		private String user;

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
		protected boolean authenticate(String username, String password)
		{
			if (PASSWORD.equals(password))
			{
				user = username;
				return true;
			}
			return false;
		}

		@Override
		public Roles getRoles()
		{
			return isSignedIn() ? new Roles("USER") : new Roles();
		}

		String getUser()
		{
			return user;
		}
	}

	private static class TestApplication extends AuthenticatedWebApplication
	{
		@Override
		protected Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass()
		{
			return TestSession.class;
		}

		@Override
		protected Class<? extends WebPage> getSignInPageClass()
		{
			return UsernamePasswordPage.class;
		}

		@Override
		public Class<? extends Page> getHomePage()
		{
			return DummyHomePage.class;
		}
	}
}
