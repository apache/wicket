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
package wicket.request.target;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import wicket.Component;
import wicket.Page;
import wicket.RequestCycle;
import wicket.WicketRuntimeException;
import wicket.request.IComponentRequestTarget;

/**
 * Default implementation of {@link wicket.request.IComponentRequestTarget}.
 * Target that denotes a single component instance.
 * 
 * @author Eelco Hillenius
 */
public class InterfaceRequestTarget implements IComponentRequestTarget
{
	/** the component instance. */
	private final Component component;

	private final Method method;
	
	/**
	 * Construct.
	 * 
	 * @param component
	 *            the component instance
	 * @param myInterface
	 */
	public InterfaceRequestTarget(final Component component, final Class myInterface)
	{
		if (component == null)
		{
			throw new IllegalArgumentException("Argument 'component' must be not null");
		}

		if (myInterface == null)
		{
			throw new IllegalArgumentException("Argument 'myInterface' must be not null");
		}
		
		if (!myInterface.isInstance(component))
		{
			throw new IllegalArgumentException("Component must implement the interface: " + myInterface.getClass().getName() + "; Component: " + component);
		}

		this.component = component;
		
		if (myInterface.getMethods().length != 1)
		{
			throw new IllegalArgumentException("Interface must implement exactly one method: " + myInterface.getClass().getName());
		}
		
		this.method = myInterface.getMethods()[0];
		
		if (method.getParameterTypes().length != 0)
		{
			throw new IllegalArgumentException("Interface must implement exactly one method without any parameter: " + myInterface.getClass().getName());
		}
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

		try
		{
			this.method.invoke(this.component, new Object[] {});
		}
		catch (IllegalArgumentException e)
		{
			throw new WicketRuntimeException("Error while rendering the response", e);
		}
		catch (IllegalAccessException e)
		{
			throw new WicketRuntimeException("Error while rendering the response", e);
		}
		catch (InvocationTargetException e)
		{
			throw new WicketRuntimeException("Error while rendering the response", e);
		}
		
		if (page != null)
		{
			page.endComponentRender(component);
		}
	}

	/**
	 * @see wicket.request.IComponentRequestTarget#getComponent()
	 */
	public final Component getComponent()
	{
		return component;
	}

	/**
	 * @see wicket.IRequestTarget#detach(wicket.RequestCycle)
	 */
	public void detach(RequestCycle requestCycle)
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
	public boolean equals(final Object obj)
	{
		if (obj instanceof InterfaceRequestTarget)
		{
			InterfaceRequestTarget that = (InterfaceRequestTarget)obj;
			return component.equals(that.component);
		}
		return false;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = "InterfaceRequestTarget".hashCode();
		result += component.hashCode();
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[InterfaceRequestTarget@" + hashCode() + " " + component + "]";
	}
}