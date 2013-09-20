/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *	  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.wicket.protocol.http.request;

import org.apache.wicket.util.string.Strings;

import java.util.Arrays;
import java.util.List;

/**
 * UserAgent
 */
enum UserAgent {

	MOZILLA("Opera,AppleWebKit,Konqueror,Trident", Arrays.asList("Mozilla", "Gecko")),

	FIREFOX("Opera,AppleWebKit,Konqueror,Trident", Arrays.asList("Mozilla", "Gecko", "Firefox")),

	INTERNET_EXPLORER("Opera",
		Arrays.asList("Mozilla", "MSIE", "Windows"),
		Arrays.asList("Mozilla", "MSIE", "Trident"),
		Arrays.asList("Mozilla", "MSIE", "Mac_PowerPC"),
		Arrays.asList("Mozilla", "Windows", "Trident", "like Gecko")),

	OPERA(Arrays.asList("Opera")),

	CHROME(Arrays.asList("Mozilla", "Chrome", "AppleWebKit", "Safari")),

	SAFARI("Chrome", Arrays.asList("Mozilla", "AppleWebKit", "Safari")),

	KONQUEROR(Arrays.asList("Konqueror"));

	/**
	 * The values which are not allowed in the user agent.
	 */
	private final String[] notAllowedList;

	/**
	 * A list with strings which has to be in the user agent string.
	 */
	private final List<String>[] detectionStrings;

	/**
	 * Construct.
	 * 
	 * @param notAllowed
	 *			comma separated list with values which are not allowed in the user agent
	 * @param detectionStrings
	 *			a list with strings which has to be in the user agent string
	 */
	UserAgent(String notAllowed, List<String>... detectionStrings)
	{
		notAllowedList = Strings.split(notAllowed, ',');
		this.detectionStrings = detectionStrings;
	}

	/**
	 * Construct.
	 * 
	 * @param detectionStrings
	 *			list with string which has to be in the user agent string
	 */
	UserAgent(List<String>... detectionStrings)
	{
		this(null, detectionStrings);
	}

	/**
	 * @param userAgent
	 *			The user agent string
	 * @return Whether the user agent matches this enum or not
	 */
	public boolean matches(String userAgent)
	{
		if (userAgent == null)
		{
			return false;
		}

		if (notAllowedList != null)
		{
			for (String value : notAllowedList)
			{
				if (userAgent.contains(value))
				{
					return false;
				}
			}
		}

		for (List<String> detectionGroup : detectionStrings)
		{
			boolean groupPassed = true;
			for (String detectionString : detectionGroup)
			{
				if (!userAgent.contains(detectionString))
				{
					groupPassed = false;
					break;
				}
			}
			if (groupPassed)
			{
				return true;
			}
		}

		return false;
	}
}