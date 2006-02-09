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
package wicket.authorization.strategies.role.metadata;

import wicket.Component;
import wicket.authorization.Action;
import wicket.authorization.strategies.role.AbstractRoleAuthorizationStrategy;

/**
 * Strategy that uses the Wicket meta data facility to check authorization.
 * 
 * @see wicket.authorization.strategies.role.metadata.RoleBinder
 * @see wicket.MetaDataKey
 * 
 * @author Eelco Hillenius
 */
public class MetaDataRoleAuthorizationStrategy extends AbstractRoleAuthorizationStrategy
{
	/**
	 * Construct.
	 * 
	 * @param rolesAuthorizer
	 *            the authorizer object
	 */
	public MetaDataRoleAuthorizationStrategy(IRoleAuthorizer rolesAuthorizer)
	{
		super(rolesAuthorizer);
	}

	/**
	 * Uses application level meta data to match roles for component
	 * instantiation.
	 * 
	 * @see wicket.authorization.IAuthorizationStrategy#authorizeInstantiation(java.lang.Class)
	 */
	public boolean authorizeInstantiation(Class componentClass)
	{
		if (componentClass == null)
		{
			throw new IllegalArgumentException("argument componentClass has to be not null");
		}

		String[] roles = RoleBinder.rolesForCreation(componentClass);
		return any(roles);
	}

	/**
	 * Uses component level meta data to match roles for component action
	 * execution.
	 * 
	 * @see wicket.authorization.IAuthorizationStrategy#authorizeAction(wicket.Component,
	 *      wicket.authorization.Action)
	 */
	public boolean authorizeAction(Component component, Action action)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("argument component has to be not null");
		}
		if (action == null)
		{
			throw new IllegalArgumentException("argument action has to be not null");
		}

		String[] roles = RoleBinder.rolesForAction(component, action);
		return any(roles);
	}
}
