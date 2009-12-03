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

import org.apache.wicket.ng.request.component.PageParametersNg;
import org.apache.wicket.ng.request.component.RequestableComponent;
import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.request.cycle.RequestCycle;
import org.apache.wicket.ng.request.handler.ComponentRequestHandler;
import org.apache.wicket.ng.request.handler.PageAndComponentProvider;
import org.apache.wicket.ng.request.handler.PageRequestHandler;
import org.apache.wicket.ng.request.listener.RequestListenerInterface;
import org.apache.wicket.util.lang.Checks;

/**
 * Request handler for bookmarkable pages with listener interface. This handler is only used to
 * generate URLs. Rendering is always handled by {@link ListenerInterfaceRequestHandler}.
 * 
 * @author Matej Knopp
 */
public class BookmarkableListenerInterfaceRequestHandler
	implements
		PageRequestHandler,
		ComponentRequestHandler
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
	public BookmarkableListenerInterfaceRequestHandler(PageAndComponentProvider pageComponentProvider,
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
	 * @param component
	 * @param listenerInterface
	 */
	public BookmarkableListenerInterfaceRequestHandler(PageAndComponentProvider pageComponentProvider,
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

	public PageParametersNg getPageParameters()
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
	 * Returns index of behavior this listener is targeted on or <code>null</code> if component
	 * is the target
	 * 
	 * @return behavior index or <code>null</code>
	 */
	public Integer getBehaviorIndex()
	{
		return behaviorIndex;
	}

	public void respond(RequestCycle requestCycle)
	{
		// nothing to do here, this handler is only used to generate URLs
	}

}
