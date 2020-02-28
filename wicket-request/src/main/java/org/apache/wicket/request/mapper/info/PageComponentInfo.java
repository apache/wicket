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
package org.apache.wicket.request.mapper.info;

import org.apache.wicket.util.lang.Args;
import org.apache.wicket.util.string.Strings;

/**
 * Encapsulates both page and component info. Rendered in form of
 * &lt;pageInfo&gt;-&lt;componentInfo&gt;
 * 
 * @author Matej Knopp
 */
public class PageComponentInfo
{
	private static final char SEPARATOR = '-';

	private final PageInfo pageInfo;

	private final ComponentInfo componentInfo;

	/**
	 * Construct.
	 * 
	 * @param pageInfo
	 * @param componentInfo
	 */
	public PageComponentInfo(final PageInfo pageInfo, final ComponentInfo componentInfo)
	{
		Args.notNull(pageInfo, "pageInfo");

		this.pageInfo = pageInfo;
		this.componentInfo = componentInfo;
	}

	/**
	 * @return page info instance
	 */
	public PageInfo getPageInfo()
	{
		return pageInfo;
	}

	/**
	 * @return component info instance or <code>null</code>
	 */
	public ComponentInfo getComponentInfo()
	{
		return componentInfo;
	}

	/**
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		if (pageInfo != null)
		{
			result.append(pageInfo.toString());
		}
		if (componentInfo != null)
		{
			result.append(SEPARATOR);
			result.append(componentInfo);
		}

		return result.toString();
	}

	/**
	 * Parses the given string
	 * 
	 * @param s
	 * @return {@link PageComponentInfo} or <code>null</code> if the string is not in valid format.
	 */
	public static PageComponentInfo parse(final String s)
	{
		if (Strings.isEmpty(s))
		{
			return null;
		}

		final PageInfo pageInfo;
		final ComponentInfo componentInfo;

		int i = s.indexOf(SEPARATOR);
		if (i == -1)

		{
			pageInfo = PageInfo.parse(s);
			componentInfo = null;
		}
		else
		{
			pageInfo = PageInfo.parse(s.substring(0, i));
			componentInfo = ComponentInfo.parse(s.substring(i + 1));
		}

		if (pageInfo == null)
		{
			return null;
		}

		return new PageComponentInfo(pageInfo, componentInfo);
	}
}
