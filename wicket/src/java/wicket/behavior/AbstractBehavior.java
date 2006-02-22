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
package wicket.behavior;

import wicket.Component;
import wicket.markup.ComponentTag;

/**
 * Adapter implementation of {@link wicket.behavior.IBehavior}. This class just
 * implements the interface with empty methods; override this class when you
 * do not want to implement the whole interface.
 * 
 * @author Ralf Ebert
 * @author Eelco Hillenius
 */
public abstract class AbstractBehavior implements IBehavior
{
	/**
	 * Construct.
	 */
	public AbstractBehavior()
	{
	}

	/**
	 * @see wicket.behavior.IBehavior#bind(wicket.Component)
	 */
	public void bind(final Component hostComponent)
	{
	}

	/**
	 * @see wicket.behavior.IBehavior#detachModel()
	 */
	public void detachModel()
	{
	}

	/**
	 * @see wicket.behavior.IBehavior#onComponentTag(wicket.Component,
	 *      wicket.markup.ComponentTag)
	 */
	public void onComponentTag(final Component component, final ComponentTag tag)
	{
	}

	/**
	 * @see wicket.behavior.IBehavior#rendered(wicket.Component)
	 */
	public void rendered(final Component hostComponent)
	{
	}
}
