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
package org.apache._wicket.request.encoder;

import org.apache._wicket.IComponent;
import org.apache._wicket.IPage;
import org.apache._wicket.PageParameters;
import org.apache._wicket.request.RequestHandler;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.handler.impl.BookmarkableListenerInterfaceRequestHandler;
import org.apache._wicket.request.handler.impl.BookmarkablePageRequestHandler;
import org.apache._wicket.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache._wicket.request.handler.impl.RenderPageRequestHandler;
import org.apache._wicket.request.request.Request;
import org.apache.wicket.RequestListenerInterface;

/**
 * Decodes and encodes the following URLs:
 * 
 * <pre>
 *  Page Instance - Render
 *  /wicket/bookmarkable/org.apache.wicket.MyPage
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?pageMap
 *  (these will redirect to hybrid alternative if page is not stateless)
 * 
 *  Page Instance - Render Hybrid 
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2.4
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?pageMap.2.4
 * 
 *  Page Instance - Bookmarkable Listener 
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2-click-foo-bar-baz
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2.4-click-foo-bar-baz
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?pageMap.2.4-click-foo-bar-baz
 *  (these will redirect to hybrid if page is not stateless)
 * </pre>
 * 
 * @author Matej Knopp
 */
public class BookmarkableEncoder extends AbstractEncoder
{
	private final PageParametersEncoder pageParametersEncoder;
	
	/**
	 * Construct.
	 * @param pageParametersEncoder 
	 */
	public BookmarkableEncoder(PageParametersEncoder pageParametersEncoder)
	{
		this.pageParametersEncoder = pageParametersEncoder;
	}
	
	/**
	 * Construct.
	 */
	public BookmarkableEncoder()
	{
		this(new SimplePageParametersEncoder());
	}

	private RequestHandler processBookmarkable(String pageMapName,
		Class<? extends IPage> pageClass, PageParameters pageParameters)
	{
		IPage page = newPageInstance(pageMapName, pageClass, pageParameters);
		return new RenderPageRequestHandler(page);
	}

	private RequestHandler processHybrid(PageInfo pageInfo, Class<? extends IPage> pageClass,
		PageParameters pageParameters)
	{
		IPage page = getPageInstance(pageInfo, pageClass, pageParameters);
		return new RenderPageRequestHandler(page);
	}

	private RequestHandler processListener(PageComponentInfo pageComponentInfo,
		Class<? extends IPage> pageClass, PageParameters pageParameters)
	{
		PageInfo pageInfo = pageComponentInfo.getPageInfo();
		ComponentInfo componentInfo = pageComponentInfo.getComponentInfo();

		IPage page = getPageInstance(pageInfo, pageClass, pageParameters, true);
		IComponent component = getComponent(page, componentInfo.getComponentPath());
		RequestListenerInterface listenerInterface = requestListenerInterfaceFromString(componentInfo.getListenerInterface());

		return new ListenerInterfaceRequestHandler(page, component, listenerInterface);
	}

	public RequestHandler decode(Request request)
	{
		Url url = request.getUrl();
		if (url.getSegments().size() >= 3 &&
			urlStartsWith(url, getContext().getNamespace(),
				getContext().getBookmarkableIdentifier()))
		{
			PageComponentInfo info = getPageComponentInfo(url);

			String className = url.getSegments().get(2);
			Class<? extends IPage> pageClass = getPageClass(className);
			PageParameters pageParameters = extractPageParameters(url,
				request.getRequestParameters(), 3, pageParametersEncoder);

			if (info == null || info.getPageInfo().getPageId() == null)
			{
				String pageMap = info != null ? info.getPageInfo().getPageMapName() : null;
				return processBookmarkable(pageMap, pageClass, pageParameters);
			}
			else if (info.getPageInfo().getPageId() != null && info.getComponentInfo() == null)
			{
				return processHybrid(info.getPageInfo(), pageClass, pageParameters);
			}
			else if (info.getComponentInfo() != null)
			{
				return processListener(info, pageClass, pageParameters);
			}
		}
		return null;
	}

	private Url newUrl(Class<? extends IPage> pageClass)
	{
		Url url = new Url();

		url.getSegments().add(getContext().getNamespace());
		url.getSegments().add(getContext().getBookmarkableIdentifier());
		url.getSegments().add(pageClass.getName());

		return url;
	}

	public Url encode(RequestHandler requestHandler)
	{
		if (requestHandler instanceof BookmarkablePageRequestHandler)
		{
			BookmarkablePageRequestHandler handler = (BookmarkablePageRequestHandler)requestHandler;
			Url url = newUrl(handler.getPageClass());

			PageInfo info = new PageInfo(null, null, handler.getPageMapName());
			encodePageComponentInfo(url, new PageComponentInfo(info, null));
			return encodePageParameters(url, handler.getPageParameters(), pageParametersEncoder);
		}
		else if (requestHandler instanceof RenderPageRequestHandler)
		{
			IPage page = ((RenderPageRequestHandler)requestHandler).getPage();

			// necessary check so that we won't generate bookmarkable URLs for all pages
			if (page.wasCreatedBookmarkable())
			{
				Url url = newUrl(page.getClass());
				PageInfo info = new PageInfo(page);
				encodePageComponentInfo(url, new PageComponentInfo(info, null));
				return encodePageParameters(url, page.getPageParameters(), pageParametersEncoder);
			}
		}
		else if (requestHandler instanceof BookmarkableListenerInterfaceRequestHandler)
		{
			BookmarkableListenerInterfaceRequestHandler handler = (BookmarkableListenerInterfaceRequestHandler)requestHandler;
			IPage page = handler.getPage();
			PageInfo pageInfo = new PageInfo(page);
			ComponentInfo componentInfo = new ComponentInfo(
				requestListenerInterfaceToString(handler.getListenerInterface()),
				handler.getComponent().getPath());
			Url url = newUrl(page.getClass());
			encodePageComponentInfo(url, new PageComponentInfo(pageInfo, componentInfo));
			return encodePageParameters(url, page.getPageParameters(), pageParametersEncoder);
		}

		return null;
	}

	public int getMachingSegmentsCount(Request request)
	{
		return 0;
	}

}
