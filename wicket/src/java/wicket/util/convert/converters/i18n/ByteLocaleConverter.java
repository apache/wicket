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
 * Converts to and from Byte objects using the current locale and optionally a
 * pattern for it conversion.
 * 
 * @author Eelco Hillenius
 */
public class ByteLocaleConverter extends DecimalLocaleConverter
{
	/**
	 * Construct.
	 */
	public ByteLocaleConverter()
	{
	}

	/**
	 * Construct. An unlocalized pattern is used for the convertion.
	 * @param pattern The convertion pattern
	 */
	public ByteLocaleConverter(String pattern)
	{
		this(pattern, false);
	}

	/**
	 * Construct.
	 * @param pattern The convertion pattern
	 * @param patternIsLocalized Indicate whether the pattern is localized or not
	 */
	public ByteLocaleConverter(String pattern, boolean patternIsLocalized)
	{
		super(pattern, patternIsLocalized);
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
		if(Number.class.isAssignableFrom(c))
		{
			Number temp = getNumber(value);
			return (temp instanceof Byte) ? (Byte)temp : new Byte(temp.byteValue());
		}
		if(String.class.isAssignableFrom(c))
		{
			return toString(value);
		}
		throw new ConversionException(this +
				" cannot handle conversions of type " + c);
	}

	/**
	 * Converts the specified locale-sensitive input object into an output object
	 * of the specified type. This method will return values of type Byte.
	 * @param value The input object to be converted
	 * @param pattern The pattern is used for the convertion
	 * @return parsed object
	 * @throws ParseException
	 * @exception ConversionException if conversion cannot be performed
	 *               successfully
	 */
	protected Object parse(Object value, String pattern) throws ParseException
	{
		final Number parsed = (Number)super.parse(value, pattern);
		if (parsed.longValue() != parsed.byteValue())
		{
			throw new ConversionException("Supplied number is not of type Byte: " + parsed.longValue());
		}
		return new Byte(parsed.byteValue());
	}
}
