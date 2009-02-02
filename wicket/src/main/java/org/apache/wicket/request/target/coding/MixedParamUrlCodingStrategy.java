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

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.value.ValueMap;


/**
 * 
 * Url coding strategy for bookmarkable pages that encodes a set of given parameters
 * 
 * in the url's path path and the rest in the querystring.
 * 
 * <p>
 * Strategy looks for path-parameters whose name is read from an array of
 * 
 * names e.g. ["param0", "param1"]. Found parameters will be appended to the url in
 * 
 * the form <code>/mount-path/paramvalue0/paramvalue1</code>.
 * </p>
 * 
 * <p>
 * All other parameters are added as parameter in the form:
 * 
 * <code>/mount-path/paramvalue0?otherparam0=otherparamvalue0&otherparam1=otherparamvalue1</code>.
 * </p>
 * 
 * <p>
 * Decode is symmetric except for when a path parameter that is not at the end has no value during
 * encode.
 * 
 * For example, the names for the path parameters are: "a", "b" and "c". When "b" is
 * 
 * not specified upon encoding, but "c" is, upon a decode "b" will get the empty string
 * 
 * as value. When both "b" and "c" are missing on encode, the will not get a value during decode.
 * </p>
 * 
 * @author erik.van.oosten
 */
public class MixedParamUrlCodingStrategy extends BookmarkablePageRequestTargetUrlCodingStrategy
{
	private final String[] parameterNames;

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            mount path
	 * @param bookmarkablePageClass
	 *            class of mounted page
	 * @param pageMapName
	 *            name of pagemap
	 * @param parameterNames
	 *            the parameter names (not null)
	 */
	public MixedParamUrlCodingStrategy(String mountPath, Class bookmarkablePageClass,
		String pageMapName, String[] parameterNames)
	{
		super(mountPath, bookmarkablePageClass, pageMapName);
		this.parameterNames = parameterNames;
	}

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            mount path (not empty)
	 * @param bookmarkablePageClass
	 *            class of mounted page (not null)
	 * @param parameterNames
	 *            the parameter names (not null)
	 */
	public MixedParamUrlCodingStrategy(String mountPath, Class bookmarkablePageClass,
		String[] parameterNames)
	{
		super(mountPath, bookmarkablePageClass, PageMap.DEFAULT_NAME);
		this.parameterNames = parameterNames;
	}

	/** {@inheritDoc} */
	@Override
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{
		if (!url.endsWith("/"))
		{
			url.append("/");
		}

		Set parameterNamesToAdd = new HashSet(parameters.keySet());
		// Find index of last specified parameter
		boolean foundParameter = false;
		int lastSpecifiedParameter = parameterNames.length;
		while (lastSpecifiedParameter != 0 && !foundParameter)
		{
			foundParameter = parameters.containsKey(parameterNames[--lastSpecifiedParameter]);
		}

		if (foundParameter)
		{
			for (int i = 0; i <= lastSpecifiedParameter; i++)
			{
				String parameterName = parameterNames[i];
				final Object param = parameters.get(parameterName);
				String value = param instanceof String[] ? ((String[])param)[0] : (String)param;
				if (value == null)
				{
					value = "";
				}
				if (!url.endsWith("/"))
				{
					url.append("/");
				}
				url.append(urlEncodePathComponent(value));
				parameterNamesToAdd.remove(parameterName);
			}
		}

		if (!parameterNamesToAdd.isEmpty())
		{
			boolean first = true;
			final Iterator iterator = parameterNamesToAdd.iterator();
			while (iterator.hasNext())
			{
				url.append(first ? '?' : '&');
				String parameterName = (String)iterator.next();
				final Object param = parameters.get(parameterName);
				String value = param instanceof String[] ? ((String[])param)[0] : (String)param;
				url.append(urlEncodeQueryComponent(parameterName)).append("=").append(
					urlEncodeQueryComponent(value));
				first = false;
			}
		}
	}

	/** {@inheritDoc} */
	@Override
	protected ValueMap decodeParameters(String urlFragment, Map urlParameters)
	{
		PageParameters params = new PageParameters();
		// Add all url parameters
		params.putAll(urlParameters);
		String urlPath = urlFragment;
		if (urlPath.startsWith("/"))
		{
			urlPath = urlPath.substring(1);
		}

		if (urlPath.length() > 0)
		{
			String[] pathParts = urlPath.split("/");
			if (pathParts.length > parameterNames.length)
			{
				throw new IllegalArgumentException(
					"Too many path parts, please provide sufficient number of path parameter names");
			}

			for (int i = 0; i < pathParts.length; i++)
			{
				params.put(parameterNames[i], urlDecodePathComponent(pathParts[i]));
			}
		}

		return params;
	}
}
