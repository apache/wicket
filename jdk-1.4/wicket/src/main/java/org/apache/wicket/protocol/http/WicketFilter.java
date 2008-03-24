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
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Properties;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.AbortException;
import org.apache.wicket.Application;
import org.apache.wicket.RequestContext;
import org.apache.wicket.RequestCycle;
import org.apache.wicket.Resource;
import org.apache.wicket.Session;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.markup.parser.XmlPullParser;
import org.apache.wicket.markup.parser.XmlTag;
import org.apache.wicket.protocol.http.portlet.FilterRequestContext;
import org.apache.wicket.protocol.http.portlet.WicketFilterPortletContext;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.session.ISessionStore;
import org.apache.wicket.settings.IRequestCycleSettings;
import org.apache.wicket.util.resource.IResourceStream;
import org.apache.wicket.util.resource.ResourceStreamNotFoundException;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Filter for initiating handling of Wicket requests.
 * 
 * @author jcompagner
 */
public class WicketFilter implements Filter
{
	/**
	 * The name of the context parameter that specifies application factory class
	 */
	public static final String APP_FACT_PARAM = "applicationFactoryClassName";

	/**
	 * The name of the root path parameter that specifies the root dir of the app.
	 */
	public static final String FILTER_MAPPING_PARAM = "filterMappingUrlPattern";

	/** Log. */
	private static final Logger log = LoggerFactory.getLogger(WicketFilter.class);

	/**
	 * The servlet path holder when the WicketSerlvet is used. So that the filter path will be
	 * computed with the first request. Note: This variable is by purpose package protected. See
	 * WicketServlet
	 */
	static final String SERVLET_PATH_HOLDER = "<servlet>";

	/** See javax.servlet.FilterConfig */
	private FilterConfig filterConfig;

	/**
	 * This is the filter path that can be specified in the filter config. Or it is the servlet path
	 * if the wicket servlet it used. both are without any / (start or end)
	 */
	private String filterPath;

	/** The Wicket Application associated with the Filter */
	private WebApplication webApplication;

	private boolean servletMode = false;

	/**
	 * The name of the optional filter parameter indicating it may/can only be accessed from within
	 * a Portlet context. Value should be true
	 */
	private static final String PORTLET_ONLY_FILTER = "portletOnlyFilter";

	/**
	 * The name of the optional filter parameter indicating a javax.portlet.PortletContext class
	 * should be looked up to determine if portlet support should be provided.
	 */
	private static final String DETECT_PORTLET_CONTEXT = "detectPortletContext";

	/**
	 * The name of the optional web.xml context parameter indicating if a portlet context is to be
	 * determined by looking up the javax.portlet.PortletContext class. Default value is false. This
	 * context parameter is only queried if the filter parameter DETECT_PORTLET_CONTEXT isn't
	 * provided. If additionally the context parameter is not specified, a WicketPortlet.properties
	 * resource will be looked up through the classpath which, if found, is queried for a property
	 * with the same name.
	 */
	private static final String DETECT_PORTLET_CONTEXT_FULL_NAME = "org.apache.wicket.detectPortletContext";

	/**
	 * The classpath resource name of an optional WicketPortlet.properties file.
	 */
	private static final String WICKET_PORTLET_PROPERTIES = "org/apache/wicket/protocol/http/portlet/WicketPortlet.properties";

	/*
	 * Delegate for handling Portlet specific filtering. Not instantiated if not running in a
	 * portlet container context
	 */
	private WicketFilterPortletContext filterPortletContext;

	/*
	 * Flag if this filter may only process request from within a Portlet context.
	 */
	private boolean portletOnlyFilter;

