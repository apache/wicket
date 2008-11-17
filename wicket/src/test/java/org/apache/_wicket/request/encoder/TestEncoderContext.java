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
import org.apache._wicket.MockPage;
import org.apache._wicket.PageParameters;
import org.apache._wicket.resource.ResourceReferenceRegistry;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.WicketRuntimeException;

/**
 * Simple {@link EncoderContext} implementation for testing purposes
 * 
 * @author Matej Knopp
 */
public class TestEncoderContext implements EncoderContext
{

	/**
	 * Construct.
	 */
	public TestEncoderContext()
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
	
	private ResourceReferenceRegistry registry = new ResourceReferenceRegistry();
	
	private boolean bookmarkable = true;

	/**
	 * Determines whether the newly created page will have bookarkable flag set
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

	public IPage getPageInstance(String pageMapName, int pageId, int versionNumber)
	{
		MockPage page = new MockPage();
		page.setPageId(pageId);
		page.setPageMapName(pageMapName);
		page.setPageVersionNumber(versionNumber);
		page.setBookmarkable(bookmarkable);
		page.setCreatedBookmarkable(createdBookmarkable);
		return page;
	}

	int idCounter = 0;

	public IPage newPageInstance(String pageMapName, Class<? extends IPage> pageClass,
		PageParameters pageParameters)
	{		
		try
		{
			MockPage page;
			page = (MockPage)pageClass.newInstance();
			page.setPageId(++idCounter);
			page.setPageMapName(pageMapName);
			page.setBookmarkable(true);
			page.setCreatedBookmarkable(true);
			page.getPageParameters().assign(pageParameters);
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
	
	public Class<? extends IPage> getHomePageClass()
	{
		return MockPage.class;
	}

}
