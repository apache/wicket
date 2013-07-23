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
import java.util.concurrent.ConcurrentHashMap;

import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.io.IOUtils;
import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.core.util.resource.locator.IResourceStreamLocator;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.util.watch.IModificationWatcher;
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

	/** Listeners will be invoked after changes to property file have been detected */
	private final List<IPropertiesChangeListener> afterReloadListeners = new ArrayList<IPropertiesChangeListener>();

	/** Cache for all property files loaded */
	private final Map<String, Properties> propertiesCache = newPropertiesCache();

	/** Provides the environment for properties factory */
	private final IPropertiesFactoryContext context;

	/** List of Properties Loader */
	private final List<IPropertiesLoader> propertiesLoader;

	/**
	 * Construct.
	 * 
	 * @param context
	 *            context for properties factory
	 */
	public PropertiesFactory(final IPropertiesFactoryContext context)
	{
		this.context = context;
		this.propertiesLoader = new ArrayList<IPropertiesLoader>();
		this.propertiesLoader.add(new IsoPropertiesFilePropertiesLoader("properties"));
		this.propertiesLoader.add(new UtfPropertiesFilePropertiesLoader("utf8.properties", "utf-8"));
		this.propertiesLoader.add(new XmlFilePropertiesLoader("properties.xml"));
	}

	/**
	 * Gets the {@link List} of properties loader. You may add or remove properties loaders at your
	 * will.
	 * 
	 * @return the {@link List} of properties loader
	 */
	public List<IPropertiesLoader> getPropertiesLoaders()
	{
		return propertiesLoader;
	}

	/**
	 * @return new Cache implementation
	 */
	protected Map<String, Properties> newPropertiesCache()
	{
		return new ConcurrentHashMap<String, Properties>();
	}

	/**
	 * @see org.apache.wicket.resource.IPropertiesFactory#addListener(org.apache.wicket.resource.IPropertiesChangeListener)
	 */
	@Override
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
	@Override
	public final void clearCache()
	{
		if (propertiesCache != null)
		{
			propertiesCache.clear();
		}

		// clear the localizer cache as well
		context.getLocalizer().clearCache();
	}

	/**
	 * 
	 * @see org.apache.wicket.resource.IPropertiesFactory#load(java.lang.Class, java.lang.String)
	 */
	@Override
	public Properties load(final Class<?> clazz, final String path)
	{
		// Check the cache
		Properties properties = null;
		if (propertiesCache != null)
		{
			properties = propertiesCache.get(path);
		}

		if (properties == null)
		{
			Iterator<IPropertiesLoader> iter = propertiesLoader.iterator();
			while ((properties == null) && iter.hasNext())
			{
				IPropertiesLoader loader = iter.next();
				String fullPath = path + "." + loader.getFileExtension();

				// If not in the cache than try to load properties
				IResourceStream resourceStream = context.getResourceStreamLocator()
					.locate(clazz, fullPath);
				if (resourceStream == null)
				{
					continue;
				}

				// Watch file modifications
				final IModificationWatcher watcher = context.getResourceWatcher(true);
				if (watcher != null)
				{
					addToWatcher(path, resourceStream, watcher);
				}

				ValueMap props = loadFromLoader(loader, resourceStream);
				if (props != null)
				{
					properties = new Properties(path, props);
				}
			}

			// Cache the lookup
			if (propertiesCache != null)
			{
				if (properties == null)
				{
					// Could not locate properties, store a placeholder
					propertiesCache.put(path, Properties.EMPTY_PROPERTIES);
				}
				else
				{
					propertiesCache.put(path, properties);
				}
			}
		}

		if (properties == Properties.EMPTY_PROPERTIES)
		{
			// Translate empty properties placeholder to null prior to returning
			properties = null;
		}

		return properties;
	}

	/**
	 * 
	 * @param loader
	 * @param resourceStream
	 * @return properties
	 */
	protected ValueMap loadFromLoader(final IPropertiesLoader loader,
		final IResourceStream resourceStream)
	{
		if (log.isInfoEnabled())
		{
			log.info("Loading properties files from " + resourceStream + " with loader " + loader);
		}

		BufferedInputStream in = null;

		try
		{
			// Get the InputStream
			in = new BufferedInputStream(resourceStream.getInputStream());
			ValueMap data = loader.loadWicketProperties(in);
			if (data == null)
			{
				java.util.Properties props = loader.loadJavaProperties(in);
				if (props != null)
				{
					// Copy the properties into the ValueMap
					data = new ValueMap();
					Enumeration<?> enumeration = props.propertyNames();
					while (enumeration.hasMoreElements())
					{
						String property = (String)enumeration.nextElement();
						data.put(property, props.getProperty(property));
					}
				}
			}
			return data;
		}
		catch (ResourceStreamNotFoundException e)
		{
			log.warn("Unable to find resource " + resourceStream, e);
		}
		catch (IOException e)
		{
			log.warn("Unable to find resource " + resourceStream, e);
		}
		finally
		{
			IOUtils.closeQuietly(in);
			IOUtils.closeQuietly(resourceStream);
		}

		return null;
	}

	/**
	 * Add the resource stream to the file being watched
	 * 
	 * @param path
	 * @param resourceStream
	 * @param watcher
	 */
	protected void addToWatcher(final String path, final IResourceStream resourceStream,
		final IModificationWatcher watcher)
	{
		watcher.add(resourceStream, new IChangeListener()
		{
			@Override
			public void onChange()
			{
				log.info("A properties files has changed. Removing all entries " +
					"from the cache. Resource: " + resourceStream);

				// Clear the whole cache as associated localized files may
				// be affected and may need reloading as well.
				clearCache();

				// Inform all listeners
				for (IPropertiesChangeListener listener : afterReloadListeners)
				{
					try
					{
						listener.propertiesChanged(path);
					}
					catch (Exception ex)
					{
						PropertiesFactory.log.error("PropertiesReloadListener has thrown an exception: " +
							ex.getMessage());
					}
				}
			}
		});
	}

	/**
	 * For subclasses to get access to the cache
	 * 
	 * @return Map
	 */
	protected final Map<String, Properties> getCache()
	{
		return propertiesCache;
	}
}
