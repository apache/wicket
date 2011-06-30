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
 * in a member variable. The cache will be valid for the lifetime of 
 * this instance. It will expire the oldest entries if the maximum number 
 * of entries is exceeded.
 * 
 * @autor Peter Ertl
 * 
 * @since 1.5
 */
public class CachingResourceVersion implements IResourceVersion
{
	/**
	 * default maximum entries in cache
	 */
	private static final int DEFAULT_MAX_CACHE_ENTRIES = 5000;

	/**
	 * null value replacement holder for storing <code>null</code> in the map 
	 */
	private static final String NULL_VALUE = "null";

	/**
	 * delegating resource version provider
	 */
	private final IResourceVersion delegate;

	/**
	 * cache for resource versions
	 */
	private final Map<CacheResourceVersionKey, String> cache;

	/**
	 * create version cache
	 * <p/>
	 * the cache will accept up to {@value #DEFAULT_MAX_CACHE_ENTRIES} before 
	 * evicting the oldest entries.
	 * 
	 * @param delegate
	 *           delegating resource version provider
	 */
	public CachingResourceVersion(IResourceVersion delegate)
	{
		this(delegate, DEFAULT_MAX_CACHE_ENTRIES);
	}

	/**
	 * create version cache
	 * <p/>
	 * the cache will accept a maximum number of entries specified
	 * by <code>maxEntries</code> before evicting the oldest entries.
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
		// get current stream information for package resource
		PackageResourceReference.StreamInfo streamInfo = resourceReference.getCurrentStreamInfo();
		
		// if no stream info is available we can not provide a version
		if(streamInfo == null)
		{
			return null;
		}

		// cache key
		final CacheResourceVersionKey key = new CacheResourceVersionKey(resourceReference, streamInfo);

		// lookup version in cache
		String version = cache.get(key);

		// if not found
		if (version == null)
		{
			// get version from delegate
			version = delegate.getVersion(resourceReference);

			// replace null values with holder
			if (version == null)
			{
				version = NULL_VALUE;
			}
			// update cache
			cache.put(key, version);
		}

		//noinspection StringEquality
		if (version == NULL_VALUE)
		{
			// replace holder with null value
			return null;
		}
		
		// return version string
		return version;
	}
}
