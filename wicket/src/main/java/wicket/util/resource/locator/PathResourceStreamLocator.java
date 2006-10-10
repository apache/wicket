/*
 * $Id: PathResourceStreamLocator.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20
 * May 2006) joco01 $ $Revision$ $Date: 2006-05-20 00:32:57 +0000 (Sat,
 * 20 May 2006) $
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

import wicket.util.file.Path;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.UrlResourceStream;

/**
 * IResourceStreamLocator implementation that locates resources along a
 * filesystem search path.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class PathResourceStreamLocator extends AbstractResourceStreamLocator
{
	/** Logging */
	private static final Log log = LogFactory.getLog(PathResourceStreamLocator.class);

	/** The path to search along */
	private Path searchPath;

	/**
	 * Constructor
	 * 
	 * @param searchPath
	 *            The path to search
	 */
	public PathResourceStreamLocator(final Path searchPath)
	{
		this.searchPath = searchPath;
	}

	/**
	 * @see wicket.util.resource.locator.AbstractResourceStreamLocator#locate(Class,
	 *      java.lang.String)
	 */
	@Override
	public IResourceStream locate(final Class clazz, final String path)
	{
		// Log attempt
		if (log.isDebugEnabled())
		{
			log.debug("Attempting to locate resource '" + path + "' on path " + searchPath);
		}

		// Try to find file resource on the path supplied
		final URL url = searchPath.find(path);

		// Found resource?
		if (url != null)
		{
			// Return file resource
			return new UrlResourceStream(url);
		}
		return null;
	}
}
