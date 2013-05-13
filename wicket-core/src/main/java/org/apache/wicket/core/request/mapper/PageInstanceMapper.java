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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.core.request.handler.PageAndComponentProvider;
import org.apache.wicket.core.request.handler.PageProvider;
import org.apache.wicket.core.request.handler.RenderPageRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.ComponentInfo;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.info.PageInfo;

/**
 * Decodes and encodes the following URLs:
 *
 * <pre>
 *  Page Instance - Render (RenderPageRequestHandler)
 *  /wicket/page?2
 *
 *
 *  Page Instance - Listener (ListenerInterfaceRequestHandler)
 *  /wicket/page?2-click-foo-bar-baz
 *  /wicket/page?2-click.1-foo-bar-baz (1 is behavior index)
 * </pre>
 *
 * @author Matej Knopp
 */
public class PageInstanceMapper extends AbstractComponentMapper
{
	/**
	 * Construct.
	 */
	public PageInstanceMapper()
	{
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapRequest(org.apache.wicket.request.Request)
	 */
	@Override
	public IRequestHandler mapRequest(Request request)
	{
		if (matches(request))
		{
			Url url = request.getUrl();
			PageComponentInfo info = getPageComponentInfo(url);
			if (info != null && info.getPageInfo().getPageId() != null)
			{
				Integer renderCount = info.getComponentInfo() != null ? info.getComponentInfo()
					.getRenderCount() : null;

				if (info.getComponentInfo() == null)
				{
					PageProvider provider = new PageProvider(info.getPageInfo().getPageId(),
						renderCount);
					provider.setPageSource(getContext());
					// render page
					return new RenderPageRequestHandler(provider);
				}
				else
				{
					ComponentInfo componentInfo = info.getComponentInfo();

					PageAndComponentProvider provider = new PageAndComponentProvider(
						info.getPageInfo().getPageId(), renderCount,
						componentInfo.getComponentPath());

					provider.setPageSource(getContext());

					// listener interface
					RequestListenerInterface listenerInterface = requestListenerInterfaceFromString(componentInfo.getListenerInterface());

					return new ListenerInterfaceRequestHandler(provider, listenerInterface,
						componentInfo.getBehaviorId());
				}
			}
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#mapHandler(org.apache.wicket.request.IRequestHandler)
	 */
	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		PageComponentInfo info = null;

		if (requestHandler instanceof RenderPageRequestHandler)
		{
			IRequestablePage page = ((RenderPageRequestHandler)requestHandler).getPage();

			PageInfo i = new PageInfo(page.getPageId());
			info = new PageComponentInfo(i, null);
		}
		else if (requestHandler instanceof ListenerInterfaceRequestHandler)
		{
			ListenerInterfaceRequestHandler handler = (ListenerInterfaceRequestHandler)requestHandler;
			IRequestablePage page = handler.getPage();
			String componentPath = handler.getComponentPath();
			RequestListenerInterface listenerInterface = handler.getListenerInterface();

			Integer renderCount = null;
			if (listenerInterface.isIncludeRenderCount())
			{
				renderCount = page.getRenderCount();
			}

			PageInfo pageInfo = new PageInfo(page.getPageId());
			ComponentInfo componentInfo = new ComponentInfo(renderCount,
				requestListenerInterfaceToString(listenerInterface), componentPath,
				handler.getBehaviorIndex());
			info = new PageComponentInfo(pageInfo, componentInfo);
		}

		if (info != null)
		{
			Url url = new Url();
			url.getSegments().add(getContext().getNamespace());
			url.getSegments().add(getContext().getPageIdentifier());
			encodePageComponentInfo(url, info);
			return url;
		}
		else
		{
			return null;
		}
	}

	/**
	 * @see org.apache.wicket.request.IRequestMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	@Override
	public int getCompatibilityScore(final Request request)
	{
		int score = 0;
		if (matches(request))
		{
			score = Integer.MAX_VALUE;
		}
		return score;
	}

	/**
	 * Matches when the request url starts with
	 * {@link org.apache.wicket.core.request.mapper.IMapperContext#getNamespace()}/{@link org.apache.wicket.core.request.mapper.IMapperContext#getPageIdentifier()}
	 * or when the base url starts with {@link org.apache.wicket.core.request.mapper.IMapperContext#getNamespace()}
	 * and the request url with {@link org.apache.wicket.core.request.mapper.IMapperContext#getPageIdentifier()}

	 * @param request
	 *      the request to check
	 * @return {@code true} if the conditions match
	 */
	private boolean matches(final Request request)
	{
		boolean matches = false;
		Url url = request.getUrl();
		String namespace = getContext().getNamespace();
		String pageIdentifier = getContext().getPageIdentifier();
		if (urlStartsWith(url, namespace, pageIdentifier))
		{
			matches = true;
		}
		else if (urlStartsWith(request.getClientUrl(), namespace) && urlStartsWith(url, pageIdentifier))
		{
			matches = true;
		}

		return matches;
	}
}
