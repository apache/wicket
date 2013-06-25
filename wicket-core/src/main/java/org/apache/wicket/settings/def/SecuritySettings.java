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
import org.apache.wicket.authorization.IUnauthorizedResourceRequestListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.settings.ISecuritySettings;
import org.apache.wicket.util.crypt.CachingSunJceCryptFactory;
import org.apache.wicket.util.crypt.ICryptFactory;

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
	private static final IUnauthorizedComponentInstantiationListener DEFAULT_UNAUTHORIZED_COMPONENT_INSTANTIATION_LISTENER = new IUnauthorizedComponentInstantiationListener()
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

	private IUnauthorizedComponentInstantiationListener unauthorizedComponentInstantiationListener =
			DEFAULT_UNAUTHORIZED_COMPONENT_INSTANTIATION_LISTENER;

	private static final IUnauthorizedResourceRequestListener DEFAULT_UNAUTHORIZED_RESOURCE_REQUEST_LISTENER =
			new DefaultUnauthorizedResourceRequestListener();

	private IUnauthorizedResourceRequestListener unauthorizedResourceRequestListener = DEFAULT_UNAUTHORIZED_RESOURCE_REQUEST_LISTENER;

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#getAuthorizationStrategy()
	 */
	@Override
	public IAuthorizationStrategy getAuthorizationStrategy()
	{
		return authorizationStrategy;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#getCryptFactory()
	 */
	@Override
	public synchronized ICryptFactory getCryptFactory()
	{
		if (cryptFactory == null)
		{
			cryptFactory = new CachingSunJceCryptFactory(ISecuritySettings.DEFAULT_ENCRYPTION_KEY);
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
		if (strategy == null)
		{
			throw new IllegalArgumentException("authorization strategy cannot be set to null");
		}
		authorizationStrategy = strategy;
	}

	/**
	 * @see org.apache.wicket.settings.ISecuritySettings#setCryptFactory(org.apache.wicket.util.crypt.ICryptFactory)
	 */
	@Override
	public void setCryptFactory(ICryptFactory cryptFactory)
	{
		if (cryptFactory == null)
		{
			throw new IllegalArgumentException("cryptFactory cannot be null");
		}
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
		IUnauthorizedComponentInstantiationListener listener)
	{
		this.unauthorizedComponentInstantiationListener = listener == null ? DEFAULT_UNAUTHORIZED_COMPONENT_INSTANTIATION_LISTENER : listener;
	}

	@Override
	public IUnauthorizedResourceRequestListener getUnauthorizedResourceRequestListener()
	{
		return unauthorizedResourceRequestListener;
	}

	@Override
	public void setUnauthorizedResourceRequestListener(IUnauthorizedResourceRequestListener listener)
	{
		this.unauthorizedResourceRequestListener = listener == null ? DEFAULT_UNAUTHORIZED_RESOURCE_REQUEST_LISTENER : listener;
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
