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


import org.apache.wicket.Application;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Utilities for dealing with classes.
 * 
 * @author Jonathan Locke
 */
public final class Classes
{
	private static final Logger log = LoggerFactory.getLogger(Classes.class);

	/**
	 * Gets the name of the given class or null if the class is null.
	 * 
	 * @param c
	 *            The class
	 * @return The class name
	 */
	public static String name(final Class< ? > c)
	{
		return (c != null) ? c.getName() : null;
	}

	/**
	 * Takes a Class and a relative path to a class and returns any class at that relative path. For
	 * example, if the given Class was java.lang.System and the relative path was "../util/List",
	 * then the java.util.List class would be returned.
	 * 
	 * @param scope
	 *            The package to start at
	 * @param path
	 *            The relative path to the class
	 * @return The class
	 * @throws ClassNotFoundException
	 */
	public static Class< ? > relativeClass(final Class< ? > scope, final String path)
		throws ClassNotFoundException
	{
		return Class.forName(Packages.absolutePath(scope, path).replace('/', '.'));
	}

	/**
	 * @param <T>
	 *            class type
	 * @param className
	 *            Class to resolve
	 * @return Resolved class
	 */
	@SuppressWarnings("unchecked")
	public static <T> Class<T> resolveClass(final String className)
	{
		if (className == null)
		{
			return null;
		}
		try
		{
			if (Application.exists())
			{
				return (Class<T>)Application.get()
					.getApplicationSettings()
					.getClassResolver()
					.resolveClass(className);
			}
			return (Class<T>)Class.forName(className);
		}
		catch (ClassNotFoundException e)
		{
			log.warn("Could not resolve class: " + className);
			return null;
		}
	}

	/**
	 * Gets the name of a given class without the prefixed package path
	 * 
	 * @param c
	 *            The class
	 * @return The class name
	 */
	public static String simpleName(final Class< ? > c)
	{
		return Strings.lastPathComponent(c.getName(), '.');
	}

	/**
	 * Instantiation not allowed
	 */
	private Classes()
	{
	}
}
