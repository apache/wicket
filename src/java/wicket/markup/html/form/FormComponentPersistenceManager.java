/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.markup.html.form;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RequestCycle;
import wicket.protocol.http.HttpRequest;
import wicket.protocol.http.HttpResponse;
import wicket.util.time.Time;

/**
 * THIS CLASS IS FOR INTERNAL USE ONLY AND IS NOT MEANT TO BE USED BY FRAMEWORK
 * CLIENTS.<br/>
 * 
 * This is an attempt to abstract the implementation details of cookies away.
 * Wicket users (and developer) should not need to care about Cookies. In that
 * context the persister is responsible to store and retrieve FormComponent
 * data.<br/>
 * 
 * The persistence manager is responsible to store and retrieve a
 * FormComponent's data by means of Cookies. That is, by means of the HTTP
 * protocol the data are transferred to the client to be stored locally. And are
 * transmitted from the client to the server whenever the Cookie's path matches
 * the request URI.
 * 
 * @see javax.servlet.http.Cookie for more details.
 * 
 * @author Juergen Donnerstag
 */
public class FormComponentPersistenceManager implements IFormComponentPersistenceManager
{
	/** Logging */
	private final static Log log = LogFactory.getLog(FormComponentPersistenceManager.class);

	/**
	 * (Protected) Constructor. Only Form should be able to instantiate a
	 * persister.
	 */
	protected FormComponentPersistenceManager()
	{
	}

	/**
	 * Remove data related to a FormComponent
	 * 
	 * @param name
	 *           The "primary key" of the data to be deleted
	 * @return the cookie that was removed or null if none was found.
	 */
	public Cookie remove(String name)
	{
		Cookie cookie = retrieve(name);
		if (cookie != null)
		{
			remove(cookie);
			if (log.isDebugEnabled())
				log.debug("cookie " + name + " removed");
		}
		return cookie;
	}


	/**
	 * Retrieve a persisted Cookie by means of its name which in wicket context
	 * by default is the components page relative path (@see
	 * wicket.markup.html.form.FormComponent#getPageRelativePath()). Be reminded
	 * that only if the cookie data have been provided by the client (browser),
	 * they'll be accessible by the server.
	 * 
	 * @param name
	 *           The "primary key" to find the data
	 * @return the cookie (if found), null if not found
	 */
	public Cookie retrieve(String name)
	{
		// Get all cookies attached to the Request by the client browser
		Cookie[] cookies = getRequest().getCookies();
		if (cookies != null)
		{
			for (int i = 0; i < cookies.length; i++)
			{
				Cookie cookie = cookies[i];

				// Names must match and Value must not be empty
				if (cookie.getName().equals(name))
				{
					// cookies with no value do me no good!
					if ((cookie.getValue() != null) && (cookie.getValue().length() > 0))
					{
						if (log.isDebugEnabled())
						{
							log.debug("retrieved: " + getCookieDebugString(cookie));
						}
						return cookie;
					}
					else
					{
						if (log.isDebugEnabled())
						{
							log
									.debug("retrieved cookie " + name
											+ ", but it had no value; returning null");
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Convenience method to retrieve the value of a cookie right away.
	 * 
	 * @param name
	 *           The "primary key" to find the data
	 * @return The value related to the name (key) or null if a cookie with the
	 *         given name was not found
	 */
	public String retrieveValue(String name)
	{
		Cookie cookie = retrieve(name);
		if (cookie == null)
		{
			return null;
		}

		return cookie.getValue();
	}

	/**
	 * Convenience method.
	 * 
	 * @param name
	 *           The FormComponent's name
	 * @param value
	 *           The FormComponent's value
	 * @return The cookie created, based on defaults and the params provided
	 */
	public Cookie save(String name, String value)
	{
		return save(name, value, getRequest().getContextPath());
	}

	/**
	 * Convenience method.
	 * 
	 * @param name
	 *           The FormComponent's name
	 * @param value
	 *           The FormComponent's value
	 * @param path
	 * @see javax.servlet.http.Cookie#setPath(java.lang.String) for details
	 * @return The cookie created, based on defaults and the params provided
	 */
	public Cookie save(String name, String value, String path)
	{
		if (value == null)
		{
			value = "";
		}

		Cookie cookie = new Cookie(name, value);
		cookie.setSecure(false);
		cookie.setPath(path);
		cookie.setMaxAge(getDefaults().getMaxAge());

		return save(cookie);
	}

	/**
	 * Gets debug info as a string for the given cookie.
	 * 
	 * @param cookie
	 *           the cookie to debug.
	 * @return a string that represents the internals of the cookie.
	 */
	private String getCookieDebugString(Cookie cookie)
	{
		return "cookie{" + "name=" + cookie.getName() + ",value=" + cookie.getValue() + ",domain="
				+ cookie.getDomain() + ",path=" + cookie.getPath() + ",maxAge="
				+ Time.valueOf(cookie.getMaxAge()).toDateString() + "(" + cookie.getMaxAge() + ")"
				+ "}";
	}

	/**
	 * Persister defaults are maintained centrally by the Application.
	 * 
	 * @return Persister default value
	 */
	private FormComponentPersistenceDefaults getDefaults()
	{
		return RequestCycle.get().getApplication().getSettings()
				.getFormComponentPersistenceDefaults();
	}

	/**
	 * Convenience method to get the http request.
	 * 
	 * @return HttpRequest related to the RequestCycle
	 */
	private HttpRequest getRequest()
	{
		return (HttpRequest)RequestCycle.get().getRequest();
	}

	/**
	 * Convinience method to get the http response.
	 * 
	 * @return HttpResponse related to the RequestCycle
	 */
	private HttpResponse getResponse()
	{
		return (HttpResponse)RequestCycle.get().getResponse();
	}

	/**
	 * Convenience method for deleting a cookie by name. Delete the cookie by
	 * setting its maximum age to zero.
	 * 
	 * @param cookie
	 *           The cookie to delete
	 */
	private void remove(Cookie cookie)
	{
		if (cookie != null)
		{
			// Delete the cookie by setting its maximum age to zero
			cookie.setMaxAge(0);
			cookie.setValue(null);
			cookie.setPath(getRequest().getContextPath());
			save(cookie);
		}
	}

	/**
	 * Persist/save the data using Cookies.
	 * 
	 * @param cookie
	 *           The Cookie to be persisted.
	 * @return The cookie provided
	 */
	private Cookie save(Cookie cookie)
	{
		if (cookie == null)
		{
			return null;
		}

		if (getDefaults().getComment() != null)
		{
			cookie.setComment(getDefaults().getComment());
		}

		if (getDefaults().getDomain() != null)
		{
			cookie.setDomain(getDefaults().getDomain());
		}

		cookie.setVersion(getDefaults().getVersion());
		cookie.setSecure(getDefaults().isSecure());

		getResponse().addCookie(cookie);

		if (log.isDebugEnabled())
		{
			log.debug("saved: " + getCookieDebugString(cookie));
		}

		return cookie;
	}
}
