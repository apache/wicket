/*
 * $Id: ComponentRequestTarget.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May
 * 2006) joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat, 20
 * May 2006) $
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
package wicket.request.target.component;

import wicket.Component;
import wicket.Page;
import wicket.RequestCycle;

/**
 * Default implementation of
 * {@link wicket.request.target.component.IComponentRequestTarget}. Target that
 * denotes a single component instance.
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
	public ComponentRequestTarget(final Component component)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("Argument 'component' must be not null");
		}

		this.component = component;
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(final RequestCycle requestCycle)
	{
		// Initialize temporary variables
		Page page = component.getPage();
		if (page != null)
		{
			page.startComponentRender(component);
		}

		// Let component render itself
		if (component instanceof Page)
		{
			// Use the default Page request target, if component is a Page
			new PageRequestTarget((Page)component).respond(requestCycle);
		}
		else
		{
			// Render the component
			component.renderComponent();
			
			if (page != null)
			{
				page.endComponentRender(component);
			}
		}
	}

	/**
	 * @see wicket.request.target.component.IComponentRequestTarget#getComponent()
	 */
	public final Component getComponent()
	{
		return component;
	}

	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(final RequestCycle requestCycle)
	{
	}

	/**
	 * @see wicket.IRequestTarget#getLock(RequestCycle)
	 */
	public Object getLock(final RequestCycle requestCycle)
	{
		return requestCycle.getSession();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	@Override
	public boolean equals(final Object obj)
	{
		if (obj instanceof ComponentRequestTarget)
		{
			ComponentRequestTarget that = (ComponentRequestTarget)obj;
			return component.equals(that.component);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	@Override
	public int hashCode()
	{
		int result = "ComponentRequestTarget".hashCode();
		result += component.hashCode();
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[ComponentRequestTarget@" + hashCode() + " " + component + "]";
	}
}