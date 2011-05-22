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

import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.MetaDataKey;
import org.apache.wicket.ThreadContext;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.time.Time;

/**
 * base class for resource caching strategies
 * <p/>
 * <ul>
 * <li>provides cached access to the last-modified timestamp</li>
 * </ul>
 * 
 * @author Peter Ertl
 */
public abstract class AbstractResourceCachingStrategy implements IResourceCachingStrategy
{
	/** timestamp cache stored in request cycle meta data (request-scoped) */
	protected static final MetaDataKey<Map<ResourceReference, Time>> TIMESTAMP_KEY = new MetaDataKey<Map<ResourceReference, Time>>()
	{
		private static final long serialVersionUID = 1L;
	};

	private boolean lastModifiedCacheEnabled = true;

	/**
	 * returns if caching if lastModified timestamp lookup is enabled
	 * 
	 * @return <code>true</code> if enabled
	 */
	public boolean isLastModifiedCacheEnabled()
	{
		return lastModifiedCacheEnabled;
	}

	/**
	 * controls request-scoped caching of lookups to last modified time of the resource reference
	 * 
	 * @param enabled
	 */
	public void setLastModifiedCacheEnabled(boolean enabled)
	{
		lastModifiedCacheEnabled = enabled;
	}

	/**
	 * That method gets the last modification timestamp for the specified resource reference.
	 * <p>
	 * The timestamp is cached in the meta data of the application to eliminate repeated lookups of
	 * the same resource reference which will harm performance.<br/>
	 * In Development mode the cache is preserved in the current {@link RequestCycle}'s meta data so
	 * the benefit is only if the same resource is used more than once in the same page. But after
	 * eventual modification of the resource the new modification time will be used for the next
	 * requests.
	 * 
	 * @param resourceReference
	 *            resource reference
	 * 
	 * @return last modification timestamp or <code>null</code> if no timestamp provided
	 */
	protected Time getLastModified(ResourceReference resourceReference)
	{
		Application application = ThreadContext.getApplication();
		if (application == null)
		{
			// no application: test case without WicketTester
			return resourceReference.getLastModified();
		}

		Map<ResourceReference, Time> cache;
		if (application.usesDevelopmentConfig())
		{
			// try to lookup current request cycle
			RequestCycle requestCycle = ThreadContext.getRequestCycle();

			// retrieve cache from current request cycle
			cache = requestCycle.getMetaData(TIMESTAMP_KEY);

			// create it on first call
			if (cache == null)
			{
				cache = Generics.newHashMap();
				requestCycle.setMetaData(TIMESTAMP_KEY, cache);
			}
		}
		else
		{
			// in deployment mode put the cache in the application's meta data
			cache = application.getMetaData(TIMESTAMP_KEY);

			// create it on first call
			if (cache == null)
			{
				// use concurrent map for better performance. two puts are not really a problem
				cache = Generics.newConcurrentHashMap();
				application.setMetaData(TIMESTAMP_KEY, cache);
			}
		}

		// lookup timestamp from cache (may contain NULL values which are valid)
		if (cache.containsKey(resourceReference))
		{
			return cache.get(resourceReference);
		}

		// no cache entry, so retrieve timestamp from resource reference
		Time lastModified = resourceReference.getLastModified();

		// and put it in cache
		cache.put(resourceReference, lastModified);

		return lastModified;
	}
}
