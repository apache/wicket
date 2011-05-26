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
package org.apache.wicket;

import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.IMapperContext;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;

/**
 * Wicket's default implementation for the mapper context
 */
public class DefaultMapperContext implements IMapperContext
{
	/**
	 * @see org.apache.wicket.request.mapper.IMapperContext#getBookmarkableIdentifier()
	 */
	public String getBookmarkableIdentifier()
	{
		return "bookmarkable";
	}

	/**
	 * 
	 * @see org.apache.wicket.request.mapper.IMapperContext#getNamespace()
	 */
	public String getNamespace()
	{
		return MarkupParser.WICKET;
	}

	/**
	 * 
	 * @see org.apache.wicket.request.mapper.IMapperContext#getPageIdentifier()
	 */
	public String getPageIdentifier()
	{
		return "page";
	}

	/**
	 * 
	 * @see org.apache.wicket.request.mapper.IMapperContext#getResourceIdentifier()
	 */
	public String getResourceIdentifier()
	{
		return "resource";
	}

	/**
	 * 
	 * @see org.apache.wicket.request.mapper.IMapperContext#getResourceReferenceRegistry()
	 */
	public ResourceReferenceRegistry getResourceReferenceRegistry()
	{
		return Application.get().getResourceReferenceRegistry();
	}

	/**
	 * 
	 * @see org.apache.wicket.request.mapper.IMapperContext#requestListenerInterfaceFromString(java.lang.String)
	 */
	public RequestListenerInterface requestListenerInterfaceFromString(final String interfaceName)
	{
		return RequestListenerInterface.forName(interfaceName);
	}

	/**
	 * 
	 * @see org.apache.wicket.request.mapper.IMapperContext#requestListenerInterfaceToString(org.apache.wicket.RequestListenerInterface)
	 */
	public String requestListenerInterfaceToString(final RequestListenerInterface listenerInterface)
	{
		return listenerInterface.getName();
	}

	/**
	 * 
	 * @see org.apache.wicket.request.mapper.IPageSource#newPageInstance(java.lang.Class,
	 *      org.apache.wicket.request.mapper.parameter.PageParameters)
	 */
	public IRequestablePage newPageInstance(final Class<? extends IRequestablePage> pageClass,
		final PageParameters pageParameters)
	{
		if (pageParameters == null)
		{
			return Application.get().getPageFactory().newPage(pageClass);
		}
		else
		{
			return Application.get().getPageFactory().newPage(pageClass, pageParameters);
		}
	}

	/**
	 * 
	 * @see org.apache.wicket.request.mapper.IPageSource#getPageInstance(int)
	 */
	public IRequestablePage getPageInstance(final int pageId)
	{
		IManageablePage manageablePage = Session.get().getPageManager().getPage(pageId);
		IRequestablePage requestablePage = null;
		if (manageablePage instanceof IRequestablePage)
		{
			requestablePage = (IRequestablePage)manageablePage;
		}
		return requestablePage;
	}

	/**
	 * 
	 * @see org.apache.wicket.request.mapper.IMapperContext#getHomePageClass()
	 */
	public Class<? extends IRequestablePage> getHomePageClass()
	{
		return Application.get().getHomePage();
	}
}