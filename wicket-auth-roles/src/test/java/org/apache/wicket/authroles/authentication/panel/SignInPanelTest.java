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

import java.util.concurrent.atomic.AtomicInteger;

import org.apache.wicket.MarkupContainer;
import org.apache.wicket.Session;
import org.apache.wicket.authroles.authentication.AuthenticatedWebApplication;
import org.apache.wicket.authroles.authentication.AuthenticatedWebSession;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.request.Request;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for {@link SignInPanel}
 */
public class SignInPanelTest extends Assert
{

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

	private static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private TestPage(AtomicInteger constructorCalls)
		{
			super();
			constructorCalls.incrementAndGet();

			add(new SignInPanel("signInPanel"));
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
	 * A {@link Session session} for the test
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
