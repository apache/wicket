/*
 * $Id: UserRolesAuthorizer.java 4289 2006-02-10 22:14:33 -0800 (Fri, 10 Feb
 * 2006) jonathanlocke $ $Revision$ $Date: 2006-02-10 22:14:33 -0800
 * (Fri, 10 Feb 2006) $
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
package wicket.authorization.strategies.role.example;

import wicket.authorization.strategies.role.IRoleCheckingStrategy;
import wicket.authorization.strategies.role.Roles;

/**
 * The authorizer we need to provide to the authorization strategy
 * implementation
 * {@link wicket.authorization.strategies.role.annotations.AnnotationsRoleAuthorizationStrategy}.
 * 
 * @author Eelco Hillenius
 */
public class UserRolesAuthorizer implements IRoleCheckingStrategy
{

	/**
	 * Construct.
	 */
	public UserRolesAuthorizer()
	{
	}

	/**
	 * @see wicket.authorization.strategies.role.IRoleCheckingStrategy#hasAnyRole(Roles)
	 */
	public boolean hasAnyRole(Roles roles)
	{
		return RolesSession.get().getUser().hasAnyRole(roles);
	}

}
