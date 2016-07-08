/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.ListenerInvocationNotAllowedException;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.util.lang.Classes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Base class for request listener interfaces.
 * 
 * @author Jonathan Locke
 */
public class RequestListenerInterface
{
	/** Map from name to request listener interface */
	private static final Map<String, RequestListenerInterface> interfaces = Collections.synchronizedMap(new HashMap<String, RequestListenerInterface>());

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(RequestListenerInterface.class);

	/**
	 * Looks up a request interface listener by name.
	 * 
	 * @param interfaceName
	 *            The interface name
	 * @return The RequestListenerInterface object, or null if none is found
	 * 
	 */
	public static RequestListenerInterface forName(final String interfaceName)
	{
		return interfaces.get(interfaceName);
	}

	/** The name of this listener interface */
	private final String name;

	/**
	 * Whether the render count should be included in URL. This should be true for every listener
	 * interface URL that should only be active on last rendered page (links, etc) and false for
	 * other (resources);
	 */
	private boolean includeRenderCount = true;

	/**
	 * Whether the page should be rendered by default after the invocation of this listener
	 * interface. Applies only during non-ajax requests.
	 */
	private boolean renderPageAfterInvocation = true;

	final Class<? extends IRequestListener> listenerInterfaceClass;

	/**
	 * Constructor.
	 * 
	 * @param listenerInterfaceClass
	 *            The interface class, which must extend IRequestListener.
	 */
	public RequestListenerInterface(final Class<? extends IRequestListener> listenerInterfaceClass)
	{
		this.listenerInterfaceClass = listenerInterfaceClass;

		// Ensure that it extends IRequestListener
		if (!IRequestListener.class.isAssignableFrom(listenerInterfaceClass))
		{
			throw new IllegalArgumentException("Class " + listenerInterfaceClass +
				" must extend IRequestListener");
		}

		// Save short class name
		name = Classes.simpleName(listenerInterfaceClass);

		// Register this listener
		register();
	}


	/**
	 * @return The interface class, which must extend IRequestListener.
	 */
	public Class<? extends IRequestListener> getListenerInterfaceClass()
	{
		return listenerInterfaceClass;
	}


	/**
	 * @param includeRenderCount
	 * @return self
	 */
	public RequestListenerInterface setIncludeRenderCount(boolean includeRenderCount)
	{
		this.includeRenderCount = includeRenderCount;
		return this;
	}

	/**
	 * @return whether the render count should be included in url
	 */
	public boolean isIncludeRenderCount()
	{
		return includeRenderCount;
	}

	/**
	 * @param renderPageAfterInvocation
	 * @return self
	 */
	public RequestListenerInterface setRenderPageAfterInvocation(boolean renderPageAfterInvocation)
	{
		this.renderPageAfterInvocation = renderPageAfterInvocation;
		return this;
	}

	/**
	 * @return whether the page should be rendered after invocation of this interface (during
	 *         non-ajax requests)
	 */
	public boolean isRenderPageAfterInvocation()
	{
		return renderPageAfterInvocation;
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
	 * @param rcomponent
	 *            The component
	 * 
	 * @throws ListenerInvocationNotAllowedException
	 *             when listener invocation attempted on a component that does not allow it
	 */
	public final void invoke(final IRequestableComponent rcomponent)
	{
		// we are in Wicket core land
		final Component component = (Component)rcomponent;

		if (!component.canCallListenerInterface())
		{
			// just return so that we have a silent fail and just re-render the
			// page
			log.info("component not enabled or visible; ignoring call. Component: " + component);
			throw new ListenerInvocationNotAllowedException(component, null,
				"Component rejected interface invocation");
		}

		internalInvoke(component, component);
	}

	/**
	 * Invokes a given interface on a component's behavior.
	 * 
	 * @param rcomponent
	 *            The component
	 * @param behavior
	 * @throws ListenerInvocationNotAllowedException
	 *             when listener invocation attempted on a component that does not allow it
	 */
	public final void invoke(final IRequestableComponent rcomponent, final Behavior behavior)
	{
		// we are in Wicket core land
		final Component component = (Component)rcomponent;

		if (!behavior.canCallListenerInterface(component))
		{
			log.warn("behavior not enabled; ignore call. Behavior {} at component {}", behavior,
				component);
			throw new ListenerInvocationNotAllowedException(component, behavior,
				"Behavior rejected interface invocation. ");
		}

		internalInvoke(component, behavior);
	}

	private void internalInvoke(final Component component, final Object target)
	{
		// save a reference to the page because the component can be removed
		// during the invocation of the listener and thus lose its parent
		Page page = component.getPage();

		// initialization is required for stateless pages
		if (!page.isInitialized())
		{
			page.internalInitialize();
		}

		((IRequestListener)target).onRequest();
	}

	/**
	 * Method to call to register this interface for use
	 */
	public void register()
	{
		// Register this listener interface
		registerRequestListenerInterface(this);
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[RequestListenerInterface name=" + name + "]";
	}


	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * <p>
	 * In previous versions of Wicket, request listeners were manually registered by calling this
	 * method. Now there is a first class RequestListenerInterface object which should be
	 * constructed as a constant member of the interface to enable automatic interface registration.
	 * <p>
	 * Adds a request listener interface to the map of interfaces that can be invoked by outsiders.
	 * 
	 * @param requestListenerInterface
	 *            The request listener interface object
	 */
	private void registerRequestListenerInterface(final RequestListenerInterface requestListenerInterface)
	{
		// Check that a different interface method with the same name has not
		// already been registered
		final RequestListenerInterface existingInterface = RequestListenerInterface.forName(requestListenerInterface.getName());
		if (existingInterface == null)
		{
			// Save this interface method by the non-qualified class name
			interfaces.put(requestListenerInterface.getName(), requestListenerInterface);

			log.info("registered listener interface " + this);
		}
	}

	/**
	 * 
	 * @return collection of all registered interfaces
	 */
	public static Collection<RequestListenerInterface> getRegisteredInterfaces()
	{
		return Collections.unmodifiableCollection(interfaces.values());
	}


}
