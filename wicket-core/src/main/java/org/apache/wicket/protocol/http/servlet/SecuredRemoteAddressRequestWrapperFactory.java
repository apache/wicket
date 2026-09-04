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

import java.util.regex.Pattern;

import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Sets {@link ServletRequest#isSecure()} to <code>true</code> if
 * {@link ServletRequest#getRemoteAddr()} matches one of the <code>securedRemoteAddresses</code> of
 * this filter.
 * <p>
 * This filter is often used in combination with {@link XForwardedRequestWrapperFactory} to get the
 * remote address of the client even if the request goes through load balancers (e.g. F5 Big IP,
 * Nortel Alteon) or proxies (e.g. Apache mod_proxy_http)
 * <p>
 * <strong>Configuration parameters:</strong>
 * <table border="1">
 * <caption>Configuration parameters</caption>
 * <tr>
 * <th>XForwardedFilter property</th>
 * <th>Description</th>
 * <th>Format</th>
 * <th>Default value</th>
 * </tr>
 * <tr>
 * <td>securedRemoteAddresses</td>
 * <td>IP addresses for which {@link ServletRequest#isSecure()} must return <code>true</code></td>
 * <td>Comma delimited list of regular expressions (in the syntax supported by the
 * {@link java.util.regex.Pattern} library)</td>
 * <td>Class A, B and C <a href="http://en.wikipedia.org/wiki/Private_network">private network IP
 * address blocks</a> : 10\.\d{1,3}\.\d{1,3}\.\d{1,3}, 192\.168\.\d{1,3}\.\d{1,3},
 * 172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}, 169\.254\.\d{1,3}\.\d{1,3},
 * 127\.\d{1,3}\.\d{1,3}\.\d{1,3}</td>
 * </tr>
 * </table>
 * Note : the default configuration is can usually be used as internal servers are often trusted.
 * </p>
 * <p>
 * <strong>Sample with secured remote addresses limited to 192.168.0.10 and 192.168.0.11</strong>
 * </p>
 * <p>
 * SecuredRemoteAddressFilter configuration sample :
 * </p>
 * 
 * <code><pre>
 * &lt;filter&gt;
 *    &lt;filter-name&gt;SecuredRemoteAddressFilter&lt;/filter-name&gt;
 *    &lt;filter-class&gt;fr.xebia.servlet.filter.SecuredRemoteAddressFilter&lt;/filter-class&gt;
 *    &lt;init-param&gt;
 *       &lt;param-name&gt;securedRemoteAddresses&lt;/param-name&gt;&lt;param-value&gt;192\.168\.0\.10, 192\.168\.0\.11&lt;/param-value&gt;
 *    &lt;/init-param&gt;
 * &lt;/filter&gt;
 * 
 * &lt;filter-mapping&gt;
 *    &lt;filter-name&gt;SecuredRemoteAddressFilter&lt;/filter-name&gt;
 *    &lt;url-pattern&gt;/*&lt;/url-pattern&gt;
 *    &lt;dispatcher&gt;REQUEST&lt;/dispatcher&gt;
 * &lt;/filter-mapping&gt;</pre></code>
 * <p>
 * A request with <code>{@link ServletRequest#getRemoteAddr()} = 192.168.0.10 or 192.168.0.11</code>
 * will be seen as <code>{@link ServletRequest#isSecure()} == true</code> even if
 * <code>{@link HttpServletRequest#getScheme()} == "http"</code>.
 * </p>
 * 
 * @author <a href="mailto:cyrille@cyrilleleclerc.com">Cyrille Le Clerc</a>
 * @author Juergen Donnerstag
 */
public class SecuredRemoteAddressRequestWrapperFactory extends AbstractRequestWrapperFactory
{
	/** Logger */
	private static final Logger log = LoggerFactory.getLogger(SecuredRemoteAddressRequestWrapperFactory.class);

	private final static String SECURED_REMOTE_ADDRESSES_PARAMETER = "securedRemoteAddresses";

	public static class Config
	{
		/** @see #setSecuredRemoteAdresses(String) */
		private Pattern[] securedRemoteAddresses = new Pattern[] {
				Pattern.compile("10\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}"),
				Pattern.compile("192\\.168\\.\\d{1,3}\\.\\d{1,3}"),
				Pattern.compile("172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}"),
				Pattern.compile("169\\.254\\.\\d{1,3}\\.\\d{1,3}"),
				Pattern.compile("127\\.\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}") };

		/**
		 * Comma delimited list of secured remote addresses. Expressed with regular expressions.
		 * <p>
		 * Default value : 10\.\d{1,3}\.\d{1,3}\.\d{1,3}, 192\.168\.\d{1,3}\.\d{1,3},
		 * 172\\.(?:1[6-9]|2\\d|3[0-1]).\\d{1,3}.\\d{1,3}, 169\.254\.\d{1,3}\.\d{1,3},
		 * 127\.\d{1,3}\.\d{1,3}\.\d{1,3}
		 * 
		 * @param comaDelimitedSecuredRemoteAddresses
		 */
		public void setSecuredRemoteAdresses(final String comaDelimitedSecuredRemoteAddresses)
		{
			securedRemoteAddresses = commaDelimitedListToPatternArray(comaDelimitedSecuredRemoteAddresses);
		}
	}

	// Filter Config
	private Config config = new Config();

	/**
	 * Construct.
	 */
	public SecuredRemoteAddressRequestWrapperFactory()
	{
	}

	/**
	 * @return SecuredRemoteAddress and XForwarded filter specific config
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

	@Override
	public HttpServletRequest getWrapper(final HttpServletRequest request)
	{
		HttpServletRequest xRequest = super.getWrapper(request);

		if (log.isDebugEnabled())
		{
			log.debug("Incoming request uri=" + request.getRequestURI() + " with originalSecure='" +
				request.isSecure() + "', remoteAddr='" + request.getRemoteAddr() +
				"' will be seen with newSecure='" + xRequest.isSecure() + "'");
		}

		return xRequest;
	}

	@Override
	public boolean needsWrapper(final HttpServletRequest request)
	{
		return !request.isSecure() &&
			matchesOne(request.getRemoteAddr(), config.securedRemoteAddresses) == false;
	}

	/**
	 * If incoming remote address matches one of the declared IP pattern, wraps the incoming
	 * {@link HttpServletRequest} to override {@link HttpServletRequest#isSecure()} to set it to
	 * <code>true</code>.
	 */
	@Override
	public HttpServletRequest newRequestWrapper(final HttpServletRequest request)
	{
		return new HttpServletRequestWrapper(request)
		{
			@Override
			public boolean isSecure()
			{
				return true;
			}
		};
	}

	/**
	 * @param filterConfig
	 */
	public void init(final FilterConfig filterConfig)
	{
		String comaDelimitedSecuredRemoteAddresses = filterConfig.getInitParameter(SECURED_REMOTE_ADDRESSES_PARAMETER);
		if (comaDelimitedSecuredRemoteAddresses != null)
		{
			config.setSecuredRemoteAdresses(comaDelimitedSecuredRemoteAddresses);
		}
	}
}
