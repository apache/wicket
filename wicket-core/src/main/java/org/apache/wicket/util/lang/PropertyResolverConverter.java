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
package org.apache.wicket.util.lang;

import java.util.Locale;

import org.apache.wicket.IClusterable;
import org.apache.wicket.IConverterLocator;
import org.apache.wicket.util.convert.IConverter;


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
	 * @param <I>
	 *            input object's type
	 * @param object
	 * @param clz
	 * @return The converted object
	 */
	public <T, I> Object convert(I object, Class<T> clz)
	{
		if (object == null)
		{
			return null;
		}
		if (clz.isAssignableFrom(object.getClass()))
		{
			return object;
		}
		IConverter<T> converter = converterSupplier.getConverter(clz);
		if (object instanceof String)
		{
			return converter.convertToObject((String)object, locale);
		}
		else if (clz == String.class)
		{
			return convertToString(object, locale);
		}
		else
		{
			try
			{
				return Objects.convertValue(object, clz);
			}
			catch (RuntimeException ex)
			{
				// ignore that try it the other way
			}
			// go through string to convert to the right object.
			String tmp = convertToString(object, locale);
			return converter.convertToObject(tmp, locale);
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
