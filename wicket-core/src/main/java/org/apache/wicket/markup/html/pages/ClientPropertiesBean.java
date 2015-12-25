/**
 * 
 */
package org.apache.wicket.markup.html.pages;

import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.util.io.IClusterable;

/**
 * @author rakesh
 *
 */
public class ClientPropertiesBean implements IClusterable
{
	private String navigatorAppCodeName;
	private String navigatorAppName;
	private String navigatorAppVersion;
	private Boolean navigatorCookieEnabled = Boolean.FALSE;
	private Boolean navigatorJavaEnabled = Boolean.FALSE;
	private String navigatorLanguage;
	private String navigatorPlatform;
	private String navigatorUserAgent;
	private String screenColorDepth;
	private String screenHeight;
	private String screenWidth;
	private String utcOffset;
	private String utcDSTOffset;
	private String browserWidth;
	private String browserHeight;
	private String hostname;

	/**
	 * Gets browserHeight.
	 * 
	 * @return browserHeight
	 */
	public String getBrowserHeight()
	{
		return browserHeight;
	}

	/**
	 * Gets browserWidth.
	 * 
	 * @return browserWidth
	 */
	public String getBrowserWidth()
	{
		return browserWidth;
	}

	/**
	 * Gets navigatorAppCodeName.
	 * 
	 * @return navigatorAppCodeName
	 */
	public String getNavigatorAppCodeName()
	{
		return navigatorAppCodeName;
	}

	/**
	 * Gets navigatorAppName.
	 * 
	 * @return navigatorAppName
	 */
	public String getNavigatorAppName()
	{
		return navigatorAppName;
	}

	/**
	 * Gets navigatorAppVersion.
	 * 
	 * @return navigatorAppVersion
	 */
	public String getNavigatorAppVersion()
	{
		return navigatorAppVersion;
	}

	/**
	 * Gets navigatorCookieEnabled.
	 * 
	 * @return navigatorCookieEnabled
	 */
	public Boolean getNavigatorCookieEnabled()
	{
		return navigatorCookieEnabled;
	}

	/**
	 * Gets navigatorJavaEnabled.
	 * 
	 * @return navigatorJavaEnabled
	 */
	public Boolean getNavigatorJavaEnabled()
	{
		return navigatorJavaEnabled;
	}

	/**
	 * Gets navigatorLanguage.
	 * 
	 * @return navigatorLanguage
	 */
	public String getNavigatorLanguage()
	{
		return navigatorLanguage;
	}

	/**
	 * Gets navigatorPlatform.
	 * 
	 * @return navigatorPlatform
	 */
	public String getNavigatorPlatform()
	{
		return navigatorPlatform;
	}

	/**
	 * Gets navigatorUserAgent.
	 * 
	 * @return navigatorUserAgent
	 */
	public String getNavigatorUserAgent()
	{
		return navigatorUserAgent;
	}

	/**
	 * Gets screenColorDepth.
	 * 
	 * @return screenColorDepth
	 */
	public String getScreenColorDepth()
	{
		return screenColorDepth;
	}

	/**
	 * Gets screenHeight.
	 * 
	 * @return screenHeight
	 */
	public String getScreenHeight()
	{
		return screenHeight;
	}

	/**
	 * Gets screenWidth.
	 * 
	 * @return screenWidth
	 */
	public String getScreenWidth()
	{
		return screenWidth;
	}

	/**
	 * Gets utcOffset.
	 * 
	 * @return utcOffset
	 */
	public String getUtcOffset()
	{
		return utcOffset;
	}

	/**
	 * Gets utcDSTOffset.
	 * 
	 * @return utcOffset
	 */
	public String getUtcDSTOffset()
	{
		return utcDSTOffset;
	}

	/**
	 * Merge this with the given properties object.
	 * 
	 * @param properties
	 *            the properties object to merge with
	 */
	public void merge(ClientProperties properties)
	{
		properties.setNavigatorAppName(navigatorAppName);
		properties.setNavigatorAppVersion(navigatorAppVersion);
		properties.setNavigatorAppCodeName(navigatorAppCodeName);
		properties.setCookiesEnabled((navigatorCookieEnabled != null) ? navigatorCookieEnabled
			: false);
		properties.setJavaEnabled((navigatorJavaEnabled != null) ? navigatorJavaEnabled : false);
		properties.setNavigatorLanguage(navigatorLanguage);
		properties.setNavigatorPlatform(navigatorPlatform);
		properties.setNavigatorUserAgent(navigatorUserAgent);
		properties.setScreenWidth(getInt(screenWidth));
		properties.setScreenHeight(getInt(screenHeight));
		properties.setBrowserWidth(getInt(browserWidth));
		properties.setBrowserHeight(getInt(browserHeight));
		properties.setScreenColorDepth(getInt(screenColorDepth));
		properties.setUtcOffset(utcOffset);
		properties.setUtcDSTOffset(utcDSTOffset);
		properties.setHostname(hostname);
	}

