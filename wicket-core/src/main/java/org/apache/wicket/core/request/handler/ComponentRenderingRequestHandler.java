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

import org.apache.wicket.Component;
import org.apache.wicket.Page;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.component.IRequestableComponent;
import org.apache.wicket.request.http.WebResponse;

/**
 * Request handler that renders a component
 * 
 * @author igor.vaynberg
 */
public class ComponentRenderingRequestHandler implements IComponentRequestHandler
{
	private final Component component;

	/**
	 * Construct.
	 * 
	 * @param component
	 *            the component to render
	 */
	public ComponentRenderingRequestHandler(Component component)
	{
		this.component = component;
	}

	@Override
	public IRequestableComponent getComponent()
	{
		return component;
	}

	@Override
	public void detach(IRequestCycle requestCycle)
	{
		component.getPage().detach();
	}

	@Override
	public void respond(IRequestCycle requestCycle)
	{
		// preventing the response to component from being cached
		if (requestCycle.getResponse() instanceof WebResponse)
		{
			WebResponse response = (WebResponse)requestCycle.getResponse();
			response.disableCaching();
		}

		Page page = component.getPage();

		page.startComponentRender(component);

		component.render();

		page.endComponentRender(component);
	}

	@Override
	public final String getComponentPath()
	{
		return component.getPageRelativePath();
	}

}
