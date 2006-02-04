/*
 * $Id: RolesAnnotAuthorizationStrategy.java,v 1.1 2006/02/02 08:12:42 eelco12
 * Exp $ $Revision$ $Date$
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

import wicket.Component;
import wicket.authorization.Action;
import wicket.authorization.IAuthorizationStrategy;
import wicket.authorization.roles.IRolesAuthorizer;

/**
 * Strategy that checks the
 * {@link wicket.authorization.roles.annot.AuthorizedRoles} annotation.
 * 
 * @author Eelco Hillenius
 */
public class RolesAnnotAuthorizationStrategy implements IAuthorizationStrategy
{
	/** the authorizer delegate. */
	private final IRolesAuthorizer rolesAuthorizer;

	/**
	 * Construct.
	 * 
	 * @param rolesAuthorizer
	 *            the authorizer delegate
	 */
	public RolesAnnotAuthorizationStrategy(IRolesAuthorizer rolesAuthorizer)
	{
		if (rolesAuthorizer == null)
		{
			throw new IllegalArgumentException("rolesAuthorizer must be not null");
		}
		this.rolesAuthorizer = rolesAuthorizer;
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#authorizeInstantiation(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean authorizeInstantiation(Class componentClass)
	{
		boolean authorized = true;
		Package annotPackage = componentClass.getPackage();
		if (annotPackage != null)
		{
			AuthorizedRoles packageRolesAllowed = (AuthorizedRoles)annotPackage
					.getAnnotation(AuthorizedRoles.class);
			if (packageRolesAllowed != null)
			{
				authorized = any(packageRolesAllowed.value());
			}
		}
		AuthorizedRoles classRolesAllowed = (AuthorizedRoles)componentClass
				.getAnnotation(AuthorizedRoles.class);
		if (classRolesAllowed != null)
		{
			// if roles are defined for the class, that overrides the package
			authorized = any(classRolesAllowed.value());
		}
		return authorized;
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#authorizeAction(wicket.Component,
	 *      wicket.authorization.Action)
	 */
	public boolean authorizeAction(Component component, Action action)
	{
		// check for a single action
		AuthorizedAction annotAction = component.getClass().getAnnotation(AuthorizedAction.class);
		if (annotAction != null)
		{
			if (annotAction.action().equals(action.toString()))
			{
				if (!any(annotAction.roles()))
				{
					return false;
				}
			}
		}

		// check for multiple actions
		AuthorizedActions annotActions = component.getClass()
				.getAnnotation(AuthorizedActions.class);
		if (annotActions != null)
		{
			for (AuthorizedAction a : annotActions.actions())
			{
				if (a.action().equals(action.toString()))
				{
					if (!any(a.roles()))
					{
						return false;
					}
				}
			}
		}

		return true;
	}

	private final boolean any(String[] roles)
	{
		boolean authorized;
		if (!isDefault(roles))
		{
			authorized = rolesAuthorizer.any(roles);
		}
		else
		{
			authorized = true;
		}
		return authorized;
	}

	private final boolean isDefault(String[] value)
	{
		if (value == null || (value.length == 1 && value[0].equals("")))
		{
			return true;
		}
		return false;
	}
}
