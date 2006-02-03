/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.authorization.roles.annot;

import java.io.Serializable;

import junit.framework.TestCase;
import wicket.Page;
import wicket.authorization.roles.IRolesAuthorizer;
import wicket.authorization.roles.RolesAuthorizationStrategy;
import wicket.authorization.roles.util.Roles;
import wicket.util.tester.ITestPageSource;
import wicket.util.tester.WicketTester;

/**
 * Test the annotations package of the auth-roles project.
 * 
 * @author Eelco Hillenius
 */
public class AnnotRolesTest extends TestCase
{
	/**
	 * @throws Exception
	 */
	public void testAuthorized() throws Exception
	{
		WicketTester tester = new WicketTester();
		tester.getSecuritySettings().setAuthorizationStrategy(
				new RolesAuthorizationStrategy(new UserRolesAuthorizer("ADMIN")));
		tester.startPage(new ITestPageSource()
		{
			public Page getTestPage()
			{
				return new AdminPage();
			}
		});
		tester.assertRenderedPage(AdminPage.class);
	}

	/**
	 * @throws Exception
	 */
	public void testNotAuthorized() throws Exception
	{
		WicketTester tester = new WicketTester();
		tester.getSecuritySettings().setAuthorizationStrategy(
				new RolesAuthorizationStrategy(new UserRolesAuthorizer("USER")));
		try
		{
			tester.startPage(new ITestPageSource()
			{
				public Page getTestPage()
				{
					return new AdminPage();
				}
			});
			fail("an authorization exception should have been thrown");
		}
		catch (Exception e)
		{
		}
	}

	/**
	 * Authorizer class that uses the TS user and it's defined string[] roles.
	 */
	private static final class UserRolesAuthorizer implements IRolesAuthorizer, Serializable
	{
		private static final long serialVersionUID = 1L;

		private final Roles roles;

		/**
		 * Construct.
		 * 
		 * @param roles
		 */
		public UserRolesAuthorizer(String roles)
		{
			this.roles = new Roles(roles);
		}

		/**
		 * @see wicket.authorization.roles.IRolesAuthorizer#any(java.lang.String[])
		 */
		public boolean any(String[] roles)
		{
			return this.roles.hasAnyRole(roles);
		}
	}
}
