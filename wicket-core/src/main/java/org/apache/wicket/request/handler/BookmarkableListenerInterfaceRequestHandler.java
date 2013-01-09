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
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request handler for bookmarkable pages with listener interface. This handler is only used to
 * generate URLs. Rendering is always handled by {@link ListenerInterfaceRequestHandler}.
 * 
 * @author Matej Knopp
 */
public class BookmarkableListenerInterfaceRequestHandler
	implements
		IPageRequestHandler,
		IComponentRequestHandler
{
	private static final Logger logger = LoggerFactory.getLogger(BookmarkableListenerInterfaceRequestHandler.class);

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
	public BookmarkableListenerInterfaceRequestHandler(
		IPageAndComponentProvider pageComponentProvider,
		RequestListenerInterface listenerInterface, Integer behaviorIndex)
	{
		Args.notNull(pageComponentProvider, "pageComponentProvider");
		Args.notNull(listenerInterface, "listenerInterface");

		this.pageComponentProvider = pageComponentProvider;
		this.listenerInterface = listenerInterface;
		this.behaviorIndex = behaviorIndex;
	}

	/**
	 * Construct.
	 * 
	 * @param pageComponentProvider
	 * @param component
	 * @param listenerInterface
	 */
	public BookmarkableListenerInterfaceRequestHandler(
		PageAndComponentProvider pageComponentProvider, RequestListenerInterface listenerInterface)
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

	public final String getComponentPath()
	{
		return pageComponentProvider.getComponentPath();
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
	 * @see org.apache.wicket.request.handler.IPageRequestHandler#getPageId()
	 */
	public Integer getPageId()
	{
		return pageComponentProvider.getPageId();
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
	 * Returns index of behavior this listener is targeted on or <code>null</code> if component is
	 * the target
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
	public void respond(IRequestCycle requestCycle)
	{
		// nothing to do here, this handler is only used to generate URLs
	}

	public final boolean isPageInstanceCreated()
	{
		// this request handler always operates on a created page instance
		return true;
	}

	/**
	 * @return the render count of the page
	 */
	public final Integer getRenderCount()
	{
		return pageComponentProvider.getRenderCount();
	}

	public static BookmarkableListenerInterfaceRequestHandler wrap(
		ListenerInterfaceRequestHandler handler)
	{
		return new BookmarkableListenerInterfaceRequestHandler(new PageAndComponentProvider(
			handler.getPage(), handler.getComponent()), handler.getListenerInterface(),
			handler.getBehaviorIndex());
	}
}
