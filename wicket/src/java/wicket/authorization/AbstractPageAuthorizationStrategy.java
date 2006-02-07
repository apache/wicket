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
package wicket.authorization;

import wicket.Component;
import wicket.Page;
import wicket.RestartResponseAtSignInPageException;

/**
 * An abstract base class for implementing simple authorization of Pages.
 * 
 * @author Jonathan Locke
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
		if (componentClass.isAssignableFrom(Page.class))
		{
			if (!isAuthorized(componentClass))
			{
				throw new RestartResponseAtSignInPageException();
			}
		}
		return true;
	}

	/**
	 * @param componentClass
	 *            The Page class
	 * @return True if the user must authenticate
	 */
	protected boolean isAuthorized(Class/* <Page> */componentClass)
	{
		return false;
	}
}
