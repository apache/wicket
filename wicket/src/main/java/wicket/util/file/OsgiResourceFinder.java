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
package wicket.util.file;

import java.net.URL;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import wicket.util.resource.IResourceStream;
import wicket.util.resource.UrlResourceStream;

/**
 * A Finder (note: it is not a Path) for working with OSGi bundles.
 * 
 * @author Timur Mehrvarz
 */
public final class OsgiResourceFinder implements IResourceFinder
{
	private final Logger log = LoggerFactory.getLogger(OsgiResourceFinder.class);

	/** ClassLoader to be used for locating resources. */
	final ClassLoader classLoader;

	/**
	 * Constructor.
	 * 
	 * @param classLoader
	 *            class loader to be used for locating resources
	 */
	public OsgiResourceFinder(ClassLoader classLoader)
	{
		this.classLoader = classLoader;
	}

	/**
	 * 
	 * @see wicket.util.file.IResourceFinder#find(Class, String)
	 */
	public IResourceStream find(final Class clazz, final String pathname)
	{
		String resourcePathName = /* "/" + */pathname;
		if (log.isDebugEnabled())
		{
			log.debug("classLoader: " + classLoader.toString());
		}
		URL url = classLoader.getResource(resourcePathName);
		if (url != null)
		{
			IResourceStream stream = new UrlResourceStream(url);
			if (stream == null)
			{
				log.error("ClassLoader problem: found the URL (" + url.toExternalForm()
						+ ") but was unable to load the resource stream");
			}
			return stream;
		}
		return null;
	}
}
