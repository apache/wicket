/*
 * $Id: IActionAuthorizer.java 4239 2006-02-09 06:51:31 +0000 (Thu, 09 Feb 2006)
 * jonathanlocke $ $Revision$ $Date: 2006-02-09 06:51:31 +0000 (Thu, 09
 * Feb 2006) $
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

import java.io.Serializable;

import wicket.Component;
import wicket.authorization.Action;

/**
 * A way to provide authorization for a specific component action.
 * 
 * @author Jonathan Locke
 * @since 1.2
 */
public interface IActionAuthorizer extends Serializable
{
	/**
	 * Gets the action that this authorizer authorizes.
	 * 
	 * @return The action that this authorizer authorizes
	 */
	Action getAction();

	/**
	 * Gets whether this action is authorized.
	 * 
	 * @param component
	 *            The component to authorize this action on
	 * @return True if this action is authorized
	 */
	boolean authorizeAction(Component component);
}
