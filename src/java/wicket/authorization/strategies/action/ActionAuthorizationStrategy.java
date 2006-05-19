/*
 * $Id: ActionAuthorizationStrategy.java,v 1.2 2006/01/12 23:57:32 jonathanlocke
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
package wicket.authorization.strategies.action;

import java.util.HashMap;
import java.util.Map;

import wicket.Component;
import wicket.authorization.Action;
import wicket.authorization.IAuthorizationStrategy;

/**
 * An authorization strategy which allows the use of a command pattern for users
 * that want to authorize a variety of different types of actions throughout an
 * application.
 * 
 * @author Jonathan Locke
 * @since Wicket 1.2
 */
public class ActionAuthorizationStrategy implements IAuthorizationStrategy
{
	/** Map from Action keys to IActionAuthorizer implementations. */
	private final Map<Action, IActionAuthorizer> actionAuthorizerForAction = new HashMap<Action, IActionAuthorizer>();

	/**
	 * Adds an action authorizer.
	 * 
	 * @param authorizer
	 *            The action authorizer to add
	 */
	public void addActionAuthorizer(IActionAuthorizer authorizer)
	{
		actionAuthorizerForAction.put(authorizer.getAction(), authorizer);
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
	 */
	public boolean isInstantiationAuthorized(Class componentClass)
	{
		return true;
	}

	/**
	 * @see wicket.authorization.IAuthorizationStrategy#isActionAuthorized(wicket.Component,
	 *      wicket.authorization.Action)
	 */
	public boolean isActionAuthorized(Component component, Action action)
	{
		IActionAuthorizer authorizer = actionAuthorizerForAction.get(action);
		if (authorizer != null)
		{
			return authorizer.authorizeAction(component);
		}
		return false;
	}
}
