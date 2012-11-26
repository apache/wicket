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
package org.apache.wicket.request;

import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.PrependingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Takes care of rendering URLs.
 * <p>
 * Normally Urls are rendered relative to the base Url. Base Url is normally Url of the page being
 * rendered. However, during Ajax request and redirect to buffer rendering the BaseUrl needs to be
 * adjusted.
 * 
 * @author Matej Knopp
 * @author Igor Vaynberg
 */
public class UrlRenderer
{
	private static final Logger LOG = LoggerFactory.getLogger(UrlRenderer.class);

	private static final Map<String, Integer> PROTO_TO_PORT = new HashMap<String, Integer>();
	static
	{
		PROTO_TO_PORT.put("http", 80);
		PROTO_TO_PORT.put("https", 443);
	}

	private final Request request;
	private Url baseUrl;

	/**
	 * Construct.
	 * 
	 * @param request
	 *            Request that serves as the base for rendering urls
	 */
	public UrlRenderer(final Request request)
	{
		this.request = request;
		baseUrl = request.getClientUrl();
	}

	/**
	 * Sets the base Url. All generated URLs will be relative to this Url.
	 * 
	 * @param base
	 * @return original base Url
	 */
	public Url setBaseUrl(final Url base)
	{
		Args.notNull(base, "base");

		Url original = baseUrl;
		baseUrl = base;
		return original;
	}

	/**
	 * Returns the base Url.
	 * 
	 * @return base Url
	 */
	public Url getBaseUrl()
	{
		return baseUrl;
	}

	/**
	 * Renders the Url
	 * 
	 * @param url
	 * @return Url rendered as string
	 */
	public String renderUrl(final Url url)
	{
		final String renderedUrl;
		if (shouldRenderAsFull(url))
		{
			if (url.isAbsolute() == false)
			{
				String relativeUrl = renderRelativeUrl(url);
				Url relative = Url.parse(relativeUrl, url.getCharset());
				relative.setPort(url.getPort());
				relative.setProtocol(url.getProtocol());
				renderedUrl = renderFullUrl(relative);
			}
			else
			{
				renderedUrl = renderFullUrl(url);
			}
		}
		else 
		{
			renderedUrl = renderRelativeUrl(url);
		}
		return renderedUrl;
	}

	/**
	 * Renders a full URL in the {@code protocol://hostname:port/path} format
	 * 
	 * @param url
	 * @return rendered URL
	 */
	public String renderFullUrl(final Url url)
	{
		final String protocol = resolveProtocol(url);
		final String host = resolveHost(url);
		final Integer port = resolvePort(url);

		final String path;
		if (url.isAbsolute())
		{
			path = url.toString();
		}
		else
		{
			Url base = new Url(baseUrl);
			base.resolveRelative(url);
			path = base.toString();
		}

		StringBuilder render = new StringBuilder();
		render.append(protocol);
		render.append("://");
		render.append(host);

		if ((port != null) && !port.equals(PROTO_TO_PORT.get(protocol)))
		{
			render.append(':');
			render.append(port);
		}

		if (url.isAbsolute() == false)
		{
			render.append(request.getContextPath());
			render.append(request.getFilterPath());
		}
		return Strings.join("/", render.toString(), path);
	}

	/**
	 * Gets port that should be used to render the url
	 * 
	 * @param url
	 *            url being rendered
	 * @return port or {@code null} if none is set
	 */
	protected Integer resolvePort(final Url url)
	{
		return choose(url.getPort(), baseUrl.getPort(), request.getClientUrl().getPort());
	}

	/**
	 * Gets the host name that should be used to render the url
	 * 
	 * @param url
	 *            url being rendered
	 * @return the host name or {@code null} if none is set
	 */
	protected String resolveHost(final Url url)
	{
		return choose(url.getHost(), baseUrl.getHost(), request.getClientUrl().getHost());
	}

	/**
	 * Gets the protocol that should be used to render the url
	 * 
	 * @param url
	 *            url being rendered
	 * @return the protocol or {@code null} if none is set
	 */
	protected String resolveProtocol(final Url url)
	{
		return choose(url.getProtocol(), baseUrl.getProtocol(), request.getClientUrl()
			.getProtocol());
	}

