/*
 * $Id$ $Revision$
 * $Date$
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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import wicket.Application;
import wicket.Component;
import wicket.MetaDataKey;
import wicket.authorization.Action;

/**
 * Utility class for authorizing component actions and component creation for
 * roles. This is the main entry point for users wanting to use the roles based
 * authorization of the wicket-auth-roles package based on wicket meta data.
 * 
 * @author Eelco Hillenius
 */
public final class RoleBinder
{
	/**
	 * Application meta data key for ations/ roles information. Typically, you
	 * do not need to use this meta data key directly, but instead use one of
	 * the bind methods of this class.
	 */
	private static final MetaDataKey MD_ROLES_FOR_CREATION = new MetaDataKey(
			ComponentCreationAuthBindings.class)
	{
	};

	/**
	 * Component meta data key for ations/ roles information. Typically, you do
	 * not need to use this meta data key directly, but instead use one of the
	 * bind methods of this class.
	 */
	public static final MetaDataKey MD_ACTION_ROLES = new MetaDataKey(
			AuthorizedAction.class)
	{
	};

	/**
	 * Structure for authorized roles for creation of components.
	 */
	private static final class ComponentCreationAuthBindings implements Serializable
	{
		private static final long serialVersionUID = 1L;

		private Map<Class, AuthorizedComponentCreationRoles> rolesForCreation = new HashMap<Class, AuthorizedComponentCreationRoles>();

		/**
		 * Add the provided roles to any existing binding or create a new one.
		 * 
		 * @param componentClass
		 *            the component class
		 * @param roles
		 *            the roles to bding
		 */
		public final void add(Class componentClass, String[] roles)
		{
			AuthorizedComponentCreationRoles binding = rolesForCreation.get(componentClass);
			if (binding == null)
			{
				binding = new AuthorizedComponentCreationRoles(componentClass, roles);
				rolesForCreation.put(componentClass, binding);
			}
			else
			{
				binding.add(binding.rolesToArray());
			}
		}

		/**
		 * Remove all roles for the given component class.
		 * 
		 * @param componentClass
		 */
		public final void remove(Class componentClass)
		{
			rolesForCreation.remove(componentClass);
		}

		/**
		 * Gets the roles that have a binding with the given component class.
		 * 
		 * @param componentClass
		 *            the component class
		 * @return the roles that have a binding with the given component class,
		 *         or null if no entries are found
		 */
		public final String[] rolesFor(Class componentClass)
		{
			AuthorizedComponentCreationRoles binding = rolesForCreation.get(componentClass);
			if (binding != null)
			{
				return binding.rolesToArray();
			}
			return null;
		}
	}

	/**
	 * Structure for holding roles that are auhorized to create a component of a
	 * certain class.
	 * 
	 * @author Eelco Hillenius
	 */
	private static final class AuthorizedComponentCreationRoles implements Serializable
	{
		private static final long serialVersionUID = 1L;

		/** allowed roles. */
		private Set<String> roles = new HashSet<String>();

		/** the component class. */
		private final Class componentClass;

		/**
		 * Construct.
		 * 
		 * @param componentClass
		 *            the component class
		 * 
		 * @param roles
		 *            the roles
		 */
		public AuthorizedComponentCreationRoles(Class componentClass, String[] roles)
		{
			if (componentClass == null)
			{
				throw new IllegalArgumentException("argument componentClass must be not null");
			}
			if (!Component.class.isAssignableFrom(componentClass))
			{
				throw new IllegalArgumentException(
						"argument componentClass must be "
								+ "of type wicket.Component or one of it's subclasses. Instead it is of type "
								+ componentClass.getName());
			}
			this.componentClass = componentClass;
			add(roles);
		}

		/**
		 * Add the provided roles to the current.
		 * 
		 * @param roles
		 *            roles to add
		 */
		public final void add(String[] roles)
		{
			if (roles == null)
			{
				throw new IllegalArgumentException("argument roles has to be not null");
			}

			for (String role : roles)
			{
				this.roles.add(role);
			}
		}

		/**
		 * Gives roles as an array.
		 * 
		 * @return the roles as an array
		 */
		public final String[] rolesToArray()
		{
			return roles.toArray(new String[roles.size()]);
		}
	}

