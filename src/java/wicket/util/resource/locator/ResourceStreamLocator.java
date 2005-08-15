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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.resource.IResourceStream;
import wicket.util.string.Strings;

/**
 * Helper class that adds convenience methods to any IResourceStreamLocator.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public class ResourceStreamLocator implements IResourceStreamLocator
{
	/** Logging */
	private static Log log = LogFactory.getLog(ResourceStreamLocator.class);

	/** The resource locator */
	private IResourceStreamLocator locator;

	/**
	 * Constructor
	 * 
	 * @param locator
	 *            The resource locator
	 */
	public ResourceStreamLocator(IResourceStreamLocator locator)
	{
		this.locator = locator;
	}

	/**
	 * Locate a resource based on a class and an extension.
	 * 
	 * @param c
	 *            Class next to which the resource should be found
	 * @param extension
	 *            Resource extension
	 * @return The resource
	 */
	public IResourceStream locate(final Class c, final String extension)
	{
		return locate(c, null, Locale.getDefault(), extension);
	}

	/**
	 * Locate a resource based on a a class, a style, a locale and an extension.
	 * 
	 * @param c
	 *            Class next to which the resource should be found
	 * @param style
	 *            Any resource style, such as a skin style (see {@link wicket.Session})
	 * @param locale
	 *            The locale of the resource to load
	 * @param extension
	 *            Resource extension
	 * @return The resource
	 */
	public IResourceStream locate(final Class c, final String style, final Locale locale,
			final String extension)
	{
		return locate(c.getClassLoader(), c.getName().replace('.','/'), style, locale, extension);
	}

	/**
	 * Convenience method to load a resource. If no extension is specified, this
	 * convenience method will extract the extension from the path. If the
	 * extension does not start with a dot, one will be added automatically.
	 * @param loader class loader
	 * @param path
	 *            The path of the resource
	 * @param style
	 *            Any resource style, such as a skin style (see {@link wicket.Session})
	 * @param locale
	 *            The locale of the resource to load
	 * @param extension
	 *            The extension of the resource
	 * 
	 * @return The resource
	 */
	public IResourceStream locate(ClassLoader loader, String path, final String style,
			final Locale locale, final String extension)
	{
		// If no extension specified, extract extension
		final String extensionString;
		if (extension == null)
		{
			extensionString = "." + Strings.lastPathComponent(path, '.');
			path = Strings.beforeLastPathComponent(path, '.');
		}
		else
		{
			if (extension.startsWith("."))
			{
				extensionString = extension;
			}
			else
			{
				extensionString = "." + extension;
			}
		}

		return locator.locate(loader, path, style, locale, extensionString);
	}
}
