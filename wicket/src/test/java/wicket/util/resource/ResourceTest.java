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
package wicket.util.resource;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Locale;

import junit.framework.TestCase;
import wicket.util.file.Folder;
import wicket.util.file.Path;
import wicket.util.resource.locator.IResourceStreamFactory;
import wicket.util.resource.locator.ResourceStreamFactory;
import wicket.util.string.Strings;


/**
 * Resources test.
 * 
 * @author Juergen Donnerstag
 */
public class ResourceTest extends TestCase
{
	private Locale locale_de = new Locale("de");
	private Locale locale_de_DE = new Locale("de", "DE");
	private Locale locale_de_DE_POSIX = new Locale("de", "DE", "POSIX");
	private Locale locale_de_POSIX = new Locale("de", "", "POSIX");
	private Locale locale_de_CH = new Locale("de", "CH");

	private Locale locale_en = new Locale("en");
	private Locale locale_en_US = new Locale("en", "US");
	private Locale locale_en_US_WIN = new Locale("en", "US", "WIN");
	private Locale locale_en_WIN = new Locale("en", "", "WIN");

	private Locale locale_fr = new Locale("fr");
	private Locale locale_fr_FR = new Locale("fr", "FR");
	private Locale locale_fr_FR_WIN = new Locale("fr", "FR", "WIN");
	private Locale locale_fr_WIN = new Locale("fr", "", "WIN");

	/**
	 * 
	 * @param sourcePath
	 * @param style
	 * @param locale
	 * @param extension
	 */
	public void createAndTestResource(Path sourcePath, String style, Locale locale, String extension)
	{
		IResourceStreamFactory locator = new ResourceStreamFactory(sourcePath);
		IResourceStream resource = locator.locate(this.getClass(), this.getClass().getName()
				.replace('.', '/'), style, locale, "txt");
		compareFilename(resource, extension);
	}

	/**
	 * 
	 * @param sourcePath
	 */
	public void executeMultiple(Path sourcePath)
	{
		createAndTestResource(sourcePath, null, null, "");
		createAndTestResource(sourcePath, "style", null, "_style");

		createAndTestResource(sourcePath, null, locale_de, "_de");
		createAndTestResource(sourcePath, null, locale_de_DE, "_de_DE");
		createAndTestResource(sourcePath, null, locale_de_DE_POSIX, "_de_DE_POSIX");
		createAndTestResource(sourcePath, null, locale_de_POSIX, "_de__POSIX");
		createAndTestResource(sourcePath, null, locale_de_CH, "_de");

		createAndTestResource(sourcePath, "style", locale_de, "_style_de");
		createAndTestResource(sourcePath, "style", locale_de_DE, "_style_de_DE");
		createAndTestResource(sourcePath, "style", locale_de_DE_POSIX, "_style_de_DE_POSIX");
		createAndTestResource(sourcePath, "style", locale_de_POSIX, "_style_de__POSIX");
		createAndTestResource(sourcePath, "style", locale_de_CH, "_style_de");

		createAndTestResource(sourcePath, null, locale_en, "");
		createAndTestResource(sourcePath, null, locale_en_US, "");
		createAndTestResource(sourcePath, null, locale_en_US_WIN, "");
		createAndTestResource(sourcePath, null, locale_en_WIN, "");
		createAndTestResource(sourcePath, "style", locale_en_WIN, "_style");

		createAndTestResource(sourcePath, null, locale_fr, "_fr");
		createAndTestResource(sourcePath, null, locale_fr_FR, "_fr");
		createAndTestResource(sourcePath, null, locale_fr_FR_WIN, "_fr");
		createAndTestResource(sourcePath, null, locale_fr_WIN, "_fr");
		createAndTestResource(sourcePath, "style", locale_fr_WIN, "_style");
	}

	/**
	 * Test locating a resource.
	 */
	public void testLocate()
	{
		// Execute without source path
		executeMultiple(new Path());

		// Determine source path
		IResourceStreamFactory locator = new ResourceStreamFactory();
		IResourceStream resource = locator.locate(getClass(), this.getClass().getName().replace(
				'.', '/'), null, null, "txt");
		String path = getPath(resource);
		path = Strings.beforeLastPathComponent(path, '/') + "/sourcePath";

		// and execute
		executeMultiple(new Path(new Folder(path)));
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
	private String getPath(IResourceStream resource)
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
}
