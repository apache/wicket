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
package org.apache.wicket.settings;

import org.apache.wicket.Component;
import org.apache.wicket.authentication.IAuthenticationStrategy;
import org.apache.wicket.authentication.strategy.DefaultAuthenticationStrategy;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.authorization.IUnauthorizedComponentInstantiationListener;
import org.apache.wicket.authorization.IUnauthorizedResourceRequestListener;
import org.apache.wicket.authorization.UnauthorizedInstantiationException;
import org.apache.wicket.core.util.crypt.KeyInSessionSunJceCryptFactory;
import org.apache.wicket.util.crypt.ICryptFactory;
import org.apache.wicket.util.lang.Args;

/**
 * Interface for security related settings
 *
 * @author Jonathan Locke
 * @author Chris Turner
 * @author Eelco Hillenius
 * @author Juergen Donnerstag
 * @author Johan Compagner
 * @author Igor Vaynberg (ivaynberg)
 * @author Martijn Dashorst
 * @author James Carman
 */
public class SecuritySettings
{
	/**
	 * encryption key used by default crypt factory
	 */
	public static final String DEFAULT_ENCRYPTION_KEY = "WiCkEt-FRAMEwork";

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
	 * Gets the authorization strategy.
	 *
	 * @return Returns the authorizationStrategy.
	 */
	public IAuthorizationStrategy getAuthorizationStrategy()
	{
		return authorizationStrategy;
	}

	/**
	 * Note: Prints a warning to stderr if no factory was set and {@link #DEFAULT_ENCRYPTION_KEY} is
	 * used instead.
	 * 
	 * @return crypt factory used to generate crypt objects
	 */
	public synchronized ICryptFactory getCryptFactory()
	{
		if (cryptFactory == null)
		{
			cryptFactory = new KeyInSessionSunJceCryptFactory();
		}
		return cryptFactory;
	}

	/**
	 * Gets whether mounts should be enforced. If true, requests for mounted targets have to done
	 * through the mounted paths. If, for instance, a bookmarkable page is mounted to a path, a
	 * request to that same page via the bookmarkablePage parameter will be denied.
	 *
	 * @return Whether mounts should be enforced
	 */
	public boolean getEnforceMounts()
	{
		return enforceMounts;
	}

	/**
	 * @return The listener
	 * @see IUnauthorizedComponentInstantiationListener
	 */
	public IUnauthorizedComponentInstantiationListener getUnauthorizedComponentInstantiationListener()
	{
		return unauthorizedComponentInstantiationListener;
	}

	/**
	 * Sets the authorization strategy.
	 *
	 * @param strategy
	 *            new authorization strategy
	 * @return {@code this} object for chaining
	 */
	public SecuritySettings setAuthorizationStrategy(IAuthorizationStrategy strategy)
	{
		Args.notNull(strategy, "strategy");
		authorizationStrategy = strategy;
		return this;
	}

	/**
	 * Sets the factory that will be used to create crypt objects. The crypt object returned from
	 * the first call is cached.
	 *
	 * @param cryptFactory
	 * @return {@code this} object for chaining
	 */
	public SecuritySettings setCryptFactory(ICryptFactory cryptFactory)
	{
		Args.notNull(cryptFactory, "cryptFactory");
		this.cryptFactory = cryptFactory;
		return this;
	}

	/**
	 * Sets whether mounts should be enforced. If true, requests for mounted targets have to done
	 * through the mounted paths. If, for instance, a bookmarkable page is mounted to a path, a
	 * request to that same page via the bookmarkablePage parameter will be denied.
	 *
	 * @param enforce
	 *            Whether mounts should be enforced
	 * @return {@code this} object for chaining
	 */
	public SecuritySettings setEnforceMounts(boolean enforce)
	{
		enforceMounts = enforce;
		return this;
	}

	/**
	 * @param listener
	 *            The listener to set
	 * @see IUnauthorizedComponentInstantiationListener
	 * @return {@code this} object for chaining
	 */
	public SecuritySettings setUnauthorizedComponentInstantiationListener(
		IUnauthorizedComponentInstantiationListener listener)
	{
		this.unauthorizedComponentInstantiationListener = listener == null ?
				DEFAULT_UNAUTHORIZED_COMPONENT_INSTANTIATION_LISTENER :
				listener;
		return this;
	}

	/**
	 * @return The listener that will be used when a request to an IResource is not allowed for some reason
	 */
	public IUnauthorizedResourceRequestListener getUnauthorizedResourceRequestListener()
	{
		return unauthorizedResourceRequestListener;
	}

	/**
	 * Sets a listener that will be used when a request to an IResource is not allowed for some reason
	 *
	 * @param listener
	 *          The listener
	 * @return {@code this} object for chaining
	 */
	public SecuritySettings setUnauthorizedResourceRequestListener(IUnauthorizedResourceRequestListener listener)
	{
		this.unauthorizedResourceRequestListener = listener == null ?
				DEFAULT_UNAUTHORIZED_RESOURCE_REQUEST_LISTENER :
				listener;
		return this;
	}

	/**
	 * Gets the authentication strategy.
	 *
	 * @return Returns the authentication strategy.
	 */
	public IAuthenticationStrategy getAuthenticationStrategy()
	{
		if (authenticationStrategy == null)
		{
			authenticationStrategy = new DefaultAuthenticationStrategy("LoggedIn");
		}
		return authenticationStrategy;
	}

	/**
	 * Sets the authentication strategy.
	 *
	 * @param strategy
	 *            new authentication strategy
	 * @return {@code this} object for chaining
	 */
	public SecuritySettings setAuthenticationStrategy(final IAuthenticationStrategy strategy)
	{
		authenticationStrategy = strategy;
		return this;
	}
}
