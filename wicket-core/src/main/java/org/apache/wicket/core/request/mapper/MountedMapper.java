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

import org.apache.wicket.Application;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.core.request.handler.ListenerInterfaceRequestHandler;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.ComponentInfo;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.info.PageInfo;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.util.ClassProvider;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * Encoder for mounted URL. The mount path can contain parameter placeholders, i.e.
 * <code>/mount/${foo}/path</code>. In that case the appropriate segment from the URL will be
 * accessible as named parameter "foo" in the {@link PageParameters}. Similarly when the URL is
 * constructed, the second segment will contain the value of the "foo" named page parameter.
 * Optional parameters are denoted by using a # instead of $: <code>/mount/#{foo}/path/${bar}</code>
 * has an optional {@code foo} parameter, a fixed {@code /path/} part and a required {@code bar}
 * parameter. When in doubt, parameters are matched from left to right, where required parameters
 * are matched before optional parameters, and optional parameters eager (from left to right).
 * <p>
 * Decodes and encodes the following URLs:
 *
 * <pre>
 *  Page Class - Render (BookmarkablePageRequestHandler for mounted pages)
 *  /mount/point
 *  (these will redirect to hybrid alternative if page is not stateless)
 *
 *  IPage Instance - Render Hybrid (RenderPageRequestHandler for mounted pages)
 *  /mount/point?2
 *
 *  IPage Instance - Bookmarkable Listener (BookmarkableListenerInterfaceRequestHandler for mounted pages)
 *  /mount/point?2-click-foo-bar-baz
 *  /mount/point?2-5.click.1-foo-bar-baz (1 is behavior index, 5 is render count)
 *  (these will redirect to hybrid if page is not stateless)
 * </pre>
 *
 * @author Matej Knopp
 */
public class MountedMapper extends AbstractBookmarkableMapper
{
	/** bookmarkable page class. */
	private final ClassProvider<? extends IRequestablePage> pageClassProvider;

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param pageClass
	 */
	public MountedMapper(String mountPath, Class<? extends IRequestablePage> pageClass)
	{
		this(mountPath, pageClass, new PageParametersEncoder());
	}

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param pageClassProvider
	 */
	public MountedMapper(String mountPath,
		ClassProvider<? extends IRequestablePage> pageClassProvider)
	{
		this(mountPath, pageClassProvider, new PageParametersEncoder());
	}

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param pageClass
	 * @param pageParametersEncoder
	 */
	public MountedMapper(String mountPath, Class<? extends IRequestablePage> pageClass,
		IPageParametersEncoder pageParametersEncoder)
	{
		this(mountPath, ClassProvider.of(pageClass), pageParametersEncoder);
	}

	/**
	 * Construct.
	 *
	 * @param mountPath
	 * @param pageClassProvider
	 * @param pageParametersEncoder
	 */
	public MountedMapper(String mountPath,
		ClassProvider<? extends IRequestablePage> pageClassProvider,
		IPageParametersEncoder pageParametersEncoder)
	{
		super(mountPath, pageParametersEncoder);

		Args.notNull(pageClassProvider, "pageClassProvider");

		this.pageClassProvider = pageClassProvider;
	}

