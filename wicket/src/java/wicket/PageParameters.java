/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket;

import java.util.Map;

import wicket.util.value.ValueMap;

/**
 * A typesafe abstraction and container for parameters to a requested page.
 * Page parameters in HTTP are query string values in the request URL.  
 * In other protocols, the parameters to a page might come from some other 
 * source.
 * <p>
 * Pages which take a PageParameters object as an argument to their constructor
 * can be accessed directly from a URL and are known as "bookmarkable" pages
 * since the URL is stable across sessions and can be stored in a browser's 
 * bookmark database.   
 * 
 * @author Jonathan Locke
 */
public final class PageParameters extends ValueMap
{
	private static final long serialVersionUID = 1L;
	
	/**
     * Null value for page parameters
     */
    public static final PageParameters NULL = new PageParameters();

    /**
     * Constructor
     */
    public PageParameters()
    {
    }

    /**
     * @see ValueMap#ValueMap(java.util.Map)
     */
    public PageParameters(final Map parameterMap)
    {
        super(parameterMap);
    }

    /**
     * @see ValueMap#ValueMap(String)
     */
    public PageParameters(final String keyValuePairs)
    {
        super(keyValuePairs);
    }
}


