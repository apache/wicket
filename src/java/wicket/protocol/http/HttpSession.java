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

import javax.servlet.http.HttpServletRequest;

import wicket.Application;
import wicket.Session;

/**
 * Session subclass for HTTP protocol.
 * @author Jonathan Locke
 */
public class HttpSession extends Session
{ // TODO finalize javadoc
	/** Serial Version ID */
	private static final long serialVersionUID = -7738551549126761943L;
	
	// The underlying HttpSession object
    private transient javax.servlet.http.HttpSession httpServletSession;

    /**
     * Constructor
     * @param application The application
     * @param httpServletSession The underlying servlet session
     */
    protected HttpSession(final Application application,
            final javax.servlet.http.HttpSession httpServletSession)
    {
        super(application);
        this.httpServletSession = httpServletSession;
    }

    /**
     * Gets session from request, creating a new one if it doesn't already exist
     * @param application The application object
     * @param request The http request object
     * @return The session object
     */
    static HttpSession getSession(final Application application,
            final HttpServletRequest request)
    {
        // Get session, creating if it doesn't exist
        final javax.servlet.http.HttpSession httpServletSession = request.getSession(true);

        // Get Session abstraction from httpSession attribute
        HttpSession session = (HttpSession) httpServletSession.getAttribute("session");

        if (session == null)
        {
            // Create session
            session = new HttpSession(application, httpServletSession);

            // Set the client Locale for this session
            session.setLocale(request.getLocale());

            // Attach to httpSession
            httpServletSession.setAttribute("session", session);
        }
        else
        {
            // Reattach http servlet session
            session.httpServletSession = httpServletSession;
        }

        // Set the current session to the session we just retrieved
        Session.set(session);

        return session;
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


