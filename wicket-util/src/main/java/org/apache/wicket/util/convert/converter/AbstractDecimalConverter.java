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

/**
 * Base class for all converters of decimal numbers.
 * 
 * @author Jonathan Locke
 * @param <N>
 */
public abstract class AbstractDecimalConverter<N extends Number> extends AbstractNumberConverter<N>
{
	private static final long serialVersionUID = 1L;

	/**
	 * Creates a new {@link NumberFormat} for the given locale. The instance is later cached and is
	 * accessible through {@link #getNumberFormat(Locale)}
	 * 
	 * @param locale
	 * @return number format
	 */
	@Override
	protected NumberFormat newNumberFormat(final Locale locale)
	{
		return NumberFormat.getInstance(locale);
	}
}
