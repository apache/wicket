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
package org.apache.wicket.core.request.mapper;

import org.apache.wicket.request.IRequestMapper;
import org.apache.wicket.request.Url;
import org.apache.wicket.request.mapper.info.PageComponentInfo;
import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * Utility class that performs common functions used by {@link IRequestMapper}s.
 */
public class MapperUtils
{
	private MapperUtils()
	{
	}

	/**
	 * Attempts to parse a {@link Url.QueryParameter} which may hold {@link PageComponentInfo}.
	 *
	 * @param parameter
	 *		The {@link Url.QueryParameter} to parse.
	 *
	 * @return The parsed {@link PageComponentInfo}, or {@code null} if the parameter could not be parsed.
	 */
	public static PageComponentInfo parsePageComponentInfoParameter(final Url.QueryParameter parameter)
	{
		Args.notNull(parameter, "parameter");

		if (Strings.isEmpty(parameter.getName()) == false && Strings.isEmpty(parameter.getValue()) == true)
		{
			return PageComponentInfo.parse(parameter.getName());
		}

		return null;
	}

	/**
	 * Extracts the {@link PageComponentInfo} from the URL. The {@link PageComponentInfo} is encoded
	 * as the very first query parameter and the parameter consists of name only (no value).
	 *
	 * @param url
	 *
	 * @return PageComponentInfo instance if one was encoded in URL, <code>null</code> otherwise.
	 */
	public static PageComponentInfo getPageComponentInfo(final Url url)
	{
		Args.notNull(url, "url");

		for (Url.QueryParameter queryParameter : url.getQueryParameters())
		{
			PageComponentInfo pageComponentInfo = parsePageComponentInfoParameter(queryParameter);

			if (pageComponentInfo != null)
			{
				return pageComponentInfo;
			}
		}

		return null;
	}
}
