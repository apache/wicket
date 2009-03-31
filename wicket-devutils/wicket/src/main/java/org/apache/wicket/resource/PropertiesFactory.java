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

import org.apache.wicket.Application;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.listener.IChangeListener;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.resource.locator.IResourceStreamLocator;
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

	/** Listeners will be invoked after changes to property file have been detected */
	private final List<IPropertiesChangeListener> afterReloadListeners = new ArrayList<IPropertiesChangeListener>();

	/** Cache for all property files loaded */
	private final Map<String, Properties> propertiesCache = new ConcurrentHashMap<String, Properties>();

	/** Application */
	private final Application application;

	/** List of Properties Loader */
	private final List<IPropertiesLoader> propertiesLoader;

	/**
	 * Construct.
	 * 
	 * @param application
	 *            Application for this properties factory.
	 */
	public PropertiesFactory(Application application)
	{
		this.application = application;

		propertiesLoader = new ArrayList<IPropertiesLoader>();
		propertiesLoader.add(new PropertiesFilePropertiesLoader());
		propertiesLoader.add(new XmlFilePropertiesLoader("xml"));
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

		// clear the localizer cache as well
		application.getResourceSettings().getLocalizer().clearCache();
	}

	/**
	 * 
	 * @see org.apache.wicket.resource.IPropertiesFactory#load(java.lang.Class, java.lang.String)
	 */
	public Properties load(final Class<?> clazz, final String path)
	{
		// Check the cache
		Properties properties = propertiesCache.get(path);

		if (properties == null)
		{
			Iterator<IPropertiesLoader> iter = propertiesLoader.iterator();
			while ((properties == null) && iter.hasNext())
			{
				IPropertiesLoader loader = iter.next();
				properties = loader.load(clazz, path);
			}

			// Cache the lookup
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

		if (properties == Properties.EMPTY_PROPERTIES)
		{
			// Translate empty properties placeholder to null prior to returning
			properties = null;
		}

		return properties;
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

	/**
	 * 
	 */
	public interface IPropertiesLoader
	{
		/**
		 * 
		 * @param clazz
		 * @param path
		 * @return Properties
		 */
		Properties load(final Class<?> clazz, final String path);
	}

	/**
	 * 
	 */
	public abstract class AbstractPropertiesLoader implements IPropertiesLoader
	{
		/**
		 * Construct.
		 */
		public AbstractPropertiesLoader()
		{
		}

		/**
		 * 
		 * @return File extension
		 */
		abstract protected String getFileExtension();

		/**
		 * 
		 * @param in
		 * @return java.util.Properties
		 * @throws IOException
		 */
		abstract protected java.util.Properties loadProperties(BufferedInputStream in)
			throws IOException;

		/**
		 * 
		 * @see org.apache.wicket.resource.PropertiesFactory.IPropertiesLoader#load(java.lang.Class,
		 *      java.lang.String)
		 */
		public Properties load(final Class<?> clazz, final String path)
		{
			String fullPath = path + getFileExtension();

			// If not in the cache than try to load properties
			final IResourceStream resourceStream = application.getResourceSettings()
				.getResourceStreamLocator()
				.locate(clazz, fullPath);

			if (resourceStream == null)
			{
				return null;
			}

			// Watch file modifications
			final ModificationWatcher watcher = application.getResourceSettings()
				.getResourceWatcher(true);
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

						// Inform all listeners
						Iterator<IPropertiesChangeListener> iter = afterReloadListeners.iterator();
						while (iter.hasNext())
						{
							IPropertiesChangeListener listener = iter.next();
							try
							{
								listener.propertiesChanged(path);
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
			ValueMap strings = null;

			try
			{
				BufferedInputStream in = null;

				try
				{
					// Get the InputStream
					in = new BufferedInputStream(resourceStream.getInputStream());

					java.util.Properties properties = loadProperties(in);

					// Copy the properties into the ValueMap
					strings = new ValueMap();
					Enumeration<?> enumeration = properties.propertyNames();
					while (enumeration.hasMoreElements())
					{
						String property = (String)enumeration.nextElement();
						strings.put(property, properties.getProperty(property));
					}

					return new Properties(path, strings);
				}
				finally
				{
					if (in != null)
					{
						in.close();
					}
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

			return null;
		}
	}

	/**
	 * 
	 */
	public class PropertiesFilePropertiesLoader extends AbstractPropertiesLoader
	{
		/**
		 * Construct.
		 */
		public PropertiesFilePropertiesLoader()
		{
		}

		/**
		 * 
		 * @see org.apache.wicket.resource.PropertiesFactory.AbstractPropertiesLoader#getFileExtension()
		 */
		@Override
		protected String getFileExtension()
		{
			return "properties";
		}

		/**
		 * 
		 * @param in
		 * @return java.util.Properties
		 * @throws IOException
		 */
		@Override
		protected java.util.Properties loadProperties(BufferedInputStream in) throws IOException
		{
			java.util.Properties properties = new java.util.Properties();
			properties.load(in);
			return properties;
		}
	}

	/**
	 * 
	 */
	public class XmlFilePropertiesLoader extends AbstractPropertiesLoader
	{
		private final String fileExtension;

		/**
		 * Construct.
		 * 
		 * @param fileExtension
		 */
		public XmlFilePropertiesLoader(final String fileExtension)
		{
			this.fileExtension = fileExtension;
		}

		/**
		 * 
		 * @see org.apache.wicket.resource.PropertiesFactory.AbstractPropertiesLoader#getFileExtension()
		 */
		@Override
		protected String getFileExtension()
		{
			return fileExtension;
		}

		/**
		 * 
		 * @param in
		 * @return java.util.Properties
		 * @throws IOException
		 */
		@Override
		protected java.util.Properties loadProperties(BufferedInputStream in) throws IOException
		{
			java.util.Properties properties = new java.util.Properties();
			Streams.loadFromXml(properties, in);
			return properties;
		}
	}
}