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
package org.apache.wicket.util.value;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * <code>ValueMap</code> for attributes.
 * 
 * @author Eelco Hillenius
 * @since 1.2.6
 */
public final class AttributeMap extends ValueMap
{
	private static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty <code>AttributeMap</code>.
	 */
	public AttributeMap()
	{
		super();
	}

	/**
	 * Copy constructor.
	 * 
	 * @param map
	 *            a <code>Map</code> to be copied
	 */
	public AttributeMap(final Map<String, Object> map)
	{
		super(map);
	}

	/**
	 * Convenience method to put a value by {@link IAttributeMapKey}
	 * <p>
	 * see {@link #put(String, Object)}
	 */
	public Object put(IAttributeMapKey key, Object value)
	{
		return super.put(key.getAttributeKey(), value);
	}

	/**
	 * Convenience method to add a value by {@link IAttributeMapKey}
	 * <p>
	 * see {@link #add(String, String)}
	 */
	public Object add(IAttributeMapKey key, String value)
	{
		return super.add(key.getAttributeKey(), value);
	}

	/**
	 * Returns an {@link AttributeMap} containing single mapping.
	 * <p>
	 * Similar to {@link #of(Object, Object)}, but returns mutable map.
	 */
	public static AttributeMap of(IAttributeMapKey k1, String v1)
	{
		AttributeMap map = new AttributeMap();
		map.add(k1, v1);
		return map;
	}

	/**
	 * Returns an {@link AttributeMap} containing two mappings.
	 * <p>
	 * Similar to {@link #of(Object, Object)}, but returns mutable map.
	 */
	public static AttributeMap of(IAttributeMapKey k1, String v1, IAttributeMapKey k2, String v2)
	{
		AttributeMap map = new AttributeMap();
		map.add(k1, v1);
		map.add(k2, v2);
		return map;
	}

	/**
	 * Returns an {@link AttributeMap} containing three mappings.
	 * <p>
	 * Similar to {@link #of(Object, Object)}, but returns mutable map.
	 */
	public static AttributeMap of(IAttributeMapKey k1, String v1, IAttributeMapKey k2, String v2, IAttributeMapKey k3, String v3)
	{
		AttributeMap map = new AttributeMap();
		map.add(k1, v1);
		map.add(k2, v2);
		map.add(k3, v3);
		return map;
	}

	/**
	 * Convenience method computing value for {@link IAttributeMapKey},
	 * which accepts simple supplier for value.
	 * Calls {@link #compute(Object, BiFunction)} internally.
	 * <p>
	 * Doesn't add a mapping if computed value is <code>null</code>
	 */
	public Object compute(IAttributeMapKey key, Supplier supplier)
	{
		return compute(key.getAttributeKey(), (s, o) -> supplier.get());
	}

	/**
	 * Convenience method computing value for {@link IAttributeMapKey}.
	 * Calls {@link #compute(Object, BiFunction)} internally.
	 * <p>
	 * Doesn't add a mapping if computed value is <code>null</code>
	 */
	public Object compute(IAttributeMapKey key, java.util.function.BiFunction<? super String, ? super Object, ?> remappingFunction)
	{
		return super.compute(key.getAttributeKey(), remappingFunction);
	}

}
