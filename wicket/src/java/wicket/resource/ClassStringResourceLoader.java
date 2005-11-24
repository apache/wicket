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

import java.util.Locale;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.Component;
import wicket.util.string.Strings;
import wicket.util.value.ValueMap;

/**
 * This string resource loader attempts to find a single resource bundle that
 * has the same name and location as the application. If this bundle is found
 * then strings are obtained from here. This implemnentation is fully aware of
 * both locale and style values when trying to obtain the appropriate bundle.
 * 
 * @author Chris Turner
 */
public class ClassStringResourceLoader 
	extends AbstractStringResourceLoader
	implements IStringResourceLoader
{
	/** Log. */
	private static final Log log = LogFactory.getLog(ClassStringResourceLoader.class);

	/** Wickets application object */
	private final Application application;
	
	/** The application we are loading for. */
	private final Class clazz;

	/**
	 * Create and initialise the resource loader.
	 *
	 * @param application
	 *            Wickets application object
	 * @param clazz
	 *            The class that this resource loader is associated with
	 */
	public ClassStringResourceLoader(final Application application, final Class clazz)
	{
		if (clazz == null)
		{
			throw new IllegalArgumentException("Parameter 'clazz' must not be null");
		}
		this.application = application;
		this.clazz = clazz;
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
	public final String loadStringResource(final Component component, String key,
			final Locale locale, final String style)
	{
		// Locate previously loaded resources from the cache
		final String id = createCacheId(clazz, style, locale);
		ValueMap strings = getResourceCache(id);
		if (strings == null)
		{
			// No resources previously loaded, attempt to load them
			strings = loadResources(clazz, style, locale, id);
		}

		if (log.isDebugEnabled())
		{
			log.debug("Try to load resource from: " + id + "; key: " + key);
		}
		
		// Check the key.  If not found and if the key contains a ".", than
		// remove the first component from the path and try again.
        String value = null;
        while ((value == null) &&  (key != null) && (key.length() != 0))
        {
        	value = strings.getString(key);
            if (value != null)
            {
                if (log.isDebugEnabled())
                {
                	log.debug("Found resource from: " + id + "; key: " + key);
                }
                break;
            }
            key = Strings.afterFirst(key, '.');
        }
		
		return value;
	}
}