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
import java.math.BigInteger;
import java.util.Locale;

import org.apache.wicket.util.string.Strings;

/**
 * BigInteger converter
 * 
 * see IConverter
 */
public class BigIntegerConverter extends AbstractIntegerConverter<BigInteger>
{
	private static final long serialVersionUID = 1L;

	@Override
	protected Class<BigInteger> getTargetType()
	{
		return BigInteger.class;
	}

	@Override
	public BigInteger convertToObject(final String value, final Locale locale)
	{
		if (Strings.isEmpty(value))
		{
			return null;
		}

		final BigDecimal number = parse(value, null, null, locale);

		if (number == null)
		{
			return null;
		}

		return new BigInteger(number.toString());
	}
}
