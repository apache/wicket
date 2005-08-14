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

import wicket.util.string.StringValue;

/**
 * ValueMap that holds strings and turns all keys into lower case.
 *
 * @author Eelco Hillenius
 */
public final class LowerCaseKeyValueMap extends ValueMap
{
	/**
	 * Constructs an empty map.
	 */
	public LowerCaseKeyValueMap()
	{
		super();
	}

	/**
	 * Copy constructor.
	 * @param map map to be copied
	 */
	public LowerCaseKeyValueMap(Map map)
	{
		super(toLowerCaseMap(map));
	}

	/**
	 * Copies all values into a lower case string value map.
	 * @param map map to copy
	 * @return new map
	 */
	private static Map toLowerCaseMap(Map map)
	{
		LowerCaseKeyValueMap newMap = new LowerCaseKeyValueMap();
		if (map != null)
		{
			for(Iterator i = map.keySet().iterator(); i.hasNext();)
			{
				Object key = i.next();
				newMap.put(key, map.get(key));
			}
		}
		return newMap;
	}

	/**
	 * Puts the value in this map with the key.
	 * @param key the key; will be converted to lower case
	 * @param value the value to store
	 * @return any old value that was stored with the given lower cased key
	 * @see wicket.util.value.ValueMap#put(java.lang.Object, java.lang.Object)
	 */
	public Object put(Object key, Object value)
	{
		return super.put(key.toString().toLowerCase(), value);
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
			for(Iterator i = map.keySet().iterator(); i.hasNext();)
			{
				Object key = i.next();
				put(key, map.get(key));
			}
		}
	}

	/**
	 * Removes the value for this key.
	 * @param key the key; will be converted to lower case
	 * @see wicket.util.value.ValueMap#remove(java.lang.Object)
	 */
	public Object remove(Object key)
	{
		return super.remove(key.toString().toLowerCase());
	}

	/**
	 * Returns whether this map contains the given key converted to a lower case string.
	 * @param key the key; will be converted to lower case
	 * @see wicket.util.value.ValueMap#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key)
	{
		return super.containsKey(key.toString().toLowerCase());
	}

	/**
	 * Gets the object by key.
	 * @param key the key; will be converted to lower case
	 * @return the object stored with the given key
	 * @see wicket.util.value.ValueMap#get(java.lang.Object)
	 */
	public Object get(Object key)
	{
		return super.get(key.toString().toLowerCase());
	}

	/**
	 * Gets a StringValue by key.
	 * @param key the key; will be converted to lower case
	 * @see wicket.util.value.ValueMap#getStringValue(java.lang.String)
	 */
	public StringValue getStringValue(String key)
	{
		return super.getStringValue(key.toString().toLowerCase());
	}
}