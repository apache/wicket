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
package org.apache.wicket.util.convert;

import java.lang.ref.WeakReference;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.wicket.IConverterLocator;
import org.apache.wicket.util.convert.converters.BooleanConverter;
import org.apache.wicket.util.convert.converters.ByteConverter;
import org.apache.wicket.util.convert.converters.CharacterConverter;
import org.apache.wicket.util.convert.converters.DateConverter;
import org.apache.wicket.util.convert.converters.DoubleConverter;
import org.apache.wicket.util.convert.converters.FloatConverter;
import org.apache.wicket.util.convert.converters.IntegerConverter;
import org.apache.wicket.util.convert.converters.LongConverter;
import org.apache.wicket.util.convert.converters.ShortConverter;
import org.apache.wicket.util.convert.converters.SqlDateConverter;
import org.apache.wicket.util.convert.converters.SqlTimeConverter;
import org.apache.wicket.util.convert.converters.SqlTimestampConverter;
import org.apache.wicket.util.lang.Objects;


/**
 * Implementation of {@link IConverterLocator} interface, which locates
 * converters for a given type. It serves as a registry for {@link IConverter}
 * instances stored by type, and is the default locator for Wicket.
 * 
 * @see IConverterLocatorFactory
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class ConverterLocator implements IConverterLocator
{
	/**
	 * CoverterLocator that is to be used when no registered converter is found.
	 */
	private class DefaultConverter implements IConverter
	{
		private static final long serialVersionUID = 1L;

		private WeakReference/*<Class<?>>*/ type;

		/**
		 * Construct.
		 * 
		 * @param type
		 */
		public DefaultConverter(Class/* ? */type)
		{
			this.type = new WeakReference(type);
		}

		/**
		 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,
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
				if (((Class)type.get()) == String.class)
				{
					return "";
				}
				return null;
			}

			try
			{
				return Objects.convertValue(value, (Class)type.get());
			}
			catch (Exception e)
			{
				throw new ConversionException(e.getMessage(), e).setSourceValue(value);
			}
		}

		/**
		 * @see org.apache.wicket.util.convert.IConverter#convertToString(java.lang.Object,
		 *      java.util.Locale)
		 */
		public String convertToString(Object value, Locale locale)
		{
			if (value == null || "".equals(value))
			{
				return "";
			}

			return (String)Objects.convertValue(value, String.class);
		}
	}

	private static final long serialVersionUID = 1L;

	/** Maps Classes to ITypeConverters. */
	private final Map/* String, IConverter */classToConverter = new HashMap/* String, IConverter */();

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
		set(java.sql.Date.class, new SqlDateConverter());
		set(java.sql.Time.class, new SqlTimeConverter());
		set(java.sql.Timestamp.class, new SqlTimestampConverter());
	}

	/**
	 * Gets the type converter that is registered for class c.
	 * 
	 * @param c
	 *            The class to get the type converter for
	 * @return The type converter that is registered for class c or null if no
	 *         type converter was registered for class c
	 */
	public final IConverter get(Class/* ? */c)
	{
		return (IConverter)classToConverter.get(c.getName());
	}

	/**
	 * Converts the given value to class c.
	 * 
	 * @param type
	 *            Class to get the converter for.
	 * 
	 * @return The converter for the given type
	 * 
	 * @see org.apache.wicket.util.convert.IConverter#convert(java.lang.Object,
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
	 * Removes the type converter currently registered for class c.
	 * 
	 * @param c
	 *            The class for which the converter registration should be
	 *            removed
	 * @return The converter that was registered for class c before removal or
	 *         null if none was registered
	 */
	public final IConverter remove(Class/* ? */c)
	{
		return (IConverter)classToConverter.remove(c.getName());
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
	public final IConverter set(final Class/* ? */c, final IConverter converter)
	{
		if (converter == null)
		{
			throw new IllegalArgumentException("CoverterLocator cannot be null");
		}
		if (c == null)
		{
			throw new IllegalArgumentException("Class cannot be null");
		}
		return (IConverter)classToConverter.put(c.getName(), converter);
	}
}
