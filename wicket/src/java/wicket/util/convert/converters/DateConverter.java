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
package wicket.util.convert.converters;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Locale;

import wicket.util.convert.ConversionException;

/**
 * Converts from Object to Date.
 * 
 * @author Eelco Hillenius
 */
public final class DateConverter extends AbstractConverter
{
	/** The date format to use. */
	private DateFormat dateFormat;
    
    /**
	 * Constructor
	 */
	public DateConverter()
	{
	}
    
    /**
	 * Constructor
     * @param locale The locale for this converter
	 */
	public DateConverter(final Locale locale)
	{
        super(locale);
	}

	/**
	 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object)
	 */
	public Object convert(final Object value)
	{
		final String stringValue = value.toString();
		try
		{
			return getDateFormat().parse(stringValue);
		}
		catch (ParseException e)
		{
			throw new ConversionException("Cannot convert '" + stringValue + "' to Date", e);
		}
		catch (NumberFormatException e)
		{
			throw new ConversionException("Cannot convert '" + stringValue + "' to Date", e);
		}
	}

    /**
     * @return Returns the date format.
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
	 * @param dateFormat The dateFormat to set.
	 */
	public void setDateFormat(final DateFormat dateFormat)
	{
		this.dateFormat = dateFormat;
	}

	/**
	 * @see wicket.util.convert.ILocalizable#setLocale(java.util.Locale)
	 */
	public void setLocale(final Locale locale)
	{
		super.setLocale(locale);
		this.dateFormat = null;
	}
}
