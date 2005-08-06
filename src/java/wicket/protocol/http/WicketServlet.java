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
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.ApplicationSettings;
import wicket.RequestCycle;
import wicket.Resource;
import wicket.WicketRuntimeException;
import wicket.markup.html.StaticResource;
import wicket.response.BufferedResponse;
import wicket.util.resource.IResourceStream;
import wicket.util.time.Time;

/**
 * Servlet class for all wicket applications. The specific application class to
 * instantiate should be specified to the application server via an init-params
 * argument named "applicationClassName" in the servlet declaration, which is
 * typically in a <i>web.xml </i> file. The servlet declaration may vary from
 * one application server to another, but should look something like this:
 * 
 * <pre>
 *    
 *                     &lt;servlet&gt;
 *                         &lt;servlet-name&gt;MyApplication&lt;/servlet-name&gt;
 *                         &lt;servlet-class&gt;wicket.protocol.http.WicketServlet&lt;/servlet-class&gt;
 *                         &lt;init-param&gt;
 *                             &lt;param-name&gt;applicationClassName&lt;/param-name&gt;
 *                             &lt;param-value&gt;com.whoever.MyApplication&lt;/param-value&gt;
 *                         &lt;/init-param&gt;
 *                         &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 *                      &lt;/servlet&gt;
 *     
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
 *    
 *      	public void init() throws ServletException
 *          {
 *               ServletConfig config = getServletConfig();
 *               String webXMLParameter = config.getInitParameter(&quot;myWebXMLParameter&quot;);
 *               ...
 *     
 * </pre>
 * 
 * </p>
 * In order to support frameworks like Spring, the class is non-final and the
 * variable webApplication is protected instead of private. Thus subclasses may
 * provide there own means of providing the application object.
 * 
 * @see wicket.RequestCycle
 * @author Jonathan Locke
 * @author Timur Mehrvarz
 */
public class WicketServlet extends HttpServlet
{
	/** Log. */
	private static final Log log = LogFactory.getLog(WicketServlet.class);

	/** The application this servlet is serving */
	protected WebApplication webApplication;

	/**
	 * The name of the context parameter that specifies application factory
	 * class
	 */
	public static final String APP_FACT_PARAM = "applicationFactoryClassName";

	/**
	 * Servlet initialization
	 */
	public void init()
	{
		IWebApplicationFactory factory = getApplicationFactory();

		// Construct WebApplication subclass
		this.webApplication = factory.createApplication(this);

		// Set this WicketServlet as the servlet for the web application
		this.webApplication.setWicketServlet(this);

		// Finished
		log.info("WicketServlet loaded application " + this.webApplication.getName() + " via "
				+ factory.getClass().getName() + " factory");

		// Call init method of web application
		this.webApplication.internalInit();
		this.webApplication.init();
	}

	/**
	 * Creates the web application factory instance.
	 * 
	 * If no APP_FACT_PARAM is specified in web.xml
	 * ContextParamWebApplicationFactory will be used by default.
	 * 
	 * @see ContextParamWebApplicationFactory
	 * 
	 * @return application factory instance
	 */
	protected IWebApplicationFactory getApplicationFactory()
	{
		final String appFactoryClassName = getInitParameter(APP_FACT_PARAM);

		if (appFactoryClassName == null)
		{
			// if no context param was specified we return the default factory
			return new ContextParamWebApplicationFactory();
		}
		else
		{
			try
			{
				// try to find the specified factory class
				final Class factoryClass = getClass().getClassLoader().loadClass(
						appFactoryClassName);

				if (IWebApplicationFactory.class.isAssignableFrom(factoryClass))
				{
					// instantiate the factory
					return (IWebApplicationFactory)factoryClass.newInstance();
				}
				else
				{
					throw new WicketRuntimeException("Application factory class "
							+ appFactoryClassName + " must implement IWebApplicationFactory");
				}
			}
			catch (ClassNotFoundException e)
			{
				throw new WicketRuntimeException("Unable to create application factory of class "
						+ appFactoryClassName, e);
			}
			catch (InstantiationException e)
			{
				throw new WicketRuntimeException("Unable to create application factory of class "
						+ appFactoryClassName, e);
			}
			catch (IllegalAccessException e)
			{
				throw new WicketRuntimeException("Unable to create application factory of class "
						+ appFactoryClassName, e);
			}
			catch (SecurityException e)
			{
				throw new WicketRuntimeException("Unable to create application factory of class "
						+ appFactoryClassName, e);
			}

		}
	}

