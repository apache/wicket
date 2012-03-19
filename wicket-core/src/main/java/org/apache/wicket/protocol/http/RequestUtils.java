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
import org.apache.wicket.request.mapper.parameter.PageParameters;
import org.apache.wicket.util.string.Strings;

/**
 * Wicket Http specific utilities class.
 */
public final class RequestUtils
{
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

	/**
	 * Remove occurrences of ".." from the path
	 * 
	 * @param path
	 * @return path string with double dots removed
	 */
	public static String removeDoubleDots(String path)
	{
		String[] segments = Strings.split(path, '/');
		List<String> newcomponents = new ArrayList<String>(Arrays.asList(segments));

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
		final StringBuilder result;
		if (requestPath.endsWith("/"))
		{
			result = new StringBuilder(requestPath);
		}
		else
		{
			// Remove everything after last slash (but not slash itself)
			result = new StringBuilder(requestPath.substring(0, requestPath.lastIndexOf('/') + 1));
		}

		if (relativePagePath.startsWith("./"))
		{
			relativePagePath = relativePagePath.substring(2);
		}

		if (relativePagePath.startsWith("../"))
		{
			StringBuilder tempRelative = new StringBuilder(relativePagePath);

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

		if (Application.exists())
		{
			charsetName = Application.get().getRequestCycleSettings().getResponseRequestEncoding();
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

	/**
	 * @param request
	 *      the http servlet request to extract the charset from
	 * @return the request's charset
	 */
	public static Charset getCharset(HttpServletRequest request)
	{
		Charset charset = null;
		if (request != null)
		{
			String charsetName = request.getCharacterEncoding();
			if (charsetName != null)
			{
				charset = Charset.forName(charsetName);
			}
		}
		if (charset == null)
		{
			charset = getDefaultCharset();
		}
		return charset;
	}
}
