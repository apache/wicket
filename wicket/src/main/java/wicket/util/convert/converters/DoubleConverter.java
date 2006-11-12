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
package wicket.util.convert.converters;

import java.util.Locale;

import wicket.util.convert.IConverter;

/**
 * Converts from Object to Double.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public final class DoubleConverter extends AbstractDecimalConverter
{
	private static final long serialVersionUID = 1L;

	/**
	 * The singleton instance for a double converter
	 */
	public static final IConverter INSTANCE = new DoubleConverter();

	/**
	 * @see wicket.util.convert.IConverter#convertToObject(java.lang.Object,Locale)
	 */
	public Object convertToObject(final String value, Locale locale)
	{
		final Number number = parse(value, -Double.MAX_VALUE, Double.MAX_VALUE, locale);
		// Double.MIN is the smallest nonzero positive number, not the largest
		// negative number

		if (number == null)
		{
			return null;
		}

		return new Double(number.doubleValue());
	}

	/**
	 * @see wicket.util.convert.converters.AbstractConverter#getTargetType()
	 */
	@Override
	protected Class getTargetType()
	{
		return Double.class;
	}
}