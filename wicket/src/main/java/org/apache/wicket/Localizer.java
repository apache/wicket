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
package org.apache.wicket;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.model.IModel;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.interpolator.PropertyVariableInterpolator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A utility class that encapsulates all of the localization related functionality in a way that it
 * can be accessed by all areas of the framework in a consistent way. A singleton instance of this
 * class is available via the <code>Application</code> object.
 * <p>
 * You may register additional IStringResourceLoader to extend or replace Wickets default search
 * strategy for the properties. E.g. string resource loaders which load the properties from a
 * database. There should be no need to extend Localizer.
 * 
 * @see org.apache.wicket.settings.Settings#getLocalizer()
 * @see org.apache.wicket.resource.loader.IStringResourceLoader
 * @see org.apache.wicket.settings.Settings#getStringResourceLoaders()
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 */
public class Localizer
{
	private static final Logger logger = LoggerFactory.getLogger(Localizer.class);

	/** ConcurrentHashMap does not allow null values */
	private static final String NULL_VALUE = "<null-value>";

	/** Cache properties */
	private Map<String, String> cache = newCache();

	private final ClassMetaDatabase metaDatabase = new ClassMetaDatabase();

	/**
	 * Create the utils instance class backed by the configuration information contained within the
	 * supplied application object.
	 */
	public Localizer()
	{
	}

	/**
	 * Clear all cache entries
	 */
	public final void clearCache()
	{
		if (cache != null)
		{
			cache = newCache();
		}
	}

	/**
	 * @see #getString(String, Component, IModel, Locale, String, String)
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param component
	 *            The component to get the resource for
	 * @return The string resource
	 * @throws MissingResourceException
	 *             If resource not found and configuration dictates that exception should be thrown
	 */
	public String getString(final String key, final Component<?> component)
		throws MissingResourceException
	{
		return getString(key, component, null, null);
	}

	/**
	 * @see #getString(String, Component, IModel, Locale, String, String)
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param component
	 *            The component to get the resource for
	 * @param model
	 *            The model to use for property substitutions in the strings (optional)
	 * @return The string resource
	 * @throws MissingResourceException
	 *             If resource not found and configuration dictates that exception should be thrown
	 */
	public String getString(final String key, final Component<?> component, final IModel<?> model)
		throws MissingResourceException
	{
		return getString(key, component, model, null);
	}

	/**
	 * @see #getString(String, Component, IModel, Locale, String, String)
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param component
	 *            The component to get the resource for
	 * @param defaultValue
	 *            The default value (optional)
	 * @return The string resource
	 * @throws MissingResourceException
	 *             If resource not found and configuration dictates that exception should be thrown
	 */
	public String getString(final String key, final Component<?> component,
		final String defaultValue) throws MissingResourceException
	{
		return getString(key, component, null, defaultValue);
	}

	/**
	 * This method is now deprecated.
	 * 
	 * @param key
	 * @param component
	 * @param model
	 * @param locale
	 * @param style
	 * @param defaultValue
	 * @return String
	 * @throws MissingResourceException
	 * 
	 * @Deprecated please use {@link #getString(String, Component, IModel, String)}
	 */
	public String getString(final String key, final Component<?> component, final IModel<?> model,
		final Locale locale, final String style, final String defaultValue)
		throws MissingResourceException
	{
		return getString(key, component, model, defaultValue);
	}

	/**
	 * Get the localized string using all of the supplied parameters. This method is left public to
	 * allow developers full control over string resource loading. However, it is recommended that
	 * one of the other convenience methods in the class are used as they handle all of the work
	 * related to obtaining the current user locale and style information.
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param component
	 *            The component to get the resource for (optional)
	 * @param model
	 *            The model to use for substitutions in the strings (optional)
	 * @param defaultValue
	 *            The default value (optional)
	 * @return The string resource
	 * @throws MissingResourceException
	 *             If resource not found and configuration dictates that exception should be thrown
	 */
	public String getString(final String key, final Component<?> component, final IModel<?> model,
		final String defaultValue) throws MissingResourceException
	{
		final IResourceSettings resourceSettings = Application.get().getResourceSettings();

		boolean addedToPage = false;
		if (component != null)
		{
			if ((component instanceof Page) || null != component.findParent(Page.class))
			{
				addedToPage = true;
			}

			if (!addedToPage)
			{
				logger.warn(
					"Tried to retrieve a localized string for a component that has not yet been added to the page. "
						+ "This can sometimes lead to an invalid or no localized resource returned. "
						+ "Make sure you are not calling Component#getString() inside your Component's constructor. "
						+ "Offending component: {}", component);
			}
		}


		String cacheKey = null;
		String string = null;

		// If this component is not yet added to page we do not want to check
		// cache as we can generate an invalid cache key
		if ((cache != null) && addedToPage)
		{
			cacheKey = getCacheKey(key, component);
		}

		// Value not found are cached as well (value = null)
		if ((cacheKey != null) && cache.containsKey(cacheKey))
		{
			string = getFromCache(cacheKey);
		}
		else
		{
			// Iterate over all registered string resource loaders until the
			// property has been found

			Iterator<IStringResourceLoader> iter = resourceSettings.getStringResourceLoaders()
				.iterator();
			while (iter.hasNext())
			{
				IStringResourceLoader loader = iter.next();
				string = loader.loadStringResource(component, key);
				if (string != null)
				{
					break;
				}
			}

			// Cache the result incl null if not found
			if (cacheKey != null)
			{
				putIntoCache(cacheKey, string);
			}
		}

		if ((string == null) && (defaultValue != null))
		{
			// Resource not found, so handle missing resources based on
			// application configuration and try the default value
			if (resourceSettings.getUseDefaultOnMissingResource())
			{
				string = defaultValue;
			}
		}

		// If a property value has been found, or a default value was given,
		// than replace the placeholder and we are done
		if (string != null)
		{
			return substitutePropertyExpressions(component, string, model);
		}

		if (resourceSettings.getThrowExceptionOnMissingResource())
		{
			AppendingStringBuffer message = new AppendingStringBuffer("Unable to find resource: " +
				key);
			if (component != null)
			{
				message.append(" for component: ");
				message.append(component.getPageRelativePath());
				message.append(" [class=").append(component.getClass().getName()).append("]");
			}
			throw new MissingResourceException(message.toString(), (component != null
				? component.getClass().getName() : ""), key);
		}

		return "[Warning: String resource for '" + key + "' not found]";
	}

