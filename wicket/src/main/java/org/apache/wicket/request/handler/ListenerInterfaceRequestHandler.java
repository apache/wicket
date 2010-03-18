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
package org.apache.wicket.request.handler;

import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.IBehavior;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Checks;

/**
 * Request handler that invokes the listener interface on component and renders page afterwards.
 * 
 * @author Matej Knopp
 */
public class ListenerInterfaceRequestHandler
	implements
		IPageRequestHandler,
		IComponentRequestHandler
{
	private final IPageAndComponentProvider pageComponentProvider;

	private final RequestListenerInterface listenerInterface;

	private final Integer behaviorIndex;

	/**
	 * Construct.
	 * 
	 * @param pageComponentProvider
	 * @param listenerInterface
	 * @param behaviorIndex
	 */
	public ListenerInterfaceRequestHandler(IPageAndComponentProvider pageComponentProvider,
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

	/**
	 * @see org.apache.wicket.request.handler.IComponentRequestHandler#getComponent()
	 */
	public IRequestableComponent getComponent()
	{
		return pageComponentProvider.getComponent();
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageRequestHandler#getPage()
	 */
	public IRequestablePage getPage()
	{
		return pageComponentProvider.getPageInstance();
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageClassRequestHandler#getPageClass()
	 */
	public Class<? extends IRequestablePage> getPageClass()
	{
		return pageComponentProvider.getPageClass();
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageClassRequestHandler#getPageParameters()
	 */
	public PageParameters getPageParameters()
	{
		return pageComponentProvider.getPageParameters();
	}

	/**
	 * @see org.apache.org.apache.wicket.request.IRequestHandler#detach(org.apache.wicket.request.cycle.RequestCycle)
	 */
	public void detach(IRequestCycle requestCycle)
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

	/**
	 * @see org.apache.org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.cycle.RequestCycle)
	 */
	public void respond(final IRequestCycle requestCycle)
	{
		if (getComponent().getPage() == getPage())
		{
			if (((WebRequest)requestCycle.getRequest()).isAjax() == false &&
				listenerInterface.isRenderPageAfterInvocation())
			{
				// schedule page render after current request handler is done. this can be
				// overridden
				// during invocation of listener
				// method (i.e. by calling RequestCycle#setResponsePage)
				RedirectPolicy policy = getPage().isPageStateless() ? RedirectPolicy.NEVER_REDIRECT
					: RedirectPolicy.AUTO_REDIRECT;
				requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(
					new PageProvider(getPage()), policy));
			}

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
