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
package org.apache._wicket.request.encoder.info;

import org.apache._wicket.IPage;
import org.apache.wicket.Application;
import org.apache.wicket.util.string.AppendingStringBuffer;
import org.apache.wicket.util.string.Strings;

/**
 * Possible string representation of PageInfo:
 * <ul>
 * <li>pageId
 * <li>pageId.version
 * <li>pageMap (only if pageMap contains a letter)
 * <li>.pageMap (for pageMap without any letter (just digits))
 * <li>pageMap.pageId.version
 * <li>pageMap.pageId (only if pageMap contains a letter)
 * </ul>
 * 
 * @author Matej Knopp
 */
public class PageInfo
{
	private final Integer pageId;
	private final Integer versionNumber;
	private final String pageMapName;

	/**
	 * Construct.
	 * 
	 * @param pageId
	 * @param versionNumber
	 * @param pageMapName
	 */
	public PageInfo(Integer pageId, Integer versionNumber, String pageMapName)
	{
		if ((pageId == null && versionNumber != null) ||
			(versionNumber == null && pageId != null))
		{
			throw new IllegalArgumentException(
				"Either both pageId and versionNumber must be null or none of them.");
		}
		this.pageId = pageId;
		this.versionNumber = versionNumber;
		this.pageMapName = pageMapName;
	}

	/**
	 * Construct.

	 * @param page
	 */
	public PageInfo(IPage page)
	{
		if (page == null)
		{
			throw new IllegalArgumentException("Argument 'page' may not be null.");
		}
		this.pageId = page.getPageId();
		this.versionNumber = page.getPageVersionNumber();
		this.pageMapName = page.getPageMapName();
	}
	
	/**
	 * @return page id
	 */
	public Integer getPageId()
	{
		return pageId;
	}

	/**
	 * @return page version number
	 */
	public Integer getVersionNumber()
	{
		return versionNumber;
	}

	/**
	 * @return pagemap name
 	 */
	public String getPageMapName()
	{
		return pageMapName;
	}

	private static char getPageInfoSeparator()
	{
		return '.';
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
		String pageMapName = this.pageMapName;

		// we don't need to encode the pageMapName when the pageId is unique
		// per session
		if (pageMapName != null && pageId != null && Application.exists() &&
			Application.get().getSessionSettings().isPageIdUniquePerSession())
		{
			pageMapName = null;
		}

		AppendingStringBuffer buffer = new AppendingStringBuffer(5);

		final boolean pmEmpty = Strings.isEmpty(pageMapName);
		final boolean pmContainsLetter = !pmEmpty && !isNumber(pageMapName);

		if (pageId != null && pmEmpty && versionNumber.intValue() == 0)
		{
			// pageId
			buffer.append(pageId);
		}
		else if (pageId != null && pmEmpty && versionNumber.intValue() != 0)
		{
			// pageId.version
			buffer.append(pageId);
			buffer.append(getPageInfoSeparator());
			buffer.append(versionNumber);
		}
		else if (pageId == null && pmContainsLetter)
		{
			// pageMap (must start with letter)
			buffer.append(pageMapName);
		}
		else if (pageId == null && !pmEmpty && !pmContainsLetter)
		{
			// .pageMap
			buffer.append(getPageInfoSeparator());
			buffer.append(pageMapName);
		}
		else if (pmContainsLetter && pageId != null && versionNumber.intValue() == 0)
		{
			// pageMap.pageId (pageMap must start with a letter)
			buffer.append(pageMapName);
			buffer.append(getPageInfoSeparator());
			buffer.append(pageId);
		}
		else if (!pmEmpty && pageId != null)
		{
			// pageMap.pageId.pageVersion
			buffer.append(pageMapName);
			buffer.append(getPageInfoSeparator());
			buffer.append(pageId);
			buffer.append(getPageInfoSeparator());
			buffer.append(versionNumber);
		}

		return buffer.toString();
	}

	/**
	 * Method that rigidly checks if the string consists of digits only.
	 * 
	 * @param string
	 * @return whether the string consists of digits only
	 */
	private static boolean isNumber(String string)
	{
		if (string == null || string.length() == 0)
		{
			return false;
		}
		for (int i = 0; i < string.length(); ++i)
		{
			if (Character.isDigit(string.charAt(i)) == false)
			{
				return false;
			}
		}
		return true;
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
	 * 
	 * @param src
	 * @return page insfo instance or <code>null</code> if the string couldn't have been parsed 
	 */
	public static PageInfo parse(String src)
	{
		if (src == null || src.length() == 0)
		{
			return null;
		}

		String segments[] = Strings.split(src, getPageInfoSeparator());

		if (segments.length > 3)
		{
			return null;
		}

		// go trhough the segments to determine if they don't contains invalid characters
		for (int i = 0; i < segments.length; ++i)
		{
			for (int j = 0; j < segments[i].length(); ++j)
			{
				char c = segments[i].charAt(j);
				if (!Character.isLetterOrDigit(c) && c != '-' && c != '_')
				{
					return null;
				}
			}
		}

		if (segments.length == 1 && isNumber(segments[0]))
		{
			// pageId
			return new PageInfo(Integer.valueOf(segments[0]), new Integer(0), null);
		}
		else if (segments.length == 2 && isNumber(segments[0]) && isNumber(segments[1]))
		{
			// pageId:pageVersion
			return new PageInfo(Integer.valueOf(segments[0]), Integer.valueOf(segments[1]), null);
		}
		else if (segments.length == 1 && !isNumber(segments[0]))
		{
			// pageMap (starts with letter)
			return new PageInfo(null, null, segments[0]);
		}
		else if (segments.length == 2 && segments[0].length() == 0)
		{
			// .pageMap
			return new PageInfo(null, null, segments[1]);
		}
		else if (segments.length == 2 && !isNumber(segments[0]) && isNumber(segments[1]))
		{
			// pageMap.pageId (pageMap starts with letter)
			return new PageInfo(Integer.valueOf(segments[1]), new Integer(0), segments[0]);
		}
		else if (segments.length == 3)
		{
			if (segments[2].length() == 0 && isNumber(segments[1]))
			{
				// we don't encode it like this, but we still should be able
				// to parse it
				// pageMapName.pageId.
				return new PageInfo(Integer.valueOf(segments[1]), new Integer(0), segments[0]);
			}
			else if (isNumber(segments[1]) && isNumber(segments[2]))
			{
				// pageMapName.pageId.pageVersion
				return new PageInfo(Integer.valueOf(segments[1]), Integer.valueOf(segments[2]),
					segments[0]);
			}
		}

		return null;
	}

};
