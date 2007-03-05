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
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import wicket.AbortException;
import wicket.Application;
import wicket.RequestCycle;
import wicket.Resource;
import wicket.Session;
import wicket.WicketRuntimeException;
import wicket.markup.parser.XmlPullParser;
import wicket.markup.parser.XmlTag;
import wicket.protocol.http.request.WebRequestCodingStrategy;
import wicket.session.ISessionStore;
import wicket.settings.IRequestCycleSettings;
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
	/**
	 * The name of the context parameter that specifies application factory
	 * class
	 */
	public static final String APP_FACT_PARAM = "applicationFactoryClassName";

	/**
	 * The name of the root path parameter that specifies the root dir of the
	 * app.
	 */
	public static final String FILTER_MAPPING_PARAM = "filterMappingUrlPattern";

	/** Log. */
	private static final Log log = LogFactory.getLog(WicketFilter.class);

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

	/** The Wicket Application associated with the Filter */
	private WebApplication webApplication;

	private boolean servletMode = false;
	
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
		String relativePath = getRelativePath(httpServletRequest);
				
		if (isWicketRequest(relativePath))
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
		String relativePath = getRelativePath(servletRequest);
		// Special-case for home page - we redirect to add a trailing slash.
		if (relativePath.length() == 0 && !Strings.stripJSessionId(servletRequest.getRequestURI()).endsWith("/"))
		{
			String foo = servletRequest.getRequestURI() + "/";
			servletResponse.sendRedirect(foo);
			return;
		}

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(getClassLoader());

			// If the request does not provide information about the encoding of
			// its
			// body (which includes POST parameters), than assume the default
			// encoding as defined by the wicket application. Bear in mind that
			// the
			// encoding of the request usually is equal to the previous
			// response.
			// However it is a known bug of IE that it does not provide this
			// information. Please see the wiki for more details and why all
			// other
			// browser deliberately copied that bug.
			if (servletRequest.getCharacterEncoding() == null)
			{
				try
				{
					// The encoding defined by the wicket settings is used to
					// encode
					// the responses. Thus, it is reasonable to assume the
					// request
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

			// Create a new webrequest
			final WebRequest request = webApplication.newWebRequest(servletRequest);

			// Are we using REDIRECT_TO_BUFFER?
			if (webApplication.getRequestCycleSettings().getRenderStrategy() == IRequestCycleSettings.REDIRECT_TO_BUFFER)
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

			// First, set the webapplication for this thread
			Application.set(webApplication);

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
		finally
		{
			Thread.currentThread().setContextClassLoader(previousClassLoader);
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
	 * Returns a relative path from an HttpServletRequest Use this to resolve a
	 * Wicket request.
	 * 
	 * @param request
	 * @return Path requested, minus query string, context path, and filterPath.
	 *         Relative, no leading '/'.
	 */
	public String getRelativePath(HttpServletRequest request) {
		String path = request.getServletPath();
		if (servletMode)
		{
			path = request.getPathInfo();
			// No path info => root.
			if (path == null)
			{
				path = "";
			}
		}
		filterPath = getFilterPath(request);
		
		if (path.length() > 0)
		{
			path = path.substring(1);
		}
		
		// We should always be under the rootPath, except
		// for the special case of someone landing on the
		// home page without a trailing slash.
		if (!path.startsWith(filterPath))
		{
			if (filterPath.equals(path + "/"))
			{
				path += "/";
			}
		}
		if (path.startsWith(filterPath))
		{
			path = path.substring(filterPath.length());
		}

		return path;

	}

	/**
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException
	{
		this.filterConfig = filterConfig;
		
		if (SERVLET_PATH_HOLDER.equals(filterConfig.getInitParameter(FILTER_MAPPING_PARAM)))
		{
			servletMode = true;
		}
		
		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		try
		{
			Thread.currentThread().setContextClassLoader(getClassLoader());
			
			// Try to configure filterPath from web.xml if it's not specified as an init-param.
			if (filterConfig.getInitParameter(WicketFilter.FILTER_MAPPING_PARAM) == null)
			{
				InputStream is = filterConfig.getServletContext().getResourceAsStream("/WEB-INF/web.xml");
				if (is != null)
				{
					try
					{
						filterPath = getFilterPath(filterConfig.getFilterName(), is);
					}
					catch (Exception e)
					{
						log.debug("Error parsing web.xml", e);
						// Swallow IOException or SecurityException or similar, and log.info below.
					}
				}
				if (filterPath == null)
				{
					log.info("Unable to parse filter mapping web.xml for " + filterConfig.getFilterName() + ". " +
							"Configure with init-param " + FILTER_MAPPING_PARAM + " if it is not \"/*\".");
				}
			}

			IWebApplicationFactory factory = getApplicationFactory();

			// Construct WebApplication subclass
			this.webApplication = factory.createApplication(this);

			// Set this WicketFilter as the filter for the web application
			this.webApplication.setWicketFilter(this);

			// Store instance of this application object in servlet context to
			// make integration with outside world easier
			String contextKey = "wicket:" + filterConfig.getFilterName();
			filterConfig.getServletContext().setAttribute(contextKey, this.webApplication);

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

			// Give the application the option to log that it is started
			this.webApplication.logStarted();
		}
		finally
		{
			Application.unset();
			Thread.currentThread().setContextClassLoader(previousClassLoader);
		}
	}

	private String getFilterPath(String filterName, InputStream is) throws ServletException
	{
		/*
		 * Filter mappings look like this:
		 * 
		 * <filter-mapping> <filter-name>WicketFilter</filter-name>
		 * <url-pattern>/*</url-pattern> <...> <filter-mapping>
		 */
		try
		{
			ArrayList urlPatterns = new ArrayList();
			XmlPullParser parser = new XmlPullParser();
			parser.parse(is);
			
			while (true) {
				XmlTag elem;
				do {
					elem = (XmlTag)parser.nextTag();
				} while (elem != null && (! (elem.getName().equals("filter-mapping") && elem.isOpen())));
				
				if (elem == null)
					break;
	
				String encounteredFilterName = null, urlPattern = null;
	
				do {
					elem = (XmlTag)parser.nextTag();
					if (elem.isOpen()) {
						parser.setPositionMarker();
					} else if (elem.isClose() && elem.getName().equals("filter-name")) {
						encounteredFilterName = parser.getInputFromPositionMarker(elem.getPos()).toString();
					} else if (elem.isClose() && elem.getName().equals("url-pattern")) {
						urlPattern = parser.getInputFromPositionMarker(elem.getPos()).toString();
					}
				} while (urlPattern == null || encounteredFilterName == null);
				
				if (filterName.equals(encounteredFilterName))
					urlPatterns.add(urlPattern);
			}

			// By the time we get here, we have a list of urlPatterns we match
			// this filter against.
			// In all likelihood, we will only have one. If we have none, we
			// have an error.
			// If we have more than one, we pick the first one to use for any
			// 302 redirects that
			// require absolute URLs.
			if (urlPatterns.size() == 0)
			{
				throw new ServletException(
						"Error initialising WicketFilter - you have no filter-mapping element with a url-pattern that uses filter: "
								+ filterName);
			}
			String urlPattern = (String)urlPatterns.get(0);

			// Check for leading '/' and trailing '*'.
			if (!urlPattern.startsWith("/") || !urlPattern.endsWith("*"))
			{
				throw new ServletException(
						"Filter mappings for Wicket filter must start with '/' and end with '*'.");
			}

			// Strip trailing '*' and leading '/'.
			return urlPattern.substring(1, urlPattern.length() - 1);
		}
		catch (Exception e)
		{
			throw new ServletException("Error finding filter " + filterName + " in web.xml", e);
		}
	}

	/**
	 * Is this a Wicket request?
	 * 
	 * @param relativePath
	 *            The relativePath
	 * @return True if this is a Wicket request
	 */
	private boolean isWicketRequest(String relativePath)
	{
		// Special case home page
		if (relativePath.equals("")) {
			return true;
		}
				
		// Resources
		if (relativePath.startsWith(WebRequestCodingStrategy.RESOURCES_PATH_PREFIX)) {
			return true;
		}
		
		// Mounted page
		return webApplication.getRequestCycleProcessor().getRequestCodingStrategy().urlCodingStrategyForPath(relativePath) != null;
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
				final Class factoryClass = Thread.currentThread().getContextClassLoader()
						.loadClass(appFactoryClassName);

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
	 * @return The class loader
	 */
	protected ClassLoader getClassLoader()
	{
		return Thread.currentThread().getContextClassLoader();
	}

	protected String getFilterPath(HttpServletRequest request)
	{
		if (filterPath != null)
		{
			return filterPath;
		}
		if (servletMode)
		{
			return filterPath = request.getServletPath();
		}
		String result;
		// Legacy migration check. TODO: Remove this after 1.3 is released and everyone's upgraded.
		if (filterConfig.getInitParameter("filterPath") != null)
		{
			throw new WicketRuntimeException("\nThe filterPath init-param for WicketFilter has been removed.\n" +
					"Please use a param called " + FILTER_MAPPING_PARAM + " with a value that exactly\n" +
					"matches that in the <url-pattern> element of your <filter-mapping> (e.g. \"/app/*\").");
		}
		
		result = filterConfig.getInitParameter(FILTER_MAPPING_PARAM);
		if (result == null || result.equals("/*"))
		{
			return "";
		}
		else if (!result.startsWith("/") || !result.endsWith("/*"))
		{
			throw new WicketRuntimeException("Your " + FILTER_MAPPING_PARAM + " must start with \"/\" and end with \"/*\". It is: " + result);
		}
		return filterPath = result.substring(1, result.length() - 2);
	}
	
	/**
	 * Gets the last modified time stamp for the given request.
	 * 
	 * @param request
	 * @return The last modified time stamp
	 */
	long getLastModified(final HttpServletRequest request)
	{
		final String pathInfo = getRelativePath(request);
		
		if (pathInfo.startsWith(WebRequestCodingStrategy.RESOURCES_PATH_PREFIX)) {
			
			final String resourceReferenceKey = pathInfo.substring(WebRequestCodingStrategy.RESOURCES_PATH_PREFIX.length());

			// Try to find shared resource
			Resource resource = webApplication.getSharedResources().get(resourceReferenceKey);

			// If resource found and it is cacheable
			if ((resource != null) && resource.isCacheable())
			{
				try
				{
					Application.set(webApplication);

					final WebRequest webRequest = webApplication.newWebRequest(request);

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
}
