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
package org.apache.wicket.examples.requestmapper;

import java.util.Locale;

/**
 * A helper class to deal with {@link Locale} as string
 * 
 * @author mgrigorov
 */
public class LocaleHelper
{
	static Locale parseLocale(final String localeAsString)
	{
		return parseLocale(localeAsString, null);
	}

	static Locale parseLocale(final String localeAsString, final Locale defaultLocale)
	{
		Locale result;

		final int idxOfUnderbar = localeAsString.indexOf('_');
		if (idxOfUnderbar > 0)
		{
			String lang = localeAsString.substring(0, idxOfUnderbar);
			String country = localeAsString.substring(idxOfUnderbar + 1);
			result = new Locale(lang, country);
		}
		else
		{
			String lang = localeAsString;

			result = new Locale(lang);
		}

		return result;
	}

	/**
	 * Checks whether the passed parameter can be parsed to an existing locale
	 * 
	 * @param localeCandidate
	 * @return
	 */
	static boolean isLocale(String localeCandidate)
	{
		boolean isLocale = false;

		Locale locale = parseLocale(localeCandidate);
		if (locale != null)
		{
			for (final Locale l : Locale.getAvailableLocales())
			{
				if (l.equals(locale))
				{
					isLocale = true;
					break;
				}
			}
		}

		return isLocale;
	}
}
