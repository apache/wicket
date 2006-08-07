/*
 * $Id: ApplicationStringResourceLoader.java,v 1.5 2005/01/19 08:07:57
 * jonathanlocke Exp $ $Revision$ $Date: 2006-04-17 22:02:21 +0200 (Mo,
 * 17 Apr 2006) $
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
package wicket.resource.loader;

import java.util.Locale;

import wicket.Application;

/**
 * Wicket's default string resource loaders are NOT behaving like Java Bundles
 * in the sense that once a proper properties files has been found but it does
 * not contain the key you are looking for, Wicket will not continue iterating
 * over the remaing properties file for the same class to check any of these
 * contain the key. The reason why Java's BundleResource can not be used is
 * because with Wicket the locale, the style and the variation must be evaluated
 * as well.
 * <p>
 * This ResourceLoader is a all in one. It first tries to resolve through the 
 * class that is given with all the possible style and location variations.
 * Then if still not found it will fall back on the Applications class and tries
 * to load the resources through that one.
 * <p>
 * E.g.
 * 
 * <pre>
 *   1) Application_myskin_fi.properties
 *        message1=AAAAAAAAA
 *   2) Application_myskin.properties
 *        message2=BBBBBBBB
 *   3) Application.properties
 *        message3=CCCCCCC
 * </pre>
 * 
 * Component.getString("message3") should correctly read from file 3.
 * 
 * TODO Post 1.2: This should become Wicket's default behavior
 * 
 * @author Marco Geier
 * @author Juergen Donnerstag
 * @author Johan Compagner
 */
public class WicketBundleStringResourceLoader extends AbstractStringResourceLoader
{
	/**
	 * Create and initialise the resource loader.
	 * 
	 * @param application
	 *            Wickets application object
	 */
	public WicketBundleStringResourceLoader(Application application)
	{
		super(application);
	}

	/**
	 * 
	 * @see wicket.resource.loader.ClassStringResourceLoader#loadStringResource(java.lang.Class,
	 *      java.lang.String, java.util.Locale, java.lang.String)
	 */
	@Override
	public String loadStringResource(Class clazz, String key, Locale locale, String style)
	{
		String value = super.loadStringResource(clazz, key, locale, style);
		if (value == null)
		{
			// only try without style if style was not null in the first call.
			if(style != null) value = super.loadStringResource(clazz, key, locale, null);
			if (value == null)
			{
				// only try without locale if it was not null in the first call.
				if(locale != null) value = super.loadStringResource(clazz, key, null, style);
				// only try without locale and style if both where not null in the first call.
				if (value == null && style != null && locale != null ) 
				{
					value = super.loadStringResource(clazz, key, null, null);
				}
			}
		}
		// as a last resort look if the key can be found for the application class by this same loader.
		if( value == null && clazz != application.getClass())
		{
			return loadStringResource(application.getClass(), key, locale, style);
		}
		return value;
	}
}