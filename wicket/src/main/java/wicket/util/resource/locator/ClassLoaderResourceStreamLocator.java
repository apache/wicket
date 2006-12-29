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
package wicket.util.resource.locator;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.util.resource.IResourceStream;
import wicket.util.resource.UrlResourceStream;

/**
 * IResourceStreamLocator implementation that locates resources using a class
 * loader.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public final class ClassLoaderResourceStreamLocator extends AbstractResourceStreamLocator
{
	/** Logging */
	private static final Logger log = LoggerFactory.getLogger(ClassLoaderResourceStreamLocator.class);

	/**
	 * Constructor
	 */
	public ClassLoaderResourceStreamLocator()
	{
	}

	/**
	 * @see wicket.util.resource.locator.AbstractResourceStreamLocator#locate(Class,
	 *      java.lang.String)
	 */
	@Override
	public IResourceStream locate(final Class clazz, final String path)
	{
		ClassLoader classLoader = null;
		if (clazz != null)
		{
			classLoader = clazz.getClassLoader();
		}

		if (classLoader == null)
		{
			// use context classloader when no specific classloader is set
			// (package resources for instance)
			classLoader = Thread.currentThread().getContextClassLoader();
		}

		if (classLoader == null)
		{
			// use Wicket classloader when no specific classloader is set
			classLoader = getClass().getClassLoader();
		}

		// Log attempt
		if (log.isDebugEnabled())
		{
			log.debug("Attempting to locate resource '" + path + "' using classloader "
					+ classLoader);
		}

		// Try loading path using classloader
		final URL url = classLoader.getResource(path);
		if (url != null)
		{
			return new UrlResourceStream(url);
		}
		return null;
	}
}
