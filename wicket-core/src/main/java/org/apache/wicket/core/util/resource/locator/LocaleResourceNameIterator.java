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
package org.apache.wicket.core.util.resource.locator;

import java.util.Iterator;
import java.util.Locale;

import org.apache.wicket.util.string.Strings;


/**
 * Given a Locale it'll iterate over all possible combinations of the attrs making up the Locale.
 * Starting the Locale provided to more 'weaker' combinations. The latest one will be no Locale in
 * which case an empty string will be returned.
 *
 * @author Juergen Donnerstag
 */
public class LocaleResourceNameIterator implements Iterator<String>
{
	/** The locale (see Session) */
	private final Locale locale;

	/** Internal state */
	private int state = 0;

	private final boolean strict;

	/**
	 * Construct.
	 *
	 * @param locale
	 * @param strict
	 */
	public LocaleResourceNameIterator(final Locale locale, boolean strict)
	{
		this.locale = locale;
		this.strict = strict;
	}

	/**
	 * @return Locale
	 */
	public Locale getLocale()
	{
		if (state == 1)
		{
			// Language, country, variation
			return locale;
		}
		else if (state == 2)
		{
			return new Locale(locale.getLanguage(), locale.getCountry());
		}
		else if (state == 3)
		{
			return new Locale(locale.getLanguage());
		}
		return null;
	}

	/**
	 *
	 * @see java.util.Iterator#hasNext()
	 */
	@Override
	public boolean hasNext()
	{
		int limit = 4;
		if (strict && locale != null)
		{
			// omit the last step
			limit = 3;
		}
		return (state < limit);
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	@Override
	public String next()
	{
		if (locale == null)
		{
			state = 999;
			return "";
		}

		// Get language and country, either of which may be the empty string
		final String language = locale.getLanguage();
		final String country = locale.getCountry();
		final String variant = locale.getVariant();

		// 1. If variant are available
		if (state == 0)
		{
			state++;
			if (!Strings.isEmpty(variant))
			{
				return '_' + language + '_' + country + '_' + variant;
			}
		}


		// 2. If country available
		if (state == 1)
		{
			state++;

			if (!Strings.isEmpty(country))
			{
				return '_' + language + '_' + country;
			}
		}


		// 4. If language is available
		if (state == 2)
		{
			state++;
			if (!Strings.isEmpty(language))
			{
				return '_' + language;
			}
		}

		// 4. The path only; without locale
		state++;
		return "";
	}

	/**
	 *
	 * @see java.util.Iterator#remove()
	 */
	@Override
	public void remove()
	{
	}
}
