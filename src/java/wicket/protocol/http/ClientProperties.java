/*
 * This file is part of the Echo Web Application Framework (hereinafter "Echo").
 * Copyright (C) 2002-2005 NextApp, Inc.
 * 
 * Version: MPL 1.1/GPL 2.0/LGPL 2.1
 * 
 * The contents of this file are subject to the Mozilla Public License Version
 * 1.1 (the "License"); you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at http://www.mozilla.org/MPL/
 * 
 * Software distributed under the License is distributed on an "AS IS" basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License for
 * the specific language governing rights and limitations under the License.
 * 
 * Alternatively, the contents of this file may be used under the terms of
 * either the GNU General Public License Version 2 or later (the "GPL"), or the
 * GNU Lesser General Public License Version 2.1 or later (the "LGPL"), in which
 * case the provisions of the GPL or the LGPL are applicable instead of those
 * above. If you wish to allow use of your version of this file only under the
 * terms of either the GPL or the LGPL, and not to allow others to use your
 * version of this file under the terms of the MPL, indicate your decision by
 * deleting the provisions above and replace them with the notice and other
 * provisions required by the GPL or the LGPL. If you do not delete the
 * provisions above, a recipient may use your version of this file under the
 * terms of any one of the MPL, the GPL or the LGPL.
 */

package wicket.protocol.http;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import wicket.util.string.AppendingStringBuffer;

/**
 * A description of the client browser environment.
 * <p>
 * Copied and adjusted from on collegue framework <a
 * href="http://www.nextapp.com/platform/echo2/echo/">Echo 2</a>'s
 * <code>nextapp.echo2.webrender.ClientProperties</code>.
 * </p>
 * 
 * @author Echo 2
 * @author Eelco Hillenius
 */
public class ClientProperties implements Serializable
{

	/**
	 * Flag indicating that the browser is a derivative of the Microsoft
	 * Internet Explorer browser platform.
	 */
	public static final String BROWSER_INTERNET_EXPLORER = "browserInternetExplorer";

	// General CSS Quirks describing specific out-of-spec behaviors particular
	// to certain browsers.

	/**
	 * Flag indicating that the browser is a derivative of the KDE Konqueror
	 * browser platform.
	 */
	public static final String BROWSER_KONQUEROR = "browserKonqueror";

	/**
	 * Flag indicating that the browser is a derivative of the Mozilla 1.0-1.8+
	 * browser platform.
	 */
	public static final String BROWSER_MOZILLA = "browserMozilla";

	/**
	 * Flag indicating that the browser is a derivative of the Mozilla Firefox
	 * 1.0+ browser platform.
	 */
	public static final String BROWSER_MOZILLA_FIREFOX = "browserMozillaFirefox";

	/**
	 * Flag indicating that the browser is a derivative of the Opera browser
	 * platform.
	 */
	public static final String BROWSER_OPERA = "browserOpera";

	// Mozilla-specific Quirk Behaviors (behaviors that are more likely to be
	// described as bugs)

	/**
	 * Flag indicating that the browser is a derivative of the Apple Safari
	 * browser platform.
	 */
	public static final String BROWSER_SAFARI = "browserSafari";

	/**
	 * The major version number of the browser.
	 */
	public static final String BROWSER_VERSION_MAJOR = "browserVersionMajor";

	// Internet Explorer-specific Quirk Behaviors (behaviors that are more
	// likely to be described as bugs)

	/**
	 * The minor version number of the browser.
	 */
	public static final String BROWSER_VERSION_MINOR = "browserVersionMinor";

	/**
	 * The <code>Locale</code> of the client, derived from the language
	 * property.
	 */
	public static final String LOCALES = "locales";

	/**
	 * The client's navigator.appCodeName property.
	 */
	public static final String NAVIGATOR_APP_CODE_NAME = "navigatorAppCodeName";

	/**
	 * The client's navigator.appName property.
	 */
	public static final String NAVIGATOR_APP_NAME = "navigatorAppName";

	/**
	 * The client's navigator.appVersion property.
	 */
	public static final String NAVIGATOR_APP_VERSION = "navigatorAppVersion";

