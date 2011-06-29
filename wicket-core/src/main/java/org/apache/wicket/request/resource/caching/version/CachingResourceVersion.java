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

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.apache.wicket.request.resource.ResourceReference;
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
	private static final String NULL_VALUE = "null";

	private final IResourceVersion delegate;
	private final ConcurrentMap<ResourceReference, String> cache;

	public CachingResourceVersion(IResourceVersion delegate)
	{
		this.delegate = Args.notNull(delegate, "delegate");
		this.cache = new ConcurrentHashMap<ResourceReference, String>();
	}

	public String getVersion(ResourceReference resourceReference)
	{
		String version = cache.get(resourceReference);

		if (version == null)
		{
			version = delegate.getVersion(resourceReference);

			if (version == null)
			{
				version = NULL_VALUE;
			}
			cache.put(resourceReference, version);
		}

		//noinspection StringEquality
		if (version == NULL_VALUE)
		{
			return null;
		}
		return version;
	}
}
