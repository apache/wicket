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
package org.apache.wicket.util.resource.locator;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.wicket.WicketRuntimeException;
import org.apache.wicket.util.string.Strings;

/**
 * Contains the logic to locate a resource based on a path, a style (see
 * {@link org.apache.wicket.Session}), a locale and a extension strings. The full filename will be
 * built like: &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;.
 * <p>
 * Resource matches will be attempted in the following order:
 * <ol>
 * <li>1. &lt;path&gt;_&lt;style&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>2. &lt;path&gt;_&lt;locale&gt;.&lt;extension&gt;</li>
 * <li>3. &lt;path&gt;_&lt;style&gt;.&lt;extension&gt;</li>
 * <li>4. &lt;path&gt;.&lt;extension&gt;</li>
 * </ol>
 * <p>
 * Locales may contain a language, a country and a region or variant. Combinations of these
 * components will be attempted in the following order:
 * <ol>
 * <li>locale.toString() see javadoc for Locale for more details</li>
 * <li>&lt;language&gt;_&lt;country&gt;</li>
 * <li>&lt;language&gt;</li>
 * </ol>
 * <p>
 * Extensions may be a comma separated list of extensions, e.g. "properties,xml"
 * 
 * @author Juergen Donnerstag
 */
public class ResourceNameIterator implements Iterator<String>
{
	private static final Pattern LOCALE_PATTERN = Pattern.compile("_[a-zA-Z]{2}($|(?=_))");

	// The locale to search for the resource file
	private final Locale locale;

	// The extensions (comma separated) to search for the resource file
	private final String extensions;

	// The various iterators used to locate the resource file
	private final Iterator<String> styleIterator;
	private LocaleResourceNameIterator localeIterator;
	private Iterator<String> extenstionsIterator;

	// The latest exact Locale used
	private Locale currentLocale;

	private final Set<String> isoCountries = new HashSet<String>(
		Arrays.asList(Locale.getISOCountries()));

	private final Set<String> isoLanguages = new HashSet<String>(
		Arrays.asList(Locale.getISOLanguages()));

	/**
	 * Construct.
	 * 
	 * @param path
	 *            The path of the resource without extension
	 * @param style
	 *            A theme or style (see {@link org.apache.wicket.Session})
	 * @param locale
	 *            The Locale to apply
	 * @param extensions
	 *            the filname's extensions (comma separated)
	 */
	public ResourceNameIterator(String path, final String style, final Locale locale,
		final String extensions)
	{
		this.locale = locale;
		if ((extensions == null) && (path.indexOf('.') != -1))
		{
			this.extensions = Strings.afterLast(path, '.');
			path = Strings.beforeLast(path, '.');
		}
		else
		{
			this.extensions = extensions;
		}

		Matcher matcher = LOCALE_PATTERN.matcher(path);
		if (matcher.find())
		{
			String language = null;
			String country = null;
			String variant = null;
			int firstValidLocalePatternFragment = -1;
			do
			{
				String s = matcher.group().substring(1, 3);
				if (Character.isLowerCase(s.charAt(0)))
				{
					if (isoLanguages.contains(s))
					{
						language = s;
						firstValidLocalePatternFragment = matcher.start();
						break;
					}
				}
			}
			while (matcher.find());

			// did we find a language?
			if (language != null)
			{
				// check for country
				if (matcher.find())
				{
					do
					{
						String s = matcher.group().substring(1, 3);
						if (Character.isUpperCase(s.charAt(0)))
						{
							if (isoCountries.contains(s))
							{
								country = s;
								break;
							}
						}
					}
					while (matcher.find());
				}
				if (country != null)
				{
					// country found... just get the rest of the string for any variant
					if (matcher.find())
					{
						variant = path.substring(matcher.start());
					}
				}
				path = path.substring(0, firstValidLocalePatternFragment);
				localeIterator = new LocaleResourceNameIterator(path, new Locale(language,
					country != null ? country : "", variant != null ? variant : ""));
			} // else skip the whole thing... probably user specific underscores used
		}

		styleIterator = new StyleAndVariationResourceNameIterator(path, style, null);
	}

	/**
	 * Get the exact Locale which has been used for the latest resource path.
	 * 
	 * @return current Locale
	 */
	public final Locale getLocale()
	{
		return currentLocale;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext()
	{
		// Most inner loop. Loop through all extensions provided
		if (extenstionsIterator != null)
		{
			if (extenstionsIterator.hasNext() == true)
			{
				return true;
			}

			// If there are no more extensions, than return to the next outer
			// loop (locale), get the next value from that loop and start
			// over again with the first extension in the list.
			extenstionsIterator = null;
		}

		// 2nd inner loop: Loop through all Locale combinations
		if (localeIterator != null)
		{
			while (localeIterator.hasNext())
			{
				// Get the next Locale from the iterator and start the next
				// inner iterator over again.
				String newPath = localeIterator.next();
				currentLocale = localeIterator.getLocale();
				extenstionsIterator = new ExtensionResourceNameIterator(newPath, extensions);
				if (extenstionsIterator.hasNext() == true)
				{
					return true;
				}
			}
			localeIterator = null;
		}

		// Most outer loop: Loop through all combinations of styles and
		// variations
		while (styleIterator.hasNext())
		{
			String newPath = styleIterator.next();

			localeIterator = new LocaleResourceNameIterator(newPath, locale);
			while (localeIterator.hasNext())
			{
				newPath = localeIterator.next();
				currentLocale = localeIterator.getLocale();
				extenstionsIterator = new ExtensionResourceNameIterator(newPath, extensions);
				if (extenstionsIterator.hasNext() == true)
				{
					return true;
				}
			}
		}

		// No more combinations found. End of iteration.
		return false;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public String next()
	{
		if (extenstionsIterator != null)
		{
			return extenstionsIterator.next();
		}
		throw new WicketRuntimeException(
			"Illegal call of next(). Iterator not properly initialized");
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	public void remove()
	{
		// ignore
	}
}
