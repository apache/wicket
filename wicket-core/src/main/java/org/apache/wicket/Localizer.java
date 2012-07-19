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

import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.wicket.core.util.string.interpolator.PropertyVariableInterpolator;
import org.apache.wicket.markup.repeater.AbstractRepeater;
import org.apache.wicket.model.IModel;
import org.apache.wicket.resource.loader.IStringResourceLoader;
import org.apache.wicket.settings.IResourceSettings;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * A utility class that encapsulates all of the localization related functionality in a way that it
 * can be accessed by all areas of the framework in a consistent way. A singleton instance of this
 * class is available via the <code>Application</code> object.
 * <p>
 * You may register additional IStringResourceLoader to extend or replace Wickets default search
 * strategy for the properties. E.g. string resource loaders which load the properties from a
 * database. There should be hardly any need to extend Localizer.
 * 
 * @see org.apache.wicket.settings.IResourceSettings#getLocalizer()
 * @see org.apache.wicket.resource.loader.IStringResourceLoader
 * @see org.apache.wicket.settings.IResourceSettings#getStringResourceLoaders()
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 */
public class Localizer
{
	private static final Logger log = LoggerFactory.getLogger(Localizer.class);

	/** ConcurrentHashMap does not allow null values */
	private static final String NULL_VALUE = "<null-value>";

	/** Cache properties */
	private Map<String, String> cache = newCache();

	/** Database that maps class names to an integer id. */
	private final ClassMetaDatabase metaDatabase = new ClassMetaDatabase();

	/**
	 * @return Same as Application.get().getResourceSettings().getLocalizer()
	 */
	public static Localizer get()
	{
		return Application.get().getResourceSettings().getLocalizer();
	}

	/**
	 * Create the utils instance class backed by the configuration information contained within the
	 * supplied application object.
	 */
	public Localizer()
	{
	}

