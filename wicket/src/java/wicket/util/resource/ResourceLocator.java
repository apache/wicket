/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.resource;

import java.net.URL;
import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.file.File;
import wicket.util.file.Path;
import wicket.util.string.Strings;

/**
 * Contains static methods for use in locating resources.
 * <p>
 * A very simple ResourceLocator to locate a resource based on a path, a style,
 * a locale and an extension string. The full filename will be built like:
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
public abstract class ResourceLocator
{
	/** Logging */
	private static Log log = LogFactory.getLog(ResourceLocator.class);

	/**
	 * Locate a resource based on a class and an extension.
	 * 
	 * @param c
	 *            Class next to which the resource should be found
	 * @param extension
	 *            Resource extension
	 * @return The resource
	 * @see ResourceLocator#locate(Path, Class, String, Locale, String)
	 */
	public static IResource locate(final Class c, final String extension)
	{
		return locate(new Path(), c, extension);
	}

	/**
	 * Locate a resource based on a path, a class and an extension.
	 * 
	 * @param path
	 *            Path to search for resource
	 * @param c
	 *            Class next to which the resource should be found
	 * @param extension
	 *            Resource extension
	 * @return The resource
	 * @see ResourceLocator#locate(Path, Class, String, Locale, String)
	 */
	public static IResource locate(final Path path, final Class c, final String extension)
	{
		return locate(path, c, null, Locale.getDefault(), extension);
	}

	/**
	 * Locate a resource based on a path, a class, a style, a locale and an
	 * extension.
	 * 
	 * @param path
	 *            Path to search for resource
	 * @param c
	 *            Class next to which the resource should be found
	 * @param style
	 *            Any resource style, such as a skin style
	 * @param locale
	 *            The locale of the resource to load
	 * @param extension
	 *            Resource extension
	 * @return The resource
	 * @see ResourceLocator#locate(Path, Class, String, Locale, String)
	 */
	public static IResource locate(final Path path, final Class c, final String style,
			final Locale locale, final String extension)
	{
		return locate(path, c.getClassLoader(), c.getName(), style, locale, extension);
	}

	/**
	 * Loads a resource. This method prefers to load from the path argument
	 * first. If the resource cannot be found on the path, the classloader
	 * provided is searched. Resources are located using the style, locale and
	 * extension provided and the naming logic encapsulated in ResourceLocator.
	 * 
	 * @param path
	 *            Path to search for resource
	 * @param classloader
	 *            ClassLoader to search if not found on path
	 * @param resourcePath
	 *            The path of the resource
	 * @param style
	 *            Any resource style, such as a skin style
	 * @param locale
	 *            The locale of the resource to load
	 * @param extension
	 *            The extension of the resource
	 * @return The resource
	 * @see ResourceLocator
	 */
	public static IResource locate(final Path path, final ClassLoader classloader,
			String resourcePath, final String style, final Locale locale, final String extension)
	{
		// If no extension specified, extract extension
		final String extensionString;
		if (extension == null)
		{
			extensionString = "." + Strings.lastPathComponent(resourcePath, '.');
			resourcePath = Strings.beforeLastPathComponent(resourcePath, '.');
		}
		else
		{
			extensionString = "." + extension;
		}

		// Compute string components
		resourcePath = resourcePath.replace('.', '/');

		// 1. Search the path provided
		if (path != null && path.size() > 0)
		{
			final IResource resource = new ResourceLocator()
			{
				/**
				 * Check if file exists.
				 * 
				 * @param name
				 *            Name of the resource to find
				 * @return Resource, or null if file not found
				 */
				protected IResource locate(final String name)
				{
					// Log attempt
					log.debug("Attempting to locate resource '" + name + "' on path");

					// Try to find file resource on the path supplied
					final File file = path.find(name);

					// Found resource?
					if (file != null)
					{
						// Return file resource
						return new FileResource(file);
					}
					return null;
				}
			}.locate(resourcePath, style, locale, extensionString);

			if (resource != null)
			{
				return resource;
			}
		}

		// 2. Search the ClassLoader provided
		return new ResourceLocator()
		{
			/**
			 * Locate resource using classloader
			 * 
			 * @param name
			 *            Name of resource
			 * @return The resource
			 */
			protected IResource locate(final String name)
			{
				// Log attempt
				log.debug("Attempting to locate resource '" + name + "' on classpath");

				// Try loading filename on classpath
				final URL url = classloader.getResource(name);
				if (url != null)
				{
					return new UrlResource(url);
				}
				return null;
			}
		}.locate(resourcePath, style, locale, extensionString);
	}

	/**
	 * Finds a Resource with a given class name and extension
	 * 
	 * @param classname
	 *            A fully qualified class name with dotted separators, such as
	 *            "com.whatever.MyPage"
	 * @param extension
	 *            The resource extension including '.', such as ".html"
	 * @return The Resource, or null if it does not exist
	 */
	public static IResource locate(final String classname, final String extension)
	{
		String filename = Strings.replaceAll(classname, ".", "/") + extension;
		final URL url = Thread.currentThread().getContextClassLoader().getResource(filename);
		return url != null ? new UrlResource(url) : null;
	}

	/**
	 * Force use of locate() static methods.
	 */
	private ResourceLocator()
	{
	}

	/**
	 * Subclass implementation locates the resource at the given path. Different
	 * subclasses may take different approaches to the search.
	 * 
	 * @param path
	 *            The complete path of the resource to locate
	 * @return The Resource, or null if not found.
	 */
	protected abstract IResource locate(final String path);

	/**
	 * Locate a file based on its path (potentially with a style), a locale and
	 * an extension. See class comments for more details on how the locale is
	 * used and the order applied to find the resource.
	 * 
	 * @param path
	 *            Full path to resource including style, but not locale or
	 *            extension
	 * @param locale
	 *            The locale to apply
	 * @param extension
	 *            The resource's extension
	 * @return The Resource, or null if not found.
	 */
	IResource locate(final String path, final Locale locale, final String extension)
	{
		// 1. Apply Locale default toString() implementation. See Locale.
		{
			final IResource resource = locate(path + locale.toString() + extension);
			if (resource != null)
			{
				return resource;
			}
		}

		// 2. If country is available
		if (locale.getCountry() != null && locale.getCountry().length() > 0)
		{
			final String localeString = locale.getLanguage() + "_" + locale.getCountry();
			final IResource resource = locate(path + localeString + extension);
			if (resource != null)
			{
				return resource;
			}
		}

		// 3. If (at least the) language is available
		if (locale.getLanguage() != null && locale.getLanguage().length() > 0)
		{
			final String localeString = locale.getLanguage();
			final IResource resource = locate(path + localeString + extension);
			if (resource != null)
			{
				return resource;
			}
		}

		// Not found
		return null;
	}

	/**
	 * Locate a resource. See class comments for more details.
	 * 
	 * @param path
	 *            The path of the resource without extension
	 * @param style
	 *            A theme or style
	 * @param locale
	 *            The Locale to apply
	 * @param extension
	 *            the filname's extensions
	 * @return The Resource, or null if not found.
	 */
	IResource locate(final String path, final String style, final Locale locale,
			final String extension)
	{
		// 1. Try style and locale on classpath
		if (style != null && locale != null)
		{
			final IResource resource = locate(path + "_" + style + "_", locale, extension);
			if (resource != null)
			{
				return resource;
			}
		}

		// 2. Try locale only
		if (locale != null)
		{
			final IResource resource = locate(path + "_", locale, extension);
			if (resource != null)
			{
				return resource;
			}
		}

		// 3. Try style only
		if (style != null)
		{
			final IResource resource = locate(path + "_" + style + extension);
			if (resource != null)
			{
				return resource;
			}
		}

		// 4. Try without style and without locale
		return locate(path + extension);
	}
}