	/**
	 * Renders the Url relative to currently set Base Url.
	 * 
	 * This method is only intended for Wicket URLs, because the {@link Url} object represents part
	 * of URL after Wicket Filter.
	 * 
	 * For general URLs within context use {@link #renderContextRelativeUrl(String)}
	 * 
	 * @param url
	 * @return Url rendered as string
	 */
	public String renderRelativeUrl(final Url url)
	{
		Args.notNull(url, "url");

		List<String> baseUrlSegments = getBaseUrl().getSegments();
		List<String> urlSegments = new ArrayList<String>(url.getSegments());

		removeCommonPrefixes(request, baseUrlSegments);
		removeCommonPrefixes(request, urlSegments);

		List<String> newSegments = new ArrayList<String>();

		int common = 0;

		String last = null;

		for (String s : baseUrlSegments)
		{
			if (!urlSegments.isEmpty() && s.equals(urlSegments.get(0)))
			{
				++common;
				last = urlSegments.remove(0);
			}
			else
			{
				break;
			}
		}

		// we want the new URL to have at least one segment (other than possible ../)
		if ((last != null) && (urlSegments.isEmpty() || (baseUrlSegments.size() == common)))
		{
			--common;
			urlSegments.add(0, last);
		}

		int baseUrlSize = baseUrlSegments.size();
		if (common + 1 == baseUrlSize && urlSegments.isEmpty())
		{
			newSegments.add(".");
		}
		else
		{
			for (int i = common + 1; i < baseUrlSize; ++i)
			{
				newSegments.add("..");
			}
		}
		newSegments.addAll(urlSegments);

		String renderedUrl = new Url(newSegments, url.getQueryParameters()).toString();
		if (!renderedUrl.startsWith(".."))
		{
			// WICKET-4260
			renderedUrl = "./" + renderedUrl;
		}
		if (renderedUrl.endsWith(".."))
		{
			// WICKET-4401
			renderedUrl = renderedUrl + '/';
		}
		return renderedUrl;
	}

	/**
	 * Removes common prefixes like empty first segment, context path and filter path
	 *
	 * @param request
	 *      the current web request
	 * @param segments
	 *      the segments to clean
	 */
	private void removeCommonPrefixes(Request request, List<String> segments)
	{
		if (segments.isEmpty())
		{
			return;
		}

		if ("".equals(segments.get(0)))
		{
			LOG.debug("Removing an empty first segment from '{}'", segments);
			segments.remove(0);

			// try to remove context/filter path only if the Url starts with '/',
			//  i.e. has an empty segment in the beginning
			String contextPath = UrlUtils.normalizePath(request.getContextPath());
			if (contextPath != null && segments.isEmpty() == false)
			{
                String contextSegments[] = Strings.split(contextPath, '/');

                StringBuilder segmentPath = new StringBuilder();
                for (int i=0; i < (contextSegments.length - 1); i++) {
                    segmentPath.append('/' + segments.get(i));
                }

				if (contextPath.equals(UrlUtils.normalizePath(segmentPath.toString())))
				{
					LOG.debug("Removing the context path '{}' from '{}'", contextPath, segments);

                    for (int i=0; i < (contextSegments.length - 1); i++) {
                        segments.remove(0);
                    }
				}
			}

			String filterPath = UrlUtils.normalizePath(request.getFilterPath());
			if (filterPath != null && segments.isEmpty() == false)
			{
                String filterSegments[] = Strings.split(filterPath, '/');

                StringBuilder segmentPath = new StringBuilder();
                for (int i=0; i < (filterSegments.length - 1); i++) {
                    segmentPath.append('/' + segments.get(i));
                }

                if (filterPath.equals(UrlUtils.normalizePath(segmentPath.toString())))
                {
                    LOG.debug("Removing the filter path '{}' from '{}'", filterPath, segments);

                    for (int i=0; i < (filterSegments.length - 1); i++) {
                        segments.remove(0);
                    }
                }
            }
		}
	}

	/**
	 * Determines whether a URL should be rendered in its full form
	 * 
	 * @param url
	 * @return {@code true} if URL should be rendered in the full form
	 */
	protected boolean shouldRenderAsFull(final Url url)
	{
		Url clientUrl = request.getClientUrl();

		if (!Strings.isEmpty(url.getProtocol()) &&
			!url.getProtocol().equals(clientUrl.getProtocol()))
		{
			return true;
		}
		if (!Strings.isEmpty(url.getHost()) && !url.getHost().equals(clientUrl.getHost()))
		{
			return true;
		}
		if ((url.getPort() != null) && !url.getPort().equals(clientUrl.getPort()))
		{
			return true;
		}
		return false;
	}

	/**
	 * Renders the URL within context relative to current base URL.
	 * 
	 * @param url
	 * @return relative URL
	 */
	public String renderContextRelativeUrl(String url)
	{
		Args.notNull(url, "url");

		if (url.startsWith("/"))
		{
			url = url.substring(1);
		}

		PrependingStringBuffer buffer = new PrependingStringBuffer(url);
		for (int i = 0; i < getBaseUrl().getSegments().size() - 1; ++i)
		{
			buffer.prepend("../");
		}

		buffer.prepend(request.getPrefixToContextPath());

		return buffer.toString();
	}

	private static String choose(String value, final String fallback1, final String fallback2)
	{
		if (Strings.isEmpty(value))
		{
			value = fallback1;
			if (Strings.isEmpty(value))
			{
				value = fallback2;
			}
		}
		return value;
	}

	private static Integer choose(Integer value, final Integer fallback1, final Integer fallback2)
	{
		if (value == null)
		{
			value = fallback1;
			if (value == null)
			{
				value = fallback2;
			}
		}
		return value;
	}
}