	@Override
	protected UrlInfo parseRequest(Request request)
	{
		Url url = request.getUrl();

		// when redirect to buffer/render is active and redirectFromHomePage returns true
		// check mounted class against the home page class. if it matches let wicket redirect
		// to the mounted URL
		if (redirectFromHomePage() && checkHomePage(url))
		{
			return new UrlInfo(null, getContext().getHomePageClass(), newPageParameters());
		}
		// check if the URL starts with the proper segments
		else if (urlStartsWith(url, mountSegments))
		{
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);
			Class<? extends IRequestablePage> pageClass = getPageClass();
			PageParameters pageParameters = extractPageParameters(request, url);

			return new UrlInfo(info, pageClass, pageParameters);
		}
		else
		{
			return null;
		}
	}

	@Override
	protected boolean urlStartsWith(Url url, String... segments)
	{
		if (url == null)
		{
			return false;
		}
		else
		{
			return getMatchedSegmentSizes(url) != null;
		}
	}

	protected PageParameters newPageParameters()
	{
		return new PageParameters();
	}

	@Override
	public Url mapHandler(IRequestHandler requestHandler)
	{
		Url url = super.mapHandler(requestHandler);

		if (url == null && requestHandler instanceof ListenerInterfaceRequestHandler &&
			getRecreateMountedPagesAfterExpiry())
		{
			ListenerInterfaceRequestHandler handler = (ListenerInterfaceRequestHandler)requestHandler;
			IRequestablePage page = handler.getPage();
			if (checkPageInstance(page))
			{
				String componentPath = handler.getComponentPath();
				RequestListenerInterface listenerInterface = handler.getListenerInterface();

				Integer renderCount = null;
				if (listenerInterface.isIncludeRenderCount())
				{
					renderCount = page.getRenderCount();
				}

				PageInfo pageInfo = getPageInfo(handler);
				ComponentInfo componentInfo = new ComponentInfo(renderCount,
					requestListenerInterfaceToString(listenerInterface), componentPath,
					handler.getBehaviorIndex());
				PageComponentInfo pageComponentInfo = new PageComponentInfo(pageInfo, componentInfo);
				PageParameters parameters = new PageParameters(page.getPageParameters());
				UrlInfo urlInfo = new UrlInfo(pageComponentInfo, page.getClass(),
					parameters.mergeWith(handler.getPageParameters()));
				url = buildUrl(urlInfo);
			}
		}

		return url;
	}

	boolean getRecreateMountedPagesAfterExpiry()
	{
		return Application.get().getPageSettings().getRecreateMountedPagesAfterExpiry();
	}

	/**
	 * @see AbstractBookmarkableMapper#buildUrl(AbstractBookmarkableMapper.UrlInfo)
	 */
	@Override
	protected Url buildUrl(UrlInfo info)
	{
		Url url = new Url();
		for (String s : mountSegments)
		{
			url.getSegments().add(s);
		}
		encodePageComponentInfo(url, info.getPageComponentInfo());

		PageParameters copy = new PageParameters(info.getPageParameters());
		setPlaceholders(copy, url);
		return encodePageParameters(url, copy, pageParametersEncoder);
	}

	/**
	 * Check if the URL is for home page and the home page class match mounted class. If so,
	 * redirect to mounted URL.
	 *
	 * @param url
	 * @return request handler or <code>null</code>
	 */
	private boolean checkHomePage(Url url)
	{
		if (url.getSegments().isEmpty() && url.getQueryParameters().isEmpty())
		{
			// this is home page
			if (getPageClass().equals(getContext().getHomePageClass()) && redirectFromHomePage())
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * If this method returns <code>true</code> and application home page class is same as the class
	 * mounted with this encoder, request to home page will result in a redirect to the mounted
	 * path.
	 *
	 * @return whether this encode should respond to home page request when home page class is same
	 *         as mounted class.
	 */
	protected boolean redirectFromHomePage()
	{
		return true;
	}

	/**
	 * @see AbstractBookmarkableMapper#pageMustHaveBeenCreatedBookmarkable()
	 */
	@Override
	protected boolean pageMustHaveBeenCreatedBookmarkable()
	{
		return false;
	}

	/**
	 * @see AbstractBookmarkableMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	@Override
	public int getCompatibilityScore(Request request)
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

	/**
	 * @see AbstractBookmarkableMapper#checkPageClass(java.lang.Class)
	 */
	@Override
	protected boolean checkPageClass(Class<? extends IRequestablePage> pageClass)
	{
		return pageClass.equals(this.getPageClass());
	}

	private Class<? extends IRequestablePage> getPageClass()
	{
		return pageClassProvider.get();
	}

	@Override
	public String toString()
	{
		return "MountedMapper [mountSegments=" + Strings.join("/", mountSegments) + "]";
	}
}
