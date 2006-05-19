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
package wicket.authorization.strategies.page;

import wicket.Application;
import wicket.Component;
import wicket.Page;
import wicket.RestartResponseAtInterceptPageException;
import wicket.authorization.IUnauthorizedComponentInstantiationListener;
import wicket.authorization.UnauthorizedInstantiationException;

/**
 * A very simple authorization strategy that takes a supertype (a base class or
 * tagging interface) and performs a simple authorization check by calling the
 * abstract method isAuthorized() whenever a Page class that extends or
 * implements the supertype is about to be instantiated. If that method returns
 * true, page instantiation proceeds normally. If it returns false, the user is
 * automatically directed to the specified sign-in page for authentication,
 * which will presumably allow authorization to succeed once they have signed
 * in.
 * <p>
 * In your Application.init() method do something like the following:
 * 
 * <pre>
 * SimplePageAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
 * 		MySecureWebPage.class, MySignInPage.class)
 * {
 *     protected boolean isAuthorized()
 * 	   {
 * 		   // Authorize access based on user authentication in the session
 * 		   return (((MySession)Session.get()).isSignedIn());
 * 	   }
 * };
 * 
 * getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
 * </pre>
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public abstract class SimplePageAuthorizationStrategy extends AbstractPageAuthorizationStrategy
{
	/**
	 * The supertype (class or interface) of Pages that require authorization to
	 * be instantiated.
	 */
	private final Class securePageSuperType;

	/**
	 * Construct.
	 * 
	 * @param securePageSuperType
	 *            The class or interface supertype that indicates that a given
	 *            Page requires authorization
	 * @param signInPageClass
	 *            The sign in page class
	 */
	public SimplePageAuthorizationStrategy(final Class securePageSuperType,
			final Class signInPageClass)
	{
		if (securePageSuperType == null)
		{
			throw new IllegalArgumentException("Secure page super type must not be null");
		}

		this.securePageSuperType = securePageSuperType;

		// Handle unauthorized access to pages
		Application.get().getSecuritySettings().setUnauthorizedComponentInstantiationListener(
				new IUnauthorizedComponentInstantiationListener()
				{
					public void onUnauthorizedInstantiation(final Component component)
					{
						// If there is a sign in page class declared, and the
						// unauthorized component is a page, but it's not the
						// sign in page
						if (component instanceof Page)
						{
							// Redirect to page to let the user sign in
							throw new RestartResponseAtInterceptPageException(signInPageClass);
						}
						else
						{
							// The component was not a page, so throw exception
							throw new UnauthorizedInstantiationException(component.getClass());
						}
					}
				});
	}

	/**
	 * @see wicket.authorization.strategies.page.AbstractPageAuthorizationStrategy#isPageAuthorized(java.lang.Class)
	 */
	protected boolean isPageAuthorized(final Class pageClass)
	{
		if (instanceOf(pageClass, securePageSuperType))
		{
			return isAuthorized();
		}

		// Allow construction by default
		return true;
	}

	/**
	 * Gets whether the current user/session is authorized to instantiate a page
	 * class which extends or implements the supertype (base class or tagging
	 * interface) passed to the constructor.
	 * 
	 * @return True if the instantiation should be allowed to proceed. False, if
	 *         the user should be directed to the application's sign-in page.
	 */
	protected abstract boolean isAuthorized();
}
