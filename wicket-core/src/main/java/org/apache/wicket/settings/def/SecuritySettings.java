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
package org.apache.wicket.settings.def;

import org.apache.wicket.Component;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.authentication.strategy.DefaultAuthenticationStrategy;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory;
import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.util.crypt.ICryptFactory;
import org.apache.wicket.util.lang.Args;

/**
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class SecuritySettings implements ISecuritySettings
{
	/** The authorization strategy. */
	private IAuthorizationStrategy authorizationStrategy = IAuthorizationStrategy.ALLOW_ALL;

	/** The authentication strategy. */
	private IAuthenticationStrategy authenticationStrategy;

	/** factory for creating crypt objects */
	private ICryptFactory cryptFactory;

	/**
	 * Whether mounts should be enforced. If true, requests for mounted targets have to done through
	 * the mounted paths. If, for instance, a bookmarkable page is mounted to a path, a request to
	 * that same page via the bookmarkablePage parameter will be denied.
	 */
	private boolean enforceMounts = false;

	/** Authorizer for component instantiations */
	private IUnauthorizedComponentInstantiationListener unauthorizedComponentInstantiationListener = new IUnauthorizedComponentInstantiationListener()
	{
		/**
		 * Called when an unauthorized component instantiation is about to take place (but before it
		 * happens).
		 * 
		 * @param component
		 *            The partially constructed component (only the id is guaranteed to be valid).
		 */
		@Override
		public void onUnauthorizedInstantiation(final Component component)
		{
			throw new UnauthorizedInstantiationException(component.getClass());
		}
	};

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#getAuthorizationStrategy()
	 */
	@Override
	public IAuthorizationStrategy getAuthorizationStrategy()
	{
		return authorizationStrategy;
	}

	/**
	 * @return crypt factory used to generate crypt objects. By default it uses
	 * {@link org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory} that
	 * binds an HTTP session to store the user specific key
	 */
	@Override
	public synchronized ICryptFactory getCryptFactory()
	{
		if (cryptFactory == null)
		{
			cryptFactory = new KeyInSessionSunJceCryptFactory();
		}
		return cryptFactory;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#getEnforceMounts()
	 */
	@Override
	public boolean getEnforceMounts()
	{
		return enforceMounts;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#getUnauthorizedComponentInstantiationListener()
	 */
	@Override
	public IUnauthorizedComponentInstantiationListener getUnauthorizedComponentInstantiationListener()
	{
		return unauthorizedComponentInstantiationListener;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#setAuthorizationStrategy(org.apache.wicket.authorization.IAuthorizationStrategy)
	 */
	@Override
	public void setAuthorizationStrategy(IAuthorizationStrategy strategy)
	{
		Args.notNull(strategy, "authorization strategy");
		authorizationStrategy = strategy;
	}

	@Override
	public void setCryptFactory(ICryptFactory cryptFactory)
	{
		Args.notNull(cryptFactory, "Crypt factory");
		this.cryptFactory = cryptFactory;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#setEnforceMounts(boolean)
	 */
	@Override
	public void setEnforceMounts(boolean enforce)
	{
		enforceMounts = enforce;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#setUnauthorizedComponentInstantiationListener(org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener)
	 */
	@Override
	public void setUnauthorizedComponentInstantiationListener(
		IUnauthorizedComponentInstantiationListener unauthorizedComponentInstantiationListener)
	{
		this.unauthorizedComponentInstantiationListener = unauthorizedComponentInstantiationListener;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#getAuthenticationStrategy()
	 */
	@Override
	public IAuthenticationStrategy getAuthenticationStrategy()
	{
		if (authenticationStrategy == null)
		{
			authenticationStrategy = new DefaultAuthenticationStrategy("LoggedIn");
		}
		return authenticationStrategy;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#setAuthenticationStrategy(org.apache.wicket.authentication.IAuthenticationStrategy)
	 */
	@Override
	public void setAuthenticationStrategy(final IAuthenticationStrategy strategy)
	{
		authenticationStrategy = strategy;
	}
}
