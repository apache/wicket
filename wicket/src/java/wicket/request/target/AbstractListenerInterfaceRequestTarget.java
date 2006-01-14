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
package wicket.request.target;

import java.lang.reflect.Method;

import wicket.Component;
import wicket.Page;
import wicket.WicketRuntimeException;
import wicket.request.IListenerInterfaceRequestTarget;

/**
 * The abstract implementation of
 * {@link wicket.request.IListenerInterfaceRequestTarget}. Target that denotes
 * a page instance and a call to a component on that page using an listener
 * interface method.
 * 
 * @author Eelco Hillenius
 * @author Johan Compagner
 */
public abstract class AbstractListenerInterfaceRequestTarget extends PageRequestTarget
		implements
			IListenerInterfaceRequestTarget,
			IEventProcessor
{
	/** optionally the id of the behavior to dispatch to. */
	private final String behaviorId;

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
	public AbstractListenerInterfaceRequestTarget(Page page, Component component,
			Method listenerMethod)
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
	 * @param behaviorId
	 *            optionally the id of the behavior to dispatch to
	 */
	public AbstractListenerInterfaceRequestTarget(Page page, Component component,
			Method listenerMethod, String behaviorId)
	{
		super(page);

		if (component == null)
		{
			throw new IllegalArgumentException("Argument component must be not null");
		}

		this.component = component;

		if (listenerMethod == null)
		{
			throw new IllegalArgumentException("Argument listenerMethod must be not null");
		}

		this.listenerMethod = listenerMethod;
		this.behaviorId = behaviorId;
	}

	/**
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object obj)
	{
		boolean equal = false;
		if (obj != null && obj.getClass().equals(getClass()))
		{
			AbstractListenerInterfaceRequestTarget that = (AbstractListenerInterfaceRequestTarget)obj;
			if (component.equals(that.component) && listenerMethod.equals(that.listenerMethod))
			{
				if (behaviorId != null)
				{
					return behaviorId.equals(that.behaviorId);
				}
				else
				{
					return that.behaviorId == null;
				}
			}
		}
		return equal;
	}

	/**
	 * @see wicket.request.IListenerInterfaceRequestTarget#getBehaviorId()
	 */
	public final String getBehaviorId()
	{
		return behaviorId;
	}

	/**
	 * @see wicket.request.IListenerInterfaceRequestTarget#getListenerMethod()
	 */
	public final Method getListenerMethod()
	{
		return listenerMethod;
	}

	/**
	 * @see wicket.request.IListenerInterfaceRequestTarget#getTarget()
	 */
	public final Component getTarget()
	{
		return component;
	}

	/**
	 * @see java.lang.Object#hashCode()
	 */
	public int hashCode()
	{
		int result = getClass().hashCode();
		result += component.hashCode();
		result += listenerMethod.hashCode();
		result += behaviorId != null ? behaviorId.hashCode() : 0;
		return 17 * result;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		StringBuffer b = new StringBuffer(getClass().getName()).append("@").append(hashCode())
				.append(getPage().toString()).append("->").append(getTarget().getId()).append("->")
				.append(getListenerMethod().getDeclaringClass()).append(".").append(
						getListenerMethod().getName());
		if (getBehaviorId() != null)
		{
			b.append(" (behavior ").append(getBehaviorId()).append(")");
		}
		return b.toString();
	}

	/**
	 * Invokes a given interface on a component.
	 * 
	 * @param component
	 *            The component
	 * @param method
	 *            The name of the method to call
	 * @param page
	 *            The page on which the component resides
	 */
	protected final void invokeInterface(final Component component, final Method method,
			final Page page)
	{
		page.beforeCallComponent(component, method);

		try
		{
			// Invoke the interface method on the component
			method.invoke(component, new Object[] {});
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("method " + method.getName() + " of "
					+ method.getDeclaringClass() + " targetted at component " + component
					+ " threw an exception", e);
		}
		finally
		{
			page.afterCallComponent(component, method);
		}
	}
}