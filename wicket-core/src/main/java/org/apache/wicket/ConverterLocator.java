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
package org.apache.wicket;

import java.lang.ref.WeakReference;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.util.convert.ConversionException;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.convert.converter.BigDecimalConverter;
import org.apache.wicket.util.convert.converter.BigIntegerConverter;
import org.apache.wicket.util.convert.converter.BooleanConverter;
import org.apache.wicket.util.convert.converter.ByteConverter;
import org.apache.wicket.util.convert.converter.CalendarConverter;
import org.apache.wicket.util.convert.converter.CharacterConverter;
import org.apache.wicket.util.convert.converter.DateConverter;
import org.apache.wicket.util.convert.converter.DoubleConverter;
import org.apache.wicket.util.convert.converter.FloatConverter;
import org.apache.wicket.util.convert.converter.IntegerConverter;
import org.apache.wicket.util.convert.converter.LongConverter;
import org.apache.wicket.util.convert.converter.ShortConverter;
import org.apache.wicket.util.convert.converter.SqlDateConverter;
import org.apache.wicket.util.convert.converter.SqlTimeConverter;
import org.apache.wicket.util.convert.converter.SqlTimestampConverter;
import org.apache.wicket.util.lang.Objects;


/**
 * Implementation of {@link IConverterLocator} interface, which locates converters for a given type.
 * It serves as a registry for {@link IConverter} instances stored by type, and is the default
 * locator for Wicket.
 * 
 * @see IConverterLocator
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * 
 */
public class ConverterLocator implements IConverterLocator
{
	/**
	 * CoverterLocator that is to be used when no registered converter is found.
	 * 
	 * @param <C>
	 *            The object to convert from and to String
	 */
	private static class DefaultConverter<C> implements IConverter<C>
	{
		private static final long serialVersionUID = 1L;

		private final transient WeakReference<Class<C>> type;

		/**
		 * Construct.
		 * 
		 * @param type
		 */
		private DefaultConverter(Class<C> type)
		{
			this.type = new WeakReference<Class<C>>(type);
		}

		/**
		 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,
		 *      java.util.Locale)
		 */
		@Override
		public C convertToObject(String value, Locale locale)
		{
			if (value == null)
			{
				return null;
			}
			Class<C> theType = type.get();
			if ("".equals(value))
			{
				if (String.class.equals(theType))
				{
					return theType.cast("");
				}
				return null;
			}

			try
			{
				C converted = Objects.convertValue(value, theType);
				if (converted != null)
				{
					return converted;
				}

				if (theType.isInstance(value))
				{
					return theType.cast(value);
				}
			}
			catch (Exception e)
			{
				throw new ConversionException(e.getMessage(), e).setSourceValue(value);
			}

			throw new ConversionException("Could not convert value: " + value + " to type: " +
				theType.getName() + ". Could not find compatible converter.").setSourceValue(value);
		}

		/**
		 * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object,
		 *      java.util.Locale)
		 */
		@Override
		public String convertToString(C value, Locale locale)
		{
			if (value == null || "".equals(value))
			{
				return "";
			}

			try
			{
				return Objects.convertValue(value, String.class);
			}
			catch (RuntimeException e)
			{
				throw new ConversionException("Could not convert object of type: " +
					value.getClass() + " to string. Possible its #toString() returned null. " +
					"Either install a custom converter (see IConverterLocator) or " +
					"override #toString() to return a non-null value.", e).setSourceValue(value)
					.setConverter(this);
			}
		}
	}

	private static final long serialVersionUID = 1L;

	/** Maps Classes to ITypeConverters. */
	private final Map<String, IConverter<?>> classToConverter = new HashMap<String, IConverter<?>>();

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
		set(BigDecimal.class, new BigDecimalConverter());
		set(BigInteger.class, new BigIntegerConverter());
		set(Date.class, new DateConverter());
		set(java.sql.Date.class, new SqlDateConverter());
		set(java.sql.Time.class, new SqlTimeConverter());
		set(java.sql.Timestamp.class, new SqlTimestampConverter());
		set(Calendar.class, new CalendarConverter());
	}

	/**
	 * Gets the type converter that is registered for class c.
	 * 
	 * @param <C>
	 *            The object to convert from and to String
	 * @param c
	 *            The class to get the type converter for
	 * @return The type converter that is registered for class c or null if no type converter was
	 *         registered for class c
	 */
	public final <C> IConverter<C> get(Class<C> c)
	{
		return  (IConverter<C>) classToConverter.get(c.getName());
	}

	/**
	 * Converts the given value to class c.
	 * 
	 * @param type
	 *            Class to get the converter for.
	 * 
	 * @return The converter for the given type
	 * 
	 * @see org.apache.wicket.util.convert.IConverter#convertToObject(String, java.util.Locale)
	 */
	@Override
	public final <C> IConverter<C> getConverter(Class<C> type)
	{
		// Null is always converted to null
		if (type == null)
		{
			@SuppressWarnings("unchecked")
			IConverter<C> converter = (IConverter<C>)new DefaultConverter<String>(String.class);
			return converter;
		}

		// Get type converter for class
		final IConverter<C> converter = get(type);
		if (converter == null)
		{
			return new DefaultConverter<C>(type);
		}
		return converter;
	}

	/**
	 * Removes the type converter currently registered for class c.
	 * 
	 * @param c
	 *            The class for which the converter registration should be removed
	 * @return The converter that was registered for class c before removal or null if none was
	 *         registered
	 */
	public final IConverter<?> remove(Class<?> c)
	{
		return classToConverter.remove(c.getName());
	}

	/**
	 * Registers a converter for use with class c.
	 * 
	 * @param converter
	 *            The converter to add
	 * @param c
	 *            The class for which the converter should be used
	 * @return The previous registered converter for class c or null if none was registered yet for
	 *         class c
	 */
	public final IConverter<?> set(final Class<?> c, final IConverter<?> converter)
	{
		if (converter == null)
		{
			throw new IllegalArgumentException("CoverterLocator cannot be null");
		}
		if (c == null)
		{
			throw new IllegalArgumentException("Class cannot be null");
		}
		return classToConverter.put(c.getName(), converter);
	}
}
