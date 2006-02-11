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
package wicket.authorization.strategies.role.annotations;

import wicket.Component;
import wicket.authorization.Action;
import wicket.authorization.strategies.role.AbstractRoleAuthorizationStrategy;
import wicket.authorization.strategies.role.IRoleCheckingStrategy;
import wicket.authorization.strategies.role.Roles;

/**
 * Strategy that checks the
 * {@link wicket.authorization.strategies.role.annotations.AuthorizedRoles} annotation.
 * 
 * @author Eelco Hillenius
 */
public class AnnotationsRoleAuthorizationStrategy extends AbstractRoleAuthorizationStrategy
{
	/**
	 * Construct.
	 * 
	 * @param roleCheckingStrategy
	 *            the authorizer delegate
	 */
	public AnnotationsRoleAuthorizationStrategy(IRoleCheckingStrategy roleCheckingStrategy)
	{
		super(roleCheckingStrategy);
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
				authorized = hasAny(new Roles(packageRolesAllowed.value()));
			}
		}
		AuthorizedRoles classRolesAllowed = (AuthorizedRoles)componentClass
				.getAnnotation(AuthorizedRoles.class);
		if (classRolesAllowed != null)
		{
			// if roles are defined for the class, that overrides the package
			authorized = hasAny(new Roles(classRolesAllowed.value()));
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
		final AuthorizedAction authorizedAction = component.getClass().getAnnotation(AuthorizedAction.class);
		if (authorizedAction != null)
		{
			if (authorizedAction.action().equals(action.toString()))
			{
				if (!hasAny(new Roles(authorizedAction.roles())))
				{
					return false;
				}
			}
		}

		// check for multiple actions
		AuthorizedActions authorizedActions = component.getClass()
				.getAnnotation(AuthorizedActions.class);
		if (authorizedActions != null)
		{
			for (AuthorizedAction a : authorizedActions.actions())
			{
				if (a.action().equals(action.toString()))
				{
					if (!hasAny(new Roles(a.roles())))
					{
						return false;
					}
				}
			}
		}

		return true;
	}
}
