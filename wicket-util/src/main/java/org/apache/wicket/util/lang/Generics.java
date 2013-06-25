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
package org.apache.wicket.util.lang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Generics related utilities
 * 
 * @author igor.vaynberg
 */
public class Generics
{
	private Generics()
	{

	}

	/**
	 * Silences generics warning when need to cast iterator types
	 * 
	 * @param <T>
	 * @param delegate
	 * @return <code>delegate</code> iterator cast to proper generics type
	 */
	@SuppressWarnings("unchecked")
	public static <T> Iterator<T> iterator(final Iterator<?> delegate)
	{
		return (Iterator<T>)delegate;
	}

	/**
	 * Creates a new HashMap
	 * 
	 * @param <K>
	 * @param <V>
	 * @return new hash map
	 */
	public static <K, V> HashMap<K, V> newHashMap()
	{
		return new HashMap<>();
	}

	/**
	 * Creates a new HashMap
	 * 
	 * @param <K>
	 * @param <V>
	 * @param capacity
	 *            initial capacity
	 * @return new hash map
	 */
	public static <K, V> HashMap<K, V> newHashMap(final int capacity)
	{
		return new HashMap<>(capacity);
	}

	/**
	 * Creates a new ArrayList
	 * 
	 * @param <T>
	 * @param capacity
	 *            initial capacity
	 * @return array list
	 */
	public static <T> ArrayList<T> newArrayList(final int capacity)
	{
		return new ArrayList<>(capacity);
	}

	/**
	 * Creates a new ArrayList
	 * 
	 * @param <T>
	 * @return array list
	 */
	public static <T> ArrayList<T> newArrayList()
	{
		return new ArrayList<>();
	}

	/**
	 * Creates a new ConcurrentHashMap
	 * 
	 * @param <K>
	 * @param <V>
	 * @return new hash map
	 */
	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap()
	{
		return new ConcurrentHashMap<>();
	}

	/**
	 * Creates a new ConcurrentHashMap
	 * 
	 * @param <K>
	 * @param <V>
	 * @param initialCapacity
	 *            initial capacity
	 * @return new hash map
	 */
	public static <K, V> ConcurrentHashMap<K, V> newConcurrentHashMap(final int initialCapacity)
	{
		return new ConcurrentHashMap<>(initialCapacity);
	}


}
