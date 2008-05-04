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
package org.apache.wicket.util.convert.converters;

import java.math.BigDecimal;
import java.util.Locale;

/**
 * BigDecimal converter
 * 
 * see IConverter
 * 
 */
public class BigDecimalConverter extends AbstractDecimalConverter<BigDecimal>
{
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<BigDecimal> getTargetType()
	{
		return BigDecimal.class;
	}

	public BigDecimal convertToObject(String value, Locale locale)
	{
		if (value == null || value.trim().equals(""))
			return null;

		final Number number = parse(value, -Double.MAX_VALUE, Double.MAX_VALUE, locale);
		if (number instanceof BigDecimal)
		{
			return (BigDecimal)number;
		}
		else if (number instanceof Double)
		{
			return new BigDecimal(number.doubleValue());
		}
		else if (number instanceof Long)
		{
			return new BigDecimal(number.longValue());
		}
		else if (number instanceof Float)
		{
			return new BigDecimal(number.floatValue());
		}
		else if (number instanceof Integer)
		{
			return new BigDecimal(number.intValue());
		}
		else
		{
			return new BigDecimal(value);
		}
	}
}