	/**
	 * Sets browserHeight.
	 * 
	 * @param browserHeight
	 *            browserHeight
	 */
	public void setBrowserHeight(String browserHeight)
	{
		this.browserHeight = browserHeight;
	}

	/**
	 * Sets browserWidth.
	 * 
	 * @param browserWidth
	 *            browserWidth
	 */
	public void setBrowserWidth(String browserWidth)
	{
		this.browserWidth = browserWidth;
	}

	/**
	 * Sets navigatorAppCodeName.
	 * 
	 * @param navigatorAppCodeName
	 *            navigatorAppCodeName
	 */
	public void setNavigatorAppCodeName(String navigatorAppCodeName)
	{
		this.navigatorAppCodeName = navigatorAppCodeName;
	}

	/**
	 * Sets navigatorAppName.
	 * 
	 * @param navigatorAppName
	 *            navigatorAppName
	 */
	public void setNavigatorAppName(String navigatorAppName)
	{
		this.navigatorAppName = navigatorAppName;
	}

	/**
	 * Sets navigatorAppVersion.
	 * 
	 * @param navigatorAppVersion
	 *            navigatorAppVersion
	 */
	public void setNavigatorAppVersion(String navigatorAppVersion)
	{
		this.navigatorAppVersion = navigatorAppVersion;
	}

	/**
	 * Sets navigatorCookieEnabled.
	 * 
	 * @param navigatorCookieEnabled
	 *            navigatorCookieEnabled
	 */
	public void setNavigatorCookieEnabled(Boolean navigatorCookieEnabled)
	{
		this.navigatorCookieEnabled = navigatorCookieEnabled;
	}

	/**
	 * Sets navigatorJavaEnabled.
	 * 
	 * @param navigatorJavaEnabled
	 *            navigatorJavaEnabled
	 */
	public void setNavigatorJavaEnabled(Boolean navigatorJavaEnabled)
	{
		this.navigatorJavaEnabled = navigatorJavaEnabled;
	}

	/**
	 * Sets navigatorLanguage.
	 * 
	 * @param navigatorLanguage
	 *            navigatorLanguage
	 */
	public void setNavigatorLanguage(String navigatorLanguage)
	{
		this.navigatorLanguage = navigatorLanguage;
	}

	/**
	 * Sets navigatorPlatform.
	 * 
	 * @param navigatorPlatform
	 *            navigatorPlatform
	 */
	public void setNavigatorPlatform(String navigatorPlatform)
	{
		this.navigatorPlatform = navigatorPlatform;
	}

	/**
	 * Sets navigatorUserAgent.
	 * 
	 * @param navigatorUserAgent
	 *            navigatorUserAgent
	 */
	public void setNavigatorUserAgent(String navigatorUserAgent)
	{
		this.navigatorUserAgent = navigatorUserAgent;
	}

	/**
	 * Sets screenColorDepth.
	 * 
	 * @param screenColorDepth
	 *            screenColorDepth
	 */
	public void setScreenColorDepth(String screenColorDepth)
	{
		this.screenColorDepth = screenColorDepth;
	}

	/**
	 * Sets screenHeight.
	 * 
	 * @param screenHeight
	 *            screenHeight
	 */
	public void setScreenHeight(String screenHeight)
	{
		this.screenHeight = screenHeight;
	}

	/**
	 * Sets screenWidth.
	 * 
	 * @param screenWidth
	 *            screenWidth
	 */
	public void setScreenWidth(String screenWidth)
	{
		this.screenWidth = screenWidth;
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
	 * @return The clients hostname shown in the browser
	 */
	public String getHostname()
	{
		return hostname;
	}

	/**
	 * Sets utcOffset.
	 * 
	 * @param utcOffset
	 *            utcOffset
	 */
	public void setUtcOffset(String utcOffset)
	{
		this.utcOffset = utcOffset;
	}

	/**
	 * Sets utcDSTOffset.
	 * 
	 * @param utcDSTOffset
	 *            utcDSTOffset
	 */
	public void setUtcDSTOffset(String utcDSTOffset)
	{
		this.utcDSTOffset = utcDSTOffset;
	}

	private int getInt(String value)
	{
		int intValue = -1;
		try
		{
			intValue = Integer.parseInt(value);
		}
		catch (NumberFormatException e)
		{
			// Do nothing
		}
		return intValue;
	}
}
