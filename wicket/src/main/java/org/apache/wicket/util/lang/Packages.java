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
package org.apache.wicket.util.lang;

import org.apache.wicket.util.string.StringList;

/**
 * Utilities for dealing with packages.
 * 
 * @author Jonathan Locke
 * @author Niclas Hedhman
 */
public final class Packages
{
	/**
	 * Takes a package and a relative path to a resource and returns an absolute path to the
	 * resource. For example, if the given package was java.lang and the relative path was
	 * "../util/List", then "java/util/List" would be returned.
	 * 
	 * @param p
	 *            The package to start at
	 * @param relativePath
	 *            The relative path to the class
	 * @return The absolute path
	 */
	public static String absolutePath(final Class<?> p, final String relativePath)
	{
		String packName = (p != null ? extractPackageName(p) : "");
		return absolutePath(packName, relativePath);
	}

	/**
	 * Takes a package and a relative path to a resource and returns an absolute path to the
	 * resource. For example, if the given package was java.lang and the relative path was
	 * "../util/List", then "java/util/List" would be returned.
	 * 
	 * @param p
	 *            The package to start at
	 * @param relativePath
	 *            The relative path to the class
	 * @return The absolute path
	 */
	public static String absolutePath(final Package p, final String relativePath)
	{
		return absolutePath(p.getName(), relativePath);
	}

	/**
	 * Takes a package and a relative path to a resource and returns an absolute path to the
	 * resource. For example, if the given package was java.lang and the relative path was
	 * "../util/List", then "java/util/List" would be returned.
	 * 
	 * @param packageName
	 *            The package to start at
	 * @param relativePath
	 *            The relative path to the class
	 * @return The absolute path
	 */
	public static String absolutePath(final String packageName, final String relativePath)
	{
		// Is path already absolute?
		if (relativePath.startsWith("/"))
		{
			return relativePath;
		}
		else
		{
			// Break package into list of package names
			final StringList absolutePath = StringList.tokenize(packageName, ".");

			// Break path into folders
			final StringList folders = StringList.tokenize(relativePath, "/\\");

			// Iterate through folders
			for (int i = 0, size = folders.size(); i < size; i++)
			{
				// Get next folder
				final String folder = folders.get(i);

				// Up one?
				if ("..".equals(folder))
				{
					// Pop off stack
					if (absolutePath.size() > 0)
					{
						absolutePath.removeLast();
					}
					else
					{
						throw new IllegalArgumentException("Invalid path " + relativePath);
					}
				}
				else if (absolutePath.size() <= i || absolutePath.get(i).equals(folder) == false)
				{
					// Add to stack
					absolutePath.add(folder);
				}
			}

			// Return absolute path
			return absolutePath.join("/");
		}
	}

	/**
	 * Determines the package name for the given class.
	 * 
	 * @param forClass
	 *            the class
	 * @return the package name
	 */
	public static String extractPackageName(final Class<?> forClass)
	{
		String classname = forClass.getName();
		String parent = parent(classname);
		return parent;
	}

	/**
	 * Gets the parent package name.
	 * 
	 * @param packageName
	 *            The Package name
	 * @return The parent Package
	 */
	public static String parent(final String packageName)
	{
		int pos = packageName.lastIndexOf(".");
		String parent;
		if (pos < 0)
		{
			pos = packageName.lastIndexOf("/");
			if (pos < 0)
			{
				pos = 0;
			}
		}
		parent = packageName.substring(0, pos);
		return parent;
	}


	/**
	 * Resolve scope for the given class by extracting it's package name and converting all dots to
	 * slashes.
	 * 
	 * @param forClass
	 *            the class
	 * @return the scope string
	 */
	public static String resolveScope(final Class<?> forClass)
	{
		String packName = extractPackageName(forClass);
		return packName.replace('.', '/');
	}

	/**
	 * Instantiation not allowed.
	 */
	private Packages()
	{
	}
}
