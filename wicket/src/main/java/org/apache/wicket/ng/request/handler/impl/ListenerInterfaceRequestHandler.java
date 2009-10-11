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
package org.apache.wicket.ng.request.handler.impl;

import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.ng.WicketRuntimeException;
import org.apache.wicket.ng.request.component.PageParameters;
import org.apache.wicket.ng.request.component.RequestableComponent;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.ComponentRequestHandler;
import org.apache.wicket.ng.request.handler.PageAndComponentProvider;
import org.apache.wicket.ng.request.handler.PageProvider;
import org.apache.wicket.ng.request.handler.PageRequestHandler;
import org.apache.wicket.ng.request.handler.impl.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.ng.request.listener.RequestListenerInterface;
import org.apache.wicket.util.lang.Checks;

/**
 * Request handler that invokes the listener interface on component and renders page afterwards.
 * 
 * @author Matej Knopp
 */
public class ListenerInterfaceRequestHandler implements PageRequestHandler, ComponentRequestHandler
{
	private final PageAndComponentProvider pageComponentProvider;
	private final RequestListenerInterface listenerInterface;
	private final Integer behaviorIndex;

	/**
	 * Construct.
	 * 
	 * @param pageComponentProvider
	 * @param listenerInterface
	 * @param behaviorIndex
	 */
	public ListenerInterfaceRequestHandler(PageAndComponentProvider pageComponentProvider,
		RequestListenerInterface listenerInterface, Integer behaviorIndex)
	{
		Checks.argumentNotNull(pageComponentProvider, "pageComponentProvider");
		Checks.argumentNotNull(listenerInterface, "listenerInterface");

		this.pageComponentProvider = pageComponentProvider;
		this.listenerInterface = listenerInterface;
		this.behaviorIndex = behaviorIndex;
	}
	
	/**
	 * Construct.
	 * 
	 * @param pageComponentProvider
	 * @param listenerInterface
	 */
	public ListenerInterfaceRequestHandler(PageAndComponentProvider pageComponentProvider,
		RequestListenerInterface listenerInterface)
	{
		this(pageComponentProvider, listenerInterface, null);
	}

	public RequestableComponent getComponent()
	{
		return pageComponentProvider.getComponent();
	}

	public RequestablePage getPage()
	{
		return pageComponentProvider.getPageInstance();
	}

	public Class<? extends RequestablePage> getPageClass()
	{
		return pageComponentProvider.getPageClass();
	}

	public PageParameters getPageParameters()
	{
		return pageComponentProvider.getPageParameters();
	}

	public void detach(RequestCycle requestCycle)
	{
		pageComponentProvider.detach();
	}

	/**
	 * Returns the listener interface.
	 * 
	 * @return listener interface
	 */
	public RequestListenerInterface getListenerInterface()
	{
		return listenerInterface;
	}

	/**
	 * Index of target behavior or <code>null</code> if component is the target.
	 * 
	 * @return behavior index or <code>null</code>
	 */
	public Integer getBehaviorIndex()
	{
		return behaviorIndex;
	}

	public void respond(RequestCycle requestCycle)
	{
		if (getComponent().getPage() == getPage())
		{
			// schedule page render after current request handler is done. this can be overridden during invocation of listener
			// method (i.e. by calling RequestCycle#setResponsePage)
			RedirectPolicy policy = getPage().isPageStateless() ? RedirectPolicy.NEVER_REDIRECT : RedirectPolicy.AUTO_REDIRECT;
			requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(new PageProvider(getPage()), policy));

			if (getBehaviorIndex() == null)
			{
				listenerInterface.invoke(getComponent());
			}
			else
			{
				try
				{
					IBehavior behavior = getComponent().getBehaviors().get(behaviorIndex);
					listenerInterface.invoke(getComponent(), behavior);
				}
				catch (IndexOutOfBoundsException e)
				{
					throw new WicketRuntimeException("Couldn't find component behavior.");
				}

			}
		}
		else
		{
			throw new WicketRuntimeException("Component " + getComponent() +
				" has been removed from page.");
		}
	}

}
