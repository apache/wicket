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
package org.apache._wicket.request.handler.resource;

import org.apache._wicket.PageParameters;
import org.apache._wicket.RequestCycle;
import org.apache._wicket.request.RequestHandler;
import org.apache._wicket.resource.ResourceReference;

/**
 * Request handler for {@link ResourceReference}. This handler is only used to generate URLs.
 * 
 * @author Matej Knopp
 */
public class ResourceReferenceRequestHandler implements RequestHandler
{
	private final ResourceReference resourceReference;
	private final PageParameters pageParameters;
	
	/**
	 * Construct.
	 * 
	 * @param resourceReference
	 * @param pageParameters
	 */
	public ResourceReferenceRequestHandler(ResourceReference resourceReference, PageParameters pageParameters)
	{
		if (resourceReference == null)
		{
			throw new IllegalArgumentException("Argument 'resourceReference' may not be null.");
		}		
		this.resourceReference = resourceReference;
		this.pageParameters = pageParameters;
	}
	
	/**
	 * @return resource reference
	 */
	public ResourceReference getResourceReference()
	{
		return resourceReference;
	}
	
	/**
	 * @return page parameters
	 */
	public PageParameters getPageParameters()
	{
		return pageParameters;
	}

	public void detach(RequestCycle requestCycle)
	{
	}

	public void respond(RequestCycle requestCycle)
	{
	}

}
