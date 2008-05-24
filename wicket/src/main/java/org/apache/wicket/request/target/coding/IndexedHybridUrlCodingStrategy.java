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

import org.apache.wicket.PageParameters;
import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.protocol.http.request.WebRequestCodingStrategy;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.value.ValueMap;

/**
 * {@link HybridUrlCodingStrategy} subclass that encodes the parameters in an indexed way like
 * {@link IndexedParamUrlCodingStrategy} does.
 * 
 * @author Matej Knopp
 */
public class IndexedHybridUrlCodingStrategy extends HybridUrlCodingStrategy
{

	/**
	 * 
	 * Construct.
	 * 
	 * @param mountPath
	 * @param pageClass
	 */
	public IndexedHybridUrlCodingStrategy(String mountPath, Class pageClass)
	{
		super(mountPath, pageClass);
	}

	protected void appendParameters(AppendingStringBuffer url, Map parameters)
	{
		int i = 0;
		while (parameters.containsKey(String.valueOf(i)))
		{
			String value = (String)parameters.get(String.valueOf(i));
			if (!url.endsWith("/"))
			{
				url.append("/");
			}
			url.append(urlEncodePathComponent(value)).append("/");
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
			url.append(WebRequestCodingStrategy.PAGEMAP).append("/").append(urlEncodePathComponent(pageMap))
					.append("/");
		}

		if (i != parameters.size())
		{
			throw new WicketRuntimeException(
					"Not all parameters were encoded. Make sure all parameter names are integers in consecutive order starting with zero. Current parameter names are: " +
							parameters.keySet().toString());
		}
	}

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

		String[] parts = urlFragment.split("/");
		for (int i = 0; i < parts.length; i++)
		{
			if (WebRequestCodingStrategy.PAGEMAP.equals(parts[i]))
			{
				i++;
				params.put(WebRequestCodingStrategy.PAGEMAP, WebRequestCodingStrategy
						.decodePageMapName(urlDecodePathComponent(parts[i])));
			}
			else
			{
				params.put(String.valueOf(i), urlDecodePathComponent(parts[i]));
			}
		}
		return params;
	}

}
