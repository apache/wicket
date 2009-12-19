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
package org.apache.wicket.protocol.http.servlet;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.RequestContext;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.ng.request.IRequestParameters;
import org.apache.wicket.ng.request.Url;
import org.apache.wicket.ng.request.Url.QueryParameter;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WicketURLDecoder;
import org.apache.wicket.util.io.Streams;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.apache.wicket.util.string.StringValue;
import org.apache.wicket.util.string.StringValueConversionException;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.upload.FileUploadException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Servlet specific WebRequest implementation wrapping a HttpServletRequest
 * 
 * @author Ate Douma
 */
public class ServletWebRequest extends WebRequest
{
	/** Log */
	private static final Logger log = LoggerFactory.getLogger(ServletWebRequest.class);

	/** Servlet request information. */
	private final HttpServletRequest httpServletRequest;

	private int depthRelativeToWicketHandler = -1;
	private String relativePathPrefixToWicketHandler;
	private String relativePathPrefixToContextRoot;
	private Map parameterMap;

	private String wicketRedirectUrl;

	private int previousUrlDepth;

	/** Marks this request as an ajax request. */
	private boolean ajax;

	private final Url url;

	/**
	 * Protected constructor.
	 * 
	 * @param httpServletRequest
	 *            The servlet request information
	 * @deprecated use ServletWebRequest(HttpServletRequest httpServletRequest, String filterPrefix)
	 */
	@Deprecated
	public ServletWebRequest(final HttpServletRequest httpServletRequest)
	{
		this(httpServletRequest, null);

	}

	/**
	 * Construct.
	 * 
	 * @param httpServletRequest
	 * 
	 * @param filterPrefix
	 *            contentPath + filterPath, used to extract the actual {@link Url}
	 */
	public ServletWebRequest(HttpServletRequest httpServletRequest, String filterPrefix)
	{
		Checks.argumentNotNull(httpServletRequest, "httpServletRequest");
		// TODO WICKET-NG reenable once sorted
		// Checks.argumentNotNull(filterPrefix, "filterPrefix");

		url = (filterPrefix != null) ? getUrl(httpServletRequest, filterPrefix) : null;
		this.httpServletRequest = httpServletRequest;

		ajax = false;
		String ajaxHeader = httpServletRequest.getHeader("Wicket-Ajax");

		if (Strings.isEmpty(ajaxHeader))
			ajaxHeader = httpServletRequest.getParameter("wicket:ajax");

		if (Strings.isEmpty(ajaxHeader) == false)
		{
			try
			{
				ajax = Strings.isTrue(ajaxHeader);
			}
			catch (StringValueConversionException e)
			{
				// We are not interested in this exception but we log it anyway
				log.debug("Couldn't convert the Wicket-Ajax header: " + ajaxHeader);
			}
		}
	}


	/**
	 * Gets the wrapped http servlet request object.
	 * 
	 * @return the wrapped http serlvet request object.
	 */
	@Override
	public final HttpServletRequest getHttpServletRequest()
	{
		return httpServletRequest;
	}

	/**
	 * Returns the preferred <code>Locale</code> that the client will accept content in, based on
	 * the Accept-Language header. If the client request doesn't provide an Accept-Language header,
	 * this method returns the default locale for the server.
	 * 
	 * @return the preferred <code>Locale</code> for the client
	 */
	@Override
	public Locale getLocale()
	{
		return httpServletRequest.getLocale();
	}

	/**
	 * Gets the request parameter with the given key.
	 * 
	 * @param key
	 *            Parameter name
	 * @return Parameter value
	 */
	@Override
	public String getParameter(final String key)
	{
		return httpServletRequest.getParameter(key);
	}

	/**
	 * Gets the request parameters.
	 * 
	 * @return Map of parameters
	 */
	@Override
	public Map getParameterMap()
	{
		// Lazy-init parameter map. Only make one copy. It's more efficient, and
		// we can add stuff to it (which the BookmarkablePage stuff does).
		if (parameterMap == null)
		{
			parameterMap = new HashMap(httpServletRequest.getParameterMap());
		}
		// return a mutable copy
		return parameterMap;
	}

	/**
	 * Gets the request parameters with the given key.
	 * 
	 * @param key
	 *            Parameter name
	 * @return Parameter values
	 */
	@Override
	public String[] getParameters(final String key)
	{
		return httpServletRequest.getParameterValues(key);
	}

