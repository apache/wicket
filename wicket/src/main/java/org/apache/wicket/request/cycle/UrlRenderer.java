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
package org.apache.wicket.request.cycle;

import java.util.ArrayList;
import java.util.List;

import org.apache.wicket.request.Request;
import org.apache.wicket.request.Url;
import org.apache.wicket.util.lang.Checks;
import org.apache.wicket.util.string.PrependingStringBuffer;

/**
 * Takes care of rendering relative (or in future possibly absolute - depending on configuration)
 * URLs.
 * <p>
 * All Urls are rendered relative to the base Url. Base Url is normally Url of the page being
 * rendered. However, during Ajax request and redirect to buffer rendering the BaseUrl needs to be
 * adjusted.
 * 
 * @author Matej Knopp
 */
public class UrlRenderer
{
	private Url baseUrl;

	/**
	 * Construct.
	 * 
	 * @param base
	 *            base Url. All generated Urls will be relative to this Url.
	 */
	public UrlRenderer(final Url base)
	{
		Checks.argumentNotNull(base, "base");

		baseUrl = base;
	}

	/**
	 * Sets the base Url. All generated URLs will be relative to this Url.
	 * 
	 * @param base
	 * @return original base Url
	 */
	public Url setBaseUrl(final Url base)
	{
		Checks.argumentNotNull(base, "base");

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
	 * Renders the Url relative to currently set Base Url.
	 * 
	 * This method is only intended for Wicket URLs, because the {@link Url} object represents part
	 * of URL after Wicket Filter.
	 * 
	 * For general URLs within context use {@link #renderContextPathRelativeUrl(String, Request)}
	 * 
	 * @param url
	 * @return Url rendered as string
	 */
	public String renderUrl(final Url url)
	{
		Checks.argumentNotNull(url, "url");

		if (url.isAbsolute())
		{
			return url.toString();
		}
		else
		{
			List<String> baseUrlSegments = getBaseUrl().getSegments();
			List<String> urlSegments = new ArrayList<String>(url.getSegments());

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
			if (last != null && (urlSegments.isEmpty() || baseUrlSegments.size() == common))
			{
				--common;
				urlSegments.add(0, last);
			}

			for (int i = common + 1; i < baseUrlSegments.size(); ++i)
			{
				newSegments.add("..");
			}
			newSegments.addAll(urlSegments);

			return new Url(newSegments, url.getQueryParameters()).toString();
		}
	}

	/**
	 * Renders the URL within context relative to current base URL.
	 * 
	 * @param url
	 * @param request
	 * @return relative URL
	 */
	public String renderContextPathRelativeUrl(final String url, final Request request)
	{
		PrependingStringBuffer buffer = new PrependingStringBuffer(url);
		for (int i = 0; i < baseUrl.getSegments().size() - 1; ++i)
		{
			buffer.prepend("../");
		}

		buffer.prepend(request.getPrefixToContextPath());

		return buffer.toString();
	}
}
