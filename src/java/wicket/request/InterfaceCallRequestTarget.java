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

import java.lang.reflect.Method;

import wicket.Component;
import wicket.Page;

/**
 * Default implementation of {@link wicket.request.IInterfaceCallRequestTarget}.
 * Target that denotes a page instance and a call to a component on that page
 * using an listener interface method.
 * 
 * @author Eelco Hillenius
 */
public class InterfaceCallRequestTarget extends PageRequestTarget
		implements
			IInterfaceCallRequestTarget
{
	/** the target component. */
	private final Component component;

	/** the listener method. */
	private final Method listenerMethod;

	/**
	 * Construct.
	 * 
	 * @param page
	 *            the page instance
	 * @param component
	 *            the target component
	 * @param listenerMethod
	 *            the listener method
	 */
	public InterfaceCallRequestTarget(Page page, Component component, Method listenerMethod)
	{
		super(page);
		this.component = component;

		if (component == null)
		{
			throw new NullPointerException("argument component must be not null");
		}
		if (listenerMethod == null)
		{
			throw new NullPointerException("argument listenerMethod must be not null");
		}

		this.listenerMethod = listenerMethod;
	}

	/**
	 * @see wicket.request.IInterfaceCallRequestTarget#getComponent()
	 */
	public Component getComponent()
	{
		return component;
	}

	/**
	 * @see wicket.request.IInterfaceCallRequestTarget#getListenerMethod()
	 */
	public Method getListenerMethod()
	{
		return listenerMethod;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return getPage().toString() + "->" + component.getId() + "->"
				+ listenerMethod.getDeclaringClass() + "." + listenerMethod.getName();
	}

}