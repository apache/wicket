/*
 * $Id$ $Revision$ $Date$
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
package wicket.protocol.http.servlet;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.WicketRuntimeException;
import wicket.protocol.http.WebApplication;
import wicket.protocol.http.WebRequest;
import wicket.util.lang.Bytes;
import wicket.util.string.StringValueConversionException;
import wicket.util.string.Strings;
import wicket.util.upload.FileUploadException;

/**
 * A Servlet specific WebRequest implementation wrapping a HttpServletRequest
 * 
 * @author Ate Douma
 */
public class ServletWebRequest extends WebRequest
{
	/** Servlet request information. */
	private final HttpServletRequest httpServletRequest;
	
	/** Log */
	private static final Log log = LogFactory.getLog(ServletWebRequest.class); 

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
	 * Gets the servlet context path.
	 * 
	 * @return Servlet context path
	 */
	@Override
	public String getContextPath()
	{
		return httpServletRequest.getContextPath();
	}

	/**
	 * Returns the preferred <code>Locale</code> that the client will accept
	 * content in, based on the Accept-Language header. If the client request
	 * doesn't provide an Accept-Language header, this method returns the
	 * default locale for the server.
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
	public Map<String, Object> getParameterMap()
	{
		final Map<String, Object> map = new HashMap<String, Object>();

		for (final Enumeration enumeration = httpServletRequest.getParameterNames(); enumeration
				.hasMoreElements();)
		{
			final String name = (String)enumeration.nextElement();
			String[] parameterValues = httpServletRequest.getParameterValues(name);
			if (parameterValues.length == 1)
			{
				map.put(name, parameterValues[0]);
			}
			else
			{
				map.put(name, parameterValues);
			}
		}

		return map;
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
		String url = httpServletRequest.getRequestURI();
		String rootPath = ((WebApplication)Application.get()).getRootPath();
		if (url.startsWith(rootPath))
		{
			return url.substring(rootPath.length());
		}
		return null;
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
	 * Gets the relative url (url without the context path and without a leading
	 * '/'). Use this method to load resources using the servlet context.
	 * 
	 * @return Request URL
	 */
	@Override
	public String getRelativeURL()
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
		String url = httpServletRequest.getServletPath();
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

		// If url is non-empty it has to start with '/', which we should lose
		if (!url.equals(""))
		{
			// Remove leading '/'
			url = url.substring(1);
		}
		return url;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
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

	/**
	 * @see wicket.protocol.http.WebRequest#newMultipartWebRequest(wicket.util.lang.Bytes)
	 */
	@Override
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

	@Override
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
				log.debug("Couldn't convert the Wicket-Ajax header: "+ajaxHeader);
			}
		}
		
		return ajax;
	}
}