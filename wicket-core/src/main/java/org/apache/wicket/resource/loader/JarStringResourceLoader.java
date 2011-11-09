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
package org.apache.wicket.resource.loader;

import java.util.Locale;

import org.apache.wicket.Component;
import org.apache.wicket.resource.IPropertiesFactory;
import org.apache.wicket.resource.Properties;
import org.apache.wicket.util.resource.locator.ResourceNameIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is one of Wicket's default string resource loaders. It is designed to let wicket extension
 * modules contribute default resource bundles for their components.
 * <p>
 * The jar based string resource loader attempts to find the resource from a bundle that corresponds
 * to the supplied component objects jar or one of its parent's jar.
 * <p>
 * This implementation is fully aware of both locale and style values when trying to obtain the
 * appropriate resources.
 * <p>
 * Looks for a file called (by default) <code>wicket-jar.properties</code> at the root of the jar.
 * 
 * @author Bertrand Guay-Paquet
 * @author Sven Meier
 */
public class JarStringResourceLoader extends ComponentStringResourceLoader
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(JarStringResourceLoader.class);

	/** The name (without extension) of the properties file */
	private String filename = "wicket-jar";

	/**
	 * Create and initialize the resource loader.
	 */
	public JarStringResourceLoader()
	{
	}

	/**
	 * 
	 * @see org.apache.wicket.resource.loader.ComponentStringResourceLoader#loadStringResource(java.lang.Class,
	 *      java.lang.String, java.util.Locale, java.lang.String, java.lang.String)
	 */
	@Override
	public String loadStringResource(Class<?> clazz, final String key, final Locale locale,
		final String style, final String variation)
	{
		if (clazz == null)
		{
			return null;
		}

		// Load the properties associated with the path
		IPropertiesFactory propertiesFactory = getPropertiesFactory();

		// Search the component's class and its parent classes
		while (!isStopResourceSearch(clazz))
		{
			// Iterator over all the combinations
			ResourceNameIterator iter = newResourceNameIterator(filename, locale, style, variation);
			while (iter.hasNext())
			{
				String newPath = iter.next();
				Properties props = propertiesFactory.load(clazz, newPath);
				if (props != null)
				{
					// Lookup the value
					String value = props.getString(key);
					if (value != null)
					{
						return value;
					}
				}
			}

			clazz = clazz.getSuperclass();
		}

		// not found
		return null;
	}

	/**
	 * @see org.apache.wicket.resource.loader.ComponentStringResourceLoader#loadStringResource(org.apache.wicket.Component,
	 *      java.lang.String, java.util.Locale, java.lang.String, java.lang.String)
	 */
	@Override
	public String loadStringResource(final Component component, final String key,
		final Locale locale, final String style, final String variation)
	{
		if (component == null)
		{
			return null;
		}
		return loadStringResource(component.getClass(), key, locale, style, variation);
	}

	/**
	 * Gets the properties file filename (without extension)
	 * 
	 * @return filename
	 */
	public String getFilename()
	{
		return filename;
	}

	/**
	 * Sets the properties filename (without extension)
	 * 
	 * @param filename
	 *            filename
	 */
	public void setFilename(String filename)
	{
		this.filename = filename;
	}
}