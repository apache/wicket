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
package org.apache.wicket.request.handler.resource;

import org.apache.wicket.Application;
import org.apache.wicket.Session;
import org.apache.wicket.authorization.IAuthorizationStrategy;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.settings.DefaultUnauthorizedResourceRequestListener;
import org.apache.wicket.util.lang.Args;

/**
 * Request handler that renders a resource.
 * 
 * @author Matej Knopp
 */
public class ResourceRequestHandler implements IRequestHandler
{
	private final IResource resource;
	private final PageParameters parameters;

	/**
	 * Construct.
	 * 
	 * @param resource
	 * @param parameters
	 */
	public ResourceRequestHandler(IResource resource, PageParameters parameters)
	{
		Args.notNull(resource, "resource");

		this.resource = resource;

		this.parameters = parameters != null ? parameters : new PageParameters();

		authorize();
	}

	private void authorize()
	{
		IAuthorizationStrategy authorizationStrategy = null;
		if (Session.exists())
		{
			authorizationStrategy = Session.get().getAuthorizationStrategy();
		}
		else if (Application.exists())
		{
			authorizationStrategy = Application.get().getSecuritySettings().getAuthorizationStrategy();
		}

		if (authorizationStrategy != null && authorizationStrategy.isResourceAuthorized(resource, parameters) == false)
		{
			if (Application.exists())
			{
				Application.get().getSecuritySettings().getUnauthorizedResourceRequestListener().onUnauthorizedRequest(resource, parameters);
			}
			else
			{
				new DefaultUnauthorizedResourceRequestListener().onUnauthorizedRequest(resource, parameters);
			}
		}
	}

	/**
	 * @return page parameters
	 */
	public PageParameters getPageParameters()
	{
		return parameters;
	}

	/**
	 * @return resource
	 */
	public IResource getResource()
	{
		return resource;
	}

	/**
	 * @see org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void respond(final IRequestCycle requestCycle)
	{
		IResource.Attributes a = new IResource.Attributes(requestCycle.getRequest(),
			requestCycle.getResponse(), parameters);
		resource.respond(a);
	}

	/**
	 * @see org.apache.wicket.request.IRequestHandler#detach(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void detach(IRequestCycle requestCycle)
	{
	}
}
