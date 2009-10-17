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
package org.apache.wicket.session.pagemap;

import org.apache.wicket.IPageMap;

/**
 * A simple eviction strategy that evicts the least recently accessed page version from the given
 * page map.
 * 
 * @author Jonathan Locke
 */
public class LeastRecentlyAccessedEvictionStrategy implements IPageMapEvictionStrategy
{
	private static final long serialVersionUID = 1L;

	/** Maximum number of page versions in a page map before evictions start */
	private final int maxVersions;

	/**
	 * Constructor.
	 * 
	 * @param maxVersions
	 *            Maximum number of page versions before eviction occurs
	 */
	public LeastRecentlyAccessedEvictionStrategy(int maxVersions)
	{
		if (maxVersions < 1)
		{
			throw new IllegalArgumentException("Value for maxVersions must be >= 1");
		}
		this.maxVersions = maxVersions;
	}

	/**
	 * @see org.apache.wicket.session.pagemap.IPageMapEvictionStrategy#evict(org.apache.wicket.IPageMap)
	 */
	public void evict(final IPageMap pageMap)
	{
		// TODO WICKET-NG this method was replaced with a noop because ipagemap will soon be removed
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[LeastRecentlyAccessedEvictionStrategy maxVersions = " + maxVersions + "]";
	}
}
