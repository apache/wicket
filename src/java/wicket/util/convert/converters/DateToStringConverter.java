/*
 * $Id$
 * $Revision$ $Date$
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
import java.util.Locale;

/**
 * Converts from Date to String.
 * 
 * @author Eelco Hillenius
 */
public final class DateToStringConverter extends AbstractConverter
{
	/** The date format to use */
	private DateFormat dateFormat;

	/**
	 * Constructor
	 */
	public DateToStringConverter()
	{
	}

	/**
	 * Constructor
	 * 
	 * @param locale
	 *            The locale for this converter
	 */
	public DateToStringConverter(final Locale locale)
	{
		super(locale);
	}

	/**
	 * @see wicket.util.convert.converters.AbstractConverter#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale)
	{
		super.setLocale(locale);
		dateFormat = null;
	}

	/**
	 * @return Returns the dateFormat.
	 */
	public final DateFormat getDateFormat()
	{
		if (dateFormat == null)
		{
			dateFormat = DateFormat.getDateInstance(DateFormat.SHORT, getLocale());
		}
		return dateFormat;
	}

	/**
	 * @param dateFormat
	 *            The numberFormat to set.
	 */
	public final void setDateFormat(final DateFormat dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	/**
	 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object)
	 */
	public Object convert(final Object value)
	{
		final DateFormat dateFormat = getDateFormat();
		if (dateFormat != null)
		{
			return dateFormat.format(value);
		}
		return value.toString();
	}

	/**
	 * @see wicket.util.convert.converters.AbstractConverter#getTargetType()
	 */
	protected Class getTargetType()
	{
		return String.class;
	}
}
