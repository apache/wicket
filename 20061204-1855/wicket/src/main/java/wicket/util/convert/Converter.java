/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package wicket.util.convert;

import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import wicket.util.convert.converters.BooleanConverter;
import wicket.util.convert.converters.ByteConverter;
import wicket.util.convert.converters.CharacterConverter;
import wicket.util.convert.converters.DateConverter;
import wicket.util.convert.converters.DoubleConverter;
import wicket.util.convert.converters.FloatConverter;
import wicket.util.convert.converters.IntegerConverter;
import wicket.util.convert.converters.LongConverter;
import wicket.util.convert.converters.ShortConverter;
import wicket.util.convert.converters.StringConverter;
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
 * final IConverter converter = new ConverterFactory().newConverter();
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
 * @see IConverterFactory
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class Converter implements IConverter
{
	private static final long serialVersionUID = 1L;

	/** Maps Classes to ITypeConverters. */
	private final Map classToConverter = new HashMap();

	/**
	 * Converter that is to be used when no registered converter is found.
	 */
	private IConverter defaultConverter = new IConverter()
	{
		private static final long serialVersionUID = 1L;

		/**
		 * Converts the given value object to class c.
		 * 
		 * @see wicket.util.convert.IConverter#convert(java.lang.Object,
		 *      java.lang.Class)
		 */
		public Object convert(Object value, Class c)
		{
			if (value == null || "".equals(value))
			{
				return null;
			}

			try
			{
				return Objects.convertValue(value, c);
			}
			catch (Exception e)
			{
				throw new ConversionException(e.getMessage(), e).setSourceValue(value);
			}
		}

		public Locale getLocale()
		{
			return Locale.getDefault();
		}

		public void setLocale(Locale locale)
		{
		}
	};

	/** The current locale. */
	private Locale locale = null;

	/**
	 * Constructor
	 */
	public Converter()
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
		set(String.class, new StringConverter());
		set(Date.class, new DateConverter());
	}

	/**
	 * Constructor
	 * 
	 * @param locale
	 *            The locale
	 */
	public Converter(final Locale locale)
	{
		this();
		setLocale(locale);
	}

	/**
	 * Removes all registered converters.
	 */
	public final void clear()
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
	public final Object convert(Object value, Class c)
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
		final ITypeConverter converter = get(c);
		if (converter == null)
		{
			return defaultConverter.convert(value, c);
		}

		try
		{
			// Use type converter to convert to value
			return converter.convert(value, locale);
		}
		catch (ConversionException e)
		{
			throw e.setConverter(this);
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
	public final ITypeConverter get(Class c)
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
	public final Locale getLocale()
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
	public final ITypeConverter remove(Class c)
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
	public final ITypeConverter set(final Class c, final ITypeConverter converter)
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

		// Set locale on default converter
		defaultConverter.setLocale(locale);
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