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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.ApplicationSettings;
import wicket.IApplication;
import wicket.util.lang.Classes;


/**
 * Base class for applications such as WebApplications that are based on the HTTP
 * protocol. Each HttpApplication has a name that comes from the name of the concrete
 * subclass which is instantiated. It also holds application settings which are typically
 * initialized in the subclass constructor. When GET/POST requests are made via HTTP, a
 * RequestCycle object is created from the request, response and session objects. The
 * RequestCycle's render() method is called to produce a response to the HTTP request.
 * </p>
 * <p>
 * If you want to use servlet specific configuration, e.g. using init parameters from
 * the {@link javax.servlet.ServletConfig} object, you should override the init() method
 * of {@link javax.servlet.GenericServlet}. For example:
 * <pre>
 *  public void init() throws ServletException
 *  {
 *    ServletConfig config = getServletConfig();
 *    String webXMLParameter = config.getInitParameter("myWebXMLParameter");
 *    ...
 * </pre>
 * </p>
 *
 * @see wicket.RequestCycle
 * @author Jonathan Locke
 */
public abstract class HttpApplication extends HttpServlet implements IApplication
{
    /** Log. */
    private static final Log log = LogFactory.getLog(HttpApplication.class);

    /** Name of application subclass. */
    private final String name;

    /**
     * Construct using configuraton information in XML of same name and location as class.
     */
    public HttpApplication()
    {
        log.info("Constructed HttpApplication " + getClass());
        this.name = Classes.name(getClass());
    }

    /**
     * Gets the name of this application.
     * @return Returns the name.
     */
    public final String getName()
    {
        return name;
    }

    /**
     * Gets the application settings.
     * @return Returns the settings.
     */
    public abstract ApplicationSettings getSettings();

    /**
     * Calls doGet with arguments.
     * @param servletRequest Servlet request object
     * @param servletResponse Servlet response object
     * @see HttpApplication#doGet(HttpServletRequest, HttpServletResponse)
     * @throws ServletException Thrown if something goes wrong during request handling
     * @throws IOException
     */
    protected final void doPost(final HttpServletRequest servletRequest,
            final HttpServletResponse servletResponse) throws ServletException, IOException
    {
        doGet(servletRequest, servletResponse);
    }

    /**
     * Handles servlet page requests.
     * @param servletRequest Servlet request object
     * @param servletResponse Servlet response object
     * @throws ServletException Thrown if something goes wrong during request handling
     * @throws IOException
     */
    protected final void doGet(final HttpServletRequest servletRequest,
            final HttpServletResponse servletResponse) throws ServletException, IOException
    {
        // Get session for request
        final HttpSession session = HttpSession.getSession(this, servletRequest);
        final HttpRequest request = new HttpRequest(servletRequest);
        final HttpResponse response = new HttpResponse(servletResponse);
        final HttpRequestCycle cycle = new HttpRequestCycle(this, session, request, response);

        // Render response for request cycle
        cycle.render();

        // Clear down the session thread local so that the only reference to it
        // is as a Servlet HttpSession
        HttpSession.set(null);
    }
}