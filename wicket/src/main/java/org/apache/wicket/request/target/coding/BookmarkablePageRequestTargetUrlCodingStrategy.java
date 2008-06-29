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

import java.lang.ref.WeakReference;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.Page;
import org.apache.wicket.PageParameters;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.component.BookmarkableListenerInterfaceRequestTarget;
import org.apache.wicket.request.target.component.BookmarkablePageRequestTarget;
import org.apache.wicket.request.target.component.IBookmarkablePageRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;

/**
 * Encodes and decodes mounts for a single bookmarkable page class.
 * 
 * @author Eelco Hillenius
 */
public class BookmarkablePageRequestTargetUrlCodingStrategy extends
	AbstractRequestTargetUrlCodingStrategy
{
	/** bookmarkable page class. */
	protected final WeakReference<Class<? extends Page>> bookmarkablePageClassRef;

	/** page map name. */
	private final String pageMapName;

	/**
	 * Construct.
	 * 
	 * @param <C>
	 *            type of page.
	 * 
	 * @param mountPath
	 *            the mount path
	 * @param bookmarkablePageClass
	 *            the class of the bookmarkable page
	 * @param pageMapName
	 *            the page map name if any
	 */
	public <C extends Page> BookmarkablePageRequestTargetUrlCodingStrategy(
		final String mountPath, final Class<C> bookmarkablePageClass, String pageMapName)
	{
		super(mountPath);

		if (bookmarkablePageClass == null)
		{
			throw new IllegalArgumentException("Argument bookmarkablePageClass must be not null");
		}

		bookmarkablePageClassRef = new WeakReference<Class<? extends Page>>(
			bookmarkablePageClass);
		this.pageMapName = pageMapName;
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#decode(org.apache.wicket.request.RequestParameters)
	 */
	public IRequestTarget decode(RequestParameters requestParameters)
	{
		final String parametersFragment = requestParameters.getPath().substring(
			getMountPath().length());
		final PageParameters parameters = new PageParameters(decodeParameters(parametersFragment,
			requestParameters.getParameters()));

		// do some extra work for checking whether this is a normal request to a
		// bookmarkable page, or a request to a stateless page (in which case a
		// wicket:interface parameter should be available

		// the page map name can be defined already by logic done in
		// WebRequestCodingStrategy.decode(),
		// but it could also be done by the decodeParameters() call
		// So we always remove the pagemap parameter just in case.
		String pageMapNameEncoded = (String)parameters.remove(WebRequestCodingStrategy.PAGEMAP);
		if (requestParameters.getPageMapName() == null)
		{
			requestParameters.setPageMapName(pageMapNameEncoded);
		}

		// the interface can be defined already by logic done in
		// WebRequestCodingStrategy.decode(),
		// but it could also be done by the decodeParameters() call
		// So we always remove the interface parameter just in case.
		String interfaceParameter = (String)parameters.remove(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME);
		if (requestParameters.getInterfaceName() == null)
		{
			WebRequestCodingStrategy.addInterfaceParameters(interfaceParameter, requestParameters);
		}

		// if an interface name was set prior to this method or in the
		// above block, process it
		if (requestParameters.getInterfaceName() != null)
		{
			return new BookmarkableListenerInterfaceRequestTarget(
				requestParameters.getPageMapName(), bookmarkablePageClassRef.get(), parameters,
				requestParameters.getComponentPath(), requestParameters.getInterfaceName(),
				requestParameters.getVersionNumber());
		}
		// otherwise process as a normal bookmark page request
		else
		{
			return new BookmarkablePageRequestTarget(requestParameters.getPageMapName(),
				(Class<? extends Page>)bookmarkablePageClassRef.get(), parameters);
		}
	}

	/**
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#encode(org.apache.wicket.IRequestTarget)
	 */
	public final CharSequence encode(final IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof IBookmarkablePageRequestTarget))
		{
			throw new IllegalArgumentException("This encoder can only be used with " +
				"instances of " + IBookmarkablePageRequestTarget.class.getName());
		}
		final AppendingStringBuffer url = new AppendingStringBuffer(40);
		url.append(getMountPath());
		final IBookmarkablePageRequestTarget target = (IBookmarkablePageRequestTarget)requestTarget;

		PageParameters pageParameters = target.getPageParameters();
		String pagemap = pageMapName != null ? pageMapName : target.getPageMapName();
		if (pagemap != null)
		{
			if (pageParameters == null)
			{
				pageParameters = new PageParameters();
			}
			pageParameters.put(WebRequestCodingStrategy.PAGEMAP,
				WebRequestCodingStrategy.encodePageMapName(pagemap));
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
			if (((Class<? extends Page>)bookmarkablePageClassRef.get()).equals(target.getPageClass()))
			{
				if (pageMapName == null)
				{
					return true;
				}
				else
				{
					return pageMapName.equals(target.getPageMapName());
				}
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
		return "BookmarkablePageEncoder[page=" + bookmarkablePageClassRef.get() + "]";
	}
}
