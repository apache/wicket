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
package org.apache.wicket.authroles.authorization.strategies.role.annotations;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the annotations package of the auth-roles project.
 * 
 * @author Eelco Hillenius
 */
class AnnotationsRoleTest
{
	WicketTester tester;

	@BeforeEach
    void setUp() {
		tester = new WicketTester();
	}

	@AfterEach
    void tearDown() {
		tester.destroy();
	}

	/**
     */
	@Test
    void testClear() {
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(new RoleAuthorizationStrategy(new UserRolesAuthorizer("FOO")));
		tester.startPage(NormalPage.class);
		tester.assertRenderedPage(NormalPage.class);
	}

	/**
     */
	@Test
    void testAuthorized() {
		WicketTester tester = new WicketTester();
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(
				new RoleAuthorizationStrategy(new UserRolesAuthorizer("ADMIN")));
		tester.startPage(AdminPage.class);
		tester.assertRenderedPage(AdminPage.class);
	}

	/**
     */
	@Test
    void testNotAuthorized() {
		WicketTester tester = new WicketTester();
		tester.getApplication()
			.getSecuritySettings()
			.setAuthorizationStrategy(
				new RoleAuthorizationStrategy(new UserRolesAuthorizer("USER")));
		final class Listener implements IUnauthorizedComponentInstantiationListener
		{
			private boolean eventReceived = false;

			@Override
			public void onUnauthorizedInstantiation(Component component)
			{
				eventReceived = true;
			}
		}
		Listener listener = new Listener();
		tester.getApplication()
			.getSecuritySettings()
			.setUnauthorizedComponentInstantiationListener(listener);

		try
		{
			tester.startPage(AdminPage.class);
			assertTrue(listener.eventReceived, "an authorization exception event should have been received");
		}
		catch (Exception e)
		{
			if (!(e.getCause() instanceof InvocationTargetException && ((InvocationTargetException)e.getCause()).getTargetException() instanceof UnauthorizedInstantiationException))
			{
				throw e;
			}
		}
	}

	/**
	 * Authorizer class that uses the TS user and it's defined string[] roles.
	 */
	private static final class UserRolesAuthorizer implements IRoleCheckingStrategy, Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Roles roles;

		/**
		 * Construct.
		 */
        UserRolesAuthorizer(String roles)
		{
			this.roles = new Roles(roles);
		}

		/**
		 * @see IRoleCheckingStrategy#hasAnyRole(Roles)
		 */
		@Override
		public boolean hasAnyRole(Roles roles)
		{
			return this.roles.hasAnyRole(roles);
		}
	}
}
