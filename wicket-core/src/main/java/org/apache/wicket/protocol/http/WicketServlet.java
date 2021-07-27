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
package org.apache.wicket.protocol.http;

import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;

import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Please use {@link WicketFilter} if you require advanced chaining of resources.
 * 
 * <p>
 * Servlet class for all wicket applications. The specific application class to instantiate should
 * be specified to the application server via an init-params argument named "applicationClassName"
 * in the servlet declaration, which is typically in a <i>web.xml </i> file. The servlet declaration
 * may vary from one application server to another, but should look something like this:
 * 
 * <pre>
 * &lt;servlet&gt;
 *   &lt;servlet-name&gt;MyApplication&lt;/servlet-name&gt;
 *   &lt;servlet-class&gt;org.apache.wicket.protocol.http.WicketServlet&lt;/servlet-class&gt;
 *   &lt;init-param&gt;
 *     &lt;param-name&gt;applicationClassName&lt;/param-name&gt;
 *     &lt;param-value&gt;com.whoever.MyApplication&lt;/param-value&gt;
 *   &lt;/init-param&gt;
 *   &lt;load-on-startup&gt;1&lt;/load-on-startup&gt;
 * &lt;/servlet&gt;
 * </pre>
 * 
 * Note that the applicationClassName parameter you specify must be the fully qualified name of a
 * class that extends WebApplication. If your class cannot be found, does not extend WebApplication
 * or cannot be instantiated, a runtime exception of type WicketRuntimeException will be thrown.
 * <p>
 * As an alternative, you can configure an application factory instead. This looks like:
 * 
 * <pre>
 * &lt;init-param&gt;
 *   &lt;param-name&gt;applicationFactoryClassName&lt;/param-name&gt;
 *   &lt;param-value&gt;teachscape.platform.web.wicket.SpringApplicationFactory&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre>
 * 
 * and it has to satisfy interface {@link org.apache.wicket.protocol.http.IWebApplicationFactory}.
 * 
 * <p>
 * The servlet can also be configured to skip certain paths, this is especially useful when the
 * servlet is mapped to <code>/*</code> mapping:
 * 
 * <pre>
 * &lt;init-param&gt;
 *   &lt;param-name&gt;ignorePaths&lt;/param-name&gt;
 *   &lt;param-value&gt;/images/products/,/documents/pdf/&lt;/param-value&gt;
 * &lt;/init-param&gt;
 * </pre>
 * 
 * <p>
 * When GET/POST requests are made via HTTP, a WebRequestCycle object is created from the request,
 * response and session objects (after wrapping them in the appropriate wicket wrappers). The
 * RequestCycle's render() method is then called to produce a response to the HTTP request.
 * <p>
 * If you want to use servlet specific configuration, e.g. using init parameters from the
 * {@link javax.servlet.ServletConfig}object, you should override the init() method of
 * {@link javax.servlet.GenericServlet}. For example:
 * 
 * <pre>
 * public void init() throws ServletException
 * {
 *     ServletConfig config = getServletConfig();
 *     String webXMLParameter = config.getInitParameter(&quot;myWebXMLParameter&quot;);
 *     ...
 * </pre>
 * 
 * <p>
 * In order to support frameworks like Spring, the class is non-final and the variable
 * webApplication is protected instead of private. Thus subclasses may provide their own means of
 * providing the application object.
 * 
 * @see org.apache.wicket.request.cycle.RequestCycle
 * @author Jonathan Locke
 * @author Timur Mehrvarz
 * @author Juergen Donnerstag
 * @author Igor Vaynberg (ivaynberg)
 * @author Al Maw
 */
public class WicketServlet extends HttpServlet
{
	private static final long serialVersionUID = 1L;

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(WicketServlet.class);

