/*
 * $Id$ $Revision:
 * 1.7 $ $Date$
 * 
 * ==================================================================== Licensed
 * under the Apache License, Version 2.0 (the "License"); you may not use this
 * file except in compliance with the License. You may obtain a copy of the
 * License at
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

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import wicket.util.convert.ConversionException;
import wicket.util.convert.ILocalizable;
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
	/** Maps value Classes to specific StringConverters. */
	private final Map classToConverter = new HashMap();

	/**
	 * Constructor
	 *  
	 */
	public StringConverter()
	{
        set(Date.class, new DateToStringConverter());
        set(Byte.class, new NumberToStringConverter());
        set(Short.class, new NumberToStringConverter());
        set(Integer.class, new NumberToStringConverter());
        set(Long.class, new NumberToStringConverter());
        set(Float.class, new NumberToStringConverter());
        set(Double.class, new NumberToStringConverter());
	}

	/**
	 * Converter that is to be used when no registered converter is found.
	 */
	private ITypeConverter defaultConverter = new ITypeConverter()
	{
		/**
		 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object)
		 */
		public Object convert(final Object value)
		{
			return value.toString();
		}
	};

	/**
	 * Removes all registered string converters.
	 */
	public void clear()
	{
		classToConverter.clear();
	}

	/**
	 * @see wicket.util.convert.ITypeConverter#convert(java.lang.Object)
	 */
	public Object convert(final Object value)
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
			if (defaultConverter instanceof ILocalizable)
			{
				((ILocalizable)defaultConverter).setLocale(getLocale());
			}
			return defaultConverter.convert(value);
		}

		// Set locale
		if (converter instanceof ILocalizable)
		{
			((ILocalizable)converter).setLocale(getLocale());
		}

		try
		{
			// Use type converter to convert to value
			return converter.convert(value);
		}
		catch (ConversionException e)
		{
			throw e.setTypeConverter(this).setLocale(getLocale()).setTargetType(c).setSourceValue(value);
		}
		catch (Exception e)
		{
			throw new ConversionException(e).setTypeConverter(this).setLocale(getLocale())
					.setTargetType(c).setSourceValue(value);
		}
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
}