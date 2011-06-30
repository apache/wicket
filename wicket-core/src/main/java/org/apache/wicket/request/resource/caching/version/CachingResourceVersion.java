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

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.request.resource.PackageResourceReference;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.collections.MostRecentlyUsedMap;
import org.apache.wicket.util.lang.Args;

/**
 * Caches the results of a delegating {@link IResourceVersion} instance
 * 
 * @autor Peter Ertl
 * 
 * @since 1.5
 */
public class CachingResourceVersion implements IResourceVersion
{
	private static final int DEFAULT_MAX_CACHE_ENTRIES = 5000;
	private static final String NULL_VALUE = "null";

	private final IResourceVersion delegate;
	private final Map<CacheResourceVersionKey, String> cache;

	public CachingResourceVersion(IResourceVersion delegate)
	{
		this(delegate, DEFAULT_MAX_CACHE_ENTRIES);
	}

	/**
	 * constructor
	 * 
	 * @param delegate
	 *          resource version provider
	 * @param maxEntries
	 *          maximum number of cache entries
	 */        
	public CachingResourceVersion(IResourceVersion delegate, int maxEntries)
	{
		this.delegate = Args.notNull(delegate, "delegate");
		this.cache = Collections.synchronizedMap(
			new MostRecentlyUsedMap<CacheResourceVersionKey, String>(maxEntries));
	}

	public String getVersion(PackageResourceReference resourceReference)
	{
		PackageResourceReference.StreamInfo streamInfo = resourceReference.getCurrentStreamInfo();
		
		if(streamInfo == null)
		{
			return null;
		}

		final CacheResourceVersionKey key = new CacheResourceVersionKey(resourceReference, streamInfo);

		String version = cache.get(key);

		if (version == null)
		{
			version = delegate.getVersion(resourceReference);

			if (version == null)
			{
				version = NULL_VALUE;
			}
			cache.put(key, version);
		}

		//noinspection StringEquality
		if (version == NULL_VALUE)
		{
			return null;
		}
		return version;
	}
}
