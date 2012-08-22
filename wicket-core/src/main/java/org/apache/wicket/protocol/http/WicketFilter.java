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
import java.util.HashSet;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.file.WebXmlFile;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Filter for initiating handling of Wicket requests.
 * <p>
 * The advantage of a filter is that, unlike a servlet, it can choose not to process the request and
 * let whatever is next in chain try. So when using a Wicket filter and a request comes in for
 * foo.gif the filter can choose not to process it because it knows it is not a wicket-related
 * request. Since the filter didn't process it, it falls on to the application server to try, and
 * then it works."
 * 
 * @see WicketServlet for documentation
 * 
 * @author Jonathan Locke
 * @author Timur Mehrvarz
 * @author Juergen Donnerstag
 * @author Igor Vaynberg (ivaynberg)
 * @author Al Maw
 * @author jcompagner
 * @author Matej Knopp
 */
public class WicketFilter implements Filter
{
	private static final Logger log = LoggerFactory.getLogger(WicketFilter.class);

	/** The name of the root path parameter that specifies the root dir of the app. */
	public static final String FILTER_MAPPING_PARAM = "filterMappingUrlPattern";

	/** The name of the context parameter that specifies application factory class */
	public static final String APP_FACT_PARAM = "applicationFactoryClassName";

	/**
	 * Name of parameter used to express a comma separated list of paths that should be ignored
	 */
	public static final String IGNORE_PATHS_PARAM = "ignorePaths";

	// Wicket's Application object
	private WebApplication application;

	/** the factory used to create the web aplication instance */
	private IWebApplicationFactory applicationFactory;

	private FilterConfig filterConfig;

	private String filterPath;

	// filterPath length without trailing "/"
	private int filterPathLength = -1;

	/** set of paths that should be ignored by the wicket filter */
	private final Set<String> ignorePaths = new HashSet<String>();

	/**
	 * A flag indicating whether WicketFilter is used directly or through WicketServlet
	 */
	private boolean isServlet = false;

	/**
	 * default constructor, usually invoked through the servlet 
	 * container by the web.xml configuration
	 */
	public WicketFilter()
	{
	}

	/**
	 * constructor supporting programmatic setup of the filter
	 * <p/>
	 *  this can be useful for programmatically creating and appending the 
	 *  wicket filter to the servlet context using servlet 3 features.
	 * 
	 * @param application
	 *           web application
	 */
	public WicketFilter(WebApplication application)
	{
		this.application = Args.notNull(application, "application");
	}

