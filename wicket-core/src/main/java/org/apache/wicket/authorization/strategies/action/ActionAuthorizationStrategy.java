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
package org.apache.wicket.authorization.strategies.action;

import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.Component;
import org.apache.wicket.authorization.Action;
import org.apache.wicket.authorization.IAuthorizationStrategy;


/**
 * An authorization strategy which allows the use of a command pattern for users that want to
 * authorize a variety of different types of actions throughout an application.
 * 
 * @author Jonathan Locke
 * @since Wicket 1.2
 */
public class ActionAuthorizationStrategy extends IAuthorizationStrategy.AllowAllAuthorizationStrategy
{
	/** Map from Action keys to IActionAuthorizer implementations. */
	private final Map<Action, IActionAuthorizer> actionAuthorizerForAction = new HashMap<>();

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
	 * @see org.apache.wicket.authorization.IAuthorizationStrategy#isActionAuthorized(org.apache.wicket.Component,
	 *      org.apache.wicket.authorization.Action)
	 */
	@Override
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
