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
package org.apache.wicket.ng.request.mapper.info;

import org.apache.wicket.ng.request.component.RequestablePage;
import org.apache.wicket.ng.util.lang.Check;
import org.apache.wicket.ng.util.string.Strings;

/**
 * Possible string representation of PageInfo:
 * <ul>
 * <li>pageId
 * </ul>
 * 
 * @author Matej Knopp
 */
public class PageInfo
{
	private final Integer pageId;

	/**
	 * Construct.
	 * 
	 * @param pageId
	 */
	public PageInfo(Integer pageId)
	{
		this.pageId = pageId;
	}

	/**
	 * Construct.
	 */
	public PageInfo()
	{
		this((Integer)null);
	}

	/**
	 * Construct.
	 * 
	 * @param page
	 */
	public PageInfo(RequestablePage page)
	{
		Check.argumentNotNull(page, "page");

		this.pageId = page.getPageId();
	}

	/**
	 * @return page id
	 */
	public Integer getPageId()
	{
		return pageId;
	}

	/**
	 * <ul>
	 * <li>pageId
	 * <li>pageId.version
	 * <li>pageMap (only in if pagemap starts with a letter)
	 * <li>.pageMap
	 * <li>pageMap.pageId (only in if pageMap name starts with a letter)
	 * <li>pageMap.pageId.version
	 * </ul>
	 */
	@Override
	public String toString()
	{
		if (getPageId() == null)
		{
			return "";
		}
		else
		{
			return getPageId().toString();
		}
	}


	/**
	 * @param src
	 * @return page insfo instance or <code>null</code> if the string couldn't have been parsed
	 */
	public static PageInfo parse(String src)
	{
		if (Strings.isEmpty(src))
		{
			return new PageInfo();
		}
		else
		{
			try
			{
				return new PageInfo(Integer.valueOf(src));
			}
			catch (NumberFormatException e)
			{
				return null;
			}
		}
	}
};
