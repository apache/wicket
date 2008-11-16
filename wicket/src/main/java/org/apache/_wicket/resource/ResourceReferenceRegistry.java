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
package org.apache._wicket.resource;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.util.lang.Objects;

/**
 * Allows to register and lookup {@link ResourceReference}s.
 * 
 * @author Matej Knopp
 */
public class ResourceReferenceRegistry
{
	private static class Key
	{
		private final String scope;
		private final String path;
		private final Locale locale;
		private final String style;

		public Key(String scope, String path, Locale locale, String style)
		{
			if (scope == null)
			{
				throw new IllegalArgumentException("Argument 'scope' can not be null.");
			}
			this.scope = scope.toString();

			if (path == null)
			{
				throw new IllegalArgumentException("Argument 'path' can not be null.");
			}
			this.path = path;
			this.locale = locale;
			this.style = style;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
			{
				return true;
			}
			if (obj instanceof Key)
			{
				return false;
			}
			Key that = (Key)obj;
			return Objects.equal(scope, that.scope) && //
				Objects.equal(path, that.path) && //
				Objects.equal(locale, that.locale) && //
				Objects.equal(style, that.style);
		}

		@Override
		public int hashCode()
		{
			return Objects.hashCode(scope, path, locale, style);
		}
	};

	private static Map<Key, ResourceReference> map = new ConcurrentHashMap<Key, ResourceReference>();

	/**
	 * Registers the given {@link ResourceReference}.
	 * 
	 * @param reference
	 */
	public void registerResourceReference(ResourceReference reference)
	{
		if (reference == null)
		{
			throw new IllegalArgumentException("Argument 'reference' may not be null.");
		}
		Key key = new Key(reference.getScope().getName(), reference.getPath(),
			reference.getLocale(), reference.getStyle());
		map.put(key, reference);
	}

	/**
	 * Unregisters the given {@link ResourceReference}.
	 * 
	 * @param reference
	 */
	public void unregisterResourceReference(ResourceReference reference)
	{
		if (reference == null)
		{
			throw new IllegalArgumentException("Argument 'reference' may not be null.");
		}
		Key key = new Key(reference.getScope().getName(), reference.getPath(),
			reference.getLocale(), reference.getStyle());
		map.remove(key);
	}

	/**
	 * Looks up resource reference with specified attributes. If the reference is not found and
	 * <code>strict</code> is set to <code>false</code>, result of
	 * {@link #createDefaultResourceReference(Class, String, Locale, String)} is returned.
	 * 
	 * @param scope
	 *            mandatory parameter
	 * @param name
	 *            mandatory parameter
	 * @param locale
	 * @param style
	 * @param strict
	 *            if <code>strict</code> is <code>true</code> only resources that match exactly
	 *            are returened. Otherwise if there is no resource registered that is an exact
	 *            match, also resources with <code>null</code> style and locale are tried. If
	 *            still no resource is found, result of
	 *            {@link #createDefaultResourceReference(Class, String, Locale, String)} is
	 *            returned.
	 * @return {@link ResourceReference} or <code>null</code>
	 */
	public ResourceReference getResourceReference(Class<?> scope, String name, Locale locale,
		String style, boolean strict)
	{
		Key key = new Key(scope.getName(), name, locale, style);
		ResourceReference res = map.get(key);
		if (strict || res != null)
		{
			return res;
		}
		else
		{
			res = getResourceReference(scope, name, locale, null, true);
			if (res == null)
			{
				res = getResourceReference(scope, name, null, null, true);
			}
			if (res == null)
			{
				res = createDefaultResourceReference(scope, name, locale, style);
			}
			return res;
		}
	}

	protected ResourceReference createDefaultResourceReference(Class<?> scope, String name,
		Locale locale, String style)
	{
		// override in superclass to e.g. return PackageResourceReference if there is one
		return null;
	}
}
