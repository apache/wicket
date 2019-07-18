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
package org.apache.wicket.protocol.http;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Collection;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

import org.apache.wicket.markup.html.pages.BrowserInfoPage;
import org.apache.wicket.request.IRequestParameters;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebRequest;
import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.util.string.AppendingStringBuffer;


/**
 * Description of various user agent (browser) properties. To fill the properties with values from
 * the user agent you need to probe the browser using javascript and request header analysis. Wicket
 * provides a default implementation of this in {@link BrowserInfoPage}.
 * <p>
 * A convenient way of letting Wicket do a sneaky redirect to {@link BrowserInfoPage} (and back
 * again) is to put this in your Application's init method:
 * 
 * <pre>
 * getRequestCycleSettings().setGatherExtendedBrowserInfo(true);
 * </pre>
 * 
 * </p>
 * 
 * WARNING: Be sure you think about the dangers of depending on information you pull from the client
 * too much. They may be easily spoofed or inaccurate in other ways, and properties like window and
 * browser size are all too easy to be used naively.
 * 
 * @see BrowserInfoPage
 * @author Frank Bille (frankbille)
 */
public class ClientProperties implements IClusterable
{
	private static final long serialVersionUID = 1L;

	private int browserHeight = -1;
	private int browserWidth = -1;
	private boolean navigatorCookieEnabled;
	private boolean navigatorJavaEnabled;
	private String navigatorAppCodeName;
	private String navigatorAppName;
	private String navigatorAppVersion;
	private String navigatorLanguage;
	private String navigatorPlatform;
	private String navigatorUserAgent;
	private String remoteAddress;
	private int screenColorDepth = -1;
	private int screenHeight = -1;
	private int screenWidth = -1;
	private String utcDSTOffset;
	private String utcOffset;
	private String hostname;

	private boolean javaScriptEnabled;

	/** Cached timezone for repeating calls to {@link #getTimeZone()} */
	private transient TimeZone timeZone;

	/**
	 * @return The browser height at the time it was measured
	 */
	public int getBrowserHeight()
	{
		return browserHeight;
	}

	/**
	 * @return The browser width at the time it was measured
	 */
	public int getBrowserWidth()
	{
		return browserWidth;
	}

	/**
	 * @return The client's navigator.appCodeName property.
	 */
	public String getNavigatorAppCodeName()
	{
		return navigatorAppCodeName;
	}

	/**
	 * @return The client's navigator.appName property.
	 */
	public String getNavigatorAppName()
	{
		return navigatorAppName;
	}

	/**
	 * @return The client's navigator.appVersion property.
	 */
	public String getNavigatorAppVersion()
	{
		return navigatorAppVersion;
	}

	/**
	 * @return The client's navigator.language (or navigator.userLanguage) property.
	 */
	public String getNavigatorLanguage()
	{
		return navigatorLanguage;
	}

	/**
	 * @return The client's navigator.platform property.
	 */
	public String getNavigatorPlatform()
	{
		return navigatorPlatform;
	}

	/**
	 * @return The client's navigator.userAgent property.
	 */
	public String getNavigatorUserAgent()
	{
		return navigatorUserAgent;
	}

	/**
	 * @return The client's remote/ip address.
	 */
	public String getRemoteAddress()
	{
		return remoteAddress;
	}

	/**
	 * @return The clients hostname shown in the browser
	 */
	public String getHostname()
	{
		return hostname;
	}

	/**
	 * @return Color depth of the screen in bits (integer).
	 */
	public int getScreenColorDepth()
	{
		return screenColorDepth;
	}

	/**
	 * @return Height of the screen in pixels (integer).
	 */
	public int getScreenHeight()
	{
		return screenHeight;
	}

	/**
	 * @return Height of the screen in pixels (integer).
	 */
	public int getScreenWidth()
	{
		return screenWidth;
	}

