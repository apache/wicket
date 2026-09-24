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
package org.apache.wicket.authroles.authorization.strategies.role.metadata;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;

import org.apache.wicket.Component;
import org.apache.wicket.MarkupContainer;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A link reserved for a role with
 * {@link MetaDataRoleAuthorizationStrategy#authorize(Component, org.apache.wicket.authorization.Action, String)}
 * must not be invoked for a user who does not hold that role, not even when that user asks for a
 * listener URL that leaves out the page identifier and so reaches a page instance Wicket creates
 * for that very request.
 */
class MetaDataRoleFreshPageListenerTest
{
	private WicketTester tester;

	@BeforeEach
	void setUp()
	{
		tester = new WicketTester();
		TestPage.adminOnlyClicked = false;
		TestPage.ordinaryClicked = false;
	}

	@AfterEach
	void tearDown()
	{
		tester.destroy();
	}

	private void signInAs(String role)
	{
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new RoleAuthorizationStrategy(new UserRolesAuthorizer(role)));
	}

	/**
	 * The listener URL of a component with the page identifier left out.
	 */
	private void requestWithoutPageId(String componentPath)
	{
		tester.getRequest()
			.setUrl(Url.parse(
				"wicket/bookmarkable/" + TestPage.class.getName() + "?-1.-" + componentPath));
		tester.processRequest();
	}

	@Test
	void adminMayInvokeTheAdminLink()
	{
		signInAs("ADMIN");
		tester.startPage(TestPage.class);
		tester.assertRenderedPage(TestPage.class);
		assertTrue(tester.getLastResponseAsString().contains("wicket:id=\"adminOnly\""));

		tester.clickLink("adminOnly");

		assertTrue(TestPage.adminOnlyClicked);
	}

	@Test
	void userMayNotInvokeTheAdminLinkOnAFreshPage()
	{
		signInAs("USER");
		tester.startPage(TestPage.class);
		tester.assertRenderedPage(TestPage.class);

		// the link is reserved for ADMIN, so this user never received it
		assertFalse(tester.getLastResponseAsString().contains("wicket:id=\"adminOnly\""));

		assertThrows(ListenerInvocationNotAllowedException.class,
			() -> requestWithoutPageId("adminOnly"));

		assertFalse(TestPage.adminOnlyClicked,
			"Link#onClick() was invoked although RENDER is reserved for the ADMIN role");
	}

	/** Control: reserving one link leaves the rest of the page alone. */
	@Test
	void userMayStillInvokeTheOrdinaryLink()
	{
		signInAs("USER");
		tester.startPage(TestPage.class);

		tester.clickLink("ordinary");

		assertTrue(TestPage.ordinaryClicked);
		assertFalse(TestPage.adminOnlyClicked);
	}

	/** A page everyone may open, carrying one link reserved for ADMIN. */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		// a fresh page instance is created per request, so the invocations are recorded statically
		static boolean adminOnlyClicked;
		static boolean ordinaryClicked;

		public TestPage()
		{
			Link<Void> adminOnly = new Link<Void>("adminOnly")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					adminOnlyClicked = true;
				}
			};
			add(adminOnly);
			MetaDataRoleAuthorizationStrategy.authorize(adminOnly, Component.RENDER, "ADMIN");

			add(new Link<Void>("ordinary")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onClick()
				{
					ordinaryClicked = true;
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<a wicket:id=\"adminOnly\">adminOnly</a>" //
				+ "<a wicket:id=\"ordinary\">ordinary</a>" //
				+ "</body></html>");
		}
	}

	private static final class UserRolesAuthorizer implements IRoleCheckingStrategy, Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Roles roles;

		UserRolesAuthorizer(String roles)
		{
			this.roles = new Roles(roles);
		}

		@Override
		public boolean hasAnyRole(Roles roles)
		{
			return this.roles.hasAnyRole(roles);
		}
	}
}
