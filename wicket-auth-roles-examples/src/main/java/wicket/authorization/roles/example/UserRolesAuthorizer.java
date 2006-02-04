/*
 * $Id$ $Revision$ $Date$
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
package wicket.authorization.roles.example;

import wicket.Session;
import wicket.authorization.roles.IRolesAuthorizer;

/**
 * The authorizer we need to provide to the authorization strategy
 * implementation
 * {@link wicket.authorization.roles.annot.RolesAnnotAuthorizationStrategy}.
 * 
 * @author Eelco Hillenius
 */
public class UserRolesAuthorizer implements IRolesAuthorizer
{

	/**
	 * Construct.
	 */
	public UserRolesAuthorizer()
	{
	}

	/**
	 * @see wicket.authorization.roles.IRolesAuthorizer#any(java.lang.String[])
	 */
	public boolean any(String[] roles)
	{
		RolesAuthSession authSession = (RolesAuthSession)Session.get();
		return authSession.getUser().hasAnyRole(roles);
	}

}
