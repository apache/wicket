/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.protocol.http;

import java.io.IOException;
import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * Please use {@link WicketFilter} if possible instead of this servlet.
 * 
 * Servlet class for all wicket applications. The specific application class to
 * instantiate should be specified to the application server via an init-params
 * argument named "applicationClassName" in the servlet declaration, which is
 * typically in a <i>web.xml </i> file. The servlet declaration may vary from
 * one application server to another, but should look something like this:
 * 
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;MyApplication&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;wicket.protocol.http.WicketServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;applicationClassName&lt;/param-name&gt;
 *     &lt;param-value&gt;com.whoever.MyApplication&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 * </pre>
 * 
 * Note that the applicationClassName parameter you specify must be the fully
 * qualified name of a class that extends WebApplication. If your class cannot
 * be found, does not extend WebApplication or cannot be instantiated, a runtime
 * exception of type WicketRuntimeException will be thrown.
 * </p>
 * As an alternative, you can configure an application factory instead. This
 * looks like:
 * 
 * <pre>
 * &lt;init-param&gt;
 *   &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *   &lt;param-value&gt;teachscape.platform.web.wicket.SpringApplicationFactory&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre>
 * 
 * and it has to satisfy interface
 * {@link wicket.protocol.http.IWebApplicationFactory}.
 * 
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
 * public void init() throws ServletException
 * {
 *     ServletConfig config = getServletConfig();
 *     String webXMLParameter = config.getInitParameter(&quot;myWebXMLParameter&quot;);
 *     ...
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
 * @author Juergen Donnerstag
 * @author Igor Vaynberg (ivaynberg)
 * 
 */
public class WicketServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/** Log. */
	private static final Log log = LogFactory.getLog(WicketServlet.class);

	/** The WicketFilter where all the handling is done in */
	protected WicketFilter wicketFilter;

	/**
	 * Construct.
	 */
	public WicketServlet() {
		// log warning
		log.info("********************************************");
		log.info("DEPRECATED! Please use WicketFilter instead.");
		log.info("********************************************");
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
		wicketFilter.doGet(servletRequest, servletResponse);
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
		wicketFilter.doGet(servletRequest, servletResponse);
	}

	/**
	 * Servlet initialization
	 */
	public void init() throws ServletException
	{
		wicketFilter = new WicketFilter();
		wicketFilter.init(new FilterConfig()
		{
			/**
			 * @see javax.servlet.FilterConfig#getServletContext()
			 */
			public ServletContext getServletContext()
			{
				return WicketServlet.this.getServletContext();
			}

			/**
			 * @see javax.servlet.FilterConfig#getInitParameterNames()
			 */
			public Enumeration getInitParameterNames()
			{
				return WicketServlet.this.getInitParameterNames();
			}

			/**
			 * @see javax.servlet.FilterConfig#getInitParameter(java.lang.String)
			 */
			public String getInitParameter(String name)
			{
				if (WicketFilter.FILTER_PATH_PARAM.equals(name))
				{
					return WicketFilter.SERVLET_PATH_HOLDER;
				}
				return WicketServlet.this.getInitParameter(name);
			}

			/**
			 * @see javax.servlet.FilterConfig#getFilterName()
			 */
			public String getFilterName()
			{
				return WicketServlet.this.getServletName();
			}
		});
	}

	/**
	 * Servlet cleanup.
	 */
	public void destroy()
	{
		wicketFilter.destroy();
		wicketFilter = null;
	}

	/**
	 * @see javax.servlet.http.HttpServlet#getLastModified(javax.servlet.http.HttpServletRequest)
	 */
	protected long getLastModified(final HttpServletRequest servletRequest)
	{
		return wicketFilter.getLastModified(servletRequest);
	}
}
