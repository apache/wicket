/*
 * $Id$ $Revision$
 * $Date$
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

import java.io.Serializable;

import wicket.authorization.strategies.role.Roles;

/**
 * Simple user object.
 * 
 * @author Eelco Hillenius
 */
public class User implements Serializable
{
	private final String uid;
	private final Roles roles;

	/**
	 * Construct.
	 * 
	 * @param uid
	 *            the unique user id
	 * @param roles
	 *            a comma seperated list of roles (e.g. USER,ADMIN)
	 */
	public User(String uid, String roles)
	{
		if (uid == null)
		{
			throw new IllegalArgumentException("uid must be not null");
		}
		if (roles == null)
		{
			throw new IllegalArgumentException("roles must be not null");
		}
		this.uid = uid;
		this.roles = new Roles(roles);
	}

	/**
	 * Whether this user has the given role.
	 * 
	 * @param role
	 * @return whether this user has the given role
	 */
	public boolean hasRole(String role)
	{
		return this.roles.hasRole(role);
	}

	/**
	 * Whether this user has any of the given roles.
	 * 
	 * @param roles set of roles
	 * @return whether this user has any of the given roles
	 */
	public boolean hasAnyRole(Roles roles)
	{
		return this.roles.hasAnyRole(roles);
	}

	/**
	 * Gets the uid.
	 * 
	 * @return the uid
	 */
	public String getUid()
	{
		return uid;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return uid + " " + roles;
	}
}