	/**
	 * Hidden constructor; this is a utility class that should only be used
	 * through it's static methods.
	 */
	private RoleBinder()
	{
	}

	/**
	 * Authorizes the given role to create component instances of type
	 * componentClass. This authorization is added to any previously authorized
	 * roles.
	 * 
	 * @param componentClass
	 *            the component type that is subject for the authorization
	 * @param role
	 *            the role that is authorized to create component instances of
	 *            type componentClass
	 */
	public static final void authorize(Class componentClass, String role)
	{
		authorize(componentClass, new String[] { role });
	}

	/**
	 * Authorizes the given roles to create component instances of type
	 * componentClass. This authorization is added to any previously authorized
	 * roles.
	 * 
	 * @param componentClass
	 *            the component type that is subject for the authorization
	 * @param roles
	 *            the roles that are authorized to create component instances of
	 *            type componentClass
	 */
	public static final void authorize(Class componentClass, String[] roles)
	{
		Application application = Application.get();
		ComponentCreationAuthBindings authorizedRoles = (ComponentCreationAuthBindings)application
				.getMetaData(MD_ROLES_FOR_CREATION);
		if (authorizedRoles == null)
		{
			authorizedRoles = new ComponentCreationAuthBindings();
			application.setMetaData(MD_ROLES_FOR_CREATION, authorizedRoles);
		}
		authorizedRoles.add(componentClass, roles);
	}

	/**
	 * Resets all authorizations for creation of components of type
	 * componentClass. This has the effect that everyone is allowed to create
	 * instances of the component again.
	 * 
	 * @param componentClass
	 */
	public static final void clear(Class componentClass)
	{
		Application application = Application.get();
		ComponentCreationAuthBindings authorizedRoles = (ComponentCreationAuthBindings)application
				.getMetaData(MD_ROLES_FOR_CREATION);
		if (authorizedRoles != null)
		{
			authorizedRoles.remove(componentClass);
		}
	}

	/**
	 * Authorizes the given role to do perform the given action with component.
	 * 
	 * @param component
	 *            the component that is subject to the authorization
	 * @param action
	 *            the action to authorize
	 * @param role
	 *            the role to authorize
	 */
	public static final void authorize(Component component, Action action, String role)
	{
		authorize(component, action, new String[] { role });
	}

	/**
	 * Authorizes the given roles to do perform the given action with component.
	 * 
	 * @param component
	 *            the component that is subject to the authorization
	 * @param action
	 *            the action to authorize
	 * @param roles
	 *            the roles to authorize
	 */
	public static final void authorize(Component component, Action action, String[] roles)
	{
		AuthorizedAction actionRoles = (AuthorizedAction)component
				.getMetaData(MD_ACTION_ROLES);
		if (actionRoles == null)
		{
			actionRoles = new AuthorizedAction(action, roles);
			component.setMetaData(MD_ACTION_ROLES, actionRoles);
		}
		else
		{
			actionRoles.add(roles);
		}
	}

	/**
	 * Gets the roles for the given action/ component combination.
	 * 
	 * @param component
	 *            the component
	 * @param action
	 *            the action
	 * @return the roles for the action as defined with the given component
	 */
	static String[] rolesForAction(Component component, Action action)
	{
		AuthorizedAction actionRoles = (AuthorizedAction)component
				.getMetaData(MD_ACTION_ROLES);
		if (actionRoles != null)
		{
			return actionRoles.getRoles();
		}

		return null;
	}

	/**
	 * Gets the roles for creation of the given component class, or null if none
	 * were registered.
	 * 
	 * @param componentClass
	 *            the component class
	 * @return the roles that are authorized for creation of the componentClass,
	 *         or null if no specific authorization was configured
	 */
	static String[] rolesForCreation(Class componentClass)
	{
		Application application = Application.get();
		ComponentCreationAuthBindings authorizedRoles = (ComponentCreationAuthBindings)application
				.getMetaData(MD_ROLES_FOR_CREATION);
		if (authorizedRoles != null)
		{
			return authorizedRoles.rolesFor(componentClass);
		}

		return null;
	}
}
