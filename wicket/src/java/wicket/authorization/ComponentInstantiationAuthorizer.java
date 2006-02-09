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

import wicket.Component;
import wicket.Session;
import wicket.application.IComponentInstantiationListener;

/**
 * Calls the application's (or session's)
 * {@link wicket.authorization.IAuthorizationStrategy} to find out whether the
 * component in question may be instantiated.
 * 
 * @author Eelco Hillenius
 */
public class ComponentInstantiationAuthorizer implements IComponentInstantiationListener
{
	/**
	 * Construct.
	 */
	public ComponentInstantiationAuthorizer()
	{
	}

	/**
	 * @see wicket.application.IComponentInstantiationListener#onInstantiation(wicket.Component)
	 */
	public void onInstantiation(Component component)
	{
		if (!Session.get().getAuthorizationStrategy().authorizeInstantiation(component.getClass()))
		{
			throw new UnauthorizedInstantiationException(component.getClass());
		}
	}
}
