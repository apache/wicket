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
package wicket.util.value;


import java.io.Serializable;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import wicket.util.parse.metapattern.parsers.VariableAssignmentParser;
import wicket.util.string.IStringIterator;
import wicket.util.string.StringList;
import wicket.util.string.StringValue;
import wicket.util.string.StringValueConversionException;
import wicket.util.time.Duration;
import wicket.util.time.Time;

/**
 * A map of values with convenient getters.
 * @author Jonathan Locke
 */
public class ValueMap implements Map, Serializable
{ // TODO finalize javadoc
	/** serialVersionUID. */
	private static final long serialVersionUID = -693116621545826988L;

	/** empty map. */
	public static final ValueMap EMPTY_MAP = new ValueMap();

    /** The underlying map. */
    private Map map;

    /** True if this value map has been made immutable. */
    private boolean immutable = false;

    /**
     * Constructs empty value map.
     */
    public ValueMap()
    {
        this.map = new HashMap();
    }

    /**
     * Constructor.
     * @param map The map to copy
     */
    public ValueMap(final Map map)
    {
        this.map = new HashMap(map);
    }

    /**
     * Constructor.
     * @param keyValuePairs List of key value pairs separated by commas. For example,
     *            "param1=foo,param2=bar"
     * @param delimiter Delimiter string used to separate key/value pairs
     */
    public ValueMap(final String keyValuePairs, final String delimiter)
    {
        this.map = new HashMap();

        final StringList pairs = StringList.tokenize(keyValuePairs, delimiter);

        for (IStringIterator iterator = pairs.iterator(); iterator.hasNext();)
        {
            final String pair = iterator.next();
            final VariableAssignmentParser parser = new VariableAssignmentParser(pair);

            if (parser.matches())
            {
                put(parser.getKey(), parser.getValue());
            }
            else
            {
                throw new IllegalArgumentException("Invalid key value list: '"
                        + keyValuePairs + "'");
            }
        }
    }

    /**
     * Constructor.
     * @param keyValuePairs List of key value pairs separated by commas. For example,
     *            "param1=foo,param2=bar"
     */
    public ValueMap(final String keyValuePairs)
    {
        this(keyValuePairs, ",");
    }
    
    /**
     * Makes this value map immutable by changing the underlying map representation to a
     * collections "unmodifiableMap". After calling this method, any attempt to modify
     * this map will result in a runtime exception being thrown by the collections
     * classes.
     */
    public final void makeImmutable()
    {
        if (!immutable)
        {
            map = Collections.unmodifiableMap(map);
            immutable = true;
        }
    }

    /**
     * Sets a string.
     * @param key The key
     * @return The string value object
     */
    public final StringValue getStringValue(final String key)
    {
        return StringValue.valueOf(get(key));
    }

    /**
     * Gets a string.
     * @param key The get
     * @return The string
     */
    public final String getString(final String key)
    {
        final Object o = getStringValue(key);

        if (o == null)
        {
            return null;
        }
        else
        {
            return o.toString();
        }
    }

    /**
     * Gets an int.
     * @param key The key
     * @return The value
     * @throws StringValueConversionException
     */
    public final int getInt(final String key) throws StringValueConversionException
    {
        return getStringValue(key).toInt();
    }

    /**
     * Gets a long.
     * @param key The key
     * @return The value
     * @throws StringValueConversionException
     */
    public final long getLong(final String key) throws StringValueConversionException
    {
        return getStringValue(key).toLong();
    }

    /**
     * Gets a long using a default if not found.
     * @param key The key
     * @param defaultValue Value to use if no value in map
     * @return The value
     * @throws StringValueConversionException
     */
    public final long getLong(final String key, final long defaultValue)
            throws StringValueConversionException
    {
        final StringValue value = getStringValue(key);

        return (value != null) ? value.toLong() : defaultValue;
    }

    /**
     * Gets a double.
     * @param key The key
     * @return The value
     * @throws StringValueConversionException
     */
    public final double getDouble(final String key) throws StringValueConversionException
    {
        return getStringValue(key).toLong();
    }

    /**
     * Gets a time.
     * @param key The key
     * @return The value
     * @throws StringValueConversionException
     */
    public final Time getTime(final String key) throws StringValueConversionException
    {
        return getStringValue(key).toTime();
    }

    /**
     * Gets a duration.
     * @param key The key
     * @return The value
     * @throws StringValueConversionException
     */
    public final Duration getDuration(final String key) throws StringValueConversionException
    {
        return getStringValue(key).toDuration();
    }

    /**
     * @see java.util.Map#size()
     */
    public final int size()
    {
        return map.size();
    }

    /**
     * @see java.util.Map#clear()
     */
    public final void clear()
    {
        map.clear();
    }

    /**
     * @see java.util.Map#values()
     */
    public final Collection values()
    {
        return map.values();
    }

    /**
     * @see java.util.Map#containsKey(java.lang.Object)
     */
    public final boolean containsKey(final Object key)
    {
        return map.containsKey(key);
    }

    /**
     * @see java.util.Map#containsValue(java.lang.Object)
     */
    public final boolean containsValue(final Object value)
    {
        return map.containsValue(value);
    }

    /**
     * @see java.util.Map#entrySet()
     */
    public final Set entrySet()
    {
        return map.entrySet();
    }

    /**
     * @see java.util.Map#get(java.lang.Object)
     */
    public final Object get(final Object key)
    {
        return map.get(key);
    }

    /**
     * @see java.util.Map#isEmpty()
     */
    public final boolean isEmpty()
    {
        return map.isEmpty();
    }

    /**
     * @see java.util.Map#keySet()
     */
    public final Set keySet()
    {
        return map.keySet();
    }

    /**
     * @see java.util.Map#put(java.lang.Object, java.lang.Object)
     */
    public final Object put(final Object key, final Object value)
    {
        return map.put(key, value);
    }

    /**
     * @see java.util.Map#putAll(java.util.Map)
     */
    public final void putAll(final Map t)
    {
        map.putAll(t);
    }

    /**
     * @see java.util.Map#remove(java.lang.Object)
     */
    public final Object remove(final Object key)
    {
        return map.remove(key);
    }

    /**
     * @return Debug string representation of this map
     */
    public final String toDebugString()
    {
        return "[" + toString() + "]";
    }

    /**
     * Gets the string representation.
     * @return String representation of map consistent with tag attribute style of markup
     *         elements, for example: a="x" b="y" c="z"
     */
    public String toString()
    {
        final StringList list = new StringList();

        for (final Iterator iterator = map.keySet().iterator(); iterator.hasNext();)
        {
            final String key = (String) iterator.next();

            list.add(key + " = \"" + getString(key) + "\"");
        }

        return list.join(" ");
    }
}

///////////////////////////////// End of File /////////////////////////////////
