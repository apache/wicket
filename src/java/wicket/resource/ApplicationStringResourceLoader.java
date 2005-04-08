/*
 * $Id: ApplicationStringResourceLoader.java,v 1.5 2005/01/19 08:07:57
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
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.Page;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.value.ValueMap;
import EDU.oswego.cs.dl.util.concurrent.ConcurrentReaderHashMap;

/**
 * This string resource loader attempts to find a single resource bundle that
 * has the same name and location as the application. If this bundle is found
 * then strings are obtained from here. This implemnentation is fully aware of
 * both locale and style values when trying to obtain the appropriate bundle.
 * 
 * @author Chris Turner
 */
public class ApplicationStringResourceLoader implements IStringResourceLoader
{
	/** Log. */
	private static final Log log = LogFactory.getLog(Page.class);

	/** The application we are loading for. */
	private Application application;

	/** The cache of previously loaded resources. */
	private Map resourceCache;

	/**
	 * Create and initialise the resource loader.
	 * 
	 * @param application
	 *            The application that this resource loader is associated with
	 */
	public ApplicationStringResourceLoader(final Application application)
	{
		if (application == null)
		{
			throw new IllegalArgumentException("Application cannot be null");
		}
		this.application = application;
		this.resourceCache = new ConcurrentReaderHashMap();
	}

	/**
	 * Get the string resource for the given combination of key, locale and
	 * style. The information is obtained from a single application-wide
	 * resource bundle.
	 * 
	 * @param component
	 *            Not used - can be null
	 * @param key
	 *            The key to obtain the string for
	 * @param locale
	 *            The locale identifying the resource set to select the strings
	 *            from
	 * @param style
	 *            The (optional) style identifying the resource set to select
	 *            the strings from (see {@link wicket.Session})
	 * @return The string resource value or null if resource not loaded
	 */
	public final String loadStringResource(final Component component, final String key,
			final Locale locale, final String style)
	{
		// Locate previously loaded resources from the cache
		final String id = createCacheId(style, locale);
		ValueMap strings = (ValueMap)resourceCache.get(id);
		if (strings == null)
		{
			// No resources previously loaded, attempt to load them
			strings = loadResources(style, locale, id);
		}

		return strings.getString(key);
	}

	/**
	 * Helper method to do the actual loading of resources if required.
	 * 
	 * @param style
	 *            The style to load resources for (see {@link wicket.Session})
	 * @param locale
	 *            The locale to load reosurces for
	 * @param id
	 *            The cache id to use
	 * @return The map of loaded resources
	 */
	private synchronized ValueMap loadResources(final String style, final Locale locale,
			final String id)
	{
		// Make sure someone else didn't load our resources while we were
		// waiting for the synchronized lock on the method
		ValueMap strings = (ValueMap)resourceCache.get(id);
		if (strings != null)
		{
			return strings;
		}

		// Do the resource load
		final Properties properties = new Properties();
		final IResourceStream resource = application.getResourceStreamLocator().locate(application.getClass(),
				style, locale, "properties");
		if (resource != null)
		{
			try
			{
				try
				{
					properties.load(new BufferedInputStream(resource.getInputStream()));
					strings = new ValueMap(properties);
				}
				finally
				{
					resource.close();
				}
			}
			catch (ResourceStreamNotFoundException e)
			{
				log.warn("Unable to find resource " + resource, e);
				strings = ValueMap.EMPTY_MAP;
			}
			catch (IOException e)
			{
				log.warn("Unable to access resource " + resource, e);
				strings = ValueMap.EMPTY_MAP;
			}
		}
		else
		{
			// Unable to load resources
			strings = ValueMap.EMPTY_MAP;
		}

		resourceCache.put(id, strings);
		return strings;
	}

	/**
	 * Helper method to create a unique id for caching previously loaded
	 * resources.
	 * 
	 * @param style
	 *            The style of the resources (see {@link wicket.Session})
	 * @param locale
	 *            The locale of the resources
	 * @return The unique cache id
	 */
	private String createCacheId(final String style, final Locale locale)
	{
		final StringBuffer buffer = new StringBuffer();
		buffer.append(application.getClass().getName());
		if (style != null)
		{
			buffer.append('.');
			buffer.append(style);
		}
		if (locale != null)
		{
			buffer.append('.');
			buffer.append(locale.toString());
		}
		final String id = buffer.toString();
		return id;
	}
}