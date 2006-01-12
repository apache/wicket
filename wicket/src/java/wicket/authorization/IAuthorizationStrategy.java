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

package wicket.authorization;

import wicket.Component;

/**
 * Authorization strategies specify aspect-like constraints on significant
 * actions taken by the framework in a given application. These constraints are
 * guaranteed by the framework to be applied consistently throughout. Violations
 * will result in a security action directed by the strategy, such as the
 * throwing of an AuthorizationException or the filtering out of
 * security-sensitive information.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * @since Wicket 1.2
 */
public interface IAuthorizationStrategy
{
	/**
	 * Implementation of {@link IAuthorizationStrategy} that allows everything.
	 */
	public static final IAuthorizationStrategy ALLOW_ALL = new IAuthorizationStrategy()
	{
		/**
		 * @see wicket.authorization.IAuthorizationStrategy#authorizeInstantiation(java.lang.Class)
		 */
		public boolean authorizeInstantiation(final Class c)
		{
			return true;
		}

		/**
		 * @see wicket.authorization.IAuthorizationStrategy#authorizeAction(wicket.Component,
		 *      wicket.authorization.Action)
		 */
		public boolean authorizeAction(Component c, Action action)
		{
			return true;
		}
	};

	/**
	 * Checks whether an instance of the given component class may be created.
	 * If this method returns false, a {@link AuthorizationException} is thrown
	 * during construction.
	 * 
	 * @param componentClass
	 *            The component class to check
	 * @return Whether the given component may be created
	 */
	boolean authorizeInstantiation(Class componentClass);

	/**
	 * Gets whether the given action is permitted. If it is, this method should
	 * return true. If it isn't, this method should either return false or - in
	 * case of a serious breach - throw a security exception. Returning is
	 * generally preferable over throwing an exception as that doesn't break the
	 * normal flow.
	 * 
	 * @param component
	 *            The component to check for
	 * @param action
	 *            The action to check
	 * @return Whether the given component may be rendered
	 * @throws AuthorizationException
	 *             In case the action is not allowed, and when it should block
	 *             the whole page from being rendered
	 */
	boolean authorizeAction(Component component, Action action);
}
