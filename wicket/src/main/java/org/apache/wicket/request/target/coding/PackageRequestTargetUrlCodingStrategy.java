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
package org.apache.wicket.request.target.coding;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.PageParameters;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.util.lang.Classes;
import org.apache.wicket.util.lang.PackageName;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Encodes and decodes mounts for a whole package.
 * 
 * @author Eelco Hillenius
 * @author Igor Vaynberg
 */
public class PackageRequestTargetUrlCodingStrategy extends AbstractRequestTargetUrlCodingStrategy
{
	private static final Logger log = LoggerFactory.getLogger(PackageRequestTargetUrlCodingStrategy.class);

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
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#decode(org.apache.wicket.request.RequestParameters)
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
		else
		{
			// There is nothing after the mount path!
			return null;
		}

		final String bookmarkablePageClassName = packageName + "." + remainder.substring(0, ix);
		Class bookmarkablePageClass;
		try
		{
			bookmarkablePageClass = Session.get().getClassResolver().resolveClass(
				bookmarkablePageClassName);
		}
		catch (Exception e)
		{
			log.debug(e.getMessage());
			return null;
		}
		PageParameters parameters = new PageParameters(decodeParameters(parametersFragment,
			requestParameters.getParameters()));

		String pageMapName = (String)parameters.remove(WebRequestCodingStrategy.PAGEMAP);
		pageMapName = WebRequestCodingStrategy.decodePageMapName(pageMapName);
		requestParameters.setPageMapName(pageMapName);

		// do some extra work for checking whether this is a normal request to a
		// bookmarkable page, or a request to a stateless page (in which case a
		// wicket:interface parameter should be available
		final String interfaceParameter = (String)parameters.remove(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME);

		if (interfaceParameter != null)
		{
			WebRequestCodingStrategy.addInterfaceParameters(interfaceParameter, requestParameters);
			return new BookmarkableListenerInterfaceRequestTarget(pageMapName,
				bookmarkablePageClass, parameters, requestParameters.getComponentPath(),
				requestParameters.getInterfaceName(), requestParameters.getVersionNumber());
		}
		else
		{
			return new BookmarkablePageRequestTarget(pageMapName, bookmarkablePageClass, parameters);
		}
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#encode(org.apache.wicket.IRequestTarget)
	 */
	public final CharSequence encode(IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof IBookmarkablePageRequestTarget))
		{
			throw new IllegalArgumentException("this encoder can only be used with instances of " +
				IBookmarkablePageRequestTarget.class.getName());
		}
		AppendingStringBuffer url = new AppendingStringBuffer(40);
		url.append(getMountPath());
		IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;
		url.append("/").append(Classes.simpleName(target.getPageClass()));

		PageParameters pageParameters = target.getPageParameters();
		if (target.getPageMapName() != null)
		{
			pageParameters.put(WebRequestCodingStrategy.PAGEMAP,
				WebRequestCodingStrategy.encodePageMapName(target.getPageMapName()));
		}

		appendParameters(url, pageParameters);
		return url;
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#matches(org.apache.wicket.IRequestTarget)
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
	@Override
	public String toString()
	{
		return "PackageEncoder[package=" + packageName + "]";
	}
}
