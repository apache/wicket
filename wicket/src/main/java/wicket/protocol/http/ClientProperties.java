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
package wicket.protocol.http;

import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.TimeZone;

import wicket.markup.html.pages.BrowserInfoPage;
import wicket.util.string.AppendingStringBuffer;

/**
 * Description of various user agent (browser) properties. To fill the
 * properties with values from the user agent you need to probe the browser
 * using javascript and request header analysis. Wicket provides a default
 * implementation of this in {@link BrowserInfoPage}.
 * 
 * @see BrowserInfoPage
 * @author Frank Bille (frankbille)
 */
public class ClientProperties implements Serializable
{
	private static final long serialVersionUID = 1L;

	private boolean browserInternetExplorer;
	private boolean browserKonqueror;
	private boolean browserMozilla;
	private boolean browserMozillaFirefox;
	private boolean browserOpera;
	private boolean browserSafari;
	private int browserVersionMajor = -1;
	private int browserVersionMinor = -1;
	private String navigatorAppCodeName;
	private String navigatorAppName;
	private String navigatorAppVersion;
	private boolean cookiesEnabled;
	private boolean javaEnabled;
	private String navigatorLanguage;
	private String navigatorPlatform;
	private String navigatorUserAgent;
	private boolean proprietaryIECssExpressionsSupported;
	private boolean proprietaryIEPngAlphaFilterRequired;
	private boolean quirkCssBackgroundAttachmentUseFixed;
	private boolean quirkCssBorderCollapseFor0Padding;
	private boolean quirkCssBorderCollapseInside;
	private boolean quirkCssPositioningOneSideOnly;
	private boolean quirkIERepaint;
	private boolean quirkIESelectListDomUpdate;
	private boolean quirkIESelectPercentWidth;
	private boolean quirkIESelectZIndex;
	private boolean quirkIETablePercentWidthScrollbarError;
	private boolean quirkIETextareaNewlineObliteration;
	private boolean quirkMozillaPerformanceLargeDomRemove;
	private boolean quirkMozillaTextInputRepaint;
	private int screenColorDepth = -1;
	private int screenHeight = -1;
	private int screenWidth = -1;
	private String utcOffset;
	private String remoteAddress;

	/** Cached timezone for repeating calls to {@link #getTimeZone()} */
	private TimeZone timeZone;

