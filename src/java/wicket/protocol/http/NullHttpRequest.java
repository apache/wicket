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
 * A dummy request.
 * @author Jonathan Locke
 */
public class NullHttpRequest extends HttpRequest
{
    /** singleton instance. */
    private static NullHttpRequest NULL = new NullHttpRequest();

    /**
     * Constructor.
     */
    private NullHttpRequest()
    {
        super(null);
    }

    /**
     * Gets the singleton instance.
     * @return the singleton instance
     */
    public static NullHttpRequest getInstance()
    {
        return NULL;
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
        return "[no request url]";
    }

    /**
     * @see wicket.protocol.http.HttpRequest#getContextPath()
     */
    public String getContextPath()
    {
        return "[no context path]";
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
        return "[no path info]";
    }

    /**
     * @see wicket.protocol.http.HttpRequest#getServletPath()
     */
    public String getServletPath()
    {
        return "[no servlet path]";
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[NullHttpRequest]";
    }
}

///////////////////////////////// End of File /////////////////////////////////
