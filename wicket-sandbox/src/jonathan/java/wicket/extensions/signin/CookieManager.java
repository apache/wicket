/**
 * Copyright (C) 2006, Jonathan W. Locke. All Rights Reserved.
 */

package wicket.extensions.signin;

import java.io.Serializable;

import javax.servlet.http.Cookie;

import wicket.Application;
import wicket.MetaDataKey;
import wicket.RequestCycle;
import wicket.Session;
import wicket.markup.html.form.FormComponent;
import wicket.protocol.http.WebRequest;
import wicket.protocol.http.WebResponse;
import wicket.util.time.Duration;

/**
 * Class that manages cookies in a convenient way and automatically attaches
 * itself to the session's metadata if you call CookieManager.get(). However you
 * can have multiple CookieManagers with different settings if you want. One
 * place where we ought to use it would be in the CookieValuePersister class,
 * but you could also have a separate CookieManager for legacy app integration
 * if you wanted to.
 * 
 * @author Jonathan Locke
 */
@SuppressWarnings("serial")
public class CookieManager implements Serializable
{
	/**
	 * Settings for cookie management
	 * 
	 * @author Jonathan Locke
	 */
	public static class Settings
	{
		/**
		 * Comment for cookie
		 */
		private String comment = "Wicket Cookie Manager";

		/**
		 * Cookie domain
		 */
		private String domain;

		/**
		 * Maximum age for a cookie
		 */
		private Duration maxAge = Duration.days(30);

		/**
		 * Prefix for names
		 */
		private String prefix = "wicket:cookieManager:";

		public String getComment()
		{
			return comment;
		}

		public String getDomain()
		{
			return domain;
		}

		public Duration getMaxAge()
		{
			return maxAge;
		}

		public String getPrefix()
		{
			return prefix;
		}

		public void setComment(String comment)
		{
			this.comment = comment;
		}

		public void setDomain(String domain)
		{
			this.domain = domain;
		}

		public void setMaxAge(Duration maxAge)
		{
			this.maxAge = maxAge;
		}

		public void setPrefix(String prefix)
		{
			this.prefix = prefix;
		}
	}

	private static MetaDataKey metaDataKey = new MetaDataKey(CookieManager.class)
	{
	};

	/**
	 * @return Cookie manager stored in session metadata
	 */
	static CookieManager get()
	{
		final Session session = Session.get();
		final CookieManager cookieManager = (CookieManager)session.getMetaData(metaDataKey);
		if (cookieManager != null)
		{
			return cookieManager;
		}
		else
		{
			final CookieManager newCookieManager = new CookieManager();
			session.setMetaData(metaDataKey, newCookieManager);
			return newCookieManager;
		}
	}

	/**
	 * Settings to use in managing cookies
	 */
	private Settings settings = new Settings();

	/**
	 * @param component
	 *            Component to clear cookie for
	 */
	public void clear(final FormComponent component)
	{
		clearValue(component.getId());
	}

	/**
	 * Removes cookies
	 */
	public void clearValue(final String name)
	{
		final Cookie cookie = newCookie(name);
		getWebResponse().clearCookie(cookie);
	}

	/**
	 * @param name
	 *            Name of cookie
	 * @return Value decrypted
	 */
	public String getDecryptedValue(final String name)
	{
		final String value = getValue(name);
		return Application.get().getSecuritySettings().getCryptFactory().newCrypt().decryptUrlSafe(
				value);
	}

	/**
	 * @param name
	 *            Name of cookie
	 * @return Cookie value or null if none exists
	 */
	public String getValue(final String name)
	{
		// Get all cookies attached to the Request by the client browser
		Cookie[] cookies = getWebRequest().getCookies();
		if (cookies != null)
		{
			for (Cookie cookie : cookies)
			{
				// Names must match and Value must not be empty
				if (cookie.getName().equals(getCookieName(name)))
				{
					// cookies with no value do me no good!
					if (cookie.getValue() != null && cookie.getValue().length() > 0)
					{
						return cookie.getValue();
					}
				}
			}
		}
		return null;
	}

	/**
	 * Loads value from cookie
	 * 
	 * @param component
	 *            The component
	 */
	public void load(final FormComponent component)
	{
		final String value = getValue(component.getId());
		if (value != null)
		{
			component.setModelValue(value);
		}
		else
		{
			component.setModelValue("");
		}
	}

	/**
	 * Saves value to cookie
	 * 
	 * @param component
	 *            The component
	 */
	public void save(final FormComponent component)
	{
		setValue(component.getId(), component.getModelObjectAsString());
	}

	/**
	 * @param settings
	 *            Settings for this cookie manager
	 */
	public void setSettings(Settings settings)
	{
		this.settings = settings;
	}

	/**
	 * @param name
	 *            Name of cookie
	 * @param value
	 *            Value of cookie
	 */
	public void setValue(final String name, final String value)
	{
		final Cookie cookie = newCookie(name);
		cookie.setValue(value);
		getWebResponse().addCookie(cookie);
	}

	/**
	 * @return Web response for current request cycle
	 */
	private WebRequest getWebRequest()
	{
		return (WebRequest)RequestCycle.get().getRequest();
	}

	/**
	 * @return Web response for current request cycle
	 */
	private WebResponse getWebResponse()
	{
		return (WebResponse)RequestCycle.get().getResponse();
	}

	/**
	 * @param name
	 *            Name of cookie
	 * @return Fully qualified cookie name
	 */
	private String getCookieName(final String name)
	{
		return settings.getPrefix() + name;
	}

	/**
	 * @param name
	 *            Name of cookie
	 * @return Cookie
	 */
	private Cookie newCookie(String name)
	{
		final Cookie cookie = new Cookie(getCookieName(name), "");
		if (settings.getComment() != null)
		{
			cookie.setComment(settings.getComment() + ": " + name);
		}
		cookie.setMaxAge((int)settings.getMaxAge().seconds());
		cookie.setPath(getWebRequest().getContextPath());
		if (settings.getDomain() != null)
		{
			cookie.setDomain(settings.getDomain());
		}
		return cookie;
	}
}