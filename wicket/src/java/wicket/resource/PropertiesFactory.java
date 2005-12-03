/*
 * $Id: ComponentStringResourceLoader.java,v 1.5 2005/01/19 08:07:57
 * jonathanlocke Exp $ $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.resource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.util.listener.IChangeListener;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.value.ValueMap;
import wicket.util.watch.ModificationWatcher;

/**
 * Reloadable properties. It is not a 100% replacement for java.util.Properties
 * as it does not provide the same interface. But is serves kind of the same
 * purpose with Wicket specific features. PropertiesFactory actually loads
 * and reloads the Properties and mataince a cache. Hence properties files 
 * are loaded just once.
 * <p>
 * @see Application#getPropertiesFactory()
 * 
 * @author Juergen Donnerstag
 */
public class PropertiesFactory
{
	/** Log. */
	private static final Log log = LogFactory.getLog(PropertiesFactory.class);

	/** Cache for all properties files loaded */
	private final Map propertiesCache = new HashMap();

	/** Listeners will be invoked after properties have been reloaded */
	private final List afterReloadListeners = new ArrayList();

	/**
	 * Construct.
	 */
	public PropertiesFactory()
	{
	}

	/**
	 * Add a listener
	 * 
	 * @param listener
	 */
	public void addListener(final IPropertiesReloadListener listener)
	{
		// Make sure listeners are added only once
		if (afterReloadListeners.contains(listener) == false)
		{
			afterReloadListeners.add(listener);
		}
	}
	
    /**
     * Get the properties for ...
     * 
     * @param application
     *            The application object
	 * @param clazz
	 *            The class that resources are bring loaded for
	 * @param style
	 *            The style to load resources for (see {@link wicket.Session})
	 * @param locale
	 *            The locale to load reosurces for
	 * @return The properties
	 */
	public final Properties get(final Application application, final Class clazz, final String style, final Locale locale)
	{
		final String key = createResourceKey(clazz, locale, style);
		Properties props = (Properties)propertiesCache.get(key);
		if (props == null)
		{
			final IResourceStream resource = application.getResourceStreamLocator().locate(
					clazz, style, locale, "properties");

			if (resource != null)
			{
				props = loadPropertiesFileAndWatchForChanges(key, resource, clazz, style, locale);
			}
		}
		
		return props;
	}

	/**
	 * Remove all cached properties
	 *
	 */
	public final void clearCache()
	{
		propertiesCache.clear();
	}
	
	/**
	 * Create a unique key to identify the properties file in the cache
	 * 
	 * @param componentClass
	 *            The class that resources are bring loaded for
	 * @param locale
	 *            The locale to load reosurces for
	 * @param style
	 *            The style to load resources for (see {@link wicket.Session})
	 * @return The resource key
	 */
	public final String createResourceKey(final Class componentClass, 
			final Locale locale, final String style)
	{
		final StringBuffer buffer = new StringBuffer(80);
		if (componentClass != null)
		{
			buffer.append(componentClass.getName());
		}
		if (style != null)
		{
			buffer.append(':');
			buffer.append(style);
		}
		if (locale != null)
		{
			buffer.append(':');
			buffer.append(locale.toString());
		}

		final String id = buffer.toString();
		return id;
	}

	/**
	 * Helper method to do the actual loading of resources if required.
	 * 
	 * @param key
	 *            The key for the resource
	 * @param resourceStream
	 *            The properties file stream to load and begin to watch
	 * @param componentClass
	 *            The class that resources are bring loaded for
	 * @param style
	 *            The style to load resources for (see {@link wicket.Session})
	 * @param locale
	 *            The locale to load reosurces for
	 * @return The map of loaded resources
	 */
	private synchronized Properties loadPropertiesFile(final String key,
			final IResourceStream resourceStream, final Class componentClass, final String style,
			final Locale locale)
	{
		// Make sure someone else didn't load our resources while we were
		// waiting for the synchronized lock on the method
		Properties props = (Properties)propertiesCache.get(key);
		if (props != null)
		{
			return props;
		}

		// Do the resource load
		final java.util.Properties properties = new java.util.Properties();

		if (resourceStream == null)
		{
			props = new Properties(key, ValueMap.EMPTY_MAP);
		}
		else
		{
			ValueMap strings = ValueMap.EMPTY_MAP;
			
			try
			{
				try
				{
					properties.load(new BufferedInputStream(resourceStream.getInputStream()));
					strings = new ValueMap(properties);
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

			// add the markup to the cache
			synchronized (propertiesCache)
			{
				propertiesCache.put(key, props);
			}
		}

		return props;
	}

	/**
	 * Load properties file from an IResourceStream and add an
	 * {@link IChangeListener}to the {@link ModificationWatcher} so that if the
	 * resource changes, we can reload it automatically.
	 * 
	 * @param key
	 *            The key for the resource
	 * @param resourceStream
	 *            The properties file stream to load and begin to watch
	 * @param componentClass
	 *            The class that resources are bring loaded for
	 * @param style
	 *            The style to load resources for (see {@link wicket.Session})
	 * @param locale
	 *            The locale to load reosurces for
	 * @return The map of loaded resources
	 */
	private final Properties loadPropertiesFileAndWatchForChanges(final String key,
			final IResourceStream resourceStream, final Class componentClass, final String style,
			final Locale locale)
	{
		// Watch file in the future
		final ModificationWatcher watcher = Application.get().getResourceWatcher();
		if (watcher != null)
		{
			watcher.add(resourceStream, new IChangeListener()
			{
				public void onChange()
				{
					log.info("Reloading properties files from " + resourceStream);
					loadPropertiesFile(key, resourceStream, componentClass, style, locale);

					// Inform all listeners
					for(Iterator iter=afterReloadListeners.iterator(); iter.hasNext();)
					{
						IPropertiesReloadListener listener = (IPropertiesReloadListener)iter.next();
						try
						{
							listener.propertiesLoaded(key);
						}
						catch (Throwable ex)
						{
							log.error("PropertiesReloadListener throw an exception: " 
									+ ex.getMessage());
						}
					}
				}
			});
		}

		log.info("Loading properties files from " + resourceStream);
		return loadPropertiesFile(key, resourceStream, componentClass, style, locale);
	}
}