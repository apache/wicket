/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.collections;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Holds a map of most recently used items
 * @author Jonathan Locke
 */
public class MostRecentlyUsedMap extends LinkedHashMap
{ // TODO finalize javadoc
	/** serialVersionUID */
	private static final long serialVersionUID = 895458107686513000L;

	/**
     * Gets instance of LRU map with given maximum limit on entries
     * @param maxEntries Maximum number of entries allowed in the map
     * @return MRU map instance
     */
    public static MostRecentlyUsedMap newInstance(final int maxEntries)
    {
        if (maxEntries <= 0)
        {
            throw new IllegalArgumentException("Must have at least one entry");
        }

        return new MostRecentlyUsedMap()
        {
        	/** serialVersionUID */
			private static final long serialVersionUID = -3137250910658864184L;

			protected boolean removeEldestEntry(final Map.Entry eldest)
            {
                return size() > maxEntries;
            }
        };
    }
}

///////////////////////////////// End of File /////////////////////////////////
