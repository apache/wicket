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
 * {@link wicket.authorization.strategies.role.annotations.AuthorizeInstantiation}
 * annotation.
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
	 * @see wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean isInstantiationAuthorized(Class componentClass)
	{
		boolean authorized = true;
		Package annotPackage = componentClass.getPackage();
		if (annotPackage != null)
		{
			AuthorizeInstantiation packageRolesAllowed = (AuthorizeInstantiation)annotPackage
					.getAnnotation(AuthorizeInstantiation.class);
			if (packageRolesAllowed != null)
			{
				authorized = hasAny(new Roles(packageRolesAllowed.value()));
			}
		}
		AuthorizeInstantiation classRolesAllowed = (AuthorizeInstantiation)componentClass
				.getAnnotation(AuthorizeInstantiation.class);
		if (classRolesAllowed != null)
		{
			// if roles are defined for the class, that overrides the package
			authorized = hasAny(new Roles(classRolesAllowed.value()));
		}
		return authorized;
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#isActionAuthorized(wicket.Component,
	 *      wicket.authorization.Action)
	 */
	public boolean isActionAuthorized(final Component component, final Action action)
	{
		// Check for a single action
		final AuthorizeAction authorizeAction = component.getClass().getAnnotation(
				AuthorizeAction.class);
		if (authorizeAction != null)
		{
			if (!check(action, authorizeAction))
			{
				return false;
			}
		}

		// Check for multiple actions
		final AuthorizeActions authorizedActions = component.getClass().getAnnotation(
				AuthorizeActions.class);
		if (authorizedActions != null)
		{
			for (final AuthorizeAction a : authorizedActions.actions())
			{
				if (!check(action, a))
				{
					return false;
				}
			}
		}

		return true;
	}

	/**
	 * @param action
	 *            The action to check
	 * @param authorizedAction
	 *            The annotations information
	 * @return False if the action is not authorized
	 */
	private boolean check(Action action, final AuthorizeAction authorizedAction)
	{
		if (authorizedAction.action().equals(action.toString()))
		{
			if (!hasAny(new Roles(authorizedAction.roles())))
			{
				return false;
			}
		}
		return true;
	}
}
