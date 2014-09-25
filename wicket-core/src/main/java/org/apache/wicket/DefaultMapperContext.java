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

import org.apache.wicket.core.request.mapper.IMapperContext;
import org.apache.wicket.markup.MarkupParser;
import org.apache.wicket.page.IManageablePage;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;

/**
 * Wicket's default implementation for the mapper context
 */
public class DefaultMapperContext implements IMapperContext
{
	private final Application application;

	/**
	 * Constructor.
	 *
	 * Uses the threal local Application
	 */
	public DefaultMapperContext()
	{
		this(Application.get());
	}

	/**
	 * Constructor.
	 *
	 * @param application
	 *      the application instance to use
	 */
	public DefaultMapperContext(final Application application)
	{
		this.application = application;
	}

	@Override
	public String getBookmarkableIdentifier()
	{
		return "bookmarkable";
	}

	@Override
	public String getNamespace()
	{
		return MarkupParser.WICKET;
	}

	@Override
	public String getPageIdentifier()
	{
		return "page";
	}

	@Override
	public String getResourceIdentifier()
	{
		return "resource";
	}

	@Override
	public ResourceReferenceRegistry getResourceReferenceRegistry()
	{
		return application.getResourceReferenceRegistry();
	}

	@Override
	public RequestListenerInterface requestListenerInterfaceFromString(final String interfaceName)
	{
		return RequestListenerInterface.forName(interfaceName);
	}

	@Override
	public String requestListenerInterfaceToString(final RequestListenerInterface listenerInterface)
	{
		return listenerInterface.getName();
	}

	@Override
	public IRequestablePage newPageInstance(final Class<? extends IRequestablePage> pageClass,
		final PageParameters pageParameters)
	{
		if (pageParameters == null)
		{
			return application.getPageFactory().newPage(pageClass);
		}
		else
		{
			return application.getPageFactory().newPage(pageClass, pageParameters);
		}
	}

	@Override
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

	@Override
	public Class<? extends IRequestablePage> getHomePageClass()
	{
		return application.getHomePage();
	}
}
