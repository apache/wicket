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

import wicket.WicketRuntimeException;

/**
 * Servlet class for all wicket applications. The specific application class to
 * instantiate should be specified to the application server via an init-params
 * argument named "applicationClassName" in the servlet declaration, which is
 * typically in a <i>web.xml </i> file. The servlet declaration may vary from
 * one application server to another, but should look something like this:
 * 
 * <pre>
 *        &lt;servlet&gt;
 *            &lt;servlet-name&gt;MyApplication&lt;/servlet-name&gt;
 *            &lt;servlet-class&gt;wicket.protocol.http.WicketServlet&lt;/servlet-class&gt;
 *            &lt;init-param&gt;
 *                &lt;param-name&gt;applicationClassName&lt;/param-name&gt;
 *                &lt;param-value&gt;com.whoever.MyApplication&lt;/param-value&gt;
 *            &lt;/init-param&gt;
 *            &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *         &lt;/servlet&gt;
 * </pre>
 * 
 * Note that the applicationClassName parameter you specify must be the fully
 * qualified name of a class that extends WebApplication. If your class cannot
 * be found, does not extend WebApplication or cannot be instantiated, a runtime
 * exception of type WicketRuntimeException will be thrown.
 * <p>
 * When GET/POST requests are made via HTTP, an WebRequestCycle object is
 * created from the request, response and session objects (after wrapping them
 * in the appropriate wicket wrappers). The RequestCycle's render() method is
 * then called to produce a response to the HTTP request.
 * <p>
 * If you want to use servlet specific configuration, e.g. using init parameters
 * from the {@link javax.servlet.ServletConfig}object, you should override the
 * init() method of {@link javax.servlet.GenericServlet}. For example:
 * 
 * <pre>
 *           public void init() throws ServletException
 *           {
 *               ServletConfig config = getServletConfig();
 *               String webXMLParameter = config.getInitParameter(&quot;myWebXMLParameter&quot;);
 *               ...
 * </pre>
 * 
 * </p>
 * In order to support frameworks like Spring, the class is none-final and the variable
 * webApplication is protected instead of private. Thus subclasses may provide there
 * own means of providing the application object.
 * 
 * @see wicket.RequestCycle
 * @author Jonathan Locke
 */
public class WicketServlet extends HttpServlet
{
    /** Log. */
    private static final Log log = LogFactory.getLog(WicketServlet.class);

    /** The application this servlet is serving */
    protected WebApplication webApplication;

    /**
     * Servlet initialization
     */
    public void init()
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
                log.info("WicketServlet loaded application " + applicationClass.getName());
                
                // Call init method of web application
                this.webApplication.init();
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
        final WebSession session = WebSession.getSession(webApplication, servletRequest);
        final WebRequest request = new WebRequest(servletRequest);
        final WebResponse response = webApplication.getSettings().getBufferResponse() ? 
                new BufferedWebResponse(servletResponse) : new WebResponse(servletResponse);
        final WebRequestCycle cycle = new WebRequestCycle(webApplication, session, request,
                response);

        // Render response for request cycle
        cycle.render();

        // Clear down the session thread local so that the only reference to it
        // is as a Servlet WebSession
        WebSession.set(null);
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
}