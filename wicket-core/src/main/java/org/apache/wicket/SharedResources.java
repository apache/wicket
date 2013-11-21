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
package org.apache.wicket;

import java.util.Locale;

import org.apache.wicket.request.resource.IResource;
import org.apache.wicket.request.resource.ResourceReference;
import org.apache.wicket.request.resource.ResourceReference.Key;
import org.apache.wicket.request.resource.ResourceReferenceRegistry;
import org.apache.wicket.util.lang.Args;

/**
 * Class which holds shared resources. Resources can be shared by name. An optional scope can be
 * given to prevent naming conflicts and a locale and/or style can be given as well.
 * 
 * <p>
 * Unlike component hosted resources, shared resources have stable URLs, which makes them suitable
 * for indexing by web crawlers and caching by web browsers. As they are also not synchronised on
 * the {@link Session}, they can be loaded asynchronously, which is important with images and
 * resources such as JavaScript and CSS.
 */
public class SharedResources
{
	private final ResourceReferenceRegistry registry;

	/**
	 * Construct.
	 * 
	 * @param registry
	 */
	public SharedResources(ResourceReferenceRegistry registry)
	{
		this.registry = Args.notNull(registry, "registry");
	}

	/**
	 * A {@link ResourceReference} that is used to register a reference to a known {@link IResource}
	 */
	private static final class AutoResourceReference extends ResourceReference
	{
		private static final long serialVersionUID = 1L;

		private final IResource resource;

		private AutoResourceReference(Class<?> scope, String name, Locale locale, String style,
			String variation, IResource resource)
		{
			super(scope, name, locale, style, variation);
			this.resource = resource;
		}

		@Override
		public IResource getResource()
		{
			return resource;
		}
	}

	/**
	 * Adds a resource.
	 * 
	 * @param scope
	 *            Scope of resource
	 * @param name
	 *            Logical name of resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The resource style (see {@link org.apache.wicket.Session})
	 * @param variation
	 *            The component specific variation of the style
	 * @param resource
	 *            Resource to store
	 */
	public final void add(final Class<?> scope, final String name, final Locale locale,
		final String style, final String variation, final IResource resource)
	{
		ResourceReference ref = new AutoResourceReference(scope, name, locale, style, variation,
			resource);
		registry.registerResourceReference(ref);
	}

	/**
	 * Adds a resource.
	 * 
	 * @param name
	 *            Logical name of resource
	 * @param locale
	 *            The locale of the resource
	 * @param resource
	 *            Resource to store
	 */
	public final void add(final String name, final Locale locale, final IResource resource)
	{
		add(Application.class, name, locale, null, null, resource);
	}

	/**
	 * Adds a resource.
	 * 
	 * @param name
	 *            Logical name of resource
	 * @param resource
	 *            Resource to store
	 */
	public final void add(final String name, final IResource resource)
	{
		add(Application.class, name, null, null, null, resource);
	}

	/**
	 * Resolves a {@link ResourceReference} for a shared resource by using
	 * {@link org.apache.wicket.Application} as a scope and {@code null} for
	 * locale, style and variation.
	 *
	 * @param name
	 *            Logical name of resource
	 */
	public final ResourceReference get(String name)
	{
		return get(Application.class, name, null, null, null, false);
	}

	/**
	 * Resolves a {@link ResourceReference} for a shared resource.
	 * 
	 * @param scope
	 *            Scope of resource
	 * @param name
	 *            Logical name of resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The resource style (see {@link org.apache.wicket.Session})
	 * @param variation
	 *            The component specific variation of the style
	 * @param strict
	 *            If true, "weaker" combination of scope, name, locale etc. are not tested
	 * @return Either the resource reference found in the registry or, if requested, a resource
	 *         reference automatically created based on the parameters provided. The automatically
	 *         created resource reference will automatically be added to the registry.
	 */
	public ResourceReference get(Class<?> scope, String name, Locale locale, String style,
		String variation, boolean strict)
	{
		return registry.getResourceReference(scope, name, locale, style, variation, strict, true);
	}

	/**
	 * Removes a resource.
	 * 
	 * @param key
	 *            the resource reference's identifier
	 * @return the removed {@link ResourceReference}. {@code null} if there was no registration for
	 *         this {@link Key}
	 */
	public final ResourceReference remove(final Key key)
	{
		return registry.unregisterResourceReference(key);
	}
}
