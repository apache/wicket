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
package org.apache.wicket.protocol.http.servlet;

import java.util.List;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.servlet.http.HttpServletRequest;

import org.apache.wicket.util.lang.Generics;
import org.apache.wicket.util.string.Strings;

/**
 * Abstract base class for HttpServletRequestWrapper
 * 
 * @author Juergen Donnerstag
 */
public abstract class AbstractRequestWrapperFactory
{
	/**
	 * {@link Pattern} for a comma delimited string that support whitespace characters
	 */
	private static final Pattern commaSeparatedValuesPattern = Pattern.compile("\\s*,\\s*");

	private boolean enabled = true;

	/**
	 * Construct.
	 */
	public AbstractRequestWrapperFactory()
	{
	}

	/**
	 * 
	 * @return True, if filter is enabled
	 */
	public final boolean isEnabled()
	{
		return enabled;
	}

	/**
	 * Enable or disable the filter
	 * 
	 * @param enabled
	 */
	public final void setEnabled(boolean enabled)
	{
		this.enabled = enabled;
	}

	/**
	 * @param request
	 * @return Either return the request itself, or if needed a wrapper for the request
	 */
	public HttpServletRequest getWrapper(final HttpServletRequest request)
	{
		if (isEnabled() && needsWrapper(request))
		{
			return newRequestWrapper(request);
		}
		return request;
	}

	/**
	 * @param request
	 * @return True, if a wrapper is needed
	 */
	abstract boolean needsWrapper(final HttpServletRequest request);

	/**
	 * @param request
	 * @return Create a wrapper for the request
	 */
	abstract public HttpServletRequest newRequestWrapper(HttpServletRequest request);

	/**
	 * Convert a given comma delimited list of regular expressions into an array of compiled
	 * {@link Pattern}
	 * 
	 * @param commaDelimitedPatterns
	 * @return array of patterns (not <code>null</code>)
	 */
	public static Pattern[] commaDelimitedListToPatternArray(
		final String commaDelimitedPatterns)
	{
		String[] patterns = commaDelimitedListToStringArray(commaDelimitedPatterns);
		List<Pattern> patternsList = Generics.newArrayList();
		for (String pattern : patterns)
		{
			try
			{
				patternsList.add(Pattern.compile(pattern));
			}
			catch (PatternSyntaxException e)
			{
				throw new IllegalArgumentException("Illegal pattern syntax '" + pattern + "'", e);
			}
		}
		return patternsList.toArray(new Pattern[patternsList.size()]);
	}

	/**
	 * Convert a given comma delimited list of regular expressions into an array of String
	 * 
	 * @param commaDelimitedStrings
	 * @return array of patterns (non <code>null</code>)
	 */
	public static String[] commaDelimitedListToStringArray(final String commaDelimitedStrings)
	{
		if (Strings.isEmpty(commaDelimitedStrings))
		{
			return new String[0];
		}
		else
		{
			return commaSeparatedValuesPattern.split(commaDelimitedStrings);
		}
	}

	/**
	 * Convert an array of strings in a comma delimited string
	 * 
	 * @param stringList
	 * @return xxx
	 */
	public static String listToCommaDelimitedString(final List<String> stringList)
	{
		return Strings.join(", ", stringList);
	}

	/**
	 * 
	 * @param str
	 * @param patterns
	 * @return Return <code>true</code> if the given <code>str</code> matches at least one of the
	 *         given <code>patterns</code>.
	 */
	public static boolean matchesOne(final String str, final Pattern... patterns)
	{
		for (Pattern pattern : patterns)
		{
			if (pattern.matcher(str).matches())
			{
				return true;
			}
		}
		return false;
	}
}
