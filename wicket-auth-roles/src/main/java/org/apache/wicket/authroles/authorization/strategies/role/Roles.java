/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.authroles.authorization.strategies.role;

import java.util.HashSet;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.string.StringList;


/**
 * Utility class for working with roles.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class Roles extends HashSet<String> implements IClusterable
{
	private static final long serialVersionUID = 1L;

	/** USER role (for use in annotations) */
	public static final String USER = "USER";

	/** ADMIN role (for use in annotations) */
	public static final String ADMIN = "ADMIN";

	/**
	 * Construct.
	 */
	public Roles()
	{
	}

	/**
	 * Construct.
	 * 
	 * @param roles
	 *            Roles as a comma separated list, like "ADMIN, USER"
	 */
	public Roles(final String roles)
	{
		for (final String role : roles.split("\\s*,\\s*"))
		{
			add(role);
		}
	}

	/**
	 * Construct.
	 * 
	 * @param roles
	 *            Roles
	 */
	public Roles(final String[] roles)
	{
		for (final String role : roles)
		{
			add(role);
		}
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
			return contains(role);
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
	public boolean hasAnyRole(Roles roles)
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
	 * @return true if it contains all the roles or the provided roles object is null, false
	 *         otherwise
	 */
	public boolean hasAllRoles(Roles roles)
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
		return StringList.valueOf(this).join();
	}
}
