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

import java.io.Serializable;
import java.util.Map;

import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.caching.IStaticCacheableResource;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.lang.Generics;

/**
 * Caches the results of a delegating {@link IResourceVersion} instance
 * for the lifetime of the current http request.
 *
 * @author Peter Ertl
 *
 * @since 1.5
 */
public class RequestCycleCachedResourceVersion implements IResourceVersion
{
	private static final MetaDataKey<Map<Serializable, String>> CACHE_KEY =
		new MetaDataKey<Map<Serializable, String>>()
		{
			private static final long serialVersionUID = 1L;
		};

	/**
	 * resource version provider which will actually do 
	 * the hard work and retrieve the version
	 */
	private final IResourceVersion delegate;

	/**
	 * create request-scoped resource provider cache
	 * 
	 * @param delegate
	 *           resource version provider to cache
	 */
	public RequestCycleCachedResourceVersion(IResourceVersion delegate)
	{
		this.delegate = Args.notNull(delegate, "delegate");
	}

	@Override
	public String getVersion(IStaticCacheableResource resource)
	{
		// get current request cycle
		final RequestCycle requestCycle = ThreadContext.getRequestCycle();

		// cache instance
		Map<Serializable, String> cache = null;

		// cache key
		Serializable key = null;

		// is request cycle available?
		if (requestCycle != null)
		{
			// retrieve cache from current request cycle
			cache = requestCycle.getMetaData(CACHE_KEY);

			// create caching key
			key = resource.getCacheKey();

			// does cache exist within current request cycle?
			if (cache == null)
			{
				// no, so create it
				requestCycle.setMetaData(CACHE_KEY, cache = Generics.newHashMap());
			}
			else if (cache.containsKey(key))
			{
				// lookup timestamp from cache (may contain NULL values which are valid)
				return cache.get(key);
			}
		}
		
		// no cache entry found, query version from delegate
		final String version = delegate.getVersion(resource);

		// store value in cache (if it is available)
		if (cache != null && key != null)
		{
			cache.put(key, version);
		}
		
		return version;
	}
}
