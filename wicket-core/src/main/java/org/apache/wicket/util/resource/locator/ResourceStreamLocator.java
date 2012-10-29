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
package org.apache.wicket.util.resource.locator;

import java.net.URL;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceUtils;
import org.apache.wicket.util.resource.ResourceUtils.PathLocale;
import org.apache.wicket.util.resource.UrlResourceStream;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Locate Wicket resource.
 * <p>
 * Contains the logic to locate a resource based on a path, a style (see
 * {@link org.apache.wicket.Session}), a locale and an extension string. The full filename will be
 * built like: &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;.
 * <p>
 * Resource matches will be attempted in the following order:
 * <ol>
 * <li>1. &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>2. &lt;path&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>3. &lt;path&gt;_&lt;style&gt;.&lt;extension&gt;</li>
 * <li>4. &lt;path&gt;.&lt;extension&gt;</li>
 * </ol>
 * <p>
 * Locales may contain a language, a country and a region or variant. Combinations of these
 * components will be attempted in the following order:
 * <ol>
 * <li>locale.toString() see javadoc for Locale for more details</li>
 * <li>&lt;language&gt;_&lt;country&gt;</li>
 * <li>&lt;language&gt;</li>
 * </ol>
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public class ResourceStreamLocator implements IResourceStreamLocator
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(ResourceStreamLocator.class);

	/** If null, the application registered finder will be used */
	private IResourceFinder finder;

	private final List<String> classpathLocationPrefixes = new LinkedList<String>();
	{
		classpathLocationPrefixes.add("");
		classpathLocationPrefixes.add("META-INF/resources");
	}

	/**
	 * Constructor
	 */
	public ResourceStreamLocator()
	{
	}

	/**
	 * Constructor
	 * 
	 * @param finder
	 *            resource finder
	 */
	public ResourceStreamLocator(final IResourceFinder finder)
	{
		this.finder = finder;
	}

	/**
	 * 
	 * @see org.apache.wicket.util.resource.locator.IResourceStreamLocator#locate(java.lang.Class,
	 *      java.lang.String)
	 */
	public IResourceStream locate(final Class<?> clazz, final String path)
	{
		// First try with the resource finder registered with the application
		// (allows for markup reloading)
		IResourceStream stream = locateByResourceFinder(clazz, path);
		if (stream != null)
		{
			return stream;
		}

		// Then search the resource on the classpath
		stream = locateByClassLoader(clazz, path);
		if (stream != null)
		{
			return stream;
		}

		return null;
	}

	/**
	 * 
	 * @see org.apache.wicket.util.resource.locator.IResourceStreamLocator#locate(java.lang.Class,
	 *      java.lang.String, java.lang.String, java.lang.String, java.util.Locale,
	 *      java.lang.String, boolean)
	 */
	public IResourceStream locate(final Class<?> clazz, String path, final String style,
		final String variation, Locale locale, final String extension, final boolean strict)
	{
		// If path contains a locale, then it'll replace the locale provided to this method
		PathLocale data = ResourceUtils.getLocaleFromFilename(path);
		if ((data != null) && (data.locale != null))
		{
			path = data.path;
			locale = data.locale;
		}

		// Try the various combinations of style, locale and extension to find the resource.
		ResourceNameIterator iter = newResourceNameIterator(path, locale, style, variation,
			extension, strict);
		while (iter.hasNext())
		{
			String newPath = iter.next();

			IResourceStream stream = locate(clazz, newPath);
			if (stream != null)
			{
				stream.setLocale(iter.getLocale());
				stream.setStyle(iter.getStyle());
				stream.setVariation(iter.getVariation());
				return stream;
			}
		}

		return null;
	}

	/**
	 * Search the the resource my means of the various classloaders available
	 * 
	 * @param clazz
	 * @param path
	 * @return resource stream
	 */
	protected IResourceStream locateByClassLoader(final Class<?> clazz, final String path)
	{
		IResourceStream resourceStream = null;

		if (clazz != null)
		{
			resourceStream = getResourceStream(clazz.getClassLoader(), path);
			if (resourceStream != null)
			{
				return resourceStream;
			}
		}

		// use context classloader when no specific classloader is set
		// (package resources for instance)
		resourceStream = getResourceStream(Thread.currentThread().getContextClassLoader(), path);
		if (resourceStream != null)
		{
			return resourceStream;
		}

		// use Wicket classloader when no specific classloader is set
		resourceStream = getResourceStream(getClass().getClassLoader(), path);
		if (resourceStream != null)
		{
			return resourceStream;
		}
		return null;
	}

	/**
	 * Get the resource
	 * 
	 * @param classLoader
	 * @param path
	 * @return resource stream
	 */
	private IResourceStream getResourceStream(final ClassLoader classLoader, final String path)
	{
		if (classLoader == null)
		{
			return null;
		}

		if (log.isDebugEnabled())
		{
			log.debug("Attempting to locate resource '" + path + "' using classloader " +
				classLoader);
		}

		for (String prefix : classpathLocationPrefixes)
		{
			String fullPath = prefix.length() > 0 ? prefix + "/" + path : path;
			URL url = classLoader.getResource(fullPath);
			if (url != null)
			{
				return new UrlResourceStream(url);
			}
		}

		return null;
	}

	/**
	 * Search the resource by means of the application registered resource finder
	 * 
	 * @param clazz
	 * @param path
	 * @return resource stream
	 */
	protected IResourceStream locateByResourceFinder(final Class<?> clazz, final String path)
	{
		if (finder == null)
		{
			finder = Application.get().getResourceSettings().getResourceFinder();
		}

		// Log attempt
		if (log.isDebugEnabled())
		{
			log.debug("Attempting to locate resource '" + path + "' on path " + finder);
		}

		// Try to find file resource on the path supplied
		return finder.find(clazz, path);
	}

	/**
	 * 
	 * @see org.apache.wicket.util.resource.locator.IResourceStreamLocator#newResourceNameIterator(java.lang.String,
	 *      java.util.Locale, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	public ResourceNameIterator newResourceNameIterator(final String path, final Locale locale,
		final String style, final String variation, final String extension, final boolean strict)
	{
		final String realPath;
		final String realExtension;

		if ((extension == null) && (path != null) && (path.indexOf('.') != -1))
		{
			realPath = Strings.beforeLast(path, '.');
			// for extensions with separator take the first extension
			realExtension = Strings.afterLast(path, '.');
			if (realExtension.indexOf(',') > -1)
			{
				// multiple extensions are not allowed in the path parameter
				return new EmptyResourceNameIterator();
			}
		}
		else
		{
			realPath = path;
			realExtension = extension;
		}

		return new ResourceNameIterator(realPath, style, variation, locale, realExtension, strict);
	}

	/**
	 * The list of prefixes that are added to a path when trying to find resources in the classpath.
	 * By default, these are:
	 * <ul>
	 * <li><code>""</code> - no prefix</li>
	 * <li><code>"META-INF/resources"</code> - for Servlet 3.0 compatibility.</li>
	 * </ul>
	 * 
	 * @return
	 */
	public List<String> getClasspathLocationPrefixes()
	{
		return classpathLocationPrefixes;
	}
}
