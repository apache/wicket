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

import java.util.List;

import org.apache.wicket.Application;
import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.ICompoundRequestMapper;
import org.apache.wicket.request.mapper.IRequestMapperDelegate;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.util.string.Strings;

/**
 * Decodes and encodes the following URLs:
 * 
 * <pre>
 *  Page Class - Render (BookmarkablePageRequestHandler)
 *  /wicket/bookmarkable/org.apache.wicket.MyPage
 *  (will redirect to hybrid alternative if page is not stateless)
 * 
 *  Page Instance - Render Hybrid (RenderPageRequestHandler for pages that were created using bookmarkable URLs)
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2
 * 
 *  Page Instance - Bookmarkable Listener (BookmarkableListenerInterfaceRequestHandler)
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2-click-foo-bar-baz
 *  /wicket/bookmarkable/org.apache.wicket.MyPage?2-click.1-foo-bar-baz (1 is behavior index)
 *  (these will redirect to hybrid if page is not stateless)
 * </pre>
 * 
 * @author Matej Knopp
 */
public class BookmarkableMapper extends AbstractBookmarkableMapper
{
	/**
	 * Construct.
	 */
	public BookmarkableMapper()
	{
		this(new PageParametersEncoder());
	}

	/**
	 * Construct.
	 *
	 * @param pageParametersEncoder
	 */
	public BookmarkableMapper(IPageParametersEncoder pageParametersEncoder)
	{
		super("notUsed", pageParametersEncoder);
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
		if (Application.exists())
		{
			if (Application.get().getSecuritySettings().getEnforceMounts())
			{
				return null;
			}
		}

		if (matches(request))
		{
			Url url = request.getUrl();

			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);

			List<String> segments = url.getSegments();

			// load the page class
			String className;
			if (segments.size() >= 3)
			{
				className = segments.get(2);
			}
			else
			{
				className = segments.get(1);
			}

			if (Strings.isEmpty(className))
			{
				return null;
			}

			Class<? extends IRequestablePage> pageClass = getPageClass(className);

			if (pageClass != null && IRequestablePage.class.isAssignableFrom(pageClass))
			{
				// extract the PageParameters from URL if there are any
				PageParameters pageParameters = extractPageParameters(request, 3,
					pageParametersEncoder);

				return new UrlInfo(info, pageClass, pageParameters);
			}
		}
		return null;
	}

	private boolean isPageMounted(Class<? extends IRequestablePage> pageClass, ICompoundRequestMapper compoundMapper)
	{
		for (IRequestMapper requestMapper : compoundMapper)
		{
			while (requestMapper instanceof IRequestMapperDelegate)
			{
				requestMapper = ((IRequestMapperDelegate)requestMapper).getDelegateMapper();
			}

			if (requestMapper instanceof ICompoundRequestMapper)
			{
				if (isPageMounted(pageClass, (ICompoundRequestMapper)requestMapper))
				{
					return true;
				}
			}
			else
			{
				if (requestMapper instanceof AbstractBookmarkableMapper  && requestMapper != this)
				{
					AbstractBookmarkableMapper mapper = (AbstractBookmarkableMapper) requestMapper;

					if (mapper.checkPageClass(pageClass))
					{
						return true;
					}
				}
			}
		}

		return false;
	}

	@Override
	protected boolean pageMustHaveBeenCreatedBookmarkable()
	{
		return true;
	}

	@Override
	public int getCompatibilityScore(Request request)
	{
		int score = 0;
		if (matches(request))
		{
			score = Integer.MAX_VALUE;
		}
		return score;
	}

	private boolean matches(final Request request)
	{
		boolean matches = false;
		Url url = request.getUrl();
		Url baseUrl = request.getClientUrl();
		String namespace = getContext().getNamespace();
		String bookmarkableIdentifier = getContext().getBookmarkableIdentifier();
		String pageIdentifier = getContext().getPageIdentifier();

		List<String> segments = url.getSegments();
		int segmentsSize = segments.size();

		if (segmentsSize >= 3 && urlStartsWithAndHasPageClass(url, namespace, bookmarkableIdentifier))
		{
			matches = true;
		}
		// baseUrl = 'wicket/bookmarkable/com.example.SomePage[?...]', requestUrl = 'bookmarkable/com.example.SomePage'
		else if (baseUrl.getSegments().size() == 3 && urlStartsWith(baseUrl, namespace, bookmarkableIdentifier)
				&& segmentsSize >= 2 && urlStartsWithAndHasPageClass(url, bookmarkableIdentifier))
		{
			matches = true;
		}
		// baseUrl = 'bookmarkable/com.example.SomePage', requestUrl = 'bookmarkable/com.example.SomePage'
		else if (baseUrl.getSegments().size() == 2 && urlStartsWith(baseUrl, bookmarkableIdentifier)
				&& segmentsSize == 2 && urlStartsWithAndHasPageClass(url, bookmarkableIdentifier))
		{
			matches = true;
		}
		// baseUrl = 'wicket/page[?...]', requestUrl = 'bookmarkable/com.example.SomePage'
		else if (baseUrl.getSegments().size() == 2 && urlStartsWith(baseUrl, namespace, pageIdentifier)
				&& segmentsSize >= 2 && urlStartsWithAndHasPageClass(url, bookmarkableIdentifier))
		{
			matches = true;
		}

		return matches;
	}

	/**
	 * Checks whether the url starts with the given segments and additionally
	 * checks whether the following segment is non-empty
	 *
	 * @param url
	 *          The url to be checked
	 * @param segments
	 *          The expected leading segments
	 * @return {@code true} if the url starts with the given segments and there is non-empty segment after them
	 */
	protected boolean urlStartsWithAndHasPageClass(Url url, String... segments)
	{
		boolean result = urlStartsWith(url, segments);

		if (result)
		{
			List<String> urlSegments = url.getSegments();
			if (urlSegments.size() == segments.length)
			{
				result = false;
			}
			else
			{
				String pageClassSegment = urlSegments.get(segments.length);
				if (Strings.isEmpty(pageClassSegment))
				{
					result = false;
				}
			}
		}

		return result;
	}
}
