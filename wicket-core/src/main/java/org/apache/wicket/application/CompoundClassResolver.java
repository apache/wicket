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
package org.apache.wicket.application;

import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.CopyOnWriteArrayList;

import org.apache.wicket.util.collections.UrlExternalFormComparator;
import org.apache.wicket.util.lang.Args;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A thread safe compound {@link IClassResolver}. Class resolving is done by iterating through all
 * {@link IClassResolver}s until the class is found. Resource resolving is done by combining the
 * results of all {@link IClassResolver}s.
 * 
 * @author Jesse Long
 */
public class CompoundClassResolver implements IClassResolver
{
	private static final Logger logger = LoggerFactory.getLogger(CompoundClassResolver.class);

	private final List<IClassResolver> resolvers = new CopyOnWriteArrayList<IClassResolver>();

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation iterates through all the {@link IClassResolver} trying to load the class
	 * until the class is found.
	 * 
	 * @param className
	 *            The name of the class to resolve.
	 * @return The {@link Class}, if it is found.
	 * @throws ClassNotFoundException
	 *             If the class was not found
	 */
	@Override
	public Class<?> resolveClass(final String className) throws ClassNotFoundException
	{
		boolean debugEnabled = logger.isDebugEnabled();

		for (IClassResolver resolver : resolvers)
		{
			try
			{
				return resolver.resolveClass(className);
			}
			catch (ClassNotFoundException cnfx)
			{
				if (debugEnabled)
				{
					logger.debug("ClassResolver '{}' cannot find class: '{}'", resolver.getClass()
						.getName(), className);
				}
			}
		}

		throw new ClassNotFoundException(className);
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * This implementation iterates through all {@link IClassResolver}s added, and combines the
	 * results into one {@link Set} of {@link URL}s, and returns an {@link Iterator} for the set.
	 * {@link URL}s are unique in the set.
	 * 
	 * @param name
	 *            The name of the resource to find.
	 * @return An {@link Iterator} of all the {@link URL}s matching the resource name.
	 */
	@Override
	public Iterator<URL> getResources(final String name)
	{
		Args.notNull(name, "name");

		Set<URL> urls = new TreeSet<URL>(new UrlExternalFormComparator());

		for (IClassResolver resolver : resolvers)
		{
			Iterator<URL> it = resolver.getResources(name);
			while (it.hasNext())
			{
				URL url = it.next();
				urls.add(url);
			}
		}

		return urls.iterator();
	}

	/**
	 * @return the class loader returned by the first registered IClassResolver. If there is no
	 *  registered IClassResolver then the current thread's context class loader will be returned.
	 */
	@Override
	public ClassLoader getClassLoader()
	{
		final ClassLoader classLoader;
		if (resolvers.isEmpty() == false)
		{
			classLoader = resolvers.iterator().next().getClassLoader();
		}
		else
		{
			classLoader = Thread.currentThread().getContextClassLoader();
		}
		return classLoader;
	}

	/**
	 * Adds a resolver
	 * 
	 * @param resolver
	 *            The resolver to add
	 * @return {@code this} for chaining
	 */
	public CompoundClassResolver add(final IClassResolver resolver)
	{
		Args.notNull(resolver, "resolver");
		resolvers.add(resolver);
		return this;
	}

	/**
	 * Removes a resolver
	 * 
	 * @param resolver
	 *            The resolver to remove
	 * @return {@code this} for chaining
	 */
	public CompoundClassResolver remove(final IClassResolver resolver)
	{
		resolvers.remove(resolver);
		return this;
	}
}