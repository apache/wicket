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
package org.apache.wicket.request.mapper;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.lang.PackageName;

/**
 * A request mapper that mounts all bookmarkable pages at a given package.
 * 
 * <pre>
 *  Page Class - Render (BookmarkablePageRequestHandler)
 *  /mountPath/MyPage
 *  (will redirect to hybrid alternative if page is not stateless)
 * 
 *  Page Instance - Render Hybrid (RenderPageRequestHandler for pages that were created using bookmarkable URLs)
 *  /mountPath/MyPage?2
 * 
 *  Page Instance - Bookmarkable Listener (BookmarkableListenerInterfaceRequestHandler)
 *  /mountPath/MyPage?2-click-foo-bar-baz
 *  /mountPath/MyPage?2-click.1-foo-bar-baz (1 is behavior index)
 *  (these will redirect to hybrid if page is not stateless)
 * </pre>
 */
public class PackageMapper extends AbstractBookmarkableMapper
{
	/** the path to mount the bookmarkable page classes on */
	private final String mountPath;

	/**
	 * the name of the package for which all bookmarkable pages should be mounted
	 */
	private final PackageName packageName;

	/** the encoder used to encode/decode the page parameters */
	private final IPageParametersEncoder pageParametersEncoder;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param packageName
	 */
	public PackageMapper(final String mountPath, final PackageName packageName)
	{
		this(mountPath, packageName, new PageParametersEncoder());
	}

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 * @param packageName
	 * @param pageParametersEncoder
	 */
	public PackageMapper(final String mountPath, final PackageName packageName,
		final IPageParametersEncoder pageParametersEncoder)
	{
		Checks.argumentNotNull(mountPath, "mountPath");
		Checks.argumentNotNull(packageName, "packageName");
		Checks.argumentNotNull(pageParametersEncoder, "pageParametersEncoder");

		this.mountPath = mountPath;
		this.packageName = packageName;
		this.pageParametersEncoder = pageParametersEncoder;
	}

	/**
	 * @see org.apache.wicket.request.mapper.AbstractBookmarkableMapper#buildUrl(org.apache.wicket.request.mapper.AbstractBookmarkableMapper.UrlInfo)
	 */
	@Override
	protected Url buildUrl(UrlInfo info)
	{
		Class<? extends IRequestablePage> pageClass = info.getPageClass();
		if (pageClass.getName().startsWith(packageName.getName()))
		{
			Url url = new Url();
			url.getSegments().add(mountPath);
			url.getSegments().add(pageClass.getSimpleName());

			encodePageComponentInfo(url, info.getPageComponentInfo());

			return encodePageParameters(url, info.getPageParameters(), pageParametersEncoder);
		}

		return null;
	}

	/**
	 * @see org.apache.wicket.request.mapper.AbstractBookmarkableMapper#parseRequest(org.apache.wicket.request.Request)
	 */
	@Override
	protected UrlInfo parseRequest(Request request)
	{
		Url url = request.getUrl();
		if (url.getSegments().size() >= 1 && urlStartsWith(url, mountPath))
		{
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);

			// load the page class
			String className = url.getSegments().get(1);
			String fullyQualifiedClassName = packageName.getName() + '.' + className;
			Class<? extends IRequestablePage> pageClass = getPageClass(fullyQualifiedClassName);

			if (pageClass != null && IRequestablePage.class.isAssignableFrom(pageClass))
			{
				// extract the PageParameters from URL if there are any
				PageParameters pageParameters = extractPageParameters(request, 2,
					pageParametersEncoder);

				return new UrlInfo(info, pageClass, pageParameters);
			}
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.request.mapper.AbstractBookmarkableMapper#pageMustHaveBeenCreatedBookmarkable()
	 */
	@Override
	protected boolean pageMustHaveBeenCreatedBookmarkable()
	{
		return true;
	}

	/**
	 * @see org.apache.wicket.request.mapper.AbstractBookmarkableMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	@Override
	public int getCompatibilityScore(Request request)
	{
		// always return 0 here so that the mounts have higher priority
		return 0;
	}
}
