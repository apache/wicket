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
package org.apache.wicket.util.convert.converter;

import java.math.BigDecimal;
import java.util.Locale;

import org.apache.wicket.util.convert.IConverter;


/**
 * Converts from Object to Double.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 */
public class DoubleConverter extends AbstractDecimalConverter<Double>
{
	private static final long serialVersionUID = 1L;

	// Double.MIN is the smallest nonzero positive number, not the largest
	// negative number
	private static final BigDecimal MIN_VALUE = new BigDecimal(-Double.MAX_VALUE);
	private static final BigDecimal MAX_VALUE = new BigDecimal(Double.MAX_VALUE);

	/**
	 * The singleton instance for a double converter
	 */
	public static final IConverter<Double> INSTANCE = new DoubleConverter();

	/**
	 * @see org.apache.wicket.util.convert.IConverter#convertToObject(String, java.util.Locale)
	 */
	@Override
	public Double convertToObject(final String value, final Locale locale)
	{
		final BigDecimal number = parse(value, MIN_VALUE, MAX_VALUE, locale);

		if (number == null)
		{
			return null;
		}

		return number.doubleValue();
	}

	/**
	 * @see org.apache.wicket.util.convert.converter.AbstractConverter#getTargetType()
	 */
	@Override
	protected Class<Double> getTargetType()
	{
		return Double.class;
	}
}