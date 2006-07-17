/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) eelco12 $
 * $Revision: 5004 $
 * $Date: 2006-03-17 20:47:08 -0800 (Fri, 17 Mar 2006) $
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
import java.util.Map;
import java.util.Set;

import wicket.util.string.StringValue;
import wicket.util.string.StringValueConversionException;
import wicket.util.time.Duration;
import wicket.util.time.Time;

/**
 * A implementation that takes a IValueMap that could be immutable but makes a
 * copy when a call is made that wanted to change the map.
 * 
 * @author jcompagner
 */
public class CopyOnWriteValueMap implements IValueMap, Serializable
{
	private static final long serialVersionUID = 1L;

	private IValueMap wrapped;
	
	/**
	 * Construct.
	 * @param wrapped
	 */
	public CopyOnWriteValueMap(IValueMap wrapped)
	{
		this.wrapped = wrapped;
	}

	public void clear()
	{
		checkAndCopy();
		wrapped.clear();
	}

	private void checkAndCopy()
	{
		if(wrapped.isImmutable())
		{
			wrapped = new ValueMap(wrapped);
		}
	}

	public boolean containsKey(Object key)
	{
		return wrapped.containsKey(key);
	}

	public boolean containsValue(Object value)
	{
		return wrapped.containsValue(value);
	}

	public Set<Entry<String, Object>> entrySet()
	{
		checkAndCopy();
		return wrapped.entrySet();
	}

	@Override
	public boolean equals(Object o)
	{
		return wrapped.equals(o);
	}

	public Object get(Object key)
	{
		return wrapped.get(key);
	}

	public boolean getBoolean(String key) throws StringValueConversionException
	{
		return wrapped.getBoolean(key);
	}

	public CharSequence getCharSequence(String key)
	{
		return wrapped.getCharSequence(key);
	}

	public double getDouble(String key) throws StringValueConversionException
	{
		return wrapped.getDouble(key);
	}

	public Duration getDuration(String key) throws StringValueConversionException
	{
		return wrapped.getDuration(key);
	}

	public int getInt(String key, int defaultValue) throws StringValueConversionException
	{
		return wrapped.getInt(key, defaultValue);
	}

	public int getInt(String key) throws StringValueConversionException
	{
		return wrapped.getInt(key);
	}

	public String getKey(String key)
	{
		return wrapped.getKey(key);
	}

	public long getLong(String key, long defaultValue) throws StringValueConversionException
	{
		return wrapped.getLong(key, defaultValue);
	}

	public long getLong(String key) throws StringValueConversionException
	{
		return wrapped.getLong(key);
	}

	public String getString(String key, String defaultValue)
	{
		return wrapped.getString(key, defaultValue);
	}

	public String getString(String key)
	{
		return wrapped.getString(key);
	}

	public String[] getStringArray(String key)
	{
		return wrapped.getStringArray(key);
	}

	public StringValue getStringValue(String key)
	{
		return wrapped.getStringValue(key);
	}

	public Time getTime(String key) throws StringValueConversionException
	{
		return wrapped.getTime(key);
	}

	public boolean isEmpty()
	{
		return wrapped.isEmpty();
	}

	public boolean isImmutable()
	{
		return false;
	}

	public Set<String> keySet()
	{
		checkAndCopy();
		return wrapped.keySet();
	}

	public IValueMap makeImmutable()
	{
		return wrapped.makeImmutable();
	}

	public Object put(String key, Object value)
	{
		checkAndCopy();		
		return wrapped.put(key, value);
	}

	public void putAll(Map<? extends String, ? extends Object> map)
	{
		checkAndCopy();
		wrapped.putAll(map);
	}

	public Object remove(Object key)
	{
		checkAndCopy();
		return wrapped.remove(key);
	}

	public int size()
	{
		return wrapped.size();
	}


	public Collection<Object> values()
	{
		return wrapped.values();
	}
}