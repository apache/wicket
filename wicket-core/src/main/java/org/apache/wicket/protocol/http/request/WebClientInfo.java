/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.http.request;

import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.markup.html.pages.BrowserInfoPage;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Default client info object for web applications.
 * 
 * @author Eelco Hillenius
 */
public class WebClientInfo extends ClientInfo
{
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(WebClientInfo.class);

	/**
	 * The user agent string from the User-Agent header, app. Theoretically, this might differ from
	 * {@link org.apache.wicket.protocol.http.ClientProperties#isJavaEnabled()} property, which is
	 * not set until an actual reply from a browser (e.g. using {@link BrowserInfoPage} is set.
	 */
	private final String userAgent;

	/** Client properties object. */
	private final ClientProperties properties = new ClientProperties();

	/**
	 * Construct.
	 * 
	 * @param requestCycle
	 *			the request cycle
	 */
	public WebClientInfo(RequestCycle requestCycle)
	{
		this(requestCycle, ((ServletWebRequest)requestCycle.getRequest()).getContainerRequest()
			.getHeader("User-Agent"));
	}

	/**
	 * Construct.
	 * 
	 * @param requestCycle
	 *			the request cycle
	 * @param userAgent
	 *			The User-Agent string
	 */
	public WebClientInfo(final RequestCycle requestCycle, final String userAgent)
	{
		super();

		this.userAgent = userAgent;
		properties.setRemoteAddress(getRemoteAddr(requestCycle));
		init();
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

	/**
	 * returns the user agent string.
	 * 
	 * @return the user agent string
	 */
	public final String getUserAgent()
	{
		return userAgent;
	}

	/**
	 * returns the user agent string (lower case).
	 * 
	 * @return the user agent string
	 */
	private String getUserAgentStringLc()
	{
		return (getUserAgent() != null) ? getUserAgent().toLowerCase() : "";
	}

	/**
	 * When using ProxyPass, requestCycle().getHttpServletRequest(). getRemoteAddr() returns the IP
	 * of the machine forwarding the request. In order to maintain the clients ip address, the
	 * server places it in the <a
	 * href="http://httpd.apache.org/docs/2.2/mod/mod_proxy.html#x-headers">X-Forwarded-For</a>
	 * Header.
	 *
	 * Proxies may also mask the original client IP with tokens like "hidden" or "unknown".
	 * If so, the last proxy ip address is returned.
	 *
	 * @param requestCycle
	 *			the request cycle
	 * @return remoteAddr IP address of the client, using the X-Forwarded-For header and defaulting
	 *		 to: getHttpServletRequest().getRemoteAddr()
	 */
	protected String getRemoteAddr(RequestCycle requestCycle)
	{
		ServletWebRequest request = (ServletWebRequest)requestCycle.getRequest();
		HttpServletRequest req = request.getContainerRequest();
		String remoteAddr = request.getHeader("X-Forwarded-For");

		if (remoteAddr != null)
		{
			if (remoteAddr.contains(","))
			{
				// sometimes the header is of form client ip,proxy 1 ip,proxy 2 ip,...,proxy n ip,
				// we just want the client
				remoteAddr = Strings.split(remoteAddr, ',')[0].trim();
			}
			try
			{
				// If ip4/6 address string handed over, simply does pattern validation.
				InetAddress.getByName(remoteAddr);
			}
			catch (UnknownHostException e)
			{
				remoteAddr = req.getRemoteAddr();
			}
		}
		else
		{
			remoteAddr = req.getRemoteAddr();
		}
		return remoteAddr;
	}

	/**
	 * Initialize the client properties object
	 */
	private void init()
	{
		setInternetExplorerProperties();
		setOperaProperties();
		setMozillaProperties();
		setKonquerorProperties();
		setChromeProperties();
		setSafariProperties();

		if (log.isDebugEnabled())
		{
			log.debug("determined user agent: " + properties);
		}
	}

	/**
	 * sets the konqueror specific properties
	 */
	private void setKonquerorProperties()
	{
		properties.setBrowserKonqueror(UserAgent.KONQUEROR.matches(getUserAgent()));

		if (properties.isBrowserKonqueror())
		{
			// e.g.: Mozilla/5.0 (compatible; Konqueror/4.2; Linux) KHTML/4.2.96 (like Gecko)
			setMajorMinorVersionByPattern("konqueror/(\\d+)\\.(\\d+)");
		}
	}

	/**
	 * sets the chrome specific properties
	 */
	private void setChromeProperties()
	{
		properties.setBrowserChrome(UserAgent.CHROME.matches(getUserAgent()));

		if (properties.isBrowserChrome())
		{
			// e.g.: Mozilla/5.0 (Windows NT 6.1) AppleWebKit/534.24 (KHTML, like Gecko)
// Chrome/12.0.702.0 Safari/534.24
			setMajorMinorVersionByPattern("chrome/(\\d+)\\.(\\d+)");
		}
	}

	/**
	 * sets the safari specific properties
	 */
	private void setSafariProperties()
	{
		properties.setBrowserSafari(UserAgent.SAFARI.matches(getUserAgent()));

		if (properties.isBrowserSafari())
		{
			String userAgent = getUserAgentStringLc();

			if (userAgent.contains("version/"))
			{
				// e.g.: Mozilla/5.0 (Windows; U; Windows NT 6.1; sv-SE) AppleWebKit/533.19.4
// (KHTML, like Gecko) Version/5.0.3 Safari/533.19.4
				setMajorMinorVersionByPattern("version/(\\d+)\\.(\\d+)");
			}
		}
	}

	/**
	 * sets the mozilla/firefox specific properties
	 */
	private void setMozillaProperties()
	{
		properties.setBrowserMozillaFirefox(UserAgent.FIREFOX.matches(getUserAgent()));
		properties.setBrowserMozilla(UserAgent.MOZILLA.matches(getUserAgent()));

		if (properties.isBrowserMozilla())
		{
			properties.setQuirkMozillaTextInputRepaint(true);
			properties.setQuirkMozillaPerformanceLargeDomRemove(true);

			if (properties.isBrowserMozillaFirefox())
			{
				// e.g.: Mozilla/5.0 (X11; U; Linux i686; pl-PL; rv:1.9.0.2) Gecko/20121223
// Ubuntu/9.25 (jaunty) Firefox/3.8
				setMajorMinorVersionByPattern("firefox/(\\d+)\\.(\\d+)");
			}
		}
	}

	/**
	 * sets the opera specific properties
	 */
	private void setOperaProperties()
	{
		properties.setBrowserOpera(UserAgent.OPERA.matches(getUserAgent()));

		if (properties.isBrowserOpera())
		{
			String userAgent = getUserAgentStringLc();

			if (userAgent.startsWith("opera/") && userAgent.contains("version/"))
			{
				// e.g.: Opera/9.80 (Windows NT 6.0; U; nl) Presto/2.6.30 Version/10.60
				setMajorMinorVersionByPattern("version/(\\d+)\\.(\\d+)");
			}
			else if (userAgent.startsWith("opera/") && !userAgent.contains("version/"))
			{
				// e.g.: Opera/9.80 (Windows NT 6.0; U; nl) Presto/2.6.30
				setMajorMinorVersionByPattern("opera/(\\d+)\\.(\\d+)");
			}
			else
			{
				// e.g.: Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 6.0; tr) Opera 10.10
				setMajorMinorVersionByPattern("opera (\\d+)\\.(\\d+)");
			}
		}
	}

	/**
	 * sets the ie specific properties
	 */
	private void setInternetExplorerProperties()
	{
		properties.setBrowserInternetExplorer(UserAgent.INTERNET_EXPLORER.matches(getUserAgent()));

		if (properties.isBrowserInternetExplorer())
		{
            // modern IE browsers (>=IE11) uses new user agent format
			if (getUserAgentStringLc().contains("like gecko")) {
				setMajorMinorVersionByPattern("rv:(\\d+)\\.(\\d+)");
			} else {
				setMajorMinorVersionByPattern("msie (\\d+)\\.(\\d+)");
			}

            // TODO miha: check whether all these flags are correct or not (especially for browsers >=IE10)
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

			if (properties.getBrowserVersionMajor() < 7)
			{
				properties.setProprietaryIEPngAlphaFilterRequired(true);
			}
		}
	}

	/**
	 * extracts the major and minor version out of the userAgentString string.
	 * 
	 * @param patternString
	 *			The pattern must contain two matching groups
	 */
	private void setMajorMinorVersionByPattern(String patternString)
	{
		String userAgent = getUserAgentStringLc();
		Matcher matcher = Pattern.compile(patternString).matcher(userAgent);

		if (matcher.find())
		{
			properties.setBrowserVersionMajor(Integer.parseInt(matcher.group(1)));
			properties.setBrowserVersionMinor(Integer.parseInt(matcher.group(2)));
		}
	}
}
