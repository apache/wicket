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


import org.apache.wicket.util.string.Strings;


/**
 * Utilities for dealing with classes.
 *
 * @author Jonathan Locke
 */
public final class Classes
{

	/**
	 * Gets the name of the given class or null if the class is null.
	 *
	 * @param c
	 *            The class
	 * @return The class name
	 */
	public static String name(Class<?> c)
	{
		String name = null;
		if (c != null)
		{
			while (c.isAnonymousClass())
			{
				c = c.getSuperclass();
			}
			name = c.getName();
		}

		return name;
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
	public static Class<?> relativeClass(final Class<?> scope, final String path)
		throws ClassNotFoundException
	{
		return Class.forName(Packages.absolutePath(scope, path).replace('/', '.'));
	}

	/**
	 * Gets the simple name (without the package) of the given class or null if the class is null.
	 *
	 * @param c
	 *            The class
	 * @return The class simple name
	 */
	public static String simpleName(Class<?> c)
	{
		String simpleName = null;
		if (c != null)
		{
			while (c.isAnonymousClass())
			{
				c = c.getSuperclass();
			}
			simpleName = c.getSimpleName();
		}

		return simpleName;
	}

	/**
	 * Instantiation not allowed
	 */
	private Classes()
	{
	}
}
