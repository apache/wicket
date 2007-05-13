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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.IRedirectListener;
import org.apache.wicket.RequestListenerInterface;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.util.lang.Bytes;
import org.apache.wicket.util.string.PrependingStringBuffer;
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

	/**
	 * Protected constructor.
	 * 
	 * @param httpServletRequest
	 *            The servlet request information
	 */
	public ServletWebRequest(final HttpServletRequest httpServletRequest)
	{
		this.httpServletRequest = httpServletRequest;
	}

	/**
	 * Gets the wrapped http servlet request object.
	 * 
	 * @return the wrapped http serlvet request object.
	 */
	public final HttpServletRequest getHttpServletRequest()
	{
		return httpServletRequest;
	}

	/**
	 * Returns the preferred <code>Locale</code> that the client will accept
	 * content in, based on the Accept-Language header. If the client request
	 * doesn't provide an Accept-Language header, this method returns the
	 * default locale for the server.
	 * 
	 * @return the preferred <code>Locale</code> for the client
	 */
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
	public String getParameter(final String key)
	{
		return httpServletRequest.getParameter(key);
	}

	/**
	 * Gets the request parameters.
	 * 
	 * @return Map of parameters
	 */
	public Map getParameterMap()
	{
		// return a mutable copy
		return new HashMap(httpServletRequest.getParameterMap());
	}

	/**
	 * Gets the request parameters with the given key.
	 * 
	 * @param key
	 *            Parameter name
	 * @return Parameter values
	 */
	public String[] getParameters(final String key)
	{
		return httpServletRequest.getParameterValues(key);
	}

	/**
	 * Gets the path info if any.
	 * 
	 * @return Any servlet path info
	 */
	public String getPath()
	{
		return ((WebApplication)Application.get()).getWicketFilter().getRelativePath(
				httpServletRequest);
	}

	public String getRelativePathPrefixToContextRoot()
	{
		if (relativePathPrefixToWicketHandler != null)
		{
			return relativePathPrefixToWicketHandler;
		}
		String tmp = getRelativePathPrefixToWicketHandler();
		String servletPath = getServletPath();
		if (servletPath == null || servletPath.length() == 0)
		{
			return tmp;
		}

		PrependingStringBuffer prepender = new PrependingStringBuffer(tmp);
		for (int i = 1; i < servletPath.length(); i++)
		{
			if (servletPath.charAt(i) == '?')
			{
				break;
			}
			if (servletPath.charAt(i) == '/')
			{
				prepender.prepend("../");
			}
		}
		return relativePathPrefixToWicketHandler = prepender.toString();
	}
	
	/**
	 * Gets the depth of this request relative to the Wicket handler.
	 * @return
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
	
	public String getRelativePathPrefixToWicketHandler()
	{
		if (relativePathPrefixToWicketHandler != null)
		{
			return relativePathPrefixToWicketHandler;
		}
		String relativeUrl = getPath();
		PrependingStringBuffer prepender = new PrependingStringBuffer();

		/*
		 * We might be serving an error page.
		 * 
		 * In this case, the request will appear to be for something like
		 * "/ErrorPage", whereas the URL in the user's browser will actually be
		 * something like "/foo/page/where/the/error/actually/happened".
		 * 
		 * We need to generate links and resource URLs relative to the URL in
		 * the browser window, not the internal request for the error page.
		 * 
		 * This original URL is available from request attributes, so we look in
		 * there and use that for the relative path if it's available.
		 */

		HttpServletRequest httpRequest = getHttpServletRequest();

		// This is in the Servlet 2.3 spec giving us the URI of the resource
		// that caused the error. Unfortunately, this includes the context path.
		String errorUrl = (String)httpRequest.getAttribute("javax.servlet.error.request_uri");

		// This gives us a context-relative path for RequestDispatcher.forward stuff, with a leading slash.
		String forwardUrl = (String)httpRequest.getAttribute("javax.servlet.forward.servlet_path");

		if (errorUrl != null)
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
			return prepender.toString();
		}
		
		if (forwardUrl != null)
		{
			// Strip off leading slash, if forwardUrl has any length.
			relativeUrl = forwardUrl.substring(relativeUrl.length() > 0 ? 1 : 0);
		}

		if (depthRelativeToWicketHandler == -1)
		{
			int depth = 0;
			for (int i = 0; i < relativeUrl.length(); i++)
			{
				if (relativeUrl.charAt(i) == '?')
				{
					break;
				}
				if (relativeUrl.charAt(i) == '/')
				{
					depth++;
				}
			}
			depthRelativeToWicketHandler = depth;
		}
		
		for (int i = 0; i < depthRelativeToWicketHandler; i++)
		{
			prepender.prepend("../");
		}
		
		return relativePathPrefixToWicketHandler = prepender.toString();
	}

	/**
	 * @see org.apache.wicket.Request#getURL()
	 */
	public String getURL()
	{
		/*
		 * Servlet 2.3 specification :
		 * 
		 * Servlet Path: The path section that directly corresponds to the
		 * mapping which activated this request. This path starts with a "/"
		 * character except in the case where the request is matched with the
		 * "/*" pattern, in which case it is the empty string.
		 * 
		 * PathInfo: The part of the request path that is not part of the
		 * Context Path or the Servlet Path. It is either null if there is no
		 * extra path, or is a string with a leading "/".
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
		return url;
	}

	/**
	 * Gets the servlet path.
	 * 
	 * @return Servlet path
	 */
	public String getServletPath()
	{
		return httpServletRequest.getServletPath();
	}

	/**
	 * This will return true if the header "Wicket-Ajax" is set.
	 * 
	 * @see org.apache.wicket.protocol.http.WebRequest#isAjax()
	 */
	// TODO matej? should we have a simple way of supporting other ajax things?
	// or should they just set that same header??
	public boolean isAjax()
	{
		boolean ajax = false;

		String ajaxHeader = httpServletRequest.getHeader("Wicket-Ajax");
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

		return ajax;
	}

	/**
	 * This method by default calls isAjax(), wicket ajax request do have an
	 * header set. And for all the ajax request the versioning should be merged
	 * with the previous one. And when it sees that the current request is a
	 * redirect to page request the version will also be merged with the
	 * previous one because refresh in the browser or redirects to a page
	 * shouldn't generate a new version.
	 * 
	 * @see org.apache.wicket.Request#mergeVersion()
	 */
	public boolean mergeVersion()
	{
		RequestListenerInterface intface = getRequestParameters().getInterface();
		return isAjax() || intface == IRedirectListener.INTERFACE;
	}

	/**
	 * @see org.apache.wicket.protocol.http.WebRequest#newMultipartWebRequest(org.apache.wicket.util.lang.Bytes)
	 */
	public WebRequest newMultipartWebRequest(Bytes maxsize)
	{
		try
		{
			return new MultipartServletWebRequest(httpServletRequest, maxsize);
		}
		catch (FileUploadException e)
		{
			throw new WicketRuntimeException(e);
		}
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	public String toString()
	{
		return "[method = " + httpServletRequest.getMethod() + ", protocol = "
				+ httpServletRequest.getProtocol() + ", requestURL = "
				+ httpServletRequest.getRequestURL() + ", contentType = "
				+ httpServletRequest.getContentType() + ", contentLength = "
				+ httpServletRequest.getContentLength() + ", contextPath = "
				+ httpServletRequest.getContextPath() + ", pathInfo = "
				+ httpServletRequest.getPathInfo() + ", requestURI = "
				+ httpServletRequest.getRequestURI() + ", servletPath = "
				+ httpServletRequest.getServletPath() + ", pathTranslated = "
				+ httpServletRequest.getPathTranslated() + "]";
	}
}
