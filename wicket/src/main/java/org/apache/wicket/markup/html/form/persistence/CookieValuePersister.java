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
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.time.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Helper class to simplify Cookie handling in combination with Wicket
 * 
 * @author Juergen Donnerstag
 * @author Jonathan Locke
 * 
 * @TODO rename to CookieUtils and move into util package in 1.5 (WICKET-2213)
 */
public class CookieValuePersister implements IValuePersister
{
	private static final long serialVersionUID = 1L;

	/** Logging */
	private final static Logger log = LoggerFactory.getLogger(CookieValuePersister.class);

	private final CookieValuePersisterSettings settings;

	/**
	 * Construct.
	 */
	public CookieValuePersister()
	{
		settings = new CookieValuePersisterSettings();
	}

	/**
	 * Construct.
	 * 
	 * @param settings
	 */
	public CookieValuePersister(final CookieValuePersisterSettings settings)
	{
		this.settings = settings;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.persistence.IValuePersister#clear(String)
	 */
	public void clear(final String key)
	{
		final Cookie cookie = getCookie(getSaveKey(key));
		if (cookie != null)
		{
			clear(cookie);
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.form.persistence.IValuePersister#clear(org.apache.wicket.markup.html.form.FormComponent)
	 */
	public void clear(FormComponent<?> formComponent)
	{
		clear(formComponent.getPageRelativePath());
	}

	/**
	 * @see org.apache.wicket.markup.html.form.persistence.IValuePersister#load(String)
	 */
	public String load(final String key)
	{
		final Cookie cookie = getCookie(getSaveKey(key));
		if (cookie != null)
		{
			return cookie.getValue();
		}
		return null;
	}

	/**
	 * @see org.apache.wicket.markup.html.form.persistence.IValuePersister#load(org.apache.wicket.markup.html.form.FormComponent)
	 */
	public void load(FormComponent<?> formComponent)
	{
		String value = load(formComponent.getPageRelativePath());
		if (value != null)
		{
			// Assign the retrieved/persisted value to the component
			formComponent.setModelValue(value.split(FormComponent.VALUE_SEPARATOR));
			// formComponent.setModelValue(new String[] { value });
		}
	}

	/**
	 * @see org.apache.wicket.markup.html.form.persistence.IValuePersister#save(String, String)
	 */
	public void save(String key, final String value)
	{
		key = getSaveKey(key);
		Cookie cookie = getCookie(key);
		if (cookie == null)
		{
			cookie = new Cookie(key, value == null ? "" : value);
		}
		else
		{
			cookie.setValue(value == null ? "" : value);
		}
		cookie.setSecure(false);
		cookie.setMaxAge(settings.getMaxAge());

		save(cookie);
	}

	/**
	 * @see org.apache.wicket.markup.html.form.persistence.IValuePersister#save(org.apache.wicket.markup.html.form.FormComponent)
	 */
	public void save(FormComponent<?> formComponent)
	{
		save(formComponent.getPageRelativePath(), formComponent.getValue());
		// save(formComponent.getPageRelativePath(), formComponent.getDefaultModelObjectAsString());
	}

	/**
	 * Make sure the 'key' does not contain any illegal chars. For cookies ':' is not allowed.
	 * 
	 * @param key
	 *            The key to be validated
	 * @return The save key
	 */
	protected String getSaveKey(String key)
	{
		if (Strings.isEmpty(key))
		{
			throw new IllegalArgumentException("A Cookie name can not be null or empty");
		}

		// cookie names cannot contain ':',
		// we replace ':' with '.' but first we have to encode '.' as '..'
		key = key.replace(".", "..");
		key = key.replace(":", ".");
		return key;
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

			if (log.isDebugEnabled())
			{
				log.debug("Removed Cookie: " + cookie.getName());
			}
		}
	}

	/**
	 * Gets any cookies for request.
	 * 
	 * @param name
	 *            The name of the cookie to be looked up
	 * 
	 * @return Any cookies for this request
	 */
	private Cookie getCookie(final String name)
	{
		try
		{
			Cookie cookie = getWebRequest().getCookie(name);
			if (log.isDebugEnabled())
			{
				if (cookie != null)
				{
					log.debug("Found Cookie with name=" + name + " and request URI=" +
						getWebRequest().getHttpServletRequest().getRequestURI());
				}
				else
				{
					log.debug("Unable to find Cookie with name=" + name + " and request URI=" +
						getWebRequest().getHttpServletRequest().getRequestURI());
				}
			}

			return cookie;
		}
		catch (NullPointerException ex)
		{
			// Ignore any app server problem here
		}

		return null;
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

		final String comment = settings.getComment();
		if (comment != null)
		{
			cookie.setComment(comment);
		}

		final String domain = settings.getDomain();
		if (domain != null)
		{
			cookie.setDomain(domain);
		}

		String path = getWebRequest().getHttpServletRequest().getContextPath();
		if (Strings.isEmpty(path))
		{
			path = "/";
		}
		cookie.setPath(path);
		cookie.setVersion(settings.getVersion());
		cookie.setSecure(settings.getSecure());

		getWebResponse().addCookie(cookie);

		if (log.isDebugEnabled())
		{
			log.debug("Cookie saved: " + cookieToDebugString(cookie) + "; request URI=" +
				getWebRequest().getHttpServletRequest().getRequestURI());
		}

		return cookie;
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
	 * Gets debug info as a string for the given cookie.
	 * 
	 * @param cookie
	 *            the cookie to debug.
	 * @return a string that represents the internals of the cookie.
	 */
	private String cookieToDebugString(final Cookie cookie)
	{
		return "[Cookie " + " name = " + cookie.getName() + ", value = " + cookie.getValue() +
			", domain = " + cookie.getDomain() + ", path = " + cookie.getPath() + ", maxAge = " +
			Time.valueOf(cookie.getMaxAge()).toDateString() + "(" + cookie.getMaxAge() + ")" + "]";
	}
}
