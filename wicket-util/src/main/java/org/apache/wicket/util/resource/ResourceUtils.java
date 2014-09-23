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
package org.apache.wicket.util.resource;

import java.util.Arrays;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.util.collections.ConcurrentHashSet;
import org.apache.wicket.util.string.Strings;

/**
 * 
 * @author Juergen Donnerstag
 */
public class ResourceUtils
{
	private static final Pattern LOCALE_PATTERN = Pattern.compile("_([a-z]{2})(_([A-Z]{2})(_([^_]+))?)?$");

	private final static Set<String> isoCountries = new ConcurrentHashSet<String>(
		Arrays.asList(Locale.getISOCountries()));

	private final static Set<String> isoLanguages = new ConcurrentHashSet<String>(
		Arrays.asList(Locale.getISOLanguages()));

	/**
	 * Construct.
	 */
	private ResourceUtils()
	{
	}

	/**
	 * Extract the locale from the filename
	 * 
	 * @param path
	 *            The file path
	 * @return The updated path, without the locale
	 */
	public static PathLocale getLocaleFromFilename(String path)
	{
		String extension = "";
		int pos = path.lastIndexOf('.');
		if (pos != -1)
		{
			extension = path.substring(pos);
			path = path.substring(0, pos);
		}

		String filename = Strings.lastPathComponent(path, '/');
		Matcher matcher = LOCALE_PATTERN.matcher(filename);
		if (matcher.find())
		{
			String language = matcher.group(1);
			String country = matcher.group(3);
			String variant = matcher.group(5);

			// did we find a language?
			if (language != null)
			{
				if (isoLanguages.contains(language) == false)
				{
					language = null;
					country = null;
					variant = null;
				}
			}

			// did we find a country?
			if ((language != null) && (country != null))
			{
				if (isoCountries.contains(country) == false)
				{
					country = null;
					variant = null;
				}
			}

			if (language != null)
			{
				pos = path.length() - filename.length() + matcher.start();
				String basePath = path.substring(0, pos) + extension;

				Locale locale = new Locale(language, country != null ? country : "",
					variant != null ? variant : "");

				return new PathLocale(basePath, locale);
			}
		} // else skip the whole thing... probably user specific underscores used

		return new PathLocale(path + extension, null);
	}

	/**
	 * 
	 */
	public static class PathLocale
	{
		/** */
		public final String path;

		/** */
		public final Locale locale;

		/**
		 * @param path
		 * @param locale
		 */
		public PathLocale(final String path, final Locale locale)
		{
			this.path = path;
			this.locale = locale;
		}
	}
}
