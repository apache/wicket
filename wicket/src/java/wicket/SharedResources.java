/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import wicket.util.file.Files;

/**
 * Class which holds shared resources. Resources can be shared by name. An
 * optional scope can be given to prevent naming conflicts and a locale and/or
 * style can be given as well.
 * 
 * @author Jonathan Locke
 */
public class SharedResources
{
	/** Map of shared resources */
	private final Map resourceMap = new HashMap();
	
	private final Application application;
	
	SharedResources(Application application)
	{
		this.application = application;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * Inserts _[locale] and _[style] into path just before any extension that
	 * might exist.
	 * 
	 * @param path
	 *            The resource path
	 * @param locale
	 *            The locale
	 * @param style
	 *            The style (see {@link wicket.Session})
	 * @return The localized path
	 */
	public static String path(final String path, final Locale locale, final String style)
	{
		final StringBuffer buffer = new StringBuffer();
		final String extension = Files.extension(path);
		final String basePath = Files.basePath(path, extension);
		buffer.append(basePath);
		if (locale != null)
		{
			if (!locale.equals(Locale.getDefault()))
			{
				buffer.append('_');
				buffer.append(locale.toString());
			}
		}
		if (style != null)
		{
			buffer.append('_');
			buffer.append(style);
		}
		if (extension != null)
		{
			buffer.append('.');
			buffer.append(extension);
		}
		return buffer.toString();
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT CALL IT.
	 * 
	 * @param application 
	 * `		  The application object
	 * @param scope
	 *            The scope of the resource
	 * @param path
	 *            The resource path
	 * @param locale
	 *            The locale
	 * @param style
	 *            The style (see {@link wicket.Session})
	 * @return The localized path
	 */
	public static String path(final Application application, final Class scope, final String path, final Locale locale,
			final String style)
	{
		return application.getPages().aliasForClass(scope) + '/' + path(path, locale, style);
	}

	/**
	 * @param scope
	 *            Scope of resource
	 * @param name
	 *            Logical name of resource
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The resource style (see {@link wicket.Session})
	 * @param resource
	 *            Resource to store
	 */
	public final void add(final Class scope, final String name, final Locale locale,
			final String style, final Resource resource)
	{
		// Store resource
		final String key = path(application,scope, name, locale, style);
		resourceMap.put(key, resource);

		// Application shared resources are cacheable.
		resource.setCacheable(true);
	}

	/**
	 * @param name
	 *            Logical name of resource
	 * @param locale
	 *            The locale of the resource
	 * @param resource
	 *            Resource to store
	 */
	public final void add(final String name, final Locale locale, final Resource resource)
	{
		add(Application.class, name, locale, null, resource);
	}

	/**
	 * @param name
	 *            Logical name of resource
	 * @param resource
	 *            Resource to store
	 */
	public final void add(final String name, final Resource resource)
	{
		add(Application.class, name, null, null, resource);
	}

	/**
	 * @param scope
	 *            The resource's scope
	 * @param name
	 *            Name of resource to get
	 * @param locale
	 *            The locale of the resource
	 * @param style
	 *            The resource style (see {@link wicket.Session})
	 * @return The logical resource
	 */
	public final Resource get(final Class scope, final String name, final Locale locale,
			final String style)
	{
		// 1. Look for fully qualified entry with locale and style
		if (locale != null && style != null)
		{
			final String key = path(application,scope, name, locale, style);
			final Resource resource = get(key);
			if (resource != null)
			{
				return resource;
			}
		}

		// 2. Look for entry without style
		if (locale != null)
		{
			final String key = path(application,scope, name, locale, null);
			final Resource resource = get(key);
			if (resource != null)
			{
				return resource;
			}
		}

		// 3. Look for entry without locale
		if (style != null)
		{
			final String key = path(application,scope, name, null, style);
			final Resource resource = get(key);
			if (resource != null)
			{
				return resource;
			}
		}

		// 4. Look for base name with no locale or style
		if (locale == null && style == null)
		{
			final String key = application.getPages().aliasForClass(scope) + '/' + name;
			return get(key);
		}

		// Resource not found!
		return null;
	}

	/**
	 * THIS METHOD IS NOT PART OF THE WICKET PUBLIC API. DO NOT USE IT.
	 * 
	 * @param key
	 *            Shared resource key
	 * @return The resource
	 */
	public final Resource get(final String key)
	{
		return (Resource)resourceMap.get(key);
	}
}
