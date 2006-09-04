/*
 * $Id: IAuthorizationStrategy.java 4415 2006-02-13 10:31:58 +0000 (Mon, 13 Feb
 * 2006) jonathanlocke $ $Revision$ $Date: 2006-02-13 10:31:58 +0000
 * (Mon, 13 Feb 2006) $
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
		 * @see wicket.authorization.IAuthorizationStrategy#isInstantiationAuthorized(java.lang.Class)
		 */
		public boolean isInstantiationAuthorized(final Class<? extends Component> c)
		{
			return true;
		}

		/**
		 * @see wicket.authorization.IAuthorizationStrategy#isActionAuthorized(wicket.Component,
		 *      wicket.authorization.Action)
		 */
		public boolean isActionAuthorized(Component c, Action action)
		{
			return true;
		}
	};

	/**
	 * Checks whether an instance of the given component class may be created.
	 * If this method returns false, an
	 * {@link UnauthorizedInstantiationException} will be thrown by the
	 * framework.
	 * <p>
	 * If you wish to implement a strategy that authenticates users which cannot
	 * access a given Page (or other Component), you can simply throw a
	 * {@link wicket.RestartResponseAtInterceptPageException} in your
	 * implementation of this method.
	 * 
	 * @param componentClass
	 *            The component class to check
	 * @return Whether the given component may be created
	 */
	boolean isInstantiationAuthorized(Class<? extends Component> componentClass);

	/**
	 * Gets whether the given action is permitted. If it is, this method should
	 * return true. If it isn't, this method should either return false or - in
	 * case of a serious breach - throw a security exception. Returning is
	 * generally preferable over throwing an exception as that doesn't break the
	 * normal flow.
	 * 
	 * @param component
	 *            The component to be acted upon
	 * @param action
	 *            The action to authorize on the component
	 * @return Whether the given action may be taken on the given component
	 * @throws AuthorizationException
	 *             Can be thrown by implementation if action is unauthorized
	 * @see Component#ENABLE
	 * @see Component#RENDER
	 */
	boolean isActionAuthorized(Component component, Action action);
}
