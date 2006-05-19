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
package wicket.util.resource.locator;

import java.util.Locale;

import wicket.util.resource.IResourceStream;
import wicket.util.string.Strings;

/**
 * Base class for implementing IResourceStreamLocators.
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
public abstract class AbstractResourceStreamLocator implements IResourceStreamLocator
{
	/**
	 * Constructor
	 */
	protected AbstractResourceStreamLocator()
	{
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
			final Locale locale, String extension)
	{
		if (extension == null)
		{
			extension = "." + Strings.lastPathComponent(path, '.');
			path = Strings.beforeLastPathComponent(path, '.');
		}
		else
		{
			if (!extension.startsWith("."))
			{
				extension = "." + extension;
			}
		}
		
		// 1. Try style, locale and extension
		if (style != null && locale != null)
		{
			final IResourceStream resource = locate(clazz, path + '_' + style, locale, extension);
			if (resource != null)
			{
				return resource;
			}
		}

		// 2. Try locale and extension
		if (locale != null)
		{
			final IResourceStream resource = locate(clazz, path, locale, extension);
			if (resource != null)
			{
				return resource;
			}
		}

		// 3. Try style and extension
		if (style != null)
		{
			final IResourceStream resource = locate(clazz, path + '_' + style + extension);
			if (resource != null)
			{
				return resource;
			}
		}

		// 4. Try just extension
		return locate(clazz, path + extension);
	}

	/**
	 * Subclass implementation locates the resource at the given path. Different
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
	protected abstract IResourceStream locate(final Class clazz, final String path);

	/**
	 * Locate a file based on its path (potentially with a style), a locale and
	 * an extension. See class comments for more details on how the locale is
	 * used and the order applied to find the resource.
	 * 
	 * @param clazz
	 *            The class requesting the resource
	 * @param path
	 *            Full path to resource, possibly including style, but not
	 *            locale or extension
	 * @param locale
	 *            The locale to apply
	 * @param extension
	 *            The resource's extension
	 * @return The resource, or null if not found.
	 */
	private IResourceStream locate(final Class clazz, final String path, final Locale locale,
			final String extension)
	{
		// 1. Apply Locale default toString() implementation. See Locale.
		{
			final IResourceStream resource = locate(clazz, path + '_' + locale.toString()
					+ extension);
			if (resource != null)
			{
				resource.setLocale(locale);
				return resource;
			}
		}

		// Get language and country, either of which may be the empty string
		final String language = locale.getLanguage();
		final String country = locale.getCountry();

		// 2. If country and language are available
		if (!Strings.isEmpty(language) && !Strings.isEmpty(country))
		{
			final IResourceStream resource = locate(clazz, path + '_' + language + '_' + country
					+ extension);
			if (resource != null)
			{
				resource.setLocale(new Locale(language, country));
				return resource;
			}
		}

		// 3. If language is available
		if (!Strings.isEmpty(language))
		{
			final IResourceStream resource = locate(clazz, path + '_' + language + extension);
			if (resource != null)
			{
				resource.setLocale(new Locale(language));
				return resource;
			}
		}

		// Not found
		return null;
	}
}
