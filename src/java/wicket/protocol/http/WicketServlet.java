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

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WebApplication;
import wicket.WicketRuntimeException;

/**
 * Base class for applications such as WebApplications that are based on the
 * HTTP protocol. Each WicketServlet has a name that comes from the name of the
 * concrete subclass which is instantiated. It also holds application settings
 * which are typically initialized in the subclass constructor. When GET/POST
 * requests are made via HTTP, a RequestCycle object is created from the
 * request, response and session objects. The RequestCycle's render() method is
 * called to produce a response to the HTTP request.
 * </p>
 * <p>
 * If you want to use servlet specific configuration, e.g. using init parameters
 * from the {@link javax.servlet.ServletConfig}object, you should override the
 * init() method of {@link javax.servlet.GenericServlet}. For example:
 * 
 * <pre>
 * 
 *       public void init() throws ServletException
 *       {
 *         ServletConfig config = getServletConfig();
 *         String webXMLParameter = config.getInitParameter(&quot;myWebXMLParameter&quot;);
 *         ...
 *  
 * </pre>
 * 
 * </p>
 * 
 * @see wicket.RequestCycle
 * @author Jonathan Locke
 */
public abstract class WicketServlet extends HttpServlet
{
    /** Log. */
    private static final Log log = LogFactory.getLog(WicketServlet.class);

    /** The application this servlet is serving */
    private final WebApplication webApplication;

    /**
     * Constructor
     */
    public WicketServlet()
    {
        final String applicationClassName = getInitParameter("applicationClassName");
        try
        {
            final Class applicationClass = Class.forName(applicationClassName);
            if (WebApplication.class.isAssignableFrom(applicationClass))
            {
                // Construct WebApplication subclass
                this.webApplication = (WebApplication)applicationClass.newInstance();

                // Set this WicketServlet as the servlet for the web application
                this.webApplication.setWicketServlet(this);

                // Finished
                log.info("Successfully constructed WicketServlet for application class "
                        + applicationClass);
            }
            else
            {
                throw new WicketRuntimeException("Application class " + applicationClassName
                        + " must be a subclass of WebApplication");
            }
        }
        catch (ClassNotFoundException e)
        {
            throw new WicketRuntimeException("Unable to create application of class "
                    + applicationClassName, e);
        }
        catch (InstantiationException e)
        {
            throw new WicketRuntimeException("Unable to create application of class "
                    + applicationClassName, e);
        }
        catch (IllegalAccessException e)
        {
            throw new WicketRuntimeException("Unable to create application of class "
                    + applicationClassName, e);
        }
        catch (SecurityException e)
        {
            throw new WicketRuntimeException("Unable to create application of class "
                    + applicationClassName, e);
        }
    }

    /**
     * Calls doGet with arguments.
     * 
     * @param servletRequest
     *            Servlet request object
     * @param servletResponse
     *            Servlet response object
     * @see WicketServlet#doGet(HttpServletRequest, HttpServletResponse)
     * @throws ServletException
     *             Thrown if something goes wrong during request handling
     * @throws IOException
     */
    public final void doPost(final HttpServletRequest servletRequest,
            final HttpServletResponse servletResponse) throws ServletException, IOException
    {
        doGet(servletRequest, servletResponse);
    }

    /**
     * Handles servlet page requests.
     * 
     * @param servletRequest
     *            Servlet request object
     * @param servletResponse
     *            Servlet response object
     * @throws ServletException
     *             Thrown if something goes wrong during request handling
     * @throws IOException
     */
    public final void doGet(final HttpServletRequest servletRequest,
            final HttpServletResponse servletResponse) throws ServletException, IOException
    {
        // Get session for request
        final HttpSession session = HttpSession.getSession(webApplication, servletRequest);
        final HttpRequest request = new HttpRequest(servletRequest);
        final HttpResponse response = new HttpResponse(servletResponse);
        final HttpRequestCycle cycle = new HttpRequestCycle(webApplication, session, request,
                response);

        // Render response for request cycle
        cycle.render();

        // Clear down the session thread local so that the only reference to it
        // is as a Servlet HttpSession
        HttpSession.set(null);
    }
}