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
package org.apache.wicket.util.reference;

import java.io.Serializable;
import java.lang.ref.WeakReference;

import org.apache.wicket.core.util.lang.WicketObjects;
import org.apache.wicket.util.IProvider;

/**
 * A serialization-safe reference to a {@link Class}
 * 
 * @author igor
 * 
 * @param <T>
 *            type of class
 */
public class ClassReference<T> implements Serializable, IProvider<Class<T>>
{
	private static final long serialVersionUID = 1L;

	private transient WeakReference<Class<T>> cache;
	private final String name;

	/**
	 * Constructor
	 * 
	 * @param clazz
	 *          The referenced class
	 */
	public ClassReference(Class<T> clazz)
	{
		name = clazz.getName();
		cache(clazz);
	}

	/**
	 * @return the {@link Class} stored in this reference
	 */
	public Class<T> get()
	{
		Class<T> clazz = cache != null ? cache.get() : null;
		if (clazz == null)
		{
			clazz = WicketObjects.resolveClass(name);
			if (clazz == null)
			{
				throw new RuntimeException("Could not resolve class: " + name);
			}
			cache(clazz);
		}
		return clazz;
	}

	private void cache(Class<T> clazz)
	{
		cache = new WeakReference<>(clazz);
	}

	/**
	 * Diamond operator factory
	 * 
	 * @param clazz
	 *          The referenced class
	 * @return class reference
	 */
	public static <T> ClassReference<T> of(Class<T> clazz)
	{
		return new ClassReference<>(clazz);
	}
}
