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


import java.text.ParseException;

import wicket.util.convert.ConversionException;

/**
 * Converts to and from Float objects using the current locale and optionally
 * a pattern for it conversion.
 *
 * @author Eelco Hillenius
 */
public final class FloatLocaleConverter extends DecimalLocaleConverter
{
	/**
	 * Construct.
	 */
	public FloatLocaleConverter()
	{
	}

	/**
	 * Construct. An unlocalized pattern is used for the convertion.
	 * @param pattern The convertion pattern
	 */
	public FloatLocaleConverter(String pattern)
	{
		this(pattern, false);
	}

	/**
	 * Construct.
	 * @param pattern The convertion pattern
	 * @param locPattern Indicate whether the pattern is localized or not
	 */
	public FloatLocaleConverter(String pattern, boolean locPattern)
	{
		super(pattern, locPattern);
	}

	/**
	 * @see wicket.util.convert.IConverter#convert(java.lang.Object, java.lang.Class)
	 */
	public Object convert(Object value, Class c)
	{
		if (value == null)
		{
			return null;
		}
		if(c == CONVERT_TO_DEFAULT_TYPE || Number.class.isAssignableFrom(c))
		{
			Number temp = getNumber(value);
			return (temp instanceof Float) ? (Float)temp : new Float(temp.floatValue());
		}
		else if(String.class.isAssignableFrom(c))
		{
			return toString(value);
		}
		throw new ConversionException(this +
				" cannot handle conversions of type " + c);
	}

	/**
	 * Converts the specified locale-sensitive input object into an output object
	 * of the specified type. This method will return Float value or throw
	 * exception if value can not be stored in the Float.
	 * @param value The input object to be converted
	 * @return converted object
	 * @throws ParseException
	 * @exception ConversionException if conversion cannot be performed
	 *               successfully
	 */
	protected Object parse(Object value) throws ParseException
	{
		final String pattern = getPattern();
		final Number parsed = (Number)super.parse(value, pattern);
		if (Math.abs(parsed.doubleValue() - parsed.floatValue()) > (parsed.floatValue() * 0.00001))
		{
			throw new ConversionException("Suplied number is not of type Float: " + parsed.longValue());
		}
		return new Float(parsed.floatValue());
	}
}