	/**
	 * Clear all cache entries by instantiating a new cache object
	 * 
	 * @see #newCache()
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
	public String getString(final String key, final Component component)
		throws MissingResourceException
	{
		return getString(key, component, null, null, null, null);
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
	public String getString(final String key, final Component component, final IModel<?> model)
		throws MissingResourceException
	{
		return getString(key, component, model, null, null, null);
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
	public String getString(final String key, final Component component, final String defaultValue)
		throws MissingResourceException
	{
		return getString(key, component, null, null, null, defaultValue);
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
	 * @param defaultValue
	 *            The default value (optional)
	 * @return The string resource
	 * @throws MissingResourceException
	 *             If resource not found and configuration dictates that exception should be thrown
	 */
	public String getString(final String key, final Component component, final IModel<?> model,
		final String defaultValue) throws MissingResourceException
	{
		return getString(key, component, model, null, null, defaultValue);
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
	 * @param locale
	 *            If != null, it'll supersede the component's locale
	 * @param style
	 *            If != null, it'll supersede the component's style
	 * @param defaultValue
	 *            The default value (optional)
	 * @return The string resource
	 * @throws MissingResourceException
	 *             If resource not found and configuration dictates that exception should be thrown
	 */
	public String getString(final String key, final Component component, final IModel<?> model,
		final Locale locale, final String style, final String defaultValue)
		throws MissingResourceException
	{
		final IResourceSettings resourceSettings = Application.get().getResourceSettings();

		String value = getStringIgnoreSettings(key, component, model, locale, style, null);
		if ((value == null) && (defaultValue != null))
		{
			// Resource not found, so handle missing resources based on
			// application configuration and try the default value
			if (resourceSettings.getUseDefaultOnMissingResource())
			{
				value = defaultValue;

				// If a property value has been found, or a default value was given,
				// than replace the placeholder and we are done
				if (value != null)
				{
					return substitutePropertyExpressions(component, value, model);
				}
			}
		}

		// If a property value has been found, or a default value was given,
		// than replace the placeholder and we are done
		if (value != null)
		{
			return value;
		}

		if (resourceSettings.getThrowExceptionOnMissingResource())
		{
			AppendingStringBuffer message = new AppendingStringBuffer("Unable to find property: '");
			message.append(key);
			message.append("'");

			if (component != null)
			{
				message.append(" for component: ");
				message.append(component.getPageRelativePath());
				message.append(" [class=").append(component.getClass().getName()).append("]");
			}

			throw new MissingResourceException(message.toString(), (component != null
				? component.getClass().getName() : ""), key);
		}

		return "[Warning: Property for '" + key + "' not found]";
	}

	/**
	 * @see #getStringIgnoreSettings(String, Component, IModel, Locale, String, String)
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
	 */
	public final String getStringIgnoreSettings(final String key, final Component component,
		final IModel<?> model, final String defaultValue)
	{
		return getStringIgnoreSettings(key, component, model, null, null, defaultValue);
	}

	/**
	 * This is similar to {@link #getString(String, Component, IModel, String)} except that the
	 * resource settings are ignored. This allows to to code something like
	 * 
	 * <pre>
	 * String option = getLocalizer().getStringIgnoreSettings(getId() + &quot;.null&quot;, this, &quot;&quot;);
	 * if (Strings.isEmpty(option))
	 * {
	 * 	option = getLocalizer().getString(&quot;null&quot;, this, CHOOSE_ONE);
	 * }
	 * </pre>
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param component
	 *            The component to get the resource for (optional)
	 * @param model
	 *            The model to use for substitutions in the strings (optional)
	 * @param locale
	 *            If != null, it'll supersede the component's locale
	 * @param style
	 *            If != null, it'll supersede the component's style
	 * @param defaultValue
	 *            The default value (optional)
	 * @return The string resource
	 */
	public final String getStringIgnoreSettings(final String key, final Component component,
		final IModel<?> model, Locale locale, String style, final String defaultValue)
	{
		boolean addedToPage = false;
		if (component != null)
		{
			if ((component instanceof Page) || (null != component.findParent(Page.class)))
			{
				addedToPage = true;
			}

			if (!addedToPage && log.isWarnEnabled())
			{
				log.warn(
					"Tried to retrieve a localized string for a component that has not yet been added to the page. "
						+ "This can sometimes lead to an invalid or no localized resource returned. "
						+ "Make sure you are not calling Component#getString() inside your Component's constructor. "
						+ "Offending component: {}", component);
			}
		}

		String cacheKey = null;
		String value = null;

		// Make sure locale, style and variation have the right values
		String variation = (component != null ? component.getVariation() : null);

		if ((locale == null) && (component != null))
		{
			locale = component.getLocale();
		}
		if (locale == null)
		{
			locale = Session.exists() ? Session.get().getLocale() : Locale.getDefault();
		}

		if ((style == null) && (component != null))
		{
			style = component.getStyle();
		}
		if (style == null)
		{
			style = Session.exists() ? Session.get().getStyle() : null;
		}

		// If this component is not yet added to page we do not want to check
		// cache as we can generate an invalid cache key
		if ((cache != null) && ((component == null) || addedToPage))
		{
			cacheKey = getCacheKey(key, component, locale, style, variation);
		}

		// Value not found are cached as well (value = null)
		if ((cacheKey != null) && cache.containsKey(cacheKey))
		{
			value = getFromCache(cacheKey);
			if (log.isDebugEnabled())
			{
				log.debug("Property found in cache: '" + key + "'; Component: '" +
					(component != null ? component.toString(false) : null) + "'; value: '" + value +
					'\'');
			}
		}
		else
		{
			if (log.isDebugEnabled())
			{
				log.debug("Locate property: key: '" + key + "'; Component: '" +
					(component != null ? component.toString(false) : null) + '\'');
			}

			// Iterate over all registered string resource loaders until the property has been found
			Iterator<IStringResourceLoader> iter = getStringResourceLoaders().iterator();
			value = null;
			while (iter.hasNext() && (value == null))
			{
				IStringResourceLoader loader = iter.next();
				value = loader.loadStringResource(component, key, locale, style, variation);
			}

			// Cache the result incl null if not found
			if (cacheKey != null)
			{
				putIntoCache(cacheKey, value);
			}

			if ((value == null) && log.isDebugEnabled())
			{
				log.debug("Property not found; key: '" + key + "'; Component: '" +
					(component != null ? component.toString(false) : null) + '\'');
			}
		}

		if (value == null)
		{
			value = defaultValue;
		}

		// If a property value has been found, or a default value was given,
		// than replace the placeholder and we are done
		if (value != null)
		{
			return substitutePropertyExpressions(component, value, model);
		}

		return null;
	}

	/**
	 * In case you want to provide your own list of string resource loaders
	 * 
	 * @return List of string resource loaders
	 */
	protected List<IStringResourceLoader> getStringResourceLoaders()
	{
		return Application.get().getResourceSettings().getStringResourceLoaders();
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
		{
			return;
		}

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
		{
			return null;
		}

		final String value = cache.get(cacheKey);

		// ConcurrentHashMap does not allow null values
		if (NULL_VALUE == value)
		{
			return null;
		}
		return value;
	}

	/**
	 * Gets the cache key
	 * 
	 * @param key
	 * @param component
	 * @param locale
	 *            Guaranteed to be != null
	 * @param style
	 * @param variation
	 * @return The value of the key
	 */
	protected String getCacheKey(final String key, final Component component, final Locale locale,
		final String style, final String variation)
	{
		if (component != null)
		{
			StringBuilder buffer = new StringBuilder(200);
			buffer.append(key);

			Component cursor = component;

			while (cursor != null)
			{
				buffer.append('-').append(metaDatabase.id(cursor.getClass()));

				if (cursor instanceof Page)
				{
					break;
				}

				/*
				 * only append component id if component is not a loop item because (a) these ids
				 * are irrelevant when generating resource cache keys (b) they cause a lot of
				 * redundant keys to be generated
				 * 
				 * also if the cursor component is an auto component we append a constant string
				 * instead of component's id because auto components have a newly generated id on
				 * every render.
				 */
				final Component parent = cursor.getParent();
				final boolean skip = parent instanceof AbstractRepeater;

				if (skip == false)
				{
					String cursorKey = cursor.isAuto() ? "wicket-auto" : cursor.getId();
					buffer.append(':').append(cursorKey);
				}

				cursor = parent;
			}

			buffer.append('-').append(locale);
			buffer.append('-').append(style);
			buffer.append('-').append(variation);

			return buffer.toString();
		}
		else
		{
			// locale is guaranteed to be != null
			return key + '-' + locale.toString() + '-' + style;
		}
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
	public String substitutePropertyExpressions(final Component component, final String string,
		final IModel<?> model)
	{
		if ((string != null) && (model != null))
		{
			return new PropertyVariableInterpolator(string, model.getObject())
			{
				@SuppressWarnings({ "rawtypes", "unchecked" })
				@Override
				protected String toString(Object value)
				{
					IConverter converter;
					Locale locale;
					if (component == null)
					{
						converter = Application.get()
							.getConverterLocator()
							.getConverter(value.getClass());

						if (Session.exists())
						{
							locale = Session.get().getLocale();
						}
						else
						{
							locale = Locale.getDefault();
						}
					}
					else
					{
						converter = component.getConverter(value.getClass());
						locale = component.getLocale();
					}

					return converter.convertToString(value, locale);
				}
			}.toString();
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
		private final ConcurrentMap<String, Long> nameToId = Generics.newConcurrentHashMap();
		private final AtomicLong nameCounter = new AtomicLong();

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
			Long id = nameToId.get(name);
			if (id == null)
			{
				id = nameCounter.incrementAndGet();
				Long previousId = nameToId.putIfAbsent(name, id);
				if (previousId != null)
				{
					id = previousId;
				}
			}
			return id;
		}
	}
}
