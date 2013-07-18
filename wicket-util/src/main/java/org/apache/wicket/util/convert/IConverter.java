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

import java.util.Date;
import java.util.Locale;

import org.apache.wicket.util.io.IClusterable;


/**
 * Converts input to output and vice versa. Converters are needed in web applications because we
 * have to switch between Java objects on the server and Strings in the browser output and input.
 * <p>
 * Output conversion, which is handled by {@link #convertToString(Object, Locale)}, is typically
 * used by components when they render, so that a date can be displayed as '12/12/2007'. Input
 * conversion, handled by {@link #convertToObject(String, Locale)}, is typically used by form
 * components to interpret incoming values Such values are strings as they are send as request
 * parameters from browsers. An incoming value could be the string '12/12/2007' which could be
 * translated to a corresponding {@link Date} object.
 * 
 * Notice that incoming value, when used by a FormComponent, will never be null because before
 * validation form components perform the required (see FormComponent.isRequired()) check which
 * errors out on null values. In the case the FormComponent is not required and the user enters a
 * null value converters will not be invoked because no type conversion is necessary.
 * </p>
 * 
 * @see org.apache.wicket.Component#getConverter(Class)
 * @see org.apache.wicket.Application#getConverterLocator()
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * 
 * @param <C>
 *            The object to convert from and to String
 */
public interface IConverter<C> extends IClusterable
{
	/**
	 * Converts the given {@link String} value
	 * 
	 * @param value
	 *            The string value to convert
	 * @param locale
	 *            The locale used to convert the value
	 * @return The converted value
	 * @throws ConversionException
	 *             if value could not be converted
	 */
	C convertToObject(String value, Locale locale) throws ConversionException;

	/**
	 * Converts the given value to a string.
	 * 
	 * @param value
	 *            The value to convert
	 * @param locale
	 *            The locale used to convert the value
	 * 
	 * @return The converted string value
	 */
	String convertToString(C value, Locale locale);
}