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
package org.apache.wicket.resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.wicket.Application;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.concurrent.ConcurrentHashMap;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.resource.IFixedLocationResourceStream;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.util.watch.ModificationWatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Default implementation of {@link IPropertiesFactory} which uses the
 * {@link IResourceStreamLocator} as defined by {@link IResourceSettings#getResourceStreamLocator()}
 * to load the {@link Properties} objects. Depending on the settings, it will assign
 * {@link ModificationWatcher}s to the loaded resources to support reloading.
 * 
 * @see org.apache.wicket.settings.IResourceSettings#getPropertiesFactory()
 * 
 * @author Juergen Donnerstag
 */
public class PropertiesFactory implements IPropertiesFactory
{
	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(PropertiesFactory.class);

	/**
	 * Listeners will be invoked after changes to property file have been detected
	 */
	private final List afterReloadListeners = new ArrayList();

	/** Cache for all property files loaded */
	private final Map propertiesCache = new ConcurrentHashMap();

	/** Application */
	private final Application application;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            Application for this properties factory.
	 */
	public PropertiesFactory(Application application)
	{
		this.application = application;
	}

	/**
	 * @see org.apache.wicket.resource.IPropertiesFactory#addListener(org.apache.wicket.resource.IPropertiesChangeListener)
	 */
	public void addListener(final IPropertiesChangeListener listener)
	{
		// Make sure listeners are added only once
		if (afterReloadListeners.contains(listener) == false)
		{
			afterReloadListeners.add(listener);
		}
	}

	/**
	 * @see org.apache.wicket.resource.IPropertiesFactory#clearCache()
	 */
	public final void clearCache()
	{
		propertiesCache.clear();
	}

	/**
	 * 
	 * @see org.apache.wicket.resource.IPropertiesFactory#load(java.lang.Class, java.lang.String)
	 */
	public Properties load(final Class clazz, final String path)
	{
		// Check the cache
		Properties properties = (Properties)propertiesCache.get(path);
		if (properties != null)
		{
			// Return null, if no resource stream was found
			if (properties == Properties.EMPTY_PROPERTIES)
			{
				properties = null;
			}
			return properties;
		}

		// If not in the cache than try to load the resource stream
		IResourceStream stream = application.getResourceSettings().getResourceStreamLocator()
				.locate(clazz, path);
		if (stream != null)
		{
			// Load the properties from the stream
			properties = loadPropertiesFileAndWatchForChanges(path, stream);
			if (properties != null)
			{
				propertiesCache.put(path, properties);
				return properties;
			}
		}

		// Add a placeholder to the cache. Null is not a valid value to add.
		propertiesCache.put(path, Properties.EMPTY_PROPERTIES);
		return null;
	}

	/**
	 * Helper method to do the actual loading of resources if required.
	 * 
	 * @param key
	 *            The key for the resource
	 * @param resourceStream
	 *            The properties file stream to load and begin to watch
	 * @return The map of loaded resources
	 */
	private synchronized Properties loadPropertiesFile(final String key,
			final IResourceStream resourceStream)
	{
		// Make sure someone else didn't load our resources while we were
		// waiting for the synchronized lock on the method
		Properties props = (Properties)propertiesCache.get(key);
		if (props != null)
		{
			return props;
		}

		if (resourceStream == null)
		{
			props = new Properties(key, ValueMap.EMPTY_MAP);
		}
		else
		{
			ValueMap strings = null;

			try
			{
				try
				{
					// Get the InputStream
					BufferedInputStream in = new BufferedInputStream(resourceStream
							.getInputStream());

					// Determine if resource is a XML File
					boolean loadAsXml = false;
					if (resourceStream instanceof IFixedLocationResourceStream)
					{
						String location = ((IFixedLocationResourceStream)resourceStream)
								.locationAsString();
						if (location != null)
						{
							String ext = Strings.lastPathComponent(location, '.').toLowerCase();
							if ("xml".equals(ext))
							{
								loadAsXml = true;
							}
						}
					}

					// Load the properties
					java.util.Properties properties = new java.util.Properties();
					if (loadAsXml)
					{
						Streams.loadFromXml(properties, in);
					}
					else
					{
						properties.load(in);
					}

					// Copy the properties into the ValueMap
					strings = new ValueMap();
					Enumeration enumeration = properties.propertyNames();
					while (enumeration.hasMoreElements())
					{
						String property = (String)enumeration.nextElement();
						strings.put(property, properties.getProperty(property));
					}
				}
				finally
				{
					resourceStream.close();
				}
			}
			catch (ResourceStreamNotFoundException e)
			{
				log.warn("Unable to find resource " + resourceStream, e);
				strings = ValueMap.EMPTY_MAP;
			}
			catch (IOException e)
			{
				log.warn("Unable to access resource " + resourceStream, e);
				strings = ValueMap.EMPTY_MAP;
			}

			props = new Properties(key, strings);
		}

		return props;
	}

	/**
	 * Load properties file from an IResourceStream and add an {@link IChangeListener}to the
	 * {@link ModificationWatcher} so that if the resource changes, we can reload it automatically.
	 * 
	 * @param key
	 *            The key for the resource
	 * @param resourceStream
	 *            The properties file stream to load and begin to watch
	 * @return The map of loaded resources
	 */
	private final Properties loadPropertiesFileAndWatchForChanges(final String key,
			final IResourceStream resourceStream)
	{
		// Watch file modifications
		final ModificationWatcher watcher = application.getResourceSettings().getResourceWatcher(
				true);
		if (watcher != null)
		{
			watcher.add(resourceStream, new IChangeListener()
			{
				public void onChange()
				{
					log.info("A properties files has changed. Removing all entries " +
							"from the cache. Resource: " + resourceStream);

					// Clear the whole cache as associated localized files may
					// be affected and may need reloading as well.
					clearCache();

					// clear the localizer cache as well
					application.getResourceSettings().getLocalizer().clearCache();

					// Inform all listeners
					Iterator iter = afterReloadListeners.iterator();
					while (iter.hasNext())
					{
						IPropertiesChangeListener listener = (IPropertiesChangeListener)iter.next();
						try
						{
							listener.propertiesChanged(key);
						}
						catch (Throwable ex)
						{
							log.error("PropertiesReloadListener has thrown an exception: " +
									ex.getMessage());
						}
					}
				}
			});
		}

		log.info("Loading properties files from " + resourceStream);
		return loadPropertiesFile(key, resourceStream);
	}

	/**
	 * For subclasses to get access to the cache
	 * 
	 * @return Map
	 */
	protected final Map getCache()
	{
		return propertiesCache;
	}
}