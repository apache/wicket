/*
 * $Id$
 * $Revision$
 * $Date$
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
package wicket.util.value;

import java.util.Iterator;
import java.util.Map;

/**
 * ValueMap for attribtues.
 *
 * @author Eelco Hillenius
 */
public final class AttributeMap extends ValueMap
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty map.
	 */
	public AttributeMap()
	{
		super();
	}

	/**
	 * Copy constructor.
	 * @param map map to be copied
	 */
	public AttributeMap(Map map)
	{
		super(toMap(map));
	}

	/**
	 * Copies all values into a value map.
	 * @param map map to copy
	 * @return new map
	 */
	private static Map toMap(Map map)
	{
		AttributeMap newMap = new AttributeMap();
		if (map != null)
		{
			for (Iterator i = map.keySet().iterator(); i.hasNext();)
			{
				Object key = i.next();
				newMap.put(key, map.get(key));
			}
		}
		return newMap;
	}

	/**
	 * Puts all pairs of the given map in this map, converting all keys to lower case
	 * strings along the way.
	 * @param map the map to add to this map
	 * @see wicket.util.value.ValueMap#putAll(java.util.Map)
	 */
	public void putAll(Map map)
	{
		if (map != null)
		{
			for (Iterator i = map.keySet().iterator(); i.hasNext();)
			{
				Object key = i.next();
				put(key, map.get(key));
			}
		}
	}
}