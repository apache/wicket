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
 * Base class for page request implementations allowing access to request parameters.
 * @author Jonathan Locke
 */
public abstract class Request
{
    /**
     * @return The full original request URL
     */
    public abstract String getURL();

    /**
     * @return Map of parameters
     */
    public abstract Map getParameterMap();

    /**
     * @param key Parameter name
     * @return Parameter value
     */
    public abstract String getParameter(final String key);

    /**
     * @param key Parameter name
     * @return Parameter values
     */
    public abstract String[] getParameters(final String key);
}

///////////////////////////////// End of File /////////////////////////////////
