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
package org.apache.wicket.protocol.http.servlet;

import java.util.LinkedList;
import java.util.regex.Pattern;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Request wrapper factory to integrate "X-Forwarded-For" and "X-Forwarded-Proto" HTTP headers.
 * <p>
 * Most of the design of this Servlet Filter is a port of <a
 * href="http://httpd.apache.org/docs/trunk/mod/mod_remoteip.html">mod_remoteip</a>, this servlet
 * filter replaces the apparent client remote IP address and hostname for the request with the IP
 * address list presented by a proxy or a load balancer via a request headers (e.g.
 * "X-Forwarded-For").
 * <p>
 * Another feature of this servlet filter is to replace the apparent scheme (http/https) and server
 * port with the scheme presented by a proxy or a load balancer via a request header (e.g.
 * "X-Forwarded-Proto").
 * <p>
 * This wrapper proceeds as follows:
 * <p>
 * If the incoming <code>request.getRemoteAddr()</code> matches the servlet filter's list of
 * internal proxies :
 * <ul>
 * <li>Loop on the comma delimited list of IPs and hostnames passed by the preceding load balancer
 * or proxy in the given request's Http header named <code>$remoteIPHeader</code> (default value
 * <code>x-forwarded-for</code>). Values are processed in right-to-left order.</li>
 * <li>For each ip/host of the list:
 * <ul>
 * <li>if it matches the internal proxies list, the ip/host is swallowed</li>
 * <li>if it matches the trusted proxies list, the ip/host is added to the created proxies header</li>
 * <li>otherwise, the ip/host is declared to be the remote ip and looping is stopped.</li>
 * </ul>
 * </li>
 * <li>If the request http header named <code>$protocolHeader</code> (e.g.
 * <code>x-forwarded-for</code>) equals to the value of <code>protocolHeaderHttpsValue</code>
 * configuration parameter (default <code>https</code>) then <code>request.isSecure = true</code>,
 * <code>request.scheme = https</code> and <code>request.serverPort = 443</code>. Note that 443 can
 * be overwritten with the <code>$httpsServerPort</code> configuration parameter.</li>
 * </ul>
 * </p>
 * <p>
 * <strong>Configuration parameters:</strong>
 * <table border="1">
 * <tr>
 * <th>XForwardedFilter property</th>
 * <th>Description</th>
 * <th>Equivalent mod_remoteip directive</th>
 * <th>Format</th>
 * <th>Default Value</th>
 * </tr>
 * <tr>
 * <td>remoteIPHeader</td>
 * <td>Name of the Http Header read by this servlet filter that holds the list of traversed IP
 * addresses starting from the requesting client</td>
 * <td>RemoteIPHeader</td>
 * <td>Compliant http header name</td>
 * <td>x-forwarded-for</td>
 * </tr>
 * <tr>
 * <td>allowedInternalProxies</td>
 * <td>List of internal proxies ip adress. If they appear in the <code>remoteIpHeader</code> value,
 * they will be trusted and will not appear in the <code>proxiesHeader</code> value</td>
 * <td>RemoteIPInternalProxy</td>
 * <td>Comma delimited list of regular expressions (in the syntax supported by the
 * {@link java.util.regex.Pattern} library)</td>
 * <td>10\.\d{1,3}\.\d{1,3}\.\d{1,3}, 192\.168\.\d{1,3}\.\d{1,3},
 * 172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}, 169\.254\.\d{1,3}\.\d{1,3},
 * 127\.\d{1,3}\.\d{1,3}\.\d{1,3} <br/>
 * By default, 10/8, 192.168/16, 172.16/12, 169.254/16 and 127/8 are allowed</td>
 * </tr>
 * </tr>
 * <tr>
 * <td>proxiesHeader</td>
 * <td>Name of the http header created by this servlet filter to hold the list of proxies that have
 * been processed in the incoming <code>remoteIPHeader</code></td>
 * <td>RemoteIPProxiesHeader</td>
 * <td>Compliant http header name</td>
 * <td>x-forwarded-by</td>
 * </tr>
 * <tr>
 * <td>trustedProxies</td>
 * <td>List of trusted proxies ip adress. If they appear in the <code>remoteIpHeader</code> value,
 * they will be trusted and will appear in the <code>proxiesHeader</code> value</td>
 * <td>RemoteIPTrustedProxy</td>
 * <td>Comma delimited list of regular expressions (in the syntax supported by the
 * {@link java.util.regex.Pattern} library)</td>
 * <td>&nbsp;</td>
 * </tr>
 * <tr>
 * <td>protocolHeader</td>
 * <td>Name of the http header read by this servlet filter that holds the flag that this request</td>
 * <td>N/A</td>
 * <td>Compliant http header name like <code>X-Forwarded-Proto</code>, <code>X-Forwarded-Ssl</code>
 * or <code>Front-End-Https</code></td>
 * <td><code>null</code></td>
 * </tr>
 * <tr>
 * <td>protocolHeaderHttpsValue</td>
 * <td>Value of the <code>protocolHeader</code> to indicate that it is an Https request</td>
 * <td>N/A</td>
 * <td>String like <code>https</code> or <code>ON</code></td>
 * <td><code>https</code></td>
 * </tr>
 * <tr>
 * <tr>
 * <td>httpServerPort</td>
 * <td>Value returned by {@link ServletRequest#getServerPort()} when the <code>protocolHeader</code>
 * indicates <code>http</code> protocol</td>
 * <td>N/A</td>
 * <td>integer</td>
 * <td>80</td>
 * </tr>
 * <tr>
 * <td>httpsServerPort</td>
 * <td>Value returned by {@link ServletRequest#getServerPort()} when the <code>protocolHeader</code>
 * indicates <code>https</code> protocol</td>
 * <td>N/A</td>
 * <td>integer</td>
 * <td>443</td>
 * </tr>
 * </table>
 * </p>
 * <p>
 * <p>
 * <strong>Regular expression vs. IP address blocks:</strong> <code>mod_remoteip</code> allows to
 * use address blocks (e.g. <code>192.168/16</code>) to configure <code>RemoteIPInternalProxy</code>
 * and <code>RemoteIPTrustedProxy</code> ; as the JVM doesnt have a library similar to <a href=
 * "http://apr.apache.org/docs/apr/1.3/group__apr__network__io.html#gb74d21b8898b7c40bf7fd07ad3eb993d"
 * >apr_ipsubnet_test</a>.
 * </p>
 * <hr/>
 * <p>
 * <strong>Sample with internal proxies</strong>
 * </p>
 * <p>
 * XForwardedFilter configuration:
 * </p>
 * <code><pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;fr.xebia.servlet.filter.XForwardedFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;allowedInternalProxies&lt;/param-name&gt;&lt;param-value&gt;192\.168\.0\.10, 192\.168\.0\.11&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-for&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPProxiesHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-by&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;protocolHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-proto&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;</pre></code>
 * <p>
 * Request values:
 * <table border="1">
 * <tr>
 * <th>property</th>
 * <th>Value Before XForwardedFilter</th>
 * <th>Value After XForwardedFilter</th>
 * </tr>
 * <tr>
 * <td>request.remoteAddr</td>
 * <td>192.168.0.10</td>
 * <td>140.211.11.130</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-for']</td>
 * <td>140.211.11.130, 192.168.0.10</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-by']</td>
 * <td>null</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-proto']</td>
 * <td>https</td>
 * <td>https</td>
 * </tr>
 * <tr>
 * <td>request.scheme</td>
 * <td>http</td>
 * <td>https</td>
 * </tr>
 * <tr>
 * <td>request.secure</td>
 * <td>false</td>
 * <td>true</td>
 * </tr>
 * <tr>
 * <td>request.serverPort</td>
 * <td>80</td>
 * <td>443</td>
 * </tr>
 * </table>
 * Note : <code>x-forwarded-by</code> header is null because only internal proxies as been traversed
 * by the request. <code>x-forwarded-by</code> is null because all the proxies are trusted or
 * internal.
 * </p>
 * <hr/>
 * <p>
 * <strong>Sample with trusted proxies</strong>
 * </p>
 * <p>
 * XForwardedFilter configuration:
 * </p>
 * <code><pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;fr.xebia.servlet.filter.XForwardedFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;allowedInternalProxies&lt;/param-name&gt;&lt;param-value&gt;192\.168\.0\.10, 192\.168\.0\.11&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-for&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPProxiesHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-by&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;trustedProxies&lt;/param-name&gt;&lt;param-value&gt;proxy1, proxy2&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;</pre></code>
 * <p>
 * Request values:
 * <table border="1">
 * <tr>
 * <th>property</th>
 * <th>Value Before XForwardedFilter</th>
 * <th>Value After XForwardedFilter</th>
 * </tr>
 * <tr>
 * <td>request.remoteAddr</td>
 * <td>192.168.0.10</td>
 * <td>140.211.11.130</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-for']</td>
 * <td>140.211.11.130, proxy1, proxy2</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-by']</td>
 * <td>null</td>
 * <td>proxy1, proxy2</td>
 * </tr>
 * </table>
 * Note : <code>proxy1</code> and <code>proxy2</code> are both trusted proxies that come in
 * <code>x-forwarded-for</code> header, they both are migrated in <code>x-forwarded-by</code>
 * header. <code>x-forwarded-by</code> is null because all the proxies are trusted or internal.
 * </p>
 * <hr/>
 * <p>
 * <strong>Sample with internal and trusted proxies</strong>
 * </p>
 * <p>
 * XForwardedFilter configuration:
 * </p>
 * <code><pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;fr.xebia.servlet.filter.XForwardedFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;allowedInternalProxies&lt;/param-name&gt;&lt;param-value&gt;192\.168\.0\.10, 192\.168\.0\.11&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-for&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPProxiesHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-by&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;trustedProxies&lt;/param-name&gt;&lt;param-value&gt;proxy1, proxy2&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;</pre></code>
 * <p>
 * Request values:
 * <table border="1">
 * <tr>
 * <th>property</th>
 * <th>Value Before XForwardedFilter</th>
 * <th>Value After XForwardedFilter</th>
 * </tr>
 * <tr>
 * <td>request.remoteAddr</td>
 * <td>192.168.0.10</td>
 * <td>140.211.11.130</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-for']</td>
 * <td>140.211.11.130, proxy1, proxy2, 192.168.0.10</td>
 * <td>null</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-by']</td>
 * <td>null</td>
 * <td>proxy1, proxy2</td>
 * </tr>
 * </table>
 * Note : <code>proxy1</code> and <code>proxy2</code> are both trusted proxies that come in
 * <code>x-forwarded-for</code> header, they both are migrated in <code>x-forwarded-by</code>
 * header. As <code>192.168.0.10</code> is an internal proxy, it does not appear in
 * <code>x-forwarded-by</code>. <code>x-forwarded-by</code> is null because all the proxies are
 * trusted or internal.
 * </p>
 * <hr/>
 * <p>
 * <strong>Sample with an untrusted proxy</strong>
 * </p>
 * <p>
 * XForwardedFilter configuration:
 * </p>
 * <code><pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;fr.xebia.servlet.filter.XForwardedFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;allowedInternalProxies&lt;/param-name&gt;&lt;param-value&gt;192\.168\.0\.10, 192\.168\.0\.11&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-for&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;remoteIPProxiesHeader&lt;/param-name&gt;&lt;param-value&gt;x-forwarded-by&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;trustedProxies&lt;/param-name&gt;&lt;param-value&gt;proxy1, proxy2&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;XForwardedFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;</pre></code>
 * <p>
 * Request values:
 * <table border="1">
 * <tr>
 * <th>property</th>
 * <th>Value Before XForwardedFilter</th>
 * <th>Value After XForwardedFilter</th>
 * </tr>
 * <tr>
 * <td>request.remoteAddr</td>
 * <td>192.168.0.10</td>
 * <td>untrusted-proxy</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-for']</td>
 * <td>140.211.11.130, untrusted-proxy, proxy1</td>
 * <td>140.211.11.130</td>
 * </tr>
 * <tr>
 * <td>request.header['x-forwarded-by']</td>
 * <td>null</td>
 * <td>proxy1</td>
 * </tr>
 * </table>
 * Note : <code>x-forwarded-by</code> holds the trusted proxy <code>proxy1</code>.
 * <code>x-forwarded-by</code> holds <code>140.211.11.130</code> because
 * <code>untrusted-proxy</code> is not trusted and thus, we can not trust that
 * <code>untrusted-proxy</code> is the actual remote ip. <code>request.remoteAddr</code> is
 * <code>untrusted-proxy</code> that is an IP verified by <code>proxy1</code>.
 * </p>
 * <hr/>
 * 
 * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
 * @author Juergen Donnerstag
 */
public class XForwardedRequestWrapperFactory extends AbstractRequestWrapperFactory
{
	/** Logger */
	private static final Logger log = LoggerFactory.getLogger(XForwardedRequestWrapperFactory.class);

	protected static final String HTTP_SERVER_PORT_PARAMETER = "httpServerPort";

	protected static final String HTTPS_SERVER_PORT_PARAMETER = "httpsServerPort";

	protected static final String INTERNAL_PROXIES_PARAMETER = "allowedInternalProxies";

	protected static final String PROTOCOL_HEADER_PARAMETER = "protocolHeader";

	protected static final String PROTOCOL_HEADER_SSL_VALUE_PARAMETER = "protocolHeaderSslValue";

	protected static final String PROXIES_HEADER_PARAMETER = "proxiesHeader";

	protected static final String REMOTE_IP_HEADER_PARAMETER = "remoteIPHeader";

	protected static final String TRUSTED_PROXIES_PARAMETER = "trustedProxies";

	/**
	 * Filter Config
	 */
	public static class Config
	{
		// Enable / disable xforwarded functionality
		private boolean enabled = true;

		/** @see #setHttpServerPort(int) */
		private int httpServerPort = 80;

		/** @see #setHttpsServerPort(int) */
		private int httpsServerPort = 443;

		/** @see #setProtocolHeader(String) */
		private String protocolHeader = null;

		/** @see #setProtocolHeaderSslValue(String) */
		private String protocolHeaderSslValue = "https";

		/** @see #setProxiesHeader(String) */
		private String proxiesHeader = "X-Forwarded-By";

		/** @see #setRemoteIPHeader(String) */
		private String remoteIPHeader = "X-Forwarded-For";

		/** @see #setTrustedProxies(String) */
		private Pattern[] trustedProxies = new Pattern[0];

		/** @see #setTrustedProxies(String) */
		private Pattern[] allowedInternalProxies = new Pattern[] {
				Pattern.compile("10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"),
				Pattern.compile("192\\.168\\.\\d{1,3}\\.\\d{1,3}"),
				Pattern.compile("172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}"),
				Pattern.compile("169\\.254\\.\\d{1,3}\\.\\d{1,3}"),
				Pattern.compile("127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") };

		/**
		 * Comma delimited list of internal proxies. Expressed with regular expressions.
		 * <p>
		 * Default value : 10\.\d{1,3}\.\d{1,3}\.\d{1,3}, 192\.168\.\d{1,3}\.\d{1,3},
		 * 172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}, 169\.254\.\d{1,3}\.\d{1,3},
		 * 127\.\d{1,3}\.\d{1,3}\.\d{1,3}
		 * 
		 * @param allowedInternalProxies
		 */
		public void setAllowedInternalProxies(final String allowedInternalProxies)
		{
			this.allowedInternalProxies = commaDelimitedListToPatternArray(allowedInternalProxies);
		}

		/**
		 * Server Port value if the {@link #protocolHeader} does not indicate HTTPS
		 * <p>
		 * Default value : 80
		 * 
		 * @param httpServerPort
		 */
		public void setHttpServerPort(final int httpServerPort)
		{
			this.httpServerPort = httpServerPort;
		}

		/**
		 * Server Port value if the {@link #protocolHeader} indicates HTTPS
		 * <p>
		 * Default value : 443
		 * 
		 * @param httpsServerPort
		 */
		public void setHttpsServerPort(final int httpsServerPort)
		{
			this.httpsServerPort = httpsServerPort;
		}

		/**
		 * Header that holds the incoming protocol, usally named <code>X-Forwarded-Proto</code>. If
		 * <code>null</code>, request.scheme and request.secure will not be modified.
		 * <p>
		 * Default value : <code>null</code>
		 * 
		 * @param protocolHeader
		 */
		public void setProtocolHeader(final String protocolHeader)
		{
			this.protocolHeader = protocolHeader;
		}

		/**
		 * Case insensitive value of the protocol header to indicate that the incoming http request
		 * uses SSL.
		 * <p>
		 * Default value : <code>HTTPS</code>
		 * 
		 * @param protocolHeaderSslValue
		 */
		public void setProtocolHeaderSslValue(final String protocolHeaderSslValue)
		{
			this.protocolHeaderSslValue = protocolHeaderSslValue;
		}

		/**
		 * The proxiesHeader directive specifies a header into which mod_remoteip will collect a
		 * list of all of the intermediate client IP addresses trusted to resolve the actual remote
		 * IP. Note that intermediate RemoteIPTrustedProxy addresses are recorded in this header,
		 * while any intermediate RemoteIPInternalProxy addresses are discarded.
		 * <p>
		 * Name of the http header that holds the list of trusted proxies that has been traversed by
		 * the http request.
		 * <p>
		 * The value of this header can be comma delimited.
		 * <p>
		 * Default value : <code>X-Forwarded-By</code>
		 * 
		 * @param proxiesHeader
		 */
		public void setProxiesHeader(final String proxiesHeader)
		{
			this.proxiesHeader = proxiesHeader;
		}

		/**
		 * Name of the http header from which the remote ip is extracted.
		 * <p>
		 * The value of this header can be comma delimited.
		 * <p>
		 * Default value : <code>X-Forwarded-For</code>
		 * 
		 * @param remoteIPHeader
		 */
		public void setRemoteIPHeader(final String remoteIPHeader)
		{
			this.remoteIPHeader = remoteIPHeader;
		}

		/**
		 * Comma delimited list of proxies that are trusted when they appear in the
		 * {@link #remoteIPHeader} header. Can be expressed as a regular expression.
		 * <p>
		 * Default value : empty list, no external proxy is trusted.
		 * 
		 * @param trustedProxies
		 */
		public void setTrustedProxies(final String trustedProxies)
		{
			this.trustedProxies = commaDelimitedListToPatternArray(trustedProxies);
		}

		/**
		 * Enable / disable XForwarded related processing
		 * 
		 * @param enable
		 */
		public void setEnabled(boolean enable)
		{
			enabled = enable;
		}

		/**
		 * @return True, if filter is active
		 */
		public boolean isEnabled()
		{
			return enabled;
		}
	}

	// Filter Config
	private Config config = new Config();

	/**
	 * Construct.
	 */
	public XForwardedRequestWrapperFactory()
	{
	}

	/**
	 * @return XForwarded filter specific config
	 */
	public final Config getConfig()
	{
		return config;
	}

	/**
	 * The Wicket application might want to provide its own config
	 * 
	 * @param config
	 */
	public final void setConfig(final Config config)
	{
		this.config = config;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean needsWrapper(final HttpServletRequest request)
	{
		boolean rtn = matchesOne(request.getRemoteAddr(), config.allowedInternalProxies);
		if (rtn == false)
		{
			if (log.isDebugEnabled())
			{
				log.debug("Skip XForwardedFilter for request " + request.getRequestURI() +
					" with remote address " + request.getRemoteAddr());
			}
		}
		return rtn;
	}

	/**
	 * 
	 * @param request
	 * @return Either the original request or the wrapper
	 */
	@Override
	public HttpServletRequest newRequestWrapper(final HttpServletRequest request)
	{
		String remoteIp = null;

		// In java 6, proxiesHeaderValue should be declared as a java.util.Deque
		LinkedList<String> proxiesHeaderValue = new LinkedList<String>();

		String[] remoteIPHeaderValue = commaDelimitedListToStringArray(request.getHeader(config.remoteIPHeader));

		// loop on remoteIPHeaderValue to find the first trusted remote ip and to build the
		// proxies chain
		int idx;
		for (idx = remoteIPHeaderValue.length - 1; idx >= 0; idx--)
		{
			String currentRemoteIp = remoteIPHeaderValue[idx];
			remoteIp = currentRemoteIp;
			if (matchesOne(currentRemoteIp, config.allowedInternalProxies))
			{
				// do nothing, allowedInternalProxies IPs are not appended to the
			}
			else if (matchesOne(currentRemoteIp, config.trustedProxies))
			{
				proxiesHeaderValue.addFirst(currentRemoteIp);
			}
			else
			{
				idx--; // decrement idx because break statement doesn't do it
				break;
			}
		}

		// continue to loop on remoteIPHeaderValue to build the new value of the remoteIPHeader
		LinkedList<String> newRemoteIpHeaderValue = new LinkedList<String>();
		for (; idx >= 0; idx--)
		{
			String currentRemoteIp = remoteIPHeaderValue[idx];
			newRemoteIpHeaderValue.addFirst(currentRemoteIp);
		}

		XForwardedRequestWrapper xRequest = new XForwardedRequestWrapper(request);
		if (remoteIp != null)
		{
			xRequest.setRemoteAddr(remoteIp);
			xRequest.setRemoteHost(remoteIp);

			if (proxiesHeaderValue.size() == 0)
			{
				xRequest.removeHeader(config.proxiesHeader);
			}
			else
			{
				String commaDelimitedListOfProxies = listToCommaDelimitedString(proxiesHeaderValue);
				xRequest.setHeader(config.proxiesHeader, commaDelimitedListOfProxies);
			}
			if (newRemoteIpHeaderValue.size() == 0)
			{
				xRequest.removeHeader(config.remoteIPHeader);
			}
			else
			{
				String commaDelimitedRemoteIpHeaderValue = listToCommaDelimitedString(newRemoteIpHeaderValue);
				xRequest.setHeader(config.remoteIPHeader, commaDelimitedRemoteIpHeaderValue);
			}
		}

		if (config.protocolHeader != null)
		{
			String protocolHeaderValue = request.getHeader(config.protocolHeader);
			if (protocolHeaderValue == null)
			{
				// don't modify the secure,scheme and serverPort attributes of the request
			}
			else if (config.protocolHeaderSslValue.equalsIgnoreCase(protocolHeaderValue))
			{
				xRequest.setSecure(true);
				xRequest.setScheme("https");
				xRequest.setServerPort(config.httpsServerPort);
			}
			else
			{
				xRequest.setSecure(false);
				xRequest.setScheme("http");
				xRequest.setServerPort(config.httpServerPort);
			}
		}

		if (log.isDebugEnabled())
		{
			log.debug("Incoming request " + request.getRequestURI() + " with originalRemoteAddr '" +
				request.getRemoteAddr() + "', originalRemoteHost='" + request.getRemoteHost() +
				"', originalSecure='" + request.isSecure() + "', originalScheme='" +
				request.getScheme() + "', original[" + config.remoteIPHeader + "]='" +
				request.getHeader(config.remoteIPHeader) + ", original[" + config.protocolHeader +
				"]='" +
				(config.protocolHeader == null ? null : request.getHeader(config.protocolHeader)) +
				"' will be seen as newRemoteAddr='" + xRequest.getRemoteAddr() +
				"', newRemoteHost='" + xRequest.getRemoteHost() + "', newScheme='" +
				xRequest.getScheme() + "', newSecure='" + xRequest.isSecure() + "', new[" +
				config.remoteIPHeader + "]='" + xRequest.getHeader(config.remoteIPHeader) +
				", new[" + config.proxiesHeader + "]='" + xRequest.getHeader(config.proxiesHeader) +
				"'");
		}
		return xRequest;
	}

	/**
	 * 
	 * @param filterConfig
	 */
	public void init(final FilterConfig filterConfig)
	{
		if (filterConfig.getInitParameter(INTERNAL_PROXIES_PARAMETER) != null)
		{
			config.setAllowedInternalProxies(filterConfig.getInitParameter(INTERNAL_PROXIES_PARAMETER));
		}

		if (filterConfig.getInitParameter(PROTOCOL_HEADER_PARAMETER) != null)
		{
			config.setProtocolHeader(filterConfig.getInitParameter(PROTOCOL_HEADER_PARAMETER));
		}

		if (filterConfig.getInitParameter(PROTOCOL_HEADER_SSL_VALUE_PARAMETER) != null)
		{
			config.setProtocolHeaderSslValue(filterConfig.getInitParameter(PROTOCOL_HEADER_SSL_VALUE_PARAMETER));
		}

		if (filterConfig.getInitParameter(PROXIES_HEADER_PARAMETER) != null)
		{
			config.setProxiesHeader(filterConfig.getInitParameter(PROXIES_HEADER_PARAMETER));
		}

		if (filterConfig.getInitParameter(REMOTE_IP_HEADER_PARAMETER) != null)
		{
			config.setRemoteIPHeader(filterConfig.getInitParameter(REMOTE_IP_HEADER_PARAMETER));
		}

		if (filterConfig.getInitParameter(TRUSTED_PROXIES_PARAMETER) != null)
		{
			config.setTrustedProxies(filterConfig.getInitParameter(TRUSTED_PROXIES_PARAMETER));
		}

		if (filterConfig.getInitParameter(HTTP_SERVER_PORT_PARAMETER) != null)
		{
			try
			{
				config.setHttpServerPort(Integer.parseInt(filterConfig.getInitParameter(HTTP_SERVER_PORT_PARAMETER)));
			}
			catch (NumberFormatException e)
			{
				throw new NumberFormatException("Illegal " + HTTP_SERVER_PORT_PARAMETER + " : " +
					e.getMessage());
			}
		}

		if (filterConfig.getInitParameter(HTTPS_SERVER_PORT_PARAMETER) != null)
		{
			try
			{
				config.setHttpsServerPort(Integer.parseInt(filterConfig.getInitParameter(HTTPS_SERVER_PORT_PARAMETER)));
			}
			catch (NumberFormatException e)
			{
				throw new NumberFormatException("Illegal " + HTTPS_SERVER_PORT_PARAMETER + " : " +
					e.getMessage());
			}
		}
	}
}
