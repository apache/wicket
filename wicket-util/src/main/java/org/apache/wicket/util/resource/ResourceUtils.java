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
	/** The default postfix for minified names (ex: /css/mystyle.min.css) **/
	public static final String MIN_POSTFIX_DEFAULT = "min";
	/** Regular expression patter to extract the locale from the filename (ex: de_DE) **/
	private static final Pattern LOCALE_PATTERN = Pattern.compile("_([a-z]{2})(_([A-Z]{2})(_([^_]+))?)?$");
	/** Stores standard ISO country codes from {@code java.util.Locale} **/
	private final static Set<String> isoCountries = new ConcurrentHashSet<String>(
		Arrays.asList(Locale.getISOCountries()));
	/** Stores standard ISO language codes from {@code java.util.Locale} **/
	private final static Set<String> isoLanguages = new ConcurrentHashSet<String>(
		Arrays.asList(Locale.getISOLanguages()));
	
	/**
	 * Return the minified version for a given resource name.
	 * For example '/css/coolTheme.css' becomes '/css/coolTheme.min.css'
	 * 
	 * @param name
	 * 			The original resource name
	 * @param minPostfix
	 * 			The postfix to use for minified name
	 * @return The minified resource name
	 */
	public static String getMinifiedName(String name, String minPostfix)
	{
		String minifiedName;
		int idxOfExtension = name.lastIndexOf('.');
		final String dottedPostfix = "." + minPostfix;
		
		if (idxOfExtension > -1)
		{
			String extension = name.substring(idxOfExtension);
			final String baseName = name.substring(0, name.length() - extension.length() + 1);
			if (!dottedPostfix.equals(extension) && !baseName.endsWith(dottedPostfix + "."))
			{
				minifiedName = baseName + minPostfix + extension;
			} else
			{
				minifiedName = name;
			}
		} else
		{
			minifiedName = name + dottedPostfix;
		}
		return minifiedName;
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
