/*
 * $Id$ $Revision$
 * $Date$
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
package wicket.util.convert;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import ognl.OgnlOps;

/**
 * Default converter implementation.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class Converter implements IConverter, ILocalizable
{
	/** Maps Classes to ITypeConverters. */
	private final Map classToConverter = new HashMap();

	/**
	 * Converter that is to be used when no registered converter is found.
	 */
	private IConverter defaultConverter = new IConverter()
	{
		/**
		 * Converts the given value object to class c using OgnlOps.
		 * 
		 * @see wicket.util.convert.IConverter#convert(java.lang.Object,
		 *      java.lang.Class)
		 */
		public Object convert(Object value, Class c)
		{
			return OgnlOps.convertValue(value, c);
		}
	};

	/** The current locale. */
	private Locale locale = null;

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
	 * Removes all registered converters.
	 */
	public void clear()
	{
		classToConverter.clear();
	}

	/**
	 * Converts the given value to class c.
	 * 
	 * @param value
	 *            The value to convert
	 * @param c
	 *            The class to convert to
	 * @return The converted value
	 * 
	 * @see wicket.util.convert.IConverter#convert(java.lang.Object,
	 *      java.lang.Class)
	 */
	public Object convert(Object value, Class c)
	{
		// Null is always converted to null
		if (value == null)
		{
			return null;
		}

		// Class cannot be null
		if (c == null)
		{
			throw new IllegalArgumentException("Class cannot be null");
		}

		// Catch all cases where value is already the right type
		if (c.isAssignableFrom(value.getClass()))
		{
			return value;
		}

		// Get type converter for class
		ITypeConverter converter = get(c);
		if (converter == null)
		{
			if (defaultConverter instanceof ILocalizable)
			{
				((ILocalizable)defaultConverter).setLocale(locale);
			}
			return defaultConverter.convert(value, c);
		}

		// Set locale
		if (converter instanceof ILocalizable)
		{
			((ILocalizable)converter).setLocale(locale);
		}

		try
		{
			// Use type converter to convert to value
			return converter.convert(value);
		}
		catch (ConversionException e)
		{
			throw e.setConverter(this).setTypeConverter(converter).setLocale(locale).setTargetType(
					c).setSourceValue(value);
		}
		catch (Exception e)
		{
			throw new ConversionException(e).setConverter(this).setLocale(locale).setTargetType(c)
					.setSourceValue(value);
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
	public ITypeConverter get(Class c)
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
	public final IConverter getDefaultConverter()
	{
		return defaultConverter;
	}

	/**
	 * @see wicket.util.convert.ILocalizable#getLocale()
	 */
	public Locale getLocale()
	{
		return locale;
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
	public ITypeConverter remove(Class c)
	{
		return (ITypeConverter)classToConverter.remove(c);
	}

	/**
	 * Sets the converter that is to be used when no registered converter is
	 * found. This allows converter chaining.
	 * 
	 * @param defaultConverter
	 *            The converter that is to be used when no registered converter
	 *            is found
	 */
	public final void setDefaultConverter(IConverter defaultConverter)
	{
		this.defaultConverter = defaultConverter;
	}

	/**
	 * @see wicket.util.convert.ILocalizable#setLocale(java.util.Locale)
	 */
	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	/**
	 * @param value
	 *            The value to convert to a String
	 * @return The string
	 */
	public String toString(Object value)
	{
		return (String)convert(value, String.class);
	}
}