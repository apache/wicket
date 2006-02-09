/*
 * $Id$
 * $Revision$ $Date$
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
package wicket.authorization;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import wicket.Page;
import wicket.util.lang.PackageName;

/**
 * Very simple authorization strategy that takes a set of base classes and
 * packages to decide whether an authorization check should be performed. If a
 * page is about to be constructed of which it's class doesn't match with the
 * set of classes/ pages, creation is allowed and no authorization check will be
 * performed. If the class does match, method {@link #isAuthorized()} is called.
 * If that method call returns false, the application's sign in page ({@link wicket.settings.IApplicationSettings#getSignInPage()})
 * is show. If it returns true, nothing happens
 * 
 * @author Eelco Hillenius
 */
public abstract class SimplePageAuthorizationStrategy extends AbstractPageAuthorizationStrategy
{
	/**
	 * Set of classes that need authentication.
	 */
	private Set/* <Class> */classes = new HashSet();

	/**
	 * Set of packages that need authentication.
	 */
	private Set/* <PackageName> */packages = new HashSet();

	/**
	 * Adds a Page class for which authorization should be performed.
	 * 
	 * @param clazz
	 *            the page class
	 */
	public void add(Class clazz)
	{
		if (clazz == null)
		{
			throw new IllegalArgumentException("argument clazz must be not null");
		}
		if (!Page.class.isAssignableFrom(clazz))
		{
			throw new IllegalArgumentException("argument clazz must be of type "
					+ Page.class.getName());
		}

		classes.add(clazz);
	}

	/**
	 * Adds a Package for which authorization should be performed.
	 * 
	 * @param packageName
	 *            the package
	 */
	public void add(PackageName packageName)
	{
		if (packageName == null)
		{
			throw new IllegalArgumentException("argument packageName must be not null");
		}

		packages.add(packageName);
	}

	/**
	 * @see wicket.authorization.AbstractPageAuthorizationStrategy#isAuthorized(java.lang.Class)
	 */
	protected boolean isAuthorized(Class componentClass)
	{
		// check classes
		for (Iterator i = classes.iterator(); i.hasNext();)
		{
			Class clazz = (Class)i.next();
			if (clazz.isAssignableFrom(componentClass))
			{
				return isAuthorized();
			}
		}

		// check packages
		Package pkg = componentClass.getPackage();
		if (pkg != null)
		{
			for (Iterator i = packages.iterator(); i.hasNext();)
			{
				PackageName packageName = (PackageName)i.next();
				if (packageName.equals(PackageName.forPackage(pkg)))
				{
					return isAuthorized();
				}
			}
		}

		// no match found: allow construction
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
