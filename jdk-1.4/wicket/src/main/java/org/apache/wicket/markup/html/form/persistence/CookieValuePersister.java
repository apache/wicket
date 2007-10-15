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
package org.apache.wicket.markup.html.form.persistence;

import javax.servlet.http.Cookie;

import org.apache.wicket.RequestCycle;
import org.apache.wicket.markup.html.form.FormComponent;
import org.apache.wicket.protocol.http.WebRequest;
import org.apache.wicket.protocol.http.WebResponse;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * This class implements IValuePersister by means of HTTP cookies.
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 */
public class CookieValuePersister implements IValuePersister
{
	private static final long serialVersionUID = 1L;

	/** Logging */
	private final static Logger log = LoggerFactory.getLogger(CookieValuePersister.class);

	/**
	 * @see org.apache.wicket.markup.html.form.persistence.IValuePersister#clear(org.apache.wicket.markup.html.form.FormComponent)
	 */
	public void clear(final FormComponent component)
	{
		final Cookie cookie = getCookie(component);
		if (cookie != null)
		{
			clear(cookie);
			if (log.isDebugEnabled())
			{
				log.debug("Cookie for " + component + " removed");
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.form.persistence.IValuePersister#load(org.apache.wicket.markup.html.form.FormComponent)
	 */
	public void load(final FormComponent component)
	{
		final Cookie cookie = getCookie(component);
		if (cookie != null)
		{
			final String value = cookie.getValue();
			if (value != null)
			{
				// Assign the retrieved/persisted value to the component
				component.setModelValue(value.split(FormComponent.VALUE_SEPARATOR));
			}
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.form.persistence.IValuePersister#save(org.apache.wicket.markup.html.form.FormComponent)
	 */
	public void save(final FormComponent component)
	{
		final String name = getName(component);
		final String value = component.getValue();

		Cookie cookie = getCookie(component);
		if (cookie == null)
		{
			cookie = new Cookie(name, value == null ? "" : value);
		}
		else
		{
			cookie.setValue(value == null ? "" : value);
		}
		cookie.setSecure(false);
		cookie.setMaxAge(getSettings().getMaxAge());

		save(cookie);
	}

	/**
	 * @param component
	 *            Component to get name for
	 * @return The name of the component.
	 */
	protected String getName(final FormComponent component)
	{
		return component.getPageRelativePath();
	}

	/**
	 * Convenience method for deleting a cookie by name. Delete the cookie by setting its maximum
	 * age to zero.
	 * 
	 * @param cookie
	 *            The cookie to delete
	 */
	private void clear(final Cookie cookie)
	{
		if (cookie != null)
		{
			// Delete the cookie by setting its maximum age to zero
			cookie.setMaxAge(0);
			cookie.setValue(null);

			save(cookie);
		}
	}

	/**
	 * Gets debug info as a string for the given cookie.
	 * 
	 * @param cookie
	 *            the cookie to debug.
	 * @return a string that represents the internals of the cookie.
	 */
	private String cookieToDebugString(final Cookie cookie)
	{
		return "[Cookie " + " name = " + cookie.getName() + ", value = " + cookie.getValue() +
				", domain = " + cookie.getDomain() + ", path = " + cookie.getPath() +
				", maxAge = " + Time.valueOf(cookie.getMaxAge()).toDateString() + "(" +
				cookie.getMaxAge() + ")" + "]";
	}

	/**
	 * Gets the cookie for a given persistent form component. The name of the cookie will be the
	 * component's page relative path (@see
	 * org.apache.wicket.markup.html.form.FormComponent#getPageRelativePath()). Be reminded that
	 * only if the cookie data have been provided by the client (browser), they'll be accessible by
	 * the server.
	 * 
	 * @param component
	 *            The form component
	 * @return The cookie for the component or null if none is available
	 */
	private Cookie getCookie(final FormComponent component)
	{
		// Gets the cookie's name
		final String name = getName(component);

		// Get the cookie attached to the Request by the client browser
		Cookie cookie = getCookie(name);
		if (cookie != null)
		{
			// cookie with no value do me no good!
			if (cookie.getValue() != null && cookie.getValue().length() > 0)
			{
				if (log.isDebugEnabled())
				{
					log.debug("Got cookie: " + cookieToDebugString(cookie));
				}
				return cookie;
			}
			else
			{
				if (log.isDebugEnabled())
				{
					log.debug("Got cookie " + name + ", but it had no value; returning null");
				}
			}
		}
		return null;
	}

	/**
	 * Gets any cookies for request.
	 * 
	 * @param name
	 *            The name of the cookie to be looked up
	 * 
	 * @return Any cookies for this request
	 */
	private Cookie getCookie(String name)
	{
		try
		{
			return getWebRequest().getCookie(name);
		}
		catch (NullPointerException ex)
		{
			// Ignore any app server problem here
		}

		return null;
	}

	/**
	 * Persister defaults are maintained centrally by the Application.
	 * 
	 * @return Persister default value
	 */
	private CookieValuePersisterSettings getSettings()
	{
		return RequestCycle.get().getApplication().getSecuritySettings()
				.getCookieValuePersisterSettings();
	}

	/**
	 * Convenience method to get the http request.
	 * 
	 * @return WebRequest related to the RequestCycle
	 */
	private WebRequest getWebRequest()
	{
		return (WebRequest)RequestCycle.get().getRequest();
	}

	/**
	 * Convenience method to get the http response.
	 * 
	 * @return WebResponse related to the RequestCycle
	 */
	private WebResponse getWebResponse()
	{
		return (WebResponse)RequestCycle.get().getResponse();
	}

	/**
	 * Persist/save the data using Cookies.
	 * 
	 * @param cookie
	 *            The Cookie to be persisted.
	 * @return The cookie provided
	 */
	private Cookie save(final Cookie cookie)
	{
		if (cookie == null)
		{
			return null;
		}
		else
		{
			final String comment = getSettings().getComment();
			if (comment != null)
			{
				cookie.setComment(comment);
			}

			final String domain = getSettings().getDomain();
			if (domain != null)
			{
				cookie.setDomain(domain);
			}

			cookie.setPath("/");
			// cookie.setPath(getWebRequest().getContextPath());

			cookie.setVersion(getSettings().getVersion());
			cookie.setSecure(getSettings().getSecure());

			getWebResponse().addCookie(cookie);

			if (log.isDebugEnabled())
			{
				log.debug("saved: " + cookieToDebugString(cookie));
			}

			return cookie;
		}
	}
}
