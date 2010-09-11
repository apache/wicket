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
package org.apache.wicket.protocol.http;

import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.Application;
import org.apache.wicket.request.UrlDecoder;
import org.apache.wicket.request.cycle.RequestCycle;
import org.apache.wicket.request.http.WebResponse;
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * Wicket Http specific utilities class.
 */
public final class RequestUtils
{
	 // one year, maximum recommended cache duration in RFC-2616
	public static final int MAX_CACHE_DURATION = 60 * 60 * 24 * 365;

	/**
	 * Decode the provided queryString as a series of key/ value pairs and set them in the provided
	 * value map.
	 * 
	 * @param queryString
	 *            string to decode, uses '&' to separate parameters and '=' to separate key from
	 *            value
	 * @param params
	 *            parameters map to write the found key/ value pairs to
	 */
	public static void decodeParameters(String queryString, PageParameters params)
	{
		for (String paramTuple : Strings.split(queryString, '&'))
		{
			final String[] bits = Strings.split(paramTuple, '=');

			if (bits.length == 2)
			{
				params.add(UrlDecoder.QUERY_INSTANCE.decode(bits[0], getCurrentCharset()),
				                         UrlDecoder.QUERY_INSTANCE.decode(bits[1], getCurrentCharset()));
			}
			else
			{
				params.add(UrlDecoder.QUERY_INSTANCE.decode(bits[0], getCurrentCharset()), "");
			}
		}
	}

// TODO review
// NO LONGER USED SINCE WE HAVE URL OBJECT
// /**
// * decores url parameters form <code>queryString</code> into <code>parameters</code> map
// *
// * @param queryString
// * @param parameters
// */
// public static void decodeUrlParameters(String queryString, Map<String, String[]> parameters)
// {
// Map<String, List<String>> temp = new HashMap<String, List<String>>();
// final String[] paramTuples = queryString.split("&");
// for (int t = 0; t < paramTuples.length; t++)
// {
// final String[] bits = paramTuples[t].split("=");
// final String key;
// final String value;
// key = WicketURLDecoder.QUERY_INSTANCE.decode(bits[0]);
// if (bits.length == 2)
// {
// value = WicketURLDecoder.QUERY_INSTANCE.decode(bits[1]);
// }
// else
// {
// value = "";
// }
// List<String> l = temp.get(key);
// if (l == null)
// {
// l = new ArrayList<String>();
// temp.put(key, l);
// }
// l.add(value);
// }
//
// for (Map.Entry<String, List<String>> entry : temp.entrySet())
// {
// String s[] = new String[entry.getValue().size()];
// entry.getValue().toArray(s);
// parameters.put(entry.getKey(), s);
// }
// }

	/**
	 * Remove occurrences of ".." from the path
	 * 
	 * @param path
	 * @return path string with double dots removed
	 */
	public static String removeDoubleDots(String path)
	{
		List<String> newcomponents = new ArrayList<String>(Arrays.asList(path.split("/")));

		for (int i = 0; i < newcomponents.size(); i++)
		{
			if (i < newcomponents.size() - 1)
			{
				// Verify for a ".." component at next iteration
				if ((newcomponents.get(i)).length() > 0 && newcomponents.get(i + 1).equals(".."))
				{
					newcomponents.remove(i);
					newcomponents.remove(i);
					i = i - 2;
					if (i < -1)
					{
						i = -1;
					}
				}
			}
		}
		String newpath = Strings.join("/", newcomponents.toArray(new String[newcomponents.size()]));
		if (path.endsWith("/"))
		{
			return newpath + "/";
		}
		return newpath;
	}

	/**
	 * Hidden utility class constructor.
	 */
	private RequestUtils()
	{
	}


	/**
	 * Calculates absolute path to url relative to another absolute url.
	 * 
	 * @param requestPath
	 *            absolute path.
	 * @param relativePagePath
	 *            path, relative to requestPath
	 * @return absolute path for given url
	 */
	public static String toAbsolutePath(final String requestPath, String relativePagePath)
	{
		final StringBuffer result;
		if (requestPath.endsWith("/"))
		{
			result = new StringBuffer(requestPath);
		}
		else
		{
			// Remove everything after last slash (but not slash itself)
			result = new StringBuffer(requestPath.substring(0, requestPath.lastIndexOf('/') + 1));
		}

		if (relativePagePath.startsWith("./"))
		{
			relativePagePath = relativePagePath.substring(2);
		}

		if (relativePagePath.startsWith("../"))
		{
			StringBuffer tempRelative = new StringBuffer(relativePagePath);

			// Go up through hierarchy until we find most common directory for both pathes.
			while (tempRelative.indexOf("../") == 0)
			{
				// Delete ../ from relative path
				tempRelative.delete(0, 3);

				// Delete last slash from result
				result.setLength(result.length() - 1);

				// Delete everyting up to last slash
				result.delete(result.lastIndexOf("/") + 1, result.length());
			}
			result.append(tempRelative);
		}
		else
		{
			// Pages are in the same directory
			result.append(relativePagePath);
		}
		return result.toString();
	}