	/** The WicketFilter where all the handling is done */
	protected transient WicketFilter wicketFilter;

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
	@Override
	public final void doGet(final HttpServletRequest servletRequest,
		final HttpServletResponse servletResponse) throws ServletException, IOException
	{
		if (wicketFilter.processRequest(servletRequest, servletResponse, null) == false)
		{
			fallback(servletRequest, servletResponse);
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
	@Override
	public final void doPost(final HttpServletRequest servletRequest,
		final HttpServletResponse servletResponse) throws ServletException, IOException
	{
		if (wicketFilter.processRequest(servletRequest, servletResponse, null) == false)
		{
			fallback(servletRequest, servletResponse);
		}
	}

	/**
	 * 
	 * @param httpServletRequest
	 * @return URL
	 */
	private static String getURL(final HttpServletRequest httpServletRequest)
	{
		/*
		 * Servlet 2.3 specification :
		 * 
		 * Servlet Path: The path section that directly corresponds to the mapping which activated
		 * this request. This path starts with a "/" character except in the case where the request
		 * is matched with the "/*" pattern, in which case it is the empty string.
		 * 
		 * PathInfo: The part of the request path that is not part of the Context Path or the
		 * Servlet Path. It is either null if there is no extra path, or is a string with a leading
		 * "/".
		 */
		String url = httpServletRequest.getServletPath();
		final String pathInfo = httpServletRequest.getPathInfo();

		if (pathInfo != null)
		{
			url += pathInfo;
		}

		String queryString = httpServletRequest.getQueryString();
		if (queryString != null)
		{
			url += ("?" + queryString);
		}

		// If url is non-empty it will start with '/', which we should lose
		if (url.length() > 0 && url.charAt(0) == '/')
		{
			// Remove leading '/'
			url = url.substring(1);
		}
		return url;
	}

	/**
	 * 
	 * @param request
	 * @param response
	 * @throws IOException
	 */
	private void fallback(final HttpServletRequest request, final HttpServletResponse response)
		throws IOException
	{
		// The ServletWebRequest is created here to avoid code duplication. The getURL call doesn't
		// depend on anything wicket specific
		String url = getURL(request);

		// WICKET-2185: strip of query string
		if (url.indexOf('?') != -1)
		{
			url = Strings.beforeFirst(url, '?');
		}

		// Get the relative URL we need for loading the resource from the servlet context
		// NOTE: we NEED to put the '/' in front as otherwise some versions of application servers
		// (e.g. Jetty 5.1.x) will fail for requests like '/mysubdir/myfile.css'
		if ((url.length() > 0 && url.charAt(0) != '/') || url.length() == 0)
		{
			url = '/' + url;
		}

		InputStream stream = getServletContext().getResourceAsStream(url);
		String mimeType = getServletContext().getMimeType(url);

		if (stream == null)
		{
			if (response.isCommitted())
			{
				response.setStatus(HttpServletResponse.SC_NOT_FOUND);
			}
			else
			{
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}
		else
		{
			if (mimeType != null)
			{
				response.setContentType(mimeType);
			}
			try
			{
				Streams.copy(stream, response.getOutputStream());
			}
			finally
			{
				stream.close();
			}
		}
	}

	/**
	 * Servlet initialization
	 */
	@Override
	public void init() throws ServletException
	{
		wicketFilter = newWicketFilter();
		wicketFilter.init(true, new FilterConfig()
		{
			/**
			 * @see javax.servlet.FilterConfig#getServletContext()
			 */
			@Override
			public ServletContext getServletContext()
			{
				return WicketServlet.this.getServletContext();
			}

			/**
			 * @see javax.servlet.FilterConfig#getInitParameterNames()
			 */
			@Override
			@SuppressWarnings("unchecked")
			public Enumeration<String> getInitParameterNames()
			{
				return WicketServlet.this.getInitParameterNames();
			}

			/**
			 * @see javax.servlet.FilterConfig#getInitParameter(java.lang.String)
			 */
			@Override
			public String getInitParameter(final String name)
			{
				return WicketServlet.this.getInitParameter(name);
			}

			/**
			 * @see javax.servlet.FilterConfig#getFilterName()
			 */
			@Override
			public String getFilterName()
			{
				return WicketServlet.this.getServletName();
			}
		});
	}

	/**
	 * @return The wicket filter
	 */
	protected WicketFilter newWicketFilter()
	{
		return new WicketFilter();
	}

	/**
	 * Servlet cleanup.
	 */
	@Override
	public void destroy()
	{
		wicketFilter.destroy();
		wicketFilter = null;
	}
}