	/**
	 * Put the value into the cache and associate it with the cache key
	 * 
	 * @param cacheKey
	 * @param string
	 */
	protected void putIntoCache(final String cacheKey, final String string)
	{
		if (cache == null)
			return;

		// ConcurrentHashMap does not allow null values
		if (string == null)
		{
			cache.put(cacheKey, NULL_VALUE);
		}
		else
		{
			cache.put(cacheKey, string);
		}
	}

	/**
	 * Get the value associated with the key from the cache.
	 * 
	 * @param cacheKey
	 * @return The value of the key
	 */
	protected String getFromCache(final String cacheKey)
	{
		if (cache == null)
			return null;

		final String value = cache.get(cacheKey);

		// ConcurrentHashMap does not allow null values
		if (value == NULL_VALUE)
		{
			return null;
		}
		else
		{
			return value;
		}
	}

	/**
	 * Gets the cache key
	 * 
	 * @param key
	 * @param component
	 * @return The value of the key
	 */
	protected String getCacheKey(final String key, final Component<?> component)
	{
		String cacheKey = key;
		if (component != null)
		{
			AppendingStringBuffer buffer = new AppendingStringBuffer(200);
			buffer.append(key);

			Component<?> cursor = component;
			while (cursor != null)
			{
				buffer.append("-").append(metaDatabase.id(cursor.getClass()));

				if (cursor instanceof Page)
				{
					break;
				}

				if (cursor.getParent() != null && !(cursor.getParent() instanceof AbstractRepeater))
				{
					/*
					 * only append component id if parent is not a repeater because
					 * 
					 * (a) these ids are irrelevant when generating resource cache keys
					 * 
					 * (b) they cause a lot of redundant keys to be generated
					 */
					buffer.append(":").append(cursor.getId());
				}
				cursor = cursor.getParent();

			}

			buffer.append("-").append(component.getLocale());
			buffer.append("-").append(component.getStyle());
			// TODO 1.4 look if we want to properly separate getstyle/getvariation
			// for now getvariation() is rolled up into getstyle()
			// buffer.append("-").append(component.getVariation());
			cacheKey = buffer.toString();
		}
		return cacheKey;
	}

	/**
	 * Helper method to handle property variable substitution in strings.
	 * 
	 * @param component
	 *            The component requesting a model value
	 * @param string
	 *            The string to substitute into
	 * @param model
	 *            The model
	 * @return The resulting string
	 */
	public String substitutePropertyExpressions(final Component<?> component, final String string,
		final IModel<?> model)
	{
		if ((string != null) && (model != null))
		{
			return PropertyVariableInterpolator.interpolate(string, model.getObject());
		}
		return string;
	}

	/**
	 * By default the cache is enabled. Disabling the cache will disable it and clear the cache.
	 * This can be handy for example in development mode.
	 * 
	 * @param value
	 */
	public final void setEnableCache(boolean value)
	{
		if (value == false)
		{
			cache = null;
		}
		else if (cache == null)
		{
			cache = newCache();
		}
	}

	/**
	 * Create a new cache, override this method if you want a different map to store the cache keys,
	 * for example a map that hold only the last X number of elements..
	 * 
	 * By default it uses the {@link ConcurrentHashMap}
	 * 
	 * @return cache
	 */
	protected Map<String, String> newCache()
	{
		return new ConcurrentHashMap<String, String>();
	}

	/**
	 * Database that maps class names to an integer id. This is used to make localizer keys shorter
	 * because sometimes they can contain a large number of class names.
	 * 
	 * @author igor.vaynberg
	 */
	private static class ClassMetaDatabase
	{
		private final Map<String, Long> nameToId = new HashMap<String, Long>();
		private final ReadWriteLock nameToIdLock = new ReentrantReadWriteLock();
		private long nameCounter = 0;

		/**
		 * Returns a unique id that represents this class' name. This can be used for compressing
		 * class names. Notice this id should not be used across cluster nodes.
		 * 
		 * @param clazz
		 * @return long id of class name
		 */
		public long id(Class<?> clazz)
		{
			final String name = clazz.getName();
			nameToIdLock.readLock().lock();
			try
			{
				Long id = nameToId.get(name);
				if (id == null)
				{
					nameToIdLock.readLock().unlock();
					nameToIdLock.writeLock().lock();
					try
					{
						nameToId.put(name, nameCounter);
						id = nameCounter;
						nameCounter++;
					}
					finally
					{
						nameToIdLock.readLock().lock();
						nameToIdLock.writeLock().unlock();
					}
				}
				return id;
			}
			finally
			{
				nameToIdLock.readLock().unlock();
			}
		}
	}


}