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
package wicket;

import java.util.Iterator;
import java.util.Locale;
import java.util.MissingResourceException;

import wicket.model.IModel;
import wicket.resource.loader.IStringResourceLoader;
import wicket.settings.IResourceSettings;
import wicket.util.string.AppendingStringBuffer;
import wicket.util.string.interpolator.PropertyVariableInterpolator;

/**
 * A utility class that encapsulates all of the localization related
 * functionality in a way that it can be accessed by all areas of the framework
 * in a consistent way. A singleton instance of this class is available via the
 * <code>Application</code> object.
 * <p>
 * You may register additional IStringResourceLoader to extend or replace
 * Wickets default search strategy for the properties. E.g. string resource
 * loaders which load the properties from a database. There should be no need to
 * extend Localizer.
 * 
 * @see wicket.settings.Settings#getLocalizer()
 * @see wicket.resource.loader.IStringResourceLoader
 * @see wicket.settings.Settings#getStringResourceLoaders()
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 * @todo implement properties caching
 */
public class Localizer
{
	/**
	 * Create the utils instance class backed by the configuration information
	 * contained within the supplied application object.
	 */
	public Localizer()
	{
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
	 *             If resource not found and configuration dictates that
	 *             exception should be thrown
	 */
	public String getString(final String key, final Component component, final String defaultValue)
			throws MissingResourceException
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
	public String getString(final String key, final Component component, final IModel model,
			final Locale locale, final String style, final String defaultValue)
			throws MissingResourceException
	{
		return null;
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
		// Iterate over all registered string resource loaders until the
		// property has been found
		String string = null;
		
		Iterator iter = Application.get().getResourceSettings().getStringResourceLoaders().iterator();
		while (iter.hasNext())
		{
			IStringResourceLoader loader = (IStringResourceLoader)iter.next();
			string = loader.loadStringResource(component, key);
			if (string != null)
			{
				break;
			}
		}

		// If a property value has been found, than replace the placeholder
		// and we are done
		if (string != null)
		{
			return substitutePropertyExpressions(component, string, model);
		}

		// Resource not found, so handle missing resources based on application
		// configuration
		final IResourceSettings resourceSettings = Application.get().getResourceSettings();
		if (resourceSettings.getUseDefaultOnMissingResource() && (defaultValue != null))
		{
			return defaultValue;
		}

		if (resourceSettings.getThrowExceptionOnMissingResource())
		{
			AppendingStringBuffer message = new AppendingStringBuffer("Unable to find resource: "
					+ key);
			if (component != null)
			{
				message.append(" for component: ");
				message.append(component.getPageRelativePath());
				message.append(" [class=").append(component.getClass().getName()).append("]");
			}
			throw new MissingResourceException(message.toString(), (component != null ? component
					.getClass().getName() : ""), key);
		}

		return "[Warning: String resource for '" + key + "' not found]";
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
		if ((string != null) && (model != null))
		{
			return PropertyVariableInterpolator.interpolate(string, model.getObject());
		}
		return string;
	}
}