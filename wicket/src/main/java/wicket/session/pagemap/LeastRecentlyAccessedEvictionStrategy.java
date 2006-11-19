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
package wicket.session.pagemap;

import wicket.AccessStackPageMap;
import wicket.Page;
import wicket.PageMap;
import wicket.Session;
import wicket.AccessStackPageMap.Access;

/**
 * A simple eviction strategy that evicts the least recently accessed page
 * version from the given page map.
 * 
 * @author Jonathan Locke
 */
public class LeastRecentlyAccessedEvictionStrategy implements IPageMapEvictionStrategy
{
	private static final long serialVersionUID = 1L;

	/** Maximum number of page versions in a page map before evictions start */
	private int maxVersions;

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
	 * @see wicket.session.pagemap.IPageMapEvictionStrategy#evict(wicket.PageMap)
	 */
	public void evict(final PageMap pageMap)
	{
		if (pageMap instanceof AccessStackPageMap)
		{
			synchronized (Session.get())
			{
				AccessStackPageMap accessPM = (AccessStackPageMap)pageMap;
				// Do we need to evict under this strategy?
				if (accessPM.getVersions() > maxVersions)
				{
					// Remove oldest entry from access stack
					final AccessStackPageMap.Access oldestAccess = (Access)accessPM.getAccessStack()
							.remove(0);
					final IPageMapEntry oldestEntry = pageMap.getEntry(oldestAccess.getId());

					// If entry is a page (cannot be null if we're evicting)
					if (oldestEntry instanceof Page)
					{
						Page page = (Page)oldestEntry;

						// If there is more than one version of this page
						if (page.getVersions() > 1)
						{
							// expire the oldest version
							page.expireOldestVersion();
						}
						else
						{
							// expire whole page
							accessPM.removeEntry(page);
						}
					}
					else
					{
						// If oldestEntry is not an instance of Page, then it is
						// some
						// custom, user-defined IPageMapEntry class and cannot
						// contain
						// versioning information, so we just remove the entry.
						if (oldestEntry != null)
						{
							accessPM.removeEntry(oldestEntry);
						}
					}
				}
			}
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[LeastRecentlyAccessedEvictionStrategy maxVersions = " + maxVersions + "]";
	}
}
