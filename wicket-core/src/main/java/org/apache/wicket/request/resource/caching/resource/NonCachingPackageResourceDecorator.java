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
package org.apache.wicket.request.resource.caching.resource;

import java.io.Serializable;

import org.apache.wicket.request.resource.IPackageResource;
import org.apache.wicket.request.resource.PackageResourceDecorator;

/**
 * wraps an existing {@link IPackageResource} and disables caching via 
 * {@link org.apache.wicket.request.resource.caching.IResourceCachingStrategy}
 * by returning {@code null} for the cache key in method 
 * {@link org.apache.wicket.request.resource.caching.IStaticCacheableResource#getCacheKey()}
 *
 * @autor Peter Ertl
 */
public class NonCachingPackageResourceDecorator extends PackageResourceDecorator
{
	public NonCachingPackageResourceDecorator(final IPackageResource resource)
	{
		super(resource);
	}

	@Override
	public Serializable getCacheKey()
	{
		// disable caching by returning null
		return null;
	}
}
