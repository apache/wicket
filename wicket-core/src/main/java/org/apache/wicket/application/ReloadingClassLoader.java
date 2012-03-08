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
package org.apache.wicket.application;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.wicket.util.collections.UrlExternalFormComparator;
import org.apache.wicket.util.file.File;
import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.time.Duration;
import org.apache.wicket.util.watch.IModificationWatcher;
import org.apache.wicket.util.watch.ModificationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Custom ClassLoader that reverses the classloader lookups, and that is able to notify a listener
 * when a class file is changed.
 * 
 * @author <a href="mailto:jbq@apache.org">Jean-Baptiste Quenot</a>
 */
public class ReloadingClassLoader extends URLClassLoader
{
	private static final Logger log = LoggerFactory.getLogger(ReloadingClassLoader.class);

	private static final Set<URL> urls = new TreeSet<URL>(new UrlExternalFormComparator());

	private static final List<String> patterns = new ArrayList<String>();

	private IChangeListener listener;

	private final Duration pollFrequency = Duration.seconds(3);

	private final IModificationWatcher watcher;

	static
	{
		addClassLoaderUrls(ReloadingClassLoader.class.getClassLoader());
		excludePattern("org.apache.wicket.*");
		includePattern("org.apache.wicket.examples.*");
	}

	/**
	 * 
	 * @param name
	 * @return true if class if found, false otherwise
	 */
	protected boolean tryClassHere(String name)
	{
		// don't include classes in the java or javax.servlet package
		if (name != null && (name.startsWith("java.") || name.startsWith("javax.servlet")))
		{
			return false;
		}
		// Scan includes, then excludes
		boolean tryHere;

		// If no explicit includes, try here
		if (patterns == null || patterns.size() == 0)
		{
			tryHere = true;
		}
		else
		{
			// See if it matches include patterns
			tryHere = false;
			for (String rawpattern : patterns)
			{
				if (rawpattern.length() <= 1)
				{
					continue;
				}
				// FIXME it seems that only "includes" are handled. "Excludes" are ignored
				boolean isInclude = rawpattern.substring(0, 1).equals("+");
				String pattern = rawpattern.substring(1);
				if (WildcardMatcherHelper.match(pattern, name) != null)
				{
					tryHere = isInclude;
				}
			}
		}

		return tryHere;
	}

	/**
	 * Include a pattern
	 * 
	 * @param pattern
	 *            the pattern to include
	 */
	public static void includePattern(String pattern)
	{
		patterns.add("+" + pattern);
	}

	/**
	 * Exclude a pattern
	 * 
	 * @param pattern
	 *            the pattern to exclude
	 */
	public static void excludePattern(String pattern)
	{
		patterns.add("-" + pattern);
	}

	/**
	 * Returns the list of all configured inclusion or exclusion patterns
	 * 
	 * @return list of patterns as String
	 */
	public static List<String> getPatterns()
	{
		return patterns;
	}

	/**
	 * Add the location of a directory containing class files
	 * 
	 * @param url
	 *            the URL for the directory
	 */
	public static void addLocation(URL url)
	{
		urls.add(url);
	}

	/**
	 * Returns the list of all configured locations of directories containing class files
	 * 
	 * @return list of locations as URL
	 */
	public static Set<URL> getLocations()
	{
		return urls;
	}

