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

import java.util.Map;

import org.apache.wicket.PageMap;
import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.value.ValueMap;


/**
 * Url coding strategy for bookmarkable pages that encodes index based parameters.
 * 
 * Strategy looks for parameters whose name is an integer in an incremented order starting with
 * zero. Found parameters will be appended to the url in the form
 * /mount-path/paramvalue0/paramvalue1/paramvalue2
 * 
 * When decoded these parameters will once again be available under their index (
 * PageParameters.getString("0"); )
 * 
 * @author Igor Vaynberg (ivaynberg)
 */
public class IndexedParamUrlCodingStrategy extends BookmarkablePageRequestTargetUrlCodingStrategy
{
	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            mount path
	 * @param bookmarkablePageClass
	 *            class of mounted page
	 */
	public IndexedParamUrlCodingStrategy(String mountPath, Class bookmarkablePageClass)
	{
		super(mountPath, bookmarkablePageClass, PageMap.DEFAULT_NAME);
	}

	/**
	 * Construct.
	 * 
	 * @param mountPath
	 *            mount path
	 * @param bookmarkablePageClass
	 *            class of mounted page
	 * @param pageMapName
	 *            name of pagemap
	 */
	public IndexedParamUrlCodingStrategy(String mountPath, Class bookmarkablePageClass,
		String pageMapName)
	{
		super(mountPath, bookmarkablePageClass, pageMapName);
	}

	@Override
	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{
		int i = 0;
		while (parameters.containsKey(String.valueOf(i)))
		{
			String value = null;
			Object parameter = parameters.get(String.valueOf(i));
			if (parameter instanceof String[] && ((String[])parameter).length > 0)
			{
				value = ((String[])parameter)[0];
			}
			else
			{
				value = parameter.toString();
			}

			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(urlEncodePathComponent(value));
			i++;
		}

		String pageMap = (String)parameters.get(WebRequestCodingStrategy.PAGEMAP);
		if (pageMap != null)
		{
			i++;
			pageMap = WebRequestCodingStrategy.encodePageMapName(pageMap);
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(WebRequestCodingStrategy.PAGEMAP).append("/").append(
				urlEncodePathComponent(pageMap));
		}

		String intface = (String)parameters.get(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME);
		if (intface != null)
		{
			i++;
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME).append("/").append(
				urlEncodePathComponent(intface));
		}
		if (i != parameters.size())
		{
			throw new WicketRuntimeException(
				"Not all parameters were encoded. Make sure all parameter names are integers in consecutive order starting with zero. Current parameter names are: " +
					parameters.keySet().toString());
		}
	}

	@Override
	protected ValueMap decodeParameters(String urlFragment, Map urlParameters)
	{
		PageParameters params = new PageParameters();
		if (urlFragment == null)
		{
			return params;
		}
		if (urlFragment.startsWith("/"))
		{
			urlFragment = urlFragment.substring(1);
		}
		if (urlFragment.length() > 0 && urlFragment.endsWith("/"))
		{
			urlFragment = urlFragment.substring(0, urlFragment.length() - 1);
		}

		if (urlFragment.length() > 0)
		{
			String[] parts = urlFragment.split("/");
			for (int i = 0; i < parts.length; i++)
			{
				if (WebRequestCodingStrategy.PAGEMAP.equals(parts[i]))
				{
					i++;
					params.put(
						WebRequestCodingStrategy.PAGEMAP,
						WebRequestCodingStrategy.decodePageMapName(urlDecodePathComponent(parts[i])));
				}
				else if (WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME.equals(parts[i]))
				{
					i++;
					params.put(WebRequestCodingStrategy.INTERFACE_PARAMETER_NAME,
						urlDecodePathComponent(parts[i]));
				}
				else
				{
					params.put(String.valueOf(i), urlDecodePathComponent(parts[i]));
				}
			}
		}
		return params;
	}

}
