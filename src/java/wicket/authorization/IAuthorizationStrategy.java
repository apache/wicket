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
 * <p>
 * An authorization strategy tells the framework whether a component may be:
 * <p>
 * 1. Created - This is especially useful for implementing secure bookmarkable
 * pages and sensitive components that should always be secure no matter what
 * environment they are put in.
 * <p>
 * 2. Rendered - Components can be 'filtered' from a page, or the page may not
 * be rendered at all, redirecting to a error or logon page.
 * <p>
 * 3. Enabled - Components may be disabled, making them unavailable for user
 * interaction.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public interface IAuthorizationStrategy
{
	/**
	 * Implementation of {@link IAuthorizationStrategy} that allows everything.
	 */
	public static final IAuthorizationStrategy ALLOW_ALL = new IAuthorizationStrategy()
	{
		public boolean allowEnabledState(final Component c)
		{
			return true;
		}

		public boolean allowInstantiation(final Class c)
		{
			return true;
		}

		public boolean allowRender(final Component c)
		{
			return true;
		}
	};

	/**
	 * Gets whether a component is allowed to be enabled. If this method returns
	 * true, a component may decide by itself (typically using it's enabled
	 * property) whether it is enabled or not. If this method returns false, the
	 * passed component is marked disabled, regardless its enabled property.
	 * <p>
	 * When a component is not allowed to be enabled (in effect disabled through
	 * the implementation of this interface), Wicket will try to prevent model
	 * updates too. This is not completely fail safe, as constructs like:
	 * 
	 * <pre>
	 * User u = (User)getModelObject();
	 * u.setName(&quot;got you there!&quot;);
	 * </pre>
	 * 
	 * can't be prevented. Indeed it can be argued that any model protection is
	 * best dealt with in your model objects to be completely secured. Wicket
	 * will catch all normal framework-directed use though.
	 * 
	 * @param c
	 *            The component to check for
	 * @return Whether a component is allowed to be enabled
	 */
	boolean allowEnabledState(Component c);

	/**
	 * Checks whether an instance of the given component class may be created.
	 * If this method returns false, a {@link AuthorizationException} is thrown
	 * during construction.
	 * 
	 * @param componentClass
	 *            The component class to check
	 * @return Whether the given component may be created
	 */
	boolean allowInstantiation(Class componentClass);

	/**
	 * Gets whether the given component may be rendered. If this method returns
	 * false, the component is not rendered, and neither are it's children.
	 * <p>
	 * There are two uses for this method:
	 * <ul>
	 * <li>The 'normal' use is for controlling whether a component is rendered
	 * without having any effect on the rest of the processing. If a strategy
	 * lets this method return 'false', then the target component and its
	 * children will not be rendered, in the same fashion as if that component
	 * had visibility property 'false'.</li>
	 * <li>The other use is when a component should block the rendering of the
	 * whole page. So instead of 'hiding' a component, what we generally want to
	 * achieve here is that we force the user to logon/give-credentials for a
	 * higher level of authorization. For this functionality, the strategy
	 * implementation should throw a {@link AuthorizationException}, which will
	 * then be handled further by the framework.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param c
	 *            Whe component to check for
	 * @return Whether the given component may be rendered
	 * @throws AuthorizationException
	 *             In case the rendering is not allowed, and when it should
	 *             block the whole page from being rendered
	 */
	boolean allowRender(Component c);
}