	/**
	 * @return The class loader
	 */
	protected ClassLoader getClassLoader()
	{
		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * This is Wicket's main method to execute a request
	 * 
	 * @param request
	 * @param response
	 * @param chain
	 * @return false, if the request could not be processed
	 * @throws IOException
	 * @throws ServletException
	 */
	boolean processRequest(ServletRequest request, final ServletResponse response,
		final FilterChain chain) throws IOException, ServletException
	{
		final ThreadContext previousThreadContext = ThreadContext.detach();

		// Assume we are able to handle the request
		boolean res = true;

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader newClassLoader = getClassLoader();

		try
		{
			if (previousClassLoader != newClassLoader)
			{
				Thread.currentThread().setContextClassLoader(newClassLoader);
			}

			HttpServletRequest httpServletRequest = (HttpServletRequest)request;
			HttpServletResponse httpServletResponse = (HttpServletResponse)response;

			// Make sure getFilterPath() gets called before checkIfRedirectRequired()
			String filterPath = getFilterPath(httpServletRequest);

			if (filterPath == null)
			{
				throw new IllegalStateException("filter path was not configured");
			}

			if (shouldIgnorePath(httpServletRequest))
			{
				log.debug("Ignoring request {}", httpServletRequest.getRequestURL());
				if (chain != null)
				{
					chain.doFilter(request, response);
				}
				return false;
			}

			String redirectURL = checkIfRedirectRequired(httpServletRequest);
			if (redirectURL == null)
			{
				// No redirect; process the request
				ThreadContext.setApplication(application);

				WebRequest webRequest = application.createWebRequest(httpServletRequest, filterPath);
				WebResponse webResponse = application.createWebResponse(webRequest,
					httpServletResponse);

				RequestCycle requestCycle = application.createRequestCycle(webRequest, webResponse);
				if (!requestCycle.processRequestAndDetach())
				{
					if (chain != null)
					{
						chain.doFilter(request, response);
					}
					res = false;
				}
				else
				{
					webResponse.flush();
				}
			}
			else
			{
				if (Strings.isEmpty(httpServletRequest.getQueryString()) == false)
				{
					redirectURL += "?" + httpServletRequest.getQueryString();
				}

				try
				{
					// send redirect - this will discard POST parameters if the request is POST
					// - still better than getting an error because of lacking trailing slash
					httpServletResponse.sendRedirect(httpServletResponse.encodeRedirectURL(redirectURL));
				}
				catch (IOException e)
				{
					throw new RuntimeException(e);
				}
			}
		}
		finally
		{
			ThreadContext.restore(previousThreadContext);

			if (newClassLoader != previousClassLoader)
			{
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}

			if (response.isCommitted())
			{
				response.flushBuffer();
			}
		}
		return res;
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	@Override
	public void doFilter(final ServletRequest request, final ServletResponse response,
		final FilterChain chain) throws IOException, ServletException
	{
		processRequest(request, response, chain);
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
				// see http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6500212
				// final Class<?> factoryClass = Thread.currentThread()
				// .getContextClassLoader()
				// .loadClass(appFactoryClassName);
				final Class<?> factoryClass = Class.forName(appFactoryClassName, false,
					Thread.currentThread().getContextClassLoader());

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
	 * If you do have a need to subclass, you may subclass {@link #init(boolean, FilterConfig)}
	 * 
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	@Override
	public final void init(final FilterConfig filterConfig) throws ServletException
	{
		init(false, filterConfig);
	}

	/**
	 * Servlets and Filters are treated essentially the same with Wicket. This is the entry point
	 * for both of them.
	 * 
	 * @see #init(FilterConfig)
	 * 
	 * @param isServlet
	 *            True if Servlet, false if Filter
	 * @param filterConfig
	 * @throws ServletException
	 */
	public void init(final boolean isServlet, final FilterConfig filterConfig)
		throws ServletException
	{
		this.filterConfig = filterConfig;
		this.isServlet = isServlet;
		initIgnorePaths(filterConfig);

		// locate application instance unless it was already specified during construction
		if (application == null)
		{
			applicationFactory = getApplicationFactory();
			application = applicationFactory.createApplication(this);
		}

		application.setName(filterConfig.getFilterName());
		application.setWicketFilter(this);

		// Allow the filterPath to be preset via setFilterPath()
		String configureFilterPath = getFilterPath();

		if (configureFilterPath == null)
		{
			configureFilterPath = getFilterPathFromConfig(filterConfig);
		}

		if (configureFilterPath == null)
		{
			configureFilterPath = getFilterPathFromWebXml(isServlet, filterConfig);
		}

		if (configureFilterPath == null)
		{
			configureFilterPath = getFilterPathFromAnnotation(isServlet);
		}

		if (configureFilterPath != null)
		{
			setFilterPath(configureFilterPath);
		}

		if (getFilterPath() == null)
		{
			log.warn("Unable to determine filter path from filter init-parm, web.xml, "
				+ "or servlet 3.0 annotations. Assuming user will set filter path "
				+ "manually by calling setFilterPath(String)");
		}

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader newClassLoader = getClassLoader();

		ThreadContext.setApplication(application);
		try
		{
			if (previousClassLoader != newClassLoader)
			{
				Thread.currentThread().setContextClassLoader(newClassLoader);
			}

			application.initApplication();

			// Give the application the option to log that it is started
			application.logStarted();
		}
		finally
		{
			ThreadContext.detach();

			if (newClassLoader != previousClassLoader)
			{
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}
		}
	}

	/**
	 * Stub method that lets subclasses configure filter path from annotations.
	 * 
	 * @param isServlet
	 * @return Filter path from annotation
	 */
	protected String getFilterPathFromAnnotation(boolean isServlet)
	{
		// @formatter:off
		/* TODO JAVA6,SERVLET3.0
		 * the code below is disabled because servlet 3.0 requires java 6 and wicket still supports java 5
		 * for now the code below will go into a wicket-stuff module
		String[] patterns = null;

		if (isServlet)
		{
			WebServlet servlet = getClass().getAnnotation(WebServlet.class);
			if (servlet != null)
			{
				patterns = servlet.urlPatterns();
			}
		}
		else
		{
			WebFilter filter = getClass().getAnnotation(WebFilter.class);
			if (filter != null)
			{
				patterns = filter.urlPatterns();
			}
		}
		if (patterns != null && patterns.length > 0)
		{
			String pattern = patterns[0];
			if (patterns.length > 1)
			{
				log.warn(
					"Multiple url patterns defined for Wicket filter/servlet, using the first: {}",
					pattern);
			}
			return pattern;
		}
		*/
		// @formatter:on
		return null;
	}

	/**
	 * 
	 * @param isServlet
	 * @param filterConfig
	 * @return filter path from web.xml
	 */
	protected String getFilterPathFromWebXml(final boolean isServlet,
		final FilterConfig filterConfig)
	{
		return new WebXmlFile().getUniqueFilterPath(isServlet, filterConfig);
	}

	/**
	 * @return filter config
	 */
	public FilterConfig getFilterConfig()
	{
		return filterConfig;
	}

	/**
	 * Either get the filterPath retrieved from web.xml, or if not found the old (1.3) way via a
	 * filter mapping param.
	 * 
	 * @param request
	 * @return filterPath
	 */
	protected String getFilterPath(final HttpServletRequest request)
	{
		return filterPath;
	}

	/**
	 * Provide a standard getter for filterPath.
	 * @return The configured filterPath.
	 */
	protected String getFilterPath()
	{
		return filterPath;
	}

	/**
	 * 
	 * @param filterConfig
	 * @return filter path
	 */
	protected String getFilterPathFromConfig(FilterConfig filterConfig)
	{
		String result = filterConfig.getInitParameter(FILTER_MAPPING_PARAM);
		if (result != null)
		{
			if (result.equals("/*"))
			{
				result = "";
			}
			else if (!result.startsWith("/") || !result.endsWith("/*"))
			{
				throw new WicketRuntimeException("Your " + FILTER_MAPPING_PARAM +
					" must start with \"/\" and end with \"/*\". It is: " + result);
			}
			else
			{
				// remove leading "/" and trailing "*"
				result = result.substring(1, result.length() - 1);
			}
		}
		return result;
	}

	/**
	 * @see javax.servlet.Filter#destroy()
	 */
	@Override
	public void destroy()
	{
		if (application != null)
		{
			try
			{
				ThreadContext.setApplication(application);
				application.internalDestroy();
			}
			finally
			{
				ThreadContext.detach();
				application = null;
			}
		}

		if (applicationFactory != null)
		{
			applicationFactory.destroy(this);
		}
	}

	/**
	 * Try to determine as fast as possible if a redirect is necessary
	 * 
	 * @param request
	 * @return null, if no redirect is necessary. Else the redirect URL
	 */
	private String checkIfRedirectRequired(final HttpServletRequest request)
	{
		return checkIfRedirectRequired(request.getRequestURI(), request.getContextPath());
	}

	/**
	 * Try to determine as fast as possible if a redirect is necessary
	 * 
	 * @param requestURI
	 * @param contextPath
	 * @return null, if no redirect is necessary. Else the redirect URL
	 */
	protected final String checkIfRedirectRequired(final String requestURI, final String contextPath)
	{
		// length without jsessionid (http://.../abc;jsessionid=...?param)
		int uriLength = requestURI.indexOf(';');
		if (uriLength == -1)
		{
			uriLength = requestURI.length();
		}

		// request.getContextPath() + "/" + filterPath. But without any trailing "/".
		int homePathLength = contextPath.length() +
			(filterPathLength > 0 ? 1 + filterPathLength : 0);
		if (uriLength != homePathLength)
		{
			// requestURI and homePath are different (in length)
			// => continue with standard request processing. No redirect.
			return null;
		}

		// Fail fast failed. Revert to "slow" but exact check
		String uri = Strings.stripJSessionId(requestURI);

		// home page without trailing slash URI
		String homePageUri = contextPath + '/' + getFilterPath();
		if (homePageUri.endsWith("/"))
		{
			homePageUri = homePageUri.substring(0, homePageUri.length() - 1);
		}

		// If both are equal => redirect
		if (uri.equals(homePageUri))
		{
			uri += "/";
			return uri;
		}

		// no match => standard request processing; no redirect
		return null;
	}

	/**
	 * Sets the filter path instead of reading it from web.xml.
	 * 
	 * Please note that you must subclass WicketFilter.init(FilterConfig) and set your filter path
	 * before you call super.init(filterConfig).
	 * 
	 * @param filterPath
	 */
	public final void setFilterPath(String filterPath)
	{
		// see https://issues.apache.org/jira/browse/WICKET-701
		if (this.filterPath != null)
		{
			throw new IllegalStateException(
				"Filter path is write-once. You can not change it. Current value='" + filterPath + '\'');
		}
		if (filterPath != null)
		{
			filterPath = canonicaliseFilterPath(filterPath);

			// We only need to determine it once. It'll not change.
			if (filterPath.endsWith("/"))
			{
				filterPathLength = filterPath.length() - 1;
			}
			else
			{
				filterPathLength = filterPath.length();
			}
		}
		this.filterPath = filterPath;
	}

	/**
	 * Returns a relative path to the filter path and context root from an HttpServletRequest - use
	 * this to resolve a Wicket request.
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
		if (isServlet)
		{
			String servletPath = request.getServletPath();
			path = path.substring(servletPath.length());
		}

		if (path.length() > 0)
		{
			path = path.substring(1);
		}

		// We should always be under the rootPath, except
		// for the special case of someone landing on the
		// home page without a trailing slash.
		String filterPath = getFilterPath();
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

	protected WebApplication getApplication()
	{
		return application;
	}

	/**
	 * Checks whether this is a request to an ignored path
	 * 
	 * @param request
	 *            the current http request
	 * @return {@code true} when the request should be ignored, {@code false} - otherwise
	 */
	private boolean shouldIgnorePath(final HttpServletRequest request)
	{
		boolean ignore = false;
		if (ignorePaths.size() > 0)
		{
			String relativePath = getRelativePath(request);
			if (Strings.isEmpty(relativePath) == false)
			{
				for (String path : ignorePaths)
				{
					if (relativePath.startsWith(path))
					{
						ignore = true;
						break;
					}
				}
			}
		}

		return ignore;
	}

	/**
	 * initializes the ignore paths parameter
	 * 
	 * @param filterConfig
	 */
	private void initIgnorePaths(final FilterConfig filterConfig)
	{
		String paths = filterConfig.getInitParameter(IGNORE_PATHS_PARAM);
		if (Strings.isEmpty(paths) == false)
		{
			String[] parts = Strings.split(paths, ',');
			for (String path : parts)
			{
				path = path.trim();
				if (path.startsWith("/"))
				{
					path = path.substring(1);
				}
				ignorePaths.add(path);
			}
		}
	}

	/**
	 * A filterPath should have all leading slashes removed and exactly one trailing slash. A
	 * wildcard asterisk character has no special meaning. If your intention is to mean the top
	 * level "/" then an empty string should be used instead.
	 *
	 * @param filterPath
	 * @return
	 */
	static String canonicaliseFilterPath(String filterPath)
	{
		if (Strings.isEmpty(filterPath))
		{
			return filterPath;
		}

		int beginIndex = 0;
		int endIndex = filterPath.length();
		while (beginIndex < endIndex)
		{
			char c = filterPath.charAt(beginIndex);
			if (c != '/')
			{
				break;
			}
			beginIndex++;
		}
		int o;
		int i = o = beginIndex;
		while (i < endIndex)
		{
			char c = filterPath.charAt(i);
			i++;
			if (c != '/')
			{
				o = i;
			}
		}
		if (o < endIndex)
		{
			o++; // include exactly one trailing slash
			filterPath = filterPath.substring(beginIndex, o);
		}
		else
		{
			// ensure to append trailing slash
			filterPath = filterPath.substring(beginIndex) + '/';
		}

		if (filterPath.equals("/"))
		{
			return "";
		}
		return filterPath;
	}
}
