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
package org.apache.wicket.util.resource;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import org.apache.wicket.WicketTestCase;
import org.apache.wicket.core.util.resource.ClassPathResourceFinder;
import org.apache.wicket.core.util.resource.UrlResourceStream;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.core.util.resource.locator.ResourceStreamLocator;
import org.apache.wicket.util.file.IResourceFinder;
import org.apache.wicket.util.string.Strings;
import org.junit.Test;


/**
 * ResourceStreamLocator test. Tests construction of resource names with
 * 
 * @author Juergen Donnerstag
 */
public class ResourceStreamLocatorTest extends WicketTestCase
{
	private final Locale locale_de = new Locale("de");
	private final Locale locale_de_DE = new Locale("de", "DE");
	private final Locale locale_de_DE_POSIX = new Locale("de", "DE", "POSIX");
	private final Locale locale_de_POSIX = new Locale("de", "", "POSIX");
	private final Locale locale_de_CH = new Locale("de", "CH");

	private final Locale locale_en = new Locale("en");
	private final Locale locale_en_US = new Locale("en", "US");
	private final Locale locale_en_US_WIN = new Locale("en", "US", "WIN");
	private final Locale locale_en_WIN = new Locale("en", "", "WIN");

	private final Locale locale_fr = new Locale("fr");
	private final Locale locale_fr_FR = new Locale("fr", "FR");
	private final Locale locale_fr_FR_WIN = new Locale("fr", "FR", "WIN");
	private final Locale locale_fr_WIN = new Locale("fr", "", "WIN");

	/**
	 * 
	 * @param sourcePath
	 * @param style
	 * @param variation
	 * @param locale
	 * @param extension
	 */
	public void createAndTestResource(IResourceFinder sourcePath, String style, String variation,
		Locale locale, String extension)
	{
		IResourceStreamLocator locator = new ResourceStreamLocator(sourcePath);
		IResourceStream resource = locator.locate(this.getClass(), this.getClass()
			.getName()
			.replace('.', '/'), style, variation, locale, "txt", false);
		compareFilename(resource, extension);
	}

	/**
	 * 
	 * @param sourcePath
	 */
	public void executeMultiple(IResourceFinder sourcePath)
	{
		createAndTestResource(sourcePath, null, null, null, "");
		createAndTestResource(sourcePath, "style", null, null, "_style");

		createAndTestResource(sourcePath, null, null, locale_de, "_de");
		createAndTestResource(sourcePath, null, null, locale_de_DE, "_de_DE");
		createAndTestResource(sourcePath, null, null, locale_de_DE_POSIX, "_de_DE_POSIX");
		createAndTestResource(sourcePath, null, null, locale_de_POSIX, "_de__POSIX");
		createAndTestResource(sourcePath, null, null, locale_de_CH, "_de");

		createAndTestResource(sourcePath, "style", null, locale_de, "_style_de");
		createAndTestResource(sourcePath, "style", null, locale_de_DE, "_style_de_DE");
		createAndTestResource(sourcePath, "style", null, locale_de_DE_POSIX, "_style_de_DE_POSIX");
		createAndTestResource(sourcePath, "style", null, locale_de_POSIX, "_style_de__POSIX");
		createAndTestResource(sourcePath, "style", null, locale_de_CH, "_style_de");

		createAndTestResource(sourcePath, null, null, locale_en, "");
		createAndTestResource(sourcePath, null, null, locale_en_US, "");
		createAndTestResource(sourcePath, null, null, locale_en_US_WIN, "");
		createAndTestResource(sourcePath, null, null, locale_en_WIN, "");
		createAndTestResource(sourcePath, "style", null, locale_en_WIN, "_style");

		createAndTestResource(sourcePath, null, null, locale_fr, "_fr");
		createAndTestResource(sourcePath, null, null, locale_fr_FR, "_fr");
		createAndTestResource(sourcePath, null, null, locale_fr_FR_WIN, "_fr");
		createAndTestResource(sourcePath, null, null, locale_fr_WIN, "_fr");
		createAndTestResource(sourcePath, "style", null, locale_fr_WIN, "_style");
	}

	/**
	 * Test locating a resource.
	 */
	@Test
	public void locateInClasspath()
	{
		// Execute without source path
		executeMultiple(new ClassPathResourceFinder(""));

		// Determine source path
		IResourceStreamLocator locator = new ResourceStreamLocator();
		IResourceStream resource = locator.locate(getClass(),
			this.getClass().getName().replace('.', '/'), null, null, null, "txt", false);
		String path = getPath(resource);
		path = Strings.beforeLastPathComponent(path, '/') + "/sourcePath";
	}

	/**
	 * Compares the given name with the resource.
	 * 
	 * @param resource
	 * @param name
	 */
	private void compareFilename(IResourceStream resource, String name)
	{
		assertNotNull("Did not find resource: " + name, resource);

		String filename = Strings.replaceAll(this.getClass().getName(), ".", "/").toString();
		filename += name + ".txt";
		String resourcePath = getPath(resource);

		if (!resourcePath.endsWith(filename))
		{
			filename = Strings.afterLast(filename, '/');
			resourcePath = Strings.afterLast(resourcePath, '/');
			assertEquals("Did not find resource", filename, resourcePath);
		}
	}

	/**
	 * Gets the path of the resource as a string.
	 * 
	 * @param resource
	 *            the resource
	 * @return the path of the resource as a string
	 */
	public static String getPath(IResourceStream resource)
	{
		if (resource instanceof UrlResourceStream)
		{
			try
			{
				URL url = ((UrlResourceStream)resource).getURL();
				CharSequence path = new File(new URI(url.toString())).getPath();
				path = Strings.replaceAll(path, "\\", "/");
				return path.toString();
			}
			catch (URISyntaxException e)
			{
				throw new RuntimeException(e);
			}
		}
		else if (resource instanceof FileResourceStream)
		{
			try
			{
				return ((FileResourceStream)resource).getFile().getCanonicalPath();
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
		}
		else
		{
			return null;
		}
	}

	public static String getFilename(IResourceStream resource)
	{
		return Strings.afterLast(getPath(resource), '/');
	}
}
