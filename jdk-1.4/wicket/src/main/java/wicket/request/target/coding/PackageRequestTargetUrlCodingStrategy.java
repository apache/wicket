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
package wicket.request.target.coding;

import wicket.IRequestTarget;
import wicket.PageParameters;
import wicket.Session;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.request.RequestParameters;
import wicket.request.target.component.BookmarkablePageRequestTarget;
import wicket.request.target.component.IBookmarkablePageRequestTarget;
import wicket.util.lang.Classes;
import wicket.util.lang.PackageName;
import wicket.util.string.AppendingStringBuffer;

/**
 * Encodes and decodes mounts for a whole package.
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg
 */
public class PackageRequestTargetUrlCodingStrategy extends AbstractRequestTargetUrlCodingStrategy
{
	/** package for this mount. */
	private final PackageName packageName;

	/**
	 * Construct.
	 * 
	 * @param path
	 *            the mount path
	 * @param packageName
	 *            The name of the package to mount
	 */
	public PackageRequestTargetUrlCodingStrategy(final String path, PackageName packageName)
	{
		super(path);
		this.packageName = packageName;
	}

	/**
	 * @see wicket.request.target.coding.IRequestTargetUrlCodingStrategy#decode(wicket.request.RequestParameters)
	 */
	public IRequestTarget decode(RequestParameters requestParameters)
	{
		String remainder = requestParameters.getPath().substring(getMountPath().length());
		final String parametersFragment;
		int ix = remainder.indexOf('/', 1);
		if (ix == -1)
		{
			ix = remainder.length();
			parametersFragment = "";
		}
		else
		{
			parametersFragment = remainder.substring(ix);
		}

		if (remainder.startsWith("/"))
		{
			remainder = remainder.substring(1);
			ix--;
		}

		final String bookmarkablePageClassName = packageName + "." + remainder.substring(0, ix);
		Class bookmarkablePageClass = Session.get().getClassResolver().resolveClass(
				bookmarkablePageClassName);
		PageParameters parameters = new PageParameters(decodeParameters(parametersFragment,
				requestParameters.getParameters()));

		final String pageMapName = (String)parameters.remove(WebRequestCodingStrategy.PAGEMAP);
		requestParameters.setPageMapName(pageMapName);

		BookmarkablePageRequestTarget target = new BookmarkablePageRequestTarget(pageMapName,
				bookmarkablePageClass, parameters);
		return target;
	}

	/**
	 * @see wicket.request.target.coding.IRequestTargetUrlCodingStrategy#encode(wicket.IRequestTarget)
	 */
	public final CharSequence encode(IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof IBookmarkablePageRequestTarget))
		{
			throw new IllegalArgumentException("this encoder can only be used with instances of "
					+ IBookmarkablePageRequestTarget.class.getName());
		}
		AppendingStringBuffer url = new AppendingStringBuffer(40);
		url.append(getMountPath());
		IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;
		url.append("/").append(Classes.simpleName(target.getPageClass()));

		PageParameters pageParameters = target.getPageParameters();
		if (target.getPageMapName() != null)
		{
			pageParameters.put(WebRequestCodingStrategy.PAGEMAP, target.getPageMapName());
		}

		appendParameters(url, pageParameters);
		return url;
	}

	/**
	 * @see wicket.request.target.coding.IRequestTargetUrlCodingStrategy#matches(wicket.IRequestTarget)
	 */
	public boolean matches(IRequestTarget requestTarget)
	{
		if (requestTarget instanceof IBookmarkablePageRequestTarget)
		{
			IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;
			if (packageName.equals(PackageName.forClass(target.getPageClass())))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "PackageEncoder[package=" + packageName + "]";
	}
}
