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

import java.lang.ref.WeakReference;

import org.apache._wicket.IComponent;
import org.apache._wicket.IPage;
import org.apache._wicket.PageParameters;
import org.apache._wicket.request.RequestHandler;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.encoder.info.ComponentInfo;
import org.apache._wicket.request.encoder.info.PageComponentInfo;
import org.apache._wicket.request.encoder.info.PageInfo;
import org.apache._wicket.request.encoder.parameters.PageParametersEncoder;
import org.apache._wicket.request.encoder.parameters.SimplePageParametersEncoder;
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
 *  Page Class - Render (BookmarkablePageRequestHandler for mounted pages)
 *  /mount/point
 *  /mount/point?pageMap
 *  (these will redirect to hybrid alternative if page is not stateless)
 * 
 *  IPage Instance - Render Hybrid (RenderPageRequestHandler for mounted pages) 
 *  /mount/point?2
 *  /mount/point?2.4
 *  /mount/point?pageMap.2.4
 * 
 *  IPage Instance - Bookmarkable Listener (BookmarkableListenerInterfaceRequestHandler for mounted pages) 
 *  /mount/point?2-click-foo-bar-baz
 *  /mount/point?2.4-click-foo-bar-baz
 *  /mount/point?pageMap.2.4-click-foo-bar-baz
 *  /mount/point?2.4-click.1-foo-bar-baz (1 is behavior index)
 *  (these will redirect to hybrid if page is not stateless)
 * </pre>
 * 
 * @author Matej Knopp
 */
public class MountedEncoder extends AbstractEncoder
{
	private final PageParametersEncoder pageParametersEncoder;
	private final String[] mountSegments;

	/** bookmarkable page class. */
	protected final WeakReference<Class<? extends IPage>> pageClass;


	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param pageClass
	 * @param pageParametersEncoder
	 */
	public MountedEncoder(String mountPath, Class<? extends IPage> pageClass,
		PageParametersEncoder pageParametersEncoder)
	{
		if (pageParametersEncoder == null)
		{
			throw new IllegalArgumentException("Argument 'pageParametersEncoder' may not be null.");
		}
		if (pageClass == null)
		{
			throw new IllegalArgumentException("Argument 'pageClass' may not be null.");
		}
		if (mountPath == null)
		{
			throw new IllegalArgumentException("Argument 'mountPath' may not be null.");
		}
		this.pageParametersEncoder = pageParametersEncoder;
		this.pageClass = new WeakReference<Class<? extends IPage>>(pageClass);
		this.mountSegments = getMountSegments(mountPath);
	}

	private String[] getMountSegments(String mountPath)
	{
		if (mountPath.startsWith("/"))
		{
			mountPath = mountPath.substring(1);
		}
		Url url = Url.parse(mountPath);

		if (url.getSegments().isEmpty())
		{
			throw new IllegalArgumentException("Mount path must have at least one segment.");
		}

		String[] res = new String[url.getSegments().size()];
		for (int i = 0; i < res.length; ++i)
		{
			res[i] = url.getSegments().get(i);
		}
		return res;
	}

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param pageClass
	 */
	public MountedEncoder(String mountPath, Class<? extends IPage> pageClass)
	{
		this(mountPath, pageClass, new SimplePageParametersEncoder());
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

		return new ListenerInterfaceRequestHandler(page, component, listenerInterface,
			componentInfo.getBehaviorIndex());
	}

	public RequestHandler decode(Request request)
	{
		Url url = request.getUrl();

		// check if the URL is long enough and starts with the proper segments
		if (url.getSegments().size() >= mountSegments.length && urlStartsWith(url, mountSegments))
		{
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);

			Class<? extends IPage> pageClass = this.pageClass.get();

			// extract the PageParameters from URL if there are any
			PageParameters pageParameters = extractPageParameters(url,
				request.getRequestParameters(), 3, pageParametersEncoder);

			if (info == null || info.getPageInfo().getPageId() == null)
			{
				// if there are is no page instance information (only page map name - optionally)
				// then this is a simple bookmarkable URL
				String pageMap = info != null ? info.getPageInfo().getPageMapName() : null;
				return processBookmarkable(pageMap, pageClass, pageParameters);
			}
			else if (info.getPageInfo().getPageId() != null && info.getComponentInfo() == null)
			{
				// if there is page instance ifnromation in the URL but no component and listener
				// interface then this is a hybrid URL - we need to try to reuse existing cpage
				// instance
				return processHybrid(info.getPageInfo(), pageClass, pageParameters);
			}
			else if (info.getComponentInfo() != null)
			{
				// with both page instance and component+listener this is a listener interface URL
				return processListener(info, pageClass, pageParameters);
			}
		}
		return null;
	}

	private Url newUrl(Class<? extends IPage> pageClass)
	{
		Url url = new Url();

		for (String s : mountSegments)
		{
			url.getSegments().add(s);
		}

		return url;
	}

	public Url encode(RequestHandler requestHandler)
	{
		if (requestHandler instanceof BookmarkablePageRequestHandler)
		{
			// simple bookmarkable URL with no page instance information
			BookmarkablePageRequestHandler handler = (BookmarkablePageRequestHandler)requestHandler;
			Url url = newUrl(handler.getPageClass());

			PageInfo info = new PageInfo(null, null, handler.getPageMapName());
			encodePageComponentInfo(url, new PageComponentInfo(info, null));
			return encodePageParameters(url, handler.getPageParameters(), pageParametersEncoder);
		}
		else if (requestHandler instanceof RenderPageRequestHandler)
		{
			// possibly hybrid URL - bookmarkable URL with page instance information
			// but only allowed if the page was created by bookamarkable URL

			IPage page = ((RenderPageRequestHandler)requestHandler).getPage();

			Url url = newUrl(page.getClass());
			PageInfo info = null;
			if (!page.isPageStateless())
			{
				info = new PageInfo(page);
				encodePageComponentInfo(url, new PageComponentInfo(info, null));
			}
			return encodePageParameters(url, page.getPageParameters(), pageParametersEncoder);

		}
		else if (requestHandler instanceof BookmarkableListenerInterfaceRequestHandler)
		{
			// listener interface URL with page class information
			BookmarkableListenerInterfaceRequestHandler handler = (BookmarkableListenerInterfaceRequestHandler)requestHandler;
			IPage page = handler.getPage();
			PageInfo pageInfo = new PageInfo(page);
			ComponentInfo componentInfo = new ComponentInfo(
				requestListenerInterfaceToString(handler.getListenerInterface()),
				handler.getComponent().getPath(), handler.getBehaviorIndex());
			Url url = newUrl(page.getClass());
			encodePageComponentInfo(url, new PageComponentInfo(pageInfo, componentInfo));
			return encodePageParameters(url, page.getPageParameters(), pageParametersEncoder);
		}

		return null;
	}

	public int getMachingSegmentsCount(Request request)
	{
		if (urlStartsWith(request.getUrl(), mountSegments))
		{
			return mountSegments.length;
		}
		else
		{
			return 0;
		}
	}

}
