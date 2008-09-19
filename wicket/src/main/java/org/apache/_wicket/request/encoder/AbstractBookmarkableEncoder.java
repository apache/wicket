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
import org.apache._wicket.request.encoder.info.ComponentInfo;
import org.apache._wicket.request.encoder.info.PageComponentInfo;
import org.apache._wicket.request.encoder.info.PageInfo;
import org.apache._wicket.request.handler.impl.BookmarkableListenerInterfaceRequestHandler;
import org.apache._wicket.request.handler.impl.BookmarkablePageRequestHandler;
import org.apache._wicket.request.handler.impl.ListenerInterfaceRequestHandler;
import org.apache._wicket.request.handler.impl.RenderPageRequestHandler;
import org.apache._wicket.request.request.Request;
import org.apache.wicket.RequestListenerInterface;

/**
 * Abstract encoder for Bookmarkable, Hybrid and BookmarkableListenerInterface URLs.
 * 
 * @author Matej Knopp
 */
public abstract class AbstractBookmarkableEncoder extends AbstractEncoder
{
	/**
	 * Construct.
	 */
	public AbstractBookmarkableEncoder()
	{
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

	/**
	 * Represents information stored in URL.
	 * 
	 * @author Matej
	 */
	protected static final class UrlInfo
	{
		private final PageComponentInfo pageComponentInfo;
		private final PageParameters pageParameters;
		private final Class<? extends IPage> pageClass;

		/**
		 * Construct.
		 * 
		 * @param pageComponentInfo
		 *            optional parameter providing the page instance and component information
		 * @param pageClass
		 *            mandatory parameter
		 * @param pageParameters
		 *            optional parameter providing pageParameters
		 */
		public UrlInfo(PageComponentInfo pageComponentInfo, Class<? extends IPage> pageClass,
			PageParameters pageParameters)
		{
			if (pageClass == null)
			{
				throw new IllegalArgumentException("Argument 'pageClass' may not be null.");
			}
			this.pageComponentInfo = pageComponentInfo;
			this.pageParameters = pageParameters != null ? pageParameters : null;
			this.pageClass = pageClass;
		}

		/**
		 * @return PageComponentInfo instance or <code>null</code>
		 */
		public PageComponentInfo getPageComponentInfo()
		{
			return pageComponentInfo;
		}

		/**
		 * @return page class
		 */
		public Class<? extends IPage> getPageClass()
		{
			return pageClass;
		}

		/**
		 * @return PageParameters instance (never <code>null</code>)
		 */
		public PageParameters getPageParameters()
		{
			return pageParameters;
		}
	}

	/**
	 * Parse the given request to an {@link UrlInfo} instance.
	 * 
	 * @param request
	 * @return UrlInfo instance or <code>null</code> if this encoder can not handle the request
	 */
	protected abstract UrlInfo parseRequest(Request request);

	/**
	 * Builds URL for the given {@link UrlInfo} instance. The URL this method produces must be
	 * parseable by the {@link #parseRequest(Request)} method.
	 * 
	 * @param info
	 * @return Url result URL
	 */
	protected abstract Url buildUrl(UrlInfo info);

	public RequestHandler decode(Request request)
	{
		UrlInfo urlInfo = parseRequest(request);

		// check if the URL is long enough and starts with the proper segments
		if (urlInfo != null)
		{
			PageComponentInfo info = urlInfo.getPageComponentInfo();
			Class<? extends IPage> pageClass = urlInfo.getPageClass();
			PageParameters pageParameters = urlInfo.getPageParameters();

			if (info == null || info.getPageInfo().getPageId() == null)
			{
				// if there are is no page instance information (only page map name - optionally)
				// then this is a simple bookmarkable URL
				String pageMap = info != null ? info.getPageInfo().getPageMapName() : null;
				return processBookmarkable(pageMap, pageClass, pageParameters);
			}
			else if (info.getPageInfo().getPageId() != null && info.getComponentInfo() == null)
			{
				// if there is page instance information in the URL but no component and listener
				// interface then this is a hybrid URL - we need to try to reuse existing page
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

	/**
	 * Indicates whether hybrid {@link RenderPageRequestHandler} URL for page will be generated only
	 * if page has been created with bookmarkable URL.
	 * <p>
	 * Generic bookmarkable encoders this method should return <code>true</code>. For explicit
	 * (mounted) encoders this method should return <code>false</code>
	 * 
	 * @return <code>true</code> if hybrid URL requires page created bookmarkable,
	 *         <code>false</code> otherwise.
	 */
	protected abstract boolean pageMustHaveBeenCreatedBookmarkable();

	public Url encode(RequestHandler requestHandler)
	{
		if (requestHandler instanceof BookmarkablePageRequestHandler)
		{
			// simple bookmarkable URL with no page instance information
			BookmarkablePageRequestHandler handler = (BookmarkablePageRequestHandler)requestHandler;

			PageInfo info = new PageInfo(null, null, handler.getPageMapName());
			UrlInfo urlInfo = new UrlInfo(new PageComponentInfo(info, null),
				handler.getPageClass(), handler.getPageParameters());

			return buildUrl(urlInfo);
		}
		else if (requestHandler instanceof RenderPageRequestHandler)
		{
			// possibly hybrid URL - bookmarkable URL with page instance information
			// but only allowed if the page was created by bookamarkable URL

			RenderPageRequestHandler handler = (RenderPageRequestHandler)requestHandler;
			IPage page = handler.getPage();

			if (!pageMustHaveBeenCreatedBookmarkable() || page.wasCreatedBookmarkable())
			{
				PageInfo info = null;
				if (!page.isPageStateless())
				{
					info = new PageInfo(page);
				}
				PageComponentInfo pageComponentInfo = info != null ? new PageComponentInfo(info,
					null) : null;

				UrlInfo urlInfo = new UrlInfo(pageComponentInfo, page.getClass(),
					handler.getPageParameters());
				return buildUrl(urlInfo);
			}
			else
			{
				return null;
			}

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

			UrlInfo urlInfo = new UrlInfo(new PageComponentInfo(pageInfo, componentInfo),
				page.getClass(), handler.getPageParameters());
			return buildUrl(urlInfo);
		}

		return null;
	}

	public abstract int getMachingSegmentsCount(Request request);

	/**
	 * Convenience method for representing mountPath as array of segments
	 * 
	 * @param mountPath
	 * @return array of path segments
	 */
	protected String[] getMountSegments(String mountPath)
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
}
