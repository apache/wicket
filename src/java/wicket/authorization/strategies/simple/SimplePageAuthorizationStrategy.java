/*
 * $Id: SimplePageAuthorizationStrategy.java,v 1.1 2006/02/09 01:04:49 eelco12
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
package wicket.authorization.strategies.simple;

import wicket.authorization.AbstractPageAuthorizationStrategy;


/**
 * Very simple authorization strategy that takes a base class or tagging interface class argument and
 * to decide whether an authorization check should be performed.
 * 
 * Use like:
 * 
 * <pre>
 * //... (in your Application class), method init()) ...
 * // create a simple authorization strategy
 * SimplePageAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy()
 * {
 * 	protected boolean isAuthorized()
 * 	{
 * 		// check whether the user is logged on
 * 		return (((LibrarySession)Session.get()).isSignedIn());
 * 	}
 * };
 * // all pages of type AuthenticatedWebPage must be checked for
 * // authorization
 * authorizationStrategy.add(AuthenticatedWebPage.class);
 * 
 * // set the strategy
 * getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
 * </pre>
 * 
 * @author Eelco Hillenius
 */
public abstract class SimplePageAuthorizationStrategy extends AbstractPageAuthorizationStrategy
{
	/**
	 * Base class or interface of classes that need authentication.
	 */
	private final Class needsAuthorization;

	/**
	 * Construct.
	 * 
	 * @param needsAuthorization
	 *            the class
	 */
	public SimplePageAuthorizationStrategy(Class needsAuthorization)
	{
		if (needsAuthorization == null)
		{
			throw new IllegalArgumentException("argument clazz must be not null");
		}

		this.needsAuthorization = needsAuthorization;
	}

	/**
	 * @see wicket.authorization.AbstractPageAuthorizationStrategy#isAuthorized(java.lang.Class)
	 */
	protected boolean isAuthorized(Class componentClass)
	{
		if (needsAuthorization.isAssignableFrom(componentClass))
		{
			return isAuthorized();
		}

		// allow construction by default
		return true;
	}

	/**
	 * Gets whether the current 'user' (or whatever context object is choosen)
	 * is sufficiently authenticated.
	 * 
	 * @return true if the user is authorization, false if he/ she should be
	 *         redirected to the application's login page
	 */
	protected abstract boolean isAuthorized();
}
