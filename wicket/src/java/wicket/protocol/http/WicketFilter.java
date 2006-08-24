/*
 * $Id: org.eclipse.jdt.ui.prefs 5004 2006-03-17 20:47:08 -0800 (Fri, 17 Mar
 * 2006) eelco12 $ $Revision: 5004 $ $Date: 2006-03-17 20:47:08 -0800 (Fri, 17
 * Mar 2006) $
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
import java.io.UnsupportedEncodingException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.AbortException;
import wicket.Application;
import wicket.RequestCycle;
import wicket.Resource;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.session.ISessionStore;
import wicket.settings.IRequestCycleSettings.RenderStrategy;
import wicket.util.resource.IResourceStream;
import wicket.util.string.Strings;
import wicket.util.time.Time;

/**
 * Filter for initiating handling of Wicket requests.
 * 
 * @author jcompagner
 */
public class WicketFilter implements Filter
{
	/** Log. */
	private static final Log log = LogFactory.getLog(WicketFilter.class);

	/**
	 * The name of the context parameter that specifies application factory
	 * class
	 */
	public static final String APP_FACT_PARAM = "applicationFactoryClassName";

	/**
	 * The name of the root path parameter that specifies the root dir of the
	 * app.
	 */
	public static final String FILTER_PATH_PARAM = "filterPath";

	/** The URL path prefix expected for (so called) resources (not html pages). */
	private static final String RESOURCES_PATH_PREFIX = "/resources/";

	/**
	 * The servlet path holder when the WicketSerlvet is used. So that the
	 * filter path will be computed with the first request. Note: This variable
	 * is by purpose package protected. See WicketServlet
	 */
	static final String SERVLET_PATH_HOLDER = "<servlet>";

	/** See javax.servlet.FilterConfig */
	private FilterConfig filterConfig;

	/**
	 * This is the filter path that can be specified in the filter config. Or it
	 * is the servlet path if the wicket servlet it used. both are without any /
	 * (start or end)
	 */
	private String filterPath;

	/**
	 * This holds the complete full root path including context and filter or
	 * servlet path
	 */
	private String rootPath;

	/** The Wicket Application associated with the Filter */
	private WebApplication webApplication;

