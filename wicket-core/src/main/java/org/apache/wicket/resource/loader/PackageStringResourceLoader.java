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

import org.apache.wicket.Application;
import org.apache.wicket.core.util.resource.locator.IResourceNameIterator;
import org.apache.wicket.resource.IPropertiesFactory;
import org.apache.wicket.resource.Properties;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This is one of Wicket's default string resource loaders.
 * <p>
 * The package based string resource loader attempts to find the resource from a bundle that
 * corresponds to the supplied component objects package or one of its parent packages.
 * <p>
 * The search order for resources is component object package towards root package.
 * <p>
 * This implementation is fully aware of both locale and style values when trying to obtain the
 * appropriate resources.
 * <p>
 * Looks for file called <code>package.properties</code>
 * 
 * @author Juergen Donnerstag
 * @author igor.vaynberg
 */
public class PackageStringResourceLoader extends ComponentStringResourceLoader
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(PackageStringResourceLoader.class);

	/** The name (without extension) of the properties file */
	private String filename = "wicket-package";

	/**
	 * Create and initialize the resource loader.
	 */
	public PackageStringResourceLoader()
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


		while (true)
		{
			Package pkg = clazz.getPackage();
			String packageName = (pkg == null) ? "" : pkg.getName();
			packageName = packageName.replace('.', '/');

			do
			{
				// Create the base path
				String path = filename;
				if (packageName.length() > 0)
				{
					path = packageName + "/" + path;
				}

				// Iterator over all the combinations
				IResourceNameIterator iter = newResourceNameIterator(path, locale, style, variation);
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

				// Didn't find the key yet, continue searching if possible
				packageName = Strings.beforeLast(packageName, '/');
			}
			while (packageName.length() > 0);

			clazz = clazz.getSuperclass();
			if (clazz == null)
			{
				break;
			}
		}
		// not found
		return null;
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
