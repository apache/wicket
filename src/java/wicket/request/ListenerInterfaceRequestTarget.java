/*
 * $Id: ListenerInterfaceRequestTarget.java,v 1.1 2005/11/27 23:22:45 eelco12
 * Exp $ $Revision$ $Date$
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
 * Default implementation of
 * {@link wicket.request.IListenerInterfaceRequestTarget}. Target that denotes
 * a page instance and a call to a component on that page using an listener
 * interface method.
 * 
 * @author Eelco Hillenius
 */
public class ListenerInterfaceRequestTarget extends PageRequestTarget
		implements
			IListenerInterfaceRequestTarget
{
	/** the target component. */
	private final Component component;

	/** the listener method. */
	private final Method listenerMethod;

	/** optionally the id of the behaviour to dispatch to. */
	private final String behaviourId;

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
	public ListenerInterfaceRequestTarget(Page page, Component component, Method listenerMethod)
	{
		this(page, component, listenerMethod, null);
	}


	/**
	 * Construct.
	 * 
	 * @param page
	 *            the page instance
	 * @param component
	 *            the target component
	 * @param listenerMethod
	 *            the listener method
	 * @param behaviourId
	 *            optionally the id of the behaviour to dispatch to
	 */
	public ListenerInterfaceRequestTarget(Page page, Component component, Method listenerMethod,
			String behaviourId)
	{
		super(page);

		if (component == null)
		{
			throw new NullPointerException("argument component must be not null");
		}

		this.component = component;

		if (listenerMethod == null)
		{
			throw new NullPointerException("argument listenerMethod must be not null");
		}

		this.listenerMethod = listenerMethod;
		this.behaviourId = behaviourId;
	}

	/**
	 * @see wicket.request.IListenerInterfaceRequestTarget#getComponent()
	 */
	public final Component getComponent()
	{
		return component;
	}

	/**
	 * @see wicket.request.IListenerInterfaceRequestTarget#getListenerMethod()
	 */
	public final Method getListenerMethod()
	{
		return listenerMethod;
	}

	/**
	 * @see wicket.request.IListenerInterfaceRequestTarget#getBehaviourId()
	 */
	public final String getBehaviourId()
	{
		return behaviourId;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		boolean equal = false;
		if (obj instanceof ListenerInterfaceRequestTarget)
		{
			ListenerInterfaceRequestTarget that = (ListenerInterfaceRequestTarget)obj;
			if (component.equals(that.component) && listenerMethod.equals(that.listenerMethod))
			{
				if (behaviourId != null)
				{
					return behaviourId.equals(that.behaviourId);
				}
				else
				{
					return that.behaviourId == null;
				}
			}
		}
		return equal;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = "ListenerInterfaceRequestTarget".hashCode();
		result += component.hashCode();
		result += listenerMethod.hashCode();
		result += behaviourId != null ? behaviourId.hashCode() : 0;
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer b = new StringBuffer().append(getPage().toString()).append("->").append(
				getComponent().getId()).append("->")
				.append(getListenerMethod().getDeclaringClass()).append(".").append(
						getListenerMethod().getName());
		if (getBehaviourId() != null)
		{
			b.append(" (behaviour ").append(getBehaviourId()).append(")");
		}
		return b.toString();
	}
}