	/**
	 * @see javax.servlet.http.HttpServlet#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	protected long getLastModified(final HttpServletRequest servletRequest)
	{
		final String pathInfo = servletRequest.getPathInfo();
		if ((pathInfo != null) && pathInfo.startsWith(WebRequestCycle.resourceReferencePrefix))
		{
			final String resourceReferenceKey = pathInfo
					.substring(WebRequestCycle.resourceReferencePrefix.length());

			final Resource resource = webApplication.getSharedResources().get(resourceReferenceKey);
			if (resource != null)
			{
				if (resource instanceof StaticResource)
				{
					((StaticResource)resource).setApplication(webApplication);
				}

				IResourceStream stream = resource.getResourceStream();
				// first ask the length so the content is created/accessed
				stream.length();
				Time time = stream.lastModifiedTime();
				return time != null? time.getMilliseconds():-1;
			}
		}
		return -1;
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
		// try to see if there is a redirect stored
		if (webApplication.getSettings().getRenderStrategy() == ApplicationSettings.REDIRECT_TO_BUFFER)
		{
			// TODO should we test here for
			// queryString.indexOf("IRedirectListener") ?
			// only such urls should have a bufferedresponse.
			String requestUri = servletRequest.getRequestURI() + "?"
					+ servletRequest.getQueryString();

			BufferedResponse bufferedResponse = (BufferedResponse)webApplication
					.getBufferedResponse(servletRequest, requestUri);

			if (bufferedResponse != null)
			{
				// got a buffered response; now write it
				servletResponse.setContentLength(bufferedResponse.getContentLength());
				servletResponse.setContentType(bufferedResponse.getContentType());

				// PrintWriter pw = servletResponse.getWriter();
				// pw.write(bufferedResponse.getString());
				// pw.close();
				final OutputStream os = servletResponse.getOutputStream();
				os.write(bufferedResponse.getBytes());
				os.close();

				return;
			}
		}

		// If the request does not provide information about the encoding of its
		// body (which includes POST parameters), than assume the default encoding
		// as defined by the wicket application. Bare in mind that the encoding of
		// the request usually is equal to the previous response. However it is a
		// known bug of IE that it does not provide this information. Please see
		// the wiki for more details and why all other browser deliberately copied
		// that bug.
		if (servletRequest.getCharacterEncoding() == null)
		{
			try
			{
				// The encoding defined by the wicket settings is used to encode
				// the responses. Thus, it is reasonable to assume the request
				// has the same encoding. This is especially important for
				// forms and form parameters.
				servletRequest.setCharacterEncoding(webApplication.getSettings()
						.getResponseRequestEncoding());
			}
			catch (UnsupportedEncodingException ex)
			{
				throw new WicketRuntimeException(ex.getMessage());
			}
		}

		// Get session for request
		final WebSession session = webApplication.getSession(servletRequest);

		// create a new webrequest
		final WebRequest request = webApplication.newWebRequest(servletRequest);

		// Create a response object and set the output encoding according to
		// wicket's application setttings.
		final WebResponse response = webApplication.newWebResponse(servletResponse);
		response.setCharacterEncoding(webApplication.getSettings().getResponseRequestEncoding());

		try
		{
			// create a new request cycle
			RequestCycle cycle = session.newRequestCycle(request, response);

			// Process request
			cycle.request();
		}
		finally
		{
			// Close response
			response.close();
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
}