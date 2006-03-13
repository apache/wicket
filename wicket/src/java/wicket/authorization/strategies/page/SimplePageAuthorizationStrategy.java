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

/**
 * A very simple authorization strategy that takes a supertype (a base class or
 * tagging interface) and performs a simple authorization check by calling the
 * abstract method isAuthorized() whenever a Page class that extends or
 * implements the supertype is about to be instantiated. If that method returns
 * true, page instantiation proceeds normally. If it returns false, the user is
 * automatically directed to the application's sign-in page for authentication,
 * which will presumably allow authorization to succeed once they have signed
 * in.
 * <p>
 * In your Application.init() method do something like the following:
 * 
 * <pre>
 * SimplePageAuthorizationStrategy authorizationStrategy = new SimplePageAuthorizationStrategy(
 * 		MySecureWebPage.class)
 * {
 * 	protected boolean isAuthorized()
 * 	{
 * 		// Authorize access based on user authentication in the session
 * 		return (((MySession)Session.get()).isSignedIn());
 * 	}
 * };
 * 
 * getSecuritySettings().setAuthorizationStrategy(authorizationStrategy);
 * </pre>
 * 
 * FIXME: General: Javadoc is out of date, we no longer have anywhere to set the
 * the login page, without that is this class even useful?
 * 
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
	 */
	public SimplePageAuthorizationStrategy(final Class securePageSuperType)
	{
		if (securePageSuperType == null)
		{
			throw new IllegalArgumentException("Secure page super type must not be null");
		}

		this.securePageSuperType = securePageSuperType;
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
