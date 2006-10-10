/*
 * $Id$ $Revision$ $Date$
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
package wicket.util.file;

import java.net.URL;

/**
 * Path for working with OSGi bundles.
 * 
 * @author Timur Mehrvarz
 */
public final class OsgiPath implements IResourceFinder
{
	/** ClassLoader to be used for locating resources. */
	final ClassLoader classLoader;

	/**
	 * Constructor.
	 * 
	 * @param classLoader
	 *            class loader to be used for locating resources
	 */
	public OsgiPath(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

	/**
	 * Looks for a given pathname along this path in the OSGi (jar) bundle.
	 * 
	 * @param pathname
	 *            The filename with possible path
	 * @return The url located on the path
	 */
	public URL find(final String pathname)
	{
		String resourcePathName = "/" + pathname;
		return classLoader.getResource(resourcePathName);
	}
}
