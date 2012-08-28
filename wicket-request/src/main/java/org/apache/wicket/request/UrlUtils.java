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

import org.apache.wicket.util.string.Strings;

/**
 * Various url utilities
 *
 * @author igor.vaynberg
 */
public class UrlUtils
{
	/**
	 * Constructor
	 */
	// TODO make it private in Wicket 7.0
	protected UrlUtils()
	{

	}

	/**
	 * Checks if the url is relative or absolute
	 *
	 * @param url
	 * @return <code>true</code> if url is relative, <code>false</code> otherwise
	 */
	public static boolean isRelative(final String url)
	{
		// the regex means "doesn't start with 'scheme://'"
		if ((url != null) && (url.startsWith("/") == false) && (!url.matches("^\\w+\\:\\/\\/.*")) &&
			!(url.startsWith("#")))
		{
			return true;
		}
		else
		{
			return false;
		}
	}

	/**
	 * Rewrites a relative url to be context relative, leaves absolute urls same.
	 *
	 * @param url
	 * @param requestCycle
	 * @return rewritten url
	 */
	public static String rewriteToContextRelative(String url, IRequestCycle requestCycle)
	{
		if (isRelative(url))
		{
			return requestCycle.getUrlRenderer().renderContextRelativeUrl(url);
		}
		else
		{
			return url;
		}
	}

	/**
	 * Makes sure the path starts with a slash and does not end with a slash. Empty or null paths
	 * are normalized into an empty string.
	 *
	 * @param path
	 *            path to normalize
	 * @return normalized path
	 */
	public static String normalizePath(String path)
	{
		if (Strings.isEmpty(path))
		{
			return "";
		}
		path = path.trim();
		if (!path.startsWith("/"))
		{
			path = "/" + path;
		}
		if (path.endsWith("/"))
		{
			path = path.substring(0, path.length() - 1);
		}
		return path;
	}
}