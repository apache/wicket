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

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for all number converters.
 * 
 * @author Jonathan Locke
 * @param <I>
 */
public abstract class AbstractIntegerConverter<I extends Number> extends AbstractNumberConverter<I>
{
	private static final long serialVersionUID = 1L;

	/** The date format to use */
	private final ConcurrentHashMap<Locale, NumberFormat> numberFormats = new ConcurrentHashMap<Locale, NumberFormat>();

	/**
	 * @param locale
	 *            The locale
	 * @return Returns the numberFormat.
	 */
	@Override
	public NumberFormat getNumberFormat(final Locale locale)
	{
		NumberFormat numberFormat = numberFormats.get(locale);
		if (numberFormat == null)
		{
			numberFormat = NumberFormat.getIntegerInstance(locale);
			numberFormat.setParseIntegerOnly(true);
			numberFormat.setGroupingUsed(false);
			NumberFormat tmpNumberFormat = numberFormats.putIfAbsent(locale, numberFormat);
			if (tmpNumberFormat != null)
			{
				numberFormat = tmpNumberFormat;
			}
		}
		return (NumberFormat)numberFormat.clone();
	}
}
