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
import java.lang.ref.WeakReference;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.Application;
import org.apache.wicket.WicketRuntimeException;

/**
 * Resolves a class by using the classloader that loaded this class.
 * 
 * @see org.apache.wicket.settings.IApplicationSettings#getClassResolver()
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class DefaultClassResolver implements IClassResolver
{
	/**
	 * Usually class loaders implement more efficient caching strategies than we could possibly do,
	 * but we experienced synchronization issue resulting in stack traces like:
	 * java.lang.LinkageError: duplicate class definition:
	 * 
	 * <pre>
	 *    wicket/examples/repeater/RepeatingPage at java.lang.ClassLoader.defineClass1(Native Method) 
	 * </pre>
	 * 
	 * This problem has gone since we synchronize the access.
	 */
	private final ConcurrentHashMap<String, WeakReference<Class>> classes = new ConcurrentHashMap<String, WeakReference<Class>>();

	/**
	 * @see org.apache.wicket.application.IClassResolver#resolveClass(java.lang.String)
	 */
	public final Class resolveClass(final String classname) throws ClassNotFoundException
	{
		Class clazz = null;
		WeakReference ref = classes.get(classname);

		// Might be garbage-collected between getting the WeakRef and retrieving
		// the Class from it.
		if (ref != null)
		{
			clazz = (Class)ref.get();
		}
		if (clazz == null)
		{
			if (classname.equals("byte"))
			{
				clazz = byte.class;
			}
			else if (classname.equals("short"))
			{
				clazz = short.class;
			}
			else if (classname.equals("int"))
			{
				clazz = int.class;
			}
			else if (classname.equals("long"))
			{
				clazz = long.class;
			}
			else if (classname.equals("float"))
			{
				clazz = float.class;
			}
			else if (classname.equals("double"))
			{
				clazz = double.class;
			}
			else if (classname.equals("boolean"))
			{
				clazz = boolean.class;
			}
			else if (classname.equals("char"))
			{
				clazz = char.class;
			}
			else
			{
				synchronized (classes)
				{
					ClassLoader loader = Thread.currentThread().getContextClassLoader();
					if (loader == null)
					{
						loader = DefaultClassResolver.class.getClassLoader();
					}
					clazz = loader.loadClass(classname);
				}
				classes.put(classname, new WeakReference<Class>(clazz));
			}
		}
		return clazz;
	}

	public Iterator<URL> getResources(String name)
	{
		HashSet<URL> loadedFiles = new HashSet<URL>();
		try
		{
			// Try the classloader for the wicket jar/bundle
			Enumeration resources = Application.class.getClassLoader().getResources(
				"wicket.properties");
			loadResources(resources, loadedFiles);

			// Try the classloader for the user's application jar/bundle
			resources = Application.get().getClass().getClassLoader().getResources(
				"wicket.properties");
			loadResources(resources, loadedFiles);

			// Try the context class loader
			resources = Thread.currentThread().getContextClassLoader().getResources(
				"wicket.properties");
			loadResources(resources, loadedFiles);
		}
		catch (IOException e)
		{
			throw new WicketRuntimeException(e);
		}

		return loadedFiles.iterator();
	}

	private void loadResources(Enumeration resources, Set<URL> loadedFiles)
	{
		if (resources != null)
		{
			while (resources.hasMoreElements())
			{
				final URL url = (URL)resources.nextElement();
				if (!loadedFiles.contains(url))
				{
					loadedFiles.add(url);
				}
			}
		}
	}

}
