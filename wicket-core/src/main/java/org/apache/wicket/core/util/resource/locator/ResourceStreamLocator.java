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
package org.apache.wicket.core.util.resource.locator;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.apache.wicket.Application;
import org.apache.wicket.core.util.file.WebApplicationPath;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.file.Path;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceUtils;
import org.apache.wicket.util.resource.ResourceUtils.PathLocale;
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
 * <p>
 * Resources will be actually loaded by the {@link IResourceFinder}s defined in the resource
 * settings. By default there are finders that look in the classpath and in the classpath in
 * META-INF/resources. You can add more by adding {@link WebApplicationPath}s or {@link Path}s to
 * {@link IResourceSettings#getResourceFinders()}.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public class ResourceStreamLocator implements IResourceStreamLocator
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(ResourceStreamLocator.class);

	private static final Iterable<String> NO_EXTENSIONS = new ArrayList<String>(0);

	/** If null, the application registered finder will be used */
	private List<IResourceFinder> finders;

	/**
	 * Constructor
	 */
	public ResourceStreamLocator()
	{
		this((List<IResourceFinder>)null);
	}

	/**
	 * Constructor
	 * 
	 * @param finders
	 *            resource finders. These will be tried in the given order.
	 */
	public ResourceStreamLocator(final IResourceFinder... finders)
	{
		this(Arrays.asList(finders));
	}

	/**
	 * Constructor
	 * 
	 * @param finders
	 *            resource finders. These will be tried in the given order.
	 */
	public ResourceStreamLocator(final List<IResourceFinder> finders)
	{
		this.finders = finders;
	}

	/**
	 * 
	 * @see org.apache.wicket.core.util.resource.locator.IResourceStreamLocator#locate(java.lang.Class,
	 *      java.lang.String)
	 */
	@Override
	public IResourceStream locate(final Class<?> clazz, final String path)
	{
		// First try with the resource finder registered with the application
		// (allows for markup reloading)
		if (finders == null)
		{
			finders = Application.get().getResourceSettings().getResourceFinders();
		}

		IResourceStream result;
		for (IResourceFinder finder : finders)
		{
			log.debug("Attempting to locate resource '{}' using finder'{}'", path, finder);
			result = finder.find(clazz, path);
			if (result != null)
			{
				return result;
			}
		}
		return null;
	}

	/**
	 * 
	 * @see org.apache.wicket.core.util.resource.locator.IResourceStreamLocator#locate(java.lang.Class,
	 *      java.lang.String, java.lang.String, java.lang.String, java.util.Locale,
	 *      java.lang.String, boolean)
	 */
	@Override
	public IResourceStream locate(final Class<?> clazz, String path, final String style,
		final String variation, Locale locale, final String extension, final boolean strict)
	{
		// If path contains a locale, than it'll replace the locale provided to this method
		PathLocale data = ResourceUtils.getLocaleFromFilename(path);
		if ((data != null) && (data.locale != null))
		{
			path = data.path;
			locale = data.locale;
		}

		// Try the various combinations of style, locale and extension to find the resource.
		IResourceNameIterator iter = newResourceNameIterator(path, locale, style, variation,
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
	 * Get the resource
	 * 
	 * @param classLoader
	 * @param path
	 * @return resource stream
	 */
	/* package private for testing */IResourceStream getResourceStream(
		final ClassLoader classLoader, final String path)
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

		// Try loading path using classloader
		URL url = classLoader.getResource(path);
		if (url == null)
		{
			// maybe it is in the Servlet 3.0 like directory
			url = classLoader.getResource("META-INF/resources/" + path);
		}

		if (url != null)
		{
			return new UrlResourceStream(url);
		}
		return null;
	}

	/**
	 * 
	 * @see org.apache.wicket.core.util.resource.locator.IResourceStreamLocator#newResourceNameIterator(java.lang.String,
	 *      java.util.Locale, java.lang.String, java.lang.String, java.lang.String, boolean)
	 */
	@Override
	public IResourceNameIterator newResourceNameIterator(final String path, final Locale locale,
		final String style, final String variation, final String extension, final boolean strict)
	{
		final Iterable<String> extensions;

		final String realPath;

		if ((extension == null) && (path != null) && (path.indexOf('.') != -1))
		{
			// extract the path and extension
			realPath = Strings.beforeLast(path, '.');
			String realExtension = Strings.afterLast(path, '.');
			if (realExtension.indexOf(',') > -1)
			{
				// multiple extensions are not allowed in the path parameter
				// it could be an attack, so ignore it and pretend there are no resources
				return new EmptyResourceNameIterator();
			}
			extensions = Collections.singleton(realExtension);
		}
		else
		{
			realPath = path;
			if (extension == null)
			{
				extensions = NO_EXTENSIONS;
			}
			else
			{
				String[] commaSeparated = Strings.split(extension, ',');
				extensions = Arrays.asList(commaSeparated);
			}
		}

		return new ResourceNameIterator(realPath, style, variation, locale, extensions, strict);
	}
}
