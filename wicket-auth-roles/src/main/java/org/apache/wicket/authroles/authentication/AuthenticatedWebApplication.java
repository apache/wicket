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
package org.apache.wicket.authroles.authentication;

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.RestartResponseAtInterceptPageException;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.authroles.authorization.strategies.role.IRoleCheckingStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.RoleAuthorizationStrategy;
import org.apache.wicket.authroles.authorization.strategies.role.Roles;
import org.apache.wicket.markup.html.WebPage;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Response;


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
	/**
	 * @see org.apache.wicket.protocol.http.WebApplication#init()
	 */
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
	@Override
	public final boolean hasAnyRole(final Roles roles)
	{
		final Roles sessionRoles = AbstractAuthenticatedWebSession.get().getRoles();
		return (sessionRoles != null) && sessionRoles.hasAnyRole(roles);
	}

	/**
	 * @see IUnauthorizedComponentInstantiationListener#onUnauthorizedInstantiation(Component)
	 */
	@Override
	public final void onUnauthorizedInstantiation(final Component component)
	{
		// If there is a sign in page class declared, and the unauthorized
		// component is a page, but it's not the sign in page
		if (component instanceof Page)
		{
			if (!AbstractAuthenticatedWebSession.get().isSignedIn())
			{
				// Redirect to intercept page to let the user sign in
				restartResponseAtSignInPage();
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
	 * Restarts response at sign in page.
	 * 
	 * NOTE: this method internally throws a restart response exception, so no code after a call to
	 * this method will be executed
	 */
	public void restartResponseAtSignInPage()
	{
		throw new RestartResponseAtInterceptPageException(getSignInPageClass());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Session newSession(final Request request, final Response response)
	{
		Class<? extends AbstractAuthenticatedWebSession> webSessionClass = getWebSessionClass();
		try
		{
			return webSessionClass
				.getDeclaredConstructor(Request.class)
				.newInstance(request);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Unable to instantiate web session " +
				webSessionClass, e);
		}
	}

	/**
	 * @return BaseAuthenticatedWebSession subclass to use in this authenticated web application.
	 */
	protected abstract Class<? extends AbstractAuthenticatedWebSession> getWebSessionClass();

	/**
	 * @return Subclass of sign-in page
	 */
	protected abstract Class<? extends WebPage> getSignInPageClass();

	/**
	 * Called when an AUTHENTICATED user tries to navigate to a page that they are not authorized to
	 * access. You might want to override this to navigate to some explanatory page or to the
	 * application's home page.
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
