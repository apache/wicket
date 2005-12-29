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
package wicket;

import java.util.Iterator;
import java.util.List;

/**
 * A simple eviction strategy that evicts the least recently accessed page
 * source from the given page map.
 * 
 * @author Jonathan Locke
 */
public class LeastRecentlyAccessedEvictionStrategy implements IPageMapEvictionStrategy
{
	private static final long serialVersionUID = 1L;
	
	/** Maximum number of pages in a page map before evictions start */
	private int maxPages;

	/**
	 * Constructor.
	 * 
	 * @param maxPages
	 *            Maximum number of pages before eviction occurs
	 */
	public LeastRecentlyAccessedEvictionStrategy(int maxPages)
	{
		if (maxPages < 1)
		{
			throw new IllegalArgumentException("Value for maxPages must be >= 1");
		}
		this.maxPages = maxPages;
	}

	/**
	 * @see wicket.IPageMapEvictionStrategy#evict(wicket.PageMap)
	 */
	public void evict(final PageMap pageMap)
	{
		if (pageMap.size() > maxPages)
		{
			final List list = pageMap.getPageSources();
			IPageSource leastRecentlyUsed = null;
			int min = Integer.MAX_VALUE;
			for (Iterator iterator = list.iterator(); iterator.hasNext();)
			{
				IPageSource pageSource = (IPageSource)iterator.next();
				int accessSequenceNumber = pageSource.getAccessSequenceNumber();
				if (accessSequenceNumber < min)
				{
					min = accessSequenceNumber;
					leastRecentlyUsed = pageSource;
				}
			}
			if (leastRecentlyUsed != null)
			{
				pageMap.remove(leastRecentlyUsed);
			}
		}
	}
}
