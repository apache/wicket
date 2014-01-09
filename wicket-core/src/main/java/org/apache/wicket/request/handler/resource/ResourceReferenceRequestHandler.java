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

import java.util.Locale;

import org.apache.wicket.request.ILoggableRequestHandler;
import org.apache.wicket.request.IRequestCycle;
import org.apache.wicket.request.IRequestHandler;
import org.apache.wicket.core.request.handler.logger.ResourceReferenceLogData;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;

/**
 * Request handler for {@link ResourceReference}. This handler is only used to generate URLs.
 * 
 * @author Matej Knopp
 */
public class ResourceReferenceRequestHandler implements IRequestHandler, ILoggableRequestHandler
{
	private final ResourceReference resourceReference;

	private final PageParameters pageParameters;

	private ResourceReferenceLogData logData;

	/**
	 * Construct.
	 * 
	 * @param resourceReference
	 */
	public ResourceReferenceRequestHandler(ResourceReference resourceReference)
	{
		this(resourceReference, null);
	}

	/**
	 * Construct.
	 * 
	 * @param resourceReference
	 * @param pageParameters
	 */
	public ResourceReferenceRequestHandler(ResourceReference resourceReference,
		PageParameters pageParameters)
	{
		Args.notNull(resourceReference, "resourceReference");

		this.resourceReference = resourceReference;
		this.pageParameters = pageParameters != null ? pageParameters : new PageParameters();
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

	/**
	 * @see org.apache.wicket.request.IRequestHandler#detach(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void detach(IRequestCycle requestCycle)
	{
		if (logData == null)
			logData = new ResourceReferenceLogData(this);
	}

	/** {@inheritDoc} */
	@Override
	public ResourceReferenceLogData getLogData()
	{
		return logData;
	}


	/**
	 * @see org.apache.wicket.request.IRequestHandler#respond(org.apache.wicket.request.IRequestCycle)
	 */
	@Override
	public void respond(IRequestCycle requestCycle)
	{
		new ResourceRequestHandler(getResourceReference().getResource(), getPageParameters()).respond(requestCycle);
	}

	/**
	 * @return reference locale
	 */
	public Locale getLocale()
	{
		return getResourceReference().getLocale();
	}

	/**
	 * @return resource
	 */
	public IResource getResource()
	{
		return getResourceReference().getResource();
	}

	/**
	 * @return style
	 */
	public String getStyle()
	{
		return getResourceReference().getStyle();
	}

	/**
	 * @return variation
	 */
	public String getVariation()
	{
		return getResourceReference().getVariation();
	}

	@Override
	public String toString()
	{
		return "ResourceReferenceRequestHandler{" +
				"resourceReference=" + resourceReference +
				", pageParameters=" + pageParameters +
				'}';
	}
}
