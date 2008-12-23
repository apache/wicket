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
package org.apache.wicket.request.target.coding;

import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.wicket.IRequestTarget;
import org.apache.wicket.request.RequestParameters;
import org.apache.wicket.request.target.resource.ISharedResourceRequestTarget;
import org.apache.wicket.request.target.resource.SharedResourceRequestTarget;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;
import org.apache.wicket.util.value.ValueMap;

/**
 * Indexed url encoding for shared resources with optional query parameters
 * <p/>
 * for example, with this url
 * 
 * <pre>
 *   /mountpath/foo/bar/baz?name=joe&amp;languages=java&amp;languages=scala
 * </pre>
 * 
 * the parameters value map will be
 * <p/>
 * <table border="1" cellpadding="4px">
 * <thead>
 * <tr>
 * <th>Key</th>
 * <th>Value</th>
 * </tr>
 * </thead> <tbody>
 * <tr>
 * <td>"0"</th>
 * <td>"foo"</td>
 * </tr>
 * <tr>
 * <td>"1"</th>
 * <td>"bar"</td>
 * </tr>
 * <tr>
 * <td>"2"</th>
 * <td>"baz"</td>
 * </tr>
 * <tr>
 * <td>"name"</th>
 * <td>"joe"</td>
 * </tr>
 * <tr>
 * <td>"languages"</th>
 * <td>String[] { "java", "scala" }</td>
 * </tr>
 * </tbody>
 * </table>
 * 
 * so you can have urls like these
 * <code>/images/{imagename}.{format} /blog/2008/05/12/47-test-blog-entry.html</code> with
 * absolutely no effort.
 * <p/>
 * Can be used in WebApplication like this:
 * <code>mount(new IndexedSharedResourceCodingStrategy(path, sharedResourceKey);</code>
 * <p/>
 * The greatest benefit is that shared resource urls look like static resources for the browser.
 * This comes especially handy when utilizing browser caching. Also, the user will not realize the
 * resources are served dynamically and bookmarking is easy.
 * 
 */
public class IndexedSharedResourceCodingStrategy extends AbstractRequestTargetUrlCodingStrategy
{
	// resource key of of resource we map to
	private final String resourceKey;

	/**
	 * mount resource with specified key under indexed path
	 * 
	 * @param mountPath
	 *            path the resource will be mounted to
	 * @param resourceKey
	 *            key of the resource
	 */
	public IndexedSharedResourceCodingStrategy(String mountPath, String resourceKey)
	{
		super(mountPath);

		if (resourceKey == null)
		{
			throw new IllegalArgumentException("resource key must not be null");
		}
		this.resourceKey = resourceKey;
	}

	/**
	 * 
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#encode(org.apache.wicket.IRequestTarget)
	 */
	public CharSequence encode(final IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof ISharedResourceRequestTarget))
		{
			throw new IllegalArgumentException("This encoder can only be used with instances of " +
				ISharedResourceRequestTarget.class.getName());
		}

		final ISharedResourceRequestTarget target = (ISharedResourceRequestTarget)requestTarget;

		// create url to shared resource
		final AppendingStringBuffer url = new AppendingStringBuffer();
		url.append(getMountPath());

		final RequestParameters requestParameters = target.getRequestParameters();

		@SuppressWarnings("unchecked")
		Map<String, Object> params = requestParameters.getParameters();

		if (params != null)
		{
			params = new HashMap<String, Object>(params);

			int index = 0;

			// append indexed parameters to url:
			// these parameters are enumerated with the keys "0", "1", ...
			while (!params.isEmpty())
			{
				final String key = Integer.toString(index++);
				final Object value = params.get(key);

				// no more indexed parameters?
				if (value == null)
				{
					break;
				}

				// indexed parameters may not contain arrays
				if (value instanceof String[])
				{
					throw new IllegalArgumentException(
						"indexed parameter value must not be an array");
				}

				// remove indexed parameters from rest of parameters
				params.remove(key);

				// append indexed parameter to url
				url.append('/').append(urlEncodePathComponent(value.toString()));
			}

			// create query string from remaining parameters
			if (!params.isEmpty())
			{
				boolean first = true;

				// go through remaining parameters
				for (Map.Entry<String, Object> arg : params.entrySet())
				{
					final String key = urlEncodeQueryComponent(arg.getKey());
					final Object obj = arg.getValue();

					// for string arrays, create multiple query string parameters with all the
					// values
					if (obj instanceof String[])
					{
						for (String value : (String[])obj)
						{
							appendToQueryString(url, first, key, value);
							first = false;
						}
					}
					else
					{
						// for single query string value, just append it to url
						appendToQueryString(url, first, key, obj.toString());
						first = false;
					}
				}
			}
		}
		return url;
	}

	/**
	 * helper
	 * 
	 * @param url
	 * @param first
	 * @param key
	 * @param value
	 */
	private void appendToQueryString(AppendingStringBuffer url, boolean first, final String key,
		final String value)
	{
		url.append(first ? '?' : '&');
		url.append(key);
		url.append('=');
		url.append(urlEncodeQueryComponent(value));
	}

	/**
	 * 
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#decode(org.apache.wicket.request.RequestParameters)
	 */
	public IRequestTarget decode(final RequestParameters requestParameters)
	{
		if (requestParameters == null)
		{
			throw new IllegalArgumentException("request parameters must not be null");
		}

		// get resource path
		String path = requestParameters.getPath().substring(getMountPath().length());

		// cut away query string
		int startOfQueryString = path.indexOf("?");
		if (startOfQueryString != -1)
		{
			path = path.substring(0, startOfQueryString);
		}

		final ValueMap parameters = decodeParameters(path, requestParameters.getParameters());

		requestParameters.setParameters(parameters);
		requestParameters.setResourceKey(resourceKey);
		return new SharedResourceRequestTarget(requestParameters);
	}

	/**
	 * 
	 * @see org.apache.wicket.request.target.coding.AbstractRequestTargetUrlCodingStrategy#decodeParameters(java.lang.String,
	 *      java.util.Map)
	 */
	@Override
	protected ValueMap decodeParameters(String path, Map<String, Object> queryParameters)
	{
		final ValueMap parameters = new ValueMap(queryParameters);

		// add indexed parameters to parameters map
		if (!Strings.isEmpty(path))
		{
			final StringTokenizer tokens = new StringTokenizer(path, "/");

			int index = 0;
			while (tokens.hasMoreTokens())
			{
				parameters.add(Integer.toString(index++), tokens.nextToken());
			}
		}
		return parameters;
	}

	/**
	 * 
	 * @see org.apache.wicket.request.target.coding.IRequestTargetUrlCodingStrategy#matches(org.apache.wicket.IRequestTarget)
	 */
	public boolean matches(final IRequestTarget requestTarget)
	{
		if (!(requestTarget instanceof ISharedResourceRequestTarget))
		{
			return false;
		}

		final ISharedResourceRequestTarget target = (ISharedResourceRequestTarget)requestTarget;
		return resourceKey.equals(target.getRequestParameters().getResourceKey());
	}
}