	/**
	 * Get the client's time zone if that could be detected.
	 * 
	 * @return The client's time zone
	 */
	public TimeZone getTimeZone()
	{
		if (timeZone == null)
		{
			String utc = getUtcOffset();
			if (utc != null)
			{
				// apparently it is platform dependent on whether you get the
				// offset in a decimal form or not. This parses the decimal
				// form of the UTC offset, taking into account several
				// possibilities
				// such as getting the format in +2.5 or -1.2

				int dotPos = utc.indexOf('.');
				if (dotPos >= 0)
				{
					String hours = utc.substring(0, dotPos);
					String hourPart = utc.substring(dotPos + 1);

					if (hours.startsWith("+"))
					{
						hours = hours.substring(1);
					}
					int offsetHours = Integer.parseInt(hours);
					int offsetMins = (int)(Double.parseDouble(hourPart) * 6);

					// construct a GMT timezone offset string from the retrieved
					// offset which can be parsed by the TimeZone class.

					AppendingStringBuffer sb = new AppendingStringBuffer("GMT");
					sb.append(offsetHours > 0 ? '+' : '-');
					sb.append(Math.abs(offsetHours));
					sb.append(':');
					if (offsetMins < 10)
					{
						sb.append('0');
					}
					sb.append(offsetMins);
					timeZone = TimeZone.getTimeZone(sb.toString());
				}
				else
				{
					int offset = Integer.parseInt(utc);
					if (offset < 0)
					{
						utc = utc.substring(1);
					}
					timeZone = TimeZone.getTimeZone("GMT" + ((offset > 0) ? '+' : '-') + utc);
				}

				String dstOffset = getUtcDSTOffset();
				if (timeZone != null && dstOffset != null)
				{
					TimeZone dstTimeZone;
					dotPos = dstOffset.indexOf('.');
					if (dotPos >= 0)
					{
						String hours = dstOffset.substring(0, dotPos);
						String hourPart = dstOffset.substring(dotPos + 1);

						if (hours.startsWith("+"))
						{
							hours = hours.substring(1);
						}
						int offsetHours = Integer.parseInt(hours);
						int offsetMins = (int)(Double.parseDouble(hourPart) * 6);

						// construct a GMT timezone offset string from the
						// retrieved
						// offset which can be parsed by the TimeZone class.

						AppendingStringBuffer sb = new AppendingStringBuffer("GMT");
						sb.append(offsetHours > 0 ? '+' : '-');
						sb.append(Math.abs(offsetHours));
						sb.append(':');
						if (offsetMins < 10)
						{
							sb.append('0');
						}
						sb.append(offsetMins);
						dstTimeZone = TimeZone.getTimeZone(sb.toString());
					}
					else
					{
						int offset = Integer.parseInt(dstOffset);
						if (offset < 0)
						{
							dstOffset = dstOffset.substring(1);
						}
						dstTimeZone = TimeZone.getTimeZone("GMT" + ((offset > 0) ? '+' : '-') +
							dstOffset);
					}
					// if the dstTimezone (1 July) has a different offset then
					// the real time zone (1 January) try to combine the 2.
					if (dstTimeZone != null &&
						dstTimeZone.getRawOffset() != timeZone.getRawOffset())
					{
						int dstSaving = Math.abs(dstTimeZone.getRawOffset() - timeZone.getRawOffset());
						String[] availableIDs = TimeZone.getAvailableIDs(dstTimeZone.getRawOffset() < timeZone.getRawOffset() ? dstTimeZone.getRawOffset() : timeZone.getRawOffset());
						for (String availableID : availableIDs)
						{
							TimeZone zone = TimeZone.getTimeZone(availableID);
							if (zone.getDSTSavings() == dstSaving)
							{
								// this is a best guess... still the start and end of the DST should
								// be needed to know to be completely correct, or better yet
								// not just the GMT offset but the TimeZone ID should be transfered
								// from the browser.
								timeZone = zone;
								break;
							}
						}
					}
				}
			}
		}

		return timeZone;
	}

	/**
	 * @return The client's time DST offset from UTC in hours (note: if you do this yourself, use
	 *         'new Date(new Date().getFullYear(), 0, 6, 0, 0, 0, 0).getTimezoneOffset() / -60'
	 *         (note the -)).
	 */
	public String getUtcDSTOffset()
	{
		return utcDSTOffset;
	}