	/**
	 * Add all the url locations we can find for the provided class loader
	 * 
	 * @param loader
	 *            class loader
	 */
	private static void addClassLoaderUrls(ClassLoader loader)
	{
		if (loader != null)
		{
			final Enumeration<URL> resources;
			try
			{
				resources = loader.getResources("");
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			while (resources.hasMoreElements())
			{
				URL location = resources.nextElement();
				ReloadingClassLoader.addLocation(location);
			}
		}
	}

	/**
	 * Create a new reloading ClassLoader from a list of URLs, and initialize the
	 * ModificationWatcher to detect class file modifications
	 * 
	 * @param parent
	 *            the parent classloader in case the class file cannot be loaded from the above
	 *            locations
	 */
	public ReloadingClassLoader(ClassLoader parent)
	{
		super(new URL[] { }, parent);
		// probably doubles from this class, but just in case
		addClassLoaderUrls(parent);

		for (URL url : urls)
		{
			addURL(url);
		}
		watcher = new ModificationWatcher(pollFrequency);
	}

	/**
	 * Gets a resource from this <code>ClassLoader</class>.  If the
	 * resource does not exist in this one, we check the parent.
	 * Please note that this is the exact opposite of the
	 * <code>ClassLoader</code> spec. We use it to work around inconsistent class loaders from third
	 * party vendors.
	 * 
	 * @param name
	 *            of resource
	 */
	@Override
	public final URL getResource(final String name)
	{
		URL resource = findResource(name);
		ClassLoader parent = getParent();
		if (resource == null && parent != null)
		{
			resource = parent.getResource(name);
		}

		return resource;
	}

	/**
	 * Loads the class from this <code>ClassLoader</class>.  If the
	 * class does not exist in this one, we check the parent.  Please
	 * note that this is the exact opposite of the
	 * <code>ClassLoader</code> spec. We use it to load the class from the same classloader as
	 * WicketFilter or WicketServlet. When found, the class file is watched for modifications.
	 * 
	 * @param name
	 *            the name of the class
	 * @param resolve
	 *            if <code>true</code> then resolve the class
	 * @return the resulting <code>Class</code> object
	 * @exception ClassNotFoundException
	 *                if the class could not be found
	 */
	@Override
	public final Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException
	{
		// First check if it's already loaded
		Class<?> clazz = findLoadedClass(name);

		if (clazz == null)
		{
			final ClassLoader parent = getParent();

			if (tryClassHere(name))
			{
				try
				{
					clazz = findClass(name);
					watchForModifications(clazz);
				}
				catch (ClassNotFoundException cnfe)
				{
					if (parent == null)
					{
						// Propagate exception
						throw cnfe;
					}
				}
			}

			if (clazz == null)
			{
				if (parent == null)
				{
					throw new ClassNotFoundException(name);
				}
				else
				{
					// Will throw a CFNE if not found in parent
					// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6500212
					// clazz = parent.loadClass(name);
					clazz = Class.forName(name, false, parent);
				}
			}
		}

		if (resolve)
		{
			resolveClass(clazz);
		}

		return clazz;
	}

	/**
	 * Sets the listener that will be notified when a class changes
	 * 
	 * @param listener
	 *            the listener to notify upon class change
	 */
	public void setListener(IChangeListener listener)
	{
		this.listener = listener;
	}

	/**
	 * Watch changes of a class file by locating it in the list of location URLs and adding the
	 * corresponding file to the ModificationWatcher
	 * 
	 * @param clz
	 *            the class to watch
	 */
	private void watchForModifications(Class<?> clz)
	{
		// Watch class in the future
		Iterator<URL> locationsIterator = urls.iterator();
		File clzFile = null;
		while (locationsIterator.hasNext())
		{
			// FIXME only works for directories, but JARs etc could be checked
			// as well
			URL location = locationsIterator.next();
			String clzLocation = location.getFile() + clz.getName().replaceAll("\\.", "/") +
				".class";
			log.debug("clzLocation=" + clzLocation);
			clzFile = new File(clzLocation);
			final File finalClzFile = clzFile;
			if (clzFile.exists())
			{
				log.info("Watching changes of class " + clzFile);
				watcher.add(clzFile, new IChangeListener()
				{
					@Override
					public void onChange()
					{
						log.info("Class file " + finalClzFile + " has changed, reloading");
						try
						{
							listener.onChange();
						}
						catch (Exception e)
						{
							log.error("Could not notify listener", e);
							// If an error occurs when the listener is notified,
							// remove the watched object to avoid rethrowing the
							// exception at next check
							// FIXME check if class file has been deleted
							watcher.remove(finalClzFile);
						}
					}
				});
				break;
			}
			else
			{
				log.debug("Class file does not exist: " + clzFile);
			}
		}
		if (clzFile != null && !clzFile.exists())
		{
			log.debug("Could not locate class " + clz.getName());
		}
	}

	/**
	 * Remove the ModificationWatcher from the current reloading class loader
	 */
	public void destroy()
	{
		watcher.destroy();
	}
}
