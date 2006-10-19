/*
 * $Id: AttributeMap.java 5844 2006-05-24 20:53:56 +0000 (Wed, 24 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-25 22:46:21 +0000 (Thu, 25 May
 * 2006) $
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

import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

import wicket.WicketRuntimeException;
import wicket.util.string.StringValue;
import wicket.util.string.StringValueConversionException;
import wicket.util.time.Duration;
import wicket.util.time.Time;

/**
 * A ValueMap which keeps remembers the original value in addition to the new
 * value in case of changes (add and modify). Deleting entries is not supported.
 * <p>
 * To allow modifications to a Components markup tag in a components constructor
 * (as opposed to using SimpleAttributeModifier or subclassing
 * onComponentTag()), the markup attributes are made available to the user.
 * However, as Markup is cached per Component class and not per instance, the
 * markup attributes are unmodifiable while Wicket components are created and
 * only modifiable during the render process (Component.onComponentTag()).
 * <p>
 * To allow for changes not only during the render process a modifiably copy of
 * the attributes is maintained by each Component instance and used during the
 * render process.
 * <p>
 * Due to changes to the Component's locale or style, it might be necessary to
 * reload the markup file. Hence, we can not simply make a copy of the
 * attributes, but rather maintain the user's modifications. When a markup file
 * gets reloaded only the underlying base map is replaced with "new" attributes
 * from the markup and the user's changes remain unchanged and now supersede the
 * new attributes.
 * 
 * @author Juergen Donnerstag
 */
public final class MarkupAttributeValueMap implements IValueMap, Serializable
{
	private static final long serialVersionUID = 1L;

	/** The map to track the changes on */
	private IValueMap baseMap;

	/** The changed entries of the Map */
	private final IValueMap changes = new ValueMap(2);

	/**
	 * Constructs an empty map.
	 */
	public MarkupAttributeValueMap()
	{
		setBaseMap(null);
	}

	/**
	 * Create a modifiable copy of 'baseMap'
	 * 
	 * @param baseMap
	 *            The map to track the changes on.
	 */
	public MarkupAttributeValueMap(final IValueMap baseMap)
	{
		setBaseMap(baseMap);
	}

	/**
	 * 
	 * @return An unmodifiable map of the changes (add and modify only; no
	 *         deletes)
	 */
	public Map<String, Object> getChangeMap()
	{
		return Collections.unmodifiableMap(this.changes);
	}

	/**
	 * Apply a new 'baseMap'. The changes recorded remain unchanged and are
	 * applied to the new base map.
	 * 
	 * @param map
	 *            The new base map.
	 */
	public void setBaseMap(final IValueMap map)
	{
		if (map == null)
		{
			this.baseMap = new ValueMap(2);
		}
		else
		{
			this.baseMap = new ValueMap(map);
		}
		this.baseMap.putAll(this.changes);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#clear()
	 */
	public void clear()
	{
		throw new WicketRuntimeException("clear() is not supported");
	}

	/**
	 * 
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key)
	{
		return baseMap.containsKey(key);
	}

	/**
	 * 
	 * @see java.util.Map#containsValue(java.lang.Object)
	 */
	public boolean containsValue(Object value)
	{
		return baseMap.containsValue(value);
	}

	/**
	 * 
	 * @see java.util.Map#entrySet()
	 */
	public Set<Entry<String, Object>> entrySet()
	{
		return baseMap.entrySet();
	}

	/**
	 * 
	 * @see java.lang.Object#equals(java.lang.Object)
	 */
	public boolean equals(Object o)
	{
		return baseMap.equals(o);
	}

	/**
	 * 
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public Object get(Object key)
	{
		return baseMap.get(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getBoolean(java.lang.String)
	 */
	public boolean getBoolean(String key) throws StringValueConversionException
	{
		return baseMap.getBoolean(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getCharSequence(java.lang.String)
	 */
	public CharSequence getCharSequence(String key)
	{
		return baseMap.getCharSequence(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getDouble(java.lang.String, double)
	 */
	public double getDouble(String key, double defaultValue) throws StringValueConversionException
	{
		return baseMap.getDouble(key, defaultValue);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getDouble(java.lang.String)
	 */
	public double getDouble(String key) throws StringValueConversionException
	{
		return baseMap.getDouble(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getDuration(java.lang.String)
	 */
	public Duration getDuration(String key) throws StringValueConversionException
	{
		return baseMap.getDuration(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getInt(java.lang.String, int)
	 */
	public int getInt(String key, int defaultValue) throws StringValueConversionException
	{
		return baseMap.getInt(key, defaultValue);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getInt(java.lang.String)
	 */
	public int getInt(String key) throws StringValueConversionException
	{
		return baseMap.getInt(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getKey(java.lang.String)
	 */
	public String getKey(String key)
	{
		return baseMap.getKey(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getLong(java.lang.String, long)
	 */
	public long getLong(String key, long defaultValue) throws StringValueConversionException
	{
		return baseMap.getLong(key, defaultValue);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getLong(java.lang.String)
	 */
	public long getLong(String key) throws StringValueConversionException
	{
		return baseMap.getLong(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getString(java.lang.String,
	 *      java.lang.String)
	 */
	public String getString(String key, String defaultValue)
	{
		return baseMap.getString(key, defaultValue);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getString(java.lang.String)
	 */
	public String getString(String key)
	{
		return baseMap.getString(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getStringArray(java.lang.String)
	 */
	public String[] getStringArray(String key)
	{
		return baseMap.getStringArray(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getStringValue(java.lang.String)
	 */
	public StringValue getStringValue(String key)
	{
		return baseMap.getStringValue(key);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#getTime(java.lang.String)
	 */
	public Time getTime(String key) throws StringValueConversionException
	{
		return baseMap.getTime(key);
	}

	public int hashCode()
	{
		return baseMap.hashCode();
	}

	/**
	 * 
	 * @see java.util.Map#isEmpty()
	 */
	public boolean isEmpty()
	{
		return baseMap.isEmpty();
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#isImmutable()
	 */
	public boolean isImmutable()
	{
		return baseMap.isImmutable();
	}

	/**
	 * 
	 * @see java.util.Map#keySet()
	 */
	public Set<String> keySet()
	{
		return baseMap.keySet();
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#makeImmutable()
	 */
	public IValueMap makeImmutable()
	{
		return baseMap.makeImmutable();
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#put(java.lang.String, java.lang.Object)
	 */
	public Object put(String key, Object value)
	{
		this.changes.put(key, value);
		return baseMap.put(key, value);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#putAll(java.util.Map)
	 */
	public void putAll(Map<? extends String, ? extends Object> map)
	{
		// We assume putAll() calls put() for each entry
		baseMap.putAll(map);
	}

	/**
	 * 
	 * @see wicket.util.value.IValueMap#remove(java.lang.Object)
	 */
	public Object remove(Object key)
	{
		throw new WicketRuntimeException("remove(key) is not supported by");
	}

	/**
	 * 
	 * @see java.util.Map#size()
	 */
	public int size()
	{
		return baseMap.size();
	}

	/**
	 * 
	 * @see java.util.Map#values()
	 */
	public Collection<Object> values()
	{
		return baseMap.values();
	}
}
