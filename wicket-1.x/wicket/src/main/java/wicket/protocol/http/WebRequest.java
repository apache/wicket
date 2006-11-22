/*
 * $Id: WebRequest.java 4194 2006-02-08 10:39:03 -0800 (Wed, 08 Feb 2006)
 * jonathanlocke $ $Revision$ $Date: 2006-02-08 10:39:03 -0800 (Wed, 08
 * Feb 2006) $
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

import java.util.Locale;
import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;

import wicket.Request;
import wicket.util.lang.Bytes;

/**
 * Subclass of Request for HTTP protocol requests which holds an underlying
 * HttpServletRequest object. A variety of convenience methods are available
 * that operate on the HttpServletRequest object. These methods do things such
 * as providing access to parameters, cookies, URLs and path information.
 * 
 * @author Jonathan Locke
 */
public abstract class WebRequest extends Request
{
	/**
	 * Gets the application context path.
	 * 
	 * @return application context path
	 */
	public abstract String getContextPath();

	/**
	 * Get the requests' cookies
	 * 
	 * @return Cookies
	 */
	public Cookie[] getCookies()
	{
		return getHttpServletRequest().getCookies();
	}

	/**
	 * Gets the wrapped http servlet request object.
	 * <p>
	 * WARNING: it is usually a bad idea to depend on the http servlet request
	 * directly. Please use the classes and methods that are exposed by Wicket
	 * (such as {@link wicket.Session} instead. Send an email to the mailing
	 * list in case it is not clear how to do things or you think you miss
	 * functionality which causes you to depend on this directly.
	 * </p>
	 * 
	 * @return the wrapped http serlvet request object.
	 */
	public abstract HttpServletRequest getHttpServletRequest();

	/**
	 * Returns the preferred <code>Locale</code> that the client will accept
	 * content in, based on the Accept-Language header. If the client request
	 * doesn't provide an Accept-Language header, this method returns the
	 * default locale for the server.
	 * 
	 * @return the preferred <code>Locale</code> for the client
	 */
	public abstract Locale getLocale();

	/**
	 * Gets the request parameter with the given key.
	 * 
	 * @param key
	 *            Parameter name
	 * @return Parameter value
	 */
	public abstract String getParameter(final String key);

	/**
	 * Gets the request parameters.
	 * 
	 * @return Map of parameters
	 */
	public abstract Map getParameterMap();

	/**
	 * Gets the request parameters with the given key.
	 * 
	 * @param key
	 *            Parameter name
	 * @return Parameter values
	 */
	public abstract String[] getParameters(final String key);

	/**
	 * Gets the servlet path.
	 * 
	 * @return Servlet path
	 */
	public abstract String getServletPath();

	/**
	 * Retrieves the URL of this request for local use.
	 * 
	 * @return The request URL for local use, which is the context path + the
	 *         relative url
	 */
	public String getURL()
	{
		/*
		 * Servlet 2.3 specification : Context Path: The path prefix associated
		 * with the ServletContext that this servlet is a part of. If this
		 * context is the default context rooted at the base of the web server's
		 * URL namespace, this path will be an empty string. Otherwise, this
		 * path starts with a "/" character but does not end with a "/"
		 * character.
		 */
		return getContextPath() + '/' + getRelativeURL();
	}

	/**
	 * Create a runtime context type specific (e.g. Servlet or Portlet)
	 * MultipartWebRequest wrapper for handling multipart content uploads.
	 * 
	 * @param maxSize
	 *            the maximum size this request may be
	 * @return new WebRequest wrapper implementing MultipartWebRequest
	 */
	public abstract WebRequest newMultipartWebRequest(Bytes maxSize);
	
	/**
	 * Is the request an ajax request?
	 *
	 * @return True if the ajax is an ajax request. False if it's not.
	 */
	public abstract boolean isAjax();
}
