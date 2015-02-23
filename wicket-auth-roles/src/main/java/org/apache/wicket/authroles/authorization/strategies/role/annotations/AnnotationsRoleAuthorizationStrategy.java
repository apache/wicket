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
package org.apache.wicket.authroles.authorization.strategies.role.annotations;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authroles.authorization.strategies.role.AbstractRoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;


/**
 * Strategy that checks the {@link AuthorizeInstantiation} annotation.
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
	@Override
	public <T extends IRequestableComponent> boolean isInstantiationAuthorized(
		final Class<T> componentClass)
	{
		// We are authorized unless we are found not to be
		boolean authorized = true;

		// Check class annotation first because it is more specific than package annotation
		final AuthorizeInstantiation classAnnotation = componentClass.getAnnotation(AuthorizeInstantiation.class);
		if (classAnnotation != null)
		{
			authorized = hasAny(new Roles(classAnnotation.value()));
		}
		else
		{
			// Check package annotation if there is no one on the the class
			final Package componentPackage = componentClass.getPackage();
			if (componentPackage != null)
			{
				final AuthorizeInstantiation packageAnnotation = componentPackage.getAnnotation(AuthorizeInstantiation.class);
				if (packageAnnotation != null)
				{
					authorized = hasAny(new Roles(packageAnnotation.value()));
				}
			}
		}

		return authorized;
	}

	/**
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
	 *      org.apache.wicket.authorization.Action)
	 */
	@Override
	public boolean isActionAuthorized(final Component component, final Action action)
	{
		// Get component's class
		final Class<?> componentClass = component.getClass();

		return isActionAuthorized(componentClass, action);
	}

	protected boolean isActionAuthorized(final Class<?> componentClass, final Action action)
	{
		// Check for a single action
		if (!check(action, componentClass.getAnnotation(AuthorizeAction.class)))
		{
			return false;
		}

		// Check for multiple actions
		final AuthorizeActions authorizeActionsAnnotation = componentClass.getAnnotation(AuthorizeActions.class);
		if (authorizeActionsAnnotation != null)
		{
			for (final AuthorizeAction authorizeActionAnnotation : authorizeActionsAnnotation.actions())
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
				Roles deniedRoles = new Roles(authorizeActionAnnotation.deny());
				if (isEmpty(deniedRoles) == false && hasAny(deniedRoles))
				{
					return false;
				}

				Roles acceptedRoles = new Roles(authorizeActionAnnotation.roles());
				if (!(isEmpty(acceptedRoles) || hasAny(acceptedRoles)))
				{
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public boolean isResourceAuthorized(IResource resource, PageParameters pageParameters)
	{
		return checkResource(resource.getClass().getAnnotation(AuthorizeResource.class)) || checkResource(
				resource.getClass().getPackage().getAnnotation(AuthorizeResource.class));
	}

	private boolean checkResource(AuthorizeResource annotation)
	{
		if (annotation != null)
		{
			return hasAny(new Roles(annotation.value()));
		} else
		{
			return false;
		}
	}
}
