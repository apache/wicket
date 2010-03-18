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

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.wicket.ThreadContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.protocol.http.servlet.ServletWebResponse;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * Filter for initiating handling of Wicket requests.
 * 
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

	static final String SERVLET_PATH_HOLDER = "<servlet>";

	private WebApplication webApplication;

	private FilterConfig filterConfig;

	private String filterPath;

	private final boolean servletMode = false;

	/**
	 * @return The class loader
	 */
	protected ClassLoader getClassLoader()
	{
		return Thread.currentThread().getContextClassLoader();
	}

	/**
	 * Checks if the request is for home page and lacks trailing slash. If necessary redirects to
	 * URL with trailing slash.
	 * 
	 * @param request
	 * @param response
	 * @param filterPath
	 * @return <code>true</code> if there is a trailing slash, <code>false</code> if redirect was
	 *         necessary.
	 */
	private boolean checkForTrailingSlash(HttpServletRequest request, HttpServletResponse response,
		String filterPath)
	{
		// current URI
		String uri = Strings.stripJSessionId(request.getRequestURI());

		// home page without trailing slash URI
		String homePageUri = request.getContextPath() + "/" + filterPath;
		if (homePageUri.endsWith("/"))
		{
			homePageUri = homePageUri.substring(0, homePageUri.length() - 1);
		}

		if (uri.equals(homePageUri))
		{
			// construct redirect URL
			String redirect = uri + "/";
			if (!Strings.isEmpty(request.getQueryString()))
			{
				redirect += "?" + request.getQueryString();
			}
			try
			{
				// send redirect - this will discard POST parameters if the request is POST
				// - still better than getting an error because of lacking trailing slash
				response.sendRedirect(response.encodeRedirectURL(redirect));
			}
			catch (IOException e)
			{
				throw new RuntimeException(e);
			}
			return false;
		}

		return true;
	}

	boolean processRequest(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException
	{
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

			String filterPath = getFilterPath(httpServletRequest);

			webApplication.set();

			if (checkForTrailingSlash(httpServletRequest, httpServletResponse, filterPath))
			{
				ServletWebRequest req;

				req = new ServletWebRequest(httpServletRequest, filterPath);

				WebResponse resp = new HeaderBufferingWebResponse(new ServletWebResponse(
					httpServletRequest, httpServletResponse));

				RequestCycle requestCycle = webApplication.createRequestCycle(req, resp);

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
					resp.flush();
				}
			}
		}
		finally
		{
			ThreadContext.detach();

			if (newClassLoader != previousClassLoader)
			{
				Thread.currentThread().setContextClassLoader(previousClassLoader);
			}

			response.flushBuffer();
		}
		return res;
	}

	/**
	 * @see javax.servlet.Filter#doFilter(javax.servlet.ServletRequest,
	 *      javax.servlet.ServletResponse, javax.servlet.FilterChain)
	 */
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
		throws IOException, ServletException
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
	 * @see javax.servlet.Filter#init(javax.servlet.FilterConfig)
	 */
	public void init(FilterConfig filterConfig) throws ServletException
	{
		this.filterConfig = filterConfig;
		IWebApplicationFactory factory = getApplicationFactory();
		webApplication = factory.createApplication(this);
		webApplication.setName(filterConfig.getFilterName());
		webApplication.setWicketFilter(this);

		initFilterPath();

		final ClassLoader previousClassLoader = Thread.currentThread().getContextClassLoader();
		final ClassLoader newClassLoader = getClassLoader();

		webApplication.set();
		try
		{
			if (previousClassLoader != newClassLoader)
			{
				Thread.currentThread().setContextClassLoader(newClassLoader);
			}

			webApplication.initApplication();
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
	 * @return filter config
	 */
	public FilterConfig getFilterConfig()
	{
		return filterConfig;
	}

	/**
	 * 
	 */
	private void initFilterPath()
	{
		InputStream is = filterConfig.getServletContext().getResourceAsStream("/WEB-INF/web.xml");
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
	};

	/**
	 * 
	 * @param filterName
	 * @param name
	 * @param node
	 * @return
	 */
	private String getFilterPath(String filterName, String name, Node node)
	{
		String foundUrlPattern = null;
		String foundFilterName = null;

		for (int i = 0; i < node.getChildNodes().getLength(); ++i)
		{
			Node n = node.getChildNodes().item(i);
			if (name.equals(n.getNodeName()))
			{
				foundFilterName = n.getTextContent();
			}
			else if ("url-pattern".equals(n.getNodeName()))
			{
				foundUrlPattern = n.getTextContent();
			}
		}

		if (foundFilterName != null)
		{
			foundFilterName = foundFilterName.trim();
		}


		if (filterName.equals(foundFilterName))
		{
			return (foundUrlPattern != null) ? foundUrlPattern.trim() : null;
		}
		else
		{
			return null;
		}
	}

	/**
	 * 
	 * @param filterName
	 * @param mapping
	 * @param name
	 * @param nodeList
	 * @return
	 */
	private String getFilterPath(String filterName, String mapping, String name, NodeList nodeList)
	{
		for (int i = 0; i < nodeList.getLength(); ++i)
		{
			Node node = nodeList.item(i);
			if (mapping.equals(node.getNodeName()))
			{
				String path = getFilterPath(filterName, name, node);
				if (path != null)
				{
					return path;
				}
			}
			else
			{
				String path = getFilterPath(filterName, mapping, name, node.getChildNodes());
				if (path != null)
				{
					return path;
				}
			}
		}
		return null;
	}

	/**
	 * 
	 * @param filterName
	 * @param is
	 * @return
	 * @throws ServletException
	 */
	private String getFilterPath(String filterName, InputStream is) throws ServletException
	{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		try
		{
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.parse(is);

			String prefix = servletMode ? "servlet" : "filter";
			String mapping = prefix + "-mapping";
			String name = prefix + "-name";

			String urlPattern = getFilterPath(filterName, mapping, name, document.getChildNodes());
			return stripWildcard(urlPattern);
		}
		catch (Exception e)
		{
			throw new ServletException(e);
		}
	}

	/**
	 * 
	 * @param request
	 * @return
	 */
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
	 * @see javax.servlet.Filter#destroy()
	 */
	public void destroy()
	{
		if (webApplication != null)
		{
			webApplication.internalDestroy();
			webApplication = null;
		}
	}
}
