/*
 * $Id: RequestListenerInterface.java,v 1.3 2006/02/13 02:15:14 jonathanlocke
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
package wicket.authentication;

import wicket.Application;
import wicket.ISessionFactory;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.authorization.strategies.role.IRoleCheckingStrategy;
import wicket.authorization.strategies.role.RoleAuthorizationStrategy;
import wicket.authorization.strategies.role.Roles;
import wicket.protocol.http.WebApplication;

/**
 * A web application subclass that does role based authentication.
 * 
 * @author Jonathan Locke
 */
public abstract class AuthenticatedWebApplication extends WebApplication
		implements
			IRoleCheckingStrategy
{
	/** Subclass of authenticated web session to instantiate */
	private final Class< ? extends AuthenticatedWebSession> webSessionClass;

	/**
	 * Constructor
	 */
	public AuthenticatedWebApplication()
	{
		this.webSessionClass = getWebSessionClass();
		getSecuritySettings().setAuthorizationStrategy(new RoleAuthorizationStrategy(this));
	}

	/**
	 * @return AuthenticatedWebSession subclass to use in this authenticated web
	 *         application.
	 */
	protected abstract Class< ? extends AuthenticatedWebSession> getWebSessionClass();

	/**
	 * Get session factory
	 * 
	 * @return The Session factory
	 */
	@Override
	protected final ISessionFactory getSessionFactory()
	{
		return new ISessionFactory()
		{
			public Session newSession()
			{
				try
				{
					return webSessionClass.getConstructor(new Class[] { Application.class })
							.newInstance(AuthenticatedWebApplication.this);
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
	 * @param roles
	 *            The roles to check
	 * @return True if the authenticated user has any of the given roles
	 */
	public boolean hasAnyRole(final Roles roles)
	{
		return AuthenticatedWebSession.get().getUser().getRoles().hasAnyRole(roles);
	}
}
