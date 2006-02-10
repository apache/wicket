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
package wicket.authorization.strategies.role.util;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

/**
 * Utility class for working with roles. The string argument is a comma
 * seperated list, like ADMIN,USER. This is then returned by this wrapper is an
 * array.
 * 
 * @author Eelco Hillenius
 */
public final class Roles implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** roles as a string. */
	private final String roles;

	/** roles put in set as seperate strings. */
	private final HashSet<String> roleSet = new HashSet<String>();

	/**
	 * Construct.
	 * 
	 * @param roles
	 *            roles as a comma separated list, like "ADMIN,USER"
	 */
	public Roles(final String roles)
	{
		this.roles = roles;
		for (String role : roles.split(","))
		{
			this.roleSet.add(role);
		}
	}

	/**
	 * Returns the original role string
	 * 
	 * @return the roles as a comma seperated list
	 */
	public String getRoles()
	{
		return roles;
	}
	
	/**
	 * @return The set of roles
	 */
	public Set<String> getRoleSet()
	{
		return roleSet;
	}

	/**
	 * Whether this roles object containes the provided role.
	 * 
	 * @param role
	 *            the role to check
	 * @return true if it contains the role, false otherwise
	 */
	public boolean hasRole(final String role)
	{
		if (role != null)
		{
			return roleSet.contains(role);
		}
		return false;
	}

	/**
	 * Whether this roles object contains any of the provided roles.
	 * 
	 * @param roles
	 *            the roles to check
	 * @return true if it contains any of the roles, false otherwise
	 */
	public boolean hasAnyRole(Set<String> roles)
	{
		if (roles != null)
		{
			for (String role : roles)
			{
				if (hasRole(role))
				{
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Whether this roles object contains all the provided roles.
	 * 
	 * @param roles
	 *            the roles to check
	 * @return true if it contains all the roles or the provided roles object is
	 *         null, false otherwise
	 */
	public boolean hasAllRoles(Set<String> roles)
	{
		if (roles != null)
		{
			for (String role : roles)
			{
				if (!hasRole(role))
				{
					return false;
				}
			}
		}
		return true;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return roleSet.toString();
	}
}
