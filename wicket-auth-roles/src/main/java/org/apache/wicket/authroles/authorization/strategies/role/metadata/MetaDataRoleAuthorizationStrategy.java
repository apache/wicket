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

import org.apache.wicket.Application;
import org.apache.wicket.Component;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authroles.authorization.strategies.role.AbstractRoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.component.IRequestableComponent;


/**
 * Strategy that uses the Wicket metadata facility to check authorization. The static
 * <code>authorize</code> methods are for authorizing component actions and component instantiation
 * by role. This class is the main entry point for users wanting to use the roles-based
 * authorization of the wicket-auth-roles package based on wicket metadata.
 * 
 * For instance, use like:
 * 
 * <pre>
 * MetaDataRoleAuthorizationStrategy.authorize(myPanel, RENDER, &quot;ADMIN&quot;);
 * </pre>
 * 
 * for actions on component instances, or:
 * 
 * <pre>
 * MetaDataRoleAuthorizationStrategy.authorize(AdminBookmarkablePage.class, &quot;ADMIN&quot;);
 * </pre>
 * 
 * for doing role based authorization for component instantation.
 * 
 * @see org.apache.wicket.MetaDataKey
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class MetaDataRoleAuthorizationStrategy extends AbstractRoleAuthorizationStrategy
{
	/**
	 * Component meta data key for actions/roles information. Typically, you do not need to use this
	 * meta data key directly, but instead use one of the bind methods of this class.
	 */
	public static final MetaDataKey<ActionPermissions> ACTION_PERMISSIONS = new MetaDataKey<ActionPermissions>()
	{
		private static final long serialVersionUID = 1L;
	};

	/**
	 * Application meta data key for actions/roles information. Typically, you do not need to use
	 * this meta data key directly, but instead use one of the bind methods of this class.
	 */
	public static final MetaDataKey<InstantiationPermissions> INSTANTIATION_PERMISSIONS = new MetaDataKey<InstantiationPermissions>()
	{
		private static final long serialVersionUID = 1L;
	};

	/** Special role string for denying access to all */
	public static final String NO_ROLE = "wicket:NO_ROLE";

	/**
	 * Authorizes the given role to create component instances of type componentClass. This
	 * authorization is added to any previously authorized roles.
	 * 
	 * @param <T>
	 * 
	 * @param componentClass
	 *            The component type that is subject for the authorization
	 * @param roles
	 *            The comma separated roles that are authorized to create component instances of
	 *            type componentClass
	 */
	public static final <T extends Component> void authorize(final Class<T> componentClass,
		final String roles)
	{
		final Application application = Application.get();
		InstantiationPermissions permissions = application.getMetaData(INSTANTIATION_PERMISSIONS);
		if (permissions == null)
		{
			permissions = new InstantiationPermissions();
			application.setMetaData(INSTANTIATION_PERMISSIONS, permissions);
		}
		permissions.authorize(componentClass, new Roles(roles));
	}

	/**
	 * Authorizes the given role to perform the given action on the given component.
	 * 
	 * @param component
	 *            The component that is subject to the authorization
	 * @param action
	 *            The action to authorize
	 * @param roles
	 *            The comma separated roles to authorize
	 */
	public static final void authorize(final Component component, final Action action,
		final String roles)
	{
		ActionPermissions permissions = component.getMetaData(ACTION_PERMISSIONS);
		if (permissions == null)
		{
			permissions = new ActionPermissions();
			component.setMetaData(ACTION_PERMISSIONS, permissions);
		}
		permissions.authorize(action, new Roles(roles));
	}

	/**
	 * Grants permission to all roles to create instances of the given component class.
	 * 
	 * @param <T>
	 * 
	 * @param componentClass
	 *            The component class
	 */
	public static final <T extends Component> void authorizeAll(final Class<T> componentClass)
	{
		Application application = Application.get();
		InstantiationPermissions authorizedRoles = application.getMetaData(INSTANTIATION_PERMISSIONS);
		if (authorizedRoles != null)
		{
			authorizedRoles.authorizeAll(componentClass);
		}
	}

	/**
	 * Grants permission to all roles to perform the given action on the given component.
	 * 
	 * @param component
	 *            The component that is subject to the authorization
	 * @param action
	 *            The action to authorize
	 */
	public static final void authorizeAll(final Component component, final Action action)
	{
		ActionPermissions permissions = component.getMetaData(ACTION_PERMISSIONS);
		if (permissions != null)
		{
			permissions.authorizeAll(action);
		}
	}

	/**
	 * Removes permission for the given roles to create instances of the given component class.
	 * There is no danger in removing authorization by calling this method. If the last
	 * authorization grant is removed for a given componentClass, the internal role NO_ROLE will
	 * automatically be added, effectively denying access to all roles (if this was not done, all
	 * roles would suddenly have access since no authorization is equivalent to full access).
	 * 
	 * @param <T>
	 * 
	 * @param componentClass
	 *            The component type
	 * @param roles
	 *            The comma separated list of roles that are no longer to be authorized to create
	 *            instances of type componentClass
	 */
	public static final <T extends Component> void unauthorize(final Class<T> componentClass,
		final String roles)
	{
		final InstantiationPermissions permissions = Application.get().getMetaData(
			INSTANTIATION_PERMISSIONS);
		if (permissions != null)
		{
			permissions.unauthorize(componentClass, new Roles(roles));
		}
	}

	/**
	 * Removes permission for the given role to perform the given action on the given component.
	 * There is no danger in removing authorization by calling this method. If the last
	 * authorization grant is removed for a given action, the internal role NO_ROLE will
	 * automatically be added, effectively denying access to all roles (if this was not done, all
	 * roles would suddenly have access since no authorization is equivalent to full access).
	 * 
	 * @param component
	 *            The component
	 * @param action
	 *            The action
	 * @param roles
	 *            The comma separated list of roles that are no longer allowed to perform the given
	 *            action
	 */
	public static final void unauthorize(final Component component, final Action action,
		final String roles)
	{
		final ActionPermissions permissions = component.getMetaData(ACTION_PERMISSIONS);
		if (permissions != null)
		{
			permissions.unauthorize(action, new Roles(roles));
		}
	}

	/**
	 * Grants authorization to instantiate the given class to just the role NO_ROLE, effectively
	 * denying all other roles.
	 * 
	 * @param <T>
	 * 
	 * @param componentClass
	 *            The component class
	 */
	public static final <T extends Component> void unauthorizeAll(Class<T> componentClass)
	{
		authorizeAll(componentClass);
		authorize(componentClass, NO_ROLE);
	}

	/**
	 * Grants authorization to perform the given action to just the role NO_ROLE, effectively
	 * denying all other roles.
	 * 
	 * @param component
	 *            the component that is subject to the authorization
	 * @param action
	 *            the action to authorize
	 */
	public static final void unauthorizeAll(final Component component, final Action action)
	{
		authorizeAll(component, action);
		authorize(component, action, NO_ROLE);
	}

	/**
	 * Construct.
	 * 
	 * @param roleCheckingStrategy
	 *            the authorizer object
	 */
	public MetaDataRoleAuthorizationStrategy(final IRoleCheckingStrategy roleCheckingStrategy)
	{
		super(roleCheckingStrategy);
	}

	/**
	 * Uses component level meta data to match roles for component action execution.
	 * 
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
	 *      org.apache.wicket.authorization.Action)
	 */
	@Override
	public boolean isActionAuthorized(final Component component, final Action action)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component has to be not null");
		}
		if (action == null)
		{
			throw new IllegalArgumentException("argument action has to be not null");
		}

		final Roles roles = rolesAuthorizedToPerformAction(component, action);
		if (roles != null)
		{
			return hasAny(roles);
		}
		return true;
	}

	/**
	 * Uses application level meta data to match roles for component instantiation.
	 * 
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
	 */
	@Override
	public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
		final Class<T> componentClass)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("argument componentClass cannot be null");
		}

		// as long as the interface does not use generics, we should check this
		if (!Component.class.isAssignableFrom(componentClass))
		{
			throw new IllegalArgumentException("argument componentClass must be of type " +
				Component.class.getName());
		}

		final Roles roles = rolesAuthorizedToInstantiate(componentClass);
		if (roles != null)
		{
			return hasAny(roles);
		}
		return true;
	}

	/**
	 * Gets the roles for creation of the given component class, or null if none were registered.
	 * 
	 * @param <T>
	 * 
	 * @param componentClass
	 *            the component class
	 * @return the roles that are authorized for creation of the componentClass, or null if no
	 *         specific authorization was configured
	 */
	private static <T extends IRequestableComponent> Roles rolesAuthorizedToInstantiate(
		final Class<T> componentClass)
	{
		final InstantiationPermissions permissions = Application.get().getMetaData(
			INSTANTIATION_PERMISSIONS);
		if (permissions != null)
		{
			return permissions.authorizedRoles(componentClass);
		}
		return null;
	}

	/**
	 * Gets the roles for the given action/component combination.
	 * 
	 * @param component
	 *            the component
	 * @param action
	 *            the action
	 * @return the roles for the action as defined with the given component
	 */
	private static Roles rolesAuthorizedToPerformAction(final Component component,
		final Action action)
	{
		final ActionPermissions permissions = component.getMetaData(ACTION_PERMISSIONS);
		if (permissions != null)
		{
			return permissions.rolesFor(action);
		}
		return null;
	}
}
