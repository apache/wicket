/*
 * $Id: RequestListenerInterface.java,v 1.3 2006/02/13 02:15:14 jonathanlocke
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
package wicket.authentication;

import wicket.authorization.strategies.role.Roles;

/**
 * Interface to get roles that have been authenticated for this session
 * 
 * @author Jonathan Locke
 */
public interface IAuthenticatedRoles
{
	/**
	 * @return The roles this user can play
	 */
	public Roles getRoles();
}
