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

import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.MissingResourceException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.model.IModel;
import wicket.resource.IPropertiesReloadListener;
import wicket.resource.IStringResourceLoader;
import wicket.util.concurrent.ConcurrentReaderHashMap;
import wicket.util.string.interpolator.PropertyVariableInterpolator;

/**
 * Utility class that encapsulates all of the localization related functionality
 * in a way that is can be accessed by all areas of the framework in a
 * consistent way. A singleton instance of this class is available via the
 * <code>Application</code> object.
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 * @see Application#getLocalizer()
 */
public class Localizer
{
	/** Log */
	private static final Log log = LogFactory.getLog(Localizer.class);

	/** The application and its settings to use to control the utils. */
	private Application application;

	/** Because properties search can be expensive, we cache the value */
	private Map cachedValues = new ConcurrentReaderHashMap();

	/** ConcurrentReaderHashMap does not allow null values. This is a substitute */
	private static final String NULL = new String();

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
		application.getPropertiesFactory().addListener(new IPropertiesReloadListener()
		{
			public void propertiesLoaded(final String key)
			{
				// Remove all cached values. Unfortunately I did not yet
				// find a proper way (which is easy and clean to implement)
				// which selectively removes just the cache entries
				// affected. Hence they all get removed. Actually that
				// is less worse as it may sound, because type
				// Properties does cache them as well. We only have
				// to walk the properties resolution path once again.
				// And, the feature of reloading the properties file
				// is usually activated during development only and
				// for production. Hence, it affect development only.
				cachedValues.clear();
			}
		});
	}

	/**
	 * @see #getString(String, Component, IModel, Locale, String, String)
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param component
	 *            The component to get the resource for (optional)
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
	 *            The component to get the resource for (optional)
	 * @param model
	 *            The model to use for OGNL substitutions in the strings
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
	 * @see #getString(String, Component, IModel, Locale, String, String)
	 * 
	 * @param key
	 *            The key to obtain the resource for
	 * @param component
	 *            The component to get the resource for (optional)
	 * @param model
	 *            The model to use for OGNL substitutions in the strings
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
	 *            The component to get the resource for (optional)
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
	 *            The model to use for OGNL substitutions in the strings
	 *            (optional)
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
		// The key value
		String string = null;

		// Get application settings
		final ApplicationSettings settings = application.getSettings();

		// If value is cached already ...
		String id = createCacheId(component, locale, style, key);
		if (cachedValues.containsKey(id))
		{
			if (log.isDebugEnabled())
			{
				log.debug("Found message key in cache: " + id);
			}
			string = (String)cachedValues.get(id);
			if (string == NULL)
			{
				string = null;
			}
		}
		else
		{
			// Search each loader in turn and return the string if it is found
			final Iterator iterator = settings.getStringResourceLoaders().iterator();
			while (iterator.hasNext())
			{
				IStringResourceLoader loader = (IStringResourceLoader)iterator.next();
				string = loader.loadStringResource(component, key, locale, style);
				if (string != null)
				{
					break;
				}
			}

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
			return substituteOgnl(component, string, model);
		}

		// Resource not found, so handle missing resources based on application
		// configuration
		if (settings.getUseDefaultOnMissingResource() && (defaultValue != null))
		{
			return defaultValue;
		}

		if (settings.getThrowExceptionOnMissingResource())
		{
			throw new MissingResourceException("Unable to find resource: " + key, getClass()
					.getName(), key);
		}
		else
		{
			return "[Warning: String resource for '" + key + "' not found]";
		}
	}

	/**
	 * Helper method to handle OGNL variable substituion in strings.
	 * 
	 * @param component
	 *            The component requesting a model value
	 * @param string
	 *            The string to substitute into
	 * @param model
	 *            The model
	 * @return The resulting string
	 */
	private String substituteOgnl(final Component component, final String string, final IModel model)
	{
		if (string != null && model != null)
		{
			return PropertyVariableInterpolator.interpolate(string, model.getObject(component));
		}
		return string;
	}

	/**
	 * Helper method to create a unique id for caching previously loaded
	 * resources.
	 * 
	 * @param component
	 *            The component that the resources are being loaded for
	 * @param locale
	 *            The locale of the resources
	 * @param style
	 *            The style of the resources (see {@link wicket.Session})
	 * @param key
	 *            The message key
	 * @return The unique cache id
	 */
	private String createCacheId(final Component component, final Locale locale,
			final String style, final String key)
	{
		String id = application.getPropertiesFactory().createResourceKey(
				component != null ? component.getClass() : null, locale, style)
				+ '.' + key;
		return id;
	}

	/**
	 * Remove all cached properties
	 */
	public final void clearCache()
	{
		this.cachedValues.clear();
	}
}