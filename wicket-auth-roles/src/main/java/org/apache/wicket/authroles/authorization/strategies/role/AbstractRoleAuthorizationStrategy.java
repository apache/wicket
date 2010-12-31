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
package org.apache.wicket.authroles.authorization.strategies.role;

import org.apache.wicket.authorization.IAuthorizationStrategy;

/**
 * Base strategy that uses an instance of
 * {@link org.apache.wicket.authorization.strategies.role.IRoleCheckingStrategy}.
 * 
 * @author Eelco Hillenius
 */
public abstract class AbstractRoleAuthorizationStrategy implements IAuthorizationStrategy
{
	/** Role checking strategy. */
	private final IRoleCheckingStrategy roleCheckingStrategy;

	/**
	 * Construct.
	 * 
	 * @param roleCheckingStrategy
	 *            the authorizer delegate
	 */
	public AbstractRoleAuthorizationStrategy(IRoleCheckingStrategy roleCheckingStrategy)
	{
		if (roleCheckingStrategy == null)
		{
			throw new IllegalArgumentException("roleCheckingStrategy must be not null");
		}
		this.roleCheckingStrategy = roleCheckingStrategy;
	}

	/**
	 * Gets whether any of the given roles applies to the authorizer.
	 * 
	 * @param roles
	 *            the roles
	 * @return whether any of the given roles applies to the authorizer
	 */
	protected final boolean hasAny(Roles roles)
	{
		if (roles.isEmpty())
		{
			return true;
		}
		else
		{
			return roleCheckingStrategy.hasAnyRole(roles);
		}
	}

	/**
	 * Conducts a check to see if the roles object is empty. Since the roles object does not contain
	 * any null values and will always hold an empty string, an extra test is required beyond
	 * roles.isEmpty().
	 * 
	 * @param roles
	 *            the Roles object to test
	 * @return true if the object holds no real roles
	 */
	protected final boolean isEmpty(Roles roles)
	{
		if (roles.isEmpty())
		{
			return true;
		}

		if (roles.size() == 1)
		{
			return "".equals(roles.iterator().next());
		}

		return false;
	}
}
