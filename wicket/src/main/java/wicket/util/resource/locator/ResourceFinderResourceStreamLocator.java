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

import wicket.util.file.IResourceFinder;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.UrlResourceStream;

/**
 * IResourceStreamLocator implementation that locates resources along a
 * filesystem search path.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public class ResourceFinderResourceStreamLocator extends AbstractResourceStreamLocator
{
	/** Logging */
	private static final Logger log = LoggerFactory
			.getLogger(ResourceFinderResourceStreamLocator.class);

	/** The finder to use to locate the resource stream */
	private IResourceFinder finder;

	/**
	 * Constructor
	 * 
	 * @param finder
	 *            The path to search
	 */
	public ResourceFinderResourceStreamLocator(final IResourceFinder finder)
	{
		this.finder = finder;
	}

	/**
	 * @see wicket.util.resource.locator.AbstractResourceStreamLocator#locate(Class,
	 *      java.lang.String)
	 */
	@Override
	public IResourceStream locate(final Class clazz, final String path)
	{
		// Log attempt
		if (log.isDebugEnabled())
		{
			log.debug("Attempting to locate resource '" + path + "' on path " + finder);
		}

		// Try to find file resource on the path supplied
		final URL file = finder.find(path);

		// Found resource?
		if (file != null)
		{
			// Return file resource
			IResourceStream stream = new UrlResourceStream(file);

			if (log.isDebugEnabled())
			{
				if (stream != null)
				{
					log.error("ResourceFinder found a file (URL) but the locator failed to create the UrlResourceStream. Maybe a classloader issue?");
				}
				else
				{
					log.debug("'file' classLoader: " + file.getClass().getClassLoader().toString()
							+ "; 'stream' classLoader: "
							+ stream.getClass().getClassLoader().toString());
				}
			}

			return stream;
		}
		return null;
	}
}
