/*
 * $Id: CookieValuePersister.java 5763 2006-05-19 09:29:03 +0000 (Fri, 19 May
 * 2006) jonathanlocke $ $Revision$ $Date: 2006-05-19 09:29:03 +0000
 * (Fri, 19 May 2006) $
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
package wicket.markup.html.form.persistence;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.Cookie;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.Application;
import wicket.RequestCycle;
import wicket.markup.html.form.FormComponent;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebResponse;
import wicket.util.time.Time;

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
	private final static Log log = LogFactory.getLog(CookieValuePersister.class);

	/**
	 * @see wicket.markup.html.form.persistence.IValuePersister#clear(wicket.markup.html.form.FormComponent)
	 */
	public void clear(final FormComponent component)
	{
		final CookieWrapper cookie = getCookie(component);
		if (cookie != null)
		{
			clear(cookie.getCookie());
			if (log.isDebugEnabled())
			{
				log.debug("Cookie for " + component + " removed");
			}
		}
	}

	/**
	 * @see wicket.markup.html.form.persistence.IValuePersister#load(wicket.markup.html.form.FormComponent)
	 */
	public void load(final FormComponent component)
	{
		final CookieWrapper cookie = getCookie(component);
		if (cookie != null)
		{
			String value = cookie.getValue();
			if (value != null)
			{
				// Assign the retrieved/persisted value to the component
				component.setModelValue(value.split(FormComponent.VALUE_SEPARATOR));
			}
		}
	}

	/**
	 * @see wicket.markup.html.form.persistence.IValuePersister#save(wicket.markup.html.form.FormComponent)
	 */
	public void save(final FormComponent component)
	{
		CookieWrapper cookie = getCookie(component);
		if (cookie == null)
		{
			cookie = new CookieWrapper(getName(component), component.getModelObjectAsString());
		}
		else
		{
			cookie.setValue(component.getModelObjectAsString());
		}
		cookie.getCookie().setSecure(false);
		cookie.getCookie().setMaxAge(getSettings().getMaxAge());

		save(cookie.getCookie());
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
	 * Convenience method for deleting a cookie by name. Delete the cookie by
	 * setting its maximum age to zero.
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
	private String cookieToDebugString(final CookieWrapper cookie)
	{
		return "[Cookie " + " name = " + cookie.getName() + ", value = " + cookie.getValue()
				+ ", domain = " + cookie.getCookie().getDomain() + ", path = "
				+ cookie.getCookie().getPath() + ", maxAge = "
				+ Time.valueOf(cookie.getCookie().getMaxAge()).toDateString() + "("
				+ cookie.getCookie().getMaxAge() + ")" + "]";
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
	private CookieWrapper getCookie(final FormComponent component)
	{
		// Gets the cookie's name
		String name = getName(component);

		// Get all cookies attached to the Request by the client browser
		List<CookieWrapper> cookies = getCookies();
		if (cookies != null)
		{
			for (CookieWrapper cookie : cookies)
			{
				// Names must match and Value must not be empty
				if (cookie.getName().equals(name))
				{
					// cookies with no value do me no good!
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
	 * Gets any cookies for request.
	 * 
	 * @return Any cookies for this request
	 */
	private List<CookieWrapper> getCookies()
	{
		final List<CookieWrapper> cookies = new ArrayList<CookieWrapper>();
		try
		{
			for (Cookie cookie : getWebRequest().getCookies())
			{
				cookies.add(new CookieWrapper(cookie));
			}
		}
		catch (NullPointerException ex)
		{
			// Ignore any app server problem here
		}

		return cookies;
	}

	/**
	 * Persister defaults are maintained centrally by the Application.
	 * 
	 * @return Persister default value
	 */
	private CookieValuePersisterSettings getSettings()
	{
		return Application.get().getSecuritySettings().getCookieValuePersisterSettings();
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
	 * Convinience method to get the http response.
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

		cookie.setPath(getWebRequest().getContextPath());

		cookie.setVersion(getSettings().getVersion());
		cookie.setSecure(getSettings().getSecure());

		getWebResponse().addCookie(cookie);

		if (log.isDebugEnabled())
		{
			log.debug("saved: " + cookieToDebugString(new CookieWrapper(cookie)));
		}

		return cookie;
	}

	/**
	 * A wrapper class for Cookie which does the encoding/decoding of NON-ASCII
	 * chars
	 * 
	 */
	private static class CookieWrapper
	{
		/** The cookie with ASCII only */
		private Cookie cookie;

		/** real name */
		private String name;

		/** real value */
		private String value;

		/**
		 * Construct.
		 * 
		 * @param cookie
		 */
		private CookieWrapper(final Cookie cookie)
		{
			this.cookie = cookie;
			this.name = EncoderDecoder.decode(cookie.getName());
			this.value = EncoderDecoder.decode(cookie.getValue());
		}

		/**
		 * Construct. Create a new Cookie.
		 * 
		 * @param name
		 * @param value
		 */
		private CookieWrapper(final String name, final String value)
		{
			final String encodedName = EncoderDecoder.encode(name);
			final String encodedValue = EncoderDecoder.encode(value == null ? "" : value);
			this.cookie = new Cookie(encodedName, encodedValue);
		}

		/**
		 * @return Cookie name
		 */
		public String getName()
		{
			return this.name;
		}

		/**
		 * @return Cookie value
		 */
		public String getValue()
		{
			return this.value;
		}

		/**
		 * @return The cookie
		 */
		public Cookie getCookie()
		{
			return this.cookie;
		}

		/**
		 * @param value
		 */
		public void setValue(final String value)
		{
			final String encodedValue = EncoderDecoder.encode(value == null ? "" : value);
			this.cookie.setValue(encodedValue);
		}
	}

	/**
	 * RFC 2109 defines that cookie only supports ASCII code.
	 * 
	 */
	private static class EncoderDecoder
	{
		/** The encoding used */
		private final static String UTF8 = "UTF-8";

		/**
		 * Encode
		 * 
		 * @param string
		 * @return encoded string
		 */
		private static String encode(final String string)
		{
			try
			{
				return URLEncoder.encode(string, UTF8);
			}
			catch (UnsupportedEncodingException e)
			{
				log.error(e.getMessage(), e);
			}
			return null;
		}

		/**
		 * Decode
		 * 
		 * @param string
		 * @return decoded string
		 */
		private static String decode(final String string)
		{
			try
			{
				return URLDecoder.decode(string, UTF8);
			}
			catch (UnsupportedEncodingException e)
			{
				log.error(e.getMessage(), e);
			}
			return null;
		}
	}
}
