/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.resource;

import java.util.Locale;

/**
 * A very simple ResourceLocator to  
 * locate a resource based on a path, a style, locale and an 
 * extension string. The full filename will be build like:
 * "resourcePath"_"style"_"locale in different combination"."extension".
 * While trying to find the resource it apply the following order:
 * <ol>
 * <li>"resourcePath"_"style"_"locale in different combination"."extension"</li>
 * <li>"resourcePath"_"locale in different combination"."extension"</li>
 * <li>"resourcePath"_"style"."extension"</li>
 * <li>"resourcePath"."extension"</li>
 * </ol>
 * 
 * Locale may contain a language, a country and a region or variant.
 * The following order is used to find the resource.
 * <ol>
 * <li>locale.toString() see javadoc for Locale for more details</li>
 * <li>"language"_"country"</li>
 * <li>"language"</li>
 * </ol>
 * 
 * @author Juergen Donnerstag
 */
abstract class ResourceLocator 
{ // TODO finalize javadoc
	/**
	 * Locate a resource. See above for more details.
	 * 
	 * @param resourcePath The path of the resource including its filename,
	 *      but with extension.
	 * @param style A theme or style
	 * @param locale The Locale to apply
	 * @param extensionString the filname's extensions
	 * @return null, if resource not found
	 */
    public Resource locate(final String resourcePath, final String style, 
    		final Locale locale, final String extensionString)
    {
        Resource resource = null;
        
        // 1. Try style and locale on classpath
        if ((style != null) && (locale != null))
        {
            resource = locate(resourcePath
                    + "_" + style + "_", locale, extensionString);

            if (resource != null)
            {
                return resource;
            }
        }

        // 2. Try locale only
        if (locale != null)
        {
            resource = locate(resourcePath 
                    + "_", locale, extensionString);

            if (resource != null)
            {
                return resource;
            }
        }

        // 3. Try style only        
        if (style != null)
        {
            resource = locate(resourcePath + "_" + style + extensionString);

            if (resource != null)
            {
                return resource;
            }
        }

        // 4. Try without style and without locale
        resource = locate(resourcePath + extensionString);
        return resource;
    }

    /**
     * Locate a file based on its name (potentially with a style), a 
     * locale and an extension. See above for more details on how
     * the locale is used and the order applied to find the resource.
     * 
     * @param path filename including path and style, but without locale and extension
     * @param locale the locale to apply
     * @param extension the filename's extension
     * @return null, if resource not found
     */
    private Resource locate(final String path, final Locale locale, final String extension)
    {
    	// 1. apply Locale default toString() implementation. See Locale 
    	//    javadoc for more details on that.
        Resource resource = locate(path + locale.toString() + extension);
        if (resource != null)
        {
        	return resource;
        }
        
        // 2. if country is avaible
        if ((locale.getCountry() != null) && (locale.getCountry().length() > 0))
        {
            String localeString = locale.getLanguage() + "_" + locale.getCountry();
            resource = locate(path + localeString + extension);
            if (resource != null)
            {
                return resource;
            }
        }

        // 3. if (at least the) language is available 
        if ((locale.getLanguage() != null) && (locale.getLanguage().length() > 0))
        {
            String localeString = locale.getLanguage();
            resource = locate(path + localeString + extension);
            if (resource != null)
            {
                return resource;
            }
        }

        // Not found
        return null;
    }

    /**
     * Subclass specific implementation on how to locate the file.
     * Subclasses may implement classloader or sourcePath depending
     * locators, or ...
     *  
     * @param filename the complete filename, including path, style, locale and extension
     * @return null, if resource not found.
     */
    protected abstract Resource locate(final String filename);
}
