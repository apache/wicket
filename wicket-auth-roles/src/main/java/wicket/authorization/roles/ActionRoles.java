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
package wicket.authorization.roles;

import java.io.Serializable;

import wicket.authorization.Action;

/**
 * Maps roles and an action.
 * 
 * @author Eelco Hillenius
 */
final class ActionRoles implements Serializable
{
	private static final long serialVersionUID = 1L;

	/** the auth action. */
	private final Action action;

	/** the roles for this action. */
	private final String[] roles;

	/**
	 * Construct.
	 * 
	 * @param action
	 *            the auth action
	 * @param roles
	 *            the roles
	 */
	public ActionRoles(Action action, String[] roles)
	{
		this.action = action;
		this.roles = roles;
	}

	/**
	 * Gets action.
	 * 
	 * @return action
	 */
	public Action getAction()
	{
		return action;
	}

	/**
	 * Gets roles.
	 * 
	 * @return roles
	 */
	public String[] getRoles()
	{
		return roles;
	}

}
