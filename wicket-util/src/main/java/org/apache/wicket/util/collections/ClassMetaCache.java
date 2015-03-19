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
package org.apache.wicket.util.collections;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

/**
 * This class wraps a WeakHashMap that holds one ConcurrentHashMap per ClassLoader. In the rare
 * event of a previously unmapped ClassLoader, the WeakHashMap is replaced by a new one. This avoids
 * any synchronization overhead, much like a {@link java.util.concurrent.CopyOnWriteArrayList}
 * 
 * @param <T>
 *            type of objects stored in cache
 */
public class ClassMetaCache<T>
{
	private volatile Map<ClassLoader, ConcurrentHashMap<String, T>> cache = Collections.emptyMap();

	/**
	 * Puts value into cache
	 * 
	 * @param key
	 *            the class that will be used as the value's key
	 * @param value
	 *            the value that should be stored in cache
	 * @return value previously stored in cache for this key, or {@code null} if none
	 */
	public T put(final Class<?> key, final T value)
	{
		ConcurrentHashMap<String, T> container = getClassLoaderCache(key.getClassLoader(), true);
		return container.put(key(key), value);
	}

	/**
	 * Gets value from cache or returns {@code null} if not in cache
	 * 
	 * @param key
	 *            the class that is the key for the value
	 * @return value stored in cache or {@code null} if none
	 */
	public T get(final Class<?> key)
	{
		ConcurrentHashMap<String, T> container = getClassLoaderCache(key.getClassLoader(), false);
		if (container == null)
		{
			return null;
		}
		else
		{
			return container.get(key(key));
		}
	}

	/**
	 * @param classLoader
	 * @param create
	 * @return a {@link ConcurrentHashMap} mapping class names to injectable fields, never
	 *         <code>null</code>
	 */
	private ConcurrentHashMap<String, T> getClassLoaderCache(final ClassLoader classLoader,
		final boolean create)
	{
		ConcurrentHashMap<String, T> container = cache.get(classLoader);
		if (container == null)
		{
			if (!create)
			{
				return container;
			}

			// only lock in rare event of unknown ClassLoader
			synchronized (this)
			{
				// check again inside lock
				container = cache.get(classLoader);
				if (container == null)
				{
					container = new ConcurrentHashMap<>();

					/*
					 * don't write to current cache, copy instead
					 */
					Map<ClassLoader, ConcurrentHashMap<String, T>> newCache = new WeakHashMap<ClassLoader, ConcurrentHashMap<String, T>>(
						cache);
					newCache.put(classLoader, container);
					cache = Collections.unmodifiableMap(newCache);
				}
			}
		}
		return container;
	}

	/**
	 * converts class into a key used by the cache
	 * 
	 * @param clazz
	 * 
	 * @return string representation of the clazz
	 */
	private static String key(final Class<?> clazz)
	{
		return clazz.getName();
	}
}