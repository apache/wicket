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
package org.apache.wicket.requestng;

import java.util.ArrayList;
import java.util.List;

/**
 * Takes care of rendering relative (or in future possibly absolute - depending on configuration)
 * URLs.
 * <p>
 * All Urls are rendered relative to the base Url. Base Url is normally Url of the page being
 * rendered. However, during Ajax request and redirect to buffer rendering the BaseUrl need to be
 * adjusted.
 * 
 * @author Matej Knopp
 */
public class UrlRenderer
{
	/**
	 * Construct.
	 * 
	 * @param base
	 *            base Url. All generated Urls will be relative to this Url.
	 */
	public UrlRenderer(Url base)
	{
		if (base == null)
		{
			throw new IllegalArgumentException("Argument 'base' may not be null.");
		}
		this.baseUrl = base;
	}

	/**
	 * Sets the base Url. All generated URLs will be relative to this Url.
	 * 
	 * @param base
	 */
	public void setBaseUrl(Url base)
	{
		if (base == null)
		{
			throw new IllegalArgumentException("Argument 'base' may not be null.");
		}
		this.baseUrl = base;
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

	private Url baseUrl;

	/**
	 * Renders the Url relative to currently set Base Url. 
	 * @param url
	 * @return Url rendered as string
	 */
	public String renderUrl(Url url)
	{
		if (url == null)
		{
			throw new IllegalArgumentException("Argument 'url' may not be null.");
		}
		List<String> baseUrlSegments = getBaseUrl().getSegments();
		List<String> urlSegments = new ArrayList<String>(url.getSegments());
		
		int common = 0;
		for (String s : baseUrlSegments)
		{
			if (!urlSegments.isEmpty() && s.equals(urlSegments.get(0)))
			{
				++common;
				urlSegments.remove(0);
			}
		}
		
		List<String> newSegments = new ArrayList<String>();
		for (int i = common; i < baseUrlSegments.size(); ++i)
		{
			newSegments.add("..");
		}
		newSegments.addAll(urlSegments);
		
		return new Url(newSegments, url.getQueryParameters()).toString();
	}
}
