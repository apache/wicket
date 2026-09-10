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
package org.apache.wicket.markup.html;

import java.io.File;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.wicket.Application;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of {@link IPackageResourceGuard}. By default, the extensions 'properties',
 * 'class' and 'java' are blocked and also files like 'log4j.xml' and 'applicationContext.xml'
 * 
 * A more secure implementation which by default denies access to any resource is
 * {@link SecurePackageResourceGuard}
 * 
 * @author eelcohillenius
 */
public class PackageResourceGuard implements IPackageResourceGuard
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(PackageResourceGuard.class);

	/** Set of extensions that are denied access. */
	private Set<String> blockedExtensions = new HashSet<>(4);

	/** Set of filenames that are denied access. */
	private Set<String> blockedFiles = new HashSet<>(4);

	private boolean allowAccessToRootResources = false;

	/**
	 * Construct.
	 */
	public PackageResourceGuard()
	{
		blockedExtensions.add("properties");
		blockedExtensions.add("class");
		blockedExtensions.add("java");

		blockedFiles.add("applicationContext.xml");
		blockedFiles.add("log4j.xml");
	}

	/**
	 * @see org.apache.wicket.markup.html.IPackageResourceGuard#accept(java.lang.String)
	 */
	public boolean accept(String path)
	{
		int ixExtension = path.lastIndexOf('.');
		int len = path.length();
		final String ext;
		if (ixExtension <= 0 || ixExtension == len ||
			(path.lastIndexOf('/') + 1) == ixExtension ||
			(path.lastIndexOf('\\') + 1) == ixExtension)
		{
			ext = null;
		}
		else
		{
			ext = path.substring(ixExtension + 1).toLowerCase(Locale.ROOT).trim();
		}

		if ("html".equals(ext))
		{
			String prefix = path.substring(0, ixExtension);

			while (true)
			{
				if (classExists(prefix))
				{
					log.warn("Access denied to shared (static) resource because it is a Wicket markup file: " +
						path);
					return false;
				}

				int ixUnderscore = prefix.lastIndexOf('_');
				if (ixUnderscore == -1)
				{
					break;
				}

				prefix = prefix.substring(0, ixUnderscore);
			}
		}

		if (acceptExtension(ext) == false)
		{
			log.warn("Access denied to shared (static) resource because of the file extension: " +
				path);
			return false;
		}

		String filename = Strings.lastPathComponent(path, File.separatorChar);
		if (acceptFile(filename) == false)
		{
			log.warn("Access denied to shared (static) resource because of the file name: " + path);
			return false;
		}

		// Only if a placeholder, e.g. $up$ is defined, access to parent directories is allowed
		if (Strings.isEmpty(Application.get().getResourceSettings().getParentFolderPlaceholder()))
		{
			if (path.contains(".."))
			{
				log.warn("Access to parent directories via '..' is by default disabled for shared resources: " +
					path);
				return false;
			}
		}

		//
		// for windows we have to check both File.separator ('\') and the usual '/' since both can
		// be used and are used interchangeably
		//

		if (!allowAccessToRootResources)
		{
			String absolute = path;
			if ("\\".equals(File.separator))
			{
				// handle a windows path which may have a drive letter in it

				int drive = absolute.indexOf(":\\");
				if (drive < 0)
				{
					drive = absolute.indexOf(":/");
				}
				if (drive > 0)
				{
					// strip the drive letter off the path
					absolute = absolute.substring(drive + 2);
				}
			}

			if (absolute.startsWith(File.separator) || absolute.startsWith("/"))
			{
				absolute = absolute.substring(1);
			}
			if (!absolute.contains(File.separator) && !absolute.contains("/"))
			{
				log.warn("Access to root directory is by default disabled for shared resources: " +
					path);
				return false;
			}
		}

		return true;
	}

	/**
	 * Whether the provided extension is accepted.
	 * 
	 * @param extension
	 *            The extension, starting from the class root (packages are separated with forward
	 *            slashes instead of dots).
	 * @return True if accepted, false otherwise.
	 */
	protected boolean acceptExtension(String extension)
	{
		return (!blockedExtensions.contains(extension));
	}

	/**
	 * Whether the provided filename is accepted.
	 * 
	 * @param file
	 *            filename
	 * @return True if accepted, false otherwise.
	 */
	protected boolean acceptFile(String file)
	{
		if (file != null)
		{
			file = file.trim();
		}
		return (!blockedFiles.contains(file));
	}

	/**
	 * Whether a class file exists for the given path, which is what makes an html resource with
	 * that path the markup template of a component rather than a static html file that may be
	 * served.
	 * <p>
	 * The class file is looked for on the class loader that loaded Wicket itself, and on the class
	 * loaders the application's {@link org.apache.wicket.application.IClassResolver} searches: the
	 * one the application itself was loaded by, and the current thread's context class loader.
	 * Wicket is not always loaded by a class loader that can see the application's classes. It can
	 * sit in a container's shared library folder, in the library folder of an ear with the
	 * application in the war, in an application server's global module, or in an osgi bundle that
	 * cannot resolve a class an application bundle holds, while the markup is located by
	 * {@link org.apache.wicket.core.util.resource.ClassPathResourceFinder}, which searches the
	 * class loader of the scope class and the context class loader as well. Asking Wicket's own
	 * class loader alone lets a component's markup template be served as a static resource in
	 * exactly those deployments: a class that loader cannot see is still a class the application
	 * has.
	 * 
	 * @param path
	 *            The path of the resource without its extension, starting from the class root
	 *            (packages are separated with forward slashes instead of dots).
	 * @return True if a class file exists for the path, false otherwise.
	 */
	protected boolean classExists(String path)
	{
		String name = path + ".class";

		// the class loader that holds Wicket itself, the one this check has always asked
		ClassLoader ownClassLoader = getClass().getClassLoader();
		if (ownClassLoader != null && ownClassLoader.getResource(name) != null)
		{
			return true;
		}

		// and the class loaders that can see the application's classes, which the one above need
		// not be able to
		return Application.exists() && Application.get()
			.getApplicationSettings()
			.getClassResolver()
			.getResources(name)
			.hasNext();
	}

	/**
	 * Gets the set of extensions that are denied access.
	 * 
	 * @return The set of extensions that are denied access
	 */
	protected final Set<String> getBlockedExtensions()
	{
		return blockedExtensions;
	}

	/**
	 * Gets the set of extensions that are denied access.
	 * 
	 * @return The set of extensions that are denied access
	 */
	protected final Set<String> getBlockedFiles()
	{
		return blockedFiles;
	}

	/**
	 * Sets the set of extensions that are denied access.
	 * 
	 * @param blockedExtensions
	 *            Set of extensions that are denied access
	 */
	protected final void setBlockedExtensions(Set<String> blockedExtensions)
	{
		this.blockedExtensions = blockedExtensions;
	}

	/**
	 * Sets the set of filenames that are denied access.
	 * 
	 * @param blockedFiles
	 *            Set of extensions that are denied access
	 */
	protected final void setBlockedFiles(Set<String> blockedFiles)
	{
		this.blockedFiles = blockedFiles;
	}

	/**
	 * Checks whether or not resources in the web root folder can be access.
	 * 
	 * @return {@code true} iff root resources can be accessed
	 */
	public final boolean isAllowAccessToRootResources()
	{
		return allowAccessToRootResources;
	}

	/**
	 * Sets whether or not resources in the web root folder can be accessed.
	 * 
	 * @param allowAccessToRootResources
	 */
	public final void setAllowAccessToRootResources(boolean allowAccessToRootResources)
	{
		this.allowAccessToRootResources = allowAccessToRootResources;
	}
}
