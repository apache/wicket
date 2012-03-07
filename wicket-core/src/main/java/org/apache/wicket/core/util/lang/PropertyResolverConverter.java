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
package org.apache.wicket.core.util.lang;

import java.util.Locale;

import org.apache.wicket.util.io.IClusterable;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.util.convert.IConverter;
import org.apache.wicket.util.lang.Objects;


/**
 * @author jcompagner
 */
public class PropertyResolverConverter implements IClusterable
{
	private static final long serialVersionUID = 1L;


	private final IConverterLocator converterSupplier;
	private final Locale locale;

	/**
	 * Construct.
	 *
	 * @param converterSupplier
	 * @param locale
	 */
	public PropertyResolverConverter(IConverterLocator converterSupplier, Locale locale)
	{
		this.converterSupplier = converterSupplier;
		this.locale = locale;
	}

	/**
	 * @param <T>
	 *            target type
	 * @param object
	 * @param clz
	 * @return converted value of the type given, or null if the value cannot be converted to the
	 *         given type.
	 */
	public <T> T convert(Object object, Class<T> clz)
	{
		if (object == null)
		{
			return null;
		}
		if (clz.isAssignableFrom(object.getClass()))
		{
			@SuppressWarnings("unchecked")
			T result = (T)object;
			return result;
		}
		IConverter<T> converter = converterSupplier.getConverter(clz);
		if (object instanceof String)
		{
			return converter.convertToObject((String)object, locale);
		}
		else if (clz == String.class)
		{
			@SuppressWarnings("unchecked")
			T result = (T)convertToString(object, locale);
			return result;
		}
		else
		{
			T result;
			try
			{
				result = Objects.convertValue(object, clz);
			}
			catch (RuntimeException ex)
			{
				result = null;
			}
			if (result == null)
			{
				String tmp = convertToString(object, locale);
				result = converter.convertToObject(tmp, locale);
			}
			return result;
		}
	}

	protected <C> String convertToString(C object, Locale locale)
	{
		@SuppressWarnings("unchecked")
		Class<C> type = (Class<C>)object.getClass();

		IConverter<C> converterForObj = converterSupplier.getConverter(type);
		return converterForObj.convertToString(object, locale);
	}
}
