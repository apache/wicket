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
package wicket.request.target;

import java.lang.reflect.Method;

import wicket.Component;
import wicket.IRequestTarget;
import wicket.Page;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.request.target.mixin.IAccessCheck;

/**
 * An implemenation of IRequestTarget that is used for the IResourceListener event request.
 * 
 * @author jcompagner
 */
public final class ComponentResourceRequestTarget implements IRequestTarget, IAccessCheck
{

	private final Page page;
	private final Component component;
	private final Method listenerMethod;

	/**
	 * Construct.
	 * @param page
	 * @param component
	 * @param listenerMethod
	 */
	public ComponentResourceRequestTarget(Page page, Component component, Method listenerMethod)
	{
		this.page = page;
		this.component = component;
		this.listenerMethod = listenerMethod;
	}

	/**
	 * @see wicket.IRequestTarget#respond(wicket.RequestCycle)
	 */
	public void respond(RequestCycle requestCycle)
	{
		page.beforeCallComponent(component, listenerMethod);

		try
		{
			// Invoke the interface method on the component
			listenerMethod.invoke(component, new Object[] {});
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("method " + listenerMethod.getName() + " of "
					+ listenerMethod.getDeclaringClass() + " targetted at component " + component
					+ " threw an exception", e);
		}
		finally
		{
			page.afterCallComponent(component, listenerMethod);
		}		
	}

	/**
	 * @see wicket.IRequestTarget#cleanUp(wicket.RequestCycle)
	 */
	public void cleanUp(RequestCycle requestCycle)
	{
		page.internalEndRequest();
	}

	/**
	 * @see wicket.IRequestTarget#synchronizeOnSession(RequestCycle)
	 */
	public boolean synchronizeOnSession(RequestCycle requestCycle)
	{
		return true;
	}

	/**
	 * @see wicket.request.target.mixin.IAccessCheck#checkAccess(RequestCycle)
	 */
	public boolean checkAccess(RequestCycle requestCycle)
	{
		return page.checkAccess();
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		if (obj instanceof ComponentResourceRequestTarget)
		{
			ComponentResourceRequestTarget that = (ComponentResourceRequestTarget)obj;
			return page.equals(that.page) && component.equals(that.component);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = getClass().hashCode();
		result += page.hashCode();
		result += component.hashCode();
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer b = new StringBuffer(getClass().getName()).append("@")
			.append(hashCode()).append(page).append("->").append(component.getId())
			.append("->IResourceListener");
		return b.toString();
	}
}
