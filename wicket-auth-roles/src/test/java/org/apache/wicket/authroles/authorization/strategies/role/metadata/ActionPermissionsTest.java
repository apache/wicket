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

import org.apache.wicket.authorization.Action;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.util.tester.WicketTester;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test case for {@link ActionPermissions}.
 * 
 * @author Eelco Hillenius
 */
public class ActionPermissionsTest extends Assert
{
	private WicketTester tester;

	@Before
	public void setUp() throws Exception
	{
		tester = new WicketTester();
	}

	@After
	public void tearDown() throws Exception
	{
		tester.destroy();
	}

	/**
	 * Test adding roles.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testAdd1() throws Exception
	{
		ActionPermissions permissions = new ActionPermissions();
		Action mambo = new Action("mambo");
		permissions.authorize(mambo, new Roles("jonathan"));
		permissions.authorize(mambo, new Roles("johan"));
		permissions.authorize(mambo, new Roles("maurice"));
		permissions.authorize(mambo, new Roles("eelco"));
		assertEquals(4, permissions.rolesFor(mambo).size());
		permissions.unauthorize(mambo, new Roles("maurice"));
		assertEquals(3, permissions.rolesFor(mambo).size());
		permissions.authorizeAll(mambo);
		assertEquals(null, permissions.rolesFor(mambo));
	}

	/**
	 * Test removing roles.
	 * 
	 * @throws Exception
	 */
	@Test
	public void testRemove1() throws Exception
	{
		ActionPermissions permissions = new ActionPermissions();
		Action mambo = new Action("mambo");
		assertEquals(null, permissions.rolesFor(mambo));
		permissions.unauthorize(mambo, new Roles("maurice"));
		assertEquals(new Roles(MetaDataRoleAuthorizationStrategy.NO_ROLE),
			permissions.rolesFor(mambo));
	}

	/**
	 * Test for issue <a href="http://issues.apache.org/jira/browse/WICKET-1152">WICKET-1152</a>.
	 */
	@Test
	public void testRemove2()
	{
		Label label = new Label("label", "text");
		Action mambo = new Action("mambo");
		MetaDataRoleAuthorizationStrategy strategy = new MetaDataRoleAuthorizationStrategy(
			new IRoleCheckingStrategy()
			{
				@Override
				public boolean hasAnyRole(Roles roles)
				{
					return false;
				}
			});
		label.setMetaData(MetaDataRoleAuthorizationStrategy.ACTION_PERMISSIONS,
			new ActionPermissions());
		MetaDataRoleAuthorizationStrategy.unauthorize(label, mambo, "johan");
		assertFalse(strategy.isActionAuthorized(label, mambo));
	}

	/**
	 * Test consistency in behavior between authorizing a role for an action and then unauthorizing
	 * it with {@link #testRemove2()}.
	 */
	@Test
	public void testRemove3()
	{
		Label label = new Label("label", "text");
		Action mambo = new Action("mambo");
		MetaDataRoleAuthorizationStrategy strategy = new MetaDataRoleAuthorizationStrategy(
			new IRoleCheckingStrategy()
			{
				@Override
				public boolean hasAnyRole(Roles roles)
				{
					return false;
				}
			});
		label.setMetaData(MetaDataRoleAuthorizationStrategy.ACTION_PERMISSIONS,
			new ActionPermissions());
		MetaDataRoleAuthorizationStrategy.authorize(label, mambo, "johan");
		MetaDataRoleAuthorizationStrategy.unauthorize(label, mambo, "johan");
		assertFalse(strategy.isActionAuthorized(label, mambo));
	}
}