	/**
	 * @return The client's time offset from UTC in hours (note: if you do this yourself, use 'new
	 *         Date(new Date().getFullYear(), 0, 1, 0, 0, 0, 0).getTimezoneOffset() / -60' (note the
	 *         -)).
	 */
	public String getUtcOffset()
	{
		return utcOffset;
	}

	/**
	 * Flag indicating support of JavaScript in the browser.
	 * 
	 * @return True if JavaScript is enabled
	 */
	public boolean isJavaScriptEnabled() {
		return javaScriptEnabled;
	}

	/**
	 * 
	 * 
	 * @return The client's navigator.cookieEnabled property.
	 */
	public boolean isNavigatorCookieEnabled()
	{
		if (!navigatorCookieEnabled && RequestCycle.get() != null)
		{
			Collection<Cookie> cookies = ((WebRequest)RequestCycle.get().getRequest()).getCookies();
			navigatorCookieEnabled = cookies != null && cookies.size() > 0;
		}
		return navigatorCookieEnabled;
	}

	/**
	 * @return The client's navigator.javaEnabled property.
	 */
	public boolean isNavigatorJavaEnabled()
	{
		return navigatorJavaEnabled;
	}

	/**
	 * @param browserHeight
	 *            The height of the browser
	 */
	public void setBrowserHeight(int browserHeight)
	{
		this.browserHeight = browserHeight;
	}

	/**
	 * @param browserWidth
	 *            The browser width
	 */
	public void setBrowserWidth(int browserWidth)
	{
		this.browserWidth = browserWidth;
	}

	/**
	 * @param cookiesEnabled
	 *            The client's navigator.cookieEnabled property.
	 */
	public void setNavigatorCookieEnabled(boolean cookiesEnabled)
	{
		this.navigatorCookieEnabled = cookiesEnabled;
	}

	/**
	 * @param navigatorJavaEnabled
	 *            The client's navigator.javaEnabled property.
	 */
	public void setNavigatorJavaEnabled(boolean navigatorJavaEnabled)
	{
		this.navigatorJavaEnabled = navigatorJavaEnabled;
	}

	/**
	 * @param navigatorAppCodeName
	 *            The client's navigator.appCodeName property.
	 */
	public void setNavigatorAppCodeName(String navigatorAppCodeName)
	{
		this.navigatorAppCodeName = navigatorAppCodeName;
	}

	/**
	 * @param navigatorAppName
	 *            The client's navigator.appName property.
	 */
	public void setNavigatorAppName(String navigatorAppName)
	{
		this.navigatorAppName = navigatorAppName;
	}

	/**
	 * @param navigatorAppVersion
	 *            The client's navigator.appVersion property.
	 */
	public void setNavigatorAppVersion(String navigatorAppVersion)
	{
		this.navigatorAppVersion = navigatorAppVersion;
	}

	/**
	 * @param navigatorLanguage
	 *            The client's navigator.language (or navigator.userLanguage) property.
	 */
	public void setNavigatorLanguage(String navigatorLanguage)
	{
		this.navigatorLanguage = navigatorLanguage;
	}

	/**
	 * @param navigatorPlatform
	 *            The client's navigator.platform property.
	 */
	public void setNavigatorPlatform(String navigatorPlatform)
	{
		this.navigatorPlatform = navigatorPlatform;
	}

	/**
	 * @param navigatorUserAgent
	 *            The client's navigator.userAgent property.
	 */
	public void setNavigatorUserAgent(String navigatorUserAgent)
	{
		this.navigatorUserAgent = navigatorUserAgent;
	}

	/**
	 * @param remoteAddress
	 *            The client's remote/ip address.
	 */
	public void setRemoteAddress(String remoteAddress)
	{
		this.remoteAddress = remoteAddress;
	}

	/**
	 * @param hostname
	 *            the hostname shown in the browser.
	 */
	public void setHostname(String hostname)
	{
		this.hostname = hostname;
	}

	/**
	 * @param screenColorDepth
	 *            Color depth of the screen in bits (integer).
	 */
	public void setScreenColorDepth(int screenColorDepth)
	{
		this.screenColorDepth = screenColorDepth;
	}

