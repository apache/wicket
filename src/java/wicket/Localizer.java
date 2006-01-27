/*
 * $Id$ $Revision:
 * 1.5 $ $Date$
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
package wicket;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.model.IModel;
import wicket.resource.IPropertiesReloadListener;
import wicket.resource.loader.IStringResourceLoader;
import wicket.settings.IResourceSettings;
import wicket.util.concurrent.ConcurrentReaderHashMap;
import wicket.util.string.Strings;
import wicket.util.string.interpolator.PropertyVariableInterpolator;

/**
 * Utility class that encapsulates all of the localization related functionality
 * in a way that is can be accessed by all areas of the framework in a
 * consistent way. A singleton instance of this class is available via the
 * <code>Application</code> object.
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 * @see wicket.settings.Settings#getLocalizer()
 */
public class Localizer
{
	/** Log */
	private static final Log log = LogFactory.getLog(Localizer.class);

	/** ConcurrentReaderHashMap does not allow null values. This is a substitute */
	private static final String NULL = new String();

	/** The application and its settings to use to control the utils. */
	private Application application;

	/** Because properties search can be expensive, we cache the value */
	private Map cachedValues = new ConcurrentReaderHashMap();

	/**
	 * Create the utils instance class backed by the configuration information
	 * contained within the supplied settings object.
	 * 
	 * @param application
	 *            The application to localize for
	 */
	public Localizer(final Application application)
	{
		this.application = application;

		// Register a listener to the properties factory which is invoked after
		// a properties file has been reloaded.
		application.getResourceSettings().getPropertiesFactory().addListener(
				new IPropertiesReloadListener()
				{
					public void propertiesLoaded(final String key)
					{
						// Remove all cached values. Unfortunately I did not yet
						// find a proper way (which is easy and clean to
						// implement) which selectively removes just the cache
						// entries affected. Hence they all get removed.
						// Actually that is less worse as it may sound, because
						// type Properties does cache them as well. We only have
						// to walk the properties resolution path once again.
						// And, the feature of reloading the properties file
						// is usually activated during development only and
						// for production. Hence, it affect development only.
						cachedValues.clear();
					}
				});
	}

