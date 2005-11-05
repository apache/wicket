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
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.MarkupContainer;
import wicket.Page;
import wicket.markup.html.WebComponent;
import wicket.markup.html.WebMarkupContainer;
import wicket.markup.html.WebPage;
import wicket.util.concurrent.ConcurrentReaderHashMap;
import wicket.util.resource.IResourceStream;
import wicket.util.resource.ResourceStreamNotFoundException;
import wicket.util.value.ValueMap;

/**
 * Abstract base class for StringResourceLoader
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 */
public abstract class AbstractStringResourceLoader
{
	/** Log. */
	private static final Log log = LogFactory.getLog(AbstractStringResourceLoader.class);

	/** The cache of previously loaded resources. */
	private final static Map resourceCache = new ConcurrentReaderHashMap();

	/**
	 * Create and initialise the resource loader.
	 */
	public AbstractStringResourceLoader()
	{
	}

	protected final ValueMap getResourceCache(final String key)
	{
		return (ValueMap)resourceCache.get(key);
	}
	
	/**
	 * Check the supplied class to see if it is one that we shouldn't bother
	 * further searches up the class hierarchy for properties.
	 *
	 * @param clazz
	 *            The class to check
	 * @return
	 *            Whether to stop the search
	 */
	protected boolean isStopResourceSearch(final Class clazz) 
	{
		if (clazz == null || clazz.equals(Object.class))
		{
			return true;
		}

		// Stop at all html markup base classes
		if (clazz.equals(WebPage.class) || clazz.equals(WebMarkupContainer.class) ||
				clazz.equals(WebComponent.class))
		{
			return true;
		}

		// Stop at all wicket base classes
		return clazz.equals(Page.class) || clazz.equals(MarkupContainer.class) ||
				clazz.equals(Component.class);
	}

	/**
	 * Helper method to do the actual loading of resources if required.
	 *
	 * @param componentClass
	 *            The class that resources are bring loaded for
	 * @param style
	 *            The style to load resources for (see {@link wicket.Session})
	 * @param locale
	 *            The locale to load reosurces for
	 * @param id
	 *            The cache id to use
	 * @return The map of loaded resources
	 */
	protected synchronized ValueMap loadResources(final Class componentClass,
			final String style, final Locale locale, final String id)
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
		final IResourceStream resource = Application.get().getResourceStreamLocator().locate(
				componentClass, style, locale, "properties");
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
	 * @param componentClass
	 *            The component class that the resources are being loaded for
	 * @param style
	 *            The style of the resources (see {@link wicket.Session})
	 * @param locale
	 *            The locale of the resources
	 * @return The unique cache id
	 */
	protected String createCacheId(final Class componentClass, final String style, final Locale locale)
	{
		final StringBuffer buffer = new StringBuffer(componentClass.getName());
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