	/**
	 * Gets the path info if any.
	 * 
	 * @return Any servlet path info
	 */
	@Override
	public String getPath()
	{
		return ((WebApplication)Application.get()).getWicketFilter().getRelativePath(
			httpServletRequest);
	}

	/**
	 * @see org.apache.wicket.Request#getRelativePathPrefixToContextRoot()
	 */
	@Override
	public String getRelativePathPrefixToContextRoot()
	{
		if (relativePathPrefixToContextRoot != null)
		{
			return relativePathPrefixToContextRoot;
		}

		if (RequestContext.get().isPortletRequest())
		{
			return relativePathPrefixToContextRoot = getHttpServletRequest().getContextPath() + "/";
		}

		// Prepend to get back to the wicket handler.
		String tmp = getRelativePathPrefixToWicketHandler();
		PrependingStringBuffer prepender = new PrependingStringBuffer(tmp);

		String path = WicketURLDecoder.PATH_INSTANCE.decode(getPath());

		if (path == null || path.length() == 0)
		{
			path = "";
		}

		// Now prepend to get back from the wicket handler to the root context.

		// Find the absolute path for the wicket filter/servlet
		String wicketPath = "";

		// We're running as a filter.
		// Note: do not call RequestUtils.decode() on getServletPath ... it is
		// already url-decoded (JIRA WICKET-1624)
		String servletPath = getServletPath();

		// We need to substitute the %3A (or the other way around) to be able to
		// get a good match, as parts of the path may have been escaped while
		// others arent
		if (servletPath.endsWith(path))
		{
			int len = servletPath.length() - path.length() - 1;
			if (len < 0)
			{
				len = 0;
			}
			wicketPath = servletPath.substring(0, len);
		}
		// We're running as a servlet
		else
		{
			wicketPath = servletPath;
		}

		for (int i = 0; i < wicketPath.length(); i++)
		{
			if (wicketPath.charAt(i) == '/')
			{
				prepender.prepend("../");
			}
		}
		return relativePathPrefixToContextRoot = prepender.toString();
	}

	/**
	 * Gets the depth of this request relative to the Wicket handler.
	 * 
	 * @return the depth
	 */
	public int getDepthRelativeToWicketHandler()
	{
		if (depthRelativeToWicketHandler == -1)
		{
			// Initialize it.
			getRelativePathPrefixToWicketHandler();
		}
		return depthRelativeToWicketHandler;
	}

	/**
	 * @see org.apache.wicket.Request#getRelativePathPrefixToWicketHandler()
	 */
	@Override
	public String getRelativePathPrefixToWicketHandler()
	{
		if (relativePathPrefixToWicketHandler != null)
		{
			return relativePathPrefixToWicketHandler;
		}

		boolean portletRequest = RequestContext.get().isPortletRequest();

		PrependingStringBuffer prepender = new PrependingStringBuffer();

		// For AJAX requests, we need to make the URLs relative to the
		// original page.
		if (!portletRequest && isAjax())
		{
			for (int i = 0; i < getObsoleteRequestParameters().getUrlDepth(); i++)
			{
				prepender.prepend("../");
			}
			return relativePathPrefixToWicketHandler = prepender.toString();
		}

		String relativeUrl = getPath();

		/*
		 * We might be serving an error page.
		 * 
		 * In this case, the request will appear to be for something like "/ErrorPage", whereas the
		 * URL in the user's browser will actually be something like
		 * "/foo/page/where/the/error/actually/happened".
		 * 
		 * We need to generate links and resource URLs relative to the URL in the browser window,
		 * not the internal request for the error page.
		 * 
		 * This original URL is available from request attributes, so we look in there and use that
		 * for the relative path if it's available.
		 */

		HttpServletRequest httpRequest = getHttpServletRequest();

		// This is in the Servlet 2.3 spec giving us the URI of the resource
		// that caused the error. Unfortunately, this includes the context path.
		String errorUrl = (String)httpRequest.getAttribute("javax.servlet.error.request_uri");

		// This gives us a context-relative path for RequestDispatcher.forward
		// stuff, with a leading slash.
		String forwardUrl = (String)httpRequest.getAttribute("javax.servlet.forward.servlet_path");

		if (forwardUrl != null && forwardUrl.length() > 0)
		{
			// If this is an error page, this will be /mount or /?wicket:foo
			relativeUrl = forwardUrl.substring(1);
		}
		else if (errorUrl != null)
		{
			// Strip off context path from front of URI.
			errorUrl = errorUrl.substring(httpRequest.getContextPath().length());

			String servletPath = httpRequest.getServletPath();
			if (!errorUrl.startsWith(servletPath))
			{
				prepender.prepend(servletPath.substring(1) + "/");
			}
			for (int i = servletPath.length() + 1; i < errorUrl.length(); i++)
			{
				if (errorUrl.charAt(i) == '?')
				{
					break;
				}
				if (errorUrl.charAt(i) == '/')
				{
					prepender.prepend("../");
				}
			}
			return relativePathPrefixToWicketHandler = prepender.toString();
		}
		else if (wicketRedirectUrl != null)
		{
			relativeUrl = wicketRedirectUrl;
		}

		int lastPathPos = -1;
		if (depthRelativeToWicketHandler == -1)
		{
			int depth = 0;
			int ajaxUrlDepth = isAjax() ? getObsoleteRequestParameters().getUrlDepth() : -1;
			for (int i = 0; i < relativeUrl.length(); i++)
			{
				if (relativeUrl.charAt(i) == '?')
				{
					break;
				}
				if (relativeUrl.charAt(i) == '/')
				{
					depth++;
					lastPathPos = i;
					if (depth == ajaxUrlDepth)
					{
						return relativeUrl.substring(0, lastPathPos + 1);
					}
				}
			}
			depthRelativeToWicketHandler = depth;
		}

		if (portletRequest)
		{
			prepender.prepend("/");
			prepender.prepend(getHttpServletRequest().getServletPath());
			prepender.prepend(getHttpServletRequest().getContextPath());
		}
		else
		{
			for (int i = 0; i < depthRelativeToWicketHandler; i++)
			{
				prepender.prepend("../");
			}
		}

		return relativePathPrefixToWicketHandler = prepender.toString();
	}