	/**
	 * Flag indicating that the browser is a derivative of the Microsoft
	 * Internet Explorer browser platform.
	 * 
	 * @return True if a derivative of the Microsoft Internet Explorer browser
	 *         platform.
	 */
	public boolean isBrowserInternetExplorer()
	{
		return browserInternetExplorer;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Microsoft
	 * Internet Explorer browser platform.
	 * 
	 * @param browserInternetExplorer
	 *            True if a derivative of the Microsoft Internet Explorer
	 *            browser platform.
	 */
	public void setBrowserInternetExplorer(boolean browserInternetExplorer)
	{
		this.browserInternetExplorer = browserInternetExplorer;
	}

	/**
	 * Flag indicating that the browser is a derivative of the KDE Konqueror
	 * browser platform.
	 * 
	 * @return True if a derivative of the KDE Konqueror browser platform.
	 */
	public boolean isBrowserKonqueror()
	{
		return browserKonqueror;
	}

	/**
	 * Flag indicating that the browser is a derivative of the KDE Konqueror
	 * browser platform.
	 * 
	 * @param browserKonqueror
	 *            True if a derivative of the KDE Konqueror browser platform.
	 */
	public void setBrowserKonqueror(boolean browserKonqueror)
	{
		this.browserKonqueror = browserKonqueror;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Mozilla 1.0-1.8+
	 * browser platform.
	 * 
	 * @return True if a derivative of the Mozilla 1.0-1.8+ browser platform.
	 */
	public boolean isBrowserMozilla()
	{
		return browserMozilla;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Mozilla 1.0-1.8+
	 * browser platform.
	 * 
	 * @param browserMozilla
	 *            True if a derivative of the Mozilla 1.0-1.8+ browser platform.
	 */
	public void setBrowserMozilla(boolean browserMozilla)
	{
		this.browserMozilla = browserMozilla;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Mozilla Firefox
	 * 1.0+ browser platform.
	 * 
	 * @return True if a derivative of the Mozilla Firefox 1.0+ browser
	 *         platform.
	 */
	public boolean isBrowserMozillaFirefox()
	{
		return browserMozillaFirefox;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Mozilla Firefox
	 * 1.0+ browser platform.
	 * 
	 * @param browserMozillaFirefox
	 *            True if a derivative of the Mozilla Firefox 1.0+ browser
	 *            platform.
	 */
	public void setBrowserMozillaFirefox(boolean browserMozillaFirefox)
	{
		this.browserMozillaFirefox = browserMozillaFirefox;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Opera browser
	 * platform.
	 * 
	 * @return True if a derivative of the Opera browser platform.
	 */
	public boolean isBrowserOpera()
	{
		return browserOpera;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Opera browser
	 * platform.
	 * 
	 * @param browserOpera
	 *            True if a derivative of the Opera browser platform.
	 */
	public void setBrowserOpera(boolean browserOpera)
	{
		this.browserOpera = browserOpera;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Apple Safari
	 * browser platform.
	 * 
	 * @return True if a derivative of the Apple Safari browser platform.
	 */
	public boolean isBrowserSafari()
	{
		return browserSafari;
	}

	/**
	 * Flag indicating that the browser is a derivative of the Apple Safari
	 * browser platform.
	 * 
	 * @param browserSafari
	 *            True if a derivative of the Apple Safari browser platform.
	 */
	public void setBrowserSafari(boolean browserSafari)
	{
		this.browserSafari = browserSafari;
	}

	/**
	 * @return The major version number of the browser.
	 */
	public int getBrowserVersionMajor()
	{
		return browserVersionMajor;
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
	 * @return The minor version number of the browser.
	 */
	public int getBrowserVersionMinor()
	{
		return browserVersionMinor;
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
	 * @return The client's navigator.appCodeName property.
	 */
	public String getNavigatorAppCodeName()
	{
		return navigatorAppCodeName;
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
	 * @return The client's navigator.appName property.
	 */
	public String getNavigatorAppName()
	{
		return navigatorAppName;
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
	 * @return The client's navigator.appVersion property.
	 */
	public String getNavigatorAppVersion()
	{
		return navigatorAppVersion;
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
	 * 
	 * 
	 * @return The client's navigator.cookieEnabled property.
	 */
	public boolean isCookiesEnabled()
	{
		return cookiesEnabled;
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
	 * @return The client's navigator.javaEnabled property.
	 */
	public boolean isJavaEnabled()
	{
		return javaEnabled;
	}

	/**
	 * @param navigatorJavaEnabled
	 *            The client's navigator.javaEnabled property.
	 */
	public void setJavaEnabled(boolean navigatorJavaEnabled)
	{
		this.javaEnabled = navigatorJavaEnabled;
	}

	/**
	 * @return The client's navigator.language (or navigator.userLanguage)
	 *         property.
	 */
	public String getNavigatorLanguage()
	{
		return navigatorLanguage;
	}

	/**
	 * @param navigatorLanguage
	 *            The client's navigator.language (or navigator.userLanguage)
	 *            property.
	 */
	public void setNavigatorLanguage(String navigatorLanguage)
	{
		this.navigatorLanguage = navigatorLanguage;
	}

	/**
	 * @return The client's navigator.platform property.
	 */
	public String getNavigatorPlatform()
	{
		return navigatorPlatform;
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
	 * @return The client's navigator.userAgent property.
	 */
	public String getNavigatorUserAgent()
	{
		return navigatorUserAgent;
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
	 * A proprietary feature flag indicating support for IE-style CSS
	 * expressions.
	 * <p>
	 * This proprietary feature is provided by:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if support for IE-style CSS expressions.
	 */
	public boolean isProprietaryIECssExpressionsSupported()
	{
		return proprietaryIECssExpressionsSupported;
	}

	/**
	 * @param proprietaryIECssExpressionsSupported
	 *            True if support for IE-style CSS expressions.
	 */
	public void setProprietaryIECssExpressionsSupported(boolean proprietaryIECssExpressionsSupported)
	{
		this.proprietaryIECssExpressionsSupported = proprietaryIECssExpressionsSupported;
	}

	/**
	 * A proprietary feature flag indicating that PNG alpha channel support is
	 * available only by using a 'filter'.
	 * <p>
	 * This proprietary feature is provided by:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if PNG alpha channel support is available only by using a
	 *         'filter'.
	 */
	public boolean isProprietaryIEPngAlphaFilterRequired()
	{
		return proprietaryIEPngAlphaFilterRequired;
	}

	/**
	 * @param proprietaryIEPngAlphaFilterRequired
	 *            True if PNG alpha channel support is available only by using a
	 *            'filter'.
	 */
	public void setProprietaryIEPngAlphaFilterRequired(boolean proprietaryIEPngAlphaFilterRequired)
	{
		this.proprietaryIEPngAlphaFilterRequired = proprietaryIEPngAlphaFilterRequired;
	}

	/**
	 * A quirk flag indicating that the 'fixed' attribute should be used to for
	 * fixed-to-element background attachment.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if the 'fixed' attribute should be used to for
	 *         fixed-to-element background attachment.
	 */
	public boolean isQuirkCssBackgroundAttachmentUseFixed()
	{
		return quirkCssBackgroundAttachmentUseFixed;
	}

	/**
	 * @param quirkCssBackgroundAttachmentUseFixed
	 *            True if the 'fixed' attribute should be used to for
	 *            fixed-to-element background attachment.
	 */
	public void setQuirkCssBackgroundAttachmentUseFixed(boolean quirkCssBackgroundAttachmentUseFixed)
	{
		this.quirkCssBackgroundAttachmentUseFixed = quirkCssBackgroundAttachmentUseFixed;
	}

	/**
	 * A quirk flag indicating the only means of achieving 0 padding in table
	 * cells is to use 0px padding.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if the only means of achieving 0 padding in table cells is
	 *         to use 0px padding.
	 */
	public boolean isQuirkCssBorderCollapseFor0Padding()
	{
		return quirkCssBorderCollapseFor0Padding;
	}

	/**
	 * @param quirkCssBorderCollapseFor0Padding
	 *            True if the only means of achieving 0 padding in table cells
	 *            is to use 0px padding.
	 */
	public void setQuirkCssBorderCollapseFor0Padding(boolean quirkCssBorderCollapseFor0Padding)
	{
		this.quirkCssBorderCollapseFor0Padding = quirkCssBorderCollapseFor0Padding;
	}

	/**
	 * A quirk flag indicating whether the client will incorrectly render CSS
	 * collapsed borders such that they reside entirely within the region of a
	 * component.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if the client will incorrectly render CSS collapsed borders
	 *         such that they reside entirely within the region of a component.
	 */
	public boolean isQuirkCssBorderCollapseInside()
	{
		return quirkCssBorderCollapseInside;
	}

	/**
	 * @param quirkCssBorderCollapseInside
	 *            True if the client will incorrectly render CSS collapsed
	 *            borders such that they reside entirely within the region of a
	 *            component.
	 */
	public void setQuirkCssBorderCollapseInside(boolean quirkCssBorderCollapseInside)
	{
		this.quirkCssBorderCollapseInside = quirkCssBorderCollapseInside;
	}

	/**
	 * A quirk flag indicating that CSS positioning values do not work correctly
	 * when either both "top" and "bottom" or "left" and "right" positions are
	 * set at the same time.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if CSS positioning values do not work correctly when either
	 *         both "top" and "bottom" or "left" and "right" positions are set
	 *         at the same time.
	 */
	public boolean isQuirkCssPositioningOneSideOnly()
	{
		return quirkCssPositioningOneSideOnly;
	}

	/**
	 * @param quirkCssPositioningOneSideOnly
	 *            True if CSS positioning values do not work correctly when
	 *            either both "top" and "bottom" or "left" and "right" positions
	 *            are set at the same time.
	 */
	public void setQuirkCssPositioningOneSideOnly(boolean quirkCssPositioningOneSideOnly)
	{
		this.quirkCssPositioningOneSideOnly = quirkCssPositioningOneSideOnly;
	}

	/**
	 * A quirk flag describing the curious repaint behavior found in Internet
	 * Explorer 6, where repaints may be excessively delayed. This quirky
	 * behavior is most visible when the DOM hierarchy is large and complex. The
	 * unlikely workaround for this quirky behavior is to "tickle" (adjust and
	 * then reset) the CSS width of an element, which will force an immediate
	 * repaint.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if means needs to be taken agains weird Internet Explorer
	 *         repaint behaviors.
	 */
	public boolean isQuirkIERepaint()
	{
		return quirkIERepaint;
	}

	/**
	 * @param quirkIERepaint
	 *            True if means needs to be taken agains weird Internet Explorer
	 *            repaint behaviors.
	 */
	public void setQuirkIERepaint(boolean quirkIERepaint)
	{
		this.quirkIERepaint = quirkIERepaint;
	}

	/**
	 * A quirk flag indicating that listbox-style select fields cannot be
	 * reliably manipulated using the client DOM API.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if listbox-style select fields cannot be reliably
	 *         manipulated using the client DOM API.
	 */
	public boolean isQuirkIESelectListDomUpdate()
	{
		return quirkIESelectListDomUpdate;
	}

	/**
	 * @param quirkIESelectListDomUpdate
	 *            True if listbox-style select fields cannot be reliably
	 *            manipulated using the client DOM API.
	 */
	public void setQuirkIESelectListDomUpdate(boolean quirkIESelectListDomUpdate)
	{
		this.quirkIESelectListDomUpdate = quirkIESelectListDomUpdate;
	}

	/**
	 * A quirk flag indicating that select fields with percentage widths are not
	 * reliably rendered.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if select fields with percentage widths are not reliably
	 *         rendered.
	 */
	public boolean isQuirkIESelectPercentWidth()
	{
		return quirkIESelectPercentWidth;
	}

	/**
	 * @param quirkIESelectPercentWidth
	 *            True if select fields with percentage widths are not reliably
	 *            rendered.
	 */
	public void setQuirkIESelectPercentWidth(boolean quirkIESelectPercentWidth)
	{
		this.quirkIESelectPercentWidth = quirkIESelectPercentWidth;
	}

	/**
	 * A quirk flag describing the issue of "windowed" select fields in Internet
	 * Explorer, which do not render correctly with regard to z-index value. See
	 * http://support.microsoft.com/kb/q177378/ for an explanation of the
	 * underlying issue.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if the browser do not render correctly with regard to
	 *         z-index value.
	 */
	public boolean isQuirkIESelectZIndex()
	{
		return quirkIESelectZIndex;
	}

	/**
	 * @param quirkIESelectZIndex
	 *            True if the browser do not render correctly with regard to
	 *            z-index value.
	 */
	public void setQuirkIESelectZIndex(boolean quirkIESelectZIndex)
	{
		this.quirkIESelectZIndex = quirkIESelectZIndex;
	}

	/**
	 * A quirk flag indicating incorrect calculation of 100% table widths when
	 * within a vertically scrolling region.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if incorrect calculation of 100% table widths when within a
	 *         vertically scrolling region.
	 */
	public boolean isQuirkIETablePercentWidthScrollbarError()
	{
		return quirkIETablePercentWidthScrollbarError;
	}

	/**
	 * @param quirkIETablePercentWidthScrollbarError
	 *            True if incorrect calculation of 100% table widths when within
	 *            a vertically scrolling region.
	 */
	public void setQuirkIETablePercentWidthScrollbarError(
			boolean quirkIETablePercentWidthScrollbarError)
	{
		this.quirkIETablePercentWidthScrollbarError = quirkIETablePercentWidthScrollbarError;
	}

	/**
	 * A quirk flag indicating the incorrect parsing of newlines in the content
	 * of a 'textarea'.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 * 
	 * @return True if the browser has incorrect parsing of newlines in the
	 *         content of a 'textarea'.
	 */
	public boolean isQuirkIETextareaNewlineObliteration()
	{
		return quirkIETextareaNewlineObliteration;
	}

	/**
	 * @param quirkIETextareaNewlineObliteration
	 *            True if the browser has incorrect parsing of newlines in the
	 *            content of a 'textarea'.
	 */
	public void setQuirkIETextareaNewlineObliteration(boolean quirkIETextareaNewlineObliteration)
	{
		this.quirkIETextareaNewlineObliteration = quirkIETextareaNewlineObliteration;
	}

	/**
	 * A quirk flag indicating whether the client has poor performance when
	 * attempting to remove large element hierarchies from a DOM. This quirk can
	 * be alleviated by removing the hierarchy in smaller chunks.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Mozilla (all platforms)</li>
	 * <li>Mozilla Firefox ((all platforms)</li>
	 * </ul>
	 * 
	 * @return True if the client has poor performance when attempting to remove
	 *         large element hierarchies from a DOM.
	 */
	public boolean isQuirkMozillaPerformanceLargeDomRemove()
	{
		return quirkMozillaPerformanceLargeDomRemove;
	}

	/**
	 * @param quirkMozillaPerformanceLargeDomRemove
	 *            True if the client has poor performance when attempting to
	 *            remove large element hierarchies from a DOM.
	 */
	public void setQuirkMozillaPerformanceLargeDomRemove(
			boolean quirkMozillaPerformanceLargeDomRemove)
	{
		this.quirkMozillaPerformanceLargeDomRemove = quirkMozillaPerformanceLargeDomRemove;
	}

	/**
	 * A quirk flag describing a Mozilla-specific behavior where the text
	 * contained within text input fields may be drawn outside of text input
	 * component due to the component having shifted its location on the page.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Mozilla (all platforms)</li>
	 * <li>Mozilla Firefox ((all platforms)</li>
	 * </ul>
	 * 
	 * @return True if the text contained within text input fields may be drawn
	 *         outside of text input component due to the component having
	 *         shifted its location on the page.
	 */
	public boolean isQuirkMozillaTextInputRepaint()
	{
		return quirkMozillaTextInputRepaint;
	}

	/**
	 * @param quirkMozillaTextInputRepaint
	 *            True if the text contained within text input fields may be
	 *            drawn outside of text input component due to the component
	 *            having shifted its location on the page.
	 */
	public void setQuirkMozillaTextInputRepaint(boolean quirkMozillaTextInputRepaint)
	{
		this.quirkMozillaTextInputRepaint = quirkMozillaTextInputRepaint;
	}

	/**
	 * @return The client's remote/ip address.
	 */
	public String getRemoteAddress()
	{
		return remoteAddress;
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
	 * @return Color depth of the screen in bits (integer).
	 */
	public int getScreenColorDepth()
	{
		return screenColorDepth;
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
	 * @return Height of the screen in pixels (integer).
	 */
	public int getScreenHeight()
	{
		return screenHeight;
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
	 * @return Height of the screen in pixels (integer).
	 */
	public int getScreenWidth()
	{
		return screenWidth;
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
	 * @return The client's time offset from UTC in minutes (note: if you do
	 *         this yourself, use 'new Date().getTimezoneOffset() / -60' (note
	 *         the -)).
	 */
	public String getUtcOffset()
	{
		return utcOffset;
	}

	/**
	 * @param utcOffset
	 *            The client's time offset from UTC in minutes (note: if you do
	 *            this yourself, use 'new Date().getTimezoneOffset() / -60'
	 *            (note the -)).
	 */
	public void setUtcOffset(String utcOffset)
	{
		this.utcOffset = utcOffset;
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
					sb.append(offsetHours > 0 ? "+" : "-");
					sb.append(Math.abs(offsetHours));
					sb.append(":");
					if (offsetMins < 10)
					{
						sb.append("0");
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
					timeZone = TimeZone.getTimeZone("GMT" + ((offset > 0) ? "+" : "-") + utc);
				}
			}
		}

		return timeZone;
	}

	public String toString()
	{
		StringBuffer b = new StringBuffer();

		Field[] fields = ClientProperties.class.getDeclaredFields();

		for (int i = 0; i < fields.length; i++)
		{
			Field field = fields[i];

			// Ignore these fields
			if (field.getName().equals("serialVersionUID") == false
					&& field.getName().startsWith("class$") == false
					&& field.getName().startsWith("timeZone") == false)
			{

				field.setAccessible(true);

				Object value = null;
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
					if (((Integer)value).intValue() == -1)
					{
						value = null;
					}
				}

				if (value != null)
				{
					b.append(field.getName());
					b.append("=");
					b.append(value);
					b.append("\n");
				}
			}
		}

		return b.toString();
	}
}
