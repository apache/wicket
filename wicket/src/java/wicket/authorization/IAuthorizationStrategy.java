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
 * Authorization strategies control on a low level how authorization is implied.
 * A strategy itself is generally not responsible for enforcing it. What a
 * strategy does do is tell the framework whether a component may be created
 * (especialy usefull for implementing secure bookmarkable pages and components
 * that should always be secure no matter what environment they are put it),
 * whether it may be rendered (which can result in components being 'filtered'
 * from a page, or triggering the page to not be rendered at all and redirecting
 * to a error or logon page), and whether a component may be enabled (thus
 * controlling it's reachability etc) at all.
 * 
 * @author Eelco Hillenius
 */
public interface IAuthorizationStrategy
{
	/**
	 * Implementation of {@link IAuthorizationStrategy} that allows everything.
	 */
	public static final IAuthorizationStrategy ALLOW_ALL = new IAuthorizationStrategy()
	{
		/**
		 * @return true allways
		 */
		public boolean allowCreateComponent(Class c)
		{
			return true;
		}

		/**
		 * @return true allways
		 */
		public boolean allowRender(Component c)
		{
			return true;
		}

		/**
		 * @return true allways
		 */
		public boolean allowEnabled(Component c)
		{
			return true;
		}
	};

	/**
	 * Checks whether an instance of the given component class may be created.
	 * If this method returns false, a {@link AuthorizationException} is thrown
	 * during construction.
	 * 
	 * @param c
	 *            the component to check for
	 * @return whether the given component may be created
	 */
	boolean allowCreateComponent(Class c);

	/**
	 * Gets whether the given component may be rendered. If this method returns
	 * false, the component is not rendered, and neither are it's children.
	 * <p>
	 * There are two uses for this method:
	 * <ul>
	 * <li>The 'normal' use is for controling whether a component is rendered
	 * without having any effect on the rest of the processing. If a strategy
	 * lats this method return 'false', then the target component and it's
	 * children will not be rendered in the same fashion as when that component
	 * would have had it's visibility property 'false'.</li>
	 * <li>The other use is when a component should block the rendering of the
	 * whole page. So instead of 'hiding' a component, what we generally want to
	 * acchieve here is that we force the user to logon/ give credentials for a
	 * higher level of authorization. For this functionality, the strategy
	 * implementation should throw a {@link AuthorizationException}, which will
	 * then be handled further with the framework.</li>
	 * </ul>
	 * </p>
	 * 
	 * @param c
	 *            the component to check for
	 * @return whether the given component may be rendered
	 * @throws AuthorizationException
	 *             in case the rendering is not allowed, and when it should
	 *             block the whole page from being rendered
	 */
	boolean allowRender(Component c);

	/**
	 * <p>
	 * Gets whether a component is allowed to be enabled. If this method returns
	 * true, a component may decide by itself (typically using it's enabled
	 * property) whether it is enabled or not. If this method returns false, the
	 * passed component is marked disabled, regardless it's enabled property.
	 * </p>
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
	 * will catch all normal use though.
	 * 
	 * </p>
	 * 
	 * @param c
	 *            the component to check for
	 * @return whether a component is allowed to be enabled
	 */
	boolean allowEnabled(Component c);
}
