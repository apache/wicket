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
package org.apache.wicket.core.request.handler;

import org.apache.wicket.Page;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.behavior.Behavior;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler.RedirectPolicy;
import org.apache.wicket.core.request.handler.logger.ListenerInterfaceLogData;
import org.apache.wicket.request.ILoggableRequestHandler;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request handler that invokes the listener interface on component and renders page afterwards.
 *
 * @author Matej Knopp
 */
public class ListenerInterfaceRequestHandler
	implements
		IPageRequestHandler,
		IComponentRequestHandler,
		ILoggableRequestHandler
{

	private static final Logger LOG = LoggerFactory.getLogger(ListenerInterfaceRequestHandler.class);

	private final IPageAndComponentProvider pageComponentProvider;

	private final RequestListenerInterface listenerInterface;

	private final Integer behaviorId;

	private ListenerInterfaceLogData logData;

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
		Args.notNull(pageComponentProvider, "pageComponentProvider");
		Args.notNull(listenerInterface, "listenerInterface");

		this.pageComponentProvider = pageComponentProvider;
		this.listenerInterface = listenerInterface;
		behaviorId = behaviorIndex;
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

	@Override
	public IRequestableComponent getComponent()
	{
		return pageComponentProvider.getComponent();
	}

	@Override
	public IRequestablePage getPage()
	{
		return pageComponentProvider.getPageInstance();
	}

	@Override
	public Class<? extends IRequestablePage> getPageClass()
	{
		return pageComponentProvider.getPageClass();
	}

	@Override
	public Integer getPageId()
	{
		return pageComponentProvider.getPageId();
	}

	@Override
	public PageParameters getPageParameters()
	{
		return pageComponentProvider.getPageParameters();
	}

	/**
	 * @see org.apache.wicket.request.IRequestHandler#detach(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void detach(IRequestCycle requestCycle)
	{
		if (logData == null)
		{
			logData = new ListenerInterfaceLogData(pageComponentProvider, listenerInterface,
				behaviorId);
		}
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
		return behaviorId;
	}

	/**
	 * @see org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void respond(final IRequestCycle requestCycle)
	{
		final IRequestablePage page = getPage();
		final boolean freshPage = pageComponentProvider.isPageInstanceFresh();
		final boolean isAjax = ((WebRequest)requestCycle.getRequest()).isAjax();

		IRequestableComponent component;
		try
		{
			component = getComponent();
		}
		catch (ComponentNotFoundException e)
		{
			// either the page is stateless and the component we are looking for is not added in the
			// constructor
			// or the page is stateful+stale and a new instances was created by pageprovider
			// we denote this by setting component to null
			component = null;
		}

		if ((component == null && freshPage) ||
			(component != null && getComponent().getPage() == page))
		{
			if (page instanceof Page)
			{
				// initialize the page to be able to check whether it is stateless
				((Page)page).internalInitialize();
			}
			final boolean isStateless = page.isPageStateless();

			RedirectPolicy policy = isStateless ? RedirectPolicy.NEVER_REDIRECT
				: RedirectPolicy.AUTO_REDIRECT;
			final IPageProvider pageProvider = new PageProvider(page);

			final boolean canCallListenerInterfaceAfterExpiry = component != null
					? component.canCallListenerInterfaceAfterExpiry()
					: false;
			if (!canCallListenerInterfaceAfterExpiry && freshPage && (isStateless == false || component == null))
			{
				// A listener interface is invoked on an expired page.

				// If the page is stateful then we cannot assume that the listener interface is
				// invoked on its initial state (right after page initialization) and that its
				// component and/or behavior will be available. That's why the listener interface
				// should be ignored and the best we can do is to re-paint the newly constructed
				// page.

				if (LOG.isDebugEnabled())
				{
					LOG.debug(
						"A ListenerInterface '{}' assigned to '{}' is executed on an expired stateful page. "
							+ "Scheduling re-create of the page and ignoring the listener interface...",
						listenerInterface, getComponentPath());
				}

				if (isAjax)
				{
					policy = RedirectPolicy.ALWAYS_REDIRECT;
				}

				requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(
					pageProvider, policy));
				return;
			}

			if (isAjax == false && listenerInterface.isRenderPageAfterInvocation())
			{
				// schedule page render after current request handler is done. this can be
				// overridden during invocation of listener
				// method (i.e. by calling RequestCycle#setResponsePage)
				requestCycle.scheduleRequestHandlerAfterCurrent(new RenderPageRequestHandler(
					pageProvider, policy));
			}

			invokeListener();

		}
		else
		{
			throw new WicketRuntimeException("Component " + getComponent() +
				" has been removed from page.");
		}
	}

	private void invokeListener()
	{
		if (getBehaviorIndex() == null)
		{
			listenerInterface.invoke(getComponent());
		}
		else
		{
			try
			{
				Behavior behavior = getComponent().getBehaviorById(behaviorId);
				listenerInterface.invoke(getComponent(), behavior);
			}
			catch (IndexOutOfBoundsException e)
			{
				throw new WicketRuntimeException("Couldn't find component behavior.", e);
			}

		}
	}

	@Override
	public final boolean isPageInstanceCreated()
	{
		return pageComponentProvider.hasPageInstance();
	}

	@Override
	public final String getComponentPath()
	{
		return pageComponentProvider.getComponentPath();
	}

	@Override
	public final Integer getRenderCount()
	{
		return pageComponentProvider.getRenderCount();
	}

	/** {@inheritDoc} */
	@Override
	public ListenerInterfaceLogData getLogData()
	{
		return logData;
	}
}