	/**
	 * Servlet cleanup.
	 */
	public void destroy()
	{
		if (webApplication != null)
		{
			webApplication.internalDestroy();
			webApplication = null;
		}
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException
	{
		HttpServletRequest httpServletRequest;
		HttpServletResponse httpServletResponse;

		boolean inPortletContext = false;
		if (filterPortletContext != null)
		{
			FilterRequestContext filterRequestContext = new FilterRequestContext(
				(HttpServletRequest)request, (HttpServletResponse)response);
			inPortletContext = filterPortletContext.setupFilter(getFilterConfig(),
				filterRequestContext, getFilterPath((HttpServletRequest)request));
			httpServletRequest = filterRequestContext.getRequest();
			httpServletResponse = filterRequestContext.getResponse();
		}
		else
		{
			httpServletRequest = (HttpServletRequest)request;
			httpServletResponse = (HttpServletResponse)response;
		}

		if (portletOnlyFilter && !inPortletContext)
		{
			chain.doFilter(request, response);
			return;
		}

		String relativePath = getRelativePath(httpServletRequest);

		if (isWicketRequest(relativePath))
		{
			try
			{
				// Set the webapplication for this thread
				Application.set(webApplication);

				long lastModified = getLastModified(httpServletRequest);
				if (lastModified == -1)
				{
					// servlet doesn't support if-modified-since, no reason
					// to go through further expensive logic
					if (doGet(httpServletRequest, httpServletResponse) == false)
					{
						chain.doFilter(request, response);
					}
				}
				else
				{
					long ifModifiedSince;
					try
					{
						ifModifiedSince = httpServletRequest.getDateHeader("If-Modified-Since");
					}
					catch (IllegalArgumentException e)
					{
						log.warn("Invalid If-Modified-Since header", e);
						ifModifiedSince = -1;
					}
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
			finally
			{
				// always unset the application thread local
				Application.unset();
				RequestContext.unset();
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
	 * @return true if the request was handled by wicket, false otherwise
	 * @throws ServletException
	 *             Thrown if something goes wrong during request handling
	 * @throws IOException
	 */
	public boolean doGet(final HttpServletRequest servletRequest,
		final HttpServletResponse servletResponse) throws ServletException, IOException
	{
		String relativePath = getRelativePath(servletRequest);

		// Special-case for home page - we redirect to add a trailing slash.
		if (relativePath.length() == 0 &&
			!Strings.stripJSessionId(servletRequest.getRequestURI()).endsWith("/"))
		{
			String redirectUrl = servletRequest.getRequestURI() + "/";
			String queryString = servletRequest.getQueryString();
			if (queryString != null)
			{
				redirectUrl += "?" + queryString;
			}
			servletResponse.sendRedirect(servletResponse.encodeRedirectURL(redirectUrl));
			return true;
		}

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader newClassLoader = getClassLoader();
		try
		{
			if (previousClassLoader != newClassLoader)
			{
				Thread.currentThread().setContextClassLoader(newClassLoader);
			}

			// If the request does not provide information about the encoding of
			// its body (which includes POST parameters), than assume the
			// default encoding as defined by the wicket application. Bear in
			// mind that the encoding of the request usually is equal to the
			// previous response.
			// However it is a known bug of IE that it does not provide this
			// information. Please see the wiki for more details and why all
			// other browser deliberately copied that bug.
			if (servletRequest.getCharacterEncoding() == null)
			{
				try
				{
					// The encoding defined by the wicket settings is used to
					// encode the responses. Thus, it is reasonable to assume
					// the request has the same encoding. This is especially
					// important for forms and form parameters.
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
				// Try to see if there is a redirect stored
				ISessionStore sessionStore = webApplication.getSessionStore();
				String sessionId = sessionStore.getSessionId(request, false);
				if (sessionId != null)
				{
					BufferedHttpServletResponse bufferedResponse = null;
					String queryString = servletRequest.getQueryString();
					if (!Strings.isEmpty(queryString))
					{
						bufferedResponse = webApplication.popBufferedResponse(sessionId,
							queryString);
					}
					else
					{
						bufferedResponse = webApplication.popBufferedResponse(sessionId,
							relativePath);
					}

					if (bufferedResponse != null)
					{
						bufferedResponse.writeTo(servletResponse);
						// redirect responses are ignored for the request
						// logger...
						return true;
					}
				}
			}

			WebResponse response = null;
			boolean externalCall = !Application.exists();
			try
			{
				// if called externally (i.e. WicketServlet) we need to set the thread local here
				// AND clean it up at the end of the request
				if (externalCall)
				{
					Application.set(webApplication);
				}

				// Create a response object and set the output encoding according to
				// wicket's application settings.
				response = webApplication.newWebResponse(servletResponse);
				response.setAjax(request.isAjax());
				response.setCharacterEncoding(webApplication.getRequestCycleSettings()
					.getResponseRequestEncoding());

				createRequestContext(request, response);

				// Create request cycle
				final RequestCycle cycle = webApplication.newRequestCycle(request, response);

				try
				{
					// Process request
					cycle.request();

					return cycle.wasHandled();
				}
				catch (AbortException e)
				{
					// noop
				}
			}
			finally
			{
				// Close response
				if (response != null)
					response.close();

				// Clean up thread local session
				Session.unset();

				if (externalCall)
				{
					// Clean up thread local application if this was an external call
					// (if not, doFilter will clean it up)
					Application.unset();
					RequestContext.unset();
				}
			}
		}
		finally
		{
			if (newClassLoader != previousClassLoader)
			{
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}
		}
		return true;
	}

	/**
	 * @return The filter config of this WicketFilter
	 */
	public FilterConfig getFilterConfig()
	{
		return filterConfig;
	}

	/**
	 * Returns a relative path from an HttpServletRequest Use this to resolve a Wicket request.
	 * 
	 * @param request
	 * @return Path requested, minus query string, context path, and filterPath. Relative, no
	 *         leading '/'.
	 */
	public String getRelativePath(HttpServletRequest request)
	{
		String path = Strings.stripJSessionId(request.getRequestURI());
		String contextPath = request.getContextPath();
		path = path.substring(contextPath.length());
		if (servletMode)
		{
			String servletPath = request.getServletPath();
			path = path.substring(servletPath.length());
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
		String filterMapping = filterConfig.getInitParameter(WicketFilter.FILTER_MAPPING_PARAM);

		if (SERVLET_PATH_HOLDER.equals(filterMapping))
		{
			servletMode = true;
		}

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader newClassLoader = getClassLoader();
		try
		{
			if (previousClassLoader != newClassLoader)
			{
				Thread.currentThread().setContextClassLoader(newClassLoader);
			}

			// Try to configure filterPath from web.xml if it's not specified as
			// an init-param.
			if (filterMapping == null)
			{
				InputStream is = filterConfig.getServletContext().getResourceAsStream(
					"/WEB-INF/web.xml");
				if (is != null)
				{
					try
					{
						filterPath = getFilterPath(filterConfig.getFilterName(), is);
					}
					catch (ServletException e)
					{
						log.error("Error reading servlet/filter path from web.xml", e);
					}
					catch (SecurityException e)
					{
						// Swallow this at INFO.
						log.info("Couldn't read web.xml to automatically pick up servlet/filter path: " +
							e.getMessage());
					}
					if (filterPath == null)
					{
						log.info("Unable to parse filter mapping web.xml for " +
							filterConfig.getFilterName() + ". " + "Configure with init-param " +
							FILTER_MAPPING_PARAM + " if it is not \"/*\".");
					}
				}
			}

			IWebApplicationFactory factory = getApplicationFactory();

			// Construct WebApplication subclass
			webApplication = factory.createApplication(this);

			// Set this WicketFilter as the filter for the web application
			webApplication.setWicketFilter(this);

			// Store instance of this application object in servlet context to
			// make integration with outside world easier
			String contextKey = "wicket:" + filterConfig.getFilterName();
			filterConfig.getServletContext().setAttribute(contextKey, webApplication);

			// set the application thread local in case initialization code uses it
			Application.set(webApplication);

			// Call internal init method of web application for default
			// initialization
			webApplication.internalInit();

			// Call init method of web application
			webApplication.init();

			// We initialize components here rather than in the constructor or
			// in the internal init, because in the init method class aliases
			// can be added, that would be used in installing resources in the
			// component.
			webApplication.initializeComponents();

			// Give the application the option to log that it is started
			webApplication.logStarted();

			portletOnlyFilter = Boolean.valueOf(filterConfig.getInitParameter(PORTLET_ONLY_FILTER))
				.booleanValue();

			if (isPortletContextAvailable(filterConfig))
			{
				filterPortletContext = newWicketFilterPortletContext();
			}
			if (filterPortletContext != null)
			{
				filterPortletContext.initFilter(filterConfig, webApplication);
			}
		}
		finally
		{
			Application.unset();
			if (newClassLoader != previousClassLoader)
			{
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}
		}
	}

	protected boolean isPortletContextAvailable(FilterConfig config) throws ServletException
	{
		boolean detectPortletContext = false;
		String parameter = config.getInitParameter(DETECT_PORTLET_CONTEXT);
		if (parameter != null)
		{
			detectPortletContext = Boolean.valueOf(parameter).booleanValue();
		}
		else
		{
			parameter = config.getServletContext().getInitParameter(
				DETECT_PORTLET_CONTEXT_FULL_NAME);
			if (parameter != null)
			{
				detectPortletContext = Boolean.valueOf(parameter).booleanValue();
			}
			else
			{
				InputStream is = Thread.currentThread()
					.getContextClassLoader()
					.getResourceAsStream(WICKET_PORTLET_PROPERTIES);
				if (is != null)
				{
					try
					{
						Properties properties = new Properties();
						properties.load(is);
						detectPortletContext = Boolean.valueOf(
							properties.getProperty(DETECT_PORTLET_CONTEXT_FULL_NAME, "false"))
							.booleanValue();
					}
					catch (IOException e)
					{
						throw new ServletException(
							"Failed to load WicketPortlet.properties from classpath", e);
					}
				}
			}
		}
		if (detectPortletContext)
		{
			try
			{
				Class portletClass = Class.forName("javax.portlet.PortletContext");
				return true;
			}
			catch (ClassNotFoundException e)
			{
			}
		}
		return false;
	}

	protected WicketFilterPortletContext newWicketFilterPortletContext()
	{
		return new WicketFilterPortletContext();
	}

	protected void createRequestContext(WebRequest request, WebResponse response)
	{
		if (filterPortletContext == null ||
			!filterPortletContext.createPortletRequestContext(request, response))
		{
			new RequestContext();
		}
	}

	private String getFilterPath(String filterName, InputStream is) throws ServletException
	{
		String prefix = servletMode ? "servlet" : "filter";
		String mapping = prefix + "-mapping";
		String name = prefix + "-name";

		// Filter mappings look like this:
		//
		// <filter-mapping> <filter-name>WicketFilter</filter-name>
		// <url-pattern>/*</url-pattern> <...> <filter-mapping>
		try
		{
			ArrayList urlPatterns = new ArrayList();
			XmlPullParser parser = new XmlPullParser();
			parser.parse(is);

			while (true)
			{
				XmlTag elem;
				do
				{
					elem = (XmlTag)parser.nextTag();
				}
				while (elem != null && (!(elem.getName().equals(mapping) && elem.isOpen())));

				if (elem == null)
				{
					break;
				}

				String encounteredFilterName = null, urlPattern = null;

				do
				{
					elem = (XmlTag)parser.nextTag();
					if (elem.isOpen())
					{
						parser.setPositionMarker();
					}
					else if (elem.isClose() && elem.getName().equals(name))
					{
						encounteredFilterName = parser.getInputFromPositionMarker(elem.getPos())
							.toString()
							.trim();
					}
					else if (elem.isClose() && elem.getName().equals("url-pattern"))
					{
						urlPattern = parser.getInputFromPositionMarker(elem.getPos())
							.toString()
							.trim();
					}
				}
				while (urlPattern == null || encounteredFilterName == null);

				if (filterName.equals(encounteredFilterName))
				{
					urlPatterns.add(urlPattern);
				}
			}

			String prefixUppered = Character.toUpperCase(prefix.charAt(0)) + prefix.substring(1);

			// By the time we get here, we have a list of urlPatterns we match
			// this filter against.
			// In all likelihood, we will only have one. If we have none, we
			// have an error.
			// If we have more than one, we pick the first one to use for any
			// 302 redirects that require absolute URLs.
			if (urlPatterns.size() == 0)
			{
				throw new IllegalArgumentException("Error initializing Wicket" + prefixUppered +
					" - you have no <" + mapping + "> element with a url-pattern that uses " +
					prefix + ": " + filterName);
			}
			String urlPattern = (String)urlPatterns.get(0);

			// Check for leading '/' and trailing '*'.
			if (!urlPattern.startsWith("/") || !urlPattern.endsWith("*"))
			{
				throw new IllegalArgumentException("<" + mapping + "> for Wicket" + prefixUppered +
					" \"" + filterName + "\" must start with '/' and end with '*'.");
			}

			// Strip trailing '*' and keep leading '/'.
			return stripWildcard(urlPattern);
		}
		catch (IOException e)
		{
			throw new ServletException("Error finding <" + prefix + "> " + filterName +
				" in web.xml", e);
		}
		catch (ParseException e)
		{
			throw new ServletException("Error finding <" + prefix + "> " + filterName +
				" in web.xml", e);
		}
		catch (ResourceStreamNotFoundException e)
		{
			throw new ServletException("Error finding <" + prefix + "> " + filterName +
				" in web.xml", e);
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
		// default location, like
		// /wicket-examples/forminput/?wicket:interface=:0::::
		// the relative path here is empty (wicket-examples is the web
		// application and the filter is mapped to /forminput/*
		if (relativePath.equals(""))
		{
			return true;
		}

		// Resources
		if (relativePath.startsWith(WebRequestCodingStrategy.RESOURCES_PATH_PREFIX))
		{
			return true;
		}

		// Mounted page
		return webApplication.getRequestCycleProcessor()
			.getRequestCodingStrategy()
			.urlCodingStrategyForPath(relativePath) != null;
	}

	/**
	 * If the response has not already a 'lastModified' header set and if 'lastModified' >= 0 than
	 * set the response header accordingly.
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
	 * If no APP_FACT_PARAM is specified in web.xml ContextParamWebApplicationFactory will be used
	 * by default.
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
				final Class factoryClass = Thread.currentThread()
					.getContextClassLoader()
					.loadClass(appFactoryClassName);

				// Instantiate the factory
				return (IWebApplicationFactory)factoryClass.newInstance();
			}
			catch (ClassCastException e)
			{
				throw new WicketRuntimeException("Application factory class " +
					appFactoryClassName + " must implement IWebApplicationFactory");
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
		// Legacy migration check.
		// TODO: Remove this after 1.3 is released and everyone's upgraded.
		if (filterConfig.getInitParameter("filterPath") != null)
		{
			throw new WicketRuntimeException(
				"\nThe filterPath init-param for WicketFilter has been removed.\n" +
					"Please use a param called " + FILTER_MAPPING_PARAM +
					" with a value that exactly\n" +
					"matches that in the <url-pattern> element of your <filter-mapping> (e.g. \"/app/*\").");
		}

		result = filterConfig.getInitParameter(FILTER_MAPPING_PARAM);
		if (result == null || result.equals("/*"))
		{
			return "";
		}
		else if (!result.startsWith("/") || !result.endsWith("/*"))
		{
			throw new WicketRuntimeException("Your " + FILTER_MAPPING_PARAM +
				" must start with \"/\" and end with \"/*\". It is: " + result);
		}
		return filterPath = stripWildcard(result);
	}

	/**
	 * Strip trailing '*' and keep leading '/'
	 * 
	 * @param result
	 * @return The stripped String
	 */
	private String stripWildcard(String result)
	{
		return result.substring(1, result.length() - 1);
	}

	/**
	 * Gets the last modified time stamp for the given request.
	 * 
	 * @param servletRequest
	 * @return The last modified time stamp
	 */
	long getLastModified(final HttpServletRequest servletRequest)
	{
		final String pathInfo = getRelativePath(servletRequest);

		if (pathInfo.startsWith(WebRequestCodingStrategy.RESOURCES_PATH_PREFIX))
		{

			final String resourceReferenceKey = pathInfo.substring(WebRequestCodingStrategy.RESOURCES_PATH_PREFIX.length());

			Resource resource = null;

			boolean externalCall = !Application.exists();
			try
			{
				// if called externally (i.e. WicketServlet) we need to set the thread local here
				// AND clean it up at the end of the request
				if (externalCall)
				{
					Application.set(webApplication);
				}

				// Try to find shared resource
				resource = webApplication.getSharedResources().get(resourceReferenceKey);

				// If resource found and it is cacheable
				if ((resource != null) && resource.isCacheable())
				{

					final WebRequest request = webApplication.newWebRequest(servletRequest);
					// make the session available.
					Session.findOrCreate(request, new WebResponse());


					// Set parameters from servlet request
					resource.setParameters(request.getParameterMap());

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
			}
			catch (AbortException e)
			{
				return -1;
			}
			finally
			{
				if (resource != null)
				{
					resource.setParameters(null);
				}
				if (externalCall)
				{
					// Clean up thread local application if this was an external call
					// (if not, doFilter will clean it up)
					Application.unset();
					RequestContext.unset();
				}
				if (Session.exists())
				{
					Session.unset();
				}
			}
		}
		return -1;
	}
}
