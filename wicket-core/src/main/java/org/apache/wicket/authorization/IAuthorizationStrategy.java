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
package org.apache.wicket.authorization;

import org.apache.wicket.Component;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;

/**
 * Authorization strategies specify aspect-like constraints on significant actions taken by the
 * framework in a given application. These constraints are guaranteed by the framework to be applied
 * consistently throughout. Violations will result in a security action directed by the strategy,
 * such as the throwing of an AuthorizationException or the filtering out of security-sensitive
 * information.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * @since Wicket 1.2
 */
public interface IAuthorizationStrategy
{
	public static class AllowAllAuthorizationStrategy implements IAuthorizationStrategy
	{
		/**
		 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
		 */
		@Override
		public <T extends IRequestableComponent> boolean isInstantiationAuthorized(final Class<T> c)
		{
			return true;
		}

		/**
		 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
		 *      org.apache.wicket.authorization.Action)
		 */
		@Override
		public boolean isActionAuthorized(Component c, Action action)
		{
			return true;
		}

		@Override
		public boolean isResourceAuthorized(IResource resource, PageParameters pageParameters)
		{
			return true;
		}
	}

	/**
	 * Implementation of {@link IAuthorizationStrategy} that allows everything.
	 */
	public static final IAuthorizationStrategy ALLOW_ALL = new AllowAllAuthorizationStrategy();

	/**
	 * Checks whether an instance of the given component class may be created. If this method
	 * returns false, the {@link IUnauthorizedComponentInstantiationListener} that is configured in
	 * the {@link org.apache.wicket.settings.def.SecuritySettings security settings} will be called. The default implementation of
	 * that listener throws a {@link UnauthorizedInstantiationException}.
	 * <p>
	 * If you wish to implement a strategy that authenticates users which cannot access a given Page
	 * (or other Component), you can simply throw a
	 * {@link org.apache.wicket.RestartResponseAtInterceptPageException} in your implementation of
	 * this method.
	 * 
	 * @param <T>
	 * 
	 * @param componentClass
	 *            The component class to check
	 * @return Whether the given component may be created
	 */
	<T extends IRequestableComponent> boolean isInstantiationAuthorized(Class<T> componentClass);

	/**
	 * Gets whether the given action is permitted. If it is, this method should return true. If it
	 * isn't, this method should either return false or - in case of a serious breach - throw a
	 * security exception. Returning is generally preferable over throwing an exception as that
	 * doesn't break the normal flow.
	 * 
	 * @param component
	 *            The component to be acted upon
	 * @param action
	 *            The action to authorize on the component
	 * @return Whether the given action may be taken on the given component
	 * @throws AuthorizationException
	 *             Can be thrown by implementation if action is unauthorized
	 * @see Component#ENABLE
	 * @see Component#RENDER
	 */
	boolean isActionAuthorized(Component component, Action action);

	/**
	 * Checks whether a request with some parameters is allowed to a resource.
	 *
	 * @param resource
	 *            The resource that should be processed
	 * @param parameters
	 *            The request parameters
	 * @return {@code true} if the request to this resource is allowed.
	 */
	boolean isResourceAuthorized(IResource resource, PageParameters parameters);
}