	@Override
	public Url getUrl()
	{
		if (url != null)
		{
			return new Url(url);
		}
		else
		{
			return getUrlObsolete();
		}
	}

	/**
	 * @see org.apache.wicket.Request#getUrl()
	 * @deprecated
	 * 
	 *             TODO WICKET-NG remove, kept here just for reference of how old url was parsed
	 */
	@Deprecated
	public Url getUrlObsolete()
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
		String url = getServletPath();
		final String pathInfo = httpServletRequest.getPathInfo();

		if (pathInfo != null)
		{
			url += pathInfo;
		}

		final String queryString = httpServletRequest.getQueryString();

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
		return Url.parse(url);
	}

	/**
	 * Gets the servlet path.
	 * 
	 * @return Servlet path
	 */
	@Override
	public String getServletPath()
	{
		return httpServletRequest.getServletPath();
	}

	/**
	 * This will return true if the header "Wicket-Ajax" is set.
	 * 
	 * @see org.apache.wicket.protocol.http.WebRequest#isAjax()
	 */
	@Override
	public final boolean isAjax()
	{
		return ajax;
	}

	/**
	 * THIS IS FOR WICKET INTERNAL USE ONLY. DO NOT USE IT IN YOUR APPLICATION.
	 * 
	 * @param ajax
	 *            ajax
	 */
	public final void setAjax(boolean ajax)
	{
		this.ajax = ajax;
	}


	/**
	 * @see org.apache.wicket.protocol.http.WebRequest#newMultipartWebRequest(org.apache.wicket.util.lang.Bytes)
	 */
	@Override
	public WebRequest newMultipartWebRequest(Bytes maxsize)
	{
		try
		{
			MultipartServletWebRequest multipart = new MultipartServletWebRequest(
				httpServletRequest, maxsize);
			multipart.setObsoleteRequestParameters(getObsoleteRequestParameters());
			return multipart;
		}
		catch (FileUploadException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * Set the redirect url where wicket will redirect to for the next page
	 * 
	 * @param wicketRedirectUrl
	 */
	public void setWicketRedirectUrl(String wicketRedirectUrl)
	{
		this.wicketRedirectUrl = wicketRedirectUrl;
		depthRelativeToWicketHandler = -1;
		relativePathPrefixToContextRoot = null;
		relativePathPrefixToWicketHandler = null;

		if (wicketRedirectUrl != null)
		{
			previousUrlDepth = getObsoleteRequestParameters().getUrlDepth();

			getObsoleteRequestParameters().setUrlDepth(getDepthRelativeToWicketHandler());
		}
		else
		{
			getObsoleteRequestParameters().setUrlDepth(previousUrlDepth);
			getDepthRelativeToWicketHandler();
		}

	}

	/**
	 * @see org.apache.wicket.Request#getQueryString()
	 */
	@Override
	public String getQueryString()
	{
		return httpServletRequest.getQueryString();
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "[method = " + httpServletRequest.getMethod() + ", protocol = " +
			httpServletRequest.getProtocol() + ", requestURL = " +
			httpServletRequest.getRequestURL() + ", contentType = " +
			httpServletRequest.getContentType() + ", contentLength = " +
			httpServletRequest.getContentLength() + ", contextPath = " +
			httpServletRequest.getContextPath() + ", pathInfo = " +
			httpServletRequest.getPathInfo() + ", requestURI = " +
			httpServletRequest.getRequestURI() + ", servletPath = " +
			httpServletRequest.getServletPath() + ", pathTranslated = " +
			httpServletRequest.getPathTranslated() + "]";
	}

	private Url getUrl(HttpServletRequest request, String filterPrefix)
	{
		if (filterPrefix.length() > 0 && !filterPrefix.endsWith("/"))
		{
			filterPrefix += "/";
		}
		StringBuilder url = new StringBuilder();
		String uri = request.getRequestURI();
		url.append(Strings.stripJSessionId(uri.substring(request.getContextPath().length() +
			filterPrefix.length() + 1)));

		String query = request.getQueryString();
		if (!Strings.isEmpty(query))
		{
			url.append("?");
			url.append(query);
		}

		return Url.parse(Strings.stripJSessionId(url.toString()));
	}

	@Override
	public long getDateHeader(String name)
	{
		return httpServletRequest.getDateHeader(name);
	}

	@Override
	public String getHeader(String name)
	{
		return httpServletRequest.getHeader(name);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<String> getHeaders(String name)
	{
		List<String> result = new ArrayList<String>();
		Enumeration<String> e = httpServletRequest.getHeaders(name);
		while (e.hasMoreElements())
		{
			result.add(e.nextElement());
		}
		return Collections.unmodifiableList(result);
	}

	private Map<String, List<StringValue>> postParameters = null;

	@SuppressWarnings("unchecked")
	private Map<String, List<StringValue>> getPostParameters()
	{
		if (postParameters == null)
		{
			postParameters = new HashMap<String, List<StringValue>>();
			try
			{
				BufferedReader reader = getHttpServletRequest().getReader();
				String value = Streams.readString(reader);

				if (!Strings.isEmpty(value))
				{
					Url url = Url.parse("?" + value);
					for (QueryParameter q : url.getQueryParameters())
					{
						List<StringValue> list = postParameters.get(q.getName());
						if (list == null)
						{
							list = new ArrayList<StringValue>();
							postParameters.put(q.getName(), list);
						}
						list.add(StringValue.valueOf(q.getValue()));
					}
				}
			}
			catch (IOException e)
			{
				log.warn(
					"Error parsing request body for post parameters; Fallback to ServletRequest#getParameters().",
					e);
				for (String name : (List<String>)Collections.list(getHttpServletRequest().getParameterNames()))
				{
					List<StringValue> list = postParameters.get(name);
					if (list == null)
					{
						list = new ArrayList<StringValue>();
						postParameters.put(name, list);
					}
					for (String value : getHttpServletRequest().getParameterValues(name))
					{
						list.add(StringValue.valueOf(value));
					}
				}
			}

		}
		return postParameters;
	}

	private final IRequestParameters postRequestParameters = new IRequestParameters()
	{
		public Set<String> getParameterNames()
		{
			return Collections.unmodifiableSet(getPostParameters().keySet());
		}

		public StringValue getParameterValue(String name)
		{
			List<StringValue> values = getPostParameters().get(name);
			if (values == null || values.isEmpty())
			{
				return StringValue.valueOf((String)null);
			}
			else
			{
				return values.iterator().next();
			}
		}

		public List<StringValue> getParameterValues(String name)
		{
			List<StringValue> values = getPostParameters().get(name);
			if (values != null)
			{
				values = Collections.unmodifiableList(values);
			}
			return values;
		}
	};

	@Override
	public IRequestParameters getPostRequestParameters()
	{
		return postRequestParameters;
	}

	@Override
	public Cookie[] getCookies()
	{
		return httpServletRequest.getCookies();
	}


}
