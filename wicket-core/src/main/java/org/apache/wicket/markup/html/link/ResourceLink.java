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
package org.apache.wicket.markup.html.link;

import org.apache.wicket.IRequestListener;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.handler.resource.ResourceReferenceRequestHandler;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.IResource.Attributes;
import org.apache.wicket.request.resource.ResourceReference;

/**
 * A link to any ResourceReference.
 * 
 * @author Jonathan Locke
 * @param <T>
 *            type of model object
 */
public class ResourceLink<T> extends Link<T> implements IRequestListener
{
	private static final long serialVersionUID = 1L;

	/** The Resource reference */
	private final ResourceReference resourceReference;

	/** The Resource */
	private final IResource resource;

	/** The resource parameters */
	private final PageParameters resourceParameters;


	/**
	 * Constructs an ResourceLink from an resourcereference. That resource reference will bind its
	 * resource to the current SharedResources.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared resource to link to
	 */
	public ResourceLink(final String id, final ResourceReference resourceReference)
	{
		this(id, resourceReference, null);
	}

	/**
	 * Constructs an ResourceLink from an resourcereference. That resource reference will bind its
	 * resource to the current SharedResources.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared resource to link to
	 * @param resourceParameters
	 *            The resource parameters
	 */
	public ResourceLink(final String id, final ResourceReference resourceReference,
		PageParameters resourceParameters)
	{
		super(id);
		this.resourceReference = resourceReference;
		this.resourceParameters = resourceParameters;
		resource = null;
	}

	/**
	 * Constructs a link directly to the provided resource.
	 * 
	 * @param id
	 *            See Component
	 * @param resource
	 *            The resource
	 */
	public ResourceLink(final String id, final IResource resource)
	{
		super(id);
		this.resource = resource;
		resourceReference = null;
		resourceParameters = null;
	}

	/**
	 * @see org.apache.wicket.markup.html.link.Link#onClick()
	 */
	@Override
	public void onClick()
	{
	}

	@Override
	public boolean includeRenderCount()
	{
		return false;
	}
	
	/**
	 * @see org.apache.wicket.IResourceListener#onResourceRequested()
	 */
	@Override
	public final void onRequest()
	{

		Attributes a = new Attributes(RequestCycle.get().getRequest(), RequestCycle.get()
			.getResponse(), null);
		resource.respond(a);
		
		super.onRequest();
	}

	/**
	 * @see org.apache.wicket.markup.html.link.Link#getURL()
	 */
	@Override
	protected final CharSequence getURL()
	{
		if (resourceReference != null)
		{
			// TODO post 1.2: should we have support for locale changes when the
			// resource reference (or resource??) is set manually..
			// We should get a new resource reference for the current locale
			// then
			// that points to the same resource but with another locale if it
			// exists.
			// something like
			// SharedResource.getResourceReferenceForLocale(resourceReference);
			if (resourceReference.canBeRegistered())
			{
				getApplication().getResourceReferenceRegistry().registerResourceReference(
					resourceReference);
			}

			return getRequestCycle().urlFor(
				new ResourceReferenceRequestHandler(resourceReference, resourceParameters));
		}
		return urlFor(resourceParameters);
	}
}
