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

import java.util.Locale;

/**
 * Converts from Object to Integer, adding zero-padding.
 * 
 * @author Eelco Hillenius
 * @author Jonathan Locke
 * @author Al Maw
 */
public class ZeroPaddingIntegerConverter extends AbstractIntegerConverter<Integer>
{
	private static final long serialVersionUID = 1L;

	private final int zeroPadLength;

	/**
	 * Constructs this converter.
	 * 
	 * @param zeroPadLength
	 *            Minimum length of String to be outputted (will be zero-padded).
	 */
	public ZeroPaddingIntegerConverter(final int zeroPadLength)
	{
		this.zeroPadLength = zeroPadLength;
	}

	/**
	 * @see org.apache.wicket.util.convert.converter.AbstractNumberConverter#convertToString(java.lang.Object,
	 *      java.util.Locale)
	 */
	@Override
	public String convertToString(final Integer value, final Locale locale)
	{
		String result = super.convertToString(value, locale);

		while (result.length() < zeroPadLength)
		{
			result = "0" + result;
		}

		return result;
	}

	/**
	 * @see org.apache.wicket.util.convert.IConverter#convertToObject(java.lang.String,Locale)
	 */
	@Override
	public Integer convertToObject(final String value, final Locale locale)
	{
		final Number number = parse(value, Integer.MIN_VALUE, Integer.MAX_VALUE, locale);

		if (number == null)
		{
			return null;
		}

		return number.intValue();
	}

	/**
	 * @see org.apache.wicket.util.convert.converter.AbstractConverter#getTargetType()
	 */
	@Override
	protected Class<Integer> getTargetType()
	{
		return Integer.class;
	}
}