/*
 * $Id: CoverterLocator.java 5775 2006-05-19 18:00:21 +0000 (Fri, 19 May 2006)
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
package wicket.util.convert;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import wicket.IConverterLocator;
import wicket.util.convert.converters.BooleanConverter;
import wicket.util.convert.converters.ByteConverter;
import wicket.util.convert.converters.CharacterConverter;
import wicket.util.convert.converters.DateConverter;
import wicket.util.convert.converters.DoubleConverter;
import wicket.util.convert.converters.FloatConverter;
import wicket.util.convert.converters.IntegerConverter;
import wicket.util.convert.converters.LongConverter;
import wicket.util.convert.converters.ShortConverter;
import wicket.util.lang.Objects;

/**
 * Implementation of IConverter interface, which converts objects from one class
 * to another. This class allows specific type converters implementing the
 * ITypeConverter interface to be registered as conversion operations for
 * specific types. By default this class registers type converters for Date,
 * String and all Java primitive types and their wrapper classes.
 * <p>
 * To convert from a Double value to a String value you can use the generalized
 * converter interface:
 * 
 * <pre>
 * final IConverter converter = new CoverterLocatorFactory().newConverter();
 * converter.setLocale(Locale.US);
 * converter.convert(new Double(7.1), String.class);
 * </pre>
 * 
 * Or this can be accomplished by directly using the StringConverter type
 * conversion class (which is registered as a type converter on the IConverter
 * returned by the converter factory above).
 * 
 * <pre>
 * final StringConverter converter = new StringConverter(Locale.US);
 * converter.convert(new Double(7.1));
 * </pre>
 * 
 * When using Wicket, you should rarely need to use any of the conversion
 * classes directly. There are convenient validators and conversion features
 * built into Wicket that you can use directly.
 * 
 * @see IConverterLocatorFactory
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class ConverterLocator implements IConverterLocator
{
	private static final long serialVersionUID = 1L;

	/** Maps Classes to ITypeConverters. */
	private final Map<Class, IConverter> classToConverter = new HashMap<Class, IConverter>();

	/**
	 * CoverterLocator that is to be used when no registered converter is found.
	 */
	private class DefaultConverter implements IConverter
	{
		private Class type;

		/**
		 * Construct.
		 * 
		 * @param type
		 */
		public DefaultConverter(Class type)
		{
			this.type = type;
		}

		private static final long serialVersionUID = 1L;

		/**
		 * @see wicket.util.convert.IConverter#convertToObject(java.lang.String,
		 *      java.util.Locale)
		 */
		public Object convertToObject(String value, Locale locale)
		{
			if (value == null)
			{
				return null;
			}
			if ("".equals(value))
			{
				if (type == String.class)
				{
					return "";
				}
				return null;
			}

			try
			{
				return Objects.convertValue(value, type);
			}
			catch (Exception e)
			{
				throw new ConversionException(e.getMessage(), e).setSourceValue(value);
			}
		}

		/**
		 * @see wicket.util.convert.IConverter#convertToString(java.lang.Object,
		 *      java.util.Locale)
		 */
		public String convertToString(Object value, Locale locale)
		{
			if (value == null || "".equals(value))
			{
				return null;
			}

			return (String)Objects.convertValue(value, String.class);
		}
	};

	/**
	 * Constructor
	 */
	public ConverterLocator()
	{
		set(Boolean.TYPE, BooleanConverter.INSTANCE);
		set(Boolean.class, BooleanConverter.INSTANCE);
		set(Byte.TYPE, ByteConverter.INSTANCE);
		set(Byte.class, ByteConverter.INSTANCE);
		set(Character.TYPE, CharacterConverter.INSTANCE);
		set(Character.class, CharacterConverter.INSTANCE);
		set(Double.TYPE, DoubleConverter.INSTANCE);
		set(Double.class, DoubleConverter.INSTANCE);
		set(Float.TYPE, FloatConverter.INSTANCE);
		set(Float.class, FloatConverter.INSTANCE);
		set(Integer.TYPE, IntegerConverter.INSTANCE);
		set(Integer.class, IntegerConverter.INSTANCE);
		set(Long.TYPE, LongConverter.INSTANCE);
		set(Long.class, LongConverter.INSTANCE);
		set(Short.TYPE, ShortConverter.INSTANCE);
		set(Short.class, ShortConverter.INSTANCE);
		set(Date.class, new DateConverter());
		// TODO convert to string will go fine, but what about to object? It
		// will make a java.util.Date
		set(java.sql.Date.class, new DateConverter());
	}

	/**
	 * Converts the given value to class c.
	 * 
	 * @param type
	 *            Class to get the converter for.
	 * 
	 * @return The converter for the given type
	 * 
	 * @see wicket.util.convert.IConverter#convert(java.lang.Object,
	 *      java.lang.Class, Locale)
	 */
	public final IConverter getConverter(Class type)
	{
		// Null is always converted to null
		if (type == null)
		{
			return new DefaultConverter(String.class);
		}


		// Get type converter for class
		final IConverter converter = get(type);
		if (converter == null)
		{
			return new DefaultConverter(type);
		}
		return converter;
	}

	/**
	 * Gets the type converter that is registered for class c.
	 * 
	 * @param c
	 *            The class to get the type converter for
	 * @return The type converter that is registered for class c or null if no
	 *         type converter was registered for class c
	 */
	public final IConverter get(Class c)
	{
		return classToConverter.get(c);
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
	public final IConverter remove(Class c)
	{
		return classToConverter.remove(c);
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
	public final IConverter set(final Class c, final IConverter converter)
	{
		if (converter == null)
		{
			throw new IllegalArgumentException("CoverterLocator cannot be null");
		}
		if (c == null)
		{
			throw new IllegalArgumentException("Class cannot be null");
		}
		return classToConverter.put(c, converter);
	}
}