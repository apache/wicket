/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.request;

import wicket.Component;
import wicket.RequestCycle;

/**
 * Default implementation of {@link wicket.request.IComponentRequestTarget}.
 * Target that denotes a single component instance.
 * 
 * @author Eelco Hillenius
 */
public class ComponentRequestTarget implements IComponentRequestTarget
{
	/** the component instance. */
	private final Component component;

	/**
	 * Construct.
	 * 
	 * @param component
	 *            the component instance
	 */
	public ComponentRequestTarget(Component component)
	{
		if (component == null)
		{
			throw new NullPointerException("argument component must be not null");
		}

		this.component = component;
	}

	/**
	 * @see wicket.request.IComponentRequestTarget#getComponent()
	 */
	public final Component getComponent()
	{
		return component;
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		// Let page render itself
		component.render();
	}

	/**
	 * @see wicket.IRequestTarget#cleanUp(wicket.RequestCycle)
	 */
	public void cleanUp(RequestCycle requestCycle)
	{
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return component.toString();
	}

}