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
	 * @see wicket.authorization.IAuthorizationStrategy#isActionAuthorized(wicket.Component,
	 *      wicket.authorization.Action)
	 */
	public final boolean isActionAuthorized(final Component component, final Action action)
	{
		return true;
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
	 */
	public final boolean isInstantiationAuthorized(final Class/* <Component> */componentClass)
	{
		if (isPageClass(componentClass))
		{
			if (!isSignInPage(componentClass) && !isAuthorized(componentClass))
			{
				throw new RestartResponseAtSignInPageException();
			}
		}
		return true;
	}

	/**
	 * Works like instanceof operator where instanceOf(a, b) is the runtime
	 * equivalent of (a instanceof b).
	 * 
	 * @param type
	 *            The type to check
	 * @param superType
	 *            The interface or superclass that the type needs to implement
	 *            or extend
	 * @return True if the type is an instance of the superType
	 */
	protected boolean instanceOf(final Class type, final Class superType)
	{
		return superType.isAssignableFrom(type);
	}

	/**
	 * Whether to page may be created. Returns true by default.
	 * 
	 * @param pageClass
	 *            The Page class
	 * @return True if to page may be created
	 */
	protected boolean isAuthorized(Class/* <Page> */pageClass)
	{
		return true;
	}

	/**
	 * @param c
	 *            The class to check
	 * @return True if the class is derived from Page.
	 */
	protected boolean isPageClass(final Class c)
	{
		return instanceOf(c, Page.class);
	}

	/**
	 * Checks whether the provided class equals the singin page class of the
	 * current application.
	 * 
	 * @param componentClass
	 *            The Page class
	 * @return True if the page class equals the signin page class
	 */
	private final boolean isSignInPage(Class/* <Page> */componentClass)
	{
		return componentClass == Application.get().getApplicationSettings().getSignInPage();
	}
}