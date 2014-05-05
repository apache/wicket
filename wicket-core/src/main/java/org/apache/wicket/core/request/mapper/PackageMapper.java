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

import java.lang.reflect.Modifier;

import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.request.mapper.parameter.IPageParametersEncoder;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.mapper.parameter.PageParametersEncoder;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.PackageName;

/**
 * A request mapper that mounts all bookmarkable pages in a given package.
 * <p>
 * To mount this mapper onto a path use the {@link WebApplication#mountPackage(String, Class)}, ex:
 *
 * <pre>
 * MyApp#init() {
 *
 *   super.init();
 *   mountPackage(&quot;/my/path&quot;, MyPage.class);
 * }
 * </pre>
 *
 * will result in urls like {@code /my/path/MyPage}
 * </p>
 *
 * <pre>
 *  Page Class - Render (BookmarkablePageRequestHandler)
 *  /MyPage
 *  (will redirect to hybrid alternative if page is not stateless)
 *
 *  Page Instance - Render Hybrid (RenderPageRequestHandler for pages that were created using bookmarkable URLs)
 *  /MyPage?2
 *
 *  Page Instance - Bookmarkable Listener (BookmarkableListenerInterfaceRequestHandler)
 *  /MyPage?2-click-foo-bar-baz
 *  /MyPage?2-click.1-foo-bar-baz (1 is behavior index)
 *  (these will redirect to hybrid if page is not stateless)
 * </pre>
 */
public class PackageMapper extends AbstractBookmarkableMapper
{
	/**
	 * the name of the package for which all bookmarkable pages should be mounted
	 */
	private final PackageName packageName;

	/** the encoder used to encode/decode the page parameters */
	private final IPageParametersEncoder pageParametersEncoder;

	/**
	 * Construct.
	 *
	 * @param packageName
	 */
	public PackageMapper(final PackageName packageName)
	{
		this(packageName, new PageParametersEncoder());
	}

	/**
	 * Construct.
	 *
	 * @param packageName
	 * @param pageParametersEncoder
	 */
	public PackageMapper(final PackageName packageName,
		final IPageParametersEncoder pageParametersEncoder)
	{
		Args.notNull(packageName, "packageName");
		Args.notNull(pageParametersEncoder, "pageParametersEncoder");

		this.packageName = packageName;
		this.pageParametersEncoder = pageParametersEncoder;
	}

	/**
	 * @see org.apache.wicket.core.request.mapper.AbstractBookmarkableMapper#buildUrl(UrlInfo)
	 */
	@Override
	protected Url buildUrl(UrlInfo info)
	{
		Class<? extends IRequestablePage> pageClass = info.getPageClass();
		PackageName pageClassPackageName = PackageName.forClass(pageClass);
		if (pageClassPackageName.equals(packageName))
		{
			Url url = new Url();

			String fullyQualifiedClassName = pageClass.getName();
			String packageRelativeClassName = fullyQualifiedClassName;
			int packageNameLength = packageName.getName().length();
			if (packageNameLength > 0)
			{
				packageRelativeClassName = fullyQualifiedClassName.substring(packageNameLength + 1);
			}
			packageRelativeClassName = transformForUrl(packageRelativeClassName);
			url.getSegments().add(packageRelativeClassName);
			encodePageComponentInfo(url, info.getPageComponentInfo());
			return encodePageParameters(url, info.getPageParameters(), pageParametersEncoder);
		}

		return null;
	}

	/**
	 * @see org.apache.wicket.core.request.mapper.AbstractBookmarkableMapper#parseRequest(org.apache.wicket.request.Request)
	 */
	@Override
	protected UrlInfo parseRequest(Request request)
	{
		Url url = request.getUrl();
		if (url.getSegments().size() >= 1)
		{
			// try to extract page and component information from URL
			PageComponentInfo info = getPageComponentInfo(url);

			// load the page class
			String name = url.getSegments().get(0);
			String className = cleanClassName(name);

			if (isValidClassName(className) == false)
			{
				return null;
			}

			className = transformFromUrl(className);
			String fullyQualifiedClassName = packageName.getName() + '.' + className;
			Class<? extends IRequestablePage> pageClass = getPageClass(fullyQualifiedClassName);

			if (pageClass != null && Modifier.isAbstract(pageClass.getModifiers()) == false &&
				IRequestablePage.class.isAssignableFrom(pageClass))
			{
				// extract the PageParameters from URL if there are any
				PageParameters pageParameters = extractPageParameters(request, 1,
					pageParametersEncoder);

				return new UrlInfo(info, pageClass, pageParameters);
			}
		}
		return null;
	}

	/**
	 * filter out invalid class names for package mapper. getting trash for class names
	 * can e.g. happen when the home page is in the same package that is mounted by package mapper
	 * but the request was previously mapped by e.g. {@link HomePageMapper}. We then get some
	 * strange url like '/example/..' and wicket tries to look up class name '..'.
	 * <p/>
	 *  @see <a href="https://issues.apache.org/jira/browse/WICKET-4303">WICKET-4303</a>
	 *  <p/>
	 */
	private boolean isValidClassName(String className)
	{
		// darn simple check - feel free to enhance this method to your needs
		if (className == null)
		{
			return false;
		}
		// java class names never start with '.'
		if (className.startsWith("."))
		{
			return false;
		}
		return true;
	}

	/**
	 * Gives a chance to specializations of this mapper to transform the alias of the class name to
	 * the real class name
	 *
	 * @param classNameAlias
	 *            the alias for the class name
	 * @return the real class name
	 */
	protected String transformFromUrl(final String classNameAlias)
	{
		return classNameAlias;
	}

	/**
	 * Gives a chance to specializations of this mapper to transform the real class name to an alias
	 * which is prettier to represent in the Url
	 *
	 * @param className
	 *            the real class name
	 * @return the class name alias
	 */
	protected String transformForUrl(final String className)
	{
		return className;
	}

	/**
	 * @see org.apache.wicket.core.request.mapper.AbstractBookmarkableMapper#pageMustHaveBeenCreatedBookmarkable()
	 */
	@Override
	protected boolean pageMustHaveBeenCreatedBookmarkable()
	{
		return false;
	}

	/**
	 * @see org.apache.wicket.core.request.mapper.AbstractBookmarkableMapper#getCompatibilityScore(org.apache.wicket.request.Request)
	 */
	@Override
	public int getCompatibilityScore(Request request)
	{
		// always return 0 here so that the mounts have higher priority
		return 0;
	}
	
	@Override
	protected boolean checkPageClass(Class<? extends IRequestablePage> pageClass)
	{
		PackageName pageClassPackageName = PackageName.forClass(pageClass);
		return packageName.equals(pageClassPackageName);
	}
}
