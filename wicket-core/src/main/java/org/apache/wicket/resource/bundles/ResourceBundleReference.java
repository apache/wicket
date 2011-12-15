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
package org.apache.wicket.resource.bundles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.ResourceBundles;
import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.resource.header.HeaderItem;

/**
 * A resource reference that wraps another resource to make it into a bundle. The resources that are
 * provided by the wrapped reference, have to be added with
 * {@link #addProvidedResources(HeaderItem...)}. Normally, you will have to register this bundle in
 * {@link ResourceBundles} under {@link Application#getResourceBundles()}. Dependencies are
 * inherited from the provided resources if the bundle does not provide them.
 * 
 * @author papegaaij
 */
public class ResourceBundleReference extends ResourceReference implements IResourceBundle
{
	private static final long serialVersionUID = 1L;

	private ResourceReference bundleRef;

	private List<HeaderItem> providedResources = new ArrayList<HeaderItem>();

	/**
	 * Creates a new bundle reference from the given reference.
	 * 
	 * @param bundleRef
	 */
	public ResourceBundleReference(ResourceReference bundleRef)
	{
		super(bundleRef.getScope(), bundleRef.getName(), bundleRef.getLocale(),
			bundleRef.getStyle(), bundleRef.getVariation());
		this.bundleRef = bundleRef;
	}

	/**
	 * Adds the {@link HeaderItem}s that this bundle provides.
	 * 
	 * @param items
	 */
	public void addProvidedResources(HeaderItem... items)
	{
		providedResources.addAll(Arrays.asList(items));
	}

	@Override
	public IResource getResource()
	{
		return bundleRef.getResource();
	}

	@Override
	public Iterable<? extends HeaderItem> getProvidedResources()
	{
		return providedResources;
	}

	@Override
	public Iterable<? extends HeaderItem> getDependencies()
	{
		Set<HeaderItem> ret = new LinkedHashSet<HeaderItem>();
		for (HeaderItem curProvided : providedResources)
		{
			for (HeaderItem curDependency : curProvided.getDependencies())
				ret.add(curDependency);
		}
		for (HeaderItem curProvided : providedResources)
		{
			ret.remove(curProvided);
		}
		return ret;
	}
}
