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
package wicket.protocol.http;

import javax.servlet.http.Cookie;

import java.util.Collections;
import java.util.Map;

/**
 * A placeholder HttpRequest where all methods do nothing.  This
 * class is not accessible outside this package because it is intended
 * to be used as a singleton.  To gain access to a NullHttpRequest
 * instance, just access the static final field HttpRequest.NULL.
 * 
 * @see HttpRequest#NULL
 * @author Jonathan Locke
 */
class NullHttpRequest extends HttpRequest
{
    /**
     * Private constructor to force use of static factory method.
     */
    NullHttpRequest()
    {
        super(null);
    }

    /**
     * @see wicket.Request#getParameter(java.lang.String)
     */
    public String getParameter(String key)
    {
        return null;
    }

    /**
     * @see wicket.Request#getParameterMap()
     */
    public Map getParameterMap()
    {
        return Collections.EMPTY_MAP;
    }

    /**
     * @see wicket.Request#getParameters(java.lang.String)
     */
    public String[] getParameters(String key)
    {
        return null;
    }

    /**
     * @see wicket.Request#getURL()
     */
    public String getURL()
    {
        return "[No request URL]";
    }

    /**
     * @see wicket.protocol.http.HttpRequest#getContextPath()
     */
    public String getContextPath()
    {
        return "[No context path]";
    }

    /**
     * @see wicket.protocol.http.HttpRequest#getCookies()
     */
    public Cookie[] getCookies()
    {
        return null;
    }

    /**
     * @see wicket.protocol.http.HttpRequest#getPathInfo()
     */
    public String getPathInfo()
    {
        return "[No path info]";
    }

    /**
     * @see wicket.protocol.http.HttpRequest#getServletPath()
     */
    public String getServletPath()
    {
        return "[No servlet path]";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[NullHttpRequest]";
    }
}
