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
import org.apache.wicket.request.resource.ResourceReferenceRegistry;

public class SharedResources
{
	private final ResourceReferenceRegistry registry;

	public SharedResources(ResourceReferenceRegistry registry)
	{
		this.registry = registry;
	}

	private static final class SharedResourceReference extends ResourceReference
	{
		private static final long serialVersionUID = 1L;

		private final IResource resource;

		public SharedResourceReference(Class<?> scope, String name, Locale locale, String style,
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
		ResourceReference ref = new SharedResourceReference(scope, name, locale, style, variation,
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
	 * Resolves a {@link ResourceReference} for a shared resource.
	 * 
	 * @param scope
	 * @param name
	 * @param locale
	 * @param style
	 * @param variation
	 * @param strict
	 * @return
	 */
	public ResourceReference get(Class<?> scope, String name, Locale locale, String style,
		String variation, boolean strict)
	{
		return registry.getResourceReference(scope, name, locale, style, variation, strict);
	}
}
