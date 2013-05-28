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
package org.apache.wicket.request.resource;

import org.apache.wicket.request.IUrlRenderer;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.UrlUtils;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.util.lang.Args;

/**
 * A ResourceReference that can be used to point to a resource by using an Url. For example to a
 * resource residing in a CDN (Content Delivering Network) or context relative one.
 * 
 * @since 6.0
 */
public class UrlResourceReference extends ResourceReference
{
	/**
	 * An Url that knows how to render itself, so it doesn't need re-calculating in UrlRenderer. It
	 * should be rendered as is.
	 */
	private static class CalculatedUrl extends Url implements IUrlRenderer
	{
		private CalculatedUrl(Url original)
		{
			super(original);
		}

		@Override
		public String renderFullUrl(Url url, Url baseUrl)
		{
			StringMode mode = getStringMode(url);
			return url.toString(mode);
		}

		@Override
		public String renderRelativeUrl(Url url, Url baseUrl)
		{
			return url.toString();
		}
	}

	/**
	 * The url to the resource.
	 */
	private final Url url;

	private boolean contextRelative = false;

	/**
	 * Constructor.
	 * 
	 * @param url
	 *            the url of the external resource
	 */
	public UrlResourceReference(final Url url)
	{
		super(asName(url));

		this.url = url;
	}

	private static String asName(Url externalUrl)
	{
		Args.notNull(externalUrl, "url");
		return externalUrl.toString();
	}

	/**
	 * @return the url of the external resource
	 */
	public final Url getUrl()
	{
		CalculatedUrl _url;

		if (contextRelative)
		{
			String contextRelative = UrlUtils.rewriteToContextRelative(url.toString(),
				RequestCycle.get());
			_url = new CalculatedUrl(Url.parse(contextRelative, url.getCharset()));
		}
		else
		{
			_url = new CalculatedUrl(url);
		}

		return _url;
	}

	/**
	 * @return {@code null} because this ResourceReference wont use an IResource to deliver the
	 *         content of the external resource. The browser will make a direct request to the
	 *         external url.
	 */
	@Override
	public final IResource getResource()
	{
		return null;
	}

	public UrlResourceReference setContextRelative(final boolean contextRelative)
	{
		if (contextRelative && (url.isFull() || url.isContextAbsolute()))
		{
			throw new IllegalStateException(String.format(
				"An absolute url '%s' cannot be rendered as context relative", url));
		}
		this.contextRelative = contextRelative;
		return this;
	}

	public boolean isContextRelative()
	{
		return contextRelative;
	}

	@Override
	public String toString()
	{
		return "UrlResourceReference{" + "url=" + url.toString(getStringMode(url)) +
			", contextRelative=" + contextRelative + '}';
	}

	private static Url.StringMode getStringMode(Url url)
	{
		final Url.StringMode mode;
		if (url.isFull())
		{
			mode = Url.StringMode.FULL;
		}
		else
		{
			mode = Url.StringMode.LOCAL;
		}
		return mode;
	}
}
