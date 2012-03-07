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
package org.apache.wicket.core.request.handler.logger;

import org.apache.wicket.Page;
import org.apache.wicket.core.request.handler.IPageClassRequestHandler;
import org.apache.wicket.core.request.handler.IPageProvider;
import org.apache.wicket.core.request.handler.IPageRequestHandler;
import org.apache.wicket.request.ILogData;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Contains logging data for request handlers that are related to pages; most likely
 * {@link IPageRequestHandler} or {@link IPageClassRequestHandler}.
 *
 * @author Emond Papegaaij
 */
public class PageLogData implements ILogData
{
	private static final long serialVersionUID = 1L;

	private final Class<? extends IRequestablePage> pageClass;
	private final Integer pageId;
	private final PageParameters pageParameters;
	private final Integer renderCount;

	/**
	 * Construct.
	 *
	 * @param pageProvider
	 */
	public PageLogData(IPageProvider pageProvider)
	{
		pageClass = tryToGetPageClass(pageProvider);
		pageId = pageProvider.getPageId();
		pageParameters = pageProvider.getPageParameters();
		renderCount = pageProvider.getRenderCount();
	}

	private static Class<? extends IRequestablePage> tryToGetPageClass(IPageProvider pageProvider)
	{
		try
		{
			return pageProvider.getPageClass();
		}
		catch (Exception e)
		{
			// getPageClass might fail if the page does not exist (ie session timeout)
			return null;
		}
	}

	/**
	 * Construct.
	 *
	 * @param page
	 */
	public PageLogData(Page page)
	{
		pageClass = page.getPageClass();
		pageId = page.getPageId();
		pageParameters = page.getPageParameters();
		renderCount = page.getRenderCount();
	}

	/**
	 * @return pageClass
	 */
	public final Class<? extends IRequestablePage> getPageClass()
	{
		return pageClass;
	}

	/**
	 * @return pageId
	 */
	public final Integer getPageId()
	{
		return pageId;
	}

	/**
	 * @return pageParameters
	 */
	public final PageParameters getPageParameters()
	{
		return pageParameters;
	}

	/**
	 * @return renderCount
	 */
	public final Integer getRenderCount()
	{
		return renderCount;
	}

	@Override
	public String toString()
	{
		StringBuilder sb = new StringBuilder("{");
		if (pageClass != null)
		{
			sb.append("pageClass=");
			sb.append(getPageClass().getName());
			sb.append(',');
		}
		sb.append("pageId=");
		sb.append(getPageId());
		sb.append(",pageParameters={");
		sb.append(getPageParameters());
		sb.append("},renderCount=");
		sb.append(getRenderCount());
		sb.append("}");
		return sb.toString();
	}
}
