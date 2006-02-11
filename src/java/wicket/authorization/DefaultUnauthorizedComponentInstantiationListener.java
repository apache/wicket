/*
 * $Id: ComponentInstantiationAuthorizer.java,v 1.4 2006/02/11 07:31:12 eelco12
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
package wicket.authorization;

import wicket.Application;
import wicket.Component;
import wicket.Page;
import wicket.RestartResponseAtInterceptPageException;

/**
 * Default handling of unauthorized component instantiations.
 * 
 * @author Jonathan Locke
 */
public class DefaultUnauthorizedComponentInstantiationListener
		implements
			IUnauthorizedComponentInstantiationListener
{
	/**
	 * Called when an unauthorized component instantiation is about to take
	 * place (but before it happens).
	 * 
	 * @param component
	 *            The partially constructed component (only the id is guaranteed
	 *            to be valid).
	 */
	public void onUnauthorizedInstantiation(final Component component)
	{
		// Get sign-in page class
		final Class signInPageClass = Application.get().getSecuritySettings().getSignInPage();

		// If there is a sign in page class declared, and the unauthorized
		// component is a page, but it's not the sign in page
		if (signInPageClass != null && component instanceof Page
				&& signInPageClass != component.getClass())
		{
			// Redirect to intercept page to let the user sign in
			throw new RestartResponseAtInterceptPageException(signInPageClass);
		}
		else
		{
			// The component was not a page, so throw an exception
			throw new UnauthorizedInstantiationException(component.getClass());
		}
	}
}
