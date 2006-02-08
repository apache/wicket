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
package wicket.authorization.strategies.role;

import java.io.Serializable;
import java.util.HashSet;
import java.util.Set;

import wicket.authorization.Action;

/**
 * Maps roles and an action.
 * 
 * @author Eelco Hillenius
 */
public class AuthorizedAction implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** the auth action. */
	private final Action action;

	/** the roles for this action. */
	private Set<String> roles = new HashSet<String>();

	/**
	 * Construct.
	 * 
	 * @param action
	 *            the auth action
	 * @param roles
	 *            the roles
	 */
	public AuthorizedAction(String action, String[] roles)
	{
		this(new Action(action), roles);
	}

	/**
	 * Construct.
	 * 
	 * @param action
	 *            the auth action
	 * @param roles
	 *            the roles
	 */
	public AuthorizedAction(Action action, String[] roles)
	{
		if (action == null)
		{
			throw new IllegalArgumentException("argument action must be not null");
		}
		if (roles == null)
		{
			throw new IllegalArgumentException("argument roles must be not null");
		}

		this.action = action;
		addToSet(roles);
	}

	/**
	 * Add roles to this authorized action.
	 * 
	 * @param roles
	 *            the roles to add
	 */
	public final void add(String[] roles)
	{
		addToSet(roles);
	}

	/**
	 * Gets action.
	 * 
	 * @return action
	 */
	public final Action getAction()
	{
		return action;
	}

	/**
	 * Gets roles.
	 * 
	 * @return roles
	 */
	public final String[] getRoles()
	{
		return roles.toArray(new String[roles.size()]);
	}

	private final void addToSet(String[] roles)
	{
		if (roles == null)
		{
			throw new IllegalArgumentException("argument null must be not null");
		}

		for (String role : roles)
		{
			this.roles.add(role);
		}
	}
}
