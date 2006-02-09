/*
 * $Id: CompoundAuthorizationStrategy.java,v 1.6 2006/01/14 22:45:04
 * jonathanlocke Exp $ $Revision$ $Date$
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
import wicket.RestartResponseAtSignInPageException;
import wicket.authorization.Action;
import wicket.authorization.IAuthorizationStrategy;

/**
 * An abstract base class for implementing simple authorization of Pages. Users
 * should override {@link #isAuthorized(Class)}, which gets called for Page
 * classes when they are being constructed. Either return true to permit page
 * construction, or false to deny it and redirect to the signin page which comes
 * from {@link wicket.settings.IApplicationSettings#getSignInPage()}.
 * 
 * @author Jonathan Locke
 * @author Eelco Hillenius
 */
public abstract class AbstractPageAuthorizationStrategy implements IAuthorizationStrategy
{
	/**
	 * @see wicket.authorization.IAuthorizationStrategy#authorizeAction(wicket.Component,
	 *      wicket.authorization.Action)
	 */
	public final boolean authorizeAction(Component component, Action action)
	{
		return true;
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#authorizeInstantiation(java.lang.Class)
	 */
	public final boolean authorizeInstantiation(Class/* <Component> */componentClass)
	{
		if (Page.class.isAssignableFrom(componentClass))
		{
			if (!isSigninPage(componentClass) && !isAuthorized(componentClass))
			{
				throw new RestartResponseAtSignInPageException();
			}
		}
		return true;
	}

	/**
	 * Whether to page may be created. Returns true by default.
	 * 
	 * @param componentClass
	 *            The Page class
	 * @return True if to page may be created
	 */
	protected boolean isAuthorized(Class/* <Page> */componentClass)
	{
		return true;
	}

	/**
	 * Checks whether the provided class equals the singin page class of the
	 * current application.
	 * 
	 * @param componentClass
	 *            The Page class
	 * @return True if the page class equals the signin page class
	 */
	private final boolean isSigninPage(Class/* <Page> */componentClass)
	{
		return componentClass == Application.get().getApplicationSettings().getSignInPage();
	}
}