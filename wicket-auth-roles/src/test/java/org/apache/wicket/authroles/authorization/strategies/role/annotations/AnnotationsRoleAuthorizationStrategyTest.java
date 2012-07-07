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

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebComponent;
import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;

/**
 * Tests for {@link AnnotationsRoleAuthorizationStrategy}
 */
public class AnnotationsRoleAuthorizationStrategyTest extends Assert
{
	/**
	 * https://issues.apache.org/jira/browse/WICKET-3974
	 */
	@Test
	public void allowNonDeniedRoles()
	{
		AnnotationsRoleAuthorizationStrategy strategy = new AnnotationsRoleAuthorizationStrategy(
			new IRoleCheckingStrategy()
			{
				@Override
				public boolean hasAnyRole(Roles roles)
				{
					return roles.contains("role1");
				}
			});

		// use mock to not need Application in the thread
		TestComponent component = Mockito.mock(TestComponent.class);
		assertTrue(strategy.isActionAuthorized(component, Component.RENDER));
	}

	/**
	 * A component without denied roles.
	 */
	@AuthorizeAction(action = "RENDER", roles = { "role1" })
	private static class TestComponent extends WebComponent
	{
		private static final long serialVersionUID = 1L;

		private TestComponent()
		{
			super("notUsed");
		}

	}
}
