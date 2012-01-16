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

import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.util.lang.Args;

/**
 * Request handler that renders a resource.
 * 
 * @author Matej Knopp
 */
public class ResourceRequestHandler implements IRequestHandler
{
	private final IResource resource;
	private final PageParameters pageParameters;

	/**
	 * Construct.
	 * 
	 * @param resource
	 * @param pageParameters
	 */
	public ResourceRequestHandler(IResource resource, PageParameters pageParameters)
	{
		Args.notNull(resource, "resource");

		this.resource = resource;

		this.pageParameters = pageParameters != null ? pageParameters : new PageParameters();
	}


	/**
	 * @return page parameters
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
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
	public void respond(final IRequestCycle requestCycle)
	{
		IResource.Attributes a = new IResource.Attributes(requestCycle.getRequest(),
			requestCycle.getResponse(), pageParameters);
		resource.respond(a);
	}

	/**
	 * @see org.apache.wicket.request.IRequestHandler#detach(org.apache.wicket.request.IRequestCycle)
	 */
	public void detach(IRequestCycle requestCycle)
	{
	}
}
