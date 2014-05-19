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
package org.apache.wicket.request.resource.caching;

import org.apache.wicket.request.resource.AbstractResource;

/**
 * resource caching strategy that does nothing at all
 * <p/>
 * when using this strategy caching of resources will effectively be disabled
 * 
 * @author Peter Ertl
 * 
 * @since 1.5
 */
public class NoOpResourceCachingStrategy implements IResourceCachingStrategy
{
	/**
	 * Global instance of {@link NoOpResourceCachingStrategy} strategy
	 */
	public static final IResourceCachingStrategy INSTANCE = new NoOpResourceCachingStrategy();

	@Override
	public void decorateUrl(ResourceUrl url, IStaticCacheableResource resource)
	{
	}

	@Override
	public void undecorateUrl(ResourceUrl url)
	{
	}

	@Override
	public void decorateResponse(AbstractResource.ResourceResponse response, IStaticCacheableResource resource)
	{
	}

	@Override
	public void clearCache()
	{
	}

}
