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
package wicket.authentication;

import wicket.Component;
import wicket.ISessionFactory;
import wicket.Page;
import wicket.Request;
import wicket.RestartResponseAtInterceptPageException;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.authorization.IUnauthorizedComponentInstantiationListener;
import wicket.authorization.UnauthorizedInstantiationException;
import wicket.authorization.strategies.role.IRoleCheckingStrategy;
import wicket.authorization.strategies.role.RoleAuthorizationStrategy;
import wicket.authorization.strategies.role.Roles;
import wicket.markup.html.WebPage;
import wicket.protocol.http.WebApplication;

/**
 * A web application subclass that does role-based authentication.
 * 
 * @author Jonathan Locke
 */
public abstract class AuthenticatedWebApplication extends WebApplication
		implements
			IRoleCheckingStrategy,
			IUnauthorizedComponentInstantiationListener
{
	/** Subclass of authenticated web session to instantiate */
	private final Class< ? extends AuthenticatedWebSession> webSessionClass;

	/**
	 * Constructor
	 */
	public AuthenticatedWebApplication()
	{
		// Get web session class to instantiate
		this.webSessionClass = getWebSessionClass();
	}
	
	@Override
	protected void init()
	{
		super.init();

		// Set authorization strategy and unauthorized instantiation listener
		getSecuritySettings().setAuthorizationStrategy(new RoleAuthorizationStrategy(this));
		getSecuritySettings().setUnauthorizedComponentInstantiationListener(this);
	}

	/**
	 * @see IRoleCheckingStrategy#hasAnyRole(Roles)
	 */
	public final boolean hasAnyRole(final Roles roles)
	{
		final Roles sessionRoles = AuthenticatedWebSession.get().getRoles();
		return sessionRoles != null && sessionRoles.hasAnyRole(roles);
	}

	/**
	 * @see IUnauthorizedComponentInstantiationListener#onUnauthorizedInstantiation(Component)
	 */
	public final void onUnauthorizedInstantiation(final Component component)
	{
		// If there is a sign in page class declared, and the unauthorized
		// component is a page, but it's not the sign in page
		if (component instanceof Page)
		{
			if (!AuthenticatedWebSession.get().isSignedIn())
			{
				// Redirect to intercept page to let the user sign in
				throw new RestartResponseAtInterceptPageException(getSignInPageClass());
			}
			else
			{
				onUnauthorizedPage((Page)component);
			}
		}
		else
		{
			// The component was not a page, so throw an exception
			throw new UnauthorizedInstantiationException(component.getClass());
		}
	}

	/**
	 * @see wicket.Application#getSessionFactory()
	 */
	@Override
	protected final ISessionFactory getSessionFactory()
	{
		return new ISessionFactory()
		{
			private static final long serialVersionUID = 1L;

			public Session newSession(Request request)
			{
				try
				{
					return webSessionClass
							.getDeclaredConstructor(AuthenticatedWebApplication.class).newInstance(
									AuthenticatedWebApplication.this);
				}
				catch (Exception e)
				{
					throw new WicketRuntimeException("Unable to instantiate web session class "
							+ webSessionClass, e);
				}
			}
		};
	}

	/**
	 * @return AuthenticatedWebSession subclass to use in this authenticated web
	 *         application.
	 */
	protected abstract Class< ? extends AuthenticatedWebSession> getWebSessionClass();

	/**
	 * @return Subclass of sign-in page
	 */
	protected abstract Class< ? extends WebPage> getSignInPageClass();

	/**
	 * Called when an AUTHENTICATED user tries to navigate to a page that they
	 * are not authorized to access. You might want to override this to navigate
	 * to some explanatory page or to the application's home page.
	 * 
	 * @param page
	 *            The page
	 */
	protected void onUnauthorizedPage(final Page page)
	{
		// The component was not a page, so throw an exception
		throw new UnauthorizedInstantiationException(page.getClass());
	}
}