	/**
	 * @param screenHeight
	 *            Height of the screen in pixels (integer).
	 */
	public void setScreenHeight(int screenHeight)
	{
		this.screenHeight = screenHeight;
	}

	/**
	 * @param screenWidth
	 *            Height of the screen in pixels (integer).
	 */
	public void setScreenWidth(int screenWidth)
	{
		this.screenWidth = screenWidth;
	}

	/**
	 * Sets time zone.
	 * 
	 * @param timeZone
	 */
	public void setTimeZone(TimeZone timeZone)
	{
		this.timeZone = timeZone;
	}

	/**
	 * @param utcDSTOffset
	 */
	public void setUtcDSTOffset(String utcDSTOffset)
	{
		this.utcDSTOffset = utcDSTOffset;
	}

	/**
	 * @param utcOffset
	 *            The client's time offset from UTC in minutes (note: if you do this yourself, use
	 *            'new Date().getTimezoneOffset() / -60' (note the -)).
	 */
	public void setUtcOffset(String utcOffset)
	{
		this.utcOffset = utcOffset;
	}

	/**
	 * @param javaScriptEnabled
	 *            is JavaScript supported in the browser
	 */
	public void setJavaScriptEnabled(boolean javaScriptEnabled) {
		this.javaScriptEnabled = javaScriptEnabled;
	}

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();

		Class<?> clazz = getClass();
		while (clazz != Object.class) {
			Field[] fields = clazz.getDeclaredFields();

			for (Field field : fields)
			{
				// Ignore these fields
				if (Modifier.isStatic(field.getModifiers()) ||
					Modifier.isTransient(field.getModifiers())  ||
					field.isSynthetic())
				{
					continue;
				}

				field.setAccessible(true);

				Object value;
				try
				{
					value = field.get(this);
				}
				catch (IllegalArgumentException e)
				{
					throw new RuntimeException(e);
				}
				catch (IllegalAccessException e)
				{
					throw new RuntimeException(e);
				}

				if (field.getType().equals(Integer.TYPE))
				{
					if (Integer.valueOf(-1).equals(value))
					{
						value = null;
					}
				}

				if (value != null)
				{
					b.append(field.getName());
					b.append('=');
					b.append(value);
					b.append('\n');
				}
			}

			clazz = clazz.getSuperclass();
		}
		return b.toString();
	}

	/**
	 * Read parameters.
	 * 
	 * @param parameters
	 *            parameters sent from browser
	 */
	public void read(IRequestParameters parameters)
	{
		setNavigatorAppCodeName(parameters.getParameterValue("navigatorAppCodeName").toString("N/A"));
		setNavigatorAppName(parameters.getParameterValue("navigatorAppName").toString("N/A"));
		setNavigatorAppVersion(parameters.getParameterValue("navigatorAppVersion").toString("N/A"));
		setNavigatorCookieEnabled(parameters.getParameterValue("navigatorCookieEnabled").toBoolean(false));
		setNavigatorJavaEnabled(parameters.getParameterValue("navigatorJavaEnabled").toBoolean(false));
		setNavigatorLanguage(parameters.getParameterValue("navigatorLanguage").toString("N/A"));
		setNavigatorPlatform(parameters.getParameterValue("navigatorPlatform").toString("N/A"));
		setNavigatorUserAgent(parameters.getParameterValue("navigatorUserAgent").toString("N/A"));
		setScreenWidth(parameters.getParameterValue("screenWidth").toInt(-1));
		setScreenHeight(parameters.getParameterValue("screenHeight").toInt(-1));
		setScreenColorDepth(parameters.getParameterValue("screenColorDepth").toInt(-1));
		setUtcOffset(parameters.getParameterValue("utcOffset").toString(null));
		setUtcDSTOffset(parameters.getParameterValue("utcDSTOffset").toString(null));
		setBrowserWidth(parameters.getParameterValue("browserWidth").toInt(-1));
		setBrowserHeight(parameters.getParameterValue("browserHeight").toInt(-1));
		setHostname(parameters.getParameterValue("hostname").toString("N/A"));
	}
}
