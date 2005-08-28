/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
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

import javax.servlet.ServletContext;
import javax.servlet.ServletException;

import wicket.Application;
import wicket.ApplicationSettings;
import wicket.Page;
import wicket.protocol.http.servlet.ServletWebRequest;
import wicket.util.file.IResourceFinder;
import wicket.util.file.WebApplicationPath;

/**
 * This class provides a mock implementation of a Wicket HTTP based application
 * that can be used for testing. It emulates all of the functionality of an
 * HttpServlet in a controlled, single-threaded environment. It is supported
 * with mock objects for WebSession, HttpServletRequest, HttpServletResponse
 * and ServletContext.
 * <p>
 * In its most basic usage you can just create a new MockWebApplication. This
 * should be sufficient to allow you to construct components and pages and so on
 * for testing. To use certain features such as localization you must also call
 * setupRequestAndResponse().
 * <p>
 * The application takes an optional path attribute that defines a directory on
 * the disk which will correspond to the root of the WAR bundle. This can then
 * be used for locating non-application resources.
 * <p>
 * To actually test the processing of a particular page or component you can
 * also call processRequestCycle() to do all the normal work of a Wicket
 * request.
 * <p>
 * Between calling setupRequestAndResponse() and processRequestCycle() you can
 * get hold of any of the objects for initialisation. The servlet request object
 * has some handy convenience methods for initialising the request to invoke
 * certain types of pages and components.
 * <p>
 * After completion of processRequestCycle() you will probably just be testing
 * component states. However, you also have full access to the response document
 * (or binary data) and result codes via the servlet response object.
 * <p>
 * IMPORTANT NOTES
 * <ul>
 * <li>This harness is SINGLE THREADED - there is only one global session. For
 * multi-threaded testing you must do integration testing with a full
 * application server.
 * </ul>
 * 
 * @author Chris Turner
 */
public class MockWebApplication extends WebApplication
{
    /** Mock http servlet context. */
    private final MockServletContext context;

    /** The last rendered page. */
    private Page lastRenderedPage;

    /** Mock http servlet request. */
    private final MockHttpServletRequest servletRequest;

    /** Mock http servlet response. */
    private final MockHttpServletResponse servletResponse;

    /** Mock http servlet session. */
    private final MockHttpSession servletSession;

    /** Request. */
    private WebRequest wicketRequest;

    /** Response. */
    private WebResponse wicketResponse;

    /** Session. */
    private WebSession wicketSession;

    /**
     * Create the mock http application that can be used for testing.
     * 
     * @param path
     *            The absolute path on disk to the web application contents
     *            (e.g. war root) - may be null
     * @see wicket.protocol.http.MockServletContext
     */
    public MockWebApplication(final String path)
    {   
        Application.set(this);
        context = new MockServletContext(this, path);
        servletSession = new MockHttpSession(context);
        servletRequest = new MockHttpServletRequest(this, servletSession, context);
        servletResponse = new MockHttpServletResponse();
        // TODO adouma: check if the next line can be replaced by the following two        
        // wicketSession = getSession(servletRequest);
		wicketRequest = newWebRequest(servletRequest);
        wicketSession = getSession(wicketRequest, true);
        ApplicationSettings settings = getSettings();
        settings.setRenderStrategy(ApplicationSettings.ONE_PASS_RENDER);
    }

    /**
     * @see wicket.protocol.http.WebApplication#createApplicationSettings()
     */
    public ApplicationSettings createApplicationSettings()
    {
    	return new ApplicationSettings(this)
    	{
    		public IResourceFinder newResourceFinder()
    		{
    			return new WebApplicationPath(context);
    		}
    	};
    }

    /**
     * Get the page that was just rendered by the last request cycle processing.
     * 
     * @return The last rendered page
     */
    public Page getLastRenderedPage()
    {
        return lastRenderedPage;
    }

    /**
     * Get the context object so that we can apply configurations to it. This
     * method always returns an instance of <code>MockServletContext</code>,
     * so it is fine to cast the result to this class in order to get access to
     * the set methods.
     * 
     * @return The servlet context
     */
    public ServletContext getServletContext()
    {
        return context;
    }

    /**
     * Get the request object so that we can apply configurations to it.
     * 
     * @return The request object
     */
    public MockHttpServletRequest getServletRequest()
    {
        return servletRequest;
    }

    /**
     * Get the response object so that we can apply configurations to it.
     * 
     * @return The response object
     */
    public MockHttpServletResponse getServletResponse()
    {
        return servletResponse;
    }

    /**
     * Get the session object so that we can apply configurations to it.
     * 
     * @return The session object
     */
    public MockHttpSession getServletSession()
    {
        return servletSession;
    }

    /**
     * Get the wicket request object.
     * 
     * @return The wicket request object
     */
    public WebRequest getWicketRequest()
    {
        return wicketRequest;
    }

    /**
     * Get the wicket response object.
     * 
     * @return The wicket response object
     */
    public WebResponse getWicketResponse()
    {
        return wicketResponse;
    }

    /**
     * Get the wicket session.
     * 
     * @return The wicket session object
     */
    public WebSession getWicketSession()
    {
        return wicketSession;
    }

    /**
     * Create and process the request cycle using the current request and
     * response information.
     * 
     * @throws ServletException
     *             If the render cycle fails
     */
    public void processRequestCycle() throws ServletException
    {
        WebRequestCycle cycle = new WebRequestCycle(wicketSession, wicketRequest,
                wicketResponse);
        cycle.request();
        
        // handle redirects which are usually managed by the browser transparently
        final MockHttpServletResponse httpResponse = 
            	(MockHttpServletResponse)cycle.getWebResponse().getHttpServletResponse();
        
        if (httpResponse.isRedirect())
        {
            lastRenderedPage = cycle.getResponsePage();
            
            final MockHttpServletRequest httpRequest = 
                	(MockHttpServletRequest)((ServletWebRequest)cycle.getWebRequest()).getHttpServletRequest();
            
            httpRequest.setRequestToRedirectString(httpResponse.getRedirectLocation());
            // TODO adouma: check if this is a valid fix            
            // wicketSession = getSession(servletRequest);            
            wicketSession = getSession(wicketRequest, true);
            new WebRequestCycle(wicketSession, wicketRequest,
                    wicketResponse).request();
        }
        lastRenderedPage = cycle.getResponsePage();
    }

    /**
     * Create and process the request cycle using the current request and
     * response information.
     * 
     * @return A new and initialized WebRequestCyle
     */
    public WebRequestCycle createRequestCycle()
    {
        return new WebRequestCycle(wicketSession, wicketRequest, wicketResponse);
    }

    /**
     * Reset the request and the response back to a starting state and recreate
     * the necessary wicket request, response and session objects. The request
     * and response objects can be accessed and initialised at this point.
     * 
     * @throws IOException
     */
    public void setupRequestAndResponse() throws IOException
    {
        servletRequest.initialize();
        servletResponse.initialize();
        wicketRequest = new ServletWebRequest(servletRequest);
        wicketSession = getSession(wicketRequest, true);
        wicketResponse = new WebResponse(servletResponse);
    }

}