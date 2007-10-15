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
package org.apache.wicket.authorization.strategies.role.annotations;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.strategies.role.AbstractRoleAuthorizationStrategy;
import org.apache.wicket.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authorization.strategies.role.Roles;


/**
 * Strategy that checks the
 * {@link org.apache.wicket.authorization.strategies.role.annotations.AuthorizeInstantiation}
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
	public AnnotationsRoleAuthorizationStrategy(final IRoleCheckingStrategy roleCheckingStrategy)
	{
		super(roleCheckingStrategy);
	}

	/**
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
	 */
	@SuppressWarnings("unchecked")
	public boolean isInstantiationAuthorized(final Class componentClass)
	{
		// We are authorized unless we are found not to be
		boolean authorized = true;

		// Check package annotation first
		final Package componentPackage = componentClass.getPackage();
		if (componentPackage != null)
		{
			final AuthorizeInstantiation packageAnnotation = (AuthorizeInstantiation)componentPackage
					.getAnnotation(AuthorizeInstantiation.class);
			if (packageAnnotation != null)
			{
				authorized = hasAny(new Roles(packageAnnotation.value()));
			}
		}

		// Check class annotation
		final AuthorizeInstantiation classAnnotation = (AuthorizeInstantiation)componentClass
				.getAnnotation(AuthorizeInstantiation.class);
		if (classAnnotation != null)
		{
			// If roles are defined for the class, that overrides the package
			authorized = hasAny(new Roles(classAnnotation.value()));
		}

		return authorized;
	}

	/**
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
	 *      org.apache.wicket.authorization.Action)
	 */
	public boolean isActionAuthorized(final Component component, final Action action)
	{
		// Get component's class
		final Class< ? extends Component> componentClass = component.getClass();

		// Check for a single action
		if (!check(action, componentClass.getAnnotation(AuthorizeAction.class)))
		{
			return false;
		}

		// Check for multiple actions
		final AuthorizeActions authorizeActionsAnnotation = componentClass
				.getAnnotation(AuthorizeActions.class);
		if (authorizeActionsAnnotation != null)
		{
			for (final AuthorizeAction authorizeActionAnnotation : authorizeActionsAnnotation
					.actions())
			{
				if (!check(action, authorizeActionAnnotation))
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
	 * @param authorizeActionAnnotation
	 *            The annotations information
	 * @return False if the action is not authorized
	 */
	private boolean check(final Action action, final AuthorizeAction authorizeActionAnnotation)
	{
		if (authorizeActionAnnotation != null)
		{
			if (action.getName().equals(authorizeActionAnnotation.action()))
			{
				if (hasAny(new Roles(authorizeActionAnnotation.deny())))
				{
					return false;
				}

				Roles roles = new Roles(authorizeActionAnnotation.roles());
				if (!(isEmpty(roles) || hasAny(roles)))
				{
					return false;
				}
			}
		}
		return true;
	}
}
