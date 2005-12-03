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

/**
 * This string resource loader attempts to find a single resource bundle that
 * has the same name and location as the application. If this bundle is found
 * then strings are obtained from here. This implementation is fully aware of
 * both locale and style values when trying to obtain the appropriate bundle.
 * 
 * @author Chris Turner
 * @author Juergen Donnerstag
 */
public class ClassStringResourceLoader extends AbstractStringResourceLoader
		implements
			IStringResourceLoader
{
	/** Log. */
	private static final Log log = LogFactory.getLog(ClassStringResourceLoader.class);

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
		super(application);
		
		if (clazz == null)
		{
			throw new IllegalArgumentException("Parameter 'clazz' must not be null");
		}
		this.clazz = clazz;
	}

	/**
	 * If component not null, than call the default implementation (@see
	 * AbstractStringResourceLoader#loadStringResource(Component, String,
	 * Locale, String), else use the class provided to the constructor to load
	 * the resource requested.
	 * 
	 * @param component
	 *            The component to use to find resources to be loaded
	 * @param key
	 *            The key to obtain the string for
	 * @param locale
	 *            The locale identifying the resource set to select the strings
	 *            from
	 * @param style
	 *            The (optional) style identifying the resource set to select
	 *            the strings from (see {@link wicket.Session})
	 * @return The string resource value or null if resource not found
	 */
	public String loadStringResource(final Component component, final String key,
			final Locale locale, final String style)
	{
		if (component == null)
		{
			return loadStringResourceByClass(this.clazz, key, locale, style);
		}

		return super.loadStringResource(component, key, locale, style);
	}

	/**
	 * @param clazz
	 *            The class to use to find resources to be loaded
	 * @param locale
	 *            The locale identifying the resource set to select the strings
	 *            from
	 * @param style
	 *            The (optional) style identifying the resource set to select
	 *            the strings from (see {@link wicket.Session})
	 * @return The string resource value or null if resource not found
	 */
	protected Properties getProperties(final Class clazz, final Locale locale, final String style)
	{
		// Use this.class instead of the clazz provided to the method. Hence
		// replacing a component class with a fixed class.
		return application.getPropertiesFactory().get(application, this.clazz, style, locale);
	}
}