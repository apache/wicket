/*
 * $Id: LeastRecentlyAccessedEvictionStrategy.java,v 1.1 2005/12/29 05:30:24
 * jonathanlocke Exp $ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.session.pagemap;

import java.util.Iterator;
import java.util.List;

import wicket.Page;
import wicket.PageMap;

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
		if (pageMap.getVersions() > maxVersions)
		{
			final List list = pageMap.getEntries();
			IPageMapEntry leastRecentlyUsed = null;
			int min = Integer.MAX_VALUE;
			for (Iterator iterator = list.iterator(); iterator.hasNext();)
			{
				IPageMapEntry entry = (IPageMapEntry)iterator.next();
				int accessSequenceNumber = entry.getAccessSequenceNumber();
				if (accessSequenceNumber < min)
				{
					min = accessSequenceNumber;
					leastRecentlyUsed = entry;
				}
			}
			if (leastRecentlyUsed != null)
			{
				// If entry is a page
				if (leastRecentlyUsed instanceof Page)
				{
					Page page = (Page)leastRecentlyUsed;
					
					// If there is more than one version of this page
					if (page.getVersions() > 1)
					{
						// expire the oldest version
						page.expireOldestVersion();
					}
					else
					{
						// expire whole page
						pageMap.remove(page);						
					}
				}
				else
				{
					// Remove the entry
					pageMap.remove(leastRecentlyUsed);
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
