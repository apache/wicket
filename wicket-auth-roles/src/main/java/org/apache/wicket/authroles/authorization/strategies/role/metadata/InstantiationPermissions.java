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

import org.apache.wicket.Component;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.util.io.IClusterable;


/**
 * An internal data structure that maps a given component class to a set of role strings.
 * Permissions can be granted to instantiate a given component class via authorize(Class, Roles
 * roles) and denied via unauthorize(Class, Roles roles). All authorization can be removed for a
 * given class with authorizeAll(Class).
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class InstantiationPermissions implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/** Holds roles objects for component classes */
	private final Map<Class<? extends Component>, Roles> rolesForComponentClass = new HashMap<>();

	/**
	 * Gives the given role permission to instantiate the given class.
	 * 
	 * @param <T>
	 * @param componentClass
	 *            The component class
	 * @param rolesToAdd
	 *            The roles to add
	 */
	public final <T extends Component> void authorize(final Class<T> componentClass,
		final Roles rolesToAdd)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		if (rolesToAdd == null)
		{
			throw new IllegalArgumentException("Argument rolesToadd cannot be null");
		}

		Roles roles = rolesForComponentClass.get(componentClass);
		if (roles == null)
		{
			roles = new Roles();
			rolesForComponentClass.put(componentClass, roles);
		}
		roles.addAll(rolesToAdd);
	}

	/**
	 * Gives all roles permission to instantiate the given class. Note that this is only relevant if
	 * a role was previously authorized for that class. If no roles where previously authorized the
	 * effect of the unauthorize call is that no roles at all will be authorized for that class.
	 * 
	 * @param <T>
	 * @param componentClass
	 *            The component class
	 */
	public final <T extends Component> void authorizeAll(final Class<T> componentClass)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		rolesForComponentClass.remove(componentClass);
	}

	/**
	 * Gets the roles that have a binding with the given component class.
	 * 
	 * @param <T>
	 * 
	 * @param componentClass
	 *            the component class
	 * @return the roles that have a binding with the given component class, or null if no entries
	 *         are found
	 */
	public <T extends IRequestableComponent> Roles authorizedRoles(final Class<T> componentClass)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		return rolesForComponentClass.get(componentClass);
	}

	/**
	 * Removes permission for the given role to instantiate the given class.
	 * 
	 * @param <T>
	 * 
	 * @param componentClass
	 *            The class
	 * @param rolesToRemove
	 *            The role to deny
	 */
	public final <T extends Component> void unauthorize(final Class<T> componentClass,
		final Roles rolesToRemove)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("Argument componentClass cannot be null");
		}

		if (rolesToRemove == null)
		{
			throw new IllegalArgumentException("Argument rolesToRemove cannot be null");
		}

		Roles roles = rolesForComponentClass.get(componentClass);
		if (roles != null)
		{
			roles.removeAll(rolesToRemove);
		}
		else
		{
			roles = new Roles();
			rolesForComponentClass.put(componentClass, roles);
		}

		// If we removed the last authorized role, we authorize the empty role
		// so that removing authorization can't suddenly open something up to
		// everyone.
		if (roles.size() == 0)
		{
			roles.add(MetaDataRoleAuthorizationStrategy.NO_ROLE);
		}
	}

	/**
	 * @return gets map with roles objects for a component classes
	 */
	protected final Map<Class<? extends Component>, Roles> getRolesForComponentClass()
	{
		return rolesForComponentClass;
	}
}
