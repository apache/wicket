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

import java.util.Locale;

import org.apache.wicket.MockPage;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.IMapperContext;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;

/**
 * Simple {@link EncoderContext} implementation for testing purposes
 * 
 * @author Matej Knopp
 */
public class TestMapperContext implements IMapperContext
{

	/**
	 * Construct.
	 */
	public TestMapperContext()
	{
	}

	public String getBookmarkableIdentifier()
	{
		return "bookmarkable";
	}

	public String getNamespace()
	{
		return "wicket";
	}

	public String getPageIdentifier()
	{
		return "page";
	}

	public String getResourceIdentifier()
	{
		return "resource";
	}

	public ResourceReferenceRegistry getResourceReferenceRegistry()
	{
		return registry;
	}

	private final ResourceReferenceRegistry registry = new ResourceReferenceRegistry()
	{
		@Override
		protected ResourceReference createDefaultResourceReference(Class<?> scope, String name,
			Locale locale, String style, String variation)
		{
			// Do not create package resource here because it requires "real" application
			return null;
		}
	};

	private boolean bookmarkable = true;

	/**
	 * Determines whether the newly created page will have bookmarkable flag set
	 * 
	 * @param bookmarkable
	 */
	public void setBookmarkable(boolean bookmarkable)
	{
		this.bookmarkable = bookmarkable;
	}

	private boolean createdBookmarkable = true;

	/**
	 * Determines whether the newly created page will have createdBookmarkable flag set
	 * 
	 * @param createdBookmarkable
	 */
	public void setCreatedBookmarkable(boolean createdBookmarkable)
	{
		this.createdBookmarkable = createdBookmarkable;
	}

	private int nextPageRenderCount = 0;

	/**
	 * 
	 * @param nextPageRenderCount
	 */
	public void setNextPageRenderCount(int nextPageRenderCount)
	{
		this.nextPageRenderCount = nextPageRenderCount;
	}

	public IRequestablePage getPageInstance(int pageId)
	{
		MockPage page = new MockPage();
		page.setPageId(pageId);
		page.setBookmarkable(bookmarkable);
		page.setCreatedBookmarkable(createdBookmarkable);
		page.setRenderCount(nextPageRenderCount);
		return page;
	}

	int idCounter = 0;

	public IRequestablePage newPageInstance(Class<? extends IRequestablePage> pageClass,
		PageParameters pageParameters)
	{
		try
		{
			MockPage page;
			page = (MockPage)pageClass.newInstance();
			page.setPageId(++idCounter);
			page.setBookmarkable(true);
			page.setCreatedBookmarkable(true);
			if (pageParameters != null)
			{
				page.getPageParameters().assign(pageParameters);
			}
			return page;
		}
		catch (Exception e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	public RequestListenerInterface requestListenerInterfaceFromString(String interfaceName)
	{
		return RequestListenerInterface.forName(interfaceName);
	}

	public String requestListenerInterfaceToString(RequestListenerInterface listenerInterface)
	{
		return listenerInterface.getName();
	}

	public Class<? extends IRequestablePage> getHomePageClass()
	{
		return MockPage.class;
	}

}
