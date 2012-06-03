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

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.IClusterable;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;


/**
 * For each Action, holds a set of roles that can perform that action. Roles can be granted access
 * to a given action via authorize(Action, String role) and denied access via unauthorize(Action,
 * String role). All permissions can be removed for a given action via authorizeAll(Action).
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class ActionPermissions implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/** Map from an action to a set of role strings */
	private final Map<Action, Roles> rolesForAction = new HashMap<Action, Roles>();

	/**
	 * Gives permission for the given roles to perform the given action
	 * 
	 * @param action
	 *            The action
	 * @param rolesToAdd
	 *            The roles
	 */
	public final void authorize(final Action action, final Roles rolesToAdd)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Argument action cannot be null");
		}

		if (rolesToAdd == null)
		{
			throw new IllegalArgumentException("Argument rolesToAdd cannot be null");
		}

		Roles roles = rolesForAction.get(action);
		if (roles == null)
		{
			roles = new Roles();
			rolesForAction.put(action, roles);
		}
		roles.addAll(rolesToAdd);
	}

	/**
	 * Remove all authorization for the given action.
	 * 
	 * @param action
	 *            The action to clear
	 */
	public final void authorizeAll(final Action action)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Argument action cannot be null");
		}

		rolesForAction.remove(action);
	}

	/**
	 * Gets the roles that have a binding for the given action.
	 * 
	 * @param action
	 *            The action
	 * @return The roles authorized for the given action
	 */
	public final Roles rolesFor(final Action action)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Argument action cannot be null");
		}

		return rolesForAction.get(action);
	}

	/**
	 * Remove the given authorized role from an action. Note that this is only relevant if a role
	 * was previously authorized for that action. If no roles where previously authorized the effect
	 * of the unauthorize call is that no roles at all will be authorized for that action.
	 * 
	 * @param action
	 *            The action
	 * @param rolesToRemove
	 *            The comma separated list of roles to remove
	 */
	public final void unauthorize(final Action action, final Roles rolesToRemove)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("Argument action cannot be null");
		}

		if (rolesToRemove == null)
		{
			throw new IllegalArgumentException("Argument rolesToRemove cannot be null");
		}

		Roles roles = rolesForAction.get(action);
		if (roles != null)
		{
			roles.removeAll(rolesToRemove);
		}
		else
		{
			roles = new Roles();
			rolesForAction.put(action, roles);
		}

		// If we removed the last authorized role, we authorize the empty role
		// so that removing authorization can't suddenly open something up to
		// everyone.
		if (roles.size() == 0)
		{
			roles.add(MetaDataRoleAuthorizationStrategy.NO_ROLE);
		}
	}
}
