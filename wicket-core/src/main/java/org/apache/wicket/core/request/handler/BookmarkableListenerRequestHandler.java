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

import org.apache.wicket.IRequestListener;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;

/**
 * Request handler for bookmarkable pages with an {@link IRequestListener}. This handler is only used to
 * generate URLs. Rendering is always handled by {@link ListenerRequestHandler}.
 *
 * @author Matej Knopp
 */
public class BookmarkableListenerRequestHandler
	implements
		IPageRequestHandler,
		IComponentRequestHandler
{
	private final IPageAndComponentProvider pageComponentProvider;

	private final Integer behaviorIndex;

	/**
	 * Construct.
	 *
	 * @param pageComponentProvider
	 * @param behaviorIndex
	 */
	public BookmarkableListenerRequestHandler(
		IPageAndComponentProvider pageComponentProvider,
		Integer behaviorIndex)
	{
		Args.notNull(pageComponentProvider, "pageComponentProvider");

		this.pageComponentProvider = pageComponentProvider;
		this.behaviorIndex = behaviorIndex;
	}

	/**
	 * Construct.
	 *
	 * @param pageComponentProvider
	 */
	public BookmarkableListenerRequestHandler(PageAndComponentProvider pageComponentProvider)
	{
		this(pageComponentProvider, null);
	}

	public boolean includeRenderCount() {
		if (behaviorIndex == null) {
			return ((IRequestListener)getComponent()).rendersPage();
		} else {
			return ((IRequestListener)getComponent().getBehaviorById(getBehaviorIndex())).rendersPage();
		}
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IComponentRequestHandler#getComponent()
	 */
	@Override
	public IRequestableComponent getComponent()
	{
		return pageComponentProvider.getComponent();
	}

	@Override
	public final String getComponentPath()
	{
		return pageComponentProvider.getComponentPath();
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageRequestHandler#getPage()
	 */
	@Override
	public IRequestablePage getPage()
	{
		return pageComponentProvider.getPageInstance();
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageClassRequestHandler#getPageClass()
	 */
	@Override
	public Class<? extends IRequestablePage> getPageClass()
	{
		return pageComponentProvider.getPageClass();
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageRequestHandler#getPageId()
	 */
	@Override
	public Integer getPageId()
	{
		return pageComponentProvider.getPageId();
	}

	/**
	 * @see org.apache.wicket.core.request.handler.IPageClassRequestHandler#getPageParameters()
	 */
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
		pageComponentProvider.detach();
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
	 * @see org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void respond(IRequestCycle requestCycle)
	{
		// nothing to do here, this handler is only used to generate URLs
	}

	@Override
	public final boolean isPageInstanceCreated()
	{
		// this request handler always operates on a created page instance
		return true;
	}

	/**
	 * @return the render count of the page
	 */
	@Override
	public final Integer getRenderCount()
	{
		return pageComponentProvider.getRenderCount();
	}
}
