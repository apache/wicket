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
package wicket;

import java.util.Map;

/**
 * Base class for page request implementations allowing access to request 
 * parameters.  A Request has a URL and a parameter map.  You can retrieve
 * the URL of the request with getURL().  The entire parameter map can be
 * retrieved via getParameterMap().  Individual parameters can be retrieved
 * via getParameter(String).  If multiple values are available for a given
 * parameter, they can be retrieved via getParameters(String).
 * 
 * @author Jonathan Locke
 */
public abstract class Request
{
    /**
     * Retrieves the URL of this request.
     * @return The full original request URL
     */
    public abstract String getURL();

    /**
     * Gets a map of (query) parameters sent with the request.
     * @return Map of parameters
     */
    public abstract Map getParameterMap();

    /**
     * Gets a given (query) parameter by name.
     * @param key Parameter name
     * @return Parameter value
     */
    public abstract String getParameter(final String key);

    /**
     * Gets an array of multiple parameters by name. 
     * @param key Parameter name
     * @return Parameter values
     */
    public abstract String[] getParameters(final String key);
}

///////////////////////////////// End of File /////////////////////////////////
