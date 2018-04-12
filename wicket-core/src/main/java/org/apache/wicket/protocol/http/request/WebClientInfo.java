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
package org.apache.wicket.protocol.http.request;

import java.net.InetAddress;
import java.net.UnknownHostException;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.core.request.ClientInfo;
import org.apache.wicket.markup.html.pages.BrowserInfoPage;
import org.apache.wicket.protocol.http.ClientProperties;
import org.apache.wicket.protocol.http.servlet.ServletWebRequest;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import nl.basjes.parse.useragent.UserAgentAnalyzer;


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
	 * {@link org.apache.wicket.protocol.http.ClientProperties#isNavigatorJavaEnabled()} property,
	 * which is not set until an actual reply from a browser (e.g. using {@link BrowserInfoPage} is
	 * set.
	 */
	private final String userAgent;

	/** Client properties object. */
	private final ClientProperties properties;

	private final static UserAgentAnalyzer UAA = UserAgentAnalyzer.newBuilder()
		.hideMatcherLoadStats()
		.withCache(25000)
		.build();

	/**
	 * Construct.
	 * 
	 * @param requestCycle
	 *            the request cycle
	 */
	public WebClientInfo(RequestCycle requestCycle)
	{
		this(requestCycle, new ClientProperties());
	}

	/**
	 * Construct.
	 * 
	 * @param requestCycle
	 *            the request cycle
	 * @param properties
	 *            the client properties
	 */
	public WebClientInfo(RequestCycle requestCycle, ClientProperties properties)
	{
		this(requestCycle, ((ServletWebRequest)requestCycle.getRequest()).getContainerRequest()
			.getHeader("User-Agent"), properties);
	}

	/**
	 * Construct.
	 * 
	 * @param requestCycle
	 *            the request cycle
	 * @param userAgent
	 *            The User-Agent string
	 */
	public WebClientInfo(final RequestCycle requestCycle, final String userAgent)
	{
		this(requestCycle, userAgent, new ClientProperties());
	}

	/**
	 * Construct.
	 * 
	 * @param requestCycle
	 *            the request cycle
	 * @param userAgent
	 *            The User-Agent string
	 * @param properties
	 *            properties of client
	 */
	public WebClientInfo(final RequestCycle requestCycle, final String userAgent,
		final ClientProperties properties)
	{
		this.userAgent = userAgent;
		this.properties = properties;
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
	 * server places it in the
	 * <a href="http://httpd.apache.org/docs/2.2/mod/mod_proxy.html#x-headers">X-Forwarded-For</a>
	 * Header.
	 *
	 * Proxies may also mask the original client IP with tokens like "hidden" or "unknown". If so,
	 * the last proxy ip address is returned.
	 *
	 * @param requestCycle
	 *            the request cycle
	 * @return remoteAddr IP address of the client, using the X-Forwarded-For header and defaulting
	 *         to: getHttpServletRequest().getRemoteAddr()
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
		nl.basjes.parse.useragent.UserAgent parsedUserAgent = UAA.parse(getUserAgent());
		setInternetExplorerProperties(parsedUserAgent);
		setKonquerorProperties(parsedUserAgent);
		setMozillaProperties(parsedUserAgent);
		setOperaProperties(parsedUserAgent);
		setChromeProperties(parsedUserAgent);
		setEdgeProperties(parsedUserAgent);
		setSafariProperties(parsedUserAgent);

		log.debug("determined user agent: {}", properties);
	}

	/**
	 * sets the konqueror specific properties
	 * 
	 * @param parsedUserAgent
	 */
	private void setKonquerorProperties(nl.basjes.parse.useragent.UserAgent parsedUserAgent)
	{
		properties.setBrowserKonqueror(
			UserAgent.KONQUEROR.getUaStrings().contains(parsedUserAgent.getValue("AgentName")));
		setBrowserVersion(parsedUserAgent);
	}

	/**
	 * sets the chrome specific properties
	 * 
	 * @param parsedUserAgent
	 */
	private void setChromeProperties(nl.basjes.parse.useragent.UserAgent parsedUserAgent)
	{
		properties.setBrowserChrome(
			UserAgent.CHROME.getUaStrings().contains(parsedUserAgent.getValue("AgentName")));
		setBrowserVersion(parsedUserAgent);
	}

	/**
	 * sets the Edge specific properties
	 * 
	 * @param parsedUserAgent
	 */
	private void setEdgeProperties(nl.basjes.parse.useragent.UserAgent parsedUserAgent)
	{
		properties.setBrowserEdge(
			UserAgent.EDGE.getUaStrings().contains(parsedUserAgent.getValue("AgentName")));
		setBrowserVersion(parsedUserAgent);
	}

	/**
	 * sets the safari specific properties
	 * 
	 * @param parsedUserAgent
	 */
	private void setSafariProperties(nl.basjes.parse.useragent.UserAgent parsedUserAgent)
	{
		properties.setBrowserSafari(
			UserAgent.SAFARI.getUaStrings().contains(parsedUserAgent.getValue("AgentName")));
		setBrowserVersion(parsedUserAgent);
	}

	/**
	 * sets the mozilla/firefox specific properties
	 * 
	 * @param parsedUserAgent
	 */
	private void setMozillaProperties(nl.basjes.parse.useragent.UserAgent parsedUserAgent)
	{
		boolean isFireFox = UserAgent.FIREFOX.getUaStrings()
			.contains(parsedUserAgent.getValue("AgentName"));
		if (isFireFox)
		{
			properties.setBrowserMozillaFirefox(true);
			properties.setBrowserMozilla(true);
		}
		else
		{
			properties.setBrowserMozilla(
				UserAgent.MOZILLA.getUaStrings().contains(parsedUserAgent.getValue("AgentName")));
		}
		setBrowserVersion(parsedUserAgent);
	}

	/**
	 * sets the opera specific properties
	 * 
	 * @param parsedUserAgent
	 */
	private void setOperaProperties(nl.basjes.parse.useragent.UserAgent parsedUserAgent)
	{
		properties.setBrowserOpera(
			UserAgent.OPERA.getUaStrings().contains(parsedUserAgent.getValue("AgentName")));
		setBrowserVersion(parsedUserAgent);
	}

	/**
	 * sets the ie specific properties
	 * 
	 * @param parsedUserAgent
	 */
	private void setInternetExplorerProperties(nl.basjes.parse.useragent.UserAgent parsedUserAgent)
	{
		properties.setBrowserInternetExplorer(UserAgent.INTERNET_EXPLORER.getUaStrings()
			.contains(parsedUserAgent.getValue("AgentName")));
		setBrowserVersion(parsedUserAgent);
	}
	
	private void setBrowserVersion(nl.basjes.parse.useragent.UserAgent parsedUserAgent)
	{
		String value = parsedUserAgent.get("AgentVersion").getValue();
		if(!"Hacker".equals(value)) {
			properties.setBrowserVersion(value);
		}
	}
}
