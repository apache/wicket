/*
 * $Id$
 * $Revision$
 * $Date$
 *
 * ====================================================================
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.convert.converters.i18n;

import java.util.Locale;

import wicket.util.convert.ILocalizable;
import wicket.util.convert.converters.AbstractConverter;

/**
 * Base class for locale aware converters.
 *
 * @author Eelco Hillenius
 */
public abstract class AbstractLocaleConverter extends AbstractConverter
	implements ILocalizable
{
	/** The current locale. */
	private Locale locale;

	/**
	 * the pattern for conversion/ formatting.
	 */
	private final String pattern;

	/**
	 * whether the pattern is localized.
	 */
	private final boolean locPattern;

	/**
	 * Construct.
	 */
	public AbstractLocaleConverter()
	{
		this(null);
	}

	/**
	 * Construct. An unlocalized pattern is used for the convertion.
	 * 
	 * @param pattern The convertion pattern
	 */
	public AbstractLocaleConverter(String pattern)
	{
		this(pattern, false);
	}

	/**
	 * Construct.
	 * @param pattern The convertion pattern
	 * @param locPattern whether the pattern is localized
	 */
	public AbstractLocaleConverter(String pattern, boolean locPattern)
	{
		this.pattern = pattern;
		this.locPattern = locPattern;

	}

	/**
	 * gets the locale.
	 * @return the locale
	 */
	protected final Locale getLocale()
	{
		return locale;
	}

	/**
	 * sets the locale.
	 * @param locale the locale
	 */
	public final void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * Gets the pattern for conversion/ formatting.
	 * @return the pattern for conversion/ formatting
	 */
	protected final String getPattern()
	{
		return pattern;
	}

	/**
	 * Gets whether the pattern is localized.
	 * @return whether the pattern is localized
	 */
	protected final boolean isLocPattern()
	{
		return locPattern;
	}
}