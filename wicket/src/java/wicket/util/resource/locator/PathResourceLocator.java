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

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.util.file.File;
import wicket.util.file.Path;
import wicket.util.resource.FileResource;
import wicket.util.resource.IResource;

/**
 * IResourceLocator implementation that locates resources along a filesystem
 * search path.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class PathResourceLocator extends AbstractResourceLocator
{
	/** Logging */
	private static Log log = LogFactory.getLog(ResourceLocator.class);

	/** The path to search along */
	private Path searchPath;

	/**
	 * Constructor
	 * 
	 * @param searchPath
	 *            The path to search
	 */
	public PathResourceLocator(final Path searchPath)
	{
		this.searchPath = searchPath;
	}

	/**
	 * @see wicket.util.resource.locator.AbstractResourceLocator#locate(java.lang.String)
	 */
	protected IResource locate(final String path)
	{
		// Log attempt
		log.debug("Attempting to locate resource '" + path + "' on path " + searchPath);

		// Try to find file resource on the path supplied
		final File file = searchPath.find(path);

		// Found resource?
		if (file != null)
		{
			// Return file resource
			return new FileResource(file);
		}
		return null;
	}
}
