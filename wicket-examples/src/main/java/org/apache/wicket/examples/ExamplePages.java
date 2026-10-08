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
package org.apache.wicket.examples;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import org.apache.wicket.Page;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.mapper.parameter.PageParameters;

/**
 * Finds the examples that live in a package, so that an index can list what it links to without
 * naming each of them.
 * <p>
 * An example is a public, concrete, top-level class of the wanted type - a {@link
 * WicketExamplePage} for most indexes, a {@code Wizard} for the one the wizard example keeps.
 * A page has to be bookmarkable on top of that, having a public constructor taking either nothing
 * or {@link PageParameters}, because that is what an index links to it with. Everything else in
 * the package is skipped, as is a class that cannot be loaded at all, so that an example whose
 * optional dependency is missing from the deployment does not take the index down with it.
 *
 * @see ExampleIndexPanel
 */
public final class ExamplePages
{
	private static final String CLASS_SUFFIX = ".class";

	private static final ConcurrentMap<String, List<Class<?>>> CACHE = new ConcurrentHashMap<>();

	private ExamplePages()
	{
	}

	/**
	 * Lists the examples of a package in no particular order. The result is cached, the classpath
	 * of a running application being fixed.
	 *
	 * @param packageName
	 *            the package to look in
	 * @param type
	 *            the type an example has to be of
	 * @param recursive
	 *            whether to descend into sub packages
	 * @return the examples found, never {@code null}
	 */
	public static List<Class<?>> inPackage(final String packageName, final Class<?> type,
		final boolean recursive)
	{
		return CACHE.computeIfAbsent(
			packageName + (recursive ? "/**:" : "/*:") + type.getName(),
			key -> scan(packageName, type, recursive));
	}

	private static List<Class<?>> scan(final String packageName, final Class<?> type,
		final boolean recursive)
	{
		List<Class<?>> examples = new ArrayList<>();
		for (String name : classNames(packageName, recursive))
		{
			Class<?> candidate = load(name);
			if (candidate != null && isExample(candidate, type))
			{
				examples.add(candidate);
			}
		}
		return List.copyOf(examples);
	}

	private static List<String> classNames(final String packageName, final boolean recursive)
	{
		String path = packageName.replace('.', '/');
		List<String> names = new ArrayList<>();
		try
		{
			Enumeration<URL> roots = classLoader().getResources(path);
			while (roots.hasMoreElements())
			{
				collect(roots.nextElement(), packageName, path, recursive, names);
			}
		}
		catch (IOException | URISyntaxException e)
		{
			throw new WicketRuntimeException("could not scan package " + packageName, e);
		}
		return names;
	}

	private static void collect(final URL root, final String packageName, final String path,
		final boolean recursive, final List<String> names) throws IOException, URISyntaxException
	{
		if ("file".equals(root.getProtocol()))
		{
			Path directory = Path.of(root.toURI());
			try (Stream<Path> files = Files.walk(directory, recursive ? Integer.MAX_VALUE : 1))
			{
				files.filter(Files::isRegularFile)
					.map(file -> directory.relativize(file).toString())
					.filter(file -> file.endsWith(CLASS_SUFFIX))
					.map(file -> packageName + '.' + strip(file).replace(File.separatorChar, '.'))
					.forEach(names::add);
			}
		}
		else if ("jar".equals(root.getProtocol()))
		{
			JarURLConnection connection = (JarURLConnection)root.openConnection();
			connection.setUseCaches(false);
			try (JarFile jar = connection.getJarFile())
			{
				String prefix = path + '/';
				for (Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements();)
				{
					String entry = entries.nextElement().getName();
					if (entry.startsWith(prefix) && entry.endsWith(CLASS_SUFFIX))
					{
						String relative = entry.substring(prefix.length());
						if (recursive || relative.indexOf('/') < 0)
						{
							names.add(packageName + '.' + strip(relative).replace('/', '.'));
						}
					}
				}
			}
		}
	}

	private static String strip(final String file)
	{
		return file.substring(0, file.length() - CLASS_SUFFIX.length());
	}

	private static boolean isExample(final Class<?> candidate, final Class<?> type)
	{
		int modifiers = candidate.getModifiers();
		return type.isAssignableFrom(candidate) && candidate != type &&
			candidate.getEnclosingClass() == null && Modifier.isPublic(modifiers) &&
			Modifier.isAbstract(modifiers) == false &&
			(Page.class.isAssignableFrom(candidate) == false || isBookmarkable(candidate));
	}

	private static boolean isBookmarkable(final Class<?> candidate)
	{
		for (Constructor<?> constructor : candidate.getConstructors())
		{
			Class<?>[] parameters = constructor.getParameterTypes();
			if (parameters.length == 0 ||
				(parameters.length == 1 && parameters[0] == PageParameters.class))
			{
				return true;
			}
		}
		return false;
	}

	private static Class<?> load(final String name)
	{
		try
		{
			return Class.forName(name, false, classLoader());
		}
		catch (ClassNotFoundException | LinkageError e)
		{
			return null;
		}
	}

	private static ClassLoader classLoader()
	{
		return ExamplePages.class.getClassLoader();
	}
}
