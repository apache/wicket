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
package org.apache.wicket.request.resource.caching.version;

import java.util.Map;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;

/**
 * Caches the results of a delegating {@link IResourceVersion} instance
 * in the current request meta data. It will be cached for the lifetime
 * of the current http request.
 *
 * @author Peter Ertl
 *
 * @since 1.5
 */
public class RequestCycleCachedResourceVersion implements IResourceVersion
{
	private static final MetaDataKey<Map<ResourceReference, String>> CACHE_KEY =
		new MetaDataKey<Map<ResourceReference, String>>()
		{
			private static final long serialVersionUID = 1L;
		};

	/**
	 * resource version provider which will actually do 
	 * the work and retrieve the version
	 */
	private final IResourceVersion delegate;

	/**
	 * Constructor
	 * 
	 * @param delegate
	 *           resource version provider to cache
	 */
	public RequestCycleCachedResourceVersion(IResourceVersion delegate)
	{
		this.delegate = Args.notNull(delegate, "delegate");
	}

	public String getVersion(ResourceReference resourceReference)
	{
		// get current request cycle
		final RequestCycle requestCycle = ThreadContext.getRequestCycle();

		Map<ResourceReference, String> cache = null;

		// is request cycle available?
		if (requestCycle != null)
		{
			// retrieve cache from current request cycle
			cache = requestCycle.getMetaData(CACHE_KEY);

			// create it on first call
			if (cache == null)
			{
				requestCycle.setMetaData(CACHE_KEY, cache = Generics.newHashMap());
			}
			
			// lookup timestamp from cache (may contain NULL values which are valid)
			if (cache.containsKey(resourceReference))
			{
				return cache.get(resourceReference);
			}
		}
		
		// no cache entry found, query version from delegate
		final String version = delegate.getVersion(resourceReference);

		// store value in cache (if it is available)
		if (cache != null)
		{
			cache.put(resourceReference, version);
		}
		
		return version;
	}
}