	/**
	 * The client's navigator.cookieEnabled property.
	 */
	public static final String NAVIGATOR_COOKIE_ENABLED = "navigatorCookieEnabled";

	// Internet Explorer-specific Proprietary Features
	// These features are used only to compensate for IE6's lack of proper CSS
	// support.

	/**
	 * The client's navigator.javaEnabled property.
	 */
	public static final String NAVIGATOR_JAVA_ENABLED = "navigatorJavaEnabled";

	/**
	 * The client's navigator.language (or navigator.userLanguage) property.
	 */
	public static final String NAVIGATOR_LANGUAGE = "navigatorLanguage";

	// General Browser Properties

	/**
	 * The client's navigator.platform property.
	 */
	public static final String NAVIGATOR_PLATFORM = "navigatorPlatform";

	/**
	 * The client's navigator.userAgent property.
	 */
	public static final String NAVIGATOR_USER_AGENT = "navigatorUserAgent";

	/**
	 * A proprietary feature flag indicating support for IE-style CSS
	 * expressions.
	 * <p>
	 * This proprietary feature is provided by:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 */
	public static final String PROPRIETARY_IE_CSS_EXPRESSIONS_SUPPORTED = "proprietaryIECssExpressionsSupported";

	/**
	 * A proprietary feature flag indicating that PNG alpha channel support is
	 * available only by using a 'filter'.
	 * <p>
	 * This proprietary feature is provided by:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 */
	public static final String PROPRIETARY_IE_PNG_ALPHA_FILTER_REQUIRED = "proprietaryIEPngAlphaFilterRequired";

	/**
	 * A quirk flag indicating that the 'fixed' attribute should be used to for
	 * fixed-to-element background attachment.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 */
	public static final String QUIRK_CSS_BACKGROUND_ATTACHMENT_USE_FIXED = "quirkCssBackgroundAttachmentUseFixed";

	/**
	 * A quirk flag indicating the only means of achieving 0 padding in table
	 * cells is to use 0px padding.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 */
	public static final String QUIRK_CSS_BORDER_COLLAPSE_FOR_0_PADDING = "quirkCssBorderCollapseFor0Padding";

	/**
	 * A quirk flag indicating whether the client will incorrectly render CSS
	 * collapsed borders such that they reside entirely within the region of a
	 * component.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 */
	public static final String QUIRK_CSS_BORDER_COLLAPSE_INSIDE = "quirkCssBorderCollapseInside";

	/**
	 * A quirk flag indicating that CSS positioning values do not work correctly
	 * when either both "top" and "bottom" or "left" and "right" positions are
	 * set at the same time.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 */
	public static final String QUIRK_CSS_POSITIONING_ONE_SIDE_ONLY = "quirkCssPositioningOneSideOnly";

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
	 */
	public static final String QUIRK_IE_REPAINT = "quirkIERepaint";

	/**
	 * A quirk flag indicating that listbox-style select fields cannot be
	 * reliably manipulated using the client DOM API.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 */
	public static final String QUIRK_IE_SELECT_LIST_DOM_UPDATE = "quirkIESelectListDomUpdate";

	/**
	 * A quirk flag indicating that select fields with percentage widths are not
	 * reliably rendered.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 */
	public static final String QUIRK_IE_SELECT_PERCENT_WIDTH = "quirkIESelectPercentWidth";

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
	 */
	public static final String QUIRK_IE_SELECT_Z_INDEX = "quirkIESelectZIndex";

	/**
	 * A quirk flag indicating incorrect calculation of 100% table widths when
	 * within a vertically scrolling region.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 */
	public static final String QUIRK_IE_TABLE_PERCENT_WIDTH_SCROLLBAR_ERROR = "quirkIETablePercentWidthScrollbarError";

	/**
	 * A quirk flag indicating the incorrect parsing of newlines in the content
	 * of a 'textarea'.
	 * <p>
	 * This quirk occurs with:
	 * <ul>
	 * <li>Internet Explorer 6 (Windows)</li>
	 * </ul>
	 */
	public static final String QUIRK_IE_TEXTAREA_NEWLINE_OBLITERATION = "quirkIETextareaNewlineObliteration";

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
	 */
	public static final String QUIRK_MOZILLA_PERFORMANCE_LARGE_DOM_REMOVE = "quirkMozillaPerformanceLargeDomRemove";

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
	 */
	public static final String QUIRK_MOZILLA_TEXT_INPUT_REPAINT = "quirkMozillaTextInputRepaint";

