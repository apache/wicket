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

import javax.servlet.http.HttpServletRequest;

import wicket.Application;
import wicket.Session;
import wicket.WicketRuntimeException;

/**
 * Session subclass for HTTP protocol which holds an underlying HttpSession
 * object and provides access to that object via getHttpServletSession. A method
 * which abstracts session invalidation is also provided via invalidate().
 * 
 * @author Jonathan Locke
 */
public class HttpSession extends Session
{
    /** Serial Version ID */
    private static final long serialVersionUID = -7738551549126761943L;

    /** The underlying HttpSession object */
    private transient javax.servlet.http.HttpSession httpServletSession;

    /**
     * Gets session from request, creating a new one if it doesn't already exist
     * 
     * @param application
     *            The application object
     * @param request
     *            The http request object
     * @return The session object
     */
    static HttpSession getSession(final Application application, final HttpServletRequest request)
    {
        // Get session, creating if it doesn't exist
        final javax.servlet.http.HttpSession httpServletSession = request.getSession(true);

        // The request session object is unique per web application, but wicket requires it
        // to be unique per servlet. That is, there must be a 1..n relationship between
        // HTTP sessions (JSESSIONID) and Wicket applications.
        final String sessionAttributeName = "session" + request.getServletPath();
        
        // Get Session abstraction from httpSession attribute
        HttpSession httpSession = (HttpSession)httpServletSession.getAttribute(sessionAttributeName);

        if (httpSession == null)
        {
            // Create session using session factory
            final Session session = application.getSessionFactory().newSession();
            if (session instanceof HttpSession)
            {
                httpSession = (HttpSession)session;            	
            }
            else
            {
                throw new WicketRuntimeException("Session created by a WebApplication session factory must be a subclass of HttpSession");
            }
            
            // Save servlet session in there
            httpSession.httpServletSession = httpServletSession;

            // Set the client Locale for this session
            httpSession.setLocale(request.getLocale());

            // Attach to httpSession
            httpServletSession.setAttribute(sessionAttributeName, httpSession);
        }
        else
        {
            // Reattach http servlet session
            httpSession.httpServletSession = httpServletSession;
        }

        // Set the current session to the session we just retrieved
        Session.set(httpSession);

        return httpSession;
    }

    /**
     * Constructor
     * 
     * @param application
     *            The application
     */
    protected HttpSession(final Application application)
    {
        super(application);
    }

    /**
     * @return The underlying HttpSession object
     */
    public javax.servlet.http.HttpSession getHttpServletSession()
    {
        return httpServletSession;
    }

    /**
     * Invalidates this session
     */
    public void invalidate()
    {
        try
        {
            httpServletSession.invalidate();
        }
        catch (IllegalStateException e)
        {
            ; // ignore
        }
    }
}


