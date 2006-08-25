/*
 * $Id: InstantiationPermissions.java 4319 2006-02-11 15:26:12 -0800 (Sat, 11
 * Feb 2006) eelco12 $ $Revision$ $Date: 2006-02-11 15:26:12 -0800 (Sat,
 * 11 Feb 2006) $
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
package wicket.authorization.strategies.role.metadata;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import wicket.Component;
import wicket.authorization.strategies.role.Roles;

/**
 * An internal data structure that maps a given component class to a set of role
 * strings. Permissions can be granted to instantiate a given component class
 * via authorize(Class, Roles roles) and denied via unauthorize(Class, Roles
 * roles). All authorization can be removed for a given class with
 * authorizeAll(Class).
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class InstantiationPermissions implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** Holds roles objects for component classes */
	private final Map<Class< ? extends Component>, Roles> rolesForComponentClass = new HashMap<Class< ? extends Component>, Roles>();

	/**
	 * Gives the given role permission to instantiate the given class.
	 * 
	 * @param componentClass
	 *            The component class
	 * @param rolesToAdd
	 *            The roles to add
	 */
	public final void authorize(final Class< ? extends Component> componentClass,
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
	 * Gives all roles permission to instantiate the given class.
	 * 
	 * @param componentClass
	 *            The component class
	 */
	public final void authorizeAll(final Class< ? extends Component> componentClass)
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
	 * @param componentClass
	 *            the component class
	 * @return the roles that have a binding with the given component class, or
	 *         null if no entries are found
	 */
	public Roles authorizedRoles(final Class< ? extends Component> componentClass)
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
	 * @param componentClass
	 *            The class
	 * @param rolesToRemove
	 *            The role to deny
	 */
	public final void unauthorize(final Class< ? extends Component> componentClass,
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

		final Roles roles = rolesForComponentClass.get(componentClass);
		if (roles != null)
		{
			roles.removeAll(rolesToRemove);
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
	protected final Map<Class< ? extends Component>, Roles> getRolesForComponentClass()
	{
		return rolesForComponentClass;
	}
}
