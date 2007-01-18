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
package wicket.util.resource.locator;

import java.net.URL;
import java.util.Iterator;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.Application;
import wicket.util.file.IResourceFinder;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.UrlResourceStream;

/**
 * Locate Wicket resource.
 * <p>
 * Contains the logic to locate a resource based on a path, a style (see
 * {@link wicket.Session}), a locale and an extension string. The full filename
 * will be built like:
 * &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;.
 * <p>
 * Resource matches will be attempted in the following order:
 * <ol>
 * <li>1. &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>2. &lt;path&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>3. &lt;path&gt;_&lt;style&gt;.&lt;extension&gt;</li>
 * <li>4. &lt;path&gt;.&lt;extension&gt;</li>
 * </ol>
 * <p>
 * Locales may contain a language, a country and a region or variant.
 * Combinations of these components will be attempted in the following order:
 * <ol>
 * <li>locale.toString() see javadoc for Locale for more details</li>
 * <li>&lt;language&gt;_&lt;country&gt;</li>
 * <li>&lt;language&gt;</li>
 * </ol>
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public class ResourceStreamFactory implements IResourceStreamFactory
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(ResourceStreamFactory.class);

	/** If null, the application registered finder will be used */
	private IResourceFinder finder;
	
	/**
	 * Constructor
	 */
	public ResourceStreamFactory()
	{
	}
	
	/**
	 * Constructor
	 * 
	 * @param finder resource finder
	 */
	public ResourceStreamFactory(final IResourceFinder finder)
	{
		this.finder = finder;
	}

	/**
	 * Helper to get the Application registered resource stream locator
	 * 
	 * @return resource stream locator
	 */
	public static IResourceStreamFactory get()
	{
		return Application.get().getResourceSettings().getResourceStreamFactory();
	}
	
	/**
	 * Locate a resource. See class comments for more details.
	 * 
	 * @param clazz
	 *            The class requesting the resource
	 * @param path
	 *            The path of the resource without extension
	 * @param style
	 *            A theme or style (see {@link wicket.Session})
	 * @param locale
	 *            The Locale to apply
	 * @param extension
	 *            the filname's extensions
	 * 
	 * @return The Resource, or null if not found.
	 */
	public IResourceStream locate(final Class clazz, String path, final String style,
			final Locale locale, final String extension)
	{
		// Try the various combinations of style, locale and extension to find
		// the resource.
		
		// Most outer loop are the styles and variations
		Iterator<String> styleIter = new StyleAndVariationResourceNameIterator(path, style, null);
		while (styleIter.hasNext())
		{
			String newPath = styleIter.next();

			// next is the Locale
			LocaleResourceNameIterator localeIter = new LocaleResourceNameIterator(newPath, locale);
			while (localeIter.hasNext())
			{
				newPath = localeIter.next();

				// and than the possible resource extensions
				Iterator<String> extIter = new ExtensionResourceNameIterator(newPath, extension);
				while (extIter.hasNext())
				{
					newPath = extIter.next();
					IResourceStream stream = locate(clazz, newPath);
					if (stream != null)
					{
						stream.setLocale(localeIter.getLocale());
						return stream;
					}
				}
			}
		}

		return null;
	}

	/**
	 * Locates the resource at the given path. Different
	 * subclasses may take different approaches to the search.
	 * 
	 * @param clazz
	 *            The class requesting the resource
	 * 
	 * @param path
	 *            The complete path of the resource to locate. Separators must
	 *            be forward slashes.
	 * @return The Resource, or null if not found.
	 */
	public IResourceStream locate(final Class clazz, final String path)
	{
		// First search it on the resource on the classpath
		IResourceStream stream = locateByClassLoader(clazz, path);
		if (stream != null)
		{
			return stream;
		}

		// Than try the resource finder registered with the application
		stream = locateByResourceFinder(clazz, path);
		if (stream != null)
		{
			return stream;
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
	protected IResourceStream locateByClassLoader(final Class clazz, final String path)
	{
		ClassLoader classLoader = null;
		if (clazz != null)
		{
			classLoader = clazz.getClassLoader();
		}

		if (classLoader == null)
		{
			// use context classloader when no specific classloader is set
			// (package resources for instance)
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		if (classLoader == null)
		{
			// use Wicket classloader when no specific classloader is set
			classLoader = getClass().getClassLoader();
		}

		// Log attempt
		if (log.isDebugEnabled())
		{
			log.debug("Attempting to locate resource '" + path + "' using classloader "
					+ classLoader);
		}

		// Try loading path using classloader
		final URL url = classLoader.getResource(path);
		if (url != null)
		{
			return new UrlResourceStream(url);
		}
		return null;
	}

	/**
	 * Search the resource by means of the application registered resource
	 * finder
	 * 
	 * @param clazz
	 * @param path
	 * @return resource stream
	 */
	protected IResourceStream locateByResourceFinder(final Class clazz, final String path)
	{
		if (this.finder == null)
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
}
