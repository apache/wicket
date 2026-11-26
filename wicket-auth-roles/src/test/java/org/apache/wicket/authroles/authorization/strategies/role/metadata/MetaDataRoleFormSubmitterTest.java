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
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.IMarkupResourceStreamProvider;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.markup.html.form.Button;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.StringResourceStream;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * A submit button reserved for a role with
 * {@link MetaDataRoleAuthorizationStrategy#authorize(Component, org.apache.wicket.authorization.Action, String)}
 * must not be invoked for a user who does not hold that role, even when that user names the button
 * in the form submission themselves.
 */
class MetaDataRoleFormSubmitterTest
{
	private WicketTester tester;

	@BeforeEach
	void setUp()
	{
		tester = new WicketTester();
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

	@Test
	void adminMayInvokeTheAdminButton()
	{
		signInAs("ADMIN");
		TestPage page = new TestPage();
		tester.startPage(page);

		tester.newFormTester("form").submit("adminOnly");

		assertTrue(page.adminOnlySubmitted);
	}

	@Test
	void userMayNotInvokeTheAdminButton()
	{
		signInAs("USER");
		TestPage page = new TestPage();
		tester.startPage(page);
		tester.assertRenderedPage(TestPage.class);

		// the button renders disabled, so the client has its input name
		assertTrue(tester.getLastResponseAsString().contains("disabled=\"disabled\""));

		tester.getRequest()
			.getPostParameters()
			.setParameterValue(page.adminOnly.getInputName(), "x");
		WicketRuntimeException e = assertThrows(WicketRuntimeException.class,
			() -> tester.submitForm(page.form));

		assertTrue(e.getMessage().contains("is not enabled"), e.getMessage());
		assertFalse(page.adminOnlySubmitted);
		assertFalse(page.formSubmitted);
	}

	@Test
	void userMayStillInvokeTheOrdinaryButton()
	{
		signInAs("USER");
		TestPage page = new TestPage();
		tester.startPage(page);

		tester.newFormTester("form").submit("ordinary");

		assertTrue(page.ordinarySubmitted);
		assertTrue(page.formSubmitted);
		assertFalse(page.adminOnlySubmitted);
	}

	/** A form everyone may submit, carrying one button reserved for ADMIN. */
	public static class TestPage extends WebPage implements IMarkupResourceStreamProvider
	{
		private static final long serialVersionUID = 1L;

		Form<Void> form;
		Button adminOnly;
		Button ordinary;

		boolean formSubmitted;
		boolean adminOnlySubmitted;
		boolean ordinarySubmitted;

		TestPage()
		{
			add(form = new Form<Void>("form")
			{
				private static final long serialVersionUID = 1L;

				@Override
				protected void onSubmit()
				{
					formSubmitted = true;
				}
			});
			form.add(adminOnly = new Button("adminOnly")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					adminOnlySubmitted = true;
				}
			});
			MetaDataRoleAuthorizationStrategy.authorize(adminOnly, Component.ENABLE, "ADMIN");

			form.add(ordinary = new Button("ordinary")
			{
				private static final long serialVersionUID = 1L;

				@Override
				public void onSubmit()
				{
					ordinarySubmitted = true;
				}
			});
		}

		@Override
		public IResourceStream getMarkupResourceStream(MarkupContainer container,
			Class<?> containerClass)
		{
			return new StringResourceStream("<html><body>" //
				+ "<form wicket:id=\"form\">" //
				+ "<button wicket:id=\"adminOnly\"></button>" //
				+ "<button wicket:id=\"ordinary\"></button>" //
				+ "</form></body></html>");
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
