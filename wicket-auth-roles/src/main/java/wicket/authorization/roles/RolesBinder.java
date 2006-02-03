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

import wicket.Component;
import wicket.MetaDataKey;
import wicket.authorization.Action;

/**
 * Binds roles to components.
 * 
 * @author Eelco Hillenius
 */
public class RolesBinder
{
	/**
	 * Component meta data key for roles information.
	 */
	public static final MetaDataKey ROLES_METADATA_KEY = new MetaDataKey(RolesBinder.class)
	{
	};

	/**
	 * Construct.
	 */
	public RolesBinder()
	{
	}

	/**
	 * Bind the given action and roles to the component.
	 * 
	 * @param c
	 *            the component to bind the action and roles to
	 * @param action
	 *            the action to bind
	 * @param roles
	 *            the roles to bind
	 */
	public static final void bind(Component c, Action action, String[] roles)
	{
		AuthorizedAction actionRoles = (AuthorizedAction)c.getMetaData(ROLES_METADATA_KEY);
		if (actionRoles == null)
		{
			actionRoles = new AuthorizedAction(action, roles);
			c.setMetaData(ROLES_METADATA_KEY, actionRoles);
		}
		else
		{
			
		}
	}
}
