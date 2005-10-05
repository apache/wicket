/*
 * $Id$ $Revision$
 * $Date$
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

import wicket.Request;
import wicket.util.lang.Bytes;

/**
 * Subclass of Request for HTTP protocol requests which holds an underlying
 * HttpServletRequest object. A NULL request object implementing all methods as
 * NOPs is available via WebRequest#NULL. A variety of convenience methods are
 * available that operate on the HttpServletRequest object. These methods do
 * things such as providing access to parameters, cookies, URLs and path
 * information.
 * 
 * @author Jonathan Locke
 */
public abstract class WebRequest extends Request
{
	/** Null WebRequest object that does nothing */
	public static final WebRequest NULL = new NullWebRequest();

	/**
	 * Gets the application context path.
	 * 
	 * @return application context path
	 */
	public abstract String getContextPath();

	/**
	 * Gets the relative url (url without the context path and without a leading
	 * '/'). Use this method to load resources using the servlet context.
	 * 
	 * @return Request URL
	 */
	public abstract String getRelativeURL();

	/**
	 * Gets the servlet path.
	 * 
	 * @return Servlet path
	 */
	public abstract String getServletPath();

	/**
	 * Create a runtime context type specific (e.g. Servlet or Portlet) MultipartWebRequest wrapper
	 * for handling multipart content uploads.
	 * 
	 * @param maxSize the maximum size this request may be
	 * @return new WebRequest wrapper implementing MultipartWebRequest
	 */
    public abstract WebRequest newMultipartWebRequest(Bytes maxSize);

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
	 * Retrieves the URL of this request for local use.
	 *
	 * @return The request URL for local use, which is the context path + the relative url
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
}
