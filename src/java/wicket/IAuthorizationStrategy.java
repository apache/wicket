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

package wicket;


/**
 * TODO docme
 * 
 * @author Eelco Hillenius
 */
public interface IAuthorizationStrategy
{
	/**
	 * Gets whether the given component may be rendered.
	 * 
	 * @param c
	 *            the component
	 * @return whether the given component may be rendered
	 */
	boolean allowRender(Component c);

	/**
	 * Checks whether the given component may be created.
	 * 
	 * @param c
	 *            the component
	 * @return whether the given component may be created
	 */
	boolean allowCreation(Component c);
}