	/**
	 * Servlet cleanup.
	 */
	public void destroy()
	{
		this.webApplication.internalDestroy();
		this.webApplication = null;
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException
	{
		HttpServletRequest httpServletRequest = (HttpServletRequest)request;
		if (isWicketRequest(httpServletRequest))
		{
			HttpServletResponse httpServletResponse = (HttpServletResponse)response;
			long lastModified = getLastModified(httpServletRequest);
			if (lastModified == -1)
			{
				// servlet doesn't support if-modified-since, no reason
				// to go through further expensive logic
				doGet(httpServletRequest, httpServletResponse);
			}
			else
			{
				long ifModifiedSince = httpServletRequest.getDateHeader("If-Modified-Since");
				if (ifModifiedSince < (lastModified / 1000 * 1000))
				{
					// If the servlet mod time is later, call doGet()
					// Round down to the nearest second for a proper compare
					// A ifModifiedSince of -1 will always be less
					maybeSetLastModified(httpServletResponse, lastModified);
					doGet(httpServletRequest, httpServletResponse);
				}
				else
				{
					httpServletResponse.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
				}
			}
		}
		else
		{
			chain.doFilter(request, response);
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
		// First, set the webapplication for this thread
		Application.set(webApplication);

		// Create a new webrequest
		final WebRequest request = webApplication.newWebRequest(servletRequest);

		if (webApplication.getRequestCycleSettings().getRenderStrategy() == RenderStrategy.REDIRECT_TO_BUFFER)
		{
			String queryString = servletRequest.getQueryString();
			if (!Strings.isEmpty(queryString))
			{
				// Try to see if there is a redirect stored
				ISessionStore sessionStore = webApplication.getSessionStore();
				String sessionId = sessionStore.getSessionId(request, false);
				if (sessionId != null)
				{
					BufferedHttpServletResponse bufferedResponse = webApplication
							.popBufferedResponse(sessionId, queryString);

					if (bufferedResponse != null)
					{
						bufferedResponse.writeTo(servletResponse);
						// redirect responses are ignored for the request
						// logger...
						return;
					}
				}
			}
		}

		// If the request does not provide information about the encoding of its
		// body (which includes POST parameters), than assume the default
		// encoding as defined by the wicket application. Bear in mind that the
		// encoding of the request usually is equal to the previous response.
		// However it is a known bug of IE that it does not provide this
		// information. Please see the wiki for more details and why all other
		// browser deliberately copied that bug.
		if (servletRequest.getCharacterEncoding() == null)
		{
			try
			{
				// The encoding defined by the wicket settings is used to encode
				// the responses. Thus, it is reasonable to assume the request
				// has the same encoding. This is especially important for
				// forms and form parameters.
				servletRequest.setCharacterEncoding(webApplication.getRequestCycleSettings()
						.getResponseRequestEncoding());
			}
			catch (UnsupportedEncodingException ex)
			{
				throw new WicketRuntimeException(ex.getMessage());
			}
		}

		// Get session for request
		final WebSession session = webApplication.getSession(request);

		// Create a response object and set the output encoding according to
		// wicket's application setttings.
		final WebResponse response = webApplication.newWebResponse(servletResponse);
		response.setAjax(request.isAjax());
		response.setCharacterEncoding(webApplication.getRequestCycleSettings()
				.getResponseRequestEncoding());

		try
		{
			RequestCycle cycle = session.newRequestCycle(request, response);
			try
			{
				// Process request
				cycle.request();
			}
			catch (AbortException e)
			{
				// noop
			}
		}
		finally
		{
			// Close response
			response.close();

			// Clean up thread local session
			Session.unset();

			// Clean up thread local application
			Application.unset();
		}
	}

	/**
	 * @return The filter config of this WicketFilter
	 */
	public FilterConfig getFilterConfig()
	{
		return filterConfig;
	}

	/**
	 * Returns the full rootpath of this application. This is the
	 * ApplicationSettings.contextpath and the WicketFilter.rootpath concatted.
	 * 
	 * @param request
	 *            The request where the optional context path (if not specified
	 *            in the settings) can be get from.
	 * 
	 * @return String the full rootpath.
	 */
	public String getRootPath(HttpServletRequest request)
	{
		if (rootPath == null)
		{
			String contextPath = webApplication.getApplicationSettings().getContextPath();
			if (contextPath == null)
			{
				contextPath = request.getContextPath();
				if (contextPath == null)
					contextPath = "";
			}

			if (SERVLET_PATH_HOLDER.equals(filterPath))
			{
				filterPath = request.getServletPath();
				if (filterPath.startsWith("/"))
				{
					filterPath = filterPath.substring(1);
				}
			}
			if (!contextPath.endsWith("/"))
			{
				rootPath = contextPath + "/" + filterPath;
			}
			else
			{
				rootPath = contextPath + filterPath;
			}
		}
		return rootPath;
	}

	/**
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException
	{
		this.filterConfig = filterConfig;

		IWebApplicationFactory factory = getApplicationFactory();

		// Construct WebApplication subclass
		this.webApplication = factory.createApplication(this);

		// Set this WicketServlet as the servlet for the web application
		this.webApplication.setWicketFilter(this);

		// Store instance of this application object in servlet context to make
		// integration with outside world easier
		String contextKey = "wicket:" + filterConfig.getFilterName();
		filterConfig.getServletContext().setAttribute(contextKey, this.webApplication);

		filterPath = filterConfig.getInitParameter(FILTER_PATH_PARAM);

		try
		{
			Application.set(webApplication);

			// Call internal init method of web application for default
			// initialisation
			this.webApplication.internalInit();

			// Call init method of web application
			this.webApplication.init();

			// We initialize components here rather than in the constructor or
			// in the internal init, because in the init method class aliases
			// can be added, that would be used in installing resources in the
			// component.
			this.webApplication.initializeComponents();

			// Finished
			log.info("Wicket application " + this.webApplication.getName() + " started [factory="
					+ factory.getClass().getName() + "]");
		}
		finally
		{
			Application.unset();
		}
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
		final String appFactoryClassName = filterConfig.getInitParameter(APP_FACT_PARAM);

		if (appFactoryClassName == null)
		{
			// If no context param was specified we return the default factory
			return new ContextParamWebApplicationFactory();
		}
		else
		{
			try
			{
				// Try to find the specified factory class
				final Class factoryClass = getClass().getClassLoader().loadClass(
						appFactoryClassName);

				// Instantiate the factory
				return (IWebApplicationFactory)factoryClass.newInstance();
			}
			catch (ClassCastException e)
			{
				throw new WicketRuntimeException("Application factory class " + appFactoryClassName
						+ " must implement IWebApplicationFactory");
			}
			catch (ClassNotFoundException e)
			{
				throw new WebApplicationFactoryCreationException(appFactoryClassName, e);
			}
			catch (InstantiationException e)
			{
				throw new WebApplicationFactoryCreationException(appFactoryClassName, e);
			}
			catch (IllegalAccessException e)
			{
				throw new WebApplicationFactoryCreationException(appFactoryClassName, e);
			}
			catch (SecurityException e)
			{
				throw new WebApplicationFactoryCreationException(appFactoryClassName, e);
			}
		}
	}

	/**
	 * Gets the last modified time stamp for the given request.
	 * 
	 * @param servletRequest
	 * @return The last modified time stamp
	 */
	long getLastModified(final HttpServletRequest servletRequest)
	{
		final String pathInfo = servletRequest.getRequestURI();

		int rootPathLength = getRootPath(servletRequest).length();
		if (pathInfo.length() > rootPathLength
				&& pathInfo.substring(rootPathLength).startsWith(RESOURCES_PATH_PREFIX))
		{
			final String resourceReferenceKey = pathInfo.substring(rootPathLength
					+ RESOURCES_PATH_PREFIX.length());

			// Try to find shared resource
			Resource resource = webApplication.getSharedResources().get(resourceReferenceKey);

			// If resource found and it is cacheable
			if ((resource != null) && resource.isCacheable())
			{
				try
				{
					Application.set(webApplication);

					final WebRequest webRequest = webApplication.newWebRequest(servletRequest);

					// Set parameters from servlet request
					resource.setParameters(webRequest.getParameterMap());

					// Get resource stream
					IResourceStream stream = resource.getResourceStream();

					// Get last modified time from stream
					Time time = stream.lastModifiedTime();

					try
					{
						stream.close();
					}
					catch (IOException e)
					{
						// ignore
					}

					return time != null ? time.getMilliseconds() : -1;
				}
				catch (AbortException e)
				{
					return -1;
				}
				finally
				{
					resource.setParameters(null);
					Application.unset();
				}
			}
		}
		return -1;
	}

	/**
	 * Is this a Wicket request?
	 * 
	 * @param request
	 *            The servlet request
	 * @return True if this is a Wicket request
	 */
	private boolean isWicketRequest(HttpServletRequest request)
	{
		String fullRootPath = getRootPath(request);
		String url = request.getRequestURI();
		// Homepage
		if (url.startsWith(fullRootPath))
		{
			// url == fullRootPath
			if (url.length() == fullRootPath.length())
			{
				return true;
			}
			//  
			if ((url.length() > fullRootPath.length())
					&& (url.charAt(fullRootPath.length()) == ';'))
			{
				return true;
			}
		}
		// SharedResources
		String tmp = Strings.join("/", fullRootPath, RESOURCES_PATH_PREFIX);
		if (url.startsWith(tmp))
		{
			return true;
		}
		// Mounted url
		String path = null;
		if (fullRootPath.length() < url.length())
		{
			path = url.substring(fullRootPath.length());
		}
		else
		{
			path = url;
		}

		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}
		return webApplication.getRequestCycleProcessor().getRequestCodingStrategy()
				.urlCodingStrategyForPath(path) != null;
	}

	/**
	 * If the response has not already a 'lastModified' header set and if
	 * 'lastModified' >= 0 than set the response header accordingly.
	 * 
	 * @param resp
	 * @param lastModified
	 */
	private void maybeSetLastModified(final HttpServletResponse resp, final long lastModified)
	{
		if (resp.containsHeader("Last-Modified"))
		{
			return;
		}
		if (lastModified >= 0)
		{
			resp.setDateHeader("Last-Modified", lastModified);
		}
	}
}
