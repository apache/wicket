/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.protocol.http;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import wicket.Request;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Subclass of Request for HTTP requests. Holds request parameters as well as
 * the underlying HttpServletRequest object.
 * 
 * @author Jonathan Locke
 */
public class HttpRequest extends Request
{
    // TODO finalize javadoc

    /** Null HttpRequest object that does nothing */
    public static final HttpRequest NULL = new NullHttpRequest();
    
    /** Servlet request information. */
    private final HttpServletRequest servletRequest;

    /**
     * Package private constructor.
     * 
     * @param servletRequest
     *            The servlet request information
     */
    HttpRequest(final HttpServletRequest servletRequest)
    {
        this.servletRequest = servletRequest;
    }

    /**
     * Gets the servlet context path.
     * 
     * @return Servlet context path
     */
    public String getContextPath()
    {
        return servletRequest.getContextPath();
    }

    /**
     * Gets any cookies for request.
     * 
     * @return Any cookies for this request
     */
    public Cookie[] getCookies()
    {
        try
        {
            return servletRequest.getCookies();
        }
        catch (NullPointerException ex)
        {
            // Ignore any app server problem here
        }

        return new Cookie[0];
    }

    /**
     * Returns the preferred <code>Locale</code> that the client will accept
     * content in, based on the Accept-Language header. If the client request
     * doesn't provide an Accept-Language header, this method returns the
     * default locale for the server.
     * 
     * @return the preferred <code>Locale</code> for the client
     */
    public Locale getLocale()
    {
        return servletRequest.getLocale();
    }

    /**
     * Returns an <code>Enumeration</code> of <code>Locale</code> objects
     * indicating, in decreasing order starting with the preferred locale, the
     * locales that are acceptable to the client based on the Accept-Language
     * header. If the client request doesn't provide an Accept-Language header,
     * this method returns an <code>Enumeration</code> containing one
     * <code>Locale</code>, the default locale for the server.
     * 
     * @return An <code>Enumeration</code> of preferred <code>Locale</code>
     *         objects for the client
     */
    public Enumeration getLocales()
    {
        return servletRequest.getLocales();
    }

    /**
     * Gets the request parameter with the given key.
     * 
     * @param key
     *            Parameter name
     * @return Parameter value
     */
    public String getParameter(final String key)
    {
        return servletRequest.getParameter(key);
    }

    /**
     * Gets the request parameters.
     * 
     * @return Map of parameters
     */
    public Map getParameterMap()
    {
        final Map map = new HashMap();

        for (final Enumeration enumeration = servletRequest.getParameterNames(); enumeration
                .hasMoreElements();)
        {
            final String name = (String)enumeration.nextElement();
            map.put(name, servletRequest.getParameter(name));
        }

        return map;
    }

    /**
     * Gets the request parameters with the given key.
     * 
     * @param key
     *            Parameter name
     * @return Parameter values
     */
    public String[] getParameters(final String key)
    {
        return servletRequest.getParameterValues(key);
    }

    /**
     * Gets the path info if any.
     * 
     * @return Any servlet path info
     */
    public String getPathInfo()
    {
        return servletRequest.getPathInfo();
    }

    /**
     * Gets the servlet path.
     * 
     * @return Servlet path
     */
    public String getServletPath()
    {
        return servletRequest.getServletPath();
    }

    /**
     * Gets the wrapped http servlet request object.
     * 
     * @return the wrapped http serlvet request object.
     */
    public final HttpServletRequest getServletRequest()
    {
        return servletRequest;
    }

    /**
     * Gets the request url.
     * 
     * @return Request URL
     */
    public String getURL()
    {
        String url = servletRequest.getContextPath() + servletRequest.getServletPath();
        final String pathInfo = servletRequest.getPathInfo();

        if (pathInfo != null)
        {
            url += pathInfo;
        }

        String queryString = servletRequest.getQueryString();

        if (queryString != null)
        {
            url += ("?" + queryString);
        }

        return url;
    }

    /**
     * @see java.lang.Object#toString()
     */
    public String toString()
    {
        return "[method = " + servletRequest.getMethod() + ", protocol = "
                + servletRequest.getProtocol() + ", requestURL = " + servletRequest.getRequestURL()
                + ", contentType = " + servletRequest.getContentType() + ", contentLength = "
                + servletRequest.getContentLength() + ", contextPath = "
                + servletRequest.getContextPath() + ", pathInfo = " + servletRequest.getPathInfo()
                + ", requestURI = " + servletRequest.getRequestURI() + ", servletPath = "
                + servletRequest.getServletPath() + ", pathTranslated = "
                + servletRequest.getPathTranslated() + "]";
    }
}


