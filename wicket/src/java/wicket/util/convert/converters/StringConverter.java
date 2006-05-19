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

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import wicket.util.convert.ITypeConverter;

/**
 * Converts objects to Strings. Since the formatting of Strings may vary
 * depending on the type of object being converted, you can register
 * ITypeConverters for each kind of formatting you want to support. For example,
 * the default StringConverter class registers a set of converters which convert
 * from various types to String, including DateToStringConverter, which converts
 * from Objects of type Date to Strings.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class StringConverter extends AbstractConverter
{
	private static final long serialVersionUID = 1L;

	/** Maps value Classes to specific StringConverters. */
	private final Map classToConverter = new HashMap();
	{
		DateToStringConverter dateConverter = new DateToStringConverter();
		NumberToStringConverter numberConverter = new NumberToStringConverter();
		set(Date.class, dateConverter);
		set(java.sql.Date.class, dateConverter);
		set(Timestamp.class, dateConverter);
		set(Float.class, numberConverter);
		set(Double.class, numberConverter);
	}

	/**
	 * Converter that is to be used when no registered converter is found.
	 */
	private ITypeConverter defaultConverter = new ITypeConverter()
	{
		private static final long serialVersionUID = 1L;

		/**
		 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object,java.util.Locale)
		 */
		public Object convert(final Object value, Locale locale)
		{
			return value.toString();
		}
	};

	/**
	 * Construct.
	 */
	public StringConverter()
	{
	}

	/**
	 * Removes all registered string converters.
	 */
	public void clear()
	{
		classToConverter.clear();
	}

	/**
	 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object,java.util.Locale)
	 */
	public Object convert(final Object value, Locale locale)
	{
		// Null is always converted to null
		if (value == null)
		{
			return null;
		}

		// Catch all cases where value is already the right type
		if (value instanceof String)
		{
			return value;
		}

		// Get string converter for value's class
		final Class c = value.getClass();
		ITypeConverter converter = get(c);
		if (converter == null)
		{
			return defaultConverter.convert(value,locale);
		}

		// Use type converter to convert to value
		return converter.convert(value,locale);
	}

	/**
	 * Gets the type converter that is registered for class c.
	 * 
	 * @param c
	 *            The class to get the type converter for
	 * @return The type converter that is registered for class c or null if no
	 *         type converter was registered for class c
	 */
	public ITypeConverter get(final Class c)
	{
		return (ITypeConverter)classToConverter.get(c);
	}

	/**
	 * Gets the converter that is to be used when no registered converter is
	 * found.
	 * 
	 * @return the converter that is to be used when no registered converter is
	 *         found
	 */
	public final ITypeConverter getDefaultConverter()
	{
		return defaultConverter;
	}

	/**
	 * Removes the type converter currently registered for class c.
	 * 
	 * @param c
	 *            The class for which the converter registration should be
	 *            removed
	 * @return The converter that was registered for class c before removal or
	 *         null if none was registered
	 */
	public ITypeConverter remove(final Class c)
	{
		return (ITypeConverter)classToConverter.remove(c);
	}

	/**
	 * Registers a converter for use with class c.
	 * 
	 * @param converter
	 *            The converter to add
	 * @param c
	 *            The class for which the converter should be used
	 * @return The previous registered converter for class c or null if none was
	 *         registered yet for class c
	 */
	public ITypeConverter set(final Class c, final ITypeConverter converter)
	{
		if (converter == null)
		{
			throw new IllegalArgumentException("Converter cannot be null");
		}
		if (c == null)
		{
			throw new IllegalArgumentException("Class cannot be null");
		}
		return (ITypeConverter)classToConverter.put(c, converter);
	}

	/**
	 * Sets the converter that is to be used when no registered converter is
	 * found. This allows converter chaining.
	 * 
	 * @param defaultConverter
	 *            The converter that is to be used when no registered converter
	 *            is found
	 */
	public final void setDefaultConverter(final ITypeConverter defaultConverter)
	{
		this.defaultConverter = defaultConverter;
	}

    /**
     * @see wicket.util.convert.converters.AbstractConverter#getTargetType()
     */
    protected Class getTargetType()
    {
        return String.class;
    }
}