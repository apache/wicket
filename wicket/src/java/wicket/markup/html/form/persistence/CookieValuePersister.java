/*
 * $Id$
 * $Revision$ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.markup.html.form.persistence;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.RequestCycle;
import wicket.markup.html.form.FormComponent;
import wicket.protocol.http.HttpRequest;
import wicket.protocol.http.HttpResponse;
import wicket.util.time.Time;

/**
 * THIS CLASS IS FOR INTERNAL USE ONLY AND IS NOT MEANT TO BE USED BY FRAMEWORK
 * CLIENTS. <br/>
 * 
 * This class implements IFormComponentValuePersister by means of the HTTP
 * protocol.
 * 
 * @see CookieValuePersister
 * @see javax.servlet.http.Cookie for more details.
 * @author Juergen Donnerstag
 */
public class CookieValuePersister implements IValuePersister
{
	/** Logging */
	private final static Log log = LogFactory.getLog(CookieValuePersister.class);

	/**
	 * @see wicket.markup.html.form.persistence.IValuePersister#clear(wicket.markup.html.form.FormComponent)
	 */
	public void clear(FormComponent component)
	{
		final Cookie cookie = getCookie(component);
		if (cookie != null)
		{
			remove(cookie);
			if (log.isDebugEnabled())
				log.debug("Cookie for " + component + " removed");
		}
	}

	/**
	 * @see wicket.markup.html.form.persistence.IValuePersister#load(wicket.markup.html.form.FormComponent)
	 */
	public void load(FormComponent component)
	{
		final Cookie cookie = getCookie(component);
		if (cookie != null)
		{
			final String value = cookie.getValue();
			if (value != null)
			{
				// Assign the retrieved/persisted value to the component
				component.setValue(value);
			}
		}
	}

	/**
	 * @see wicket.markup.html.form.persistence.IValuePersister#save(wicket.markup.html.form.FormComponent)
	 */
	public void save(FormComponent component)
	{
		final String name = component.getPageRelativePath();
		final String value = component.getValue();

		Cookie cookie = new Cookie(name, value == null ? "" : value);
		cookie.setSecure(false);
		cookie.setMaxAge(getSettings().getMaxAge());

		save(cookie);
	}

	/**
	 * Gets the cookie for a given persistent form component. The name of the
	 * cookie will be the component's page relative path (@see
	 * wicket.markup.html.form.FormComponent#getPageRelativePath()). Be reminded
	 * that only if the cookie data have been provided by the client (browser),
	 * they'll be accessible by the server.
	 * 
	 * @param component
	 *            The form component
	 * @return The cookie for the component or null if none is available
	 */
	private Cookie getCookie(final FormComponent component)
	{
		// Gets the cookie's name
		final String name = component.getPageRelativePath();

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
					if (cookie.getValue() != null && cookie.getValue().length() > 0)
					{
						if (log.isDebugEnabled())
						{
							log.debug("Got cookie: " + getCookieDebugString(cookie));
						}
						return cookie;
					}
					else
					{
						if (log.isDebugEnabled())
						{
							log.debug("Got cookie " + name
									+ ", but it had no value; returning null");
						}
					}
				}
			}
		}

		return null;
	}

	/**
	 * Gets debug info as a string for the given cookie.
	 * 
	 * @param cookie
	 *            the cookie to debug.
	 * @return a string that represents the internals of the cookie.
	 */
	private String getCookieDebugString(Cookie cookie)
	{
		return "[Cookie " + " name = " + cookie.getName() + ", value = " + cookie.getValue()
				+ ", domain = " + cookie.getDomain() + ", path = " + cookie.getPath() + ", maxAge = "
				+ Time.valueOf(cookie.getMaxAge()).toDateString() + "(" + cookie.getMaxAge() + ")"
				+ "]";
	}

	/**
	 * Persister defaults are maintained centrally by the Application.
	 * 
	 * @return Persister default value
	 */
	private CookieValuePersisterSettings getSettings()
	{
		return RequestCycle.get().getApplication().getSettings()
				.getCookieValuePersisterSettings();
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
	 *            The cookie to delete
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
	 *            The Cookie to be persisted.
	 * @return The cookie provided
	 */
	private Cookie save(Cookie cookie)
	{
		if (cookie == null)
		{
			return null;
		}

		if (getSettings().getComment() != null)
		{
			cookie.setComment(getSettings().getComment());
		}

		if (getSettings().getDomain() != null)
		{
			cookie.setDomain(getSettings().getDomain());
		}

		cookie.setVersion(getSettings().getVersion());
		cookie.setSecure(getSettings().isSecure());

		getResponse().addCookie(cookie);

		if (log.isDebugEnabled())
		{
			log.debug("saved: " + getCookieDebugString(cookie));
		}

		return cookie;
	}
}