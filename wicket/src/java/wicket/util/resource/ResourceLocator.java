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
 * A very simple ResourceLocator to locate a resource based on a path, 
 * a style, a locale and an extension string. The full filename will 
 * be built like: &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;.
 * <p>
 * Resource matches will be attempted in the following order:
 * <ol>
 * <li>1. &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>2. &lt;path&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>3. &lt;path&gt;_&lt;style&gt;.&lt;extension&gt;</li>
 * <li>4. &lt;path&gt;.&lt;extension&gt;</li>
 * </ol>
 * <p>
 * Locales may contain a language, a country and a region or variant.
 * Combinations of these components will be attempted in the following
 * order:
 * <ol>
 * <li>locale.toString() see javadoc for Locale for more details</li>
 * <li>&lt;language&gt;_&lt;country&gt;</li>
 * <li>&lt;language&gt;</li>
 * </ol>
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
abstract class ResourceLocator 
{
	/**
	 * Locate a resource. See class comments for more details.
	 * @param path The path of the resource without extension
	 * @param style A theme or style
	 * @param locale The Locale to apply
	 * @param extension the filname's extensions
     * @return The Resource, or null if not found.
	 */
    public Resource locate(final String path, final String style, 
    		final Locale locale, final String extension)
    {
        // 1. Try style and locale on classpath
        if (style != null && locale != null)
        {
            final Resource resource = locate(path + "_" + style + "_", locale, extension);
            if (resource != null)
            {
                return resource;
            }
        }

        // 2. Try locale only
        if (locale != null)
        {
            final Resource resource = locate(path + "_", locale, extension);
            if (resource != null)
            {
                return resource;
            }
        }

        // 3. Try style only        
        if (style != null)
        {
            final Resource resource = locate(path + "_" + style + extension);
            if (resource != null)
            {
                return resource;
            }
        }

        // 4. Try without style and without locale
        return locate(path + extension);
    }

    /**
     * Locate a file based on its path (potentially with a style), a locale and 
     * an extension. See class comments for more details on how the locale is used 
     * and the order applied to find the resource.
     * @param path Full path to resource including style, but not locale or extension
     * @param locale The locale to apply
     * @param extension The resource's extension
     * @return The Resource, or null if not found.
     */
    private Resource locate(final String path, final Locale locale, final String extension)
    {
    	// 1. Apply Locale default toString() implementation. See Locale.
        {
            final Resource resource = locate(path + locale.toString() + extension);
            if (resource != null)
            {
            	return resource;
            }
        }
        
        // 2. If country is available
        if (locale.getCountry() != null && locale.getCountry().length() > 0)
        {
            final String localeString = locale.getLanguage() + "_" + locale.getCountry();
            final Resource resource = locate(path + localeString + extension);
            if (resource != null)
            {
                return resource;
            }
        }

        // 3. If (at least the) language is available 
        if (locale.getLanguage() != null && locale.getLanguage().length() > 0)
        {
            final String localeString = locale.getLanguage();
            final Resource resource = locate(path + localeString + extension);
            if (resource != null)
            {
                return resource;
            }
        }

        // Not found
        return null;
    }

    /**
     * Subclass implementation locates the resource at the given path.
     * Different subclasses may take different approaches to the search.
     * @param path The complete path of the resource to locate
     * @return The Resource, or null if not found.
     */
    protected abstract Resource locate(final String path);
}


