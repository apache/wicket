/*
 * $Id: DateConverter.java 5791 2006-05-20 00:32:57 +0000 (Sat, 20 May 2006)
 * joco01 $ $Revision$ $Date: 2006-05-25 22:45:15 +0000 (Thu, 25 May
 * 2006) $
 * 
 * ==============================================================================
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package wicket.util.convert.converters;

import java.text.DateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Converts from Object to Date.
 * 
 * @author Eelco Hillenius
 */
public class DateConverter extends AbstractConverter
{
	private static final long serialVersionUID = 1L;

	/** The date format to use for the specific locales (used as the key) */
	private final Map<Locale, DateFormat> dateFormats = new HashMap<Locale, DateFormat>();

	/**
	 * Specify whether or not date/time parsing is to be lenient. With lenient
	 * parsing, the parser may use heuristics to interpret inputs that do not
	 * precisely match this object's format. With strict parsing, inputs must
	 * match the object's format.
	 */
	private final boolean lenient;

	/**
	 * Construct. Lenient is false.
	 */
	public DateConverter()
	{
		super();
		lenient = false;
	}

	/**
	 * Construct.
	 * 
	 * @param lenient
	 *            when true, parsing is lenient. With lenient parsing, the
	 *            parser may use heuristics to interpret inputs that do not
	 *            precisely match this object's format. With strict parsing,
	 *            inputs must match the object's format.
	 */
	public DateConverter(boolean lenient)
	{
		super();
		this.lenient = lenient;
	}

	/**
	 * @see wicket.util.convert.IConverter#convertToObject(java.lang.String,Locale)
	 */
	public Object convertToObject(final String value, Locale locale)
	{
		return parse(getDateFormat(locale), value, locale);
	}

	/**
	 * @see wicket.util.convert.IConverter#convertToString(java.lang.String,
	 *      Locale)
	 */
	@Override
	public String convertToString(final Object value, Locale locale)
	{
		final DateFormat dateFormat = getDateFormat(locale);
		if (dateFormat != null)
		{
			return dateFormat.format(value);
		}
		return value.toString();
	}


	/**
	 * @param locale
	 * @return Returns the date format.
	 */
	public DateFormat getDateFormat(Locale locale)
	{
		if (locale == null)
		{
			locale = Locale.getDefault();
		}

		DateFormat dateFormat = dateFormats.get(locale);
		if (dateFormat == null)
		{
			dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, locale);
			dateFormat.setLenient(lenient);
			dateFormats.put(locale, dateFormat);
		}
		return dateFormat;
	}

	/**
	 * @param locale
	 * @param dateFormat
	 *            The dateFormat to set.
	 */
	public void setDateFormat(final Locale locale, final DateFormat dateFormat)
	{
		this.dateFormats.put(locale, dateFormat);
	}

	/**
	 * @see wicket.util.convert.converters.AbstractConverter#getTargetType()
	 */
	@Override
	protected Class<Date> getTargetType()
	{
		return Date.class;
	}
}