	/**
	 * Color depth of the screen in bits (integer).
	 */
	public static final String SCREEN_COLOR_DEPTH = "screenColorDepth";

	/**
	 * Height of the screen in pixels (integer).
	 */
	public static final String SCREEN_HEIGHT = "screenHeight";

	/**
	 * Width of the screen in pixels (integer).
	 */
	public static final String SCREEN_WIDTH = "screenWidth";

	/**
	 * The client's time offset from UTC in minutes.
	 */
	public static final String UTC_OFFSET = "utcOffset";

	private static final long serialVersionUID = 1L;

	/**
	 * The actual property data.
	 */
	private Map<String, Object> data = new HashMap<String, Object>();

	/**
	 * The client's time zone, if available.
	 */
	private TimeZone timeZone;

	/**
	 * Creates a new <code>ClientProperties</code> object.
	 */
	public ClientProperties()
	{
		super();
	}

	/**
	 * Returns the value of the specified property as an <code>Object</code>.
	 * 
	 * @param propertyName
	 *            the property name
	 * @return the property value
	 */
	public Object get(String propertyName)
	{
		return data.get(propertyName);
	}

	/**
	 * Returns a <code>boolean</code> property. If the property is not set,
	 * <code>false</code> is returned.
	 * 
	 * @param propertyName
	 *            the property name
	 * @return the property value
	 */
	public boolean getBoolean(String propertyName)
	{
		Boolean value = (Boolean)data.get(propertyName);
		return value == null ? false : value.booleanValue();
	}

	/**
	 * Returns a <code>int</code> property. If the property is not set,
	 * <code>nullValue</code> is returned.
	 * 
	 * @param propertyName
	 *            the property name
	 * @param nullValue
	 *            the returned value when the property is not set
	 * @return the property value
	 */
	public int getInt(String propertyName, int nullValue)
	{
		Integer value = (Integer)data.get(propertyName);
		return value == null ? nullValue : value.intValue();
	}

	/**
	 * Returns an array of all property names which are set.
	 * 
	 * @return the array
	 */
	public String[] getPropertyNames()
	{
		return data.keySet().toArray(new String[data.size()]);
	}

	/**
	 * Returns a <code>String</code> property. If the property is not set,
	 * <code>null</code> is returned.
	 * 
	 * @param propertyName
	 *            the property name
	 * @return the property value
	 */
	public String getString(String propertyName)
	{
		Object value = data.get(propertyName);
		return value == null ? "" : value.toString();
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
			String utcOffset = (String)data.get(ClientProperties.UTC_OFFSET);
			if (utcOffset != null)
			{
				// apparently it is platform dependent on whether you get the
				// offset in a decimal form or not. This parses the decimal
				// form of the UTC offset, taking into account several
				// possibilities
				// such as getting the format in +2.5 or -1.2

				int dotPos = utcOffset.indexOf('.');
				if (dotPos >= 0)
				{
					String hours = utcOffset.substring(0, dotPos);
					String hourPart = utcOffset.substring(dotPos + 1);

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
					int offset = Integer.parseInt(utcOffset);
					if (offset < 0)
					{
						utcOffset = utcOffset.substring(1);
					}
					timeZone = TimeZone.getTimeZone("GMT" + ((offset > 0) ? "+" : "-") + utcOffset);
				}
			}
		}

		return timeZone;
	}

	/**
	 * Sets the value of the specified property.
	 * 
	 * @param propertyName
	 *            the property name
	 * @param propertyValue
	 *            the property value
	 */
	public void setProperty(String propertyName, Object propertyValue)
	{
		data.put(propertyName, propertyValue);
	}

	/**
	 * Sets the client's time zone.
	 * 
	 * @param timeZone
	 *            The time zone to set
	 */
	public void setTimeZone(TimeZone timeZone)
	{
		this.timeZone = timeZone;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		return "ClientProperties: " + data.toString();
	}
}
