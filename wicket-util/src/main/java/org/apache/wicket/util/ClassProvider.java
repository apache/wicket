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
package org.apache.wicket.util;

import java.lang.ref.WeakReference;

/**
 * A {@link IProvider provider} which holds a reference to a {@link Class class} in a
 * {@link WeakReference} so that it can be collected by the GC when needed
 * 
 * @param <T>
 *            the type of the class
 * @deprecated Use org.apache.wicket.util.reference.ClassReference instead
 */
@Deprecated
public class ClassProvider<T> implements IProvider<Class<T>>
{
	private final WeakReference<Class<T>> classRef;

	/**
	 * Construct.
	 * 
	 * @param clazz
	 */
	public ClassProvider(final Class<T> clazz)
	{
		classRef = new WeakReference<Class<T>>(clazz);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Class<T> get()
	{
		return classRef.get();
	}

	/**
	 * Creates a provider for the specified class
	 * 
	 * @param <T>
	 *            type of value
	 * @param clazz
	 *            the class to provide
	 * @return provider
	 */
	public static <T> ClassProvider<T> of(final Class<T> clazz)
	{
		return new ClassProvider<T>(clazz);
	}
}
