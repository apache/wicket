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

import org.apache._wicket.IPage;
import org.apache._wicket.PageParameters;
import org.apache._wicket.request.Url;
import org.apache._wicket.request.encoder.info.PageComponentInfo;
import org.apache._wicket.request.encoder.parameters.PageParametersEncoder;
import org.apache._wicket.request.encoder.parameters.SimplePageParametersEncoder;
import org.apache._wicket.request.request.Request;

/**
 * Decodes and encodes the following URLs:
 * 
 * <pre>
 *  Page Class - Render (BookmarkablePageRequestHandler)
 *  /wicket/bookmarkable/org.apache.wicket.MyPage
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?pageMap
 *  (these will redirect to hybrid alternative if page is not stateless)
 * 
 *  Page Instance - Render Hybrid (RenderPageRequestHandler for pages that were created using bookmarkable URLs)
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2.4
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?pageMap.2.4
 * 
 *  Page Instance - Bookmarkable Listener (BookmarkableListenerInterfaceRequestHandler)
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2-click-foo-bar-baz
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2.4-click-foo-bar-baz
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?pageMap.2.4-click-foo-bar-baz
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2.4-click.1-foo-bar-baz (1 is behavior index)
 *  (these will redirect to hybrid if page is not stateless)
 * </pre>
 * 
 * @author Matej Knopp
 */
public class BookmarkableEncoder extends AbstractBookmarkableEncoder
{
	private final PageParametersEncoder pageParametersEncoder;

	/**
	 * Construct.
	 * 
	 * @param pageParametersEncoder
	 */
	public BookmarkableEncoder(PageParametersEncoder pageParametersEncoder)
	{
		if (pageParametersEncoder == null)
		{
			throw new IllegalArgumentException("Argument 'pageParametersEncoder' may not be null.");
		}
		this.pageParametersEncoder = pageParametersEncoder;
	}

	/**
	 * Construct.
	 */
	public BookmarkableEncoder()
	{
		this(new SimplePageParametersEncoder());
	}

	@Override
	protected Url buildUrl(UrlInfo info)
	{
		Url url = new Url();
		url.getSegments().add(getContext().getNamespace());
		url.getSegments().add(getContext().getBookmarkableIdentifier());
		url.getSegments().add(info.getPageClass().getName());

		encodePageComponentInfo(url, info.getPageComponentInfo());

		return encodePageParameters(url, info.getPageParameters(), pageParametersEncoder);
	}

	@Override
	protected UrlInfo parseRequest(Request request)
	{
		Url url = request.getUrl();
		if (url.getSegments().size() >= 3 &&
			urlStartsWith(url, getContext().getNamespace(),
				getContext().getBookmarkableIdentifier()))
		{
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);

			// load the page class
			String className = url.getSegments().get(2);
			Class<? extends IPage> pageClass = getPageClass(className);

			// extract the PageParameters from URL if there are any
			PageParameters pageParameters = extractPageParameters(url,
				request.getRequestParameters(), 3, pageParametersEncoder);

			return new UrlInfo(info, pageClass, pageParameters);
		}
		return null;
	}
	
	@Override
	protected boolean pageMustHaveBeenCreatedBookmarkable()
	{
		return true;
	}

	public int getMachingSegmentsCount(Request request)
	{
		// always return 0 here so that the mounts have higher priority
		return 0;
	}

}