	/**
	 * Remove all cached properties
	 */
	public final void clearCache()
	{
		this.cachedValues.clear();
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
	 *             If resource not found and configuration dictates that
	 *             exception should be thrown
	 */
	public String getString(final String key, final Component component)
			throws MissingResourceException
	{
		return getString(key, component, null, component.getLocale(), component.getStyle(), null);
	}

	/**
	 * @see #getString(String, Component, IModel, Locale, String, String)
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param component
	 *            The component to get the resource for
	 * @param model
	 *            The model to use for property substitutions in the strings
	 *            (optional)
	 * @return The string resource
	 * @throws MissingResourceException
	 *             If resource not found and configuration dictates that
	 *             exception should be thrown
	 */
	public String getString(final String key, final Component component, final IModel model)
			throws MissingResourceException
	{
		return getString(key, component, model, component.getLocale(), component.getStyle(), null);
	}

	/**
	 * Get the localized string using all of the supplied parameters. This
	 * method is left public to allow developers full control over string
	 * resource loading. However, it is recommended that one of the other
	 * convenience methods in the class are used as they handle all of the work
	 * related to obtaining the current user locale and style information.
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param component
	 *            The component to get the resource for (optional)
	 * @param model
	 *            The model to use for substitutions in the strings (optional)
	 * @param locale
	 *            The locale to get the resource for (optional)
	 * @param style
	 *            The style to get the resource for (optional) (see
	 *            {@link wicket.Session})
	 * @param defaultValue
	 *            The default value (optional)
	 * @return The string resource
	 * @throws MissingResourceException
	 *             If resource not found and configuration dictates that
	 *             exception should be thrown
	 */
	public String getString(final String key, final Component component, final IModel model,
			final Locale locale, final String style, final String defaultValue)
			throws MissingResourceException
	{
		// Create the cache key
		Class clazz = (component != null ? component.getClass() : null);
		String id = createCacheId(clazz, locale, style, key);
		if (component != null)
		{
			id += ":" + component.getId();
		}

		// The cached key value
		String string = getCachedValue(id);

		// If not found in the cache
		if (string == null)
		{
			final List searchStack;
			final String path;
			if (component != null)
			{
				searchStack = getComponentStack(component);
				path = Strings.replaceAll(component.getPageRelativePath(), ":", ".");
			}
			else
			{
				searchStack = new ArrayList(0);
				searchStack.add(Application.get().getClass());
				path = null;
			}

			string = traverseResourceLoaders(key, path, searchStack, locale, style);

			// cache all values, not matter the key has been found or not
			if (string != null)
			{
				this.cachedValues.put(id, string);
			}
			else
			{
				// ConcurrentReaderHashMap does not allow null values. This is a
				// substitute
				this.cachedValues.put(id, NULL);
			}
		}

		if (string != null)
		{
			return substitutePropertyExpressions(component, string, model);
		}

		// Resource not found, so handle missing resources based on application
		// configuration
		final IResourceSettings resourceSettings = application.getResourceSettings();
		if (resourceSettings.getUseDefaultOnMissingResource() && (defaultValue != null))
		{
			return defaultValue;
		}

		if (resourceSettings.getThrowExceptionOnMissingResource())
		{
			throw new MissingResourceException("Unable to find resource: " + key, (clazz != null
					? getClass().getName()
					: ""), key);
		}
		else
		{
			return "[Warning: String resource for '" + key + "' not found]";
		}
	}

	/**
	 * @see #getString(String, Component, IModel, Locale, String, String)
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param component
	 *            The component to get the resource for
	 * @param model
	 *            The model to use for property substitutions in the strings
	 *            (optional)
	 * @param defaultValue
	 *            The default value (optional)
	 * @return The string resource
	 * @throws MissingResourceException
	 *             If resource not found and configuration dictates that
	 *             exception should be thrown
	 */
	public String getString(final String key, final Component component, final IModel model,
			final String defaultValue) throws MissingResourceException
	{
		return getString(key, component, model, component.getLocale(), component.getStyle(),
				defaultValue);
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
	 *             If resource not found and configuration dictates that
	 *             exception should be thrown
	 */
	public String getString(final String key, final Component component, final String defaultValue)
			throws MissingResourceException
	{
		return getString(key, component, null, component.getLocale(), component.getStyle(),
				defaultValue);
	}

	/**
	 * Note: This implementation does NOT allow variable substitution
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param searchStack
	 *            A stack of classes to get the resource for
	 * @param path
	 *            The component id path relative to the page
	 * @param locale
	 *            The locale to get the resource for (optional)
	 * @param style
	 *            The style to get the resource for (optional) (see
	 *            {@link wicket.Session})
	 * @return The string resource
	 * @throws MissingResourceException
	 *             If resource not found and configuration dictates that
	 *             exception should be thrown
	 */
	public String getString(final String key, final String path, final List searchStack,
			final Locale locale, final String style) throws MissingResourceException
	{
		if (searchStack == null)
		{
			throw new IllegalArgumentException(
					"Parameter 'searchStack' must have at least one entry");
		}

		// The top element
		Class componentClass = (searchStack.size() > 0 ? (Class)searchStack.get(0) : null);

		// If value is cached already ...
		String id = createCacheId(componentClass, locale, style, key);

		// The cached key value
		String string = getCachedValue(id);

		// If not found in the cache
		if (string == null)
		{
			string = traverseResourceLoaders(key, path, searchStack, locale, style);

			// cache all values, not matter the key has been found or not
			if (string != null)
			{
				this.cachedValues.put(id, string);
			}
			else
			{
				// ConcurrentReaderHashMap does not allow null values. This is a
				// substitute
				this.cachedValues.put(id, NULL);
			}
		}

		return string;
	}

	/**
	 * Helper method to create a unique id for caching previously loaded
	 * resources.
	 * 
	 * @param clazz
	 *            The class that the resources is being loaded for
	 * @param locale
	 *            The locale of the resources
	 * @param style
	 *            The style of the resources (see {@link wicket.Session})
	 * @param key
	 *            The message key
	 * @return The unique cache id
	 */
	private String createCacheId(final Class clazz, final Locale locale, final String style,
			final String key)
	{
		String id = application.getResourceSettings().getPropertiesFactory().createResourceKey(
				clazz, locale, style)
				+ '.' + key;
		return id;
	}

	/**
	 * Helper method to create a unique id for caching previously loaded
	 * resources.
	 * 
	 * @param cacheId
	 *            The resources cache id
	 * @return The unique cache id
	 */
	private String getCachedValue(final String cacheId)
	{
		String value = (String)cachedValues.get(cacheId);
		if (value != null)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Found message key in cache: " + cacheId);
			}

			if (value == NULL)
			{
				value = null;
			}
		}
		return value;
	}

