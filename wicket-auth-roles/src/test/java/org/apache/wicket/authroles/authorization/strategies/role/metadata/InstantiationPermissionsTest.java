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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.apache.wicket.Page;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test case for {@link InstantiationPermissions}.
 * 
 * @author Eelco Hillenius
 */
class InstantiationPermissionsTest
{
	private WicketTester tester;

	@BeforeEach
	void setUp() throws Exception
	{
		tester = new WicketTester();
	}

	@AfterEach
	void tearDown() throws Exception
	{
		tester.destroy();
	}

	/**
	 * Test adding roles.
	 * 
	 * @throws Exception
	 */
	@Test
	void testAdd1() throws Exception
	{
		InstantiationPermissions permissions = new InstantiationPermissions();
		permissions.authorize(Page.class, new Roles("jonathan"));
		permissions.authorize(Page.class, new Roles("johan"));
		permissions.authorize(Page.class, new Roles("maurice"));
		permissions.authorize(Page.class, new Roles("eelco"));
		assertEquals(4, permissions.getRolesForComponentClass().get(Page.class).size());
		permissions.unauthorize(Page.class, new Roles("maurice"));
		assertEquals(3, permissions.getRolesForComponentClass().get(Page.class).size());
		permissions.authorizeAll(Page.class);
		assertNull(permissions.getRolesForComponentClass().get(Page.class));
	}

	/**
	 * Test removing roles.
	 * 
	 * @throws Exception
	 */
	@Test
	void testRemove1() throws Exception
	{
		InstantiationPermissions permissions = new InstantiationPermissions();
		assertNull(permissions.getRolesForComponentClass().get(Page.class));
		permissions.unauthorize(Page.class, new Roles("eelco"));
		assertEquals(new Roles(MetaDataRoleAuthorizationStrategy.NO_ROLE),
			permissions.getRolesForComponentClass().get(Page.class));
	}

	/**
	 * Test for issue <a href="http://issues.apache.org/jira/browse/WICKET-1152">WICKET-1152</a>.
	 * 
	 */
	@Test
	void testRemove2()
	{
		MetaDataRoleAuthorizationStrategy strategy = new MetaDataRoleAuthorizationStrategy(
			new IRoleCheckingStrategy()
			{
				@Override
				public boolean hasAnyRole(Roles roles)
				{
					return false;
				}
			});
		tester.getApplication().setMetaData(
			MetaDataRoleAuthorizationStrategy.INSTANTIATION_PERMISSIONS,
			new InstantiationPermissions());
		MetaDataRoleAuthorizationStrategy.unauthorize(Page.class, "martijn");
		assertFalse(strategy.isInstantiationAuthorized(Page.class));
	}

	/**
	 * Test consistency in behavior between authorizing a role for a class and then unauthorizing it
	 * with {@link #testRemove2()}.
	 */
	@Test
	void testRemove3()
	{
		MetaDataRoleAuthorizationStrategy strategy = new MetaDataRoleAuthorizationStrategy(
			new IRoleCheckingStrategy()
			{
				@Override
				public boolean hasAnyRole(Roles roles)
				{
					return false;
				}
			});
		tester.getApplication().setMetaData(
			MetaDataRoleAuthorizationStrategy.INSTANTIATION_PERMISSIONS,
			new InstantiationPermissions());
		MetaDataRoleAuthorizationStrategy.authorize(Page.class, "martijn");
		MetaDataRoleAuthorizationStrategy.unauthorize(Page.class, "martijn");
		assertFalse(strategy.isInstantiationAuthorized(Page.class));
	}
}
