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
import java.util.Collection;
import java.util.TimeZone;

import javax.servlet.http.Cookie;

import org.apache.wicket.markup.html.pages.BrowserInfoPage;
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
	private boolean browserInternetExplorer;
	private boolean browserKonqueror;
	private boolean browserMozilla;
	private boolean browserMozillaFirefox;
	private boolean browserOpera;
	private boolean browserSafari;
	private boolean browserChrome;
	private int browserVersionMajor = -1;
	private int browserVersionMinor = -1;
	private int browserWidth = -1;
	private boolean cookiesEnabled;
	private boolean javaEnabled;
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
	/** Cached timezone for repeating calls to {@link #getTimeZone()} */
	private TimeZone timeZone;
	private String utcDSTOffset;

	private String utcOffset;

	private String hostname;

	/**
	 * @return The browser height at the time it was measured
	 */
	public int getBrowserHeight()
	{
		return browserHeight;
	}

	/**
	 * @return The major version number of the browser.
	 */
	public int getBrowserVersionMajor()
	{
		return browserVersionMajor;
	}

	/**
	 * @return The minor version number of the browser.
	 */
	public int getBrowserVersionMinor()
	{
		return browserVersionMinor;
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
						int dstSaving = dstTimeZone.getRawOffset() - timeZone.getRawOffset();
						String[] availableIDs = TimeZone.getAvailableIDs(timeZone.getRawOffset());
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
	 * @return The client's time DST offset from UTC in minutes (note: if you do this yourself, use
	 *         'new Date(new Date().getFullYear(), 0, 6, 0, 0, 0, 0).getTimezoneOffset() / -60'
	 *         (note the -)).
	 */
	public String getUtcDSTOffset()
	{
		return utcDSTOffset;
	}


	/**
	 * @return The client's time offset from UTC in minutes (note: if you do this yourself, use 'new
	 *         Date(new Date().getFullYear(), 0, 1, 0, 0, 0, 0).getTimezoneOffset() / -60' (note the
	 *         -)).
	 */
	public String getUtcOffset()
	{
		return utcOffset;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Microsoft Internet Explorer browser
	 * platform.
	 * 
	 * @return True if a derivative of the Microsoft Internet Explorer browser platform.
	 */
	public boolean isBrowserInternetExplorer()
	{
		return browserInternetExplorer;
	}

	/**
	 * Flag indicating that the browser is a derivative of the KDE Konqueror browser platform.
	 * 
	 * @return True if a derivative of the KDE Konqueror browser platform.
	 */
	public boolean isBrowserKonqueror()
	{
		return browserKonqueror;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Mozilla 1.0-1.8+ browser platform.
	 * 
	 * @return True if a derivative of the Mozilla 1.0-1.8+ browser platform.
	 */
	public boolean isBrowserMozilla()
	{
		return browserMozilla;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Mozilla Firefox 1.0+ browser
	 * platform.
	 * 
	 * @return True if a derivative of the Mozilla Firefox 1.0+ browser platform.
	 */
	public boolean isBrowserMozillaFirefox()
	{
		return browserMozillaFirefox;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Opera browser platform.
	 * 
	 * @return True if a derivative of the Opera browser platform.
	 */
	public boolean isBrowserOpera()
	{
		return browserOpera;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Apple Safari browser platform.
	 * 
	 * @return True if a derivative of the Apple Safari browser platform.
	 */
	public boolean isBrowserSafari()
	{
		return browserSafari;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Chrome browser platform.
	 * 
	 * @return True if a derivative of the Chrome browser platform.
	 */
	public boolean isBrowserChrome()
	{
		return browserChrome;
	}

	/**
	 * 
	 * 
	 * @return The client's navigator.cookieEnabled property.
	 */
	public boolean isCookiesEnabled()
	{
		if (!cookiesEnabled && RequestCycle.get() != null)
		{
			Collection<Cookie> cookies = ((WebRequest)RequestCycle.get().getRequest()).getCookies();
			cookiesEnabled = cookies != null && cookies.size() > 0;
		}
		return cookiesEnabled;
	}

	/**
	 * @return The client's navigator.javaEnabled property.
	 */
	public boolean isJavaEnabled()
	{
		return javaEnabled;
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
	 * Flag indicating that the browser is a derivative of the Microsoft Internet Explorer browser
	 * platform.
	 * 
	 * @param browserInternetExplorer
	 *            True if a derivative of the Microsoft Internet Explorer browser platform.
	 */
	public void setBrowserInternetExplorer(boolean browserInternetExplorer)
	{
		this.browserInternetExplorer = browserInternetExplorer;
	}

	/**
	 * Flag indicating that the browser is a derivative of the KDE Konqueror browser platform.
	 * 
	 * @param browserKonqueror
	 *            True if a derivative of the KDE Konqueror browser platform.
	 */
	public void setBrowserKonqueror(boolean browserKonqueror)
	{
		this.browserKonqueror = browserKonqueror;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Mozilla 1.0-1.8+ browser platform.
	 * 
	 * @param browserMozilla
	 *            True if a derivative of the Mozilla 1.0-1.8+ browser platform.
	 */
	public void setBrowserMozilla(boolean browserMozilla)
	{
		this.browserMozilla = browserMozilla;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Mozilla Firefox 1.0+ browser
	 * platform.
	 * 
	 * @param browserMozillaFirefox
	 *            True if a derivative of the Mozilla Firefox 1.0+ browser platform.
	 */
	public void setBrowserMozillaFirefox(boolean browserMozillaFirefox)
	{
		this.browserMozillaFirefox = browserMozillaFirefox;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Opera browser platform.
	 * 
	 * @param browserOpera
	 *            True if a derivative of the Opera browser platform.
	 */
	public void setBrowserOpera(boolean browserOpera)
	{
		this.browserOpera = browserOpera;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Apple Safari browser platform.
	 * 
	 * @param browserSafari
	 *            True if a derivative of the Apple Safari browser platform.
	 */
	public void setBrowserSafari(boolean browserSafari)
	{
		this.browserSafari = browserSafari;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Chrome browser platform.
	 * 
	 * @param browserChrome
	 *            True if a derivative of the Chrome browser platform.
	 */
	public void setBrowserChrome(boolean browserChrome)
	{
		this.browserChrome = browserChrome;
	}

	/**
	 * @param browserVersionMajor
	 *            The major version number of the browser.
	 */
	public void setBrowserVersionMajor(int browserVersionMajor)
	{
		this.browserVersionMajor = browserVersionMajor;
	}

	/**
	 * @param browserVersionMinor
	 *            The minor version number of the browser.
	 */
	public void setBrowserVersionMinor(int browserVersionMinor)
	{
		this.browserVersionMinor = browserVersionMinor;
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
	public void setCookiesEnabled(boolean cookiesEnabled)
	{
		this.cookiesEnabled = cookiesEnabled;
	}

	/**
	 * @param navigatorJavaEnabled
	 *            The client's navigator.javaEnabled property.
	 */
	public void setJavaEnabled(boolean navigatorJavaEnabled)
	{
		javaEnabled = navigatorJavaEnabled;
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

	@Override
	public String toString()
	{
		StringBuilder b = new StringBuilder();

		Field[] fields = ClientProperties.class.getDeclaredFields();

		for (Field field : fields)
		{
			// Ignore these fields
			if (field.getName().equals("serialVersionUID") == false &&
				field.getName().startsWith("class$") == false &&
				field.getName().startsWith("timeZone") == false)
			{

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
		}

		return b.toString();
	}

}
