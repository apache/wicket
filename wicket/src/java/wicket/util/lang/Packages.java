/*
 * $Id$ $Revision:
 * 1.4 $ $Date$
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
package wicket.util.lang;

import wicket.util.string.IStringIterator;
import wicket.util.string.StringList;
import wicket.util.string.Strings;

/**
 * Utilities for dealing with packages.
 * 
 * @author Jonathan Locke
 */
public final class Packages
{
	/**
	 * Instantiation not allowed
	 */
	private Packages()
	{
	}

	/**
	 * Gets the name of a given package
	 * 
	 * @param c
	 *            The class
	 * @return The class' package
	 */
	public static String name(final Class c)
	{
		return Strings.beforeLastPathComponent(c.getName(), '.');
	}

	/**
	 * Takes a package and a relative path to a resource and returns an absolute
	 * path to the resource. For example, if the given package was java.lang and
	 * the relative path was "../util/List", then "java/util/List" would be
	 * returned.
	 * 
	 * @param p
	 *            The package to start at
	 * @param path
	 *            The relative path to the class
	 * @return The absolute path
	 */
	public static String absolutePath(final Package p, final String path)
	{
		// Is path already absolute?
		if (path.startsWith("/"))
		{
			return path;
		}
		else
		{
			// Break package into list of package names
			final StringList absolutePath = StringList.tokenize(p.getName(), ".");
			
			// Break path into folders
			final StringList folders = StringList.tokenize(path, "/\\");
		
			// Iterate through folders
			for (final IStringIterator iterator = folders.iterator(); iterator.hasNext();)
			{
				// Get next folder
				final String folder = iterator.next();
	
				// Up one?
				if (folder.equals(".."))
				{
					// Pop off stack
					if (absolutePath.size() > 0)
					{
						absolutePath.removeLast();
					}
					else
					{
						throw new IllegalArgumentException("Invalid path " + path);
					}
				}
				else
				{
					// Add to stack
					absolutePath.add(folder);
				}
			}
	
			// Return absolute path
			return absolutePath.join("/");
		}
	}
}
