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
package org.apache.wicket.core.request.handler;

import org.apache.wicket.Page;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.component.IRequestablePage;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;

/**
 * Extension of {@link PageProvider} that is also capable of providing a Component belonging to the
 * page.
 *
 * @see PageProvider
 *
 * @author Matej Knopp
 */
public class PageAndComponentProvider extends PageProvider implements IPageAndComponentProvider
{
	private IRequestableComponent component;

	private String componentPath;

	/**
	 * @see PageProvider#PageProvider(IRequestablePage)
	 *
	 * @param page
	 * @param componentPath
	 */
	public PageAndComponentProvider(IRequestablePage page, String componentPath)
	{
		super(page);
		setComponentPath(componentPath);
	}

	/**
	 * @see PageProvider#PageProvider(IRequestablePage)
	 *
	 * @param page
	 * @param component
	 */
	public PageAndComponentProvider(IRequestablePage page, IRequestableComponent component)
	{
		super(page);

		Args.notNull(component, "component");

		this.component = component;
	}

	/**
	 * @see PageProvider#PageProvider(Class, PageParameters)
	 *
	 * @param pageClass
	 * @param pageParameters
	 * @param componentPath
	 */
	public PageAndComponentProvider(Class<? extends IRequestablePage> pageClass,
		PageParameters pageParameters, String componentPath)
	{
		super(pageClass, pageParameters);
		setComponentPath(componentPath);
	}

	/**
	 * @see PageProvider#PageProvider(Class)
	 *
	 * @param pageClass
	 * @param componentPath
	 */
	public PageAndComponentProvider(Class<? extends IRequestablePage> pageClass,
		String componentPath)
	{
		super(pageClass);
		setComponentPath(componentPath);
	}

	/**
	 * @see PageProvider#PageProvider(int, Class, Integer)
	 *
	 * @param pageId
	 * @param pageClass
	 * @param renderCount
	 * @param componentPath
	 */
	public PageAndComponentProvider(int pageId, Class<? extends IRequestablePage> pageClass,
		Integer renderCount, String componentPath)
	{
		super(pageId, pageClass, renderCount);
		setComponentPath(componentPath);
	}

	/**
	 * @see PageProvider#PageProvider(int, Class, PageParameters, Integer)
	 *
	 * @param pageId
	 * @param pageClass
	 * @param pageParameters
	 * @param renderCount
	 * @param componentPath
	 */
	public PageAndComponentProvider(int pageId, Class<? extends IRequestablePage> pageClass,
		PageParameters pageParameters, Integer renderCount, String componentPath)
	{
		super(pageId, pageClass, pageParameters, renderCount);
		setComponentPath(componentPath);
	}

	/**
	 * @see PageProvider#PageProvider(int, Integer)
	 *
	 * @param pageId
	 * @param renderCount
	 * @param componentPath
	 */
	public PageAndComponentProvider(int pageId, Integer renderCount, String componentPath)
	{
		super(pageId, renderCount);
		setComponentPath(componentPath);
	}

	public PageAndComponentProvider(IRequestablePage page, IRequestableComponent component,
		PageParameters parameters)
	{
		super(page);

		Args.notNull(component, "component");

		this.component = component;
		if (parameters != null)
		{
			setPageParameters(parameters);
		}
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageAndComponentProvider#getComponent()
	 */
	@Override
	public IRequestableComponent getComponent()
	{
		if (component == null)
		{
			IRequestablePage page = getPageInstance();
			component = page.get(componentPath);
			if (component == null)
			{

				/*
				 * on stateless pages it is possible that the component may not yet exist because it
				 * couldve been created in one of the lifecycle callbacks of this page. Lets invoke
				 * the callbacks to give the page a chance to create the missing component.
				 */

				// make sure this page instance was just created so the page can be stateless
				if (page.isPageStateless())
				{
					Page p = (Page)page;
					p.internalInitialize();
					p.internalPrepareForRender(false);
					component = page.get(componentPath);
				}
			}
		}
		if (component == null)
		{
			throw new ComponentNotFoundException("Could not find component '" + componentPath +
				"' on page '" + getPageClass());
		}
		return component;
	}

	/**
	 * @see org.apache.wicket.request.handler.IPageAndComponentProvider#getComponentPath()
	 */
	@Override
	public String getComponentPath()
	{
		if (componentPath != null)
		{
			return componentPath;
		}
		else
		{
			return component.getPageRelativePath();
		}
	}

	/**
	 *
	 * @param componentPath
	 */
	private void setComponentPath(String componentPath)
	{
		Args.notNull(componentPath, "componentPath");

		this.componentPath = componentPath;
	}
}
