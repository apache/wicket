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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import wicket.authorization.UnauthorizedActionException;
import wicket.util.lang.Classes;

/**
 * Base class for request listener interfaces.
 * 
 * @author Jonathan Locke
 */
public class RequestListenerInterface
{
	/** The listener interface method */
	private Method method;

	/** The name of this listener interface */
	private String name;

	/**
	 * Constructor.
	 * 
	 * @param listenerInterfaceClass
	 *            The interface class, which must extend IRequestListener.
	 */
	public RequestListenerInterface(final Class listenerInterfaceClass)
	{
		// Ensure that i extends IRequestListener
		if (!IRequestListener.class.isAssignableFrom(listenerInterfaceClass))
		{
			throw new IllegalArgumentException("Class " + listenerInterfaceClass
					+ " must extend IRequestListener");
		}

		// Get interface methods
		final Method[] methods = listenerInterfaceClass.getMethods();

		// If there is only one method
		if (methods.length == 1)
		{
			// and that method takes no parameters
			if (methods[0].getParameterTypes().length == 0)
			{
				this.method = methods[0];
			}
			else
			{
				throw new IllegalArgumentException("Method " + methods[0] + " in interface "
						+ listenerInterfaceClass + " cannot take any arguments");
			}
		}
		else
		{
			throw new IllegalArgumentException("Interface " + listenerInterfaceClass
					+ " can have only one method");
		}

		this.name = Classes.name(listenerInterfaceClass);
	}

	/**
	 * @return The method for this request listener interface
	 */
	public final Method getMethod()
	{
		return method;
	}

	/**
	 * @return The name of this request listener interface
	 */
	public final String getName()
	{
		return name;
	}

	/**
	 * Invokes a given interface on a component.
	 * 
	 * @param page
	 *            The Page that contains the component
	 * @param component
	 *            The component
	 */
	public final void invoke(final Page page, final Component component)
	{
		// Check authorization
		// FIXME Bug: Why is Component.ENABLE hardwired into this code (which came from AbstractListenerInterfaceRequestTarget)!?
		if (!component.isActionAuthorized(Component.ENABLE))
		{
			throw new UnauthorizedActionException(component, Component.ENABLE);
		}

		page.beforeCallComponent(component, this);

		try
		{
			// Invoke the interface method on the component
			method.invoke(component, new Object[] {});
		}
		catch (InvocationTargetException e)
		{
			// Honor redirect exception contract defined in IPageFactory
			if (e.getTargetException() instanceof AbstractRestartResponseException)
			{
				throw (RuntimeException)e.getTargetException();
			}
			throw new WicketRuntimeException("Method " + method.getName() + " of "
					+ method.getDeclaringClass() + " targeted at component " + component
					+ " threw an exception", e);
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException("Method " + method.getName() + " of "
					+ method.getDeclaringClass() + " targeted at component " + component
					+ " threw an exception", e);
		}
		finally
		{
			page.afterCallComponent(component, this);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "RequestListenerInterface{name=" + name + "}";
	}
}