	/**
	 * Traverse the component hierachy up to the Page and add each component
	 * class to the list (stack) returned
	 * 
	 * @param component
	 *            The component to evaluate
	 * @return The stack of classes
	 */
	private List getComponentStack(final Component component)
	{
		if (component == null)
		{
			return null;
		}

		// Build search stack
		List searchStack = new ArrayList();
		searchStack.add(component.getClass());

		if (!(component instanceof Page))
		{
			MarkupContainer container = component.getParent();
			while (container != null)
			{
				searchStack.add(container.getClass());
				if (container instanceof Page)
				{
					break;
				}

				container = container.getParent();
			}
		}
		return searchStack;
	}

	/**
	 * Helper method to handle preoprty variable substituion in strings.
	 * 
	 * @param component
	 *            The component requesting a model value
	 * @param string
	 *            The string to substitute into
	 * @param model
	 *            The model
	 * @return The resulting string
	 */
	private String substitutePropertyExpressions(final Component component, final String string,
			final IModel model)
	{
		if (string != null && model != null)
		{
			return PropertyVariableInterpolator.interpolate(string, model.getObject(component));
		}
		return string;
	}

	/**
	 * For each StringResourceLoader registered with the application, load the
	 * properties file associated with the classes in the searchStack, the
	 * locale and the style. The searchStack is traversed in reverse order.
	 * <p>
	 * The property is identified by the 'key' or 'path'+'key'. 'path' is
	 * shortened (last element removed) to always represent the page relative
	 * path of the original component associate with it.
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param path
	 *            The component id path relative to the page
	 * @param searchStack
	 *            A stack of classes to get the resource for
	 * @param locale
	 *            The locale to get the resource for (optional)
	 * @param style
	 *            The style to get the resource for (optional) (see
	 *            {@link wicket.Session})
	 * @return The string resource
	 */
	private String traverseResourceLoaders(final String key, final String path,
			final List searchStack, final Locale locale, final String style)
	{
		// Search each loader in turn and return the string if it is found
		final Iterator iterator = application.getResourceSettings().getStringResourceLoaders()
				.iterator();
		String string = null;
		while (iterator.hasNext() && (string == null))
		{
			IStringResourceLoader loader = (IStringResourceLoader)iterator.next();

			String prefix = path;
			for (int i = searchStack.size() - 1; (i >= 0) && (string == null); i--)
			{
				Class clazz = (Class)searchStack.get(i);
				string = loader.loadStringResource(clazz, key, locale, style);
				if ((string == null) && (path != null) && (prefix.length() > 0))
				{
					string = loader.loadStringResource(clazz, prefix + '.' + key, locale, style);
					if (string == null)
					{
						prefix = Strings.beforeLast(prefix, '.');
					}
				}
			}
		}

		return string;
	}
}