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
package wicket.protocol.http.request;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import wicket.WicketRuntimeException;
import wicket.protocol.http.ClientProperties;
import wicket.protocol.http.WebRequestCycle;
import wicket.request.ClientInfo;

/**
 * Default client info object for web applications.
 * 
 * Based on <code>nextapp.echo2.webrender.ClientAnalyzerProcessor</code> from
 * collegue java webapplication framework <a
 * href="http://www.nextapp.com/platform/echo2/echo/">NextApp Echo 2</a>.
 * 
 * @author Eelco Hillenius
 */
public class WebClientInfo extends ClientInfo
{
	private static final long serialVersionUID = 1L;

	/** log. */
	private static final Log log = LogFactory.getLog(WebClientInfo.class);

	/**
	 * The user agent string from the User-Agent header, app. Theoretically,
	 * this might differ from {@link ClientProperties#NAVIGATOR_JAVA_ENABLED}
	 * property, which is not set until an actual reply from a browser (e.g.
	 * using {@link wicket.markup.html.pages.BrowserInfoPage} is set.
	 */
	private final String userAgent;

	/** Client properties object. */
	private final ClientProperties properties = new ClientProperties();

	/**
	 * Construct.
	 * 
	 * @param requestCycle
	 *            the request cycle
	 */
	public WebClientInfo(WebRequestCycle requestCycle)
	{
		super();
		HttpServletRequest httpServletRequest = requestCycle.getWebRequest()
				.getHttpServletRequest();
		userAgent = httpServletRequest.getHeader("User-Agent");
		if (userAgent == null)
		{
			throw new WicketRuntimeException("unable to read header 'User-Agent'");
		}
		properties.setRemoteAddress(httpServletRequest.getRemoteAddr());
		init();
	}

	/**
	 * Construct.
	 * 
	 * @param requestCycle
	 *            the request cycle
	 * @param userAgent
	 *            the user agent
	 */
	public WebClientInfo(WebRequestCycle requestCycle, String userAgent)
	{
		super();

		if (userAgent == null)
		{
			throw new WicketRuntimeException("user agent must be provided");
		}

		this.userAgent = userAgent;
		HttpServletRequest httpServletRequest = requestCycle.getWebRequest()
		.getHttpServletRequest();
		properties.setRemoteAddress(httpServletRequest.getRemoteAddr());
		init();
	}

	/**
	 * Initialize the client properties object
	 */
	private final void init()
	{
		String userAgent = getUserAgent().toLowerCase();

		boolean browserOpera = userAgent.indexOf("opera") != -1;
		boolean browserSafari = userAgent.indexOf("safari") != -1;
		boolean browserKonqueror = userAgent.indexOf("konqueror") != -1;

		// Note deceptive user agent fields:
		// - Konqueror and Safari UA fields contain "like Gecko"
		// - Opera UA field typically contains "MSIE"
		boolean deceptiveUserAgent = browserOpera || browserSafari || browserKonqueror;

		boolean browserMozilla = !deceptiveUserAgent && userAgent.indexOf("gecko") != -1;
		boolean browserFireFox = userAgent.indexOf("firefox") != -1;
		boolean browserInternetExplorer = !deceptiveUserAgent && userAgent.indexOf("msie") != -1;

		int majorVersion = -1, minorVersion = -1;

		// Store browser information.
		if (browserOpera)
		{
			properties.setBrowserOpera(true);
		}
		else if (browserKonqueror)
		{
			properties.setBrowserKonqueror(true);
		}
		else if (browserSafari)
		{
			properties.setBrowserSafari(true);
		}
		else if (browserMozilla)
		{
			properties.setBrowserMozilla(true);
			if (browserFireFox)
			{
				properties.setBrowserMozillaFirefox(true);
			}
		}
		else if (browserInternetExplorer)
		{
			properties.setBrowserInternetExplorer(true);
			if (userAgent.indexOf("msie 6.") != -1)
			{
				majorVersion = 6;
			}
			else if (userAgent.indexOf("msie 7.") != -1)
			{
				majorVersion = 7;
			}
		}

		if (majorVersion != -1)
		{
			properties.setBrowserVersionMajor(majorVersion);
		}

		if (minorVersion != -1)
		{
			properties.setBrowserVersionMinor(minorVersion);
		}

		// Set quirk flags.
		if (browserInternetExplorer)
		{
			properties.setProprietaryIECssExpressionsSupported(true);
			properties.setQuirkCssPositioningOneSideOnly(true);
			properties.setQuirkIERepaint(true);
			properties.setQuirkIESelectZIndex(true);
			properties.setQuirkIETextareaNewlineObliteration(true);
			properties.setQuirkIESelectPercentWidth(true);
			properties.setQuirkIESelectListDomUpdate(true);
			properties.setQuirkIETablePercentWidthScrollbarError(true);
			properties.setQuirkCssBackgroundAttachmentUseFixed(true);
			properties.setQuirkCssBorderCollapseInside(true);
			properties.setQuirkCssBorderCollapseFor0Padding(true);
			if (majorVersion < 7)
			{
				properties.setProprietaryIEPngAlphaFilterRequired(true);
			}
		}
		if (browserMozilla)
		{
			properties.setQuirkMozillaTextInputRepaint(true);
			properties.setQuirkMozillaPerformanceLargeDomRemove(true);
		}

		if (log.isDebugEnabled())
		{
			log.debug("determined user agent: " + properties);
		}
	}

	/**
	 * Gets the user agent string.
	 * 
	 * @return the user agent string
	 */
	public final String getUserAgent()
	{
		return userAgent;
	}

	/**
	 * Gets the client properties object.
	 * 
	 * @return the client properties object
	 */
	public final ClientProperties getProperties()
	{
		return properties;
	}
}