	private static Charset getDefaultCharset()
	{
		String charsetName = null;

		Application application = Application.get();
		if (application != null)
		{
			charsetName = application.getRequestCycleSettings().getResponseRequestEncoding();
		}
		if (Strings.isEmpty(charsetName))
		{
			charsetName = "UTF-8";
		}
		return Charset.forName(charsetName);
	}

	private static Charset getCurrentCharset()
	{
		return RequestCycle.get().getRequest().getCharset();
	}

	public static Charset getCharset(HttpServletRequest request)
	{
		String charsetName = null;
		if (request != null)
		{
			charsetName = request.getCharacterEncoding();
		}
		if (Strings.isEmpty(charsetName))
		{
			Application application = Application.get();
			if (application != null)
			{
				charsetName = application.getRequestCycleSettings().getResponseRequestEncoding();
			}
		}
		if (Strings.isEmpty(charsetName))
		{
			charsetName = "UTF-8";
		}
		return Charset.forName(charsetName);
	}

	/**
	 * set all required headers to disable caching
	 * <p/>
	 * the following headers are set:
	 * <ul>
	 * <li>"Pragma" is set for older browsers only supporting HTTP 1.0.</li>
	 * <li>"Cache-Control" is set for modern browsers that support HTTP 1.1.</li>
	 * <li>"Expires" additionally sets the content expiry in the past which effectively prohibits caching</li>
	 * <li>"Date" is recommended in general</li>
	 * </ul>
	 *
	 * @param response web response
	 */
	public static void disableCaching(WebResponse response)
	{
		Args.notNull(response, "response");
		response.setDateHeader("Date", System.currentTimeMillis());
		response.setDateHeader("Expires", 0);
		response.setHeader("Pragma", "no-cache");
		response.setHeader("Cache-Control", "no-cache");
	}

	/**
	 * enable caching for the given response
	 * <p/>
	 * The [duration] is the maximum time in seconds until the response is invalidated from the cache. The
	 * maximum duration should not exceed one year, based on
	 * <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">RFC-2616</a>.
	 * <p/>
	 * The [cachePublic] flag will let you control if the response may be cached
	 * by public caches or just by the client itself. This sets the http response header
	 *
	 * <ul>
	 * <li><code>[Cache-Control: public]</code> if <code>cachePublic = true</code></li>
	 * <li><code>[Cache-Control: private]</code> if <code>cachePublic = false</code></li>
	 * </ul>
	 * <p/>
	 * Details on <code>Cache-Control</code> header can be found
	 *  <a href="http://palisade.plynt.com/issues/2008Jul/cache-control-attributes">here</a>
	 * or in <a href="http://www.w3.org/Protocols/rfc2616/rfc2616-sec14.html">RFC-2616</a>.
	 * <p/>
	 * Choose <code>cachePublic = false</code> wisely since setting <code>Cache-Control: private</code>
	 * may cause trouble with some versions of Firefox which will not cache SSL content at all. More details
	 * on this Firefox issue can be found <a href="http://blog.pluron.com/2008/07/why-you-should.html">here</a>.
	 * <p/>
	 * Never set <code>cachePublic=true</code> when the response is confidential or client-specific. You
	 * don't want to see your sensitive private data on some public proxy.
	 * <p/>
	 * Unless the response really is confidential / top-secret or client-specific the general advice is
	 * to always prefer <code>cachePublic=true</code> for best network performance.
	 *
	 * @param response
	 *            response that should be cacheable
	 * @param duration
	 *            duration in seconds that the response may be cached
	 *            (Integer.MAX_VALUE will select maximum duration based on RFC-2616)
	 * @param cachePublic
	 *            If <code>true</code> all caches are allowed to cache the response.
	 *            If <code>false</code> only the client may cache the response (if at all).
	 *
	 * @see RequestUtils#MAX_CACHE_DURATION
	 */
	public static void enableCaching(WebResponse response, int duration, boolean cachePublic)
	{
		Args.notNull(response, "response");

		if(duration < 0)
			throw new IllegalArgumentException("duration must be a positive value");

		// do not exceed the maximum recommended value from RFC-2616
		if(duration > MAX_CACHE_DURATION)
			duration = MAX_CACHE_DURATION;

		// Get current time
		long now = System.currentTimeMillis();

		// Time of message generation
		response.setDateHeader("Date", now);

		// Time for cache expiry = now + duration
		response.setDateHeader("Expires", now + (duration * 1000L));

		// Set caching scope
		String scope = cachePublic ? "public" : "private";

		// Enable caching and set max age
		response.setHeader("Cache-Control", scope + ", max-age=" + duration);

		// Let caches distinguish between compressed and uncompressed
		// versions of the resource so they can serve them properly
		response.setHeader("Vary", "Accept-Encoding");
	}
}
