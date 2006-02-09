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
package wicket.authorization.strategies.role.metadata;

import java.util.HashMap;
import java.util.Map;

/**
 * Groups a set (technically a list) of actions for authorization.
 * 
 * TODO expand docs and give an example
 * 
 * @author Eelco Hillenius
 */
public class AuthorizedActions
{
	private Map<String, AuthorizedAction> actions = new HashMap<String, AuthorizedAction>();

	/**
	 * Construct.
	 */
	public AuthorizedActions()
	{
	}

	/**
	 * Add an action to this list of actions.
	 * 
	 * @param action
	 */
	public void add(AuthorizedAction action)
	{
		AuthorizedAction a = get(action.getAction().toString());
		if (a != null)
		{
			a.add(action.getRoles());
		}
		else
		{
			actions.put(action.getAction().toString(), action);
		}
	}

	/**
	 * Gets the action object for the provided action
	 * 
	 * @param action
	 *            the action
	 * @return the action object or null if none found
	 */
	public AuthorizedAction get(String action)
	{
		return actions.get(action);
	}

	/**
	 * The actions that are allowed.
	 * 
	 * @return the allowed actions
	 */
	public AuthorizedAction[] actions()
	{
		return actions.values().toArray(new AuthorizedAction[actions.size()]);
	}

	/**
	 * Convenience method for getting the roles for the provided action.
	 * 
	 * @param action
	 *            the action
	 * @return the roles for the provided action or an empty array if none;
	 *         never null
	 */
	public String[] roles(String action)
	{
		AuthorizedAction a = actions.get(action);
		return (a != null) ? a.getRoles() : new String[] {};
	}
}
