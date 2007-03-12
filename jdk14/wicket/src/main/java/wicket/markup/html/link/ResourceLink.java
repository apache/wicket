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
package wicket.markup.html.link;

import wicket.IResourceListener;
import wicket.Resource;
import wicket.ResourceReference;
import wicket.util.value.ValueMap;

/**
 * A link to any ResourceReference.
 * 
 * @author Jonathan Locke
 */
public class ResourceLink extends Link implements IResourceListener
{
	private static final long serialVersionUID = 1L;

	/** The Resource reference */
	private final ResourceReference resourceReference;

	/** The Resource */
	private final Resource resource;

	/** The resource parameters */
	private final ValueMap resourceParameters;


	/**
	 * Constructs an ResourceLink from an resourcereference. That resource
	 * reference will bind its resource to the current SharedResources.
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
	 * Constructs an ResourceLink from an resourcereference. That resource
	 * reference will bind its resource to the current SharedResources.
	 * 
	 * @param id
	 *            See Component
	 * @param resourceReference
	 *            The shared resource to link to
	 * @param resourceParameters
	 *            The resource parameters
	 */
	public ResourceLink(final String id, final ResourceReference resourceReference,
			ValueMap resourceParameters)
	{
		super(id);
		this.resourceReference = resourceReference;
		this.resourceParameters = resourceParameters;
		this.resource = null;
	}

	/**
	 * Constructs a link directly to the provided resource.
	 * 
	 * @param id
	 *            See Component
	 * @param resource
	 *            The resource
	 */
	public ResourceLink(final String id, final Resource resource)
	{
		super(id);
		this.resource = resource;
		this.resourceReference = null;
		this.resourceParameters = null;
	}

	/**
	 * @see wicket.markup.html.link.Link#onClick()
	 */
	public void onClick()
	{
	}

	/**
	 * @see wicket.IResourceListener#onResourceRequested()
	 */
	public final void onResourceRequested()
	{
		resource.onResourceRequested();
		onClick();
	}

	/**
	 * @see wicket.markup.html.link.Link#getURL()
	 */
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

			resourceReference.bind(getApplication());
			return getRequestCycle().urlFor(resourceReference, resourceParameters);
		}
		return urlFor(IResourceListener.INTERFACE);
	}
}
