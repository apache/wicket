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

import java.net.URL;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.resource.IResource;
import wicket.util.resource.UrlResource;

/**
 * IResourceLocator implementation that locates resources using a class loader.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class ClassLoaderResourceLocator extends AbstractResourceLocator
{
	/** Logging */
	private static Log log = LogFactory.getLog(ResourceLocator.class);

	/** The path to search along */
	private ClassLoader classloader;

	/**
	 * Constructor
	 */
	public ClassLoaderResourceLocator()
	{
	}

	/**
	 * Constructor
	 * 
	 * @param classloader
	 *            The class loader to search
	 */
	public ClassLoaderResourceLocator(final ClassLoader classloader)
	{
		this.classloader = classloader;
	}

	/**
	 * @see wicket.util.resource.locator.AbstractResourceLocator#locate(java.lang.String)
	 */
	protected IResource locate(final String path)
	{
		// Ensure classloader
		if (classloader == null)
		{
			classloader = getClass().getClassLoader();
		}

		// Log attempt
		log.debug("Attempting to locate resource '" + path + "' using classloader " + classloader);

		// Try loading path using classloader
		final URL url = classloader.getResource(path);
		if (url != null)
		{
			return new UrlResource(url);
		}
		return null;
	}